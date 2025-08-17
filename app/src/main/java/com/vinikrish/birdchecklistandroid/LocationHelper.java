package com.vinikrish.birdchecklistandroid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {
    private static final String TAG = "LocationHelper";
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1000; // 1000 meters
    
    private Context context;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    
    private Location location;
    private double latitude;
    private double longitude;
    
    public interface LocationCallback {
        void onLocationDetected(String countryCode);
        void onLocationError(String error);
    }
    
    public LocationHelper(Context context) {
        this.context = context;
        getLocation();
    }
    
    public Location getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            
            // Getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            
            // Getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            
            if (!isGPSEnabled && !isNetworkEnabled) {
                // No network provider is enabled
                Log.d(TAG, "No location provider enabled");
            } else {
                this.canGetLocation = true;
                
                // Check for location permissions
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Location permissions not granted");
                    return null;
                }
                
                // First, get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    // Handle location updates if needed
                                }
                                
                                @Override
                                public void onStatusChanged(String provider, int status, Bundle extras) {}
                                
                                @Override
                                public void onProviderEnabled(String provider) {}
                                
                                @Override
                                public void onProviderDisabled(String provider) {}
                            });
                    
                    Log.d(TAG, "Network location enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                
                // If GPS enabled, get location using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, new LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location location) {
                                        // Handle location updates if needed
                                    }
                                    
                                    @Override
                                    public void onStatusChanged(String provider, int status, Bundle extras) {}
                                    
                                    @Override
                                    public void onProviderEnabled(String provider) {}
                                    
                                    @Override
                                    public void onProviderDisabled(String provider) {}
                                });
                        
                        Log.d(TAG, "GPS location enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting location: " + e.getMessage());
            e.printStackTrace();
        }
        
        return location;
    }
    
    public void getCurrentCountry(LocationCallback callback) {
        if (location == null) {
            getLocation();
        }
        
        if (location != null && Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String countryCode = address.getCountryCode();
                    Log.d(TAG, "Country detected: " + countryCode);
                    callback.onLocationDetected(countryCode);
                } else {
                    Log.d(TAG, "No address found for location");
                    callback.onLocationError("Unable to determine country from location");
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoder error: " + e.getMessage());
                callback.onLocationError("Geocoder service unavailable");
            }
        } else {
            Log.d(TAG, "Location not available or Geocoder not present");
            callback.onLocationError("Location not available");
        }
    }
    
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
    
    public void stopUsingGPS() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {}
                    
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}
                    
                    @Override
                    public void onProviderEnabled(String provider) {}
                    
                    @Override
                    public void onProviderDisabled(String provider) {}
                });
            }
        }
    }
    
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }
    
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }
}