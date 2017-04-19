package com.watermelonfueled.basiccards;

import android.app.DialogFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static com.watermelonfueled.basiccards.CardsContract.CardEntry;

public class TestActivity extends AppCompatActivity {

    public final static String SELECTED_SUBSTACKS = "SelectedSubstacks", TAG = "TESTACTIVITY", INVERSE = "inverse";
    private final long SWIPE_DELAY = 750;
    private NoSwipeViewPager pager;
    private ArrayList<String> substackIdsArrayList;
    private ArrayList<ArrayList<Integer>> positions;
    private String[][] cardData;
    private ArrayList<Integer> order;
    private boolean testInverse;
    private int correctCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        testInverse = getIntent().getBooleanExtra(INVERSE, false);

        loadCards();
        setView();

        correctCount = 0;
    }

    private void setView() {
        pager = (NoSwipeViewPager) findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new TestCardPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
    }

    private void loadCards() {
        String[] substackIds = getIntent().getStringArrayExtra(SELECTED_SUBSTACKS);
        substackIdsArrayList = new ArrayList<>(Arrays.asList(substackIds));
        positions = new ArrayList<>(substackIds.length);
        for (String substackId : substackIds) {
            positions.add(new ArrayList<Integer>());
        }
        Cursor cursor = DbHelper.getInstance(this).loadCardsTable(substackIds);
        order = new ArrayList<>(cursor.getCount());
        cardData = new String[3][cursor.getCount()];

        int question, answer;
        if (testInverse) {
            question = 1; answer = 0;
        } else {
            question = 0; answer = 1;
        }

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            cardData[question][i] = cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_QUESTION));
            cardData[answer][i] = cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_ANSWER));
            cardData[2][i] = cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_SUBSTACK));
            positions.get(substackIdsArrayList.indexOf(cardData[2][i])).add(i);
            order.add(i);
        }
        Collections.shuffle(order);
        cursor.close();
    }

    //TODO compare fragmentstatepageradapter vs fragmentpageradapter
    private class TestCardPagerAdapter extends FragmentStatePagerAdapter
            implements TestCardFragment.AnswerClickListener {



        public TestCardPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            int orderPosition = order.get(position);
            String question = cardData[0][orderPosition];
            String correct = cardData[1][orderPosition];

            ArrayList<Integer> substackPositions = new ArrayList<>(positions.get(substackIdsArrayList.indexOf(cardData[2][orderPosition])));
            Collections.shuffle(substackPositions);
            int optionCount = Math.min(4, substackPositions.size());
            substackPositions.remove((Integer)orderPosition);

            String[] questionAnswers = new String[1+optionCount];
            int correctPosition = new Random().nextInt(optionCount) + 1;
            questionAnswers[0] = question;
            for (int i = 1, j = 0; i <= optionCount; i++, j++) {
                if (i == correctPosition) {
                    questionAnswers[i] = correct;
                    j--;
                } else {
                    questionAnswers[i] = cardData[1][substackPositions.get(j)];
                }
            }
            return TestCardFragment.newInstance(this, correctPosition, questionAnswers);
        }

        private final Handler handler = new Handler();
        private final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                pager.arrowScroll(View.FOCUS_RIGHT);
            }
        };

        public void onAnswerClick(boolean correct) {
            if (correct) { correctCount++; }
            Log.i(TAG, "Correct count: "+correctCount);
            handler.postDelayed(runnable, SWIPE_DELAY);
        }

        @Override
        public int getCount() {
            return cardData[0].length;
        }
    }

    @Override
    public void onBackPressed() {
        DialogFragment dialog = new CancelTestDialog();
        dialog.show(getFragmentManager(), "CancelTestDialog");
    }

    public void goBack() {
        super.onBackPressed();
    }
}
