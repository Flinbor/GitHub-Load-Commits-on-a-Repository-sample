/*
 * Copyright 2016 Flinbor Bogdanov Oleksandr
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package in.flinbor.github.repocommits.mvp.view.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;

import java.util.List;

import in.flinbor.github.repocommits.mvp.presenter.RepoListPresenter;
import in.flinbor.github.repocommits.mvp.presenter.vo.Repository;

/**
 * Passive interface that displays data and routes user commands to the presenter to act upon that data
 */
public interface RepoListView {

    /**
     * set presenter interface to View layer
     * @param presenter {@link RepoListPresenter}
     */
    void setPresenter(@NonNull RepoListPresenter presenter);

    /**
     * show repositories on UI
     * @param list repo list to show
     */
    void showRepoList(@NonNull List<Repository> list);

    /**
     * shwo error on UI
     * @param error text of error
     */
    void showError(@NonNull String error);

    /**
     * get support loader manger, for using with cursor loader
     * @return LoaderManager
     */
    LoaderManager getSupportLoaderManager();

    /**
     * get context, for using with cursor loader
     * @return LoaderManager
     */
    Context getViewContext();

}
