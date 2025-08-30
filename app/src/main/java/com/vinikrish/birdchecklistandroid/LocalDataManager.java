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
        if (isDataLoaded) return;
        
        try {
            InputStream inputStream = context.getAssets().open("birds_master_list.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            
            String jsonString = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(jsonString);
            
            Set<String> countrySet = new HashSet<>();
            
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject birdObject = jsonObject.getJSONObject(key);
                
                String comName = birdObject.getString("comName");
                String sciName = birdObject.getString("sciName");
                String country = birdObject.getString("country");
                
                MasterBird masterBird = new MasterBird();
                masterBird.setComName(comName);
                masterBird.setSciName(sciName);
                masterBird.setCountry(country);
                
                if (!birdsByCountry.containsKey(country)) {
                    birdsByCountry.put(country, new ArrayList<>());
                }
                birdsByCountry.get(country).add(masterBird);
                countrySet.add(country);
            }
            
            countries = new ArrayList<>(countrySet);
            Collections.sort(countries);
            
            // Sort birds within each country
            for (List<MasterBird> birds : birdsByCountry.values()) {
                Collections.sort(birds, (b1, b2) -> b1.getComName().compareToIgnoreCase(b2.getComName()));
            }
            
            isDataLoaded = true;
            
        } catch (IOException | JSONException e) {
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
                List<MasterBird> birds = birdsByCountry.get(country);
                if (birds == null) {
                    birds = new ArrayList<>();
                }
                
                // Create final reference for lambda
                final List<MasterBird> finalBirds = birds;
                
                // Post result on main thread
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onSuccess(new ArrayList<>(finalBirds)));
                
            } catch (Exception e) {
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