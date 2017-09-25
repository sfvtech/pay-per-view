package com.nullhammer.android.viewersurvey;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.nullhammer.ViewerSurvey;

import java.util.Locale;

import static android.view.View.OnClickListener;

public class ViewerNumberActivity extends Activity implements OnClickListener {
    public static final String EXTRA_N_VIEWERS = ViewerSurvey.PACKAGE + "EXTRA_N_VIEWERS";

    Button mOneButton;
    Button mTwoButton;
    Button mThreeButton;

    Button mLanguageSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Trigger creation of unique installation id
        String installationId = new Installation().getId(this.getApplicationContext());

        // Hide the Action Bar
        setFullScreen();

        LayoutInflater inflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_viewer_number, null);
        setContentView(layout);

        // UI References
        mOneButton = (Button) findViewById(R.id.oneButton);
        mTwoButton = (Button) findViewById(R.id.twoButton);
        mThreeButton = (Button) findViewById(R.id.threeButton);
        mLanguageSelector = (Button) findViewById(R.id.languageSelector);

        // Event Listeners
        mOneButton.setOnClickListener(this);
        mTwoButton.setOnClickListener(this);
        mThreeButton.setOnClickListener(this);
        mLanguageSelector.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLanguage((String) v.getTag());
            }
        });

        // Magic Menu Buttons
        ViewHelper.addMagicMenuButtons(layout);

    }

    private void setFullScreen() {
        ActionBar actionBar = getActionBar();
        actionBar.hide();
    }

    @Override
    public void onClick(View view) {
        view.setBackgroundResource(R.drawable.button_lg_selected);
        switch(view.getId()) {
            case R.id.oneButton:
                getViewerInfo(1);
                break;

            case R.id.twoButton:
                getViewerInfo(2);
                break;

            case R.id.threeButton:
                getViewerInfo(3);
                break;
        }
    }

    private void toggleLanguage(String lang) {
        Context ctx = this.getApplication();

        Configuration cfg = new Configuration();
        if (!TextUtils.isEmpty(lang)) {
            cfg.locale = new Locale(lang);
        } else {
            cfg.locale = Locale.getDefault();
        }

        ctx.getResources().updateConfiguration(cfg, null);
        super.recreate();
    }

    private void getViewerInfo(int nViewers) {
        Intent intent = new Intent(this, ViewerInfoActivity.class);
        intent.putExtra(EXTRA_N_VIEWERS, nViewers);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}
