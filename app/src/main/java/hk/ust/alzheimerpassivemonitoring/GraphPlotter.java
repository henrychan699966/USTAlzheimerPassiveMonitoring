package hk.ust.alzheimerpassivemonitoring;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class GraphPlotter extends AppCompatActivity implements View.OnClickListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private String startingDate;
    private String endingDate;
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMdd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_plotter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        EditText startDateText = (EditText) findViewById(R.id.startDate);
        EditText endDateText = (EditText) findViewById(R.id.endDate);

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
        mFormat.setTimeZone(TimeZone.getDefault());
        try {
            Date startDate = mFormat.parse(s);
            Date endDate = mFormat.parse(e);
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

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final int FRAGMENT_COUNT = 3;

        SectionsPagerAdapter(FragmentManager fm) {
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
                    return SleepWakeCycleFragment.newInstance(startingDate);
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
