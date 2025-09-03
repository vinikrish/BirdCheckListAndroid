package com.vinikrish.birdchecklistandroid;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vinikrish.birdchecklistandroid.models.Bird;
import android.util.Log;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton class to manage Firebase database operations
 */
public class FirebaseManager {
    private static FirebaseManager instance;
    private final DatabaseReference databaseReference;
    private static final String BIRDS_PATH = "birds";
    
    private FirebaseManager() {
        // Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        
        // Persistence is already enabled in BirdCheckListApplication
    }
    
    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }
    
    /**
     * Interface for bird save callbacks
     */
    public interface OnBirdsSavedListener {
        void onSuccess();
        void onFailure(String error);
    }
    
    /**
     * Save a list of birds to Firebase (synchronous version)
     * @param birds List of birds to save
     */
    public void saveBirds(List<Bird> birds) {
        Log.d("FirebaseManager", "saveBirds() called with " + birds.size() + " birds");
        for (Bird bird : birds) {
            // Set observation flags based on the checkboxes
            // Generate a unique key for each bird entry
            String key = databaseReference.child(BIRDS_PATH).push().getKey();
            if (key != null) {
                bird.setId(key);
                Log.d("FirebaseManager", "Saving bird: " + bird.getComName() + " with key: " + key);
                databaseReference.child(BIRDS_PATH).child(key).setValue(bird);
            }
        }
    }
    
    /**
     * Save a list of birds to Firebase with callback
     * @param birds List of birds to save
     * @param listener Callback listener
     */
    public void saveBirds(List<Bird> birds, OnBirdsSavedListener listener) {
        Log.d("FirebaseManager", "saveBirds() with callback called with " + birds.size() + " birds");
        
        if (birds.isEmpty()) {
            listener.onSuccess();
            return;
        }
        
        AtomicInteger pendingWrites = new AtomicInteger(birds.size());
        
        for (Bird bird : birds) {
            saveOrUpdateBird(bird, new OnBirdsSavedListener() {
                @Override
                public void onSuccess() {
                    if (pendingWrites.decrementAndGet() == 0) {
                        Log.d("FirebaseManager", "All birds saved successfully");
                        listener.onSuccess();
                    }
                }
                
                @Override
                public void onFailure(String error) {
                    Log.e("FirebaseManager", "Failed to save bird: " + bird.getComName() + ", error: " + error);
                    listener.onFailure(error);
                }
            });
        }
    }
    
    /**
     * Save or update a single bird - checks for existing entry first
     * @param bird Bird to save or update
     * @param listener Callback listener
     */
    private void saveOrUpdateBird(Bird bird, OnBirdsSavedListener listener) {
        String userId = bird.getUserId();
        String comName = bird.getComName();
        String gender = bird.getGender();
        
        Log.d("FirebaseManager", "saveOrUpdateBird: " + comName + " (" + gender + ") for user " + userId);
        
        // Query for existing bird with same userId, comName, and gender
        databaseReference.child(BIRDS_PATH)
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                    String existingKey = null;
                    
                    // Look for existing bird with same common name and gender
                    for (com.google.firebase.database.DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        Bird existingBird = childSnapshot.getValue(Bird.class);
                        if (existingBird != null && 
                            comName.equals(existingBird.getComName()) && 
                            gender.equals(existingBird.getGender())) {
                            existingKey = childSnapshot.getKey();
                            Log.d("FirebaseManager", "Found existing bird: " + comName + " (" + gender + ") with key: " + existingKey);
                            break;
                        }
                    }
                    
                    if (existingKey != null) {
                        // Update existing bird
                        bird.setId(existingKey);
                        Log.d("FirebaseManager", "Updating existing bird: " + comName + " with key: " + existingKey);
                        databaseReference.child(BIRDS_PATH).child(existingKey).setValue(bird)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("FirebaseManager", "Successfully updated bird: " + bird.getComName());
                                listener.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirebaseManager", "Failed to update bird: " + bird.getComName(), e);
                                listener.onFailure(e.getMessage());
                            });
                    } else {
                        // Create new bird
                        String key = databaseReference.child(BIRDS_PATH).push().getKey();
                        if (key != null) {
                            bird.setId(key);
                            Log.d("FirebaseManager", "Creating new bird: " + comName + " with key: " + key);
                            databaseReference.child(BIRDS_PATH).child(key).setValue(bird)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FirebaseManager", "Successfully created bird: " + bird.getComName());
                                    listener.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirebaseManager", "Failed to create bird: " + bird.getComName(), e);
                                    listener.onFailure(e.getMessage());
                                });
                        } else {
                            Log.e("FirebaseManager", "Failed to generate key for bird: " + bird.getComName());
                            listener.onFailure("Failed to generate unique key");
                        }
                    }
                }
                
                @Override
                public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError databaseError) {
                    Log.e("FirebaseManager", "Database query cancelled: " + databaseError.getMessage());
                    listener.onFailure(databaseError.getMessage());
                }
            });
    }
    
    /**
     * Get the database reference for birds
     * @return DatabaseReference for birds path
     */
    public DatabaseReference getBirdsReference() {
        return databaseReference.child(BIRDS_PATH);
    }
}