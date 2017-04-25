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
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

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
        private static final int LOCATION_RECORD_GRAPH = 3;
        private static final int SLEEP_WAKE_GRAPH = 4;

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


            TextView info = (TextView) rootView.findViewById(R.id.section_info);
            info.setText(getString(R.string.section_format, getArguments().getInt(GRAPH_CONTENT)));

            switch (graphType) {
                case PHONE_USAGE_GRAPH:
                    CombinedChart chart1 = (CombinedChart) rootView.findViewById(R.id.chart);
                    CombinedData data1 = new CombinedData();
                    data1.setData(generateData());
                    chart1.setData(data1);
                    break;
                case STEP_DISTANCE_GRAPH:
                    CombinedChart chart2 = (CombinedChart) rootView.findViewById(R.id.chart);
                    CombinedData data2 = new CombinedData();
                    data2.setData(generateData());
                    chart2.setData(data2);
                    break;
                case LOCATION_RECORD_GRAPH:
                    CombinedChart chart3 = (CombinedChart) rootView.findViewById(R.id.chart);
                    CombinedData data3 = new CombinedData();
                    data3.setData(generateData());
                    chart3.setData(data3);
                    break;
                case SLEEP_WAKE_GRAPH:
                    CombinedChart chart4 = (CombinedChart) rootView.findViewById(R.id.chart);
                    CombinedData data4 = new CombinedData();
                    data4.setData(generateData());
                    chart4.setData(data4);
                    break;
                default:
            }
            return rootView;
        }
    }

    static LineData generateData() {
        ArrayList<Entry> values = new ArrayList<Entry>();

        for (int i = 0; i < 10; i++) {
            float val = (float) (Math.random() * 3) + 3;
            values.add(new Entry(i, val));
        }
        LineDataSet set = new LineDataSet(values, "DataSet ");
        set.setDrawIcons(false);
        return new LineData(set);
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
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Phone Usage";
                case 1:
                    return "Step Distance";
                case 2:
                    return "Location Record";
                case 3:
                    return "Sleep-Wake Cycle";
            }
            return null;
        }
    }
}
