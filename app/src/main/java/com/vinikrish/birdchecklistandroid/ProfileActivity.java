package com.vinikrish.birdchecklistandroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.vinikrish.birdchecklistandroid.utils.CustomDialogUtils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.vinikrish.birdchecklistandroid.models.Bird;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText firstNameEdit, lastNameEdit;
    private TextView emailText;
    private Button saveButton;
    private ImageView backButton;
    private LinearLayout birdCountsContainer;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "BirdChecklistPrefs";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        firstNameEdit = findViewById(R.id.firstNameEdit);
        lastNameEdit = findViewById(R.id.lastNameEdit);
        emailText = findViewById(R.id.emailText);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);
        birdCountsContainer = findViewById(R.id.birdCountsContainer);

        // Load saved user data
        loadUserData();
        
        // Load bird counts by country
        loadBirdCounts();

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            emailText.setText("Email: " + currentUser.getEmail());
        }

        // Save button click listener
        saveButton.setOnClickListener(v -> saveUserData());

        // Back button click listener
        backButton.setOnClickListener(v -> finish());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh bird counts when returning to profile
        loadBirdCounts();
    }

    private void loadUserData() {
        String firstName = sharedPreferences.getString(KEY_FIRST_NAME, "");
        String lastName = sharedPreferences.getString(KEY_LAST_NAME, "");
        
        firstNameEdit.setText(firstName);
        lastNameEdit.setText(lastName);
    }

    private void saveUserData() {
        String firstName = firstNameEdit.getText().toString().trim();
        String lastName = lastNameEdit.getText().toString().trim();
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_FIRST_NAME, firstName);
        editor.putString(KEY_LAST_NAME, lastName);
        editor.apply();
        
        CustomDialogUtils.showSuccessDialog(this, "Success", "Profile saved successfully!");
    }
    
    private void loadBirdCounts() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "No current user, cannot load bird counts");
            return;
        }
        
        String userId = currentUser.getUid();
        DatabaseReference birdsRef = FirebaseManager.getInstance().getBirdsReference();
        
        birdsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Integer> countryBirdCounts = new HashMap<>();
                
                // Group birds by country and common name to avoid counting male/female separately
                Map<String, Map<String, Boolean>> countryBirdMap = new HashMap<>();
                
                for (DataSnapshot birdSnapshot : dataSnapshot.getChildren()) {
                    Bird bird = birdSnapshot.getValue(Bird.class);
                    if (bird != null && userId.equals(bird.getUserId())) {
                        String country = bird.getCountry();
                        String comName = bird.getComName();
                        
                        if (country != null && !country.isEmpty() && comName != null && !comName.isEmpty()) {
                            // Initialize country map if not exists
                            if (!countryBirdMap.containsKey(country)) {
                                countryBirdMap.put(country, new HashMap<>());
                            }
                            
                            // Mark this bird species as present in this country
                            countryBirdMap.get(country).put(comName, true);
                        }
                    }
                }
                
                // Count unique bird species per country
                for (Map.Entry<String, Map<String, Boolean>> countryEntry : countryBirdMap.entrySet()) {
                    String country = countryEntry.getKey();
                    int uniqueBirdCount = countryEntry.getValue().size();
                    countryBirdCounts.put(country, uniqueBirdCount);
                }
                
                displayBirdCounts(countryBirdCounts);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading bird counts from Firebase", databaseError.toException());
            }
        });
    }
    
    private void displayBirdCounts(Map<String, Integer> countryBirdCounts) {
        birdCountsContainer.removeAllViews();
        
        if (countryBirdCounts.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No birds recorded yet");
            emptyText.setTextSize(16);
            emptyText.setPadding(0, 16, 0, 16);
            birdCountsContainer.addView(emptyText);
            return;
        }
        
        // Add title
        TextView titleText = new TextView(this);
        titleText.setText("Bird Counts by Country:");
        titleText.setTextSize(18);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        titleText.setPadding(0, 16, 0, 8);
        birdCountsContainer.addView(titleText);
        
        // Add country counts
        for (Map.Entry<String, Integer> entry : countryBirdCounts.entrySet()) {
            TextView countText = new TextView(this);
            countText.setText(entry.getKey() + ": " + entry.getValue() + " birds");
            countText.setTextSize(16);
            countText.setPadding(16, 4, 0, 4);
            birdCountsContainer.addView(countText);
        }
    }
}