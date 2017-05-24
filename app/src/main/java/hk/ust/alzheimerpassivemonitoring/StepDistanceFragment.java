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
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StepDistanceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StepDistanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StepDistanceFragment extends Fragment {

    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";

    private SQLiteCRUD database;
    private List<StepDistance> stepDistanceRecord;

    private String startingDate;
    private String endingDate;

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

        BarChart chart1 = (BarChart) rootView.findViewById(R.id.sdchart);
        chart1.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            private SimpleDateFormat mFormat = new SimpleDateFormat("dd-MM");
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                long millis = TimeUnit.DAYS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });
        chart1.setData(generateStepDistanceData());

        return rootView;
    }

    BarData generateStepDistanceData() {

        ArrayList<BarEntry> values = new ArrayList<>();

        long now = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis());
        Log.e("now", now+"");

        for (float x = now-7; x < now; x++) {
            values.add(new BarEntry(x, x/2 + (float) Math.pow(-1,x) * (x%4)));
        }

        BarDataSet set1 = new BarDataSet(values, "DataSet1 ");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setValueTextColor(ColorTemplate.getHoloBlue());
        set1.setDrawValues(false);

        BarData data = new BarData(set1);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        return data;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
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
