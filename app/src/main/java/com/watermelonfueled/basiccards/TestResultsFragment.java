package com.watermelonfueled.basiccards;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by dapar on 2017-04-20.
 */

public class TestResultsFragment extends Fragment implements View.OnClickListener{
    private final String TAG = "TEST_RESULTS_FRAGMENT";
    private TestActivity testActivity;
    private int correct, total;
    private int[] substackCorrect, substackTotal;
    private float[] substackPercent;
    private String[] substackFraction;
    private ArrayList<String> substackNames;
    private SubstackResultsViewAdapter adapter;

    public static TestResultsFragment newInstance(TestActivity testActivity) {
        TestResultsFragment fragment = new TestResultsFragment();
        fragment.testActivity = testActivity;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_results, container, false);
        Button exitButton = (Button) view.findViewById(R.id.exit);

        RecyclerView substackResultsRV = (RecyclerView) view.findViewById(R.id.rv_list);
        substackResultsRV.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        substackResultsRV.setHasFixedSize(true);
        adapter = new SubstackResultsViewAdapter();
        substackResultsRV.setAdapter(adapter);


        exitButton.setOnClickListener(this);
        return view;
    }

    public void onClick(View view) {
        testActivity.goBack();
    }

    public void setScore(int correct, int total, int[] substackCorrect, int[] substackTotal, ArrayList substackNames) {
        Log.d(TAG, "Setting score - " + correct + ", " + total + ", " + substackCorrect.toString() + ", " + substackTotal);
        this.correct = correct;
        this.total = total;
        this.substackCorrect = substackCorrect;
        this.substackTotal = substackTotal;
        this.substackNames = substackNames;
        substackPercent = new float[substackCorrect.length];
        substackFraction = new String[substackCorrect.length];
        for (int i = 0; i < substackCorrect.length; i++) {
            substackPercent[i] = getRawPercent(substackCorrect[i], substackTotal[i]);
            substackFraction[i] = substackCorrect[i] + "/" + substackTotal[i];
        }
        adapter.notifyDataSetChanged();

        float rawPercent = getRawPercent(correct,total);

        TextView percentView = (TextView) getView().findViewById(R.id.percentage_score);
        percentView.setText(Math.round(rawPercent)+"%");

        ResultsPieChartView pieChart = (ResultsPieChartView) getView().findViewById(R.id.pie);
        pieChart.setCorrectPercent(rawPercent);

        TextView fractionView = (TextView) getView().findViewById(R.id.fraction_score);
        fractionView.setText(correct + "/" + total);

        TextView blurb = (TextView) getView().findViewById(R.id.results_blurb);
        String blurbText;
        String[] blurbArray = getResources().getStringArray(R.array.test_results_blurb_array);
        if (rawPercent < 25) { blurbText = blurbArray[0]; }
        else if (rawPercent < 50){ blurbText = blurbArray[1]; }
        else if (rawPercent < 60){ blurbText = blurbArray[2]; }
        else if (rawPercent < 70){ blurbText = blurbArray[3]; }
        else if (rawPercent < 80){ blurbText = blurbArray[4]; }
        else if (rawPercent < 90){ blurbText = blurbArray[5]; }
        else if (rawPercent < 100){ blurbText = blurbArray[6]; }
        else { blurbText = blurbArray[7]; }
        blurb.setText(blurbText);
    }

    private float getRawPercent(int numerator, int denominator) {
        try {
            float rawPercent = 100 * numerator / denominator;
            return rawPercent;
        } catch (ArithmeticException e) {
            return 0f;
        }
    }

    private class SubstackResultsViewAdapter extends RecyclerView.Adapter<SubstackResultsViewAdapter.SubstackResultsViewHolder> {

        public SubstackResultsViewAdapter() {

        }

        @Override
        public SubstackResultsViewHolder onCreateViewHolder(ViewGroup vg, int viewType) {
            View view = LayoutInflater.from(vg.getContext()).inflate(
                    R.layout.test_results_substack_view,vg,false);
            return new SubstackResultsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SubstackResultsViewHolder holder, int position) {
            holder.pie.setCorrectPercent(substackPercent[position]);
            holder.percent.setText(Math.round(substackPercent[position])+"%");
            holder.fraction.setText(substackFraction[position]);
            holder.name.setText(substackNames.get(position));
        }

        @Override
        public int getItemCount() {
            if (substackCorrect != null) {
                return substackCorrect.length;
            } else {
                return 0;
            }
        }

        class SubstackResultsViewHolder extends RecyclerView.ViewHolder {
            TextView percent, fraction, name;
            ResultsPieChartView pie;

            public SubstackResultsViewHolder(View view) {
                super(view);
                percent = (TextView) view.findViewById(R.id.substack_results_percent);
                fraction = (TextView) view.findViewById(R.id.substack_results_fraction);
                pie = (ResultsPieChartView) view.findViewById(R.id.substack_results_pie);
                name = (TextView) view.findViewById(R.id.substack_results_name);
            }
        }
    }
}
