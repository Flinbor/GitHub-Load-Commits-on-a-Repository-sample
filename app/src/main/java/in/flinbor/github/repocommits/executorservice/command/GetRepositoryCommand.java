/*
 * Copyright 2016 Flinbor Bogdanov Oleksandr
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package in.flinbor.github.repocommits.executorservice.command;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import in.flinbor.github.BuildConfig;
import in.flinbor.github.repocommits.app.App;
import in.flinbor.github.repocommits.contentprovider.RepositoryProvider;
import in.flinbor.github.repocommits.executorservice.dto.RepositoryDTO;

/**
 * Command works in background
 * get repositories from server and store it to DB
 */
public class GetRepositoryCommand extends BaseCommand {
	public static final String ERROR 				= "error";
	private final String TAG				 		= GetRepositoryCommand.class.getSimpleName();
	private final int READ_TIMEOUT_MILLIS 			= 10000;
	private final int CONNECTION_TIMEOUT_MILLIS 	= 15000;
	private final String username;

    @Override
    public void doExecute(Intent intent, Context context, ResultReceiver callback) {
		List<RepositoryDTO> repositories = loadNewReports(username);
		if (repositories != null && repositories.size() > 0) {
			insertIntoDB(repositories);
			/*
			for debug: send repositories in parcelable
			Bundle b = new Bundle();
			b.putParcelableArrayList(DATA, repositories);*/
			notifySuccess(null);
		} else {
			Bundle b = new Bundle();
			b.putString(ERROR, "repo size == null or size is 0");
			notifyFailure(b);
		}
    }

	private List<RepositoryDTO> loadNewReports(String username) {
		Uri.Builder builtUri = new Uri.Builder()
				.scheme("https")
				.authority("api.github.com")
				.path("users")
				.appendPath(username)
				.appendPath("repos");

		if (BuildConfig.ACCESS_TOKEN != null) {
			builtUri.appendQueryParameter("access_token", BuildConfig.ACCESS_TOKEN);
		}

		String stringUrl = builtUri.build().toString();
		Log.d(TAG, "GET repos ulr: "+ stringUrl);

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
			BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line+"\n");
			}
			br.close();

			String jsonString = sb.toString();
			Log.d(TAG, jsonString);

			//create object from json string
			Gson gson = new Gson();
			Type token = new TypeToken<List<RepositoryDTO>>() {}.getType();
			List<RepositoryDTO> repoList = gson.fromJson(jsonString, token);
			return repoList;
		} catch (Exception e) {
			e.printStackTrace();
			Bundle b = new Bundle();
			b.putString(ERROR, e.toString());
			notifyFailure(b);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return null;
	}

	private void insertIntoDB(List<RepositoryDTO> repositories) {
		for (int i = 0; i < repositories.size(); i++) {
			RepositoryDTO repo = repositories.get(i);
			ContentValues values = new ContentValues();
			values.put(RepositoryProvider.repoName, 		repo.getName());
			values.put(RepositoryProvider.description, 		repo.getDescription());
			values.put(RepositoryProvider.loginOfTheOwner, 	repo.getOwner().getLogin());
			values.put(RepositoryProvider.linkToOwner, 		repo.getOwner().getHtmlUrl());
			values.put(RepositoryProvider.linkToRepo, 		repo.getHtmlUrl());
			values.put(RepositoryProvider.serverRepoId, 	repo.getId());
			App.getApp().getContentResolver().insert(RepositoryProvider.CONTENT_URI, values);
		}
	}

	@Override
    public int describeContents() {
	return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
	dest.writeString(username);
    }

    public static final Parcelable.Creator<GetRepositoryCommand> CREATOR = new Parcelable.Creator<GetRepositoryCommand>() {
	public GetRepositoryCommand createFromParcel(Parcel in) {
	    return new GetRepositoryCommand(in);
	}

	public GetRepositoryCommand[] newArray(int size) {
	    return new GetRepositoryCommand[size];
	}
    };

    private GetRepositoryCommand(Parcel in) {
		username = in.readString();
    }

    public GetRepositoryCommand(String username) {
		this.username = username;
    }

}
