
package com.vinikrish.birdchecklistandroid;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;
import com.vinikrish.birdchecklistandroid.models.Bird;

import java.util.ArrayList;
import java.util.List;

public class LifeListFragment extends Fragment implements GroupedLifeListAdapter.OnBirdRemoveListener {
    private static final String TAG = "LifeListFragment";
    private static final String ARG_USERNAME = "username";
    private static final String ARG_USER_ID = "userId";
    
    private RecyclerView recyclerView;
    private GroupedLifeListAdapter adapter;
    private List<Bird> birdList;
    private String username;
    private String userId;
    private TextView emptyStateText;
    private ValueEventListener birdsListener;
    
    public static LifeListFragment newInstance(String username, String userId) {
        LifeListFragment fragment = new LifeListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(ARG_USERNAME);
            userId = getArguments().getString(ARG_USER_ID);
        }
        Log.d(TAG, "LifeListFragment created with username: " + username + ", userId: " + userId);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_life_list, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        
        birdList = new ArrayList<>();
        adapter = new GroupedLifeListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        loadBirdsFromFirebase();
        
        return view;
    }
    
    @Override
    public void onBirdRemove(Bird bird) {
        if (bird.getId() == null) {
            Toast.makeText(getContext(), "Cannot remove bird: missing ID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Removing bird: " + bird.getComName() + " with ID: " + bird.getId());
        
        DatabaseReference birdsRef = FirebaseManager.getInstance().getBirdsReference();
        birdsRef.child(bird.getId())
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Bird successfully deleted from Firebase");
                    
                    // Remove from local list and update adapter
                    birdList.remove(bird);
                    adapter.updateBirds(birdList);
                    updateEmptyState();
                    
                    // Notify other fragments to refresh
                    if (getActivity() instanceof WelcomeActivity) {
                        ((WelcomeActivity) getActivity()).refreshProfileBirdCount();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting bird from Firebase: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Failed to remove bird", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void loadBirdsFromFirebase() {
        if (userId == null) {
            Log.e(TAG, "Cannot load birds: userId is null");
            return;
        }
        
        Log.d(TAG, "Loading birds from Firebase for userId: " + userId);
        
        DatabaseReference birdsRef = FirebaseManager.getInstance().getBirdsReference();
        
        // Remove existing listener if it exists
        if (birdsListener != null) {
            birdsRef.removeEventListener(birdsListener);
        }
        
        // Create new listener
        birdsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "Firebase query successful, found " + dataSnapshot.getChildrenCount() + " documents");
                birdList.clear();
                
                for (DataSnapshot birdSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Bird bird = birdSnapshot.getValue(Bird.class);
                        if (bird != null && userId.equals(bird.getUserId())) {
                            bird.setId(birdSnapshot.getKey());
                            birdList.add(bird);
                            Log.d(TAG, "Added bird: " + bird.getComName() + ", Gender: " + bird.getGender() + 
                                  ", Saw: " + bird.isSaw() + ", Photographed: " + bird.isPhotographed() + 
                                  ", Heard: " + bird.isHeard());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error converting snapshot to Bird: " + e.getMessage(), e);
                    }
                }
                
                Log.d(TAG, "Total birds loaded: " + birdList.size());
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        adapter.updateBirds(birdList);
                        updateEmptyState();
                    });
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading birds from Firebase: " + databaseError.getMessage(), databaseError.toException());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> updateEmptyState());
                }
            }
        };
        
        // Add the listener to the database reference
        birdsRef.addValueEventListener(birdsListener);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove listener to prevent memory leaks
        if (birdsListener != null) {
            DatabaseReference birdsRef = FirebaseManager.getInstance().getBirdsReference();
            birdsRef.removeEventListener(birdsListener);
            birdsListener = null;
        }
    }
    
    private void updateEmptyState() {
        if (birdList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }
    
    public void refreshBirdList() {
        Log.d(TAG, "Refreshing bird list");
        loadBirdsFromFirebase();
    }
}
