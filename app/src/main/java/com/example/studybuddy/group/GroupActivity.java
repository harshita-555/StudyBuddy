package com.example.studybuddy.group;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.example.studybuddy.R;
import com.example.studybuddy.StudentActivity;
import com.example.studybuddy.TeacherActivity;
import com.example.studybuddy.User;
import com.example.studybuddy.WorkActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupActivity extends AppCompatActivity {

    private ViewPager mviewPager;
    private GroupFragmentPagerAdapter mFragmentPagerAdapter;
    private TabLayout mtabLayout;
    private DatabaseReference mUserRef;
    private String occupation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        mviewPager=(ViewPager)findViewById(R.id.subject_viewPager);

        //---ADDING ADAPTER FOR FRAGMENTS IN VIEW PAGER----
        mFragmentPagerAdapter=new GroupFragmentPagerAdapter(getSupportFragmentManager());
        mviewPager.setAdapter(mFragmentPagerAdapter);

        //---SETTING TAB LAYOUT WITH VIEW PAGER
        mtabLayout=(TabLayout)findViewById(R.id.group_tabLayout);
        mtabLayout.setupWithViewPager(mviewPager);

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mUserRef.child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                occupation = dataSnapshot.getValue(User.class).getOccupation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public class GroupFragmentPagerAdapter extends FragmentPagerAdapter {

        public GroupFragmentPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new ChatFragment();
                case 1:
                    return new RequestFragment();
            }
            return null;
        }


        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return "CHATS";
                case 1:
                    return "INVITES";
            }
            return super.getPageTitle(position);
        }
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        return  getParentActivityIntentImpl();
    }

    private Intent getParentActivityIntentImpl() {
        Intent i = null;
        if (occupation.equals("Teacher")) {
            i = new Intent(this, TeacherActivity.class);
            ;
        } else  if (occupation.equals("Student")) {
            i = new Intent(this, StudentActivity.class);
        }
        else  if (occupation.equals("Work")) {
            i = new Intent(this, WorkActivity.class);
        }

        return i;
    }
}
