package hk.ust.alzheimerpassivemonitoring;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class SleepWakeCycleFragment extends Fragment {

    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";

    private SQLiteCRUD database;
    private List<SleepWakeCycle> sleepWakeCycleRecord;

    private String startingDate;
    private String endingDate;

    public SleepWakeCycleFragment() {
        // Required empty public constructor
    }

    public static SleepWakeCycleFragment newInstance(String startDate, String endDate) {
        SleepWakeCycleFragment fragment = new SleepWakeCycleFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_sleep_wake_cycle, container, false);

        LineChart chart1 = (LineChart) rootView.findViewById(R.id.swcchart);
        chart1.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm");
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                long millis = TimeUnit.HOURS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });
        LimitLine ll1 = new LimitLine(300f, "Awake");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);

        LimitLine ll2 = new LimitLine(100f, "Asleep");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);

        chart1.getAxisLeft().addLimitLine(ll1);
        chart1.getAxisLeft().addLimitLine(ll2);
        chart1.setData(generateSleepWakeData());

        return rootView;
    }

    LineData generateSleepWakeData() {

        ArrayList<Entry> values = new ArrayList<>();

        for (float x = -8; x < 0; x++) {
            values.add(new Entry(x, x*(-10)+x%3));
        }
        for (float x = 0; x < 16; x++) {
            values.add(new Entry(x, 400+ (float) Math.pow(-1,x) *(x+x%4)));
        }

        LineDataSet set1 = new LineDataSet(values, "DataSet2 ");
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void updateDate(String s, String e) {
        startingDate = s;
        endingDate = e;
    }
}
