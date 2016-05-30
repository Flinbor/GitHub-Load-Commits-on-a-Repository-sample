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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import in.flinbor.github.R;
import in.flinbor.github.repocommits.mvp.presenter.RepoListPresenter;
import in.flinbor.github.repocommits.mvp.presenter.vo.Repository;
import in.flinbor.github.repocommits.mvp.view.adapter.RepoListAdapter;


/**
 * Implementation of view layer, responsible for repositories UI
 */
public class RepoListFragment extends Fragment implements RepoListView {

    private RepoListPresenter   presenter;
    private RecyclerView        recyclerView;
    private RepoListAdapter     adapter;
    private LinearLayoutManager linearLayoutManager;
    private boolean             isLoading;

    @Override
    public void setPresenter(@NonNull RepoListPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repo_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RepoListAdapter(new ArrayList<Repository>(), presenter);
        recyclerView.setAdapter(adapter);

        presenter.onCreateView(savedInstanceState);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.onPause();
    }

    @Override
    public void showRepoList(@NonNull List<Repository> list) {
        adapter.setRepoList(list);
        isLoading = false;
    }

    @Override
    public void showError(@NonNull String error) {
        Snackbar.make(recyclerView, error, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public LoaderManager getSupportLoaderManager() {
        return getLoaderManager();
    }

    @Override
    public Context getViewContext() {
        return getContext();
    }

}
