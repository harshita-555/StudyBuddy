package com.example.studybuddy.sbj;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.studybuddy.R;
import com.example.studybuddy.sbj.mediaStore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class DetailActivity extends AppCompatActivity {

    private DatabaseReference mMediaRef;
    private static ArrayList<mediaStore> mediaList;

    MyAdapter mAdapter;
    ViewPager mViewpager;
    private int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        position  = getIntent().getIntExtra("pos",1);
        String SubKey = getIntent().getStringExtra("SubId");

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mMediaRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Subjects").child(SubKey).child("media");

        mediaList = new ArrayList<mediaStore>();
        mMediaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot mediaSnapShot : dataSnapshot.getChildren()) {
                    mediaList.add(mediaSnapShot.getValue(mediaStore.class));
                    //Log.w("dugifuegihhhhhhhh",String.valueOf(mediaList.size()));

                }
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mAdapter = new MyAdapter(getSupportFragmentManager());
        mViewpager = (ViewPager) findViewById(R.id.media_detail_pager);
        mViewpager.setAdapter(mAdapter);

        //


    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewpager.setCurrentItem(position);

    }

     public static class MyAdapter extends FragmentStatePagerAdapter {
        public MyAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return ArrayListFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return mediaList.size();
        }
    }

    public static class ArrayListFragment extends Fragment{
        int mNum;
        String url;
        boolean isVideo;

        static ArrayListFragment newInstance(int num) {
            ArrayListFragment f = new ArrayListFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            args.putBoolean("isVideo", mediaList.get(num).isVideo());
            args.putString("url", mediaList.get(num).getMediaUrl());
            f.setArguments(args);


            return f;
        }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if(getArguments()!=null)
            {
                mNum=getArguments().getInt("num");
                url=getArguments().getString("url");
                isVideo=getArguments().getBoolean("isVideo");
            }

        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_detail, container, false);
            ImageView iv = v.findViewById(R.id.detail_image);
            VideoView tv = v.findViewById(R.id.detail_video);
            if(isVideo==false){
                tv.setVisibility(View.GONE);
                iv.setVisibility(View.VISIBLE);
                Glide.with(getContext()).load(url).placeholder(R.drawable.book_32).transition(withCrossFade()).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(iv);
            }
            else
            {
                iv.setVisibility(View.GONE);
                tv.setVisibility(View.VISIBLE);
                MediaController mc= new MediaController(getActivity());

                tv.setVideoURI(Uri.parse(url));
                tv.setMediaController(mc);
                tv.start();

            }

            return v;
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w("dugifuegi",String.valueOf(mediaList.size()));
    }
}