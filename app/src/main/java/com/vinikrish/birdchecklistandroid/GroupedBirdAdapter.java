package com.vinikrish.birdchecklistandroid;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.vinikrish.birdchecklistandroid.models.Bird;
import com.vinikrish.birdchecklistandroid.models.BirdGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupedBirdAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_BIRD = 1;
    
    private List<BirdGroup> birdGroups;
    private List<BirdGroup> originalBirdGroups; // Keep original data for filtering
    private List<Object> displayItems; // Mixed list of headers and birds
    private Map<Bird, boolean[]> checkboxStates; // Bird -> [female, male, saw, photographed, heard]
    private Map<String, Bird> existingBirds = new HashMap<>();
    
    public GroupedBirdAdapter(List<BirdGroup> birdGroups) {
        this.originalBirdGroups = birdGroups != null ? new ArrayList<>(birdGroups) : new ArrayList<>();
        this.birdGroups = birdGroups != null ? birdGroups : new ArrayList<>();
        this.checkboxStates = new HashMap<>();
        updateDisplayItems();
    }
    
    public void updateDisplayItems() {
        android.util.Log.d("GroupedBirdAdapter", "updateDisplayItems called with " + birdGroups.size() + " groups");
        displayItems = new ArrayList<>();
        
        for (BirdGroup group : birdGroups) {
            displayItems.add(group);
            if (group.isExpanded()) {
                List<Bird> birds = group.getBirds();
                android.util.Log.d("GroupedBirdAdapter", "Adding " + birds.size() + " birds from group " + group.getLetter());
                if (birds != null && !birds.isEmpty()) {
                    displayItems.addAll(birds);
                } else {
                    android.util.Log.w("GroupedBirdAdapter", "Group " + group.getLetter() + " has no birds to add");
                }
            }
        }
        
        android.util.Log.d("GroupedBirdAdapter", "displayItems updated, size: " + displayItems.size());
        notifyDataSetChanged();
    }
    
    @Override
    public int getItemViewType(int position) {
        Object item = displayItems.get(position);
        return item instanceof BirdGroup ? TYPE_HEADER : TYPE_BIRD;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bird_group_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bird_dual_row, parent, false);
            return new BirdViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = displayItems.get(position);
        android.util.Log.d("GroupedBirdAdapter", "onBindViewHolder called for position: " + position + ", item type: " + (item instanceof BirdGroup ? "BirdGroup" : "Bird"));
        
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            BirdGroup group = (BirdGroup) item;
            headerHolder.bind(group);
            android.util.Log.d("GroupedBirdAdapter", "Binding header for group: " + group.getLetter() + ", bird count: " + group.getBirdCount());
            
            headerHolder.itemView.setOnClickListener(v -> {
                group.setExpanded(!group.isExpanded());
                updateDisplayItems();
                notifyDataSetChanged();
            });
        } else if (holder instanceof BirdViewHolder) {
            BirdViewHolder birdHolder = (BirdViewHolder) holder;
            Bird bird = (Bird) item;
            birdHolder.bind(bird);
            android.util.Log.d("GroupedBirdAdapter", "Binding bird: " + bird.getComName());
        }
    }
    
    @Override
    public int getItemCount() {
        int count = displayItems.size();
        android.util.Log.d("GroupedBirdAdapter", "getItemCount called, returning: " + count);
        return count;
    }
    
    public void updateBirdGroups(List<BirdGroup> newBirdGroups) {
        android.util.Log.d("GroupedBirdAdapter", "updateBirdGroups called with " + (newBirdGroups != null ? newBirdGroups.size() : 0) + " groups");
        this.originalBirdGroups = newBirdGroups != null ? new ArrayList<>(newBirdGroups) : new ArrayList<>();
        this.birdGroups = newBirdGroups != null ? newBirdGroups : new ArrayList<>();
        
        // Log details about each group - groups start collapsed by default
        for (int i = 0; i < birdGroups.size(); i++) {
            BirdGroup group = birdGroups.get(i);
            android.util.Log.d("GroupedBirdAdapter", "Group " + i + ": letter=" + group.getLetter() + ", birds=" + group.getBirds().size() + ", expanded=" + group.isExpanded());
            
            // Log each bird in the group for debugging
            for (Bird bird : group.getBirds()) {
                android.util.Log.d("GroupedBirdAdapter", "  - Bird: " + bird.getComName());
            }
        }
        
        // DON'T clear checkbox states here - preserve existing states
        // this.checkboxStates.clear(); // REMOVED - this was causing the issue
        
        int totalBirds = 0;
        for (BirdGroup group : birdGroups) {
            totalBirds += group.getBirds().size();
            android.util.Log.d("GroupedBirdAdapter", "Group: " + group.getLetter() + ", Birds: " + group.getBirdCount());
        }
        android.util.Log.d("GroupedBirdAdapter", "Total birds after update: " + totalBirds);
        updateDisplayItems();
        android.util.Log.d("GroupedBirdAdapter", "After updateDisplayItems, display items count: " + displayItems.size());
        notifyDataSetChanged();
    }
    
    public List<Bird> getSelectedBirds() {
        List<Bird> selectedBirds = new ArrayList<>();
        for (Map.Entry<Bird, boolean[]> entry : checkboxStates.entrySet()) {
            boolean[] states = entry.getValue();
            // Check if any checkbox is checked [sawMale, photographedMale, heardMale, sawFemale, photographedFemale, heardFemale]
            // Need at least one observation type checked
            boolean hasObservation = states[0] || states[1] || states[2] || states[3] || states[4] || states[5];
            if (hasObservation) {
                selectedBirds.add(entry.getKey());
            }
        }
        return selectedBirds;
    }
    

    
    public Map<Bird, boolean[]> getCheckboxStates() {
        return checkboxStates;
    }
    
    public void setExistingBirds(Map<String, Bird> existingBirds) {
        this.existingBirds = existingBirds;
        android.util.Log.d("GroupedBirdAdapter", "setExistingBirds called with " + existingBirds.size() + " birds");
        android.util.Log.d("GroupedBirdAdapter", "Current birdGroups size: " + birdGroups.size());
        
        // First, clear checkbox states for all birds that are no longer in existingBirds
        List<Bird> birdsToRemove = new ArrayList<>();
        for (Bird bird : checkboxStates.keySet()) {
            // Check if either male or female record exists for this bird
            String maleKey = bird.getComName() + "_M";
            String femaleKey = bird.getComName() + "_F";
            if (!existingBirds.containsKey(maleKey) && !existingBirds.containsKey(femaleKey)) {
                birdsToRemove.add(bird);
                android.util.Log.d("GroupedBirdAdapter", "Clearing checkbox states for removed bird: " + bird.getComName());
            }
        }
        
        // Remove checkbox states for birds no longer in existingBirds
        for (Bird bird : birdsToRemove) {
            checkboxStates.remove(bird);
        }
        
        // Log some sample birds from current groups for debugging
        int totalBirdsInGroups = 0;
        for (BirdGroup group : birdGroups) {
            totalBirdsInGroups += group.getBirds().size();
        }
        android.util.Log.d("GroupedBirdAdapter", "Total birds in current groups: " + totalBirdsInGroups);
        
        // Update checkbox states for existing birds
        // Process each bird in the current groups and look for corresponding male/female records
        for (BirdGroup group : birdGroups) {
            for (Bird bird : group.getBirds()) {
                String maleKey = bird.getComName() + "_M";
                String femaleKey = bird.getComName() + "_F";
                
                Bird maleRecord = existingBirds.get(maleKey);
                Bird femaleRecord = existingBirds.get(femaleKey);
                
                if (maleRecord != null || femaleRecord != null) {
                    android.util.Log.d("GroupedBirdAdapter", "Setting checkbox states for: " + bird.getComName() + 
                                      " (Male: " + (maleRecord != null) + ", Female: " + (femaleRecord != null) + ")");
                    
                    boolean[] states = new boolean[6];
                    // Set states: [sawMale, photographedMale, heardMale, sawFemale, photographedFemale, heardFemale]
                    
                    // Set male observation states
                    if (maleRecord != null) {
                        states[0] = maleRecord.isSawMale(); // sawMale
                        states[1] = maleRecord.isPhotographedMale(); // photographedMale
                        states[2] = maleRecord.isHeardMale(); // heardMale
                        android.util.Log.d("GroupedBirdAdapter", "Male observations - Saw: " + states[0] + 
                                          ", Photographed: " + states[1] + ", Heard: " + states[2]);
                    }
                    
                    // Set female observation states
                    if (femaleRecord != null) {
                        states[3] = femaleRecord.isSawFemale(); // sawFemale
                        states[4] = femaleRecord.isPhotographedFemale(); // photographedFemale
                        states[5] = femaleRecord.isHeardFemale(); // heardFemale
                        android.util.Log.d("GroupedBirdAdapter", "Female observations - Saw: " + states[3] + 
                                          ", Photographed: " + states[4] + ", Heard: " + states[5]);
                    }
                    
                    checkboxStates.put(bird, states);
                }
            }
        }
        notifyDataSetChanged();
    }
    
    public void filter(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            // Reset to original data
            birdGroups.clear();
            birdGroups.addAll(originalBirdGroups);
        } else {
            // Filter birds based on search text
            List<BirdGroup> filteredGroups = new ArrayList<>();
            String searchLower = searchText.toLowerCase().trim();
            
            for (BirdGroup originalGroup : originalBirdGroups) {
                List<Bird> filteredBirds = new ArrayList<>();
                for (Bird bird : originalGroup.getBirds()) {
                    if (bird.getComName().toLowerCase().contains(searchLower)) {
                        filteredBirds.add(bird);
                    }
                }
                
                if (!filteredBirds.isEmpty()) {
                    BirdGroup filteredGroup = new BirdGroup(originalGroup.getLetter(), filteredBirds);
                    filteredGroup.setExpanded(true); // Auto-expand filtered groups
                    filteredGroups.add(filteredGroup);
                }
            }
            
            birdGroups.clear();
            birdGroups.addAll(filteredGroups);
        }
        
        updateDisplayItems();
        notifyDataSetChanged();
    }
    
    // Header ViewHolder
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView letterText;
        TextView countText;
        TextView expandIcon;
        
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            letterText = itemView.findViewById(R.id.letterText);
            countText = itemView.findViewById(R.id.countText);
            expandIcon = itemView.findViewById(R.id.expandIcon);
        }
        
        public void bind(BirdGroup group) {
            letterText.setText(group.getLetter());
            countText.setText("(" + group.getBirdCount() + ")");
            expandIcon.setText(group.isExpanded() ? "▼" : "▶");
        }
    }
    
    // Bird ViewHolder
    class BirdViewHolder extends RecyclerView.ViewHolder {
        TextView birdName;
        ImageView maleIcon, femaleIcon;
        CheckBox checkSawMale, checkPhotographedMale, checkHeardMale;
        CheckBox checkSawFemale, checkPhotographedFemale, checkHeardFemale;
        
        public BirdViewHolder(@NonNull View itemView) {
            super(itemView);
            birdName = itemView.findViewById(R.id.birdNameText);
            // M/F icons
            maleIcon = itemView.findViewById(R.id.maleIcon);
            femaleIcon = itemView.findViewById(R.id.femaleIcon);
            // Male checkboxes
            checkSawMale = itemView.findViewById(R.id.checkSawMale);
            checkPhotographedMale = itemView.findViewById(R.id.checkPhotographedMale);
            checkHeardMale = itemView.findViewById(R.id.checkHeardMale);
            // Female checkboxes
            checkSawFemale = itemView.findViewById(R.id.checkSawFemale);
            checkPhotographedFemale = itemView.findViewById(R.id.checkPhotographedFemale);
            checkHeardFemale = itemView.findViewById(R.id.checkHeardFemale);
            
            // Set click listeners for M/F icons
            maleIcon.setOnClickListener(v -> showIconDescription(v.getContext(), "Male", "Indicates observations for male birds"));
            femaleIcon.setOnClickListener(v -> showIconDescription(v.getContext(), "Female", "Indicates observations for female birds"));
        }
        
        public void bind(Bird bird) {
            birdName.setText(bird.getComName());
            
            // Get or create checkbox states for this bird
            boolean[] states = checkboxStates.get(bird);
            if (states == null) {
                states = new boolean[6]; // [sawMale, photographedMale, heardMale, sawFemale, photographedFemale, heardFemale]
                checkboxStates.put(bird, states);
            }
            
            // Make states effectively final for lambda expressions
            final boolean[] finalStates = states;
            
            // Clear listeners first to prevent triggering during setChecked
            checkSawMale.setOnCheckedChangeListener(null);
            checkPhotographedMale.setOnCheckedChangeListener(null);
            checkHeardMale.setOnCheckedChangeListener(null);
            checkSawFemale.setOnCheckedChangeListener(null);
            checkPhotographedFemale.setOnCheckedChangeListener(null);
            checkHeardFemale.setOnCheckedChangeListener(null);
            
            // Set checkbox states directly from array
            // [sawMale, photographedMale, heardMale, sawFemale, photographedFemale, heardFemale]
            checkSawMale.setChecked(finalStates[0]);
            checkPhotographedMale.setChecked(finalStates[1]);
            checkHeardMale.setChecked(finalStates[2]);
            checkSawFemale.setChecked(finalStates[3]);
            checkPhotographedFemale.setChecked(finalStates[4]);
            checkHeardFemale.setChecked(finalStates[5]);
            
            // Set listeners after setting states
            checkSawMale.setOnCheckedChangeListener((buttonView, isChecked) -> {
                finalStates[0] = isChecked;
            });
            
            checkPhotographedMale.setOnCheckedChangeListener((buttonView, isChecked) -> {
                finalStates[1] = isChecked;
            });
            
            checkHeardMale.setOnCheckedChangeListener((buttonView, isChecked) -> {
                finalStates[2] = isChecked;
            });
            
            checkSawFemale.setOnCheckedChangeListener((buttonView, isChecked) -> {
                finalStates[3] = isChecked;
            });
            
            checkPhotographedFemale.setOnCheckedChangeListener((buttonView, isChecked) -> {
                finalStates[4] = isChecked;
            });
            
            checkHeardFemale.setOnCheckedChangeListener((buttonView, isChecked) -> {
                finalStates[5] = isChecked;
            });
        }
    }
    
    private void showIconDescription(Context context, String title, String description) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton("OK", null)
                .show();
    }
}