/*
 * Copyright 2016 Flinbor Bogdanov Oleksandr
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package in.flinbor.github.repocommits.mvp.presenter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import java.util.List;

import in.flinbor.github.BuildConfig;
import in.flinbor.github.repocommits.app.App;
import in.flinbor.github.repocommits.executorservice.ServiceCallbackListener;
import in.flinbor.github.repocommits.executorservice.command.GetRepositoryCommand;
import in.flinbor.github.repocommits.mvp.model.Model;
import in.flinbor.github.repocommits.mvp.presenter.vo.Repository;
import in.flinbor.github.repocommits.mvp.view.fragment.RepoListView;

/**
 * Concrete implementation of RepoListPresenter
 * responsible for repositories business logic
 */
public class RepoListPresenterImpl implements RepoListPresenter, ServiceCallbackListener {

    private RepoListView view;
    private Model        model;
    private long         requestId = -1;
    private String       repoOwner = BuildConfig.REPOSITORY_OWNER;

    @Override
    public void setView(@NonNull RepoListView view) {
        this.view = view;
    }

    @Override
    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    public void onCreateView(@Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onResume() {
        App.getApp().getServiceHelper().addListener(this);
        loadRepositoriesFromDatabaseInBackground();
        downloadRepositories(repoOwner);
    }

    @Override
    public void onPause() {
        App.getApp().getServiceHelper().removeListener(this);
    }

    @Override
    public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle resultData) {
        if (this.requestId == requestId && App.getApp().getServiceHelper().check(requestIntent, GetRepositoryCommand.class)) {
            if (resultCode == GetRepositoryCommand.RESPONSE_SUCCESS) {
                loadRepositoriesFromDatabaseInBackground();
            } else if (resultCode == GetRepositoryCommand.RESPONSE_FAILURE) {
                String error = "onServiceCallback response error";
                if (resultData != null) {
                    error = resultData.getString(GetRepositoryCommand.ERROR);
                }
                error = error == null ? "onServiceCallback response error": error;
                view.showError(error);
            }
            this.requestId = -1;
        }
    }

    private void downloadRepositories(String repoOwner) {
        if (requestId == -1) {
            requestId = App.getApp().getServiceHelper().getRepositories(repoOwner);
        }
    }


    /*******************************************Loader**************************************/

    private void loadRepositoriesFromDatabaseInBackground() {
        if (view.getSupportLoaderManager().getLoader(0) != null) {
            view.getSupportLoaderManager().restartLoader(0, null, loaderCallback);
        } else {
            view.getSupportLoaderManager().initLoader(0, null, loaderCallback);
        }
    }

    private final LoaderManager.LoaderCallbacks<Cursor> loaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new RepositoryLoader(view.getViewContext(), model);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            List<Repository> list = model.retrieveReportsFromCursor(cursor);
            view.showRepoList(list);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private static class RepositoryLoader extends CursorLoader {

        private final Model model;

        public RepositoryLoader(Context context, Model model) {
            super(context);
            this.model = model;
        }

        @Override
        public Cursor loadInBackground() {
            return model.getCursorReports();
        }
    }

    /***************************************************************************************/
}
