package com.watermelonfueled.basiccards;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import static com.watermelonfueled.basiccards.CardsContract.StackEntry;
import static com.watermelonfueled.basiccards.CardsContract.SubstackEntry;

public class SubstackActivity extends AppCompatActivity
        implements StackViewAdapter.ListItemClickListener,
        AddStackDialog.AddStackDialogListener,
        DeleteDialog.DeleteDialogListener {

    private final String TAG = "SubstackActivity";

    private DbHelper dbHelper;
    private int stackId, toDeleteOrEditId, addCardToSubstackIndex = 0 ;
    private StackViewAdapter adapter;
    private ArrayList<String> substackNameList;
    private ArrayList<Integer> substackIdList;
    private ArrayList<Boolean> substackSelectedList;
    private String toDeleteOrEditName;
    Type dialogType;

    private final String    STACK_ID_KEY = "stackidkey",
                            ADD_CARD_SUBSTACK_INDEX_KEY = "addcardsubstackindexkey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_substack);

        if (savedInstanceState != null) {
            stackId = savedInstanceState.getInt(STACK_ID_KEY);
            substackSelectedList = (ArrayList) savedInstanceState.getSerializable("substackSelectedList");
        } else {
            Intent intent = getIntent();
            stackId = intent.getIntExtra(StackEntry._ID, 0);

            //TODO set actionbar title to stack name
            String stackName = intent.getStringExtra(StackEntry.COLUMN_NAME);
        }

        dbHelper = DbHelper.getInstance(this);
        loadSubstackList();

        setView();

        //For camera write to file
        //Allowing Strict mode policy for Nougat support
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putInt(STACK_ID_KEY, stackId);
        outState.putSerializable("substackSelectedList", substackSelectedList);

        super.onSaveInstanceState(outState);
    }

    private void setView() {
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);
        adapter = new StackViewAdapter(this, substackNameList, R.layout.substack_view);
        adapter.setSubstackSelectedList(substackSelectedList);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.substackmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_substack:
                addSubstackOnClick();
                return true;
            case R.id.menu_list_cards:
                showCardListOnClick();
                return true;
            case R.id.menu_start_test:
                startTestOnClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ADD AND EDIT SUBSTACK //
    public void addSubstackOnClick() {
        dialogType = Type.CREATE;
        DialogFragment dialog = new AddStackDialog();
        dialog.show(getSupportFragmentManager(), "AddSubstackDialog");
    }

    @Override
    public void onAddStackDialogPositiveClick(DialogFragment dialog, String substackName) {
        switch (getType()) {
            case CREATE:
                dbHelper.addSubstack(substackName,stackId);
                substackSelectedList.add(false);
                break;
            case EDIT:
                if (substackName.equals(toDeleteOrEditName)) {
                    return; //no change
                }
                dbHelper.updateSubstack(toDeleteOrEditId, substackName);
                break;
        }
        updated();
    }

    public void editButtonOnClick(View button) {
        dialogType = Type.EDIT;
        int position = (int) button.getTag();
        toDeleteOrEditId = substackIdList.get(position);
        toDeleteOrEditName = substackNameList.get(position);
        DialogFragment dialog = new AddStackDialog();
        dialog.show(getSupportFragmentManager(), "DeleteSubstackDialog");
    }

    public void deleteButtonOnClick(View button) {
        int position = (int) button.getTag();
        toDeleteOrEditId = substackIdList.get(position);
        toDeleteOrEditName = substackNameList.get(position);
        DeleteDialog dialog = new DeleteDialog();
        dialog.setConfirmMessage("Are you sure you want to delete: " +
                toDeleteOrEditName + "? All its contents will be deleted.");
        dialog.show(getSupportFragmentManager(), "DeleteSubstackDialog");
    }

    @Override
    public void onDeleteDialogPositiveClick(DialogFragment dialog) {
        dbHelper.deleteSubstack(toDeleteOrEditId);
        updated();
    }

    @Override
    public Type getType(){
        return dialogType;
    }

    @Override
    public String getNameToEdit() {
        return toDeleteOrEditName;
    }

    //LIST AND TEST
    @Override
    public void onListItemClick(int clickedItemIndex) {
        substackSelectedList.set(clickedItemIndex, !substackSelectedList.get(clickedItemIndex));
        Log.i("SUBSTACKACTIVITY", "set index: " + clickedItemIndex + " to: " + substackSelectedList.get(clickedItemIndex));
    }

    public void showCardListOnClick() {
        Intent intent = new Intent(this, CardListActivity.class);
//        String[] idArray = selectedSubstackArray(substackIdList);
//        if (idArray.length <= 0) { return; }
//        String[] nameArray = selectedSubstackArray(substackNameList);
//        if (nameArray.length <= 0) { return; }
        ArrayList selectedSubstacks = selectedSubstackArrayList(substackIdList);
        if (selectedSubstacks.isEmpty()) { return; }
        intent.putIntegerArrayListExtra(CardListActivity.SELECTED_SUBSTACKS, selectedSubstacks);
        intent.putStringArrayListExtra(CardListActivity.SELECTED_SUBSTACK_NAMES, selectedSubstackArrayList(substackNameList));
        intent.putIntegerArrayListExtra(CardListActivity.ALL_SUBSTACKS, substackIdList);
        intent.putStringArrayListExtra(CardListActivity.ALL_SUBSTACK_NAMES, substackNameList);
        startActivity(intent);
    }

    public void startTestOnClick() {
        String[] arr = selectedSubstackArray(substackIdList);
        if (arr.length <= 0) { return; }
        //TODO re-implement inverse test or remove.
        boolean testInverse = false; //((CheckBox) findViewById(R.id.inverse_checkbox)).isChecked();
        Intent intent = new Intent(this, TestActivity.class);
        intent.putExtra(TestActivity.SELECTED_SUBSTACKS, arr);
        intent.putExtra(TestActivity.INVERSE, testInverse);
        startActivity(intent);
    }

    private String[] selectedSubstackArray(ArrayList list) {
        ArrayList<String> selectedSubstackIds = new ArrayList<>();
        for (int i = 0; i < substackIdList.size(); i++) {
            if (substackSelectedList.get(i)){
                selectedSubstackIds.add(list.get(i).toString());
            }
        }
        String[] arr = new String[selectedSubstackIds.size()];
        arr = selectedSubstackIds.toArray(arr);
        return arr;
    }

    private ArrayList selectedSubstackArrayList(ArrayList list) {
        ArrayList selectedList = new ArrayList();
        for (int i = 0; i < substackIdList.size(); i++) {
            if (substackSelectedList.get(i)){
                selectedList.add(list.get(i));
            }
        }
        return selectedList;
    }

    //update
    private void updated() {
        loadSubstackList();
        adapter.updated(substackNameList);
    }
}
