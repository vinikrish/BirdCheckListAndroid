package com.vinikrish.birdchecklistandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
    private List<Object> displayItems; // Mixed list of headers and birds
    private Map<Bird, boolean[]> checkboxStates; // Bird -> [female, male, saw, photographed, heard]
    
    public GroupedBirdAdapter(List<BirdGroup> birdGroups) {
        this.birdGroups = birdGroups != null ? birdGroups : new ArrayList<>();
        this.checkboxStates = new HashMap<>();
        updateDisplayItems();
    }
    
    private void updateDisplayItems() {
        displayItems = new ArrayList<>();
        for (BirdGroup group : birdGroups) {
            displayItems.add(group); // Add header
            if (group.isExpanded()) {
                displayItems.addAll(group.getBirds()); // Add birds if expanded
            }
        }
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
                    .inflate(R.layout.item_bird, parent, false);
            return new BirdViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = displayItems.get(position);
        
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            BirdGroup group = (BirdGroup) item;
            headerHolder.bind(group);
            
            headerHolder.itemView.setOnClickListener(v -> {
                group.setExpanded(!group.isExpanded());
                updateDisplayItems();
                notifyDataSetChanged();
            });
        } else if (holder instanceof BirdViewHolder) {
            BirdViewHolder birdHolder = (BirdViewHolder) holder;
            Bird bird = (Bird) item;
            birdHolder.bind(bird);
        }
    }
    
    @Override
    public int getItemCount() {
        return displayItems.size();
    }
    
    public void updateBirdGroups(List<BirdGroup> newBirdGroups) {
        this.birdGroups = newBirdGroups != null ? newBirdGroups : new ArrayList<>();
        this.checkboxStates.clear();
        updateDisplayItems();
        notifyDataSetChanged();
    }
    
    public List<Bird> getSelectedBirds() {
        List<Bird> selectedBirds = new ArrayList<>();
        for (Map.Entry<Bird, boolean[]> entry : checkboxStates.entrySet()) {
            boolean[] states = entry.getValue();
            // Check if any checkbox is checked
            if (states[0] || states[1] || states[2] || states[3] || states[4]) {
                selectedBirds.add(entry.getKey());
            }
        }
        return selectedBirds;
    }
    
    public Map<Bird, boolean[]> getCheckboxStates() {
        return checkboxStates;
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
        CheckBox checkFemale, checkMale, checkSaw, checkPhotographed, checkHeard;
        
        public BirdViewHolder(@NonNull View itemView) {
            super(itemView);
            birdName = itemView.findViewById(R.id.birdName);
            checkFemale = itemView.findViewById(R.id.checkF);
            checkMale = itemView.findViewById(R.id.checkM);
            checkSaw = itemView.findViewById(R.id.checkSaw);
            checkPhotographed = itemView.findViewById(R.id.checkPhotographed);
            checkHeard = itemView.findViewById(R.id.checkHeard);
        }
        
        public void bind(Bird bird) {
            birdName.setText(bird.getComName());
            
            // Get or create checkbox states for this bird
            boolean[] states = checkboxStates.get(bird);
            if (states == null) {
                states = new boolean[5];
                checkboxStates.put(bird, states);
            }
            
            // Make states effectively final for lambda expressions
            final boolean[] finalStates = states;
            
            // Set checkbox states
            checkFemale.setChecked(finalStates[0]);
            checkMale.setChecked(finalStates[1]);
            checkSaw.setChecked(finalStates[2]);
            checkPhotographed.setChecked(finalStates[3]);
            checkHeard.setChecked(finalStates[4]);
            
            // Set listeners
            checkFemale.setOnCheckedChangeListener((buttonView, isChecked) -> finalStates[0] = isChecked);
            checkMale.setOnCheckedChangeListener((buttonView, isChecked) -> finalStates[1] = isChecked);
            checkSaw.setOnCheckedChangeListener((buttonView, isChecked) -> finalStates[2] = isChecked);
            checkPhotographed.setOnCheckedChangeListener((buttonView, isChecked) -> finalStates[3] = isChecked);
            checkHeard.setOnCheckedChangeListener((buttonView, isChecked) -> finalStates[4] = isChecked);
        }
    }
}