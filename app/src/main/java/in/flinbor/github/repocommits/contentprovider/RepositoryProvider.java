/*
 * Copyright 2016 Flinbor Bogdanov Oleksandr
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package in.flinbor.github.repocommits.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Implementation of content provider, contain one table fro repositories
 */
public class RepositoryProvider extends ContentProvider {
    private static final String PROVIDER_NAME   = "in.flinbor.github.repocommits.contentprovider.RepositoryProvider";
    private static final String URL             = "content://" + PROVIDER_NAME + "/repo";

    public static final Uri CONTENT_URI         = Uri.parse(URL);
    public static final String serverRepoId     = "serverRepoId";
    public static final String repoName         = "repoName";
    public static final String description      = "description";
    public static final String loginOfTheOwner  = "loginOfTheOwner";
    public static final String linkToOwner      = "linkToOwner";
    public static final String linkToRepo       = "linkToRepo";

    private static final int uriCode            = 1;
    private static final UriMatcher uriMatcher;
    private static HashMap<String, String> values = new HashMap<>();
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "repo", uriCode);
        uriMatcher.addURI(PROVIDER_NAME, "repo/*", uriCode);
    }

    private SQLiteDatabase db;
    private static final String DATABASE_NAME   = "mydb";
    private static final String TABLE_NAME      = "repositories";
    private static final int DATABASE_VERSION   = 19;
    private static final String CREATE_DB_TABLE = " CREATE TABLE " + TABLE_NAME
            + " ("
            + serverRepoId      + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + description       + " TEXT, "
            + loginOfTheOwner   + " TEXT, "
            + linkToOwner       + " TEXT, "
            + linkToRepo        + " TEXT, "
            + repoName          + " TEXT" +
            ");";

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case uriCode:
                count = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case uriCode:
                return "vnd.android.cursor.dir/repo";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    /**
     * Insert method modified for replace repositories with the same id
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = db.insertWithOnConflict(TABLE_NAME, "", values, SQLiteDatabase.CONFLICT_REPLACE);
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public boolean onCreate() {
        Context context         = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        this.db                 = dbHelper.getWritableDatabase();
        if (this.db != null) {
            return true;
        }
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case uriCode:
                qb.setProjectionMap(values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = serverRepoId;
        }
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case uriCode:
                count = db.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
