/*
 * Copyright 2016 Flinbor Bogdanov Oleksandr
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package in.flinbor.github.repocommits.commitsloader;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.TintContextWrapper;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.flinbor.github.BuildConfig;
import in.flinbor.github.repocommits.executorservice.dto.CommitDTO;
import in.flinbor.github.repocommits.executorservice.dto.CommitDetailDTO;

/**
 * Using setLastCommit(UserName, RepoName, TextView) method of CommitsLoader class,
 * you can set last Commit Message to TextView from cache or load it from internet
 */
public class CommitsLoader {
    public static final String          TAG                         = "CommitsLoader";
    private final Map<RepoUser, String> lastCommitCache             = Collections.synchronizedMap(new HashMap<RepoUser, String>());
    private final List<RepoUser>        pendingRequest              = Collections.synchronizedList(new ArrayList<RepoUser>());
    private final int                   READ_TIMEOUT_MILLIS         = 10000;
    private final int                   CONNECTION_TIMEOUT_MILLIS   = 15000;
    private final ExecutorService       executorService;

    public CommitsLoader() {
        executorService= Executors.newFixedThreadPool(5);
    }

    /**
     * set last Commit Message to TextView from cache or load it from internet
     * @param ownerLogin owner of repo
     * @param repoName repo to retrieve commits from
     * @param stubText stub text that will be shown before content loaded
     * @param emptyCommitsText text stub if repo has no commits
     * @param textView TextView to witch set result
     */
    public void setLastCommit(@NonNull String ownerLogin, @NonNull String repoName, @Nullable CharSequence stubText, @Nullable CharSequence emptyCommitsText, @NonNull TextView textView){
        RepoUser repoUser = new RepoUser(ownerLogin, repoName, emptyCommitsText);
        String lastCommitName = lastCommitCache.get(repoUser);
        if (lastCommitName != null) {
            textView.setText(lastCommitName);
        } else {
            if (!pendingRequest.contains(repoUser)) {
                pendingRequest.add(repoUser);
                queueText(repoUser, textView);
            }
            if (stubText != null) {
                textView.setText(stubText);
            }
        }
    }

    private void queueText(@NonNull RepoUser repoUser, @NonNull TextView textView) {
        TextToLoad p = new TextToLoad(repoUser, textView);
        executorService.submit(new TextLoader(p));
    }

    public static class RepoUser {
        final String userName;
        final String repoName;
        final String emptyCommitsText;

        public RepoUser(@NonNull String userName, @NonNull String repoName, @Nullable CharSequence emptyCommitsText) {
            this.userName = userName;
            this.repoName = repoName;
            if (emptyCommitsText != null) {
                this.emptyCommitsText = emptyCommitsText.toString();
            } else {
                this.emptyCommitsText = null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RepoUser repoUser = (RepoUser) o;

            if (!userName.equals(repoUser.userName)) return false;
            if (!repoName.equals(repoUser.repoName)) return false;
            return emptyCommitsText != null ? emptyCommitsText.equals(repoUser.emptyCommitsText) : repoUser.emptyCommitsText == null;

        }

        @Override
        public int hashCode() {
            int result = userName.hashCode();
            result = 31 * result + repoName.hashCode();
            result = 31 * result + (emptyCommitsText != null ? emptyCommitsText.hashCode() : 0);
            return result;
        }

    }

    //Task for the queue
    private class TextToLoad {
        public final RepoUser                repoUser;
        public final WeakReference<TextView> textViewRef;
        public TextToLoad(@NonNull RepoUser u, @NonNull TextView textView){
            this.repoUser = u;
            this.textViewRef = new WeakReference<>(textView);
        }
    }

    class TextLoader implements Runnable {
        final TextToLoad textToLoad;
        TextLoader(TextToLoad textToLoad){
            this.textToLoad = textToLoad;
        }

        @Override
        public void run() {
            List<CommitDTO> commits = getCommits(textToLoad.repoUser);

            CharSequence message;

            if (commits == null || commits.size() == 0) {
                //no commits: set stub text
                message = textToLoad.repoUser.emptyCommitsText;
            } else {
                //sort commits to take last one
                Collections.sort(commits, commitsAsc);
                //store commit message to cache and set it to TextView
                CommitDetailDTO commitDTO = commits.get(0).getCommit();
                lastCommitCache.put(textToLoad.repoUser, commitDTO.getMessage());
                message = commitDTO.getMessage();
            }

            TextView textView = textToLoad.textViewRef.get();
            if (textView == null) {
                //textView not exitst anymore (garbaged or so on)
                return;
            }

            message = message == null ? "":message;
            TextSetter textSetter = new TextSetter(message, textView);
            Context context = textView.getContext();
            if (context instanceof TintContextWrapper) {
                TintContextWrapper tintContextWrapper = (TintContextWrapper) context;
                Context baseContext = tintContextWrapper.getBaseContext();
                if (baseContext instanceof Activity) {
                    Log.d(TAG, "context activity");
                    ((Activity) baseContext).runOnUiThread(textSetter);
                }
            }
            pendingRequest.remove(textToLoad.repoUser);
        }
    }

    /**
     * Used to set text in the UI thread
     */
    class TextSetter implements Runnable {
        private final CharSequence text;
        private final TextView     textView;

        public TextSetter(@NonNull CharSequence text, @NonNull TextView textView){
            this.text = text;
            this.textView = textView;
        }
        public void run() {
            textView.setText(text);
        }
    }

    private final Comparator<CommitDTO> commitsAsc = new Comparator<CommitDTO>() {
        @Override
        public int compare(CommitDTO o, CommitDTO t) {
            if (o != null && o.getCommit() != null && o.getCommit().getCommitter() != null
                    && o.getCommit().getCommitter().getDate() != null
            && t != null && t.getCommit() != null && t.getCommit().getCommitter() != null
                    && t.getCommit().getCommitter().getDate() != null) {
                return t.getCommit().getCommitter().getDate().compareTo(o.getCommit().getCommitter().getDate());
            } else {
                Log.w("Commit comparator", "compered object or date == null");
                return -1;
            }


        }
    };

    /**
     * load commits list from internet
     * @param repoUser to take user name and repo name
     * @return downloaded list or null
     */
    private List<CommitDTO> getCommits(@NonNull RepoUser repoUser) {

        Uri.Builder builtUri = new Uri.Builder()
                .scheme("https")
                .authority("api.github.com")
                .path("repos")
                .appendPath(repoUser.userName)
                .appendPath(repoUser.repoName)
                .appendPath("commits");

        if (BuildConfig.ACCESS_TOKEN != null) {
            builtUri.appendQueryParameter("access_token", BuildConfig.ACCESS_TOKEN);
        }
        String stringUrl = builtUri.build().toString();
        Log.d(TAG, "GET commits url: "+ stringUrl);

        URL url;
        HttpURLConnection connection = null;
        try {
            // Create connection
            url = new URL(stringUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setReadTimeout(READ_TIMEOUT_MILLIS);
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MILLIS);
            connection.connect();

            //read headers, used for debug (headers also contain info about pagination)
            for (Map.Entry<String, List<String>> k : connection.getHeaderFields().entrySet()) {
                for (String v : k.getValue()){
                    Log.d(TAG, k.getKey() + ":" + v);
                }
            }

            //read response as string to StringBuilder
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder  sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();

            String jsonString = sb.toString();
            Log.d(TAG, jsonString);

            //create object from json string
            Gson gson = new Gson();
            Type token = new TypeToken<List<CommitDTO>>() {}.getType();
            List<CommitDTO> repoList = gson.fromJson(jsonString, token);
            return repoList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

}

