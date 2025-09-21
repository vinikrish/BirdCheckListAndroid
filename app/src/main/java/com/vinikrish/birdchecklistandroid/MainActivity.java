package com.vinikrish.birdchecklistandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.vinikrish.birdchecklistandroid.utils.CustomDialogUtils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;
    
    // UI elements
    private EditText emailInput;
    private TextInputEditText passwordInput;
    private EditText registerEmailInput;
    private TextInputEditText registerPasswordInput, confirmPasswordInput;
    private TextView errorText, newUserLink, forgotPasswordLink;
    private Button loginButton, registerButton;
    private SignInButton googleSignInButton;
    private CheckBox rememberLoginCheckbox;
    private LinearLayout registrationSection;
    
    // SharedPreferences for remember login
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "BirdChecklistPrefs";
    private static final String KEY_REMEMBER_LOGIN = "remember_login";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_SAVED_PASSWORD = "saved_password";

    
    // Firebase Auth
    private FirebaseAuth mAuth;
    
    // Google Sign-In
    private GoogleSignInClient mGoogleSignInClient;
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToWelcome(currentUser);
            return;
        }
        
        initializeViews();
        setupGoogleSignIn();

        setupClickListeners();
    }
    
    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerEmailInput = findViewById(R.id.registerEmailInput);
        registerPasswordInput = findViewById(R.id.registerPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        rememberLoginCheckbox = findViewById(R.id.rememberLoginCheckbox);
        newUserLink = findViewById(R.id.newUserLink);
        registrationSection = findViewById(R.id.registrationSection);
        errorText = findViewById(R.id.errorText);
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Load saved login credentials if remember login is enabled
        loadSavedCredentials();
        
        // Update version display
        updateVersionDisplay();
    }
    
    private void updateVersionDisplay() {
        TextView versionDisplay = findViewById(R.id.versionDisplayTop);
        if (versionDisplay != null) {
            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                String versionText = "Version " + packageInfo.versionName + " (Build " + packageInfo.versionCode + ")";
                versionDisplay.setText(versionText);
            } catch (PackageManager.NameNotFoundException e) {
                versionDisplay.setText("Version information unavailable");
            }
        }
    }
    
    private void setupGoogleSignIn() {
        Log.d(TAG, "Setting up Google Sign-In...");
        
        // Log configuration details for debugging
        String webClientId = getString(R.string.default_web_client_id);
        Log.d(TAG, "Web Client ID: " + webClientId);
        Log.d(TAG, "Package Name: " + getPackageName());
        
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
        
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Log.d(TAG, "Google Sign-In client created successfully");
        
        // Check if there's already a signed-in account
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Log.d(TAG, "Found existing Google account: " + account.getEmail());
        } else {
            Log.d(TAG, "No existing Google account found");
        }
    }
    

    
    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> loginWithEmail());
        registerButton.setOnClickListener(v -> registerWithEmail());
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        newUserLink.setOnClickListener(v -> showRegistrationDialog());
        forgotPasswordLink.setOnClickListener(v -> showForgotPasswordDialog());
    }
    
    private void loadSavedCredentials() {
        boolean rememberLogin = sharedPreferences.getBoolean(KEY_REMEMBER_LOGIN, false);
        if (rememberLogin) {
            String savedEmail = sharedPreferences.getString(KEY_SAVED_EMAIL, "");
            String savedPassword = sharedPreferences.getString(KEY_SAVED_PASSWORD, "");
            
            emailInput.setText(savedEmail);
            passwordInput.setText(savedPassword);
            rememberLoginCheckbox.setChecked(true);
        }
    }
    
    private void saveCredentials(String email, String password) {
        if (rememberLoginCheckbox.isChecked()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_REMEMBER_LOGIN, true);
            editor.putString(KEY_SAVED_EMAIL, email);
            editor.putString(KEY_SAVED_PASSWORD, password);
            editor.apply();
        } else {
            // Clear saved credentials if remember login is unchecked
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_REMEMBER_LOGIN, false);
            editor.remove(KEY_SAVED_EMAIL);
            editor.remove(KEY_SAVED_PASSWORD);
            editor.apply();
        }
    }
    
    private void showRegistrationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_registration, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        // Get dialog views
        EditText emailInput = dialogView.findViewById(R.id.dialogRegisterEmailInput);
        TextInputEditText passwordInput = dialogView.findViewById(R.id.dialogRegisterPasswordInput);
        TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.dialogConfirmPasswordInput);
        Button cancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        Button createAccountButton = dialogView.findViewById(R.id.dialogCreateAccountButton);
        
        // Set click listeners
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        createAccountButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();
            
            if (validateRegistrationInput(email, password, confirmPassword)) {
                registerWithEmailVerification(email, password, dialog);
            }
        });
        
        dialog.show();
    }
    
    private void loginWithEmail() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError("Please enter email and password");
            return;
        }
        
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            
                            // Check if email is verified
                            if (user != null && user.isEmailVerified()) {
                                // Save credentials if remember login is checked
                                saveCredentials(email, password);
                                navigateToWelcome(user);
                            } else {
                                // Sign out the user and show verification message
                                mAuth.signOut();
                                CustomDialogUtils.showErrorDialog(MainActivity.this, 
                                    "Email Verification Required", "Please verify your email address before logging in. Check your email for the verification link.");
                            }
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            showError("Authentication failed: " + task.getException().getMessage());
                        }
                    }
                });
    }
    
    private boolean validateRegistrationInput(String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            CustomDialogUtils.showErrorDialog(this, "Registration Error", "Please fill all registration fields");
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            CustomDialogUtils.showErrorDialog(this, "Registration Error", "Passwords do not match");
            return false;
        }
        
        if (password.length() < 6) {
            CustomDialogUtils.showErrorDialog(this, "Registration Error", "Password must be at least 6 characters");
            return false;
        }
        
        return true;
    }
    
    private void registerWithEmailVerification(String email, String password, AlertDialog dialog) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Send email verification
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (dialog != null) {
                                                    dialog.dismiss();
                                                }
                                                if (task.isSuccessful()) {
                                                    CustomDialogUtils.showSuccessDialog(MainActivity.this, 
                                                        "Registration Successful", "Account created successfully! Please check your email and click the verification link before logging in.");
                                                    // Sign out the user until they verify their email
                                                    mAuth.signOut();
                                                } else {
                                                    CustomDialogUtils.showErrorDialog(MainActivity.this, 
                                                        "Email Verification Error", "Account created but failed to send verification email: " + task.getException().getMessage());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            CustomDialogUtils.showErrorDialog(MainActivity.this, "Registration Error", "Registration failed: " + task.getException().getMessage());
                        }
                    }
                });
    }
    
    private void registerWithEmail() {
        String email = registerEmailInput.getText().toString().trim();
        String password = registerPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        
        if (validateRegistrationInput(email, password, confirmPassword)) {
            registerWithEmailVerification(email, password, null);
        }
    }
    
    private void signInWithGoogle() {
        Log.d(TAG, "Starting Google Sign-In process...");
        Toast.makeText(this, "Starting Google Sign-In...", Toast.LENGTH_SHORT).show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Log.d(TAG, "onActivityResult called - requestCode: " + requestCode + ", resultCode: " + resultCode);
        
        // Google Sign-In result
        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "Processing Google Sign-In result...");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "Google Sign-In successful - Account ID: " + account.getId());
                Log.d(TAG, "Account Email: " + account.getEmail());
                Log.d(TAG, "Account Display Name: " + account.getDisplayName());
                Toast.makeText(this, "Google Sign-In successful, authenticating with Firebase...", Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Log.e(TAG, "Error code: " + e.getStatusCode());
                Log.e(TAG, "Error message: " + e.getMessage());
                Log.e(TAG, "Error status: " + e.getStatus());
                
                String errorMessage = "Google sign in failed";
                String detailedError = "";
                
                switch (e.getStatusCode()) {
                    case 10:
                        errorMessage = "Developer Configuration Error";
                        detailedError = "SHA-1 fingerprint not configured correctly in Firebase Console. Error code: " + e.getStatusCode();
                        break;
                    case 12500:
                        errorMessage = "Sign-In Disabled";
                        detailedError = "Google Sign-In is currently disabled for this app. Error code: " + e.getStatusCode();
                        break;
                    case 12501:
                        errorMessage = "Sign-In Cancelled";
                        detailedError = "You cancelled the sign-in process. Error code: " + e.getStatusCode();
                        break;
                    case 12502:
                        errorMessage = "Sign-In In Progress";
                        detailedError = "Another sign-in is already in progress. Error code: " + e.getStatusCode();
                        break;
                    case 7:
                        errorMessage = "Network Error";
                        detailedError = "Network connection error. Please check your internet connection. Error code: " + e.getStatusCode();
                        break;
                    default:
                        errorMessage = "Google Sign-In Failed";
                        detailedError = "Unknown error occurred. Error code: " + e.getStatusCode() + ", Message: " + e.getMessage();
                        break;
                }
                
                // Show both Toast and Dialog for better visibility
                Toast.makeText(this, errorMessage + " (Code: " + e.getStatusCode() + ")", Toast.LENGTH_LONG).show();
                showDetailedError("Google Sign-In Error", errorMessage, detailedError);
            }
        }
    }
    
    private void firebaseAuthWithGoogle(String idToken) {
        Log.d(TAG, "Starting Firebase authentication with Google credential...");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Successfully signed in with Google!", Toast.LENGTH_SHORT).show();
                            navigateToWelcome(user);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Exception exception = task.getException();
                            String errorMessage = "Firebase Authentication Failed";
                            String detailedError = "";
                            
                            if (exception != null) {
                                Log.e(TAG, "Firebase Auth Error: " + exception.getMessage());
                                Log.e(TAG, "Firebase Auth Error Class: " + exception.getClass().getSimpleName());
                                detailedError = "Error: " + exception.getMessage() + "\nType: " + exception.getClass().getSimpleName();
                                
                                // Check for specific Firebase Auth errors
                                if (exception.getMessage() != null) {
                                    if (exception.getMessage().contains("network")) {
                                        errorMessage = "Network Error";
                                        detailedError = "Please check your internet connection and try again.";
                                    } else if (exception.getMessage().contains("invalid")) {
                                        errorMessage = "Invalid Credentials";
                                        detailedError = "The Google authentication token is invalid. This might be a configuration issue.";
                                    }
                                }
                            } else {
                                detailedError = "Unknown Firebase authentication error occurred.";
                            }
                            
                            Toast.makeText(MainActivity.this, "Firebase Auth Failed", Toast.LENGTH_LONG).show();
                            showDetailedError("Firebase Authentication Error", errorMessage, detailedError);
                        }
                    }
                });
    }
    

    
    private void navigateToWelcome(FirebaseUser user) {
        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        intent.putExtra("user_email", user.getEmail());
        intent.putExtra("user_name", user.getDisplayName());
        startActivity(intent);
        finish();
    }
    
    private void showForgotPasswordDialog() {
        // Inflate the custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        
        // Get references to dialog elements
        EditText emailInput = dialogView.findViewById(R.id.forgotPasswordEmailInput);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button resetButton = dialogView.findViewById(R.id.resetPasswordButton);
        
        // Create the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        
        // Set up button click listeners
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        resetButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Send password reset email
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            CustomDialogUtils.showSuccessDialog(this, "Password Reset", "Password reset email sent to " + email);
                            dialog.dismiss();
                        } else {
                            String errorMessage = "Failed to send reset email";
                            if (task.getException() != null) {
                                errorMessage += ": " + task.getException().getMessage();
                            }
                            CustomDialogUtils.showErrorDialog(this, "Error", errorMessage);
                        }
                    });
        });
        
        dialog.show();
    }
    
    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
    
    private void showDetailedError(String title, String message, String details) {
        String fullMessage = message + "\n\nDetails:\n" + details + 
                           "\n\nIf this problem persists, please contact support with this error information.";
        
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(fullMessage)
                .setPositiveButton("OK", null)
                .setNegativeButton("Copy Error", (dialog, which) -> {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Error Details", title + "\n" + fullMessage);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Error details copied to clipboard", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
