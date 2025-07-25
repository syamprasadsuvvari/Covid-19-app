package com.example.covid_19;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser(); // Get user before the if check

            if (currentUser != null) {
                Log.d("AUTH_DEBUG", "SplashActivity: User is logged in. UID: " + currentUser.getUid());
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                Log.d("AUTH_DEBUG", "SplashActivity: No user logged in. Redirecting to AuthActivity.");
                startActivity(new Intent(SplashActivity.this, AuthActivity.class));
            }
            finish();
        }, 3000);

    }
}