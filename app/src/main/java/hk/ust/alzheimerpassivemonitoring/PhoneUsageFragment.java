package hk.ust.alzheimerpassivemonitoring;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class PhoneUsageFragment extends Fragment {

    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";

    private SQLiteCRUD database;
    private List<PhoneUsage> phoneUsageRecord;

    private String startingDate;
    private String endingDate;

    private OnFragmentInteractionListener mListener;

    public PhoneUsageFragment() {
        // Required empty public constructor
    }

    public static PhoneUsageFragment newInstance(String startDate, String endDate) {
        PhoneUsageFragment fragment = new PhoneUsageFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_phone_usage, container, false);

        TextView st = (TextView) rootView.findViewById(R.id.st);
        TextView et = (TextView) rootView.findViewById(R.id.et);

        st.setText(startingDate);
        et.setText(endingDate);

        PieChart chart1 = (PieChart) rootView.findViewById(R.id.puchart);
        chart1.setCenterText("Phone Usage");
        chart1.setUsePercentValues(true);
        chart1.setRotationEnabled(false);
        chart1.setData(generatePhoneUsageData());
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

        return rootView;
    }

    PieData generatePhoneUsageData() {

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        database = new SQLiteCRUD(context);
        database.openDatabase();

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
        mListener = null;
        database.closeDatabase();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
