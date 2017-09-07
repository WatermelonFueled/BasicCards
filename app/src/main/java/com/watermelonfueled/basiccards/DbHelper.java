package com.watermelonfueled.basiccards;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.watermelonfueled.basiccards.CardsContract.CardEntry;
import com.watermelonfueled.basiccards.CardsContract.StackEntry;
import com.watermelonfueled.basiccards.CardsContract.SubstackEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dapar on 2017-01-15.
 */

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "DbHelper";

    private static DbHelper instance;
    private static Context con;

    private static final String DATABASE_NAME = "cards.db";
    private static final int DATABASE_VERSION = 1;

    private static SQLiteDatabase db;

    public static synchronized DbHelper getInstance(Context context){
        if (instance == null) {
            instance = new DbHelper(context.getApplicationContext());
        }
        db = instance.getWritableDatabase();
        con = context;
        return instance;
    }

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        con = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_CARDS_TABLE_STACK = "CREATE TABLE " +
                StackEntry.TABLE_NAME + " (" +
                StackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                StackEntry.COLUMN_NAME + " TEXT NOT NULL" +
                "); ";
        final String SQL_CREATE_CARDS_TABLE_SUBSTACK = "CREATE TABLE " +
                SubstackEntry.TABLE_NAME + " (" +
                SubstackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SubstackEntry.COLUMN_STACK + " INTEGER NOT NULL, " +
                SubstackEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                " FOREIGN KEY(" + SubstackEntry.COLUMN_STACK + ") REFERENCES " +
                StackEntry.TABLE_NAME + "(_id)" +
                "); ";
        final String SQL_CREATE_CARDS_TABLE_CARD = "CREATE TABLE " +
                CardEntry.TABLE_NAME + " (" +
                CardEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CardEntry.COLUMN_SUBSTACK + " INTEGER NOT NULL, " +
                CardEntry.COLUMN_QUESTION + " TEXT, " +
                CardEntry.COLUMN_ANSWER + " TEXT, " +
                CardEntry.COLUMN_IMAGE + " TEXT, " +
                CardEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                " FOREIGN KEY(" + CardEntry.COLUMN_SUBSTACK + ") REFERENCES " +
                SubstackEntry.TABLE_NAME + "(_id)" +
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_CARDS_TABLE_STACK);
        sqLiteDatabase.execSQL(SQL_CREATE_CARDS_TABLE_SUBSTACK);
        sqLiteDatabase.execSQL(SQL_CREATE_CARDS_TABLE_CARD);

        db = sqLiteDatabase;

        createTutorialStack();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
//        switch (oldVersion) {
//            case 1:
//
//            case 2:
//
//        }
    }

    private void createTutorialStack() {
        long stackId = addStack("Click this Subject, or hold to edit");
        long substackId1 = addSubstack("Categories:", (int)stackId);
        long substackId2 = addSubstack("select 1 or more", (int)stackId);
        long substackId3 = addSubstack("to list/test ", (int)stackId);
        addCard("This is the front side of the card in category 1. Click to flip in List mode.",
                "Add more items by pressing the plus icon at the top right on the app bar.",
                (int)substackId1, null);
        addCard("This is the front side of the card in category 2. Click to flip in List mode.",
                "Add more items by pressing the plus icon at the top right on the app bar.",
                (int)substackId2, null);
        addCard("This is the front side of the card in category 3. Click to flip in List mode.",
                "Add more items by pressing the plus icon at the top right on the app bar.",
                (int)substackId3, null);
    }

    //CREATE
    public long addStack(String name) {
        ContentValues cv = new ContentValues();
        cv.put(StackEntry.COLUMN_NAME, name);
        return db.insert(StackEntry.TABLE_NAME, null, cv);
    }

    public long addSubstack(String name, int stackId) {
        ContentValues cv = new ContentValues();
        cv.put(SubstackEntry.COLUMN_NAME, name);
        cv.put(SubstackEntry.COLUMN_STACK, stackId);
        return db.insert(SubstackEntry.TABLE_NAME, null, cv);
    }

    public long addCard(String question, String answer, int substackId, String imagePath) {
        ContentValues cv = new ContentValues();
        cv.put(CardEntry.COLUMN_QUESTION, question);
        cv.put(CardEntry.COLUMN_ANSWER, answer);
        cv.put(CardEntry.COLUMN_SUBSTACK, substackId);

        cv.put(CardEntry.COLUMN_IMAGE, imagePath);

        return db.insert(CardEntry.TABLE_NAME, null, cv);
    }

    public String storeImage(Uri uri) throws FileNotFoundException, IOException{
        InputStream inputStream = con.getContentResolver().openInputStream(uri);
        File imageFile = createImageFile();
        OutputStream outputStream = new FileOutputStream(imageFile);

        byte[] buffer = new byte[1024];
        int length;
        while((length=inputStream.read(buffer))>0) {
            outputStream.write(buffer,0,length);
        }
        outputStream.close();
        inputStream.close();

        String imagePath = imageFile.getAbsolutePath();
        Log.d(TAG, "Created image file. imagePath:" + imagePath.toString());
        return imagePath;
    }

    public static File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "Card_" + timeStamp + "_";
        File storageDir = con.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(fileName,".jpg",storageDir);
        return imageFile;
    }

    //READ
    public Cursor loadStackTable() {
        String[] columns = {
                StackEntry._ID,
                StackEntry.COLUMN_NAME,
        };
        return db.query(
                StackEntry.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null
        );
    }

    public Cursor loadSubstackTable(int stackId) {
        String[] columns = {
                SubstackEntry._ID,
                SubstackEntry.COLUMN_NAME
        };
        String selection = SubstackEntry.COLUMN_STACK + " = ?";
        String[] selectionArgs = { String.valueOf(stackId) };
        return db.query(
                SubstackEntry.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    public Cursor loadCardsTable(String[] substackIds) {
        String[] columns = {
                CardEntry._ID,
                CardEntry.COLUMN_QUESTION,
                CardEntry.COLUMN_ANSWER,
                CardEntry.COLUMN_IMAGE,
                CardEntry.COLUMN_SUBSTACK
        };
        StringBuilder builder = new StringBuilder();
        builder.append(CardEntry.COLUMN_SUBSTACK).append(" = ?");
        for (int i = 1; i < substackIds.length; i++) {
            builder.append(" OR ").append(CardEntry.COLUMN_SUBSTACK).append(" = ?");
        }
        return db.query(
                CardEntry.TABLE_NAME,
                columns,
                builder.toString(),
                substackIds,
                null,
                null,
                null
        );
    }

    //UPDATE
    public boolean updateStack(int id, String newName){
        ContentValues cv = new ContentValues();
        cv.put(StackEntry.COLUMN_NAME, newName);
        String selection = StackEntry._ID + "=?";
        String[] selectionArgs = { String.valueOf(id) };
        int count = db.update(StackEntry.TABLE_NAME, cv, selection, selectionArgs);
        if (count == 1 ) {
            Log.d(TAG, "Updated stack name to: " + newName);
            return true;
        } else {
            Log.d(TAG, "Failed to update stack name");
            return false;
        }
    }

    public boolean updateSubstack(int id, String newName){
        ContentValues cv = new ContentValues();
        cv.put(SubstackEntry.COLUMN_NAME, newName);
        String selection = SubstackEntry._ID + "=?";
        String[] selectionArgs = { String.valueOf(id) };
        int count = db.update(SubstackEntry.TABLE_NAME, cv, selection, selectionArgs);
        if (count == 1 ) {
            Log.d(TAG, "Updated substack name to: " + newName);
            return true;
        } else {
            Log.d(TAG, "Failed to update substack name");
            return false;
        }
    }

    public boolean updateCard(int id, int substackId, String question, String answer, String imagePath) {
        ContentValues cv = new ContentValues();
        cv.put(CardEntry.COLUMN_SUBSTACK, substackId);
        cv.put(CardEntry.COLUMN_QUESTION, question);
        cv.put(CardEntry.COLUMN_ANSWER, answer);
        cv.put(CardEntry.COLUMN_IMAGE, imagePath);
        String selection = CardEntry._ID +"=?";
        String[] selectionArgs = { String.valueOf(id) };
        int count = db.update(CardEntry.TABLE_NAME, cv, selection, selectionArgs);
        if (count == 1 ) {
            Log.d(TAG, "Updated card: " + id + ", " + substackId + ", " + question + ", " + answer + ", " + imagePath);
            return true;
        } else {
            Log.d(TAG, "Failed to update card");
            return false;
        }
    }

    //DELETE
    public boolean deleteStack(int id) {
        //manual query method
        Cursor cursor = loadSubstackTable(id);
        while(cursor.moveToNext()) {
            if (!deleteSubstack(cursor.getInt(cursor.getColumnIndex(SubstackEntry._ID)))) {
                cursor.close();
                Log.d(TAG, "Failed to delete substack, cancelling remainder of stack delete");
                return false;
            }
        }
        cursor.close();

        if (db.delete(StackEntry.TABLE_NAME, StackEntry._ID + "=" + id, null) == 1) {
            Log.d(TAG, "Deleted stack ID: " + id);
            return true;
        } else {
            Log.d(TAG, "Failed to delete stack ID: " + id);
            return false;
        }
    }

    public boolean deleteSubstack(int id) {
        //db.delete(CardEntry.TABLE_NAME, CardEntry.COLUMN_SUBSTACK + "=" + id, null);
        //Cursor cursor = loadCardsTable(new String[]{Integer.toString(id)});
        Cursor cursor = db.rawQuery("SELECT "+CardEntry._ID+" FROM "+CardEntry.TABLE_NAME+" WHERE "+CardEntry.COLUMN_SUBSTACK+"=?", new String[]{id+""});
        boolean successfulCardDelete = true;
        while (cursor.moveToNext()) {
            int cardId = cursor.getInt(cursor.getColumnIndex(CardEntry._ID));
            successfulCardDelete = deleteCard(cardId);
            if (!successfulCardDelete) {
                //failed to delete a card (failed to delete image file)
                Log.d(TAG, "Failed to delete card ID: " + cardId + ". Cancelling remainder of delete substack ID: " + id);
                return false;
            }
        }
        if (db.delete(SubstackEntry.TABLE_NAME, SubstackEntry._ID + "=" + id, null) == 1) {
            Log.d(TAG, "Deleted substack ID: " + id);
            return true;
        } else {
            Log.d(TAG, "Failed to delete substack ID: " + id);
            return false;
        }
    }

    public boolean deleteCard(int id) {
        Cursor cursor = db.rawQuery("SELECT " + CardEntry.COLUMN_IMAGE + " FROM "
                + CardEntry.TABLE_NAME + " WHERE " + CardEntry._ID + "=?", new String[] {id+""});
        if(cursor.moveToNext()){
            String imagePath = cursor.getString(cursor.getColumnIndex(CardEntry.COLUMN_IMAGE));
            if (imagePath != null && !imagePath.equals("")) {
                File file = new File(imagePath);
                Log.d(TAG, "Deleting: " + file.toString());
                if (!file.delete()) {
                    //failed to delete file
                    //TODO decide whether to still delete row
                    return false;
                }
            }
            Log.d(TAG, "Deleted card - id: " + id);
            db.delete(CardEntry.TABLE_NAME, CardEntry._ID + "=" + id, null);
            return true;
        }
        return  false;
    }

}
