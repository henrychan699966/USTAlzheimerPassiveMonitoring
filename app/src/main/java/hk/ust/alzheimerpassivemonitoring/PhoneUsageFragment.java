package hk.ust.alzheimerpassivemonitoring;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class PhoneUsageFragment extends Fragment {

    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";

    private SQLiteCRUD database;
    private List<PhoneUsage> phoneUsageRecord;

    private String startingDate;
    private String endingDate;

    private PieChart mChart;

    public PhoneUsageFragment() {
        // Required empty public constructor
    }

    public static PhoneUsageFragment newInstance(String startDate, String endDate) {
        PhoneUsageFragment fragment = new PhoneUsageFragment();
        Bundle args = new Bundle();
        args.putString(START_DATE, startDate);
        args.putString(END_DATE, endDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            startingDate = getArguments().getString(START_DATE);
            endingDate = getArguments().getString(END_DATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_phone_usage, container, false);

        phoneUsageRecord = database.readPhoneUsage(endingDate);

        mChart = (PieChart) rootView.findViewById(R.id.puchart);
        mChart.getDescription().setEnabled(false);
        mChart.setCenterText("Phone Usage");
        mChart.setUsePercentValues(true);
        mChart.setRotationEnabled(false);
        mChart.setData(generatePhoneUsageData(startingDate, endingDate));

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        return rootView;
    }

    PieData generatePhoneUsageData(String s, String e) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        //sdf.setTimeZone(TimeZone.getDefault());
        long startDateMillis = 0;
        long endDateMillis = 0;

        Date startDate = null;
        Date endDate = null;
        try {
            startDate = sdf.parse(s);
            endDate = sdf.parse(e);
            startDateMillis = TimeUnit.MILLISECONDS.toDays(startDate.getTime());
            endDateMillis = TimeUnit.MILLISECONDS.toDays(endDate.getTime());
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        long totalMillis = endDate.getTime()-startDate.getTime() + TimeUnit.DAYS.toMillis(1);
        long totalDur = 0;
        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<Long> durationList = new ArrayList<>();

        for (long x = startDate.getTime(); x <= endDate.getTime(); x+= TimeUnit.DAYS.toMillis(1)) {
            String currentDate = sdf.format(x);
            List<PhoneUsage> pu = database.readPhoneUsage(currentDate);
            Log.e("PU",currentDate + " : " + Long.toString(x));
            if (pu != null) {
                for (PhoneUsage ap : pu) {
                    if (!nameList.contains(ap.getActivity())) {
                        nameList.add(ap.getActivity());
                        durationList.add(ap.getEndTime() - ap.getStartTime());
                    } else {
                        int index = nameList.indexOf(ap.getActivity());
                        durationList.set(index,durationList.get(index) - ap.getStartTime() + ap.getEndTime());
                    }
                    totalDur += (ap.getEndTime() - ap.getStartTime());
                }
            }
        }
        nameList.add("ScreenOff");
        durationList.add(totalMillis - totalDur);

        ArrayList<PieEntry> entries = new ArrayList<>();

        for (int i = 0; i < nameList.size(); i++) {
            entries.add(new PieEntry(TimeUnit.MILLISECONDS.toSeconds(durationList.get(i)),nameList.get(i)));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Phone Usage");

        dataSet.setDrawIcons(false);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.BLACK);

        return data;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        database = new SQLiteCRUD(context);
        database.openDatabase();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        database.closeDatabase();
    }

    public void updateDate(String s, String e) {
        startingDate = s;
        endingDate = e;
        mChart.setData(generatePhoneUsageData(startingDate, endingDate));
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }
}
