package com.sfvtech.payperview;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sfvtech.payperview.database.DatabaseContract;
import com.sfvtech.payperview.database.DatabaseHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 17)
public class DBTest {
    private Context context;
    private DatabaseHelper helper;
    private SQLiteDatabase db;

    @Before
    public void setup() {
        context = RuntimeEnvironment.application;
        helper = new DatabaseHelper(context);
        db = helper.getReadableDatabase();
    }

    @After
    public void cleanup() {
        db.close();
    }

    @Test
    public void testViewerDBCols() {
        Cursor c = db.query(DatabaseContract.ViewerEntry.TABLE_NAME, null, null, null, null, null, null);
        assertNotNull(c);

        String[] cols = c.getColumnNames();
        assertThat("Column not implemented: " + DatabaseContract.ViewerEntry.COLUMN_NAME,
                cols, hasItemInArray(DatabaseContract.ViewerEntry.COLUMN_NAME));
        assertThat("Column not implemented: " + DatabaseContract.ViewerEntry.COLUMN_EMAIL,
                cols, hasItemInArray(DatabaseContract.ViewerEntry.COLUMN_EMAIL));
        assertThat("Column not implemented: " + DatabaseContract.ViewerEntry.COLUMN_NULLABLE,
                cols, hasItemInArray(DatabaseContract.ViewerEntry.COLUMN_NULLABLE));
        assertThat("Column not implemented: " + DatabaseContract.ViewerEntry.COLUMN_SESSION_ID,
                cols, hasItemInArray(DatabaseContract.ViewerEntry.COLUMN_SESSION_ID));
        assertThat("Column not implemented: " + DatabaseContract.ViewerEntry.COLUMN_SURVEY_ANSWER,
                cols, hasItemInArray(DatabaseContract.ViewerEntry.COLUMN_SURVEY_ANSWER));
        assertThat("Column not implemented: " + DatabaseContract.ViewerEntry.COLUMN_UPLOADED_TIME,
                cols, hasItemInArray(DatabaseContract.ViewerEntry.COLUMN_UPLOADED_TIME));

        c.close();
    }

    @Test
    public void testSessionDBCols() {
        Cursor c = db.query(DatabaseContract.SessionEntry.TABLE_NAME, null, null, null, null, null, null);
        assertNotNull(c);

        String[] cols = c.getColumnNames();
        assertThat("Column not implemented: " + DatabaseContract.SessionEntry.COLUMN_START_TIME,
                cols, hasItemInArray(DatabaseContract.SessionEntry.COLUMN_START_TIME));
        assertThat("Column not implemented: " + DatabaseContract.SessionEntry.COLUMN_END_TIME,
                cols, hasItemInArray(DatabaseContract.SessionEntry.COLUMN_END_TIME));
        assertThat("Column not implemented: " + DatabaseContract.SessionEntry.COLUMN_LAT,
                cols, hasItemInArray(DatabaseContract.SessionEntry.COLUMN_LAT));
        assertThat("Column not implemented: " + DatabaseContract.SessionEntry.COLUMN_LONG,
                cols, hasItemInArray(DatabaseContract.SessionEntry.COLUMN_LONG));
        assertThat("Column not implemented: " + DatabaseContract.SessionEntry.COLUMN_LOCALE,
                cols, hasItemInArray(DatabaseContract.SessionEntry.COLUMN_LOCALE));
        assertThat("Column not implemented: " + DatabaseContract.SessionEntry.COLUMN_NULLABLE,
                cols, hasItemInArray(DatabaseContract.SessionEntry.COLUMN_NULLABLE));

        c.close();
    }

    @Test
    public void testDBDelete() {
        assertTrue(context.deleteDatabase(helper.DATABASE_NAME));
    }
}
