package com.watermelonfueled.basiccards;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import java.util.ArrayList;

import static com.watermelonfueled.basiccards.CardsContract.StackEntry;
import static com.watermelonfueled.basiccards.CardsContract.SubstackEntry;

public class SubstackActivity extends AppCompatActivity
        implements SubstackViewAdapter.ListItemClickListener,
        AddStackDialog.AddStackDialogListener,
        DeleteDialog.DeleteDialogListener,
        AddCardDialog.AddCardDialogListener{

    private DbHelper dbHelper;
    private int stackId, toDeleteIndex, addCardToSubstackIndex = 0 ;
    private SubstackViewAdapter adapter;
    private ArrayList<String> substackNameList;
    private ArrayList<Integer> substackIdList;
    private ArrayList<Boolean> substackSelectedList;
    private String toDeleteName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_substack);

        Intent intent = getIntent();
        stackId = intent.getIntExtra(StackEntry._ID, 0);

        //TODO set actionbar title to stack name
        String stackName = intent.getStringExtra(StackEntry.COLUMN_NAME);

        dbHelper = DbHelper.getInstance(this);
        loadSubstackList();

        setView();
    }

    private void setView() {
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);
        adapter = new SubstackViewAdapter(this, substackNameList);
        rv.setAdapter(adapter);
    }

    private void loadSubstackList() {
        Cursor cursor = dbHelper.loadSubstackTable(stackId);
        substackNameList = new ArrayList<>(cursor.getCount());
        substackIdList = new ArrayList<>(cursor.getCount());
        substackSelectedList = new ArrayList<>(cursor.getCount());
        while(cursor.moveToNext()){
            substackNameList.add(cursor.getString(cursor.getColumnIndex(SubstackEntry.COLUMN_NAME)));
            substackIdList.add(cursor.getInt(cursor.getColumnIndex(SubstackEntry._ID)));
            substackSelectedList.add(false);
        }
        cursor.close();
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        substackSelectedList.set(clickedItemIndex, !substackSelectedList.get(clickedItemIndex));
        Log.i("SUBSTACKACTIVITY", "set index: " + clickedItemIndex + " to: " + substackSelectedList.get(clickedItemIndex));
    }

    public void addSubstackOnClick(View button) {
        DialogFragment dialog = new AddStackDialog();
        dialog.show(getSupportFragmentManager(), "AddSubstackDialog");
    }

    @Override
    public void onAddStackDialogPositiveClick(DialogFragment dialog, String substackName) {
        dbHelper.addSubstack(substackName,stackId);
        updated();
    }

    public void deleteButtonOnClick(View button) {
        toDeleteIndex = (int) button.getTag();
        toDeleteName = substackNameList.get(toDeleteIndex);
        DeleteDialog dialog = new DeleteDialog();
        dialog.setConfirmMessage("Are you sure you want to delete: " +
                toDeleteName + "? All its contents will be deleted.");
        dialog.show(getSupportFragmentManager(), "DeleteSubstackDialog");
    }

    @Override
    public void onDeleteDialogPositiveClick(DialogFragment dialog) {
        dbHelper.deleteSubstack(substackIdList.get(toDeleteIndex));
        updated();
    }

    public void addCardOnClick(View button) {
        AddCardDialog dialog = new AddCardDialog();
        dialog.setSubstackNames(substackNameList);
        dialog.show(getSupportFragmentManager(), "AddCardDialog");
    }

    @Override
    public void onAddCardDialogSelectSubstack(int position) {
        addCardToSubstackIndex = position;
    }

    @Override
    public void onAddCardDialogPositiveClick(DialogFragment dialog, String front, String back, Bitmap image) {
        dbHelper.addCard(front,back,substackIdList.get(addCardToSubstackIndex), image);
        updated();
        addCardToSubstackIndex = 0;
    }

    public void showCardListOnClick(View button) {
        Intent intent = new Intent(this, CardListActivity.class);
        String[] arr = selectedSubstackIdsArray();
        if (arr.length <= 0) { return; }
        intent.putExtra(CardListActivity.SELECTED_SUBSTACKS, arr);
        startActivity(intent);
    }

    public void startTestOnClick(View button) {
        String[] arr = selectedSubstackIdsArray();
        if (arr.length <= 0) { return; }

        boolean testInverse = ((CheckBox) findViewById(R.id.inverse_checkbox)).isChecked();
        Intent intent = new Intent(this, TestActivity.class);
        intent.putExtra(TestActivity.SELECTED_SUBSTACKS, arr);
        intent.putExtra(TestActivity.INVERSE, testInverse);
        startActivity(intent);
    }

    private String[] selectedSubstackIdsArray() {
        ArrayList<String> selectedSubstackIds = new ArrayList<>();
        for (int i = 0; i < substackIdList.size(); i++) {
            if (substackSelectedList.get(i)){
                selectedSubstackIds.add(substackIdList.get(i).toString());
            }
        }
        String[] arr = new String[selectedSubstackIds.size()];
        arr = selectedSubstackIds.toArray(arr);
        return arr;
    }

    private void updated() {
        loadSubstackList();
        adapter.updated(substackNameList);
    }
}
