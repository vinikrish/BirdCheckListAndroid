package com.vinikrish.birdchecklistandroid;

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
            if (!existingBirds.containsKey(bird.getComName())) {
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
        int logCount = 0;
        for (Map.Entry<String, Bird> entry : existingBirds.entrySet()) {
            String birdName = entry.getKey();
            Bird existingBird = entry.getValue();
            android.util.Log.d("GroupedBirdAdapter", "Looking for existing bird: '" + birdName + "'");
            
            boolean found = false;
            // Find matching bird in current display and update checkbox states
            for (BirdGroup group : birdGroups) {
                for (Bird bird : group.getBirds()) {
                    // Only log first few comparisons to avoid spam
                    if (logCount < 5) {
                        android.util.Log.d("GroupedBirdAdapter", "Sample comparison - existing: '" + birdName + "' vs JSON: '" + bird.getComName() + "'");
                        logCount++;
                    }
                    if (bird.getComName().equals(birdName)) {
                        android.util.Log.d("GroupedBirdAdapter", "Match found! Setting checkbox states for: " + bird.getComName());
                        boolean[] states = new boolean[6];
                        // Set states: [sawMale, photographedMale, heardMale, sawFemale, photographedFemale, heardFemale]
                        if (existingBird.isMale()) {
                            states[0] = existingBird.isSaw(); // sawMale
                            states[1] = existingBird.isPhotographed(); // photographedMale
                            states[2] = existingBird.isHeard(); // heardMale
                        }
                        if (existingBird.isFemale()) {
                            states[3] = existingBird.isSaw(); // sawFemale
                            states[4] = existingBird.isPhotographed(); // photographedFemale
                            states[5] = existingBird.isHeard(); // heardFemale
                        }
                        checkboxStates.put(bird, states);
                        found = true;
                        break;
                    }
                }
                if (found) break;
            }
            
            if (!found) {
                android.util.Log.d("GroupedBirdAdapter", "No match found for existing bird: '" + birdName + "'");
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
        CheckBox checkSawMale, checkPhotographedMale, checkHeardMale;
        CheckBox checkSawFemale, checkPhotographedFemale, checkHeardFemale;
        
        public BirdViewHolder(@NonNull View itemView) {
            super(itemView);
            birdName = itemView.findViewById(R.id.birdNameText);
            // Male checkboxes
            checkSawMale = itemView.findViewById(R.id.checkSawMale);
            checkPhotographedMale = itemView.findViewById(R.id.checkPhotographedMale);
            checkHeardMale = itemView.findViewById(R.id.checkHeardMale);
            // Female checkboxes
            checkSawFemale = itemView.findViewById(R.id.checkSawFemale);
            checkPhotographedFemale = itemView.findViewById(R.id.checkPhotographedFemale);
            checkHeardFemale = itemView.findViewById(R.id.checkHeardFemale);
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
    
    private void showCustomTooltip(View anchorView, String message) {
        // Inflate the custom tooltip layout
        LayoutInflater inflater = LayoutInflater.from(anchorView.getContext());
        View tooltipView = inflater.inflate(R.layout.custom_tooltip, null);
        
        // Set the tooltip text
        TextView tooltipText = tooltipView.findViewById(R.id.tooltipText);
        tooltipText.setText(message);
        
        // Create and configure the popup window
        PopupWindow popupWindow = new PopupWindow(tooltipView, 
            ViewGroup.LayoutParams.WRAP_CONTENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT, 
            true);
        
        // Set popup window properties
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        
        // Calculate position to show above the anchor view
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        
        // Show the popup above the anchor view
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, 
            location[0], location[1] - tooltipView.getMeasuredHeight() - 20);
        
        // Auto-dismiss after 2 seconds
        anchorView.postDelayed(popupWindow::dismiss, 2000);
    }
}