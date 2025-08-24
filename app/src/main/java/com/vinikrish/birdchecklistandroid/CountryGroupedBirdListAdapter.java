package com.vinikrish.birdchecklistandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vinikrish.birdchecklistandroid.models.Bird;
import com.vinikrish.birdchecklistandroid.models.BirdGroup;
import com.vinikrish.birdchecklistandroid.models.CountryGroup;

import java.util.ArrayList;
import java.util.List;

public class CountryGroupedBirdListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_COUNTRY_HEADER = 0;
    private static final int VIEW_TYPE_LETTER_HEADER = 1;
    private static final int VIEW_TYPE_BIRD = 2;
    
    private List<CountryGroup> countryGroups;
    private List<Object> flattenedItems; // Mixed list of headers and birds
    private OnBirdRemoveListener onBirdRemoveListener;
    
    public interface OnBirdRemoveListener {
        void onBirdRemove(Bird bird);
    }
    
    public CountryGroupedBirdListAdapter(List<CountryGroup> countryGroups) {
        this.countryGroups = countryGroups != null ? countryGroups : new ArrayList<>();
        this.flattenedItems = new ArrayList<>();
        flattenList();
    }
    
    public void setOnBirdRemoveListener(OnBirdRemoveListener listener) {
        this.onBirdRemoveListener = listener;
    }
    
    public void updateCountryGroups(List<CountryGroup> newCountryGroups) {
        this.countryGroups = newCountryGroups != null ? newCountryGroups : new ArrayList<>();
        flattenList();
        notifyDataSetChanged();
    }
    
    private void flattenList() {
        flattenedItems.clear();
        
        for (CountryGroup countryGroup : countryGroups) {
            // Add country header
            flattenedItems.add(countryGroup);
            
            // If country is expanded, add its bird groups and birds
            if (countryGroup.isExpanded()) {
                for (BirdGroup birdGroup : countryGroup.getBirdGroups()) {
                    // Add letter header
                    flattenedItems.add(birdGroup);
                    
                    // If letter group is expanded, add its birds
                    if (birdGroup.isExpanded()) {
                        flattenedItems.addAll(birdGroup.getBirds());
                    }
                }
            }
        }
    }
    
    @Override
    public int getItemViewType(int position) {
        Object item = flattenedItems.get(position);
        if (item instanceof CountryGroup) {
            return VIEW_TYPE_COUNTRY_HEADER;
        } else if (item instanceof BirdGroup) {
            return VIEW_TYPE_LETTER_HEADER;
        } else {
            return VIEW_TYPE_BIRD;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        switch (viewType) {
            case VIEW_TYPE_COUNTRY_HEADER:
                View countryView = inflater.inflate(R.layout.item_country_header, parent, false);
                return new CountryHeaderViewHolder(countryView);
            case VIEW_TYPE_LETTER_HEADER:
                View letterView = inflater.inflate(R.layout.item_letter_header, parent, false);
                return new LetterHeaderViewHolder(letterView);
            case VIEW_TYPE_BIRD:
            default:
                View birdView = inflater.inflate(R.layout.item_bird_list, parent, false);
                return new BirdViewHolder(birdView);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = flattenedItems.get(position);
        
        if (holder instanceof CountryHeaderViewHolder && item instanceof CountryGroup) {
            CountryHeaderViewHolder countryHolder = (CountryHeaderViewHolder) holder;
            CountryGroup countryGroup = (CountryGroup) item;
            
            countryHolder.countryName.setText(countryGroup.getCountryName() + " (" + countryGroup.getTotalBirdCount() + ")");
            countryHolder.expandIcon.setRotation(countryGroup.isExpanded() ? 90 : 0);
            
            countryHolder.itemView.setOnClickListener(v -> {
                countryGroup.setExpanded(!countryGroup.isExpanded());
                flattenList();
                notifyDataSetChanged();
            });
            
        } else if (holder instanceof LetterHeaderViewHolder && item instanceof BirdGroup) {
            LetterHeaderViewHolder letterHolder = (LetterHeaderViewHolder) holder;
            BirdGroup birdGroup = (BirdGroup) item;
            
            letterHolder.letterText.setText(birdGroup.getLetter() + " (" + birdGroup.getBirdCount() + ")");
            letterHolder.expandIcon.setRotation(birdGroup.isExpanded() ? 90 : 0);
            
            letterHolder.itemView.setOnClickListener(v -> {
                birdGroup.setExpanded(!birdGroup.isExpanded());
                flattenList();
                notifyDataSetChanged();
            });
            
        } else if (holder instanceof BirdViewHolder && item instanceof Bird) {
            BirdViewHolder birdHolder = (BirdViewHolder) holder;
            Bird bird = (Bird) item;
            
            birdHolder.birdName.setText(bird.getComName());
            birdHolder.birdScientificName.setText(bird.getSciName());
            
            // Set observation icons
            birdHolder.iconFemale.setVisibility(bird.isFemale() ? View.VISIBLE : View.GONE);
            birdHolder.iconMale.setVisibility(bird.isMale() ? View.VISIBLE : View.GONE);
            birdHolder.iconSaw.setVisibility(bird.isSaw() ? View.VISIBLE : View.GONE);
            birdHolder.iconPhotographed.setVisibility(bird.isPhotographed() ? View.VISIBLE : View.GONE);
            birdHolder.iconHeard.setVisibility(bird.isHeard() ? View.VISIBLE : View.GONE);
            
            // Set remove button click listener
            birdHolder.removeButton.setOnClickListener(v -> {
                if (onBirdRemoveListener != null) {
                    onBirdRemoveListener.onBirdRemove(bird);
                }
            });
        }
    }
    
    @Override
    public int getItemCount() {
        return flattenedItems.size();
    }
    
    // Country Header ViewHolder
    static class CountryHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView countryName;
        ImageView expandIcon;
        
        public CountryHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            countryName = itemView.findViewById(R.id.countryName);
            expandIcon = itemView.findViewById(R.id.expandIcon);
        }
    }
    
    // Letter Header ViewHolder
    static class LetterHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView letterText;
        ImageView expandIcon;
        
        public LetterHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            letterText = itemView.findViewById(R.id.letterText);
            expandIcon = itemView.findViewById(R.id.expandIcon);
        }
    }
    
    // Bird ViewHolder
    static class BirdViewHolder extends RecyclerView.ViewHolder {
        TextView birdName, birdScientificName;
        ImageView iconFemale, iconMale, iconSaw, iconPhotographed, iconHeard;
        Button removeButton;
        
        public BirdViewHolder(@NonNull View itemView) {
            super(itemView);
            birdName = itemView.findViewById(R.id.birdName);
            birdScientificName = itemView.findViewById(R.id.birdScientificName);
            iconFemale = itemView.findViewById(R.id.iconFemale);
            iconMale = itemView.findViewById(R.id.iconMale);
            iconSaw = itemView.findViewById(R.id.iconSaw);
            iconPhotographed = itemView.findViewById(R.id.iconPhotographed);
            iconHeard = itemView.findViewById(R.id.iconHeard);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}