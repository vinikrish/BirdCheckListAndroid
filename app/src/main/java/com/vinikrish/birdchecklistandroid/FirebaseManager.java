package com.vinikrish.birdchecklistandroid;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vinikrish.birdchecklistandroid.models.Bird;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * Optimized batch save method that uses existing birds data to avoid database queries
     * @param birds List of birds to save
     * @param existingBirds Map of existing birds (comName -> Bird)
     * @param listener Callback listener
     */
    public void saveBirdsOptimized(List<Bird> birds, Map<String, Bird> existingBirds, OnBirdsSavedListener listener) {
        long startTime = System.currentTimeMillis();
        Log.d("FirebaseManager", "=== FIREBASE BATCH SAVE STARTED ===");
        Log.d("FirebaseManager", "saveBirdsOptimized() called with " + birds.size() + " birds at: " + startTime);
        Log.d("FirebaseManager", "Existing birds in memory: " + existingBirds.size());
        
        if (birds.isEmpty()) {
            Log.d("FirebaseManager", "No birds to save");
            listener.onSuccess();
            return;
        }
        
        // Process birds in memory first to prepare batch operations
        long processingStartTime = System.currentTimeMillis();
        Map<String, Object> batchUpdates = new HashMap<>();
        final int[] counters = new int[2]; // [newBirds, updatedBirds]
        
        for (Bird bird : birds) {
            String comName = bird.getComName();
            String gender = bird.getGender();
            
            // Create composite key for gender-specific lookups
            String compositeKey = comName + "_" + gender;
            Bird existingBird = existingBirds.get(compositeKey);
            
            if (existingBird != null) {
                // Update existing bird with new observations
                if ("M".equals(gender) || bird.isMale()) {
                    existingBird.setMale(true);
                    existingBird.setSawMale(bird.isSaw() || existingBird.isSawMale());
                    existingBird.setPhotographedMale(bird.isPhotographed() || existingBird.isPhotographedMale());
                    existingBird.setHeardMale(bird.isHeard() || existingBird.isHeardMale());
                }
                
                if ("F".equals(gender) || bird.isFemale()) {
                    existingBird.setFemale(true);
                    existingBird.setSawFemale(bird.isSaw() || existingBird.isSawFemale());
                    existingBird.setPhotographedFemale(bird.isPhotographed() || existingBird.isPhotographedFemale());
                    existingBird.setHeardFemale(bird.isHeard() || existingBird.isHeardFemale());
                }
                
                // Update legacy fields
                existingBird.setSaw(existingBird.isSawMale() || existingBird.isSawFemale());
                existingBird.setPhotographed(existingBird.isPhotographedMale() || existingBird.isPhotographedFemale());
                existingBird.setHeard(existingBird.isHeardMale() || existingBird.isHeardFemale());
                
                if (bird.getCountry() != null) existingBird.setCountry(bird.getCountry());
                
                // Add to batch update
                batchUpdates.put(BIRDS_PATH + "/" + existingBird.getId(), existingBird);
                counters[1]++; // updatedBirds
            } else {
                // Create new bird
                String key = databaseReference.child(BIRDS_PATH).push().getKey();
                if (key != null) {
                    bird.setId(key);
                    batchUpdates.put(BIRDS_PATH + "/" + key, bird);
                    existingBirds.put(compositeKey, bird); // Add to memory for future operations with composite key
                    counters[0]++; // newBirds
                }
            }
        }
        
        long processingEndTime = System.currentTimeMillis();
        Log.d("FirebaseManager", "In-memory processing took: " + (processingEndTime - processingStartTime) + "ms");
        Log.d("FirebaseManager", "Prepared batch: " + counters[0] + " new birds, " + counters[1] + " updates");
        
        // Execute single batch write
        long batchStartTime = System.currentTimeMillis();
        Log.d("FirebaseManager", "Starting BATCH WRITE with " + batchUpdates.size() + " operations at: " + batchStartTime);
        
        databaseReference.updateChildren(batchUpdates)
            .addOnSuccessListener(aVoid -> {
                long batchEndTime = System.currentTimeMillis();
                long totalTime = batchEndTime - startTime;
                long batchTime = batchEndTime - batchStartTime;
                
                Log.d("FirebaseManager", "=== FIREBASE BATCH SAVE COMPLETED ===");
                Log.d("FirebaseManager", "Batch write took: " + batchTime + "ms");
                Log.d("FirebaseManager", "Total operation took: " + totalTime + "ms");
                Log.d("FirebaseManager", "Saved " + birds.size() + " birds (" + counters[0] + " new, " + counters[1] + " updated)");
                Log.d("FirebaseManager", "Average time per bird: " + (totalTime / birds.size()) + "ms");
                Log.d("FirebaseManager", "Performance improvement: " + (4800 - totalTime) + "ms faster than previous method");
                
                listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                long batchEndTime = System.currentTimeMillis();
                long totalTime = batchEndTime - startTime;
                long batchTime = batchEndTime - batchStartTime;
                
                Log.e("FirebaseManager", "BATCH WRITE FAILED after " + batchTime + "ms (total: " + totalTime + "ms)", e);
                listener.onFailure(e.getMessage());
            });
    }
    
    /**
     * Optimized save or update method that uses existing birds data
     * @param bird Bird to save or update
     * @param existingBirds Map of existing birds (comName -> Bird)
     * @param listener Callback listener
     */
    private void saveOrUpdateBirdOptimized(Bird bird, Map<String, Bird> existingBirds, OnBirdsSavedListener listener) {
        long methodStartTime = System.currentTimeMillis();
        String comName = bird.getComName();
        String gender = bird.getGender();
        
        Log.d("FirebaseManager", "saveOrUpdateBirdOptimized started for: " + comName + " (gender: " + gender + ") at: " + methodStartTime);
        
        // Check if bird with same comName already exists in memory
        long lookupStartTime = System.currentTimeMillis();
        Bird existingBird = existingBirds.get(comName);
        long lookupEndTime = System.currentTimeMillis();
        Log.d("FirebaseManager", "Memory lookup took: " + (lookupEndTime - lookupStartTime) + "ms");
        
        if (existingBird != null) {
            Log.d("FirebaseManager", "Found existing bird: " + comName + ", updating with new observations");
            
            // Update the existing bird with new observations based on gender
            if ("M".equals(gender) || bird.isMale()) {
                existingBird.setMale(true);
                existingBird.setSawMale(bird.isSaw() || existingBird.isSawMale());
                existingBird.setPhotographedMale(bird.isPhotographed() || existingBird.isPhotographedMale());
                existingBird.setHeardMale(bird.isHeard() || existingBird.isHeardMale());
                Log.d("FirebaseManager", "Updated male observations for: " + comName);
            }
            
            if ("F".equals(gender) || bird.isFemale()) {
                existingBird.setFemale(true);
                existingBird.setSawFemale(bird.isSaw() || existingBird.isSawFemale());
                existingBird.setPhotographedFemale(bird.isPhotographed() || existingBird.isPhotographedFemale());
                existingBird.setHeardFemale(bird.isHeard() || existingBird.isHeardFemale());
                Log.d("FirebaseManager", "Updated female observations for: " + comName);
            }
            
            // Update legacy fields for backward compatibility
            existingBird.setSaw(existingBird.isSawMale() || existingBird.isSawFemale());
            existingBird.setPhotographed(existingBird.isPhotographedMale() || existingBird.isPhotographedFemale());
            existingBird.setHeard(existingBird.isHeardMale() || existingBird.isHeardFemale());
            
            // Update other fields that might have changed
            if (bird.getCountry() != null) existingBird.setCountry(bird.getCountry());
            
            long dbUpdateStartTime = System.currentTimeMillis();
            Log.d("FirebaseManager", "Starting database UPDATE for: " + comName + " at: " + dbUpdateStartTime);
            
            databaseReference.child(BIRDS_PATH).child(existingBird.getId()).setValue(existingBird)
                .addOnSuccessListener(aVoid -> {
                    long dbUpdateEndTime = System.currentTimeMillis();
                    long totalMethodTime = dbUpdateEndTime - methodStartTime;
                    long dbTime = dbUpdateEndTime - dbUpdateStartTime;
                    Log.d("FirebaseManager", "Successfully UPDATED existing bird: " + comName + " - DB time: " + dbTime + "ms, Total method time: " + totalMethodTime + "ms");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    long dbUpdateEndTime = System.currentTimeMillis();
                    long totalMethodTime = dbUpdateEndTime - methodStartTime;
                    long dbTime = dbUpdateEndTime - dbUpdateStartTime;
                    Log.e("FirebaseManager", "Failed to UPDATE existing bird: " + comName + " - DB time: " + dbTime + "ms, Total method time: " + totalMethodTime + "ms", e);
                    listener.onFailure(e.getMessage());
                });
        } else {
            Log.d("FirebaseManager", "No existing bird found for: " + comName + ", creating new record");
            // Create new bird
            long keyGenStartTime = System.currentTimeMillis();
            String key = databaseReference.child(BIRDS_PATH).push().getKey();
            long keyGenEndTime = System.currentTimeMillis();
            Log.d("FirebaseManager", "Key generation took: " + (keyGenEndTime - keyGenStartTime) + "ms");
            
            if (key != null) {
                bird.setId(key);
                long dbInsertStartTime = System.currentTimeMillis();
                Log.d("FirebaseManager", "Starting database INSERT for: " + comName + " at: " + dbInsertStartTime);
                
                databaseReference.child(BIRDS_PATH).child(key).setValue(bird)
                    .addOnSuccessListener(aVoid -> {
                        long dbInsertEndTime = System.currentTimeMillis();
                        long totalMethodTime = dbInsertEndTime - methodStartTime;
                        long dbTime = dbInsertEndTime - dbInsertStartTime;
                        Log.d("FirebaseManager", "Successfully INSERTED new bird: " + comName + " - DB time: " + dbTime + "ms, Total method time: " + totalMethodTime + "ms");
                        // Add to existingBirds map for future saves in this session
                        existingBirds.put(comName, bird);
                        listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        long dbInsertEndTime = System.currentTimeMillis();
                        long totalMethodTime = dbInsertEndTime - methodStartTime;
                        long dbTime = dbInsertEndTime - dbInsertStartTime;
                        Log.e("FirebaseManager", "Failed to INSERT new bird: " + comName + " - DB time: " + dbTime + "ms, Total method time: " + totalMethodTime + "ms", e);
                        listener.onFailure(e.getMessage());
                    });
            } else {
                long totalMethodTime = System.currentTimeMillis() - methodStartTime;
                Log.e("FirebaseManager", "Failed to generate bird ID - Total method time: " + totalMethodTime + "ms");
                listener.onFailure("Failed to generate bird ID");
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