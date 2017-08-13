package com.watermelonfueled.basiccards;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import static com.watermelonfueled.basiccards.CardsContract.StackEntry;

public class MainActivity extends AppCompatActivity
        implements  StackViewAdapter.ListItemClickListener,
                    AddStackDialog.AddStackDialogListener,
        DeleteDialog.DeleteDialogListener {

    private static final String TAG = "MainActivity";
    private Toast toast;
    private DbHelper dbHelper;
    private ArrayList<String> stackNameList;
    private ArrayList<Integer> stackIdList;
    private StackViewAdapter adapter;
    private int toDeleteOrEditId;
    private String toDeleteOrEditName;

    Type dialogType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = DbHelper.getInstance(this);
        loadStackList();
        setView();
    }

    private void setView(){
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);
        adapter = new StackViewAdapter(this, stackNameList, R.layout.stack_view);
        rv.setAdapter(adapter);
    }

    private void loadStackList() {
        Cursor cursor = dbHelper.loadStackTable();
        stackNameList = new ArrayList<>(cursor.getCount());
        stackIdList = new ArrayList<>(cursor.getCount());
        while(cursor.moveToNext()){
            stackNameList.add(cursor.getString(cursor.getColumnIndex(StackEntry.COLUMN_NAME)));
            stackIdList.add(cursor.getInt(cursor.getColumnIndex(StackEntry._ID)));
        }
        cursor.close();
    }

    // ACTION BAR //
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stackmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_stack:
                dialogType = Type.CREATE;
                DialogFragment dialog = new AddStackDialog();
                dialog.show(getSupportFragmentManager(), "AddStackDialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Intent intent = new Intent(this, SubstackActivity.class);
        intent.putExtra(StackEntry._ID, stackIdList.get(clickedItemIndex));
        intent.putExtra(StackEntry.COLUMN_NAME, stackNameList.get(clickedItemIndex));
        startActivity(intent);
    }

    @Override
    public void onAddStackDialogPositiveClick(DialogFragment dialog, String stackName) {
        String toast;
        switch (getType()) {
            case CREATE:
                dbHelper.addStack(stackName);
                toast = "Added: " + stackName;
                break;
            case EDIT:
                if (stackName.equals(toDeleteOrEditName)){
                    return; //no change
                }
                dbHelper.updateStack(toDeleteOrEditId, stackName);
                toast = "Changed " + toDeleteOrEditName + " to " + stackName;
                break;
            default:
                toast = "";
        }
        updated(); //TODO update single item instead of general update
        makeToast(toast);
    }

    public void editButtonOnClick(View button) {
        dialogType = Type.EDIT;
        int position = (int) button.getTag();
        toDeleteOrEditId = stackIdList.get(position);
        toDeleteOrEditName = stackNameList.get(position);
        DialogFragment dialog = new AddStackDialog();
        dialog.show(getSupportFragmentManager(), "EditStackDialog");
    }

    public void deleteButtonOnClick(View button) {
        int position = (int) button.getTag();
        toDeleteOrEditId = stackIdList.get(position);
        toDeleteOrEditName = stackNameList.get(position);
        DeleteDialog dialog = new DeleteDialog();
        dialog.setConfirmMessage("Are you sure you want to delete: " +
                toDeleteOrEditName + "?");
        dialog.show(getSupportFragmentManager(), "DeleteStackDialog");
    }

    @Override
    public void onDeleteDialogPositiveClick(DialogFragment dialog) {
        dbHelper.deleteStack(toDeleteOrEditId);
        updated();
        makeToast("Deleted: " + toDeleteOrEditName);
    }

    @Override
    public Type getType(){
        return dialogType;
    }

    @Override
    public String getNameToEdit() {
        return toDeleteOrEditName;
    }

    private void updated() {
        loadStackList();
        adapter.updated(stackNameList);
    }

    private void makeToast(String msg){
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        DbHelper.getInstance(this).close();
        super.onDestroy();
    }
}
