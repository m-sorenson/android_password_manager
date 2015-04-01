package com.sorenson.michael.passwordmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.UUID;

public class ProfileDatabaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DB_NAME = "profiles.sql";

    private static final String UUIDCol = "uuid";
    private static final String TitleCol = "title";
    private static final String UrlCol = "url";
    private static final String UserNameCol = "username";
    private static final String LengthCol = "length";
    private static final String LowerCol = "lower";
    private static final String UpperCol = "upper";
    private static final String DigitsCol = "digits";
    private static final String PunctCol = "punctuation";
    private static final String SpacesCol = "spaces";
    private static final String IncludeCol = "include";
    private static final String ExcludeCol = "exclude";


    public ProfileDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table profiles (_id integer primary key autoincrement," +
                "uuid varchar(40), title varchar(100), url varchar(100), username varchar(100)," +
                " length integer, lower integer, upper integer, digits integer," +
                " punctuation integer, spaces integer, include varchar(50), exclude varchar(50))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertProfile(Profile p) {
        ContentValues cv = new ContentValues();
        cv.put(UUIDCol, p.uuid.toString());
        cv.put(TitleCol, p.title);
        cv.put(UrlCol, p.url);
        cv.put(UserNameCol, p.username);
        cv.put(LengthCol, p.length);
        cv.put(LowerCol, p.lower);
        cv.put(UpperCol, p.upper);
        cv.put(DigitsCol, p.digits);
        cv.put(PunctCol, p.punctuation);
        cv.put(SpacesCol, p.spaces);
        cv.put(IncludeCol, p.include);
        cv.put(ExcludeCol, p.exclude);
        return getWritableDatabase().insert("profiles", null, cv);
    }

    public long updateProfile(Profile p) {
        ContentValues cv = new ContentValues();
        cv.put(UUIDCol, p.uuid.toString());
        cv.put(TitleCol, p.title);
        cv.put(UrlCol, p.url);
        cv.put(UserNameCol, p.username);
        cv.put(LengthCol, p.length);
        cv.put(LowerCol, p.lower);
        cv.put(UpperCol, p.upper);
        cv.put(DigitsCol, p.digits);
        cv.put(PunctCol, p.punctuation);
        cv.put(SpacesCol, p.spaces);
        cv.put(IncludeCol, p.include);
        cv.put(ExcludeCol, p.exclude);
        return getWritableDatabase().update("profiles", cv, UUIDCol+"='"+p.uuid.toString()+"'", null);
    }

    public boolean deleteProfile(Profile p) {
        return getWritableDatabase().delete("profiles", UUIDCol+"='"+p.uuid.toString()+"'", null)>0;
    }

 public ProfileCursor getProfiles() {
        Cursor c = getReadableDatabase().rawQuery("select * from profiles", null);
        return new ProfileCursor(c);
    }

    public static class ProfileCursor extends CursorWrapper {
        public ProfileCursor(Cursor c) {
            super(c);
        }

        public Profile getProfile() {
            Profile temp = new Profile();
            temp.uuid = UUID.fromString(getString(getColumnIndex(UUIDCol)));
            temp.title = getString(getColumnIndex(TitleCol));
            temp.url = getString(getColumnIndex(UrlCol));
            temp.username = getString(getColumnIndex(UserNameCol));
            temp.length = getInt(getColumnIndex(LengthCol));
            temp.lower = getInt(getColumnIndex(LowerCol))>0;
            temp.upper = getInt(getColumnIndex(UpperCol))>0;
            temp.digits = getInt(getColumnIndex(DigitsCol))>0;
            temp.punctuation = getInt(getColumnIndex(PunctCol))>0;
            temp.spaces = getInt(getColumnIndex(SpacesCol))>0;
            temp.include = getString(getColumnIndex(IncludeCol));
            temp.exclude = getString(getColumnIndex(ExcludeCol));
            return temp;
        }
    }

}
