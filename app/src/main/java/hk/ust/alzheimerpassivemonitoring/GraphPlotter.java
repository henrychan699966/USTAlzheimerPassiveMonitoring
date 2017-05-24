package hk.ust.alzheimerpassivemonitoring;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class GraphPlotter extends AppCompatActivity implements View.OnClickListener {

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
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 2:
                        showItem(false);
                        break;
                    default:
                        showItem(true);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        ImageButton refreshButton = (ImageButton) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.refreshButton:
                EditText startDateText = (EditText) findViewById(R.id.startDate);
                EditText endDateText = (EditText) findViewById(R.id.endDate);
                startingDate = startDateText.getText().toString();
                endingDate = endDateText.getText().toString();
                if (checkDateValid(startingDate, endingDate)) {
                    notifyViewPagerDataSetChanged();
                }
                break;
            default:
        }
    }

    private boolean checkDateValid(String s, String e) {
        final int MAX_DAY = 30;
        final int MIN_INTERVAL = 0;
        long startDateMillis = 0;
        long endDateMillis = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setTimeZone(TimeZone.getDefault());
        try {
            Date startDate = sdf.parse(s);
            Date endDate = sdf.parse(e);
            startDateMillis = TimeUnit.MILLISECONDS.toDays(startDate.getTime());
            endDateMillis = TimeUnit.MILLISECONDS.toDays(endDate.getTime());
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        if (startDateMillis > endDateMillis) {
            showToast("Error in start date");
            return false;
        } else if (endDateMillis-startDateMillis > MAX_DAY) {
            showToast("Error in interval length");
            return false;
        } else if (TimeUnit.MILLISECONDS.toDays(new Date().getTime())-endDateMillis<MIN_INTERVAL) {
            showToast("Error in end date");
            return false;
        }
        return true;
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

        private final int FRAGMENT_COUNT = 3;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return PhoneUsageFragment.newInstance(startingDate, endingDate);
                case 1:
                    return StepDistanceFragment.newInstance(startingDate, endingDate);
                case 2:
                    return SleepWakeCycleFragment.newInstance(startingDate, endingDate);
                default:
            }
            return null;
        }

        @Override
        public int getCount() {
            return FRAGMENT_COUNT;
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof PhoneUsageFragment) {
                ((PhoneUsageFragment) object).updateDate(startingDate, endingDate);
            } else if (object instanceof StepDistanceFragment) {
                ((StepDistanceFragment) object).updateDate(startingDate, endingDate);
            } else if (object instanceof SleepWakeCycleFragment) {
                ((SleepWakeCycleFragment) object).updateDate(startingDate);
            }
            return super.getItemPosition(object);
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

    private void notifyViewPagerDataSetChanged() {
        mSectionsPagerAdapter.notifyDataSetChanged();
    }

    public void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private void showItem (boolean visible) {
        TextView t = (TextView) findViewById(R.id.t1);
        EditText e = (EditText) findViewById(R.id.endDate);
        if (visible) {
            t.setVisibility(View.VISIBLE);
            e.setVisibility(View.VISIBLE);
        } else {
            t.setVisibility(View.INVISIBLE);
            e.setVisibility(View.INVISIBLE);
        }
    }
}
