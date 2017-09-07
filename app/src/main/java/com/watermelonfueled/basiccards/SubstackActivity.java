package com.watermelonfueled.basiccards;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import static com.watermelonfueled.basiccards.CardsContract.StackEntry;
import static com.watermelonfueled.basiccards.CardsContract.SubstackEntry;

//TODO extend MainActivity and reduce repetition
public class SubstackActivity extends AppCompatActivity
        implements StackViewAdapter.ListItemClickListener,
        AddStackDialog.AddStackDialogListener,
        DeleteDialog.DeleteDialogListener {

    private final String TAG = "SubstackActivity";

    private DbHelper dbHelper;
    private int stackId, toDeleteOrEditId, toDeleteOrEditPosition, numSelectedSubstacks;
    private StackViewAdapter adapter;
    private ArrayList<String> substackNameList;
    private ArrayList<Integer> substackIdList;
    private ArrayList<Boolean> substackSelectedList;
    private String toDeleteOrEditName, stackName;
    private FloatingActionButton listFloatingButton, testFloatingButton;
    Type dialogType;

    private final String STACK_ID_KEY = "stackidkey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_substack);
        dbHelper = DbHelper.getInstance(this);

        if (savedInstanceState != null) {
            stackId = savedInstanceState.getInt(STACK_ID_KEY);
            substackSelectedList = (ArrayList<Boolean>) savedInstanceState.getSerializable("substackSelectedList");
            numSelectedSubstacks = savedInstanceState.getInt("numSelectedSubstacks");
            stackName = savedInstanceState.getString("stackName");
        } else {
            Intent intent = getIntent();
            stackId = intent.getIntExtra(StackEntry._ID, 0);
            numSelectedSubstacks = 0;
            stackName = intent.getStringExtra(StackEntry.COLUMN_NAME);
        }

        loadSubstackList();
        setView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putInt(STACK_ID_KEY, stackId);
        outState.putSerializable("substackSelectedList", substackSelectedList);
        outState.putInt("numSelectedSubstacks", numSelectedSubstacks);
        outState.putString("stackName", stackName);
        super.onSaveInstanceState(outState);
    }

    private void setView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Categories | " + stackName);
        setSupportActionBar(toolbar);

        listFloatingButton = (FloatingActionButton) findViewById(R.id.list_floating_button);
        listFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCardListOnClick();
            }
        });
        testFloatingButton = (FloatingActionButton) findViewById(R.id.test_floating_button);
        testFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTestOnClick();
            }
        });

        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);
        adapter = new StackViewAdapter(this, substackNameList, R.layout.substack_view, this);
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
        updateTestAndListButtonVisibility(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_substack:
                addSubstackOnClick();
                return true;
            case R.id.menu_select_all:
                if (numSelectedSubstacks < substackSelectedList.size()) {
                    numSelectedSubstacks = substackSelectedList.size();
                    for (int i = 0; i < substackSelectedList.size(); i++) {
                        substackSelectedList.set(i, true);
                    }
                    //api 24 (android 7.0) required
                    /*substackSelectedList.replaceAll(new UnaryOperator<Boolean>() {
                        @Override
                        public Boolean apply(Boolean aBoolean) {
                            return true;
                        }
                    });*/
                    updateTestAndListButtonVisibility(true);
                } else {
                    for (int i = 0; i < substackSelectedList.size(); i++) {
                        substackSelectedList.set(i, false);
                    }
                    numSelectedSubstacks = 0;
                    updateTestAndListButtonVisibility(false);
                }
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Select All pressed. SubstackSelectedList: " + substackSelectedList.toString());
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
                long id = dbHelper.addSubstack(substackName,stackId);
                if (id == -1) {
                    //failed to add
                    return;
                } else {
                    substackSelectedList.add(false);
                    substackNameList.add(substackName);
                    substackIdList.add((int)id);
                    adapter.notifyItemInserted(adapter.getItemCount()-1);
                }
                break;
            case EDIT:
                if (substackName.equals(toDeleteOrEditName)) {
                    return; //no change
                }
                if (dbHelper.updateSubstack(toDeleteOrEditId, substackName)) {
                    substackNameList.set(toDeleteOrEditPosition, substackName);
                    adapter.notifyItemChanged(toDeleteOrEditPosition);
                }
                break;
        }
    }

    public void editButtonOnClick(View button) {
        dialogType = Type.EDIT;
        toDeleteOrEditPosition = (int) button.getTag();
        toDeleteOrEditId = substackIdList.get(toDeleteOrEditPosition);
        toDeleteOrEditName = substackNameList.get(toDeleteOrEditPosition);
        DialogFragment dialog = new AddStackDialog();
        dialog.show(getSupportFragmentManager(), "DeleteSubstackDialog");
    }

    public void deleteButtonOnClick(View button) {
        toDeleteOrEditPosition = (int) button.getTag();
        toDeleteOrEditId = substackIdList.get(toDeleteOrEditPosition);
        toDeleteOrEditName = substackNameList.get(toDeleteOrEditPosition);
        DeleteDialog dialog = new DeleteDialog();
        dialog.setConfirmMessage("Are you sure you want to delete: " +
                toDeleteOrEditName + "? All its contents will be deleted.");
        dialog.show(getSupportFragmentManager(), "DeleteSubstackDialog");
    }

    @Override
    public void onDeleteDialogPositiveClick(DialogFragment dialog) {
        if (dbHelper.deleteSubstack(toDeleteOrEditId)) {
            substackIdList.remove(toDeleteOrEditPosition);
            substackNameList.remove(toDeleteOrEditPosition);
            substackSelectedList.remove(toDeleteOrEditPosition);
            adapter.notifyItemRemoved(toDeleteOrEditPosition);
        }
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
        boolean checked = substackSelectedList.get(clickedItemIndex);
        if (checked) { numSelectedSubstacks--; } else { numSelectedSubstacks++; }
        updateTestAndListButtonVisibility(!checked);
        substackSelectedList.set(clickedItemIndex, !checked);
        Log.i("SUBSTACKACTIVITY", "set index: " + clickedItemIndex + " to: " + substackSelectedList.get(clickedItemIndex));

    }

    private void updateTestAndListButtonVisibility(boolean increased) {
        if (increased && numSelectedSubstacks >= 1) {
            testFloatingButton.show();
            listFloatingButton.show();
        } else if (!increased && numSelectedSubstacks == 0) {
            testFloatingButton.hide();
            listFloatingButton.hide();
        }
    }

    public void showCardListOnClick() {
        ArrayList selectedSubstacks = selectedSubstackArrayList(substackIdList);
        if (selectedSubstacks.isEmpty()) { return; }
        Intent intent = new Intent(this, CardListActivity.class);
        intent.putIntegerArrayListExtra(CardListActivity.SELECTED_SUBSTACKS, selectedSubstacks);
        intent.putStringArrayListExtra(CardListActivity.SELECTED_SUBSTACK_NAMES, selectedSubstackArrayList(substackNameList));
        intent.putIntegerArrayListExtra(CardListActivity.ALL_SUBSTACKS, substackIdList);
        intent.putStringArrayListExtra(CardListActivity.ALL_SUBSTACK_NAMES, substackNameList);
        startActivity(intent);
    }

    public void startTestOnClick() {
        ArrayList selectedSubstacks = selectedSubstackArrayList(substackIdList);
        if (selectedSubstacks.isEmpty()) { return; }
        //TODO re-implement inverse test or remove.
        boolean testInverse = false; //((CheckBox) findViewById(R.id.inverse_checkbox)).isChecked();
        Intent intent = new Intent(this, TestActivity.class);
        intent.putIntegerArrayListExtra(TestActivity.SELECTED_SUBSTACKS, selectedSubstacks);
        intent.putStringArrayListExtra(TestActivity.SELECTED_SUBSTACK_NAMES, selectedSubstackArrayList(substackNameList));
        intent.putExtra(TestActivity.INVERSE, testInverse);
        startActivity(intent);
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
}
