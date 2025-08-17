package com.vinikrish.birdchecklistandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vinikrish.birdchecklistandroid.models.Bird;

import java.util.ArrayList;
import java.util.List;

public class BirdListAdapter extends RecyclerView.Adapter<BirdListAdapter.BirdViewHolder> {

    private List<Bird> birds;

    public BirdListAdapter() {
        this.birds = new ArrayList<>();
    }

    @NonNull
    @Override
    public BirdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bird_list, parent, false);
        return new BirdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BirdViewHolder holder, int position) {
        Bird bird = birds.get(position);
        holder.birdName.setText(bird.getComName());
        holder.birdScientificName.setText(bird.getSciName());
        
        // Build observation details
        StringBuilder details = new StringBuilder();
        if (bird.isFemale()) details.append("Female ");
        if (bird.isMale()) details.append("Male ");
        if (bird.isSaw()) details.append("Saw ");
        if (bird.isPhotographed()) details.append("Photographed ");
        if (bird.isHeard()) details.append("Heard");
        
        holder.birdDetails.setText(details.toString().trim());
    }

    @Override
    public int getItemCount() {
        return birds.size();
    }

    public void updateBirds(List<Bird> newBirds) {
        this.birds = newBirds;
        notifyDataSetChanged();
    }

    public void addBird(Bird bird) {
        if (!birds.contains(bird)) {
            birds.add(bird);
            notifyItemInserted(birds.size() - 1);
        }
    }

    static class BirdViewHolder extends RecyclerView.ViewHolder {
        TextView birdName, birdScientificName, birdDetails;

        public BirdViewHolder(@NonNull View itemView) {
            super(itemView);
            birdName = itemView.findViewById(R.id.birdName);
            birdScientificName = itemView.findViewById(R.id.birdScientificName);
            birdDetails = itemView.findViewById(R.id.birdDetails);
        }
    }
}