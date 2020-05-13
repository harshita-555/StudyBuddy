package com.example.studybuddy.clsrm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.studybuddy.R;
import com.example.studybuddy.group.FormNewGroup;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FormNewClass extends AppCompatActivity {

    private final static int RC_PHOTO_PICKER=1,RC_MAIL=2;
    private FirebaseUser user;
    private StorageReference mStorageReference;
    private DatabaseReference mRootReference;
    private ImageView mProfile;
    private EditText mName;
    private Button mDone;
    private Map grp1;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_new_class);

        mStorageReference = FirebaseStorage.getInstance().getReference().child("Classroom");
        user = FirebaseAuth.getInstance().getCurrentUser();
        mRootReference = FirebaseDatabase.getInstance().getReference();

        grp1 = new HashMap();
        grp1.put("image", "");


        mProfile = (ImageView) findViewById(R.id.classdisplayimage);
        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{"Change", "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(FormNewClass.this);
                builder.setTitle("Change Group Image");
                builder.setItems(options, new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 0) {

                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/jpeg");
                            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                            startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
                        }

                        if (which == 1) {
                            dialog.cancel();
                        }

                    }
                });
                builder.show();
            }
        });

        mName = (EditText) findViewById(R.id.className);
        mDone= (Button) findViewById(R.id.sendMail);
        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mName.getText().toString().equals(""))
                    Toast.makeText(FormNewClass.this, "Enter some name!", Toast.LENGTH_SHORT).show();
                else
                {

                     key = mRootReference.child("Classroom").push().getKey();
                     Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                            .setLink(Uri.parse("https://www.example.com/?key="+key))
                            .setDomainUriPrefix("https://studdybuddy.page.link")
                            .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                            .buildShortDynamicLink()
                            .addOnCompleteListener(FormNewClass.this, new OnCompleteListener<ShortDynamicLink>() {
                                @Override
                                public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                                    if (task.isSuccessful()) {
                                        // Short link created
                                        Uri shortLink = task.getResult().getShortLink();
                                        Uri flowchartLink = task.getResult().getPreviewLink();


                                        Intent intent = new Intent(Intent.ACTION_SEND);
                                        //intent.setType("*/*");
                                        intent.setType("message/rfc822");
                                        intent.putExtra(Intent.EXTRA_SUBJECT, "Link to Join my Class");
                                        intent.putExtra(Intent.EXTRA_TEXT,  shortLink.toString());
                                        if (intent.resolveActivity(getPackageManager()) != null) {
                                            startActivityForResult(intent,RC_MAIL);
                                        }


                                    }
                                }
                            });
                }

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();

            StorageReference photoRef = mStorageReference.child(key + ".jpg");
            photoRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    String downloadUrl = urlTask.getResult().toString();

                    grp1.remove("image");
                    grp1.put("image", downloadUrl);
                    Picasso.get().load(downloadUrl).placeholder(R.drawable.user_img).into(mProfile);

                }

            });
        }
        else  if (requestCode==RC_MAIL ) {

            addClass();
            startActivity(new Intent(FormNewClass.this, ClassroomActivity.class));
            finish();
        }

    }

    public void  addClass()
    {
        ArrayList<String> members1 = new ArrayList<>();
        members1.add(user.getUid());

        long time = System.currentTimeMillis();
        grp1.put("name", mName.getText().toString());
        grp1.put("admin", user.getUid());
        grp1.put("createdOn", time);
        grp1.put("timestamp", time);
        grp1.put("members", members1);



        mRootReference.child("Classroom").child(key).setValue(grp1);
        mRootReference.child("Users/" + user.getUid() + "/class/" + key + "/" + "seen").setValue(true);
        mRootReference.child("Users/" + user.getUid() + "/class/" + key + "/" + "timestamp").setValue(time);
        mRootReference.child("Users/" + user.getUid() + "/class/" + key + "/" + "group").setValue(true);

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(FormNewClass.this, ClassroomActivity.class));
        finish();

    }
}
