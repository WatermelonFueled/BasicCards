package com.watermelonfueled.basiccards;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by dapar on 2017-04-20.
 */

public class TestResultsFragment extends Fragment implements View.OnClickListener{
    private TestActivity testActivity;

    public static TestResultsFragment newInstance(TestActivity testActivity) {
        TestResultsFragment fragment = new TestResultsFragment();
        fragment.testActivity = testActivity;
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_results, container, false);
        Button exitButton = (Button) view.findViewById(R.id.exit);
        exitButton.setOnClickListener(this);
        return view;
    }


    public void onClick(View view) {
        testActivity.goBack();
    }

    public void setScore(float rawPercent) {
        TextView scoreView = (TextView) getView().findViewById(R.id.score);
        scoreView.setText(Math.round(rawPercent)+"%");
    }
}
