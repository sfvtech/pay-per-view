/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sfvtech.payperview;

import android.provider.BaseColumns;

/**
 * Defines table and column names for the viewer database.
 */
public class DatabaseContract {
    public static final class SessionEntry implements BaseColumns {

        public static final String TABLE_NAME = "sessions";

        // Start timestamp
        public static final String COLUMN_START_TIME = "start_time";
        // End timestamp
        public static final String COLUMN_END_TIME = "end_time";
        // Nullable Hack
        public static final String COLUMN_NULLABLE = "end_time";
        // Locale
        public static final String COLUMN_LOCALE = "locale";
    }

    public static final class ViewerEntry implements BaseColumns {

        public static final String TABLE_NAME = "viewers";

        // Column with the foreign key into the session table.
        public static final String COLUMN_SESSION_ID = "session_id";

        // Viewer's first name
        public static final String COLUMN_NAME = "name";

        // Viewer's e-mail address
        public static final String COLUMN_EMAIL = "email";

        // Viewer's survey answer
        public static final String COLUMN_SURVEY_ANSWER = "survey_answer";

        // Upload timestamp
        public static final String COLUMN_UPLOADED_TIME = "upload_time";

        // Nullable Hack
        public static final String COLUMN_NULLABLE = "survey_answer";

    }
}
