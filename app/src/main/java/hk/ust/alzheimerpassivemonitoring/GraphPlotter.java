package hk.ust.alzheimerpassivemonitoring;

import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class GraphPlotter extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private SQLiteCRUD database;
    private List<PhoneUsage> phoneUsageRecord;
    private String startingDate;
    private String endingDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_plotter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        database = new SQLiteCRUD(this);
        database.openDatabase();
        phoneUsageRecord = database.readPhoneUsage("20170426");
        database.closeDatabase();

        EditText startDateText = (EditText) findViewById(R.id.startDate);
        EditText endDateText = (EditText) findViewById(R.id.endDate);

        SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMdd");
        mFormat.setTimeZone(TimeZone.getDefault());

        startingDate = mFormat.format(new Date());
        endingDate = startingDate;

        startDateText.setText(startingDate);
        endDateText.setText(endingDate);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    public static class GraphFragment extends Fragment {

        private static final String GRAPH_CONTENT = "graph_content";
        private static final int PHONE_USAGE_GRAPH = 1;
        private static final int STEP_DISTANCE_GRAPH = 2;
        private static final int SLEEP_WAKE_GRAPH = 3;
//        private static final int LOCATION_RECORD_GRAPH = 4;

        public GraphFragment() {
        }

        public static GraphFragment newInstance(int sectionNumber, List<PhoneUsage> data) {
            GraphFragment fragment = new GraphFragment();
            Bundle args = new Bundle();
            PhoneUsage[] puData = new PhoneUsage[data.size()];
            for (int i = 0; i < data.size(); i++) {
                puData[i] = data.get(i);
            }

            args.putInt(GRAPH_CONTENT, sectionNumber);
            args.putParcelableArray("PHONE_USAGE", puData);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
            int graphType = getArguments().getInt(GRAPH_CONTENT);

            switch (graphType) {
                case PHONE_USAGE_GRAPH:
                    LineChart c1 = (LineChart) rootView.findViewById(R.id.chart1);
                    c1.setVisibility(View.GONE);
                    PieChart chart1 = (PieChart) rootView.findViewById(R.id.chart2);

                    PhoneUsage[] phoneUsages = (PhoneUsage[]) getArguments().getParcelableArray("PHONE_USAGE");

                    chart1.setCenterText("Phone Usage");
                    chart1.setUsePercentValues(true);
                    chart1.setRotationEnabled(false);
                    chart1.setData(generatePhoneUsageData(phoneUsages));
                    chart1.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                        @Override
                        public void onValueSelected(Entry e, Highlight h) {
                        }
                        @Override
                        public void onNothingSelected() {
                        }
                    });

                    Legend l = chart1.getLegend();
                    l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
                    l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
                    l.setOrientation(Legend.LegendOrientation.VERTICAL);
                    l.setDrawInside(false);
                    l.setXEntrySpace(7f);
                    l.setYEntrySpace(0f);
                    l.setYOffset(0f);
                    break;
                case STEP_DISTANCE_GRAPH:
                    PieChart c2 = (PieChart) rootView.findViewById(R.id.chart2);
                    c2.setVisibility(View.GONE);
                    LineChart chart2 = (LineChart) rootView.findViewById(R.id.chart1);
                    chart2.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                        private SimpleDateFormat mFormat = new SimpleDateFormat("dd-MM");
                        @Override
                        public String getFormattedValue(float value, AxisBase axis) {

                            long millis = TimeUnit.DAYS.toMillis((long) value);
                            return mFormat.format(new Date(millis));
                        }
                    });
                    chart2.setData(generateStepDistanceData());
                    break;
                case SLEEP_WAKE_GRAPH:
                    PieChart c3 = (PieChart) rootView.findViewById(R.id.chart2);
                    c3.setVisibility(View.GONE);
                    LineChart chart3 = (LineChart) rootView.findViewById(R.id.chart1);
                    chart3.getXAxis().setValueFormatter(new IAxisValueFormatter() {
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

                    chart3.getAxisLeft().addLimitLine(ll1);
                    chart3.getAxisLeft().addLimitLine(ll2);
                    chart3.setData(generateSleepWakeData());
                    break;
//                case LOCATION_RECORD_GRAPH:
//                    CombinedChart chart3 = (CombinedChart) rootView.findViewById(R.id.chart);
//                    CombinedData data3 = new CombinedData();
//                    data3.setData(generateData());
//                    chart3.setData(data3);
//                    break;

                default:
            }
            return rootView;
        }
    }

    static LineData generateStepDistanceData() {

        ArrayList<Entry> values = new ArrayList<>();

        long now = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis());
        Log.e("now", now+"");

        for (float x = now-7; x < now; x++) {
            values.add(new Entry(x, x/2 + (float) Math.pow(-1,x) * (x%4)));
        }

        LineDataSet set1 = new LineDataSet(values, "DataSet1 ");
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

    static LineData generateSleepWakeData() {

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

    static PieData generatePhoneUsageData(PhoneUsage[] p) {

        String[] socialApp = {"WhatsApp = 150min", "FaceBook = 150min"};
        String[] others = {"Game = 110min"};

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(1000, "Screen Off"));
        entries.add(new PieEntry(30, "Phone Calls"));
        entries.add(new PieEntry(300, "Social Apps", socialApp));
        entries.add(new PieEntry(110, "Others", others));

//        for (PhoneUsage aP : p) {
//            entries.add(new PieEntry(aP.getStartTime() - aP.getEndTime(), aP.getActivity()));
//        }

        PieDataSet dataSet = new PieDataSet(entries, "Phone Usage");

        dataSet.setDrawIcons(false);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);

        return data;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final int NUM_OF_FRAGMENT = 3;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            EditText startDateText = (EditText) findViewById(R.id.startDate);
            EditText endDateText = (EditText) findViewById(R.id.endDate);

            switch (position) {
                case 0:
                    return PhoneUsageFragment.newInstance(startDateText.getText().toString(), endDateText.getText().toString());
                case 1:
                    return new StepDistanceFragment();
                case 2:
                    return new SleepWakeCycleFragment();
                default:
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_OF_FRAGMENT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Phone Usage";
                case 1:
                    return "Step Distance";
                case 2:
                    return "Sleep-Wake Cycle";
                //                case 3:
//                    return "Location Record";
            }
            return null;
        }
    }

}
