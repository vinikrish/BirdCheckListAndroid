
package com.vinikrish.birdchecklistandroid;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabAdapter extends FragmentStateAdapter {

    public TabAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AddBirdsFragment();
            case 1:
                return new LifeListFragment();
            default:
                return new AddBirdsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
