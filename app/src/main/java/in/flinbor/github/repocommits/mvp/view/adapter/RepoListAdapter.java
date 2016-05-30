/*
 * Copyright 2016 Flinbor Bogdanov Oleksandr
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package in.flinbor.github.repocommits.mvp.view.adapter;

import android.widget.TextView;

import java.util.List;

import in.flinbor.github.R;
import in.flinbor.github.repocommits.commitsloader.CommitsLoader;
import in.flinbor.github.repocommits.mvp.presenter.RepoListPresenter;
import in.flinbor.github.repocommits.mvp.presenter.vo.Repository;

/**
 * RecyclerView adapter for Repositories
 */
public class RepoListAdapter extends BaseAdapter<Repository>{

    private final RepoListPresenter presenter;
    private final CommitsLoader     commitsLoader;

    public RepoListAdapter(List<Repository> list, RepoListPresenter presenter) {
        super(list);
        this.presenter = presenter;
        this.commitsLoader = new CommitsLoader();
    }

    @Override
    public void onBindViewHolder(BaseAdapter.ViewHolder holder, int position) {
        Repository repo = list.get(position);

        setTextOrClearView(holder.textRepoName, repo.getRepoName());
        setTextOrClearView(holder.textDescription, repo.getDescription());
        setTextOrClearView(holder.textOwnerLogin, repo.getLoginOfTheOwner());

        CharSequence stubError = holder.textDescription.getContext().getText(R.string.repository_commits_not_available);
        CharSequence stubLoad = holder.textDescription.getContext().getText(R.string.repository_commits_loading);
        this.commitsLoader.setLastCommit(repo.getLoginOfTheOwner(), repo.getRepoName(), stubLoad, stubError, holder.textLastComment);
    }

    /**
     * set new data to recyclingView
     * @param list to insert into recyclingView
     */
    public void setRepoList(List<Repository> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    private void setTextOrClearView(TextView textView, String text) {
        if (text != null) {
            textView.setText(text);
        } else {
            textView.setText("");
        }
    }

}
