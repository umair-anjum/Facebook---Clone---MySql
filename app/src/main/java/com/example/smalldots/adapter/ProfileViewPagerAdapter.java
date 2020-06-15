package com.example.smalldots.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.smalldots.Fragments.ProfileFragment;

public class ProfileViewPagerAdapter extends FragmentPagerAdapter {

    int size=0;

    public ProfileViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        this.size=behavior;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return new ProfileFragment();

            default:
                return null;

        }


    }

    @Override
    public int getCount() {
        return size;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){
            case 0:
                return "Posts";
            default:
                return null;
        }
    }
}
