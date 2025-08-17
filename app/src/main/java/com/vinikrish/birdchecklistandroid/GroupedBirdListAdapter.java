package com.vinikrish.birdchecklistandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vinikrish.birdchecklistandroid.models.Bird;
import com.vinikrish.birdchecklistandroid.models.BirdGroup;
import java.util.ArrayList;
import java.util.List;

public class GroupedBirdListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_BIRD = 1;
    
    private List<BirdGroup> birdGroups;
    private List<Object> displayItems; // Mixed list of headers and birds
    
    public GroupedBirdListAdapter(List<BirdGroup> birdGroups) {
        this.birdGroups = birdGroups != null ? birdGroups : new ArrayList<>();
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
                    .inflate(R.layout.item_bird_list, parent, false);
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
    
    // Bird ViewHolder for life list (no checkboxes)
    static class BirdViewHolder extends RecyclerView.ViewHolder {
        TextView birdName, birdScientificName, birdDetails;
        
        public BirdViewHolder(@NonNull View itemView) {
            super(itemView);
            birdName = itemView.findViewById(R.id.birdName);
            birdScientificName = itemView.findViewById(R.id.birdScientificName);
            birdDetails = itemView.findViewById(R.id.birdDetails);
        }
        
        public void bind(Bird bird) {
            birdName.setText(bird.getComName());
            birdScientificName.setText(bird.getSciName());
            
            // Build observation details
            StringBuilder details = new StringBuilder();
            if (bird.isFemale()) details.append("Female ");
            if (bird.isMale()) details.append("Male ");
            if (bird.isSaw()) details.append("Saw ");
            if (bird.isPhotographed()) details.append("Photographed ");
            if (bird.isHeard()) details.append("Heard");
            
            birdDetails.setText(details.toString().trim());
        }
    }
}