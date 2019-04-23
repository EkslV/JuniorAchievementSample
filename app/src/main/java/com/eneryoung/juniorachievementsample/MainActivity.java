package com.eneryoung.juniorachievementsample;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView titleTxt, msgText;
    private ImageView sunImage, moonImage;
    private ConstraintLayout mainContainer;
    boolean isDay = true;
    private Snackbar mSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestAccessSettings();

        // initialize variables.
        mainContainer = findViewById(R.id.mainContainer);
        titleTxt = findViewById(R.id.titleTxt);
        msgText = findViewById(R.id.msgText);
        sunImage = findViewById(R.id.sunImage);
        moonImage = findViewById(R.id.moonImage);

        // set onClick observers.
        sunImage.setOnClickListener(this);
        moonImage.setOnClickListener(this);

        FirebaseApp.initializeApp(getApplicationContext());
    }

    private void requestAccessSettings() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && notificationManager != null
                && !notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            startActivity(intent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sunImage:
                changeImagesWithAnimation(sunImage.getId());
                break;
            case R.id.moonImage:
                changeImagesWithAnimation(moonImage.getId());
                break;
            default:
                break;
        }
    }

    private void changeImagesWithAnimation(int id) {
        ConstraintSet constraintSet = new ConstraintSet();

        // copy constraints settings from current ConstraintLayout to set
        constraintSet.clone(mainContainer);

        if (id == R.id.sunImage) {
            isDay = false;
        } else {
            isDay = true;
        }

        // change constraints settings
        changeConstraintsTopToBottom(constraintSet, id);

        // enable animation
        AutoTransition transition = getTransitionWithListener();
        TransitionManager.beginDelayedTransition(mainContainer, transition);

        // apply constraints settings from set to current ConstraintLayout
        constraintSet.applyTo(mainContainer);
    }

    private AutoTransition getTransitionWithListener() {
        AutoTransition transition = new AutoTransition();
        transition.setDuration(1000);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(mainContainer);
                if (isDay) {
                    setDayMode(constraintSet);
                } else {
                    setNightMode(constraintSet);
                }
                TransitionManager.beginDelayedTransition(mainContainer);
                constraintSet.applyTo(mainContainer);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
        return transition;
    }

    private void setNightMode(ConstraintSet constraintSet) {
        mainContainer.setBackgroundColor(Color.parseColor("#FF383838"));
        titleTxt.setText("Device in night mode");
        titleTxt.setTextColor(Color.LTGRAY);
        msgText.setText("Device sound disabled");
        msgText.setTextColor(Color.LTGRAY);
        changeConstraintsBottomToTop(constraintSet, moonImage.getId());

        try {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
        } catch (Exception e) {
            mSnackbar = Snackbar.make(mainContainer, R.string.snackmessage, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.snackbtn, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestAccessSettings();
                        }
                    });
            mSnackbar.show();
        }

        setDBChange(true);
    }

    private void setDayMode(ConstraintSet constraintSet) {
        mainContainer.setBackgroundColor(Color.WHITE);
        titleTxt.setText("Device in day mode");
        titleTxt.setTextColor(Color.DKGRAY);
        msgText.setText("Device sound enabled");
        msgText.setTextColor(Color.DKGRAY);
        changeConstraintsBottomToTop(constraintSet, sunImage.getId());

        try {
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        } catch (Exception e) {
            mSnackbar = Snackbar.make(mainContainer, R.string.snackmessage, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.snackbtn, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestAccessSettings();
                        }
                    });
            mSnackbar.show();
        }

        setDBChange(false);
    }

    private void setDBChange(boolean isNight) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("isNight");
        database.setValue(isNight);
    }

    private void changeConstraintsTopToBottom(ConstraintSet constraintSet, int id) {
        constraintSet.clear(id, ConstraintSet.TOP);
        constraintSet.connect(id, ConstraintSet.TOP, mainContainer.getId(), ConstraintSet.BOTTOM, 60);
    }

    private void changeConstraintsBottomToTop(ConstraintSet constraintSet, int id) {
        constraintSet.clear(id, ConstraintSet.TOP);
        constraintSet.connect(id, ConstraintSet.TOP, titleTxt.getId(), ConstraintSet.BOTTOM, 60);
    }
}
