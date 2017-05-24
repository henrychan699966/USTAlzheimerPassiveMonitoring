package hk.ust.alzheimerpassivemonitoring;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class StepDistanceFragment extends Fragment {

    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";

    private SQLiteCRUD database;

    private String startingDate;
    private String endingDate;

    private BarChart mChart;

    public StepDistanceFragment() {
        // Required empty public constructor
    }

    public static StepDistanceFragment newInstance(String startDate, String endDate) {
        StepDistanceFragment fragment = new StepDistanceFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_step_distance, container, false);

        mChart = (BarChart) rootView.findViewById(R.id.sdchart);
        mChart.getDescription().setEnabled(false);
        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e == null) return;
                Toast.makeText(getContext(), "\""+e.getY()+"\"", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected() {
            }
        });

        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            private SimpleDateFormat mFormat = new SimpleDateFormat("dd-MM");
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                mFormat.setTimeZone(TimeZone.getDefault());
                long millis = TimeUnit.DAYS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        mChart.setData(generateStepDistanceData(startingDate, endingDate));
        mChart.getBarData().setBarWidth(0.4f);

        Legend l = mChart.getLegend();
        l.setTextSize(8f);

        return rootView;
    }

    BarData generateStepDistanceData(String s, String e) {

        ArrayList<BarEntry> stepValues = new ArrayList<>();
        ArrayList<BarEntry> distanceValues = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setTimeZone(TimeZone.getDefault());
        long startDateDay = 0;
        long endDateDay = 0;
        try {
            Date startDate = sdf.parse(s);
            Date endDate = sdf.parse(e);
            startDateDay = TimeUnit.MILLISECONDS.toDays(startDate.getTime());
            endDateDay = TimeUnit.MILLISECONDS.toDays(endDate.getTime());
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        for (float x = startDateDay; x <= endDateDay; x++) {
            String currentDate = sdf.format(TimeUnit.DAYS.toMillis(((long) x)));
            stepValues.add(new BarEntry(x, getDailyStep(currentDate)));
            distanceValues.add(new BarEntry(x, getDailyDistance(currentDate)));
        }

        BarDataSet set1, set2;
        set1 = new BarDataSet(stepValues, "Step ");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(Color.BLUE);
        set1.setValueTextColor(ColorTemplate.getHoloBlue());
        set1.setDrawValues(false);

        set2 = new BarDataSet(distanceValues, "Distance ");
        set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set2.setColor(Color.GREEN);
        set2.setValueTextColor(ColorTemplate.getHoloBlue());
        set2.setDrawValues(false);

        BarData data = new BarData(set1, set2);
        data.groupBars(0.1f,0.08f,0.06f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        return data;
    }

    private int getDailyStep (String date) {
        List<StepDistance> stepList = database.readStepDistance(date);
        if (stepList == null) return 0;
        return stepList.get(0).getStep();
    }

    private float getDailyDistance (String date) {
        List<StepDistance> distanceList = database.readStepDistance(date);
        if (distanceList == null) return 0;
        return distanceList.get(0).getStep();
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
        mChart.setData(generateStepDistanceData(startingDate, endingDate));
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

}
