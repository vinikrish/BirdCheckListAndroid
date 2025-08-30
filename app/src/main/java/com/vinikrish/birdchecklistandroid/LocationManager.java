package com.vinikrish.birdchecklistandroid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationManager {
    private static LocationManager instance;
    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private final ExecutorService executor;
    private String detectedCountry;
    
    // Map of country codes to country names used in the bird database
    private static final Map<String, String> COUNTRY_CODE_MAP = new HashMap<String, String>() {{
        put("US", "USA");
        put("IN", "India");
        put("AU", "Australia");
        put("CA", "Canada");
        put("GB", "United Kingdom");
        put("UK", "United Kingdom");
        put("DE", "Germany");
        put("FR", "France");
        put("IT", "Italy");
        put("ES", "Spain");
        put("JP", "Japan");
        put("CN", "China");
        put("BR", "Brazil");
        put("MX", "Mexico");
        put("AR", "Argentina");
        put("CL", "Chile");
        put("PE", "Peru");
        put("CO", "Colombia");
        put("VE", "Venezuela");
        put("EC", "Ecuador");
        put("BO", "Bolivia");
        put("UY", "Uruguay");
        put("PY", "Paraguay");
        put("GY", "Guyana");
        put("SR", "Suriname");
        put("GF", "French Guiana");
        put("ZA", "South Africa");
        put("EG", "Egypt");
        put("KE", "Kenya");
        put("TZ", "Tanzania");
        put("UG", "Uganda");
        put("RW", "Rwanda");
        put("ET", "Ethiopia");
        put("GH", "Ghana");
        put("NG", "Nigeria");
        put("MA", "Morocco");
        put("TN", "Tunisia");
        put("DZ", "Algeria");
        put("LY", "Libya");
        put("SD", "Sudan");
        put("SS", "South Sudan");
        put("CF", "Central African Republic");
        put("TD", "Chad");
        put("NE", "Niger");
        put("ML", "Mali");
        put("BF", "Burkina Faso");
        put("CI", "Ivory Coast");
        put("LR", "Liberia");
        put("SL", "Sierra Leone");
        put("GN", "Guinea");
        put("GW", "Guinea-Bissau");
        put("SN", "Senegal");
        put("GM", "Gambia");
        put("MR", "Mauritania");
        put("EH", "Western Sahara");
    }};
    
    public interface LocationCallback {
        void onLocationDetected(String country);
        void onLocationError(String error);
    }
    
    private LocationManager(Context context) {
        this.context = context.getApplicationContext();
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public static synchronized LocationManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocationManager(context);
        }
        return instance;
    }
    
    public void detectLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Location permission not granted");
            return;
        }
        
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        executor.execute(() -> {
                            try {
                                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), 
                                    location.getLongitude(), 
                                    1
                                );
                                
                                if (addresses != null && !addresses.isEmpty()) {
                                    Address address = addresses.get(0);
                                    String countryCode = address.getCountryCode();
                                    String countryName = address.getCountryName();
                                    
                                    // Map country code to our database country names
                                    String mappedCountry = COUNTRY_CODE_MAP.get(countryCode);
                                    if (mappedCountry != null) {
                                        detectedCountry = mappedCountry;
                                    } else {
                                        detectedCountry = countryName;
                                    }
                                    
                                    // Post result on main thread
                                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                                    mainHandler.post(() -> callback.onLocationDetected(detectedCountry));
                                } else {
                                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                                    mainHandler.post(() -> callback.onLocationError("Unable to determine country from location"));
                                }
                            } catch (IOException e) {
                                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                                mainHandler.post(() -> callback.onLocationError("Geocoding failed: " + e.getMessage()));
                            }
                        });
                    } else {
                        callback.onLocationError("Unable to get current location");
                    }
                }
            })
            .addOnFailureListener(e -> {
                callback.onLocationError("Failed to get location: " + e.getMessage());
            });
    }
    
    public String getDetectedCountry() {
        return detectedCountry != null ? detectedCountry : "USA"; // Default to USA if no location detected
    }
    
    public void setDetectedCountry(String country) {
        this.detectedCountry = country;
    }
}