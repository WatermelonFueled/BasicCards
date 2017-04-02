package com.watermelonfueled.basiccards;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

import static com.watermelonfueled.basiccards.CardsContract.*;

public class CardListActivity extends AppCompatActivity
        implements CardListViewAdapter.ListItemClickListener,
        DeleteStackDialog.DeleteStackDialogListener {

    private DbHelper dbHelper;
    public final static String SELECTED_SUBSTACKS = "SelectedSubstacks";
    private CardListViewAdapter adapter;
    private ArrayList<String> cardFrontList, cardBackList;
    private ArrayList<Integer> cardIdList;
    private String[] substackIds;
    private int toDeleteIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        Intent intent = getIntent();
        substackIds = intent.getStringArrayExtra(SELECTED_SUBSTACKS);
        
        dbHelper = DbHelper.getInstance(this);
        loadCards();

        setView();
    }

    private void setView() {
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);
        adapter = new CardListViewAdapter(this, cardFrontList, cardBackList);
        rv.setAdapter(adapter);
    }

    private void loadCards() {
        Cursor cursor = dbHelper.loadCardsTable(substackIds);
        cardFrontList = new ArrayList<>(cursor.getCount());
        cardBackList = new ArrayList<>(cursor.getCount());
        cardIdList = new ArrayList<>(cursor.getCount());
        while(cursor.moveToNext()){
            cardFrontList.add(cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_QUESTION)));
            cardBackList.add(cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_ANSWER)));
            cardIdList.add(cursor.getInt(cursor.getColumnIndex(CardEntry._ID)));
        }
        cursor.close();
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {

    }

    public void deleteButtonOnClick(View button) {
        toDeleteIndex = (int) button.getTag();
        DeleteStackDialog dialog = new DeleteStackDialog();
        dialog.setStackName(cardFrontList.get(toDeleteIndex) + "/" + cardBackList.get(toDeleteIndex));
        dialog.show(getSupportFragmentManager(), "DeleteCardDialog");
    }

    @Override
    public void onDeleteStackDialogPositiveClick(DialogFragment dialog) {
        dbHelper.deleteCard(cardIdList.get(toDeleteIndex));
        updated();
    }

    private void updated() {
        loadCards();
        adapter.updated(cardFrontList, cardBackList);
    }
}
