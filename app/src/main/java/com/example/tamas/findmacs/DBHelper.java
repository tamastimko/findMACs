package com.example.tamas.findmacs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Tamas on 2016.02.13..
 */
public class DBHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "AppDataBase";
    public static final String PROMOCIOK_TABLE_NAME = "Promociok";
    public static final String PROMOCIOK_COLUMN_PROMOCIO_AZONOSITO = "promocioazonosito";
    public static final String PROMOCIOK_COLUMN_KATEGORIA = "kategoria";
    public static final String PROMOCIOK_COLUMN_TARTALOM = "tartalom";
    public static final String PROMOCIOK_COLUMN_KEDVEZMENYIPUS = "kedvezmenytipus";
    public static final String PROMOCIOK_COLUMN_KEDVEZMENYMERTEK = "kedvezmenymertek";
    public static final String PROMOCIOK_COLUMN_KEDVELESEK_SZAMA = "kedvelesekszama";
    public static final String PROMOCIOK_COLUMN_NEMKEDVELESEK_SZAMA = "nemkedvelesekszama";
    public static final String PROMOCIOK_COLUMN_MAC_CIM = "maccim";
    public static final String PROMOCIOK_COLUMN_LETREHOZASDATUMA = "letrehozasdatuma";


    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE Promociok " + "(promocioazonosito INTEGER PRIMARY KEY, kategoria TEXT, tartalom TEXT, kedvezmenytipus TEXT, kedvezmenymertek INTEGER, " +
                "kedvelesekszama INTEGER, nemkedvelesekszama INTEGER, maccim TEXT, letrehozasdatuma TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS Promociok");
        onCreate(db);
    }

    public boolean insertPromocio(String kategoria, String tartalom, String kedvezmenyTipus, int kedvezmenyMertek, String macCim)
    {

        int numberOfRows = numberOfRows();
        int promocio_azonosito = numberOfRows + 1;

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        String actualDate = year + "." + month + "." + day + ",";

        int kedvelesekSzama = 0;
        int nemKedvelesekSzama = 0;

        contentValues.put("promocioazonosito", promocio_azonosito);
        contentValues.put("kategoria", kategoria);
        contentValues.put("tartalom", tartalom);
        contentValues.put("kedvezmenytipus", kedvezmenyTipus);
        contentValues.put("kedvezmenymertek", kedvezmenyMertek);
        contentValues.put("kedvelesekszama", kedvelesekSzama);
        contentValues.put("nemkedvelesekszama", nemKedvelesekSzama);
        contentValues.put("maccim", macCim);
        contentValues.put("letrehozasdatuma", actualDate);

        try
        {
            db.insert("Promociok", null, contentValues);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

    }

    public int numberOfRows()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        int numberOfRows = (int) DatabaseUtils.queryNumEntries(db,PROMOCIOK_TABLE_NAME);
        return numberOfRows;
    }

    public Cursor getRecord(String mac_cim)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Promociok WHERE maccim='"+mac_cim+"'",null);
        return cursor;
    }


}
