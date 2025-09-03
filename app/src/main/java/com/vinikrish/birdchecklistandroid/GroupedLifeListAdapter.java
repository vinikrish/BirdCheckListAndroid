package com.vinikrish.birdchecklistandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vinikrish.birdchecklistandroid.models.Bird;
import com.vinikrish.birdchecklistandroid.utils.CustomDialogUtils;

import java.util.*;

public class GroupedLifeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_COUNTRY_HEADER = 0;
    private static final int TYPE_ALPHABET_HEADER = 1;
    private static final int TYPE_BIRD = 2;
    
    private List<Object> items = new ArrayList<>();
    private List<Bird> allBirds = new ArrayList<>(); // Store all birds separately
    private Map<String, Boolean> expandedCountries = new HashMap<>();
    private Map<String, Boolean> expandedAlphabets = new HashMap<>(); // country_letter format
    private OnBirdRemoveListener removeListener;
    
    public interface OnBirdRemoveListener {
        void onBirdRemove(Bird bird);
    }
    
    public GroupedLifeListAdapter(OnBirdRemoveListener removeListener) {
        this.removeListener = removeListener;
    }
    
    public void setBirds(List<Bird> birds) {
        this.allBirds = new ArrayList<>(birds);
        
        // Initialize expansion maps - group birds and set all sections to collapsed
        expandedCountries.clear();
        expandedAlphabets.clear();
        
        for (Bird bird : allBirds) {
            String country = bird.getCountry() != null ? bird.getCountry() : "Unknown";
            String firstLetter = bird.getComName().substring(0, 1).toUpperCase();
            
            // All countries start collapsed
            expandedCountries.put(country, false);
            
            // All alphabet sections start collapsed
            String alphabetKey = country + "_" + firstLetter;
            expandedAlphabets.put(alphabetKey, false);
        }
        
        rebuildItems();
    }
    
    public void updateBirds(List<Bird> birds) {
        // Store current expansion state
        Map<String, Boolean> currentExpandedCountries = new HashMap<>(expandedCountries);
        Map<String, Boolean> currentExpandedAlphabets = new HashMap<>(expandedAlphabets);
        
        this.allBirds = new ArrayList<>(birds);
        
        // Clear and rebuild expansion maps
        expandedCountries.clear();
        expandedAlphabets.clear();
        
        for (Bird bird : allBirds) {
            String country = bird.getCountry() != null ? bird.getCountry() : "Unknown";
            String firstLetter = bird.getComName().substring(0, 1).toUpperCase();
            String alphabetKey = country + "_" + firstLetter;
            
            // Restore previous expansion state if it existed, otherwise default to collapsed
            expandedCountries.put(country, currentExpandedCountries.getOrDefault(country, false));
            expandedAlphabets.put(alphabetKey, currentExpandedAlphabets.getOrDefault(alphabetKey, false));
        }
        
        rebuildItems();
    }
    
    private void rebuildItems() {
        // Group birds by country, then by first letter, then by common name
        Map<String, Map<String, Map<String, CombinedBird>>> countryGroupedBirds = new TreeMap<>();
        
        for (Bird bird : allBirds) {
            String country = bird.getCountry() != null ? bird.getCountry() : "Unknown";
            String firstLetter = bird.getComName().substring(0, 1).toUpperCase();
            String birdName = bird.getComName();
            
            CombinedBird combinedBird = countryGroupedBirds
                .computeIfAbsent(country, k -> new TreeMap<>())
                .computeIfAbsent(firstLetter, k -> new TreeMap<>())
                .computeIfAbsent(birdName, k -> new CombinedBird(birdName, country));
            
            // Add this bird's data to the combined bird
            if ("M".equals(bird.getGender())) {
                combinedBird.setMaleData(bird);
            } else if ("F".equals(bird.getGender())) {
                combinedBird.setFemaleData(bird);
            }
        }
        
        items.clear();
        
        // Add country headers, alphabet sub-headers, and combined birds based on expansion state
        for (Map.Entry<String, Map<String, Map<String, CombinedBird>>> countryEntry : countryGroupedBirds.entrySet()) {
            String country = countryEntry.getKey();
            Map<String, Map<String, CombinedBird>> alphabetGroups = countryEntry.getValue();
            
            // Count total unique birds in this country
            int totalBirds = alphabetGroups.values().stream()
                .mapToInt(birdMap -> birdMap.size()).sum();
            
            // Add country header
            items.add(new CountryHeader(country, totalBirds));
            
            // Add alphabet groups if country is expanded
            if (expandedCountries.getOrDefault(country, false)) {
                for (Map.Entry<String, Map<String, CombinedBird>> alphabetEntry : alphabetGroups.entrySet()) {
                    String letter = alphabetEntry.getKey();
                    Map<String, CombinedBird> birdMap = alphabetEntry.getValue();
                    
                    // Add alphabet header
                    items.add(new AlphabetHeader(country, letter, birdMap.size()));
                    
                    // Add combined birds if alphabet section is expanded
                    String alphabetKey = country + "_" + letter;
                    if (expandedAlphabets.getOrDefault(alphabetKey, false)) {
                        List<CombinedBird> sortedBirds = new ArrayList<>(birdMap.values());
                        sortedBirds.sort((b1, b2) -> b1.getComName().compareToIgnoreCase(b2.getComName()));
                        items.addAll(sortedBirds);
                    }
                }
            }
        }
        
        notifyDataSetChanged();
    }
    
    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof CountryHeader) {
            return TYPE_COUNTRY_HEADER;
        } else if (item instanceof AlphabetHeader) {
            return TYPE_ALPHABET_HEADER;
        } else {
            return TYPE_BIRD;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_COUNTRY_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group_header, parent, false);
            return new CountryHeaderViewHolder(view);
        } else if (viewType == TYPE_ALPHABET_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group_header, parent, false);
            return new AlphabetHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_life_list_bird_dual_row, parent, false);
            return new BirdViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        
        if (holder instanceof CountryHeaderViewHolder) {
            CountryHeader header = (CountryHeader) item;
            ((CountryHeaderViewHolder) holder).bind(header);
        } else if (holder instanceof AlphabetHeaderViewHolder) {
            AlphabetHeader header = (AlphabetHeader) item;
            ((AlphabetHeaderViewHolder) holder).bind(header);
        } else if (holder instanceof BirdViewHolder) {
            CombinedBird combinedBird = (CombinedBird) item;
            ((BirdViewHolder) holder).bind(combinedBird);
        }
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    private void toggleCountry(String country) {
        boolean isExpanded = expandedCountries.getOrDefault(country, false);
        expandedCountries.put(country, !isExpanded);
        rebuildItems();
    }
    
    private void toggleAlphabet(String country, String letter) {
        String alphabetKey = country + "_" + letter;
        boolean isExpanded = expandedAlphabets.getOrDefault(alphabetKey, false);
        expandedAlphabets.put(alphabetKey, !isExpanded);
        rebuildItems();
    }
    
    // ViewHolder for country headers
    class CountryHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;
        TextView expandIcon;
        
        CountryHeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.header_text);
            expandIcon = itemView.findViewById(R.id.expand_icon);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CountryHeader header = (CountryHeader) items.get(position);
                    toggleCountry(header.getCountry());
                }
            });
        }
        
        void bind(CountryHeader header) {
            headerText.setText(header.getCountry() + " (" + header.getCount() + " birds)");
            boolean isExpanded = expandedCountries.getOrDefault(header.getCountry(), false);
            expandIcon.setText(isExpanded ? "▼" : "▶");
        }
    }
    
    // ViewHolder for alphabet headers
    class AlphabetHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;
        TextView expandIcon;
        
        AlphabetHeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.header_text);
            expandIcon = itemView.findViewById(R.id.expand_icon);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    AlphabetHeader header = (AlphabetHeader) items.get(position);
                    toggleAlphabet(header.getCountry(), header.getLetter());
                }
            });
        }
        
        void bind(AlphabetHeader header) {
            headerText.setText("    " + header.getLetter() + " (" + header.getCount() + " birds)");
            String alphabetKey = header.getCountry() + "_" + header.getLetter();
            boolean isExpanded = expandedAlphabets.getOrDefault(alphabetKey, false);
            expandIcon.setText(isExpanded ? "▼" : "▶");
        }
    }
    
    class BirdViewHolder extends RecyclerView.ViewHolder {
        private TextView birdNameText;
        private View maleRow;
        private View femaleRow;
        private TextView countryTextMale;
        private TextView countryTextFemale;
        private ImageView sawIconMale;
        private ImageView photographedIconMale;
        private ImageView heardIconMale;
        private ImageView sawIconFemale;
        private ImageView photographedIconFemale;
        private ImageView heardIconFemale;
        private TextView removeButtonMale;
        private TextView removeButtonFemale;

        public BirdViewHolder(@NonNull View itemView) {
            super(itemView);
            birdNameText = itemView.findViewById(R.id.birdNameText);
            maleRow = itemView.findViewById(R.id.maleRow);
            femaleRow = itemView.findViewById(R.id.femaleRow);
            countryTextMale = itemView.findViewById(R.id.countryTextMale);
            countryTextFemale = itemView.findViewById(R.id.countryTextFemale);
            sawIconMale = itemView.findViewById(R.id.sawIconMale);
            photographedIconMale = itemView.findViewById(R.id.photographedIconMale);
            heardIconMale = itemView.findViewById(R.id.heardIconMale);
            sawIconFemale = itemView.findViewById(R.id.sawIconFemale);
            photographedIconFemale = itemView.findViewById(R.id.photographedIconFemale);
            heardIconFemale = itemView.findViewById(R.id.heardIconFemale);
            removeButtonMale = itemView.findViewById(R.id.removeButtonMale);
            removeButtonFemale = itemView.findViewById(R.id.removeButtonFemale);
        }
        
        public void bind(CombinedBird combinedBird) {
            birdNameText.setText(combinedBird.getComName());
            
            // Show/hide male row based on male data
            Bird maleData = combinedBird.getMaleData();
            if (maleData != null) {
                maleRow.setVisibility(View.VISIBLE);
                countryTextMale.setText(combinedBird.getCountry());
                
                // Show male observation icons
                sawIconMale.setVisibility(maleData.isSaw() ? View.VISIBLE : View.GONE);
                photographedIconMale.setVisibility(maleData.isPhotographed() ? View.VISIBLE : View.GONE);
                heardIconMale.setVisibility(maleData.isHeard() ? View.VISIBLE : View.GONE);
                
                // Set click listeners for male observation icons
                sawIconMale.setOnClickListener(v -> showIconTooltip(v.getContext(), "Saw Male", "Indicates that a male bird was visually observed"));
                photographedIconMale.setOnClickListener(v -> showIconTooltip(v.getContext(), "Photographed Male", "Indicates that a male bird was photographed"));
                heardIconMale.setOnClickListener(v -> showIconTooltip(v.getContext(), "Heard Male", "Indicates that a male bird was heard"));
                
                // Set remove button click listener for male
                removeButtonMale.setOnClickListener(v -> {
                    Context context = v.getContext();
                    new AlertDialog.Builder(context)
                        .setTitle("Remove Bird")
                        .setMessage("Are you sure you want to remove the male \"" + combinedBird.getComName() + "\" from your life list?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            if (removeListener != null) {
                                removeListener.onBirdRemove(maleData);
                                CustomDialogUtils.showSuccessDialog(context, "Success", "Male bird removed successfully!");
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                });
            } else {
                maleRow.setVisibility(View.GONE);
            }
            
            // Show/hide female row based on female data
            Bird femaleData = combinedBird.getFemaleData();
            if (femaleData != null) {
                femaleRow.setVisibility(View.VISIBLE);
                countryTextFemale.setText(combinedBird.getCountry());
                
                // Show female observation icons
                sawIconFemale.setVisibility(femaleData.isSaw() ? View.VISIBLE : View.GONE);
                photographedIconFemale.setVisibility(femaleData.isPhotographed() ? View.VISIBLE : View.GONE);
                heardIconFemale.setVisibility(femaleData.isHeard() ? View.VISIBLE : View.GONE);
                
                // Set click listeners for female observation icons
                sawIconFemale.setOnClickListener(v -> showIconTooltip(v.getContext(), "Saw Female", "Indicates that a female bird was visually observed"));
                photographedIconFemale.setOnClickListener(v -> showIconTooltip(v.getContext(), "Photographed Female", "Indicates that a female bird was photographed"));
                heardIconFemale.setOnClickListener(v -> showIconTooltip(v.getContext(), "Heard Female", "Indicates that a female bird was heard"));
                
                // Set remove button click listener for female
                removeButtonFemale.setOnClickListener(v -> {
                    Context context = v.getContext();
                    new AlertDialog.Builder(context)
                        .setTitle("Remove Bird")
                        .setMessage("Are you sure you want to remove the female \"" + combinedBird.getComName() + "\" from your life list?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            if (removeListener != null) {
                                removeListener.onBirdRemove(femaleData);
                                CustomDialogUtils.showSuccessDialog(context, "Success", "Female bird removed successfully!");
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                });
            } else {
                femaleRow.setVisibility(View.GONE);
            }
        }
        
        private void showIconTooltip(Context context, String title, String description) {
            new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton("OK", null)
                .show();
        }
    }
    
    // Header classes for grouping
    public static class CountryHeader {
        private String country;
        private int count;
        
        public CountryHeader(String country, int count) {
            this.country = country;
            this.count = count;
        }
        
        public String getCountry() { return country; }
        public int getCount() { return count; }
    }
    
    public static class AlphabetHeader {
        private String country;
        private String letter;
        private int count;
        
        public AlphabetHeader(String country, String letter, int count) {
            this.country = country;
            this.letter = letter;
            this.count = count;
        }
        
        public String getCountry() { return country; }
        public String getLetter() { return letter; }
        public int getCount() { return count; }
    }
    
    // Combined bird class to hold both male and female data for the same bird species
    public static class CombinedBird {
        private String comName;
        private String country;
        private Bird maleData;
        private Bird femaleData;
        
        public CombinedBird(String comName, String country) {
            this.comName = comName;
            this.country = country;
        }
        
        public String getComName() { return comName; }
        public String getCountry() { return country; }
        
        public Bird getMaleData() { return maleData; }
        public void setMaleData(Bird maleData) { this.maleData = maleData; }
        
        public Bird getFemaleData() { return femaleData; }
        public void setFemaleData(Bird femaleData) { this.femaleData = femaleData; }
        
        public boolean hasMale() { return maleData != null; }
        public boolean hasFemale() { return femaleData != null; }
    }
}