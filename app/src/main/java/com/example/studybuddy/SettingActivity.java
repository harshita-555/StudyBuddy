package com.example.studybuddy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {



    private CircleImageView mCircleImageView;
    private TextView mdisplayName;
    private Button mChange;
    private EditText oldEmail, oldPassword, oldUsername;

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;

    private ProgressBar progressBar;
    private FirebaseAuth auth;

    private DatabaseReference mUserRef;
    private String occupation;

    private static final int GALLERY_PICK = 1;
    String uid;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mCircleImageView = (CircleImageView) findViewById(R.id.displayimage);
        mdisplayName     = (TextView) findViewById(R.id.textViewDisplayname);
        oldEmail         = (EditText) findViewById(R.id.email_edit_text);
        oldPassword      = (EditText) findViewById(R.id.password_edit_text);
        oldUsername      = (EditText) findViewById(R.id.username_edit_text);
        mChange          = (Button) findViewById(R.id.change_ass);
        progressBar      = (ProgressBar) findViewById(R.id.progressBar1);


        oldEmail.setVisibility(View.GONE);
        oldPassword.setVisibility(View.GONE);
        oldUsername.setVisibility(View.GONE);
        mChange.setVisibility(View.GONE);

        auth = FirebaseAuth.getInstance();
        mFirebaseUser = auth.getCurrentUser();
        uid = mFirebaseUser.getUid();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mDatabaseReference.keepSynced(true);
        mStorageReference = FirebaseStorage.getInstance().getReference();

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



        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

               String name = (String) dataSnapshot.child("fullName").getValue();
                mdisplayName.setText(name);


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
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

   public void changeImg(View view) {

        oldEmail.setVisibility(View.GONE);
        oldUsername.setVisibility(View.GONE);
        oldPassword.setVisibility((View.GONE));
        mChange.setVisibility(View.GONE);

        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_PICK);
    }

    public void changeEmail(View view) {

        oldEmail.setVisibility(View.VISIBLE);
        oldPassword.setVisibility(View.GONE);
        oldUsername.setVisibility(View.GONE);
        mChange.setVisibility(View.VISIBLE);
        mChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (mFirebaseUser != null && !oldEmail.getText().toString().trim().equals("")) {
                    mFirebaseUser.updateEmail(oldEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SettingActivity.this, "Email address is updated. Please sign in with new email id!", Toast.LENGTH_LONG).show();
                                        auth.signOut();
                                        startActivity(new Intent(SettingActivity.this, LoginActivity.class));
                                        finish();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(SettingActivity.this, "Failed to update email!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                } else if (oldEmail.getText().toString().trim().equals("")) {
                    oldEmail.setError("Enter email");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }

    public void changePassword(View view) {
        oldEmail.setVisibility(View.GONE);
        oldUsername.setVisibility(View.GONE);
        oldPassword.setVisibility((View.VISIBLE));
        mChange.setVisibility(View.VISIBLE);
        mChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (mFirebaseUser != null && !oldPassword.getText().toString().trim().equals("")) {
                    mFirebaseUser.updatePassword(oldPassword.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {

                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SettingActivity.this, "Password is updated. Please sign in with new Password!", Toast.LENGTH_LONG).show();
                                        auth.signOut();
                                        startActivity(new Intent(SettingActivity.this, LoginActivity.class));
                                        finish();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(SettingActivity.this, "Failed to update Password!", Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
                else if (oldEmail.getText().toString().trim().equals("")) {
                    oldEmail.setError("Enter Password");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }

    public void changeUsername(View view) {
        oldEmail.setVisibility(View.GONE);
        oldUsername.setVisibility(View.VISIBLE);
        oldPassword.setVisibility((View.GONE));
        mChange.setVisibility(View.VISIBLE);
        mChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String newname = oldUsername.getText().toString().trim();

                if (newname.equals("")) newname = "~~";

                Map update_HashMap = new HashMap();
                update_HashMap.put("fullName", newname);

                mDatabaseReference.updateChildren(update_HashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            Toast.makeText(SettingActivity.this, "Updated Successfuly...", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);

                        } else {
                            Toast.makeText(getApplicationContext(), " Couldn't update...", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);

                        }

                    }
                });
            }
        });

    }

    public void deleteAccount(View view) {

        oldEmail.setVisibility(View.GONE);
        oldPassword.setVisibility(View.GONE);
        mChange.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if (mFirebaseUser != null) {
            mFirebaseUser.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SettingActivity.this, "Your profile is deleted:( Create a account now!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SettingActivity.this, SignupActivity.class));
                                finish();
                                progressBar.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(SettingActivity.this, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        //-----STARTING GALLERY----


        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            progressBar.setVisibility(View.VISIBLE);


            final Uri resultUri = data.getData();
            File thumb_filepath = new File(resultUri.getPath());

            final StorageReference filepath = mStorageReference.child("profile_image").child(uid + ".jpg");
            final StorageReference thumb_file_path = mStorageReference.child("profile_image").child("thumbs").child(uid + ".jpg");

            //------STORING IMAGE IN FIREBASE STORAGE--------
            filepath.putFile(resultUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapShot) {
                    Task<Uri> urlTask = taskSnapShot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    Uri downloadUrl = urlTask.getResult();

                    Map update_HashMap = new HashMap();
                    update_HashMap.put("image", downloadUrl.toString());

                    mDatabaseReference.updateChildren(update_HashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(SettingActivity.this, "Uploaded Successfuly...", Toast.LENGTH_SHORT).show();

                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), " Image is not uploading...", Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                }
            });
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








