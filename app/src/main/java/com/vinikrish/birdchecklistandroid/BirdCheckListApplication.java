package com.vinikrish.birdchecklistandroid;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Application class to initialize Firebase once at app startup
 */
public class BirdCheckListApplication extends Application {
    private static final String TAG = "BirdCheckListApp";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this);
            
            // Enable offline capabilities - this must be called before any other Firebase Database usage
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            
            Log.d(TAG, "Firebase initialized successfully with persistence enabled");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
        }
    }
}