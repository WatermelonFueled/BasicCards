package com.watermelonfueled.basiccards;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static com.watermelonfueled.basiccards.CardsContract.*;

public class TestActivity extends AppCompatActivity {

    public final static String SELECTED_SUBSTACKS = "SelectedSubstacks", TAG = "TESTACTIVITY";
    private ViewPager pager;
    private ArrayList<String> substackIdsArrayList;
    private ArrayList<ArrayList<Integer>> positions;
    private String[][] cardData;
    private ArrayList<Integer> order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        loadCards();
        setView();
    }

    private void setView() {
        pager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new TestCardPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
    }

    private void loadCards() {
        String[] substackIds = getIntent().getStringArrayExtra(SELECTED_SUBSTACKS);
        substackIdsArrayList = new ArrayList<>(Arrays.asList(substackIds));
        positions = new ArrayList<>(substackIds.length);
        for (int i = 0; i < substackIds.length; i++) { positions.add(new ArrayList<Integer>()); }
        Cursor cursor = DbHelper.getInstance(this).loadCardsTable(substackIds);
        order = new ArrayList<>(cursor.getCount());
        cardData = new String[3][cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            cardData[0][i] = cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_QUESTION));
            cardData[1][i] = cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_ANSWER));
            cardData[2][i] = cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_SUBSTACK));
            positions.get(substackIdsArrayList.indexOf(cardData[2][i])).add(i);
            order.add(i);
        }
        Collections.shuffle(order);
        cursor.close();
    }

    private class TestCardPagerAdapter extends FragmentStatePagerAdapter {
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
            return TestCardFragment.newInstance(correctPosition, questionAnswers);
        }

        @Override
        public int getCount() {
            return cardData[0].length;
        }
    }

//    @Override
//    public void onBackPressed() {
//        if (pager.getCurrentItem() == 0) {
//            super.onBackPressed();
//        } else {
//            pager.setCurrentItem(pager.getCurrentItem() - 1);
//        }
//    }
}
