package com.sfvtech.payperview;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.text.format.DateUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import okhttp3.mockwebserver.MockResponse;
import okio.Buffer;
import okio.Okio;

import static android.view.KeyEvent.KEYCODE_ENTER;

// This test visits each page and checks whether some essential elements are being displayed
public final class TestHelper {

    static final long waitingTime = DateUtils.SECOND_IN_MILLIS * 4;
    static final long webPageLoadwaitingTime = DateUtils.SECOND_IN_MILLIS * 15;
    public static UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    /********* Web View Locators ***********/
    public static UiObject browserURLbar = mDevice.findObject(new UiSelector()
            .resourceId("com.:id/oneButton")
            .clickable(true));

    public static UiObject inlineAutocompleteEditText = mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/url_edit")
            .focused(true)
            .enabled(true));
    public static UiObject hint = mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/search_hint")
            .clickable(true));
    public static UiObject webView = mDevice.findObject(new UiSelector()
            .className("android.webkit.WebView")
            .enabled(true));
    public static UiObject progressBar = mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/progress")
            .enabled(true));
    public static UiObject notificationBarDeleteItem = TestHelper.mDevice.findObject(new UiSelector()
            .text("Erase browsing history")
            .resourceId("android:id/text")
            .enabled(true));
    public static UiObject notificationExpandSwitch = TestHelper.mDevice.findObject(new UiSelector()
            .resourceId("android:id/expand_button")
            .packageName("org.mozilla.focus.debug")
            .enabled(true));
    public static UiObject AddtoHSmenuItem = TestHelper.mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/add_to_homescreen")
            .enabled(true));
    public static UiObject AddtoHSCancelBtn = TestHelper.mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/addtohomescreen_dialog_cancel")
            .enabled(true));
    /********* Main View Menu Item Locators ***********/
    public static UiObject whatsNewItem = mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/whats_new")
            .enabled(true));
    public static UiObject HelpItem = mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/help")
            .enabled(true));
    public static UiObject settingsMenuItem = mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/settings")
            .enabled(true));
    public static UiObject shareAppList = TestHelper.mDevice.findObject(new UiSelector()
            .resourceId("android:id/resolver_list")
            .enabled(true));
    /********* Settings Menu Item Locators ***********/
    public static UiScrollable settingsList = new UiScrollable(new UiSelector()
            .resourceId("android:id/list").scrollable(true));
    public static UiObject settingsHeading = mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/toolbar")
            .enabled(true));
    static UiObject cleartextField = mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/clear")
            .enabled(true));
    static UiObject tryAgainBtn = mDevice.findObject(new UiSelector()
            .description("Try Again")
            .clickable(true));
    static UiObject notFoundMsg = mDevice.findObject(new UiSelector()
            .description("The address wasn’t understood")
            .enabled(true));
    static UiObject notFounddetailedMsg = mDevice.findObject(new UiSelector()
            .description("You might need to install other software to open this address.")
            .enabled(true));
    static UiObject browserViewSettingsMenuItem = mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/settings")
            .clickable(true));
    static UiObject erasedMsg = TestHelper.mDevice.findObject(new UiSelector()
            .text("Your browsing history has been erased.")
            .resourceId("org.mozilla.focus.debug:id/snackbar_text")
            .enabled(true));
    static UiObject lockIcon = mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/lock")
            .description("Secure connection"));
    static UiObject notificationOpenItem = TestHelper.mDevice.findObject(new UiSelector()
            .resourceId("android:id/action0")
            .description("Open")
            .enabled(true));
    static UiObject notificationEraseOpenItem = TestHelper.mDevice.findObject(new UiSelector()
            .resourceId("android:id/action0")
            .description("Erase and Open")
            .enabled(true));
    static UiObject FocusInRecentApps = TestHelper.mDevice.findObject(new UiSelector()
            .text("Focus (Dev)")
            .resourceId("com.android.systemui:id/title")
            .enabled(true));
    static UiObject blockOffIcon = TestHelper.mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/block")
            .enabled(true));
    static UiObject AddtoHSOKBtn = TestHelper.mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/addtohomescreen_dialog_add")
            .enabled(true));
    static UiObject AddautoBtn = TestHelper.mDevice.findObject(new UiSelector()
            .text("ADD AUTOMATICALLY")
            .enabled(true));
    static UiObject shortcutTitle = TestHelper.mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/edit_title")
            .enabled(true));
    static UiObject savedNotification = TestHelper.mDevice.findObject(new UiSelector()
            .text("Download complete.")
            .resourceId("android:id/text")
            .enabled(true));
    static UiObject blockCounterItem = mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/trackers_count"));
    static UiObject blockToggleSwitch = mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/blocking_switch"));
    static UiObject menulist = mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/list")
            .enabled(true));
    /********** Share Menu Dialog ********************/
    static UiObject shareMenuHeader = TestHelper.mDevice.findObject(new UiSelector()
            .resourceId("android:id/title")
            .text("Share via")
            .enabled(true));
    static UiObject navigateUp = mDevice.findObject(new UiSelector()
            .description("Navigate up"));
    static UiObject toggleAnalyticBlock = mDevice.findObject(new UiSelector()
            .className("android.widget.Switch")
            .instance(1));
    static UiObject refreshBtn = mDevice.findObject(new UiSelector()
            .resourceId("org.mozilla.focus.debug:id/refresh")
            .enabled(true));

    private TestHelper() throws UiObjectNotFoundException {
    }

    static String getMenuItemText(UiObject item) throws UiObjectNotFoundException {
        String text = item.getChild(new UiSelector().index(0))
                .getChild(new UiSelector().index(0)).getText();
        return text;
    }

    static void waitForIdle() {
        mDevice.waitForIdle(waitingTime);
    }

    public static void pressEnterKey() {
        mDevice.pressKeyCode(KEYCODE_ENTER);
    }

    public static void pressBackKey() {
        mDevice.pressBack();
    }

    static void pressHomeKey() {
        mDevice.pressHome();
    }

    static void pressRecentAppsKey() throws RemoteException {
        mDevice.pressRecentApps();
    }

    public static MockResponse createMockResponseFromAsset(@NonNull String fileName) throws IOException {
        return new MockResponse()
                .setBody(TestHelper.readTestAsset(fileName));
    }

    public static Buffer readTestAsset(String filename) throws IOException {
        try (final InputStream stream = InstrumentationRegistry.getContext().getAssets().open(filename)) {
            return readStreamFile(stream);
        }
    }

    static Buffer readStreamFile(InputStream file) throws IOException {

        Buffer buffer = new Buffer();
        buffer.writeAll(Okio.source(file));
        return buffer;
    }

    static String readFileToString(File file) throws IOException {
        System.out.println("Reading file: " + file.getAbsolutePath());

        try (final FileInputStream stream = new FileInputStream(file)) {
            return readStreamIntoString(stream);
        }
    }

    static String readStreamIntoString(InputStream stream) throws IOException {
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            final StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();

            return builder.toString();
        }
    }
}

