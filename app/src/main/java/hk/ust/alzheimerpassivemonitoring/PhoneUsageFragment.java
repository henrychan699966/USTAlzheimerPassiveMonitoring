package hk.ust.alzheimerpassivemonitoring;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PhoneUsageFragment extends Fragment {

    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";

    private SQLiteCRUD database;

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

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date startDate;
        Date endDate;
        long totalMillis;
        try {
            startDate = sdf.parse(s);
            endDate = sdf.parse(e);
            totalMillis = endDate.getTime()-startDate.getTime() + TimeUnit.DAYS.toMillis(1);
        } catch (ParseException e1) {
            startDate = new Date();
            endDate = startDate;
            totalMillis = TimeUnit.DAYS.toMillis(1);
        }

        final long[] totalDur = {0};
        final ArrayList<String> nameList = new ArrayList<>();
        final ArrayList<Long> durationList = new ArrayList<>();
        final ArrayList<String> socialAppList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.social_app)));

        final int PHONECALL_INDEX = 0;
        final int SOCIALAPP_INDEX = 1;
        final int OTHERS_INDEX = 2;

        nameList.add("PhoneCall");
        durationList.add(0L);
        nameList.add("SocialApp");
        durationList.add(0L);
        nameList.add("Others");
        durationList.add(0L);
        nameList.add("ScreenOff");

        final Date finalStartDate = startDate;
        final Date finalEndDate = endDate;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                for (long x = finalStartDate.getTime(); x <= finalEndDate.getTime(); x+= TimeUnit.DAYS.toMillis(1)) {
                    String currentDate = sdf.format(x);
                    List<PhoneUsage> pu = database.readPhoneUsage(currentDate);
                    Log.e("PU",currentDate + " : " + Long.toString(x));
                    if (pu != null) {
                        for (PhoneUsage ap : pu) {
                            totalDur[0] += (ap.getEndTime() - ap.getStartTime());
                            if (ap.getActivity().contains("PhoneCall")) {
                                durationList.set(PHONECALL_INDEX, durationList.get(PHONECALL_INDEX) - ap.getStartTime() + ap.getEndTime());
                            } else if (socialAppList.contains(ap.getActivity())) {
                                durationList.set(SOCIALAPP_INDEX,durationList.get(SOCIALAPP_INDEX) - ap.getStartTime() + ap.getEndTime());
                            } else {
                                durationList.set(OTHERS_INDEX,durationList.get(OTHERS_INDEX) - ap.getStartTime() + ap.getEndTime());
                                Log.e("PU",Long.toString(durationList.get(OTHERS_INDEX)));
                            }
                        }
                    }
                }
                return null;
            }
        }.execute();

        durationList.add(totalMillis - totalDur[0]);
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
