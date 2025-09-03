package com.vinikrish.birdchecklistandroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity implements AddBirdsFragment.OnBirdsSavedListener {
    private static final String TAG = "WelcomeActivity";
    
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView profileIcon;
    private FirebaseAuth mAuth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not logged in, redirect to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        initializeViews();
        setupTabs();
    }
    
    private void initializeViews() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        profileIcon = findViewById(R.id.profileIcon);
        
        // Set up profile icon click listener
        profileIcon.setOnClickListener(v -> showProfileMenu(v));
    }
    
    private void showProfileMenu(android.view.View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_profile) {
                // Navigate to Profile Activity
                Intent intent = new Intent(WelcomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_logout) {
                // Logout user
                mAuth.signOut();
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
        
        popup.show();
    }
    
    private void setupTabs() {
        // Get current user information
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String username = currentUser != null ? currentUser.getEmail() : null;
        String userId = currentUser != null ? currentUser.getUid() : null;
        
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(LifeListFragment.newInstance(username, userId), "LIFE LIST");
        adapter.addFragment(AddBirdsFragment.newInstance(username, userId), "ADD BIRDS");
        adapter.addFragment(new AboutFragment(), "ABOUT");
        
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }
    
    @Override
    public void onBirdsSaved() {
        // Switch to Life List tab (index 0) when birds are saved
        viewPager.setCurrentItem(0);
        
        // No need to manually refresh - LifeListFragment has ValueEventListener that auto-updates
    }
    
    public void refreshProfileBirdCount() {
        // This method can be called to refresh profile bird counts
        // Currently no action needed as ProfileActivity refreshes on resume
    }
    
    private void refreshLifeList() {
        // Get the current Life List fragment and refresh it
        TabAdapter adapter = (TabAdapter) viewPager.getAdapter();
        if (adapter != null && adapter.getCount() > 0) {
            Fragment lifeListFragment = adapter.getItem(0);
            if (lifeListFragment instanceof LifeListFragment) {
                ((LifeListFragment) lifeListFragment).refreshBirdList();
            }
        }
    }
    
    private static class TabAdapter extends FragmentPagerAdapter {
        private final java.util.List<Fragment> fragmentList = new java.util.ArrayList<>();
        private final java.util.List<String> fragmentTitleList = new java.util.ArrayList<>();
        
        public TabAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }
        
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }
        
        @Override
        public int getCount() {
            return fragmentList.size();
        }
        
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
        
        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
    }
}
