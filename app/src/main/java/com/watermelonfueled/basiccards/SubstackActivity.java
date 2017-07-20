package com.watermelonfueled.basiccards;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static com.watermelonfueled.basiccards.CardsContract.StackEntry;
import static com.watermelonfueled.basiccards.CardsContract.SubstackEntry;

public class SubstackActivity extends AppCompatActivity
        implements SubstackViewAdapter.ListItemClickListener,
        AddStackDialog.AddStackDialogListener,
        DeleteDialog.DeleteDialogListener,
        AddCardDialog.AddCardDialogListener{

    private final String TAG = "SubstackActivity";

    private DbHelper dbHelper;
    private int stackId, toDeleteIndex, addCardToSubstackIndex = 0 ;
    private SubstackViewAdapter adapter;
    private ArrayList<String> substackNameList;
    private ArrayList<Integer> substackIdList;
    private ArrayList<Boolean> substackSelectedList;
    private String toDeleteName;

    private final String    STACK_ID_KEY = "stackidkey",
                            ADD_CARD_SUBSTACK_INDEX_KEY = "addcardsubstackindexkey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_substack);

        if (savedInstanceState != null) {
            stackId = savedInstanceState.getInt(STACK_ID_KEY);
            addCardToSubstackIndex = savedInstanceState.getInt(ADD_CARD_SUBSTACK_INDEX_KEY, 0);
            if (savedInstanceState.containsKey("addCardDialogBundle")) {
                addCardDialogBundle = savedInstanceState.getBundle("addCardDialogBundle");
            }
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
    protected void onResume() {
        if (addCardDialog != null) {
            addCardDialog.show(getSupportFragmentManager(), "AddCardDialog");
        }
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putInt(STACK_ID_KEY, stackId);
        outState.putInt(ADD_CARD_SUBSTACK_INDEX_KEY, addCardToSubstackIndex);
        if (addCardDialogBundle != null) {
            outState.putBundle("addCardDialogBundle", addCardDialogBundle);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            stackId = savedInstanceState.getInt(STACK_ID_KEY);
            addCardToSubstackIndex = savedInstanceState.getInt(ADD_CARD_SUBSTACK_INDEX_KEY, 0);
            if(savedInstanceState.containsKey("addCardDialogBundle")) {
                addCardDialogBundle = savedInstanceState.getBundle("addCardDialogBundle");
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
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


    private AddCardDialog addCardDialog;
    private Bundle addCardDialogBundle;

    public void addCardOnClick(View button) {
        addCardDialog = new AddCardDialog();
        addCardDialog.setSubstackNames(substackNameList);
        addCardDialog.show(getSupportFragmentManager(), "AddCardDialog");
    }

    @Override
    public void onAddCardDialogSelectSubstack(int position) {
        addCardToSubstackIndex = position;
    }
    @Override
    public int getSelectedSubstack(){
        return addCardToSubstackIndex;
    }

    @Override
    public void onAddCardDialogPositiveClick(DialogFragment dialog, String front, String back,
                                             boolean photoExists, Uri imageUri, String imagePath) {
        if (photoExists) {
            if (imageUri == null) {
                dbHelper.addCard(front, back, substackIdList.get(addCardToSubstackIndex), imagePath);
            } else {
                try {
                    dbHelper.addCard(front, back, substackIdList.get(addCardToSubstackIndex),
                            dbHelper.storeImage(imageUri));
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }
            }
        } else {
            dbHelper.addCard(front, back, substackIdList.get(addCardToSubstackIndex), "");
        }
        updated();
        addCardToSubstackIndex = 0;
        addCardDialog.dismiss();
        addCardDialog = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult called - requestCode: " + requestCode+ " resultCode: " + resultCode);
        super.onActivityResult(requestCode,resultCode,data);
        addCardDialog = new AddCardDialog();
        addCardDialog.unbundleState(addCardDialogBundle);
//        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
        if (resultCode != Activity.RESULT_OK) {
            // not okay result code
            if (addCardDialog.oldPath != null) {
                addCardDialog.imagePath = addCardDialog.oldPath;
            }
            return;
        }

        addCardDialog.photoExists = true;

        switch (requestCode) {
            case AddCardDialog.PICK_IMAGE:
                addCardDialog.imageUri = data.getData();
                addCardDialog.imagePath = null;
                addCardDialog.oldPath = null;
                addCardDialog.checkAndDeleteCapturedPhoto();
                addCardDialog.fileExists = false;
                break;
            case AddCardDialog.TAKE_PHOTO:
                addCardDialog.checkAndDeleteCapturedPhoto(); // delete previous captured photo
                addCardDialog.fileExists = true;
                addCardDialog.imageUri = null;
                break;
        }
        Log.d(TAG, "AddCardDialog Booleans - photoexists " + addCardDialog.photoExists+ ", fileexists "+addCardDialog.fileExists);
    }

    public void setAddCardDialogBundle(Bundle bundle) { addCardDialogBundle = bundle; }
    public void startAddCardDialogIntent(Intent intent, int request) {
        addCardDialog.dismiss();
        addCardDialog = null;
        startActivityForResult(intent, request);
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
