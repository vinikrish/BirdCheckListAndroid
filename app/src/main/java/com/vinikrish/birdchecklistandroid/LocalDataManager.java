package com.vinikrish.birdchecklistandroid;

import android.content.Context;
import com.vinikrish.birdchecklistandroid.models.MasterBird;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalDataManager {
    private static LocalDataManager instance;
    private final Context context;
    private final ExecutorService executor;
    private Map<String, List<MasterBird>> birdsByCountry;
    private List<String> countries;
    private boolean isDataLoaded = false;
    
    public interface MasterBirdListCallback {
        void onSuccess(List<MasterBird> masterBirds);
        void onError(String error);
    }
    
    public interface CountriesCallback {
        void onSuccess(List<String> countries);
        void onError(String error);
    }
    
    private LocalDataManager(Context context) {
        this.context = context.getApplicationContext();
        this.executor = Executors.newSingleThreadExecutor();
        this.birdsByCountry = new HashMap<>();
        this.countries = new ArrayList<>();
    }
    
    public static synchronized LocalDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocalDataManager(context);
        }
        return instance;
    }
    
    private void loadDataFromAssets() {
        if (isDataLoaded) {
            android.util.Log.d("LocalDataManager", "Data already loaded, skipping load");
            return;
        }
        
        android.util.Log.d("LocalDataManager", "Starting to load data from assets");
        
        try {
            InputStream inputStream = context.getAssets().open("birds_master_list.json");
            int size = inputStream.available();
            android.util.Log.d("LocalDataManager", "JSON file size: " + size + " bytes");
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            
            String jsonString = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(jsonString);
            android.util.Log.d("LocalDataManager", "JSON parsed, total keys: " + jsonObject.length());
            
            Set<String> countrySet = new HashSet<>();
            int birdCount = 0;
            
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject birdObject = jsonObject.getJSONObject(key);
                
                String comName = birdObject.getString("comName");
                String sciName = birdObject.getString("sciName");
                String country = birdObject.getString("country");
                
                birdCount++;
                if (birdCount <= 10 || birdCount % 100 == 0) {
                    android.util.Log.d("LocalDataManager", "Loaded bird: " + comName + ", Country: " + country + ", Total so far: " + birdCount);
                }
                
                MasterBird masterBird = new MasterBird();
                masterBird.setComName(comName);
                masterBird.setSciName(sciName);
                masterBird.setCountry(country);
                
                if (!birdsByCountry.containsKey(country)) {
                    birdsByCountry.put(country, new ArrayList<>());
                    android.util.Log.d("LocalDataManager", "Created new country entry: " + country);
                }
                birdsByCountry.get(country).add(masterBird);
                countrySet.add(country);
            }
            
            // Log the number of birds per country
            for (Map.Entry<String, List<MasterBird>> entry : birdsByCountry.entrySet()) {
                android.util.Log.d("LocalDataManager", "Country: " + entry.getKey() + ", Birds: " + entry.getValue().size());
            }
            
            countries = new ArrayList<>(countrySet);
            Collections.sort(countries);
            
            android.util.Log.d("LocalDataManager", "Data loaded successfully. Total birds: " + birdCount + ", Countries: " + countries.size());
            for (String country : countries) {
                android.util.Log.d("LocalDataManager", "Country: " + country + ", Birds: " + birdsByCountry.get(country).size());
            }
            
            // Sort birds within each country
            for (List<MasterBird> birds : birdsByCountry.values()) {
                Collections.sort(birds, (b1, b2) -> b1.getComName().compareToIgnoreCase(b2.getComName()));
            }
            
            isDataLoaded = true;
            
        } catch (IOException | JSONException e) {
            android.util.Log.e("LocalDataManager", "Error loading data from assets", e);
            e.printStackTrace();
        }
    }
    
    public void fetchMasterBirdList(MasterBirdListCallback callback) {
        executor.execute(() -> {
            try {
                loadDataFromAssets();
                List<MasterBird> allBirds = new ArrayList<>();
                for (List<MasterBird> birds : birdsByCountry.values()) {
                    allBirds.addAll(birds);
                }
                
                // Post result on main thread
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onSuccess(allBirds));
                
            } catch (Exception e) {
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    public void fetchMasterBirdListByCountry(String country, MasterBirdListCallback callback) {
        executor.execute(() -> {
            try {
                loadDataFromAssets();
                android.util.Log.d("LocalDataManager", "Fetching birds for country: " + country);
                
                // Check if the country exists in our map
                if (!birdsByCountry.containsKey(country)) {
                    android.util.Log.w("LocalDataManager", "Country not found in map: " + country);
                    android.util.Log.d("LocalDataManager", "Available countries: " + countries);
                }
                
                List<MasterBird> birds = birdsByCountry.get(country);
                if (birds == null) {
                    android.util.Log.w("LocalDataManager", "No birds found for country: " + country);
                    birds = new ArrayList<>();
                } else {
                    android.util.Log.d("LocalDataManager", "Found " + birds.size() + " birds for country: " + country);
                    if (!birds.isEmpty()) {
                        android.util.Log.d("LocalDataManager", "Sample birds: " + 
                            birds.get(0).getComName() + ", " + 
                            (birds.size() > 1 ? birds.get(1).getComName() : ""));
                    }
                }
                
                // Create final reference for lambda
                final List<MasterBird> finalBirds = birds;
                
                // Post result on main thread
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onSuccess(new ArrayList<>(finalBirds)));
                
            } catch (Exception e) {
                android.util.Log.e("LocalDataManager", "Error fetching birds for country: " + country, e);
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    public void fetchCountries(CountriesCallback callback) {
        executor.execute(() -> {
            try {
                loadDataFromAssets();
                
                // Post result on main thread
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onSuccess(new ArrayList<>(countries)));
                
            } catch (Exception e) {
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    public List<String> getCountriesSync() {
        loadDataFromAssets();
        return new ArrayList<>(countries);
    }
    
    public List<MasterBird> getBirdsByCountrySync(String country) {
        loadDataFromAssets();
        List<MasterBird> birds = birdsByCountry.get(country);
        return birds != null ? new ArrayList<>(birds) : new ArrayList<>();
    }
}