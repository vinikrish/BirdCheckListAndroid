package com.vinikrish.birdchecklistandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vinikrish.birdchecklistandroid.models.Bird;

import java.util.List;

public class LifeListAdapter extends RecyclerView.Adapter<LifeListAdapter.BirdViewHolder> {
    private List<Bird> birds;
    
    public LifeListAdapter(List<Bird> birds) {
        this.birds = birds;
    }
    
    @NonNull
    @Override
    public BirdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_life_list_bird, parent, false);
        return new BirdViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BirdViewHolder holder, int position) {
        Bird bird = birds.get(position);
        holder.bind(bird);
    }
    
    @Override
    public int getItemCount() {
        return birds.size();
    }
    
    static class BirdViewHolder extends RecyclerView.ViewHolder {
        private TextView birdNameText;
        private TextView countryText;
        private TextView genderText;
        private TextView observationsText;
        
        public BirdViewHolder(@NonNull View itemView) {
            super(itemView);
            birdNameText = itemView.findViewById(R.id.birdNameText);
            countryText = itemView.findViewById(R.id.countryText);
            genderText = itemView.findViewById(R.id.genderText);
            observationsText = itemView.findViewById(R.id.observationsText);
        }
        
        public void bind(Bird bird) {
            birdNameText.setText(bird.getComName());
            countryText.setText(bird.getCountry());
            
            // Display gender
            String gender = bird.getGender();
            if ("M".equals(gender)) {
                genderText.setText("Male");
            } else if ("F".equals(gender)) {
                genderText.setText("Female");
            } else {
                genderText.setText("Unknown");
            }
            
            // Build observations string
            StringBuilder observations = new StringBuilder();
            if (bird.isSaw()) {
                observations.append("Saw");
            }
            if (bird.isPhotographed()) {
                if (observations.length() > 0) observations.append(", ");
                observations.append("Photographed");
            }
            if (bird.isHeard()) {
                if (observations.length() > 0) observations.append(", ");
                observations.append("Heard");
            }
            
            if (observations.length() == 0) {
                observations.append("No observations");
            }
            
            observationsText.setText(observations.toString());
        }
    }
}