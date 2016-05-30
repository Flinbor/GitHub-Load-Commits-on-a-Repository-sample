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

import java.util.List;

import in.flinbor.github.repocommits.mvp.presenter.vo.Repository;

/**
 * Responsible for communication with DB, normalizing data
 */
public interface Model {

    /**
     * get cursor that points to Reports table
     * @return Cursor with Reports
     */
    Cursor getCursorReports();

    /**
     * read cursor data as {@link Repository} objects
     * @param cursor with reports to read
     * @return list of repository
     */
    List<Repository> retrieveReportsFromCursor(Cursor cursor);
}
