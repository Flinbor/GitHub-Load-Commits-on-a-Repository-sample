/*
 * Copyright 2016 Flinbor Bogdanov Oleksandr
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package in.flinbor.github.repocommits.executorservice.dto;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Data Transfer Object for github Commit
 */
public class CommitDetailDTO {
    @SerializedName("author")
    @Expose
    public AuthorDTO author;
    @SerializedName("committer")
    @Expose
    public CommitterDTO committer;
    @SerializedName("message")
    @Expose
    public String message;
    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("comment_count")
    @Expose
    public int comment_count;

    public AuthorDTO getAuthor() {
        return author;
    }

    public void setAuthor(AuthorDTO author) {
        this.author = author;
    }

    public CommitterDTO getCommitter() {
        return committer;
    }

    public void setCommitter(CommitterDTO committer) {
        this.committer = committer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getComment_count() {
        return comment_count;
    }

    public void setComment_count(int comment_count) {
        this.comment_count = comment_count;
    }
}
