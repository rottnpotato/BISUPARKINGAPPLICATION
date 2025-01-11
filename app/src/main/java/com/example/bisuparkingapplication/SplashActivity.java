package com.example.bisuparkingapplication;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logoImage = findViewById(R.id.splashLogo);

        // Create animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoImage, "scaleX", 0.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoImage, "scaleY", 0.5f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(logoImage, "alpha", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.setDuration(1500);
        animatorSet.start();

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("BisuParking", MODE_PRIVATE);
            String savedPlate = prefs.getString("plate_number", null);

            Intent intent;
            if (savedPlate != null) {
                intent = new Intent(SplashActivity.this, HomeActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, PlateNumberActivity.class);
            }

            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }
}