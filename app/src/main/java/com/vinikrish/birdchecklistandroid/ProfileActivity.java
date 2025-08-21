package com.vinikrish.birdchecklistandroid;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private TextView userIdText, emailText;
    private ImageView backButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        userIdText = findViewById(R.id.userIdText);
        emailText = findViewById(R.id.emailText);
        backButton = findViewById(R.id.backButton);

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userIdText.setText("User ID: " + currentUser.getUid());
            emailText.setText("Email: " + currentUser.getEmail());
        }

        // Back button click listener
        backButton.setOnClickListener(v -> finish());
    }
}