package hk.ust.alzheimerpassivemonitoring;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class SleepWakeCycleFragment extends Fragment {

    private static final String START_DATE = "start_date";
    private static final String[] SLEEP_CYCLE = {"deep","light","rem","wake"};

    private SQLiteCRUD database;
    private List<SleepWakeCycle> sleepWakeCycleRecord;

    private String startingDate;

    private LineChart mChart;

    public SleepWakeCycleFragment() {
        // Required empty public constructor
    }

    public static SleepWakeCycleFragment newInstance(String startDate, String endDate) {
        SleepWakeCycleFragment fragment = new SleepWakeCycleFragment();
        Bundle args = new Bundle();
        args.putString(START_DATE, startDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            startingDate = getArguments().getString(START_DATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sleep_wake_cycle, container, false);

        mChart = (LineChart) rootView.findViewById(R.id.swcchart);
        mChart.getDescription().setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm");
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                long millis = TimeUnit.HOURS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);

        LimitLine ll1 = new LimitLine(0f, "Deep");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll1.setTextSize(8f);

        LimitLine ll2 = new LimitLine(1f, "Light");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(5f, 5f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(8f);

        LimitLine ll3 = new LimitLine(2f, "REM");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(5f, 5f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(8f);

        LimitLine ll4 = new LimitLine(3f, "Wake");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(5f, 5f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(8f);

        mChart.getAxisLeft().addLimitLine(ll1);
        mChart.getAxisLeft().addLimitLine(ll2);
        mChart.getAxisLeft().addLimitLine(ll3);
        mChart.getAxisLeft().addLimitLine(ll4);
        mChart.setData(generateSleepWakeData(startingDate));

        return rootView;
    }

    LineData generateSleepWakeData(String s) {

        ArrayList<Entry> swValues = new ArrayList<>();
        swValues.add(new Entry(0, 3));
        swValues.addAll(getDailySleepWake(s));

        LineDataSet set1 = new LineDataSet(swValues, "Sleep-Wake ");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setValueTextColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(1.5f);
        set1.setDrawCircles(false);
        set1.setDrawValues(false);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setDrawCircleHole(false);

        LineData data = new LineData(set1);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        return data;
    }

    private ArrayList<Entry> getDailySleepWake(String date) {
        ArrayList<Entry> a = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setTimeZone(TimeZone.getDefault());
        long ref = 0;
        try {
            Date startDate = sdf.parse(date);
            ref = TimeUnit.MILLISECONDS.toDays(startDate.getTime());
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        List<SleepWakeCycle> swList = database.readSleepWakeCycle(date);
        if (swList == null) return a;
        for (int i = 0; i < swList.size(); i++) {
            long start = TimeUnit.MILLISECONDS.toSeconds(swList.get(i).getStartTime()-ref);
            long end = TimeUnit.MILLISECONDS.toSeconds(swList.get(i).getEndTime()-ref)-1;
            a.add(new Entry(start, convertSleepStage(swList.get(i).getSleepStage())));
            a.add(new Entry(end, convertSleepStage(swList.get(i).getSleepStage())));
        }
        return a;
    }

    private int convertSleepStage (String s) {
        if (s.equals(SLEEP_CYCLE[0])) {
            return 0;
        } else if (s.equals(SLEEP_CYCLE[1])) {
            return 1;
        } else if (s.equals(SLEEP_CYCLE[2])) {
            return 2;
        }
        return 3;
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

    public void updateDate(String s) {
        startingDate = s;
        mChart.setData(generateSleepWakeData(startingDate));
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }
}
