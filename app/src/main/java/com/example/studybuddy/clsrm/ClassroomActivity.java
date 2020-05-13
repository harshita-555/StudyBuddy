package com.example.studybuddy.clsrm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

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
import com.example.studybuddy.group.RequestFragment;
import com.example.studybuddy.quizzes.QuizStudentFragment;
import com.example.studybuddy.quizzes.QuizTeacherFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

public class ClassroomActivity extends AppCompatActivity {

    private ViewPager mviewPager;
    private GroupFragmentPagerAdapter mFragmentPagerAdapter;
    private TabLayout mtabLayout;
    private DatabaseReference mUserRef;
    private String occupation;
    private String mGroupId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        mviewPager=(ViewPager)findViewById(R.id.subject_viewPager);
        mtabLayout=(TabLayout)findViewById(R.id.group_tabLayout);


        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mUserRef.child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                occupation = dataSnapshot.getValue(User.class).getOccupation();
                mFragmentPagerAdapter=new GroupFragmentPagerAdapter(getSupportFragmentManager());
                mviewPager.setAdapter(mFragmentPagerAdapter);
                mtabLayout.setupWithViewPager(mviewPager);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mGroupId = getIntent().getStringExtra("DeepLink");
        if(mGroupId!=null)
        {
            Uri uri = Uri.parse(mGroupId);
            mGroupId = uri.getQueryParameter("key");
            FirebaseDatabase.getInstance().getReference().child("Classroom/"+ mGroupId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    Intent chatIntent = new Intent(ClassroomActivity.this, classChatActivity.class);
                    chatIntent.putExtra("group_id", mGroupId);
                    chatIntent.putExtra("name", name);
                    startActivity(chatIntent);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

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
                    return new classChatFragment();
                case 1:
                    if(occupation.equals("Student")) return new QuizStudentFragment();
                    return new QuizTeacherFragment();

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
                    return "CLASSES";
                case 1:
                    return "QUIZZES";
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

        return i;
    }
}
