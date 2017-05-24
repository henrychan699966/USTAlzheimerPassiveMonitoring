package hk.ust.alzheimerpassivemonitoring;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import java.text.DecimalFormat;
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
        mChart.getAxisRight().setValueFormatter(new IAxisValueFormatter() {
            private DecimalFormat mFormat = new DecimalFormat("##.###");
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mFormat.format(value) + " km";
            }
        });

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        generateStepDistanceData(startingDate, endingDate);

        Legend l = mChart.getLegend();
        l.setTextSize(8f);

        return rootView;
    }

    void generateStepDistanceData(String s, String e) {

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

        for (long x = startDateDay; x <= endDateDay; x++) {
            String currentDate = sdf.format(TimeUnit.DAYS.toMillis((x)));
            StepDistance sd = getStepDistanceList(currentDate);
            stepValues.add(new BarEntry(x, sd.getStep()));
            distanceValues.add(new BarEntry(x, sd.getDistance()));
            Log.e("data",Integer.toString(sd.getStep()) + " " + Float.toString(sd.getDistance()));
        }

        BarDataSet set1, set2;

        if (mChart.getData() != null && mChart.getData().getDataSetCount() == 2) {
            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set2 = (BarDataSet) mChart.getData().getDataSetByIndex(1);
            set1.setValues(stepValues);
            set2.setValues(distanceValues);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(stepValues, "Step ");
            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
            set1.setColor(Color.BLUE);
            set1.setDrawValues(false);

            set2 = new BarDataSet(distanceValues, "Distance ");
            set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set2.setColor(Color.GREEN);
            set2.setDrawValues(false);

            BarData data = new BarData(set1, set2);
            data.setValueTextColor(Color.GRAY);
            data.setValueTextSize(9f);
            data.setBarWidth(0.4f);

            mChart.setData(data);
        }
        mChart.groupBars(0.1f,0.08f,0.06f);
        mChart.invalidate();
    }

    private StepDistance getStepDistanceList (String date) {
        List<StepDistance> sdList = database.readStepDistance(date);
        if (sdList == null) return new StepDistance(0,0,0);
        Log.e("step",Integer.toString(sdList.size()));
        return database.readStepDistance(date).get(0);

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
        generateStepDistanceData(startingDate, endingDate);
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

}
