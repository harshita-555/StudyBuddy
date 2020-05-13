package com.example.studybuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.studybuddy.clsrm.ClassroomActivity;
import com.example.studybuddy.group.GroupActivity;
import com.example.studybuddy.sbj.SubjectsActivity;
import com.example.studybuddy.timetable.TimeTableActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class WorkActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference mUserReference;
    private CircleImageView mCircleImageView;
    private Button mSubject,mGroup,mClassroom,mTimeTable;
    private String Uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        mSubject=(Button) findViewById(R.id.btn_subject);
        mSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WorkActivity.this, SubjectsActivity.class));
            }
        });

        mGroup = (Button) findViewById(R.id.btn_group);
        mGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WorkActivity.this, GroupActivity.class));
            }
        });

        mClassroom= (Button) findViewById(R.id.btn_classroom);
        mClassroom.setVisibility(View.GONE);

        mTimeTable = (Button) findViewById(R.id.btn_time_table);
        mTimeTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WorkActivity.this, TimeTableActivity.class));
            }
        });


        auth = FirebaseAuth.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mUserReference= FirebaseDatabase.getInstance().getReference().child("Users");


        Uid=user.getUid();
        mUserReference.child(Uid).child("online").setValue("true");
        mUserReference.child(Uid).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fullName = dataSnapshot.getValue(User.class).getFullName();
                TextView username = (TextView) findViewById(R.id.userName);
                username.setText(fullName);

                mCircleImageView=(CircleImageView) findViewById(R.id.profile_pic);

                final String image = (String) dataSnapshot.child("image").getValue();
                if (!image.equals(""))
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.user_img).into(mCircleImageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {

                        }
                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.user_img).into(mCircleImageView);
                        }

                    });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.settings)
        {
            startActivity(new Intent(WorkActivity.this,SettingActivity.class));
        }

        if(item.getItemId()==R.id.logout)
        {
            mUserReference.child(Uid).child("online").setValue(System.currentTimeMillis());
            auth.signOut();

            startActivity(new Intent(WorkActivity.this, LoginActivity.class));
            finish();
        }
        return true;
    }


}