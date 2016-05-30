/*
 * Copyright 2016 Flinbor Bogdanov Oleksandr
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package in.flinbor.github.repocommits.mvp.model;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import in.flinbor.github.repocommits.app.App;
import in.flinbor.github.repocommits.contentprovider.RepositoryProvider;
import in.flinbor.github.repocommits.mvp.presenter.vo.Repository;


/**
 * Implementation of Model layer for work with database
 */
public class ModelImpl implements Model {

/* DIRECT RETRIEVE DATA WITHOUT LOADER
  @Override
    public List<Repository> getRepositories() {
        List<Repository> list = new ArrayList<>();
        Cursor cursor = App.getApp().getContentResolver().query(RepositoryProvider.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.moveToFirst()) {
                do {
                    Repository repository = new Repository();
                    repository.setRepoName(cursor.getString(cursor.getColumnIndex(RepositoryProvider.repoName)));
                    repository.setDescription(cursor.getString(cursor.getColumnIndex(RepositoryProvider.description)));
                    repository.setLoginOfTheOwner(cursor.getString(cursor.getColumnIndex(RepositoryProvider.loginOfTheOwner)));
                    repository.setLinkToOwner(cursor.getString(cursor.getColumnIndex(RepositoryProvider.linkToOwner)));
                    repository.setLinkToRepo(cursor.getString(cursor.getColumnIndex(RepositoryProvider.linkToRepo)));
                    repository.setServerRepoId(cursor.getInt(cursor.getColumnIndex(RepositoryProvider.serverRepoId)));

                    list.add(repository);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return list;
    }*/

    @Override
    public Cursor getCursorReports() {
        return App.getApp().getContentResolver().query(RepositoryProvider.CONTENT_URI, null, null, null, null);
    }

    @Override
    public List<Repository> retrieveReportsFromCursor(Cursor cursor) {
        List<Repository> list = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.moveToFirst()) {
                do {
                    Repository repository = new Repository();
                    repository.setRepoName(cursor.getString(cursor.getColumnIndex(RepositoryProvider.repoName)));
                    repository.setDescription(cursor.getString(cursor.getColumnIndex(RepositoryProvider.description)));
                    repository.setLoginOfTheOwner(cursor.getString(cursor.getColumnIndex(RepositoryProvider.loginOfTheOwner)));
                    repository.setLinkToOwner(cursor.getString(cursor.getColumnIndex(RepositoryProvider.linkToOwner)));
                    repository.setLinkToRepo(cursor.getString(cursor.getColumnIndex(RepositoryProvider.linkToRepo)));
                    repository.setServerRepoId(cursor.getInt(cursor.getColumnIndex(RepositoryProvider.serverRepoId)));

                    list.add(repository);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

}
