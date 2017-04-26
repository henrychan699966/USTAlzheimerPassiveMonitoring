package hk.ust.alzheimerpassivemonitoring;

import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class GraphPlotter extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_plotter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class GraphFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String GRAPH_CONTENT = "graph_content";
        private static final int PHONE_USAGE_GRAPH = 1;
        private static final int STEP_DISTANCE_GRAPH = 2;
        private static final int SLEEP_WAKE_GRAPH = 3;
        //        private static final int LOCATION_RECORD_GRAPH = 4;

        public GraphFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static GraphFragment newInstance(int sectionNumber) {
            GraphFragment fragment = new GraphFragment();
            Bundle args = new Bundle();
            args.putInt(GRAPH_CONTENT, sectionNumber);
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
                    chart1.setCenterText("Phone Usage");
                    chart1.setUsePercentValues(true);
                    chart1.setRotationEnabled(false);
                    chart1.setData(generatePieData());

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
                    chart2.setData(generateData());
                    break;
                case SLEEP_WAKE_GRAPH:
                    PieChart c3 = (PieChart) rootView.findViewById(R.id.chart2);
                    c3.setVisibility(View.GONE);
                    LineChart chart3 = (LineChart) rootView.findViewById(R.id.chart1);
                    chart3.setData(generateData());
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

    static LineData generateData() {
        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            float val = (float) (Math.random() * 3) + 3;
            values.add(new Entry(i, val));
        }
        LineDataSet set = new LineDataSet(values, "DataSet ");
        set.setDrawIcons(false);
        return new LineData(set);
    }

    static PieData generatePieData() {
        float mult = 100;

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (int i = 0; i < 4 ; i++) {
            entries.add(new PieEntry((float) ((Math.random() * mult) + mult / 5)));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Phone Usage");

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        return data;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a GraphFragment (defined as a static inner class below).
            return GraphFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Phone Usage";
                case 1:
                    return "Step Distance";
//                case 2:
//                    return "Location Record";
                case 2:
                    return "Sleep-Wake Cycle";
            }
            return null;
        }
    }
}
