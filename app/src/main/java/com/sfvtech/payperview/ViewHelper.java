package com.sfvtech.payperview;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sfvtech.payperview.fragment.AdminFragment;

import java.util.Date;

/**
 * Some useful methods for view manipulation
 */
public class ViewHelper {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 200;
    private static final String LOG_TAG = "ViewHelper";
    private static final long MAGIC_BUTTON_MAX_MS = 2000; // milliseconds

    /**
     * Adds magic buttons and their event handlers. To activate the Magic Menu, buttons 0, 1, and
     * 2 must be clicked in sequence in less than MAGIC_BUTTON_MAX_MS milliseconds.
     *
     * @param layout Layout to attach buttons to.
     */
    public static void initializeMagicMenuButtons(View layout, final String currentFragmentTag, final Bundle adminArgs) {
        Log.v("initalizeMagicButtons", "Called");
        final ViewGroup myLayout = (ViewGroup) layout;
        final Button buttons[] = new Button[3];

        View.OnClickListener listener = new View.OnClickListener() {
            Date mTimerStart;
            int mLastButtonIndex = -1;

            @Override
            public void onClick(View v) {
                if (v.getId() == buttons[0].getId()) {
                    mTimerStart = new Date();
                    mLastButtonIndex = 0;
                } else if (v.getId() == buttons[1].getId()) {
                    if (mLastButtonIndex == 0) {
                        mLastButtonIndex = 1;
                    } else {
                        mLastButtonIndex = -1;
                    }
                } else if (v.getId() == buttons[2].getId()) {
                    if (mLastButtonIndex == 1) {
                        long interval = new Date().getTime() - mTimerStart.getTime();
                        if (interval < MAGIC_BUTTON_MAX_MS) {
                            startAdminFragment(myLayout.getContext(), currentFragmentTag, adminArgs);
                        }
                    }
                    // Reset the button tracker
                    mLastButtonIndex = -1;
                }
            }
        };

        int left = layout.getLeft();
        Log.v("initalizeMagic:left", "" + left);
        int top = layout.getTop();
        Log.v("initalizeMagic:top", "" + top);
        int right = layout.getRight();
        Log.v("initalizeMagic:right", "" + right);
        int bottom = layout.getBottom();
        Log.v("initalizeMagic:bottom", "" + bottom);

        for (int i = 0; i < 3; i++) {
            buttons[i] = new Button(layout.getContext());
            buttons[i].setText(Integer.toString(i));
            buttons[i].setLayoutParams(new ViewGroup.LayoutParams(BUTTON_WIDTH, BUTTON_HEIGHT));
            buttons[i].setId(View.generateViewId());
            buttons[i].setAlpha(0);
            buttons[i].setOnClickListener(listener);
            switch (i) {
                case 0:
                    buttons[0].setX(left);
                    buttons[0].setY(top);
                    Log.v("tag", "0");
                    Log.v("tag", "" + left + " " + top + " " + right + " " + bottom);
                    break;
                case 1:
                    buttons[1].setX(left);
                    buttons[1].setY(bottom - BUTTON_HEIGHT);
                    Log.v("tag", "1");
                    break;
                case 2:
                    buttons[2].setX(right - BUTTON_WIDTH);
                    buttons[2].setY(bottom - BUTTON_HEIGHT);
                    Log.v("tag", "2");
                    break;
            }

            // Add buttons
            myLayout.addView(buttons[i]);
        }
    }

    public static void setListener(final Button[] buttons, final String currentFragmentTag, final Bundle adminArgs, final Context context) {

        for (int i = 0; i < 3; i++) {
            buttons[i].setOnClickListener(new View.OnClickListener() {
                Date mTimerStart;
                int mLastButtonIndex = -1;

                @Override
                public void onClick(View v) {
                    if (v.getId() == buttons[0].getId()) {
                        Log.v("View", "0");
                        mTimerStart = new Date();
                        mLastButtonIndex = 0;
                    } else if (v.getId() == buttons[1].getId()) {
                        Log.v("View", "1");
                        if (mLastButtonIndex == 0) {
                            mLastButtonIndex = 1;
                        } else {
                            mLastButtonIndex = -1;
                        }
                    } else if (v.getId() == buttons[2].getId()) {
                        Log.v("View", "2");
                        if (mLastButtonIndex == 1) {
                            long interval = new Date().getTime() - mTimerStart.getTime();
                            if (interval < MAGIC_BUTTON_MAX_MS) {
                                startAdminFragment(context, currentFragmentTag, adminArgs);
                            }
                        }
                        // Reset the button tracker
                        mLastButtonIndex = -1;
                    }
                }
            });
        }
    }

    public static void startAdminFragment(Context context, String currentFragmentTag, Bundle adminArgs) {
        Log.v("View", "startAdmin");
        final Fragment adminFragment = new AdminFragment();
        adminArgs.putString("fragmentTag", currentFragmentTag);
        adminFragment.setArguments(adminArgs);
        ((AppCompatActivity) context).getSupportFragmentManager().
                beginTransaction().replace(R.id.container, adminFragment, AdminFragment.FRAGMENT_TAG).
                commit();
    }

    /**
     * Adds a layout change listener to attach magic menu buttons
     *
     * @param layout
     */
    public static void addMagicMenuButtons(View layout, final String currentFragmentTag, final Bundle adminArgs) {
        layout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // Ignore event if the layout isn't complete
                if (right == 0 && bottom == 0) {
                    Log.v("ViewHelper", "Layout not complete");
                    return;
                }
                initializeMagicMenuButtons(v, currentFragmentTag, adminArgs);
            }

        });
    }

    public static void removeMagicButtons(View layout) {
        final ViewGroup myLayout = (ViewGroup) layout;
        myLayout.removeAllViews();
    }

    public static void updateMagicButtons(View layout, String fragmentTag, Bundle adminArgs) {
        removeMagicButtons(layout);
        addMagicMenuButtons(layout, fragmentTag, adminArgs);
    }
}
