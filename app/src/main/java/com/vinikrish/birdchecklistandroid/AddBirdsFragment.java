
package com.vinikrish.birdchecklistandroid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ImageView;
import android.text.TextWatcher;
import android.text.Editable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.vinikrish.birdchecklistandroid.utils.CustomDialogUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.vinikrish.birdchecklistandroid.models.Bird;
import com.vinikrish.birdchecklistandroid.models.BirdGroup;
import com.vinikrish.birdchecklistandroid.models.MasterBird;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

public class AddBirdsFragment extends Fragment {
    
    private static final String TAG = "AddBirdsFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String ARG_USERNAME = "username";
    private static final String ARG_USER_ID = "userId";
    
    // Interface for communicating with parent activity
    public interface OnBirdsSavedListener {
        void onBirdsSaved();
    }
    
    private OnBirdsSavedListener birdsSavedListener;

    private Spinner countrySpinner;
    private EditText searchEditText;
    private RecyclerView birdRecyclerView;
    private GroupedBirdAdapter groupedBirdAdapter;
    private List<Bird> fullBirdList = new ArrayList<>();
    private List<MasterBird> masterBirdList = new ArrayList<>();
    private List<String> countryNames = new ArrayList<>();
    private LocationHelper locationHelper;
    private LocalDataManager localDataManager;
    private LocationManager locationManager;
    private String detectedCountry;
    private ArrayAdapter<String> spinnerAdapter;
    private String username;
    private String userId;
    private Map<String, Bird> existingBirds = new HashMap<>();
    private ValueEventListener existingBirdsListener;
    private boolean isLocationDetectionComplete = false;
    private boolean isSpinnerSetupComplete = false;
    
    // Comprehensive mapping between ISO country codes and country names
    private static final Map<String, String> COUNTRY_CODE_TO_NAME = new HashMap<String, String>() {{
        // North America
        put("US", "USA");
        put("CA", "Canada");
        put("MX", "Mexico");
        put("GT", "Guatemala");
        put("BZ", "Belize");
        put("SV", "El Salvador");
        put("HN", "Honduras");
        put("NI", "Nicaragua");
        put("CR", "Costa Rica");
        put("PA", "Panama");
        
        // South America
        put("BR", "Brazil");
        put("AR", "Argentina");
        put("CL", "Chile");
        put("PE", "Peru");
        put("CO", "Colombia");
        put("VE", "Venezuela");
        put("EC", "Ecuador");
        put("UY", "Uruguay");
        put("PY", "Paraguay");
        put("BO", "Bolivia");
        put("GY", "Guyana");
        put("SR", "Suriname");
        put("GF", "French Guiana");
        
        // Europe
        put("GB", "United Kingdom");
        put("IE", "Ireland");
        put("FR", "France");
        put("ES", "Spain");
        put("PT", "Portugal");
        put("IT", "Italy");
        put("DE", "Germany");
        put("AT", "Austria");
        put("CH", "Switzerland");
        put("NL", "Netherlands");
        put("BE", "Belgium");
        put("LU", "Luxembourg");
        put("DK", "Denmark");
        put("SE", "Sweden");
        put("NO", "Norway");
        put("FI", "Finland");
        put("IS", "Iceland");
        put("PL", "Poland");
        put("CZ", "Czech Republic");
        put("SK", "Slovakia");
        put("HU", "Hungary");
        put("SI", "Slovenia");
        put("HR", "Croatia");
        put("BA", "Bosnia and Herzegovina");
        put("RS", "Serbia");
        put("ME", "Montenegro");
        put("MK", "North Macedonia");
        put("AL", "Albania");
        put("BG", "Bulgaria");
        put("RO", "Romania");
        put("MD", "Moldova");
        put("UA", "Ukraine");
        put("BY", "Belarus");
        put("LT", "Lithuania");
        put("LV", "Latvia");
        put("EE", "Estonia");
        put("RU", "Russia");
        put("GR", "Greece");
        put("CY", "Cyprus");
        put("MT", "Malta");
        
        // Asia
        put("CN", "China");
        put("JP", "Japan");
        put("KR", "South Korea");
        put("KP", "North Korea");
        put("MN", "Mongolia");
        put("IN", "India");
        put("PK", "Pakistan");
        put("BD", "Bangladesh");
        put("LK", "Sri Lanka");
        put("MV", "Maldives");
        put("NP", "Nepal");
        put("BT", "Bhutan");
        put("MM", "Myanmar");
        put("TH", "Thailand");
        put("LA", "Laos");
        put("VN", "Vietnam");
        put("KH", "Cambodia");
        put("MY", "Malaysia");
        put("SG", "Singapore");
        put("BN", "Brunei");
        put("ID", "Indonesia");
        put("TL", "East Timor");
        put("PH", "Philippines");
        put("TW", "Taiwan");
        put("HK", "Hong Kong");
        put("MO", "Macau");
        put("AF", "Afghanistan");
        put("IR", "Iran");
        put("IQ", "Iraq");
        put("SY", "Syria");
        put("LB", "Lebanon");
        put("JO", "Jordan");
        put("IL", "Israel");
        put("PS", "Palestine");
        put("SA", "Saudi Arabia");
        put("YE", "Yemen");
        put("OM", "Oman");
        put("AE", "United Arab Emirates");
        put("QA", "Qatar");
        put("BH", "Bahrain");
        put("KW", "Kuwait");
        put("TR", "Turkey");
        put("AM", "Armenia");
        put("AZ", "Azerbaijan");
        put("GE", "Georgia");
        put("KZ", "Kazakhstan");
        put("KG", "Kyrgyzstan");
        put("TJ", "Tajikistan");
        put("TM", "Turkmenistan");
        put("UZ", "Uzbekistan");
        
        // Africa
        put("EG", "Egypt");
        put("LY", "Libya");
        put("TN", "Tunisia");
        put("DZ", "Algeria");
        put("MA", "Morocco");
        put("EH", "Western Sahara");
        put("MR", "Mauritania");
        put("ML", "Mali");
        put("BF", "Burkina Faso");
        put("NE", "Niger");
        put("TD", "Chad");
        put("SD", "Sudan");
        put("SS", "South Sudan");
        put("ET", "Ethiopia");
        put("ER", "Eritrea");
        put("DJ", "Djibouti");
        put("SO", "Somalia");
        put("KE", "Kenya");
        put("UG", "Uganda");
        put("TZ", "Tanzania");
        put("RW", "Rwanda");
        put("BI", "Burundi");
        put("CD", "Democratic Republic of the Congo");
        put("CG", "Republic of the Congo");
        put("CF", "Central African Republic");
        put("CM", "Cameroon");
        put("GQ", "Equatorial Guinea");
        put("GA", "Gabon");
        put("ST", "Sao Tome and Principe");
        put("NG", "Nigeria");
        put("BJ", "Benin");
        put("TG", "Togo");
        put("GH", "Ghana");
        put("CI", "Ivory Coast");
        put("LR", "Liberia");
        put("SL", "Sierra Leone");
        put("GN", "Guinea");
        put("GW", "Guinea-Bissau");
        put("CV", "Cape Verde");
        put("SN", "Senegal");
        put("GM", "Gambia");
        put("ZA", "South Africa");
        put("NA", "Namibia");
        put("BW", "Botswana");
        put("ZW", "Zimbabwe");
        put("ZM", "Zambia");
        put("MW", "Malawi");
        put("MZ", "Mozambique");
        put("SZ", "Eswatini");
        put("LS", "Lesotho");
        put("MG", "Madagascar");
        put("MU", "Mauritius");
        put("SC", "Seychelles");
        put("KM", "Comoros");
        
        // Oceania
        put("AU", "Australia");
        put("NZ", "New Zealand");
        put("PG", "Papua New Guinea");
        put("FJ", "Fiji");
        put("SB", "Solomon Islands");
        put("VU", "Vanuatu");
        put("NC", "New Caledonia");
        put("PF", "French Polynesia");
        put("WS", "Samoa");
        put("TO", "Tonga");
        put("KI", "Kiribati");
        put("TV", "Tuvalu");
        put("NR", "Nauru");
        put("PW", "Palau");
        put("FM", "Micronesia");
        put("MH", "Marshall Islands");
        
        // Caribbean
        put("CU", "Cuba");
        put("JM", "Jamaica");
        put("HT", "Haiti");
        put("DO", "Dominican Republic");
        put("PR", "Puerto Rico");
        put("TT", "Trinidad and Tobago");
        put("BB", "Barbados");
        put("GD", "Grenada");
        put("VC", "Saint Vincent and the Grenadines");
        put("LC", "Saint Lucia");
        put("DM", "Dominica");
        put("AG", "Antigua and Barbuda");
        put("KN", "Saint Kitts and Nevis");
        put("BS", "Bahamas");
        
        // Antarctica
        put("AQ", "Antarctica");
    }};

    public static AddBirdsFragment newInstance(String username, String userId) {
        AddBirdsFragment fragment = new AddBirdsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnBirdsSavedListener) {
            birdsSavedListener = (OnBirdsSavedListener) context;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            username = getArguments().getString(ARG_USERNAME);
            userId = getArguments().getString(ARG_USER_ID);
        }
        return inflater.inflate(R.layout.fragment_add_birds, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        countrySpinner = view.findViewById(R.id.countrySpinner);
        searchEditText = view.findViewById(R.id.searchEditText);
        birdRecyclerView = view.findViewById(R.id.birdRecyclerView);
        View emptyView = view.findViewById(R.id.emptyView);

        // Explicitly set RecyclerView to VISIBLE and emptyView to GONE initially
        birdRecyclerView.setVisibility(View.VISIBLE);
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
        Log.d(TAG, "Setting RecyclerView to VISIBLE in onViewCreated");

        // Set up RecyclerView with fixed size for better performance
        birdRecyclerView.setHasFixedSize(true);
        birdRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupedBirdAdapter = new GroupedBirdAdapter(new ArrayList<>());
        birdRecyclerView.setAdapter(groupedBirdAdapter);
        
        // Add dividers between items
        birdRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        
        // Force layout refresh
        birdRecyclerView.post(() -> {
            Log.d(TAG, "Forcing layout refresh for RecyclerView");
            birdRecyclerView.invalidate();
            birdRecyclerView.requestLayout();
        });

        // Initialize managers
        localDataManager = LocalDataManager.getInstance(requireContext());
        locationManager = LocationManager.getInstance(requireContext());
        
        // Load data from local JSON instead of Firebase
        loadBirdsFromLocal();
        loadExistingBirdsFromFirebase();
        
        // Initialize location helper
        locationHelper = new LocationHelper(requireContext());
        
        // Set up Save button click listener
        view.findViewById(R.id.saveButton).setOnClickListener(v -> saveSelectedBirds());
        
        // Set up header icon click listeners
        setupHeaderIconClickListeners(view);
        
        // Set up search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (groupedBirdAdapter != null) {
                    groupedBirdAdapter.filter(s.toString());
                }
            }
        });
    }

    private void setupHeaderIconClickListeners(View view) {
        ImageView headerIconSaw = view.findViewById(R.id.headerIconSaw);
        ImageView headerIconPhotographed = view.findViewById(R.id.headerIconPhotographed);
        ImageView headerIconHeard = view.findViewById(R.id.headerIconHeard);

        headerIconSaw.setOnClickListener(v -> showIconDescription("Saw", "Indicates that the bird was visually observed"));
        headerIconPhotographed.setOnClickListener(v -> showIconDescription("Photographed", "Indicates that the bird was photographed"));
        headerIconHeard.setOnClickListener(v -> showIconDescription("Heard", "Indicates that the bird was heard but not necessarily seen"));
    }

    private void showIconDescription(String title, String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title)
               .setMessage(description)
               .setPositiveButton("OK", null)
               .show();
    }

    private void loadBirdsFromLocal() {
        Log.d(TAG, "Loading all countries from local data");
        
        // Load all countries from the master bird list
        localDataManager.fetchCountries(new LocalDataManager.CountriesCallback() {
            @Override
            public void onSuccess(List<String> countries) {
                countryNames = new ArrayList<>(countries);
                Log.d(TAG, "Loaded " + countryNames.size() + " countries: " + countryNames);
                
                setupSpinnerWithCountries();
                
                // Start location detection to auto-select country
                detectCurrentCountry();
                
                // Load birds for the first country as default (will be overridden by location detection)
                if (!countryNames.isEmpty()) {
                    String defaultCountry = countryNames.contains("USA") ? "USA" : countryNames.get(0);
                    loadBirdsForCountry(defaultCountry, true);
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading countries: " + error);
                // Fallback to USA only
                countryNames = new ArrayList<>();
                countryNames.add("USA");
                setupSpinnerWithCountries();
                loadBirdsForCountry("USA", true);
                detectedCountry = "USA";
                isLocationDetectionComplete = true;
            }
        });
    }
    
    private void loadBirdsForCountry(String country, boolean isInitialLoad) {
        Log.d(TAG, "Loading birds for country: " + country + ", isInitialLoad: " + isInitialLoad);
        
        localDataManager.fetchMasterBirdListByCountry(country, new LocalDataManager.MasterBirdListCallback() {
            @Override
            public void onSuccess(List<MasterBird> masterBirds) {
                if (masterBirds == null || masterBirds.isEmpty()) {
                    Log.w(TAG, "No birds found for country: " + country);
                    CustomDialogUtils.showInfoDialog(getContext(), "No Birds Found", "No birds found for " + country);
                    return;
                }
                
                if (isInitialLoad) {
                    Log.d(TAG, "Birds loaded successfully for " + country + ": " + masterBirds.size());
                    
                    // Debug: Print some sample birds
                    int sampleSize = Math.min(5, masterBirds.size());
                    for (int i = 0; i < sampleSize; i++) {
                        MasterBird sampleBird = masterBirds.get(i);
                        Log.d(TAG, "Sample master bird " + i + ": " + sampleBird.getComName() + ", Country: " + sampleBird.getCountry());
                    }
                    
                    masterBirdList = masterBirds;
                    convertMasterBirdsToFullBirdList();
                    
                    if (fullBirdList == null || fullBirdList.isEmpty()) {
                        Log.w(TAG, "Full bird list is empty after conversion");
                        CustomDialogUtils.showErrorDialog(getContext(), "Error", "Error processing birds for " + country);
                        return;
                    }
                    
                    showBirdsForCountry(country);
                    Log.d(TAG, "Initial birds loaded for " + country + ": " + masterBirds.size() + ", converted to " + fullBirdList.size() + " full birds");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading birds for " + country + ": " + error);
                if (isInitialLoad) {
                    CustomDialogUtils.showErrorDialog(getContext(), "Error", "Failed to load bird data for " + country);
                }
            }
        });
    }

    private void convertMasterBirdsToFullBirdList() {
        fullBirdList.clear();
        Log.d(TAG, "Converting " + masterBirdList.size() + " master birds to full bird list");
        
        // Debug: Print some sample master birds
        int sampleSize = Math.min(5, masterBirdList.size());
        for (int i = 0; i < sampleSize; i++) {
            MasterBird sampleBird = masterBirdList.get(i);
            Log.d(TAG, "Sample master bird " + i + ": " + sampleBird.getComName() + ", Country: " + sampleBird.getCountry());
        }
        
        for (MasterBird masterBird : masterBirdList) {
            Bird bird = new Bird();
            bird.setComName(masterBird.getComName());
            bird.setSciName(masterBird.getSciName());
            
            // Set country codes as a list containing the single country
            List<String> countryCodes = new ArrayList<>();
            String country = masterBird.getCountry();
            Log.d(TAG, "Setting country code for bird " + masterBird.getComName() + ": " + country);
            countryCodes.add(country);
            bird.setCountryCodes(countryCodes);
            
            // Also set the country field directly for easier filtering
            bird.setCountry(country);
            
            fullBirdList.add(bird);
        }
        Log.d(TAG, "Converted to " + fullBirdList.size() + " birds in full bird list");
        
        // Debug: Print some sample converted birds
        sampleSize = Math.min(5, fullBirdList.size());
        for (int i = 0; i < sampleSize; i++) {
            Bird sampleBird = fullBirdList.get(i);
            Log.d(TAG, "Sample converted bird " + i + ": " + sampleBird.getComName() + ", Country codes: " + 
                  (sampleBird.getCountryCodes() != null ? sampleBird.getCountryCodes().toString() : "null") + 
                  ", Country: " + sampleBird.getCountry());
        }
    }

    private void startLocationDetection() {
        locationManager.detectLocation(new LocationManager.LocationCallback() {
            @Override
            public void onLocationDetected(String country) {
                detectedCountry = country;
                Log.d(TAG, "Location detected: " + country);
                isLocationDetectionComplete = true;
            }

            @Override
            public void onLocationError(String error) {
                Log.w(TAG, "Location detection failed: " + error);
                detectedCountry = "USA"; // Default fallback
                isLocationDetectionComplete = true;
            }
        });
    }
    
    private void setupSpinnerWithCountries() {
        spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, countryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(spinnerAdapter);

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCountry = countryNames.get(position);
                Log.d(TAG, "Country selected: " + selectedCountry + " at position: " + position);
                
                // Load birds for selected country lazily
                localDataManager.fetchMasterBirdListByCountry(selectedCountry, new LocalDataManager.MasterBirdListCallback() {
                    @Override
                    public void onSuccess(List<MasterBird> masterBirds) {
                        if (masterBirds == null || masterBirds.isEmpty()) {
                            Log.w(TAG, "No birds found for country: " + selectedCountry);
                            CustomDialogUtils.showInfoDialog(getContext(), "No Birds Found", "No birds found for " + selectedCountry);
                            // Show empty view
                            birdRecyclerView.setVisibility(View.GONE);
                            View emptyView = getView().findViewById(R.id.emptyView);
                            if (emptyView != null) {
                                emptyView.setVisibility(View.VISIBLE);
                            }
                            return;
                        }
                        
                        masterBirdList = masterBirds;
                        Log.d(TAG, "Lazy loaded birds for " + selectedCountry + ": " + masterBirds.size());
                        
                        convertMasterBirdsToFullBirdList();
                        showBirdsForCountry(selectedCountry);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error lazy loading birds for " + selectedCountry + ": " + error);
                        CustomDialogUtils.showErrorDialog(getContext(), "Error", "Failed to load birds for " + selectedCountry);
                        // Show empty view on error
                        birdRecyclerView.setVisibility(View.GONE);
                        View emptyView = getView().findViewById(R.id.emptyView);
                        if (emptyView != null) {
                            emptyView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Auto-select detected country if available
        if (detectedCountry != null && countryNames.contains(detectedCountry)) {
            int position = countryNames.indexOf(detectedCountry);
            countrySpinner.setSelection(position);
            Log.d(TAG, "Auto-selected detected country: " + detectedCountry + " at position: " + position);
        } else if (!countryNames.isEmpty()) {
            // Default to first country if detection failed
            countrySpinner.setSelection(0);
            Log.d(TAG, "No detected country, defaulting to first country: " + countryNames.get(0));
        }
        
        isSpinnerSetupComplete = true;
    }

    private void loadBirdsDirectlyFromJson(String selectedCountry) {
        Log.d(TAG, "Loading birds directly from JSON for country: " + selectedCountry);
        
        try {
            // Load birds directly from JSON file in assets
            InputStream is = getContext().getAssets().open("birds_master_list.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            
            // Parse JSON
            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> keys = jsonObject.keys();
            
            List<Bird> directlyLoadedBirds = new ArrayList<>();
            int count = 0;
            
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject birdJson = jsonObject.getJSONObject(key);
                
                String birdCountry = birdJson.optString("country", "");
                if (selectedCountry.equals(birdCountry)) {
                    Bird bird = new Bird();
                    bird.setId(key);
                    bird.setComName(birdJson.optString("comName", ""));
                    bird.setSciName(birdJson.optString("sciName", ""));
                    bird.setCountry(birdCountry);
                    
                    directlyLoadedBirds.add(bird);
                    count++;
                    
                    if (count <= 5) {
                        Log.d(TAG, "Directly loaded bird: " + bird.getComName());
                    }
                }
            }
            
            Log.d(TAG, "Directly loaded " + directlyLoadedBirds.size() + " birds for " + selectedCountry);
            
            // Process the directly loaded birds
            if (directlyLoadedBirds.isEmpty()) {
                Log.w(TAG, "No birds found for country: " + selectedCountry);
                CustomDialogUtils.showInfoDialog(getContext(), "No Birds Found", "No birds found for " + selectedCountry);
                // Show empty view when no birds are found
                birdRecyclerView.setVisibility(View.GONE);
                View emptyView = getView().findViewById(R.id.emptyView);
                if (emptyView != null) {
                    emptyView.setVisibility(View.VISIBLE);
                }
                return;
            }
            
            // Sort birds by name
            Collections.sort(directlyLoadedBirds, (b1, b2) -> b1.getComName().compareToIgnoreCase(b2.getComName()));
            
            // Group birds by first letter
            List<BirdGroup> birdGroups = groupBirdsByFirstLetter(directlyLoadedBirds);
            Log.d(TAG, "Created " + birdGroups.size() + " bird groups from directly loaded birds");
            
            // Update the adapter with the new bird groups
            groupedBirdAdapter.updateBirdGroups(birdGroups);
            
            // Re-apply existing birds after updating bird groups
            if (!existingBirds.isEmpty()) {
                Log.d(TAG, "Re-applying existing birds after direct loading");
                groupedBirdAdapter.setExistingBirds(existingBirds);
            }
            
            // Make sure RecyclerView is visible and empty view is hidden
            birdRecyclerView.setVisibility(View.VISIBLE);
            View emptyView = getView().findViewById(R.id.emptyView);
            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
            
            // Force layout refresh
            birdRecyclerView.post(() -> {
                Log.d(TAG, "Forcing layout refresh after direct loading");
                birdRecyclerView.invalidate();
                birdRecyclerView.requestLayout();
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading birds directly from JSON: " + e.getMessage(), e);
            CustomDialogUtils.showErrorDialog(getContext(), "Error", "Error loading birds: " + e.getMessage());
            
            // Show empty view on error
            birdRecyclerView.setVisibility(View.GONE);
            View emptyView = getView().findViewById(R.id.emptyView);
            if (emptyView != null) {
                emptyView.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void showBirdsForCountry(String selectedCountry) {
        Log.d(TAG, "Showing birds for country: " + selectedCountry);
        
        // Try to load birds directly from JSON first
        loadBirdsDirectlyFromJson(selectedCountry);
        
        // If direct loading fails, the original method will continue with the existing logic
        if (fullBirdList == null || fullBirdList.isEmpty()) {
            Log.w(TAG, "Full bird list is empty or null after direct loading attempt");
            return;
        }
        
        // The rest of the original method is kept as fallback but will likely not be executed
        // since loadBirdsDirectlyFromJson handles everything
    }
    
    private List<BirdGroup> groupBirdsByFirstLetter(List<Bird> birds) {
        Map<String, List<Bird>> groupMap = new HashMap<>();
        
        Log.d(TAG, "Grouping " + birds.size() + " birds by first letter");
        
        // Group birds by first letter
        for (Bird bird : birds) {
            String firstLetter = bird.getComName().substring(0, 1).toUpperCase();
            if (!groupMap.containsKey(firstLetter)) {
                groupMap.put(firstLetter, new ArrayList<>());
            }
            groupMap.get(firstLetter).add(bird);
        }
        
        Log.d(TAG, "Created " + groupMap.size() + " letter groups");
        
        // Convert to BirdGroup list and sort by letter
        List<BirdGroup> birdGroups = new ArrayList<>();
        for (Map.Entry<String, List<Bird>> entry : groupMap.entrySet()) {
            BirdGroup group = new BirdGroup(entry.getKey(), entry.getValue());
            birdGroups.add(group);
            Log.d(TAG, "Group " + entry.getKey() + " has " + entry.getValue().size() + " birds");
        }
        
        Collections.sort(birdGroups, (g1, g2) -> g1.getLetter().compareToIgnoreCase(g2.getLetter()));
        Log.d(TAG, "Returning " + birdGroups.size() + " sorted bird groups");
        return birdGroups;
    }
    
    private void loadExistingBirdsFromFirebase() {
        Log.d(TAG, "loadExistingBirdsFromFirebase called with userId: " + userId);
        if (userId == null) {
            Log.d(TAG, "userId is null, returning early");
            return;
        }
        
        DatabaseReference birdsRef = FirebaseManager.getInstance().getBirdsReference();
        
        // Remove existing listener if it exists
        if (existingBirdsListener != null) {
            birdsRef.removeEventListener(existingBirdsListener);
        }
        
        // Create new listener
        existingBirdsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                existingBirds.clear();
                
                for (DataSnapshot birdSnapshot : dataSnapshot.getChildren()) {
                    Bird bird = birdSnapshot.getValue(Bird.class);
                    if (bird != null && userId.equals(bird.getUserId())) {
                        existingBirds.put(bird.getComName(), bird);
                        Log.d(TAG, "Loaded existing bird: " + bird.getComName() + 
                              " - Female: " + bird.isFemale() + 
                              ", Male: " + bird.isMale() + 
                              ", Saw: " + bird.isSaw() + 
                              ", Photographed: " + bird.isPhotographed() + 
                              ", Heard: " + bird.isHeard());
                    }
                }
                
                Log.d(TAG, "Total existing birds loaded: " + existingBirds.size());
                
                // Update adapter with existing bird data
                if (groupedBirdAdapter != null) {
                    Log.d(TAG, "Calling setExistingBirds on adapter");
                    groupedBirdAdapter.setExistingBirds(existingBirds);
                } else {
                    Log.d(TAG, "groupedBirdAdapter is null, cannot set existing birds");
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading existing birds from Firebase", databaseError.toException());
            }
        };
        
        // Add the listener
        birdsRef.addValueEventListener(existingBirdsListener);
    }
    
    private void saveSelectedBirds() {
        Log.d(TAG, "saveSelectedBirds() called");
        
        // Get the selected birds from the adapter
        List<Bird> selectedBirds = groupedBirdAdapter.getSelectedBirds();
        Log.d(TAG, "Selected birds count: " + selectedBirds.size());
        
        if (selectedBirds.isEmpty()) {
            Log.w(TAG, "No birds selected for saving");
            // Show a popup dialog if no birds are selected
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
            builder.setTitle("Validation Error")
                   .setMessage("Please check at least one bird to the list")
                   .setPositiveButton("OK", null) // null listener just dismisses the dialog
                   .show();
            return;
        }
        
        // Get the selected country from spinner
        String selectedCountry = null;
        if (countrySpinner.getSelectedItem() != null) {
            selectedCountry = countrySpinner.getSelectedItem().toString();
        }
        Log.d(TAG, "Selected country: " + selectedCountry);
        Log.d(TAG, "Username: " + username + ", UserId: " + userId);

        // Create clean Bird objects for Firebase with gender-specific observations
        List<Bird> cleanBirds = new ArrayList<>();
        Map<Bird, boolean[]> checkboxStates = groupedBirdAdapter.getCheckboxStates();
        Log.d(TAG, "Checkbox states map size: " + checkboxStates.size());
        
        for (Bird bird : selectedBirds) {
            boolean[] states = checkboxStates.get(bird);
            if (states != null) {
                // states: [sawMale, photographedMale, heardMale, sawFemale, photographedFemale, heardFemale]
                boolean sawMale = states[0];
                boolean photographedMale = states[1];
                boolean heardMale = states[2];
                boolean sawFemale = states[3];
                boolean photographedFemale = states[4];
                boolean heardFemale = states[5];
                
                // Create male record if any male observation is selected
                if (sawMale || photographedMale || heardMale) {
                    Bird maleBird = new Bird();
                    maleBird.setSciName(bird.getSciName());
                    maleBird.setComName(bird.getComName());
                    maleBird.setCountry(selectedCountry);
                    maleBird.setUsername(username);
                    maleBird.setUserId(userId);
                    maleBird.setGender("M");
                    maleBird.setSawMale(sawMale);
                    maleBird.setPhotographedMale(photographedMale);
                    maleBird.setHeardMale(heardMale);
                    
                    // Set legacy fields for backward compatibility
                    maleBird.setMale(true);
                    maleBird.setSaw(sawMale);
                    maleBird.setPhotographed(photographedMale);
                    maleBird.setHeard(heardMale);
                    
                    cleanBirds.add(maleBird);
                }
                
                // Create female record if any female observation is selected
                if (sawFemale || photographedFemale || heardFemale) {
                    Bird femaleBird = new Bird();
                    femaleBird.setSciName(bird.getSciName());
                    femaleBird.setComName(bird.getComName());
                    femaleBird.setCountry(selectedCountry);
                    femaleBird.setUsername(username);
                    femaleBird.setUserId(userId);
                    femaleBird.setGender("F");
                    femaleBird.setSawFemale(sawFemale);
                    femaleBird.setPhotographedFemale(photographedFemale);
                    femaleBird.setHeardFemale(heardFemale);
                    
                    // Set legacy fields for backward compatibility
                    femaleBird.setFemale(true);
                    femaleBird.setSaw(sawFemale);
                    femaleBird.setPhotographed(photographedFemale);
                    femaleBird.setHeard(heardFemale);
                    
                    cleanBirds.add(femaleBird);
                }
            }
        }

        Log.d(TAG, "About to save " + cleanBirds.size() + " birds to Firebase");
        
        // Save to Firebase with callback
        FirebaseManager.getInstance().saveBirds(cleanBirds, new FirebaseManager.OnBirdsSavedListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Firebase save successful");
                // Show success dialog
                new AlertDialog.Builder(getContext())
                        .setTitle("Success")
                        .setMessage("Changes are saved")
                        .setPositiveButton("OK", null)
                        .show();
                
                // Refresh the adapter to reflect saved state
                groupedBirdAdapter.notifyDataSetChanged();
                
                // Notify the listener (WelcomeActivity) that birds have been saved
                if (birdsSavedListener != null) {
                    Log.d(TAG, "Calling birdsSavedListener.onBirdsSaved()");
                    birdsSavedListener.onBirdsSaved();
                } else {
                    Log.w(TAG, "birdsSavedListener is null");
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Firebase save failed: " + error);
                // Show error dialog
                new AlertDialog.Builder(getContext())
                        .setTitle("Error")
                        .setMessage("Error saving birds: " + error)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }
    
    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            // Request location permissions
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permissions already granted, detect location
            detectCurrentCountry();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            Log.d(TAG, "Location permission result received");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted, starting detection");
                // Permission granted, detect location
                detectCurrentCountry();
            } else {
                Log.w(TAG, "Location permission denied by user");
                // Permission denied, show a message
                Toast.makeText(getContext(), "Location permission denied. Please select country manually.", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void detectCurrentCountry() {
        if (locationHelper != null) {
            Log.d(TAG, "Starting country detection...");
            
            locationHelper.getCurrentCountry(new LocationHelper.LocationCallback() {
                @Override
                public void onLocationDetected(String countryCode) {
                    Log.d(TAG, "Country detected successfully: " + countryCode);
                    isLocationDetectionComplete = true;
                    autoSelectCountry(countryCode);
                }
                
                @Override
                public void onLocationError(String error) {
                    Log.e(TAG, "Location detection failed: " + error);
                    isLocationDetectionComplete = true;
                    // Default to US if location detection fails
                    autoSelectCountry("US");
                }
            });
        } else {
            Log.e(TAG, "LocationHelper is null");
            isLocationDetectionComplete = true;
            // Default to USA if LocationHelper is null
            autoSelectCountry("US");
        }
    }
    
    private void autoSelectCountry(String countryCode) {
        if (countryCode != null && countryNames != null && spinnerAdapter != null) {
            Log.d(TAG, "Attempting to auto-select country: " + countryCode);
            Log.d(TAG, "Available countries: " + countryNames.size() + " total");
            
            // Convert country code to country name using mapping
            String countryName = COUNTRY_CODE_TO_NAME.get(countryCode);
            if (countryName == null) {
                Log.w(TAG, "No mapping found for country code: " + countryCode);
                // Default to USA if no mapping found
                countryName = "USA";
            }
            
            Log.d(TAG, "Looking for country name: " + countryName);
            
            // Find the country name in the list
            int position = countryNames.indexOf(countryName);
            if (position >= 0) {
                // Temporarily enable location detection complete to allow selection
                boolean wasComplete = isLocationDetectionComplete;
                isLocationDetectionComplete = true;
                
                // Set the spinner selection
                countrySpinner.setSelection(position);
                Log.d(TAG, "Successfully auto-selected country: " + countryName + " (" + countryCode + ") at position: " + position);
                
                // Restore the original state
                isLocationDetectionComplete = wasComplete;
            } else {
                Log.w(TAG, "Country name " + countryName + " not found in available countries list");
                Log.d(TAG, "First 10 available countries: " + countryNames.subList(0, Math.min(10, countryNames.size())));
                
                // Don't call triggerLocationBasedSelection to avoid recursion
                // Just log the error
                Log.e(TAG, "Could not find country name in list, skipping auto-selection");
            }
        } else {
            Log.e(TAG, "Cannot auto-select country - missing data: countryCode=" + countryCode + 
                       ", countryNames=" + (countryNames != null ? "available" : "null") + 
                       ", spinnerAdapter=" + (spinnerAdapter != null ? "available" : "null"));
            
            // Don't call triggerLocationBasedSelection to avoid recursion
            // Just log the error
            Log.e(TAG, "Missing data for country selection, skipping auto-selection");
        }
    }
    

    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh existing birds data when fragment becomes visible
        // This ensures checkboxes are updated if birds were removed from LIFE LIST
        loadExistingBirdsFromFirebase();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up location helper to prevent memory leaks
        if (locationHelper != null) {
            locationHelper.cleanup();
            Log.d(TAG, "LocationHelper cleaned up");
        }
        
        // Remove Firebase listener to prevent memory leaks
        if (existingBirdsListener != null) {
            DatabaseReference birdsRef = FirebaseManager.getInstance().getBirdsReference();
            birdsRef.removeEventListener(existingBirdsListener);
            existingBirdsListener = null;
            Log.d(TAG, "Firebase listener removed");
        }
    }
}
