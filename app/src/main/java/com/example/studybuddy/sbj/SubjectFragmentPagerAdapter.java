package com.example.studybuddy.sbj;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SubjectFragmentPagerAdapter extends FragmentPagerAdapter {

    public SubjectFragmentPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                MediaFragment mediaFragment= new MediaFragment();

                return mediaFragment;
            case 1:
                return new DocsFragment();
            case 2:
                return new notesFragment();
            case 3:
                return new FlashCardFragment();
        }
        return null;
    }


    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "MEDIA";
            case 1:
                return "DOCUMENT";
            case 2:
                return "NOTES";
            case 3:
                return "Flash\nCards";
        }
        return super.getPageTitle(position);
    }



}
