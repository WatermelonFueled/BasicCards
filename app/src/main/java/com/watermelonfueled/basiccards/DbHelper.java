package com.watermelonfueled.basiccards;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.watermelonfueled.basiccards.CardsContract.*;

/**
 * Created by dapar on 2017-01-15.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static DbHelper instance;

    private static final String DATABASE_NAME = "cards.db";
    private static final int DATABASE_VERSION = 1;

    private static SQLiteDatabase db;

    public static synchronized DbHelper getInstance(Context context){
        if (instance == null) {
            instance = new DbHelper(context.getApplicationContext());
        }
        db = instance.getWritableDatabase();
        return instance;
    }

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
                CardEntry.COLUMN_QUESTION + " TEXT NOT NULL, " +
                CardEntry.COLUMN_ANSWER + " TEXT NOT NULL, " +
                CardEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                " FOREIGN KEY(" + CardEntry.COLUMN_SUBSTACK + ") REFERENCES " +
                SubstackEntry.TABLE_NAME + "(_id)" +
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_CARDS_TABLE_STACK);
        sqLiteDatabase.execSQL(SQL_CREATE_CARDS_TABLE_SUBSTACK);
        sqLiteDatabase.execSQL(SQL_CREATE_CARDS_TABLE_CARD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int j) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CardEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SubstackEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StackEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

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

    public void addStack(String name) {
        ContentValues cv = new ContentValues();
        cv.put(StackEntry.COLUMN_NAME, name);
        db.insert(StackEntry.TABLE_NAME, null, cv);
    }

    public void addSubstack(String name, int stackId) {
        ContentValues cv = new ContentValues();
        cv.put(SubstackEntry.COLUMN_NAME, name);
        cv.put(SubstackEntry.COLUMN_STACK, stackId);
        db.insert(SubstackEntry.TABLE_NAME, null, cv);
    }

    public void addCard(String question, String answer, int substackId) {
        ContentValues cv = new ContentValues();
        cv.put(CardEntry.COLUMN_QUESTION, question);
        cv.put(CardEntry.COLUMN_ANSWER, answer);
        cv.put(CardEntry.COLUMN_SUBSTACK, substackId);
        db.insert(CardEntry.TABLE_NAME, null, cv);
    }

    public void deleteStack(int id) {
        //manual query method
        Cursor cursor = loadSubstackTable(id);
        while(cursor.moveToNext()) {
            deleteSubstack(cursor.getInt(cursor.getColumnIndex(SubstackEntry._ID)));
        }
        cursor.close();

        db.delete(StackEntry.TABLE_NAME, StackEntry._ID + "=" + id, null);
    }

    public void deleteSubstack(int id) {
        db.delete(CardEntry.TABLE_NAME, CardEntry.COLUMN_SUBSTACK + "=" + id, null);
        db.delete(SubstackEntry.TABLE_NAME, SubstackEntry._ID + "=" + id, null);
    }

    public void deleteCard(int id) {
        db.delete(CardEntry.TABLE_NAME, CardEntry._ID + "=" + id, null);
    }

}
