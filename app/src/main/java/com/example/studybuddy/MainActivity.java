package com.example.studybuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.studybuddy.clsrm.ClassroomActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

public class MainActivity extends AppCompatActivity {


    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private DatabaseReference mUserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        mUserReference = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser user = auth.getCurrentUser();

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();


                            if(user!=null)
                            {
                                Log.w("MianActivity", deepLink.toString());
                                Intent intent = new Intent(MainActivity.this, ClassroomActivity.class);
                                intent.putExtra("DeepLink", deepLink.toString());
                                startActivity(intent);
                                finish();
                            }
                            if(user==null)
                            {

                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                intent.putExtra("DeepLink", deepLink.toString());
                                startActivity(intent);
                                finish();
                            }

                        }



                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        // ...
                        else
                        {
                            if (user == null) {
                                // user auth state is changed - user is null
                                // launch login activity

                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                String Uid = user.getUid();
                                mUserReference.child("Users").child(Uid).child("online").setValue("true");

                                mUserReference.child("Users").child(Uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String occupation = dataSnapshot.getValue(User.class).getOccupation();
                                        Log.w("gdwuyfgoguo94","gfoqurp249p95mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm2");


                                        if (occupation.equals(getString(R.string.Student)))
                                            startActivity(new Intent(MainActivity.this, StudentActivity.class));
                                        else if (occupation.equals(getString(R.string.Teacher)))
                                            startActivity(new Intent(MainActivity.this, TeacherActivity.class));
                                        else if (occupation.equals(getString(R.string.Work)))
                                            startActivity(new Intent(MainActivity.this, WorkActivity.class));

                                        finish();

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }

                                });
                            }




                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("MainActivity", "getDynamicLink:onFailure", e);
                    }
                });


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();
        //auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            //auth.removeAuthStateListener(authListener);
        }
    }
}
