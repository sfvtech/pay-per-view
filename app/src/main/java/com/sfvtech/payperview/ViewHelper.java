package com.sfvtech.payperview;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
     * @param right  Width of layout
     * @param bottom Height of layout
     */
    private static void initializeMagicMenuButtons(View layout, int right, int bottom) {
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
                            startAdminFragment(myLayout);
                        }
                    }
                    // Reset the button tracker
                    mLastButtonIndex = -1;
                }
            }
        };

        for (int i = 0; i < 3; i++) {
            buttons[i] = new Button(layout.getContext());
            buttons[i].setText(Integer.toString(i));
            buttons[i].setLayoutParams(new ViewGroup.LayoutParams(BUTTON_WIDTH, BUTTON_HEIGHT));
            buttons[i].setId(View.generateViewId());
            buttons[i].setAlpha(0);
            buttons[i].setOnClickListener(listener);
            switch (i) {
                case 0:
                    buttons[0].setX(0);
                    buttons[0].setY(0);
                    break;
                case 1:
                    buttons[1].setX(0);
                    buttons[1].setY(bottom - BUTTON_HEIGHT);
                    break;
                case 2:
                    buttons[2].setX(right - BUTTON_WIDTH);
                    buttons[2].setY(bottom - BUTTON_HEIGHT);
                    break;
            }

            // Add buttons
            myLayout.addView(buttons[i]);
        }
    }

    protected static void startAdminFragment(View v) {
        Fragment adminFragment = new AdminFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("mViewers", MainActivity.mViewers);
        args.putString("ID", MainActivity.ID);
        adminFragment.setArguments(args);
        ((AppCompatActivity) v.getContext()).getSupportFragmentManager().
                beginTransaction().addToBackStack(null).replace(R.id.container, adminFragment, AdminFragment.FRAGMENT_TAG).
                commit();
    }

    /**
     * Adds a layout change listener to attach magic menu buttons
     *
     * @param layout
     */
    public static void addMagicMenuButtons(View layout) {
        layout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // Ignore event if the layout isn't complete
                if (right == 0 && bottom == 0) {
                    return;
                }
                initializeMagicMenuButtons(v, right, bottom);
            }

        });
    }
}
