package com.watermelonfueled.basiccards;

import android.app.DialogFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static com.watermelonfueled.basiccards.CardsContract.CardEntry;

public class TestActivity extends AppCompatActivity {

    public final static String  SELECTED_SUBSTACKS = "SelectedSubstacks",
                                SELECTED_SUBSTACK_NAMES = "SelectedSubstackNames",
                                TAG = "TESTACTIVITY",
                                INVERSE = "inverse";
    private final long SWIPE_DELAY = 1000;
    private NoSwipeViewPager pager;
    private ArrayList<Integer> substackIdsArrayList, order;
    private int[] substackCorrectCount, substackQuestionCount;
    private ArrayList<ArrayList<Integer>> positions;
    private String[][] cardData;
    private int question, answer, substackId = 2, image = 3; //cardData rows
    private boolean testInverse;
    private int correctCount, position;
    private ArrayList<String> substackNamesList;
    TestResultsFragment resultsFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        if (savedInstanceState != null) {
            substackIdsArrayList = savedInstanceState.getIntegerArrayList("substackIdsArrayList");
            substackQuestionCount = savedInstanceState.getIntArray("substackQuestionCount");
            substackCorrectCount = savedInstanceState.getIntArray("substackCorrectCount");
            cardData = (String[][]) savedInstanceState.getSerializable("cardData");
            positions = (ArrayList) savedInstanceState.getSerializable("positions");
            order = savedInstanceState.getIntegerArrayList("order");
            correctCount = savedInstanceState.getInt("correctCount");
            position = savedInstanceState.getInt("position");
            substackNamesList = savedInstanceState.getStringArrayList("substackNamesList");

        }else {
            substackIdsArrayList = getIntent().getIntegerArrayListExtra(SELECTED_SUBSTACKS);
            substackCorrectCount = new int[substackIdsArrayList.size()];
            substackQuestionCount = new int[substackIdsArrayList.size()];
            substackNamesList = getIntent().getStringArrayListExtra(SELECTED_SUBSTACK_NAMES);
            testInverse = getIntent().getBooleanExtra(INVERSE, false);

            if (testInverse) {
                question = 1;
                answer = 0;
            } else {
                question = 0;
                answer = 1;
            }

            loadCards();
            correctCount = 0;
            position = 0;
        }

        setView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putIntegerArrayList("substackIdsArrayList", substackIdsArrayList);
        outState.putIntArray("substackCorrectCount", substackCorrectCount);
        outState.putIntArray("substackQuestionCount", substackQuestionCount);
        outState.putSerializable("cardData", cardData);
        outState.putSerializable("positions", positions);
        outState.putIntegerArrayList("order", order);
        outState.putInt("correctCount", correctCount);
        outState.putInt("position", position);
        outState.putStringArrayList("substackNamesList", substackNamesList);
        super.onSaveInstanceState(outState);
    }

    private void setView() {
        pager = (NoSwipeViewPager) findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new TestCardPagerAdapter(getSupportFragmentManager(), this);
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                if (position == cardData[0].length) {
                    resultsFragment.setScore(correctCount, cardData[0].length, substackCorrectCount, substackQuestionCount, substackNamesList);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        pager.setCurrentItem(position);
    }

    private void loadCards() {
        positions = new ArrayList<>(substackIdsArrayList.size());
        String[] substackIdsStringArray = new String[substackIdsArrayList.size()];
        for (int i = 0; i <  substackIdsArrayList.size(); i++) {
            positions.add(new ArrayList<Integer>());
            substackIdsStringArray[i] = String.valueOf(substackIdsArrayList.get(i));
        }
        Cursor cursor = DbHelper.getInstance(this).loadCardsTable(substackIdsStringArray);
        order = new ArrayList<>(cursor.getCount());
        cardData = new String[4][cursor.getCount()];

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            cardData[question][i] = cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_QUESTION));
            cardData[answer][i] = cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_ANSWER));
            cardData[substackId][i] = cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_SUBSTACK));
            cardData[image][i] = cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_IMAGE));
            positions.get(substackIdsArrayList.indexOf(Integer.valueOf(cardData[substackId][i]))).add(i);
            order.add(i);
        }
        Collections.shuffle(order);
        cursor.close();
    }

    private class TestCardPagerAdapter extends FragmentPagerAdapter
            implements TestCardFragment.AnswerClickListener {

        TestActivity testActivity;

        public TestCardPagerAdapter(FragmentManager fm, TestActivity testActivity) {
            super(fm);
            this.testActivity = testActivity;
            resultsFragment = TestResultsFragment.newInstance(testActivity);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == cardData[0].length) {
                return resultsFragment;
            }
            int orderPosition = order.get(position);
            String question = cardData[0][orderPosition];
            String correct = cardData[1][orderPosition];
            int substackIdOfCard = Integer.parseInt(cardData[2][orderPosition]);
            String imagePath = cardData[3][orderPosition];

            ArrayList<Integer> substackPositions = new ArrayList<>(positions.get(substackIdsArrayList.indexOf(Integer.valueOf(cardData[substackId][orderPosition]))));
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
            return TestCardFragment.newInstance(this, correctPosition, substackIdOfCard, imagePath, questionAnswers);
        }

        private final Handler handler = new Handler();
        private final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                pager.arrowScroll(View.FOCUS_RIGHT);
            }
        };

        public void onAnswerClick(boolean correct, int substackId) {
            int substackPosition = substackIdsArrayList.indexOf(substackId);
            if (correct) {
                correctCount++;
                substackCorrectCount[substackPosition]++;
            }
            position++;
            substackQuestionCount[substackPosition]++;
            Log.i(TAG, "Correct count: "+correctCount);
            handler.postDelayed(runnable, SWIPE_DELAY);
        }

        @Override
        public int getCount() {
            return cardData[0].length + 1;
        }
    }

    public float getScore() {
        return 100*correctCount/cardData[0].length;
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
