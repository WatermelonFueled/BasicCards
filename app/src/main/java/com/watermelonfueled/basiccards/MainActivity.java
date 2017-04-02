package com.watermelonfueled.basiccards;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import static com.watermelonfueled.basiccards.CardsContract.*;

public class MainActivity extends AppCompatActivity
        implements  StackViewAdapter.ListItemClickListener,
                    AddStackDialog.AddStackDialogListener,
                    DeleteStackDialog.DeleteStackDialogListener{

    private static final String TAG = "MainActivity";
    private Toast toast;
    private DbHelper dbHelper;
    private ArrayList<String> stackNameList;
    private ArrayList<Integer> stackIdList;
    private StackViewAdapter adapter;
    private int toDeleteIndex;

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
        adapter = new StackViewAdapter(this, stackNameList);
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

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Intent intent = new Intent(this, SubstackActivity.class);
        intent.putExtra(StackEntry._ID, stackIdList.get(clickedItemIndex));
        intent.putExtra(StackEntry.COLUMN_NAME, stackNameList.get(clickedItemIndex));
        startActivity(intent);
    }

    public void addStackOnClick(View button){
        DialogFragment dialog = new AddStackDialog();
        dialog.show(getSupportFragmentManager(), "AddStackDialog");
    }

    @Override
    public void onAddStackDialogPositiveClick(DialogFragment dialog, String stackName) {
        dbHelper.addStack(stackName);
        updated();
        makeToast("Added: "+stackName);
    }

    public void deleteButtonOnClick(View button) {
        toDeleteIndex = (int) button.getTag();
        DeleteStackDialog dialog = new DeleteStackDialog();
        dialog.setStackName(stackNameList.get(toDeleteIndex));
        dialog.show(getSupportFragmentManager(), "DeleteStackDialog");
    }

    @Override
    public void onDeleteStackDialogPositiveClick(DialogFragment dialog) {
        dbHelper.deleteStack(stackIdList.get(toDeleteIndex));
        updated();
        String stackName = stackNameList.get(toDeleteIndex);
        makeToast("Deleted: " + stackName);
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
