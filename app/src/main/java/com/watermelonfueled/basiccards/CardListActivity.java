package com.watermelonfueled.basiccards;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static com.watermelonfueled.basiccards.CardsContract.CardEntry;

public class CardListActivity extends AppCompatActivity
        implements CardListViewAdapter.ListItemClickListener,
        DeleteDialog.DeleteDialogListener,
        AddCardDialog.AddCardDialogListener {

    private final String TAG = "CardListActivity";

    private DbHelper dbHelper;
    public final static String  SELECTED_SUBSTACKS = "SelectedSubstacks",
                                SELECTED_SUBSTACK_NAMES = "SelectedSubstackNames",
                                ALL_SUBSTACKS = "AllSubstacks",
                                ALL_SUBSTACK_NAMES = "AllSubstackNames";
    private CardListViewAdapter adapter;
    private ArrayList<Integer> cardIdList, cardSubstackIdList, substackIds, allSubstackIds;
    private ArrayList<String> cardFrontList, cardBackList, substackNames, allSubstackNames;
    private SparseArray<String> cardImageList;
//    private String[] substackIds;
    private int toDeleteOrEditIndex, addOrEditCardToSubstackIndex;
    private boolean editCardMode, editOldImageExistsNotPreserved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        if (savedInstanceState != null) {
            substackIds = savedInstanceState.getIntegerArrayList("substackIds");
            substackNames = savedInstanceState.getStringArrayList("substackNames");
            allSubstackIds = savedInstanceState.getIntegerArrayList("allSubstackIds");
            allSubstackNames = savedInstanceState.getStringArrayList("allSubstackNames");
            editCardMode = savedInstanceState.getBoolean("editCardMode");
            toDeleteOrEditIndex = savedInstanceState.getInt("toDeleteOrEditIndex");
            editOldImageExistsNotPreserved = savedInstanceState.getBoolean("editOldImageExistsNotPreserved");
            if (savedInstanceState.containsKey("addCardDialogBundle")) {
                addCardDialogBundle = savedInstanceState.getBundle("addCardDialogBundle");
            }
        } else {
            Intent intent = getIntent();
            substackIds = intent.getIntegerArrayListExtra(SELECTED_SUBSTACKS);
            substackNames = intent.getStringArrayListExtra(SELECTED_SUBSTACK_NAMES);
            allSubstackIds = intent.getIntegerArrayListExtra(ALL_SUBSTACKS);
            Log.d(TAG,"allSubstackIds: " + allSubstackIds.toString());
            allSubstackNames = intent.getStringArrayListExtra(ALL_SUBSTACK_NAMES);
            Log.d(TAG,"allSubstackNames: " + allSubstackNames.toString());
            editCardMode = false;
        }

        dbHelper = DbHelper.getInstance(this);
        loadCards();

        setView();


    }

    @Override
    protected void onResume() {
        if (addCardDialog != null) {
            addCardDialog.show(getSupportFragmentManager(), "AddCardDialog");
        }
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putIntegerArrayList("substackIds", substackIds);
        outState.putStringArrayList("substackNames", substackNames);
        outState.putIntegerArrayList("allSubstackIds", allSubstackIds);
        outState.putStringArrayList("allSubstackNames", allSubstackNames);
        outState.putBoolean("editCardMode", editCardMode);
        outState.putInt("toDeleteOrEditIndex", toDeleteOrEditIndex);
        outState.putBoolean("editOldImageExistsNotPreserved", editOldImageExistsNotPreserved);
        if (addCardDialogBundle != null) {
            outState.putBundle("addCardDialogBundle", addCardDialogBundle);
        }
        super.onSaveInstanceState(outState);
    }

    private void setView() {
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);

        //TODO make faster scrolling and loading
        rv.setHasFixedSize(true);
        rv.setItemViewCacheSize(20);
        rv.setDrawingCacheEnabled(true);
        rv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        adapter = new CardListViewAdapter(this, cardFrontList, cardBackList, cardImageList, this);
        rv.setAdapter(adapter);
    }

    private void loadCards() {
        String[] substackIdsStringArray = new String[substackIds.size()];
        for (int i = 0; i < substackIdsStringArray.length; i++) {
            substackIdsStringArray[i] = String.valueOf(substackIds.get(i));
        }
        Cursor cursor = dbHelper.loadCardsTable(substackIdsStringArray);
        cardFrontList = new ArrayList<>(cursor.getCount());
        cardBackList = new ArrayList<>(cursor.getCount());
        cardIdList = new ArrayList<>(cursor.getCount());
        cardSubstackIdList = new ArrayList<>(cursor.getCount());
        cardImageList = new SparseArray<>(cursor.getCount());
        while(cursor.moveToNext()){
            Log.d(TAG, "card +1");
            cardFrontList.add(cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_QUESTION)));
            cardBackList.add(cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_ANSWER)));
            cardIdList.add(cursor.getInt(cursor.getColumnIndex(CardEntry._ID)));
            cardSubstackIdList.add(cursor.getInt(cursor.getColumnIndex(CardEntry.COLUMN_SUBSTACK)));
            cardImageList.setValueAt(cursor.getPosition(),cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_IMAGE)));
        }
        cursor.close();
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {

    }

    // ADD/EDIT CARD //
    private AddCardDialog addCardDialog;
    private Bundle addCardDialogBundle;

    public void addCardButtonOnClick(View button) {
        editCardMode = false;
        addCardDialog =  new AddCardDialog();
        addCardDialog.setSubstackNames(substackNames);
        addCardDialog.show(getSupportFragmentManager(), "AddCardDialog");
    }

    public void editButtonOnClick(View button) {
        editCardMode = true;
        toDeleteOrEditIndex = (int) button.getTag();

        Bundle bundle = new Bundle();
        String imagePath = cardImageList.valueAt(toDeleteOrEditIndex);
        if (imagePath != null) {
            bundle.putString("imagePath", imagePath);
            bundle.putBoolean("fileExists", true);
            bundle.putBoolean("photoExists", true);
            editOldImageExistsNotPreserved = true;
        } else {
            bundle.putString("imagePath", "");
            bundle.putBoolean("fileExists", false);
            bundle.putBoolean("photoExists", false);
            editOldImageExistsNotPreserved = false;
        }
        //bundle.putStringArrayList("substack",substackNames);
        bundle.putStringArrayList("substack", allSubstackNames);
        bundle.putString("front", cardFrontList.get(toDeleteOrEditIndex));
        bundle.putString("back", cardBackList.get(toDeleteOrEditIndex));

        int substackIndex = 0;
        int substackId = cardSubstackIdList.get(toDeleteOrEditIndex);
//        for (int i = 0; i < substackIds.length; i++) {
//            if (Integer.parseInt(substackIds[i]) == substackId) {
        for (int i = 0; i < allSubstackIds.size(); i++) {
            if (allSubstackIds.get(i).intValue() == substackId) {
                substackIndex = i;
                break;
            }
        }
        bundle.putInt("selectedSubstackIndex",substackIndex);

        addCardDialog = new AddCardDialog();
        addCardDialog.unbundleState(bundle);
        addCardDialog.show(getSupportFragmentManager(), "AddCardDialog");
    }

    @Override
    public void onAddCardDialogSelectSubstack(int position) {
        addOrEditCardToSubstackIndex = position;
    }

    @Override
    public void onAddCardDialogPositiveClick(DialogFragment dialog, String front, String back,
                                                boolean photoExists, Uri imageUri, String imagePath) {
        String path = "";
        if (photoExists) {
            if (imageUri == null) {
                path = imagePath;
            } else {
                try {
                    path = dbHelper.storeImage(imageUri);
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }
            }
        }

        if (editCardMode) {
            // Edit card
            if (!editOldImageExistsNotPreserved && cardImageList.get(toDeleteOrEditIndex) != null) {
                // delete old image file
                File oldFile = new File(cardImageList.get(toDeleteOrEditIndex));
                oldFile.delete();
            }
            dbHelper.updateCard(cardIdList.get(toDeleteOrEditIndex),
                    allSubstackIds.get(addOrEditCardToSubstackIndex),
                    front, back, path);
        } else {
            // Add new card
            dbHelper.addCard(front, back, substackIds.get(addOrEditCardToSubstackIndex), path);
        }
        updated();
        clearAddOrEdit();
    }

    @Override
    public void onAddCardDialogNegativeClick() {
        if (!editOldImageExistsNotPreserved) {
            addCardDialog.checkAndDeleteCapturedPhoto();
        }
        clearAddOrEdit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult called - requestCode: " + requestCode+ " resultCode: " + resultCode);
        super.onActivityResult(requestCode,resultCode,data);
        addCardDialog = new AddCardDialog();
        addCardDialog.unbundleState(addCardDialogBundle);
        if (resultCode == Activity.RESULT_OK) {
            addCardDialog.photoExists = true;

            switch (requestCode) {
                case AddCardDialog.PICK_IMAGE:
                    addCardDialog.imageUri = data.getData();
                    addCardDialog.imagePath = null;
                    addCardDialog.fileExists = false;
                    break;
                case AddCardDialog.TAKE_PHOTO:
                    addCardDialog.fileExists = true;
                    addCardDialog.imageUri = null;
                    break;
            }

            if (editCardMode && editOldImageExistsNotPreserved) {
                addCardDialog.oldPath = null;
                editOldImageExistsNotPreserved = false;
            } else {
                addCardDialog.checkAndDeleteCapturedPhoto();
            }
        } else {
            // not okay result code
            if (addCardDialog.oldPath != null) {
                addCardDialog.imagePath = addCardDialog.oldPath;
                addCardDialog.oldPath = null;
            }
        }

    }

    public void setAddCardDialogBundle(Bundle bundle) { addCardDialogBundle = bundle; }
    public void startAddCardDialogIntent(Intent intent, int request) {
        addCardDialog.dismiss();
        addCardDialog = null;
        startActivityForResult(intent, request);
    }

    @Override
    public void onAddCardDialogImageRemove() {
        if (!editCardMode || !editOldImageExistsNotPreserved) { return; }
        addCardDialog.oldPath = null;
        editOldImageExistsNotPreserved = false;
    }

    private void clearAddOrEdit() {
        toDeleteOrEditIndex = 0;
        addOrEditCardToSubstackIndex = 0;
        addCardDialog.dismiss();
        addCardDialog = null;
        addCardDialogBundle = null;
    }

    public void deleteButtonOnClick(View button) {
        toDeleteOrEditIndex = (int) button.getTag();
        DeleteDialog dialog = new DeleteDialog();
        dialog.setConfirmMessage("Delete: " + cardFrontList.get(toDeleteOrEditIndex)
                + "/" + cardBackList.get(toDeleteOrEditIndex) + " ?");
        dialog.show(getSupportFragmentManager(), "DeleteCardDialog");
    }

    @Override
    public void onDeleteDialogPositiveClick(DialogFragment dialog) {
        dbHelper.deleteCard(cardIdList.get(toDeleteOrEditIndex));
        updated();
    }

    private void updated() {
        loadCards();
        adapter.updated(cardFrontList, cardBackList, cardImageList);
    }

}
