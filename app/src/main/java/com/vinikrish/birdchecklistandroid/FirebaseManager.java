package com.vinikrish.birdchecklistandroid;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vinikrish.birdchecklistandroid.models.Bird;

import java.util.List;

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
        
        // Enable offline capabilities
        database.setPersistenceEnabled(true);
    }
    
    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }
    
    /**
     * Save a list of birds to Firebase
     * @param birds List of birds to save
     */
    public void saveBirds(List<Bird> birds) {
        for (Bird bird : birds) {
            // Set observation flags based on the checkboxes
            // Generate a unique key for each bird entry
            String key = databaseReference.child(BIRDS_PATH).push().getKey();
            if (key != null) {
                bird.setId(key);
                databaseReference.child(BIRDS_PATH).child(key).setValue(bird);
            }
        }
    }
    
    /**
     * Get the database reference for birds
     * @return DatabaseReference for birds path
     */
    public DatabaseReference getBirdsReference() {
        return databaseReference.child(BIRDS_PATH);
    }
}