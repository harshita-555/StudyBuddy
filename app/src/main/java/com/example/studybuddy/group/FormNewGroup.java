package com.example.studybuddy.group;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FormNewGroup extends AppCompatActivity {

    private TextView mUserName;
    private TextView mUserLastSeen;
    private CircleImageView mUserImage;
    private String mCurrentUserId;

    private final static int RC_PHOTO_PICKER=1;
    private int count=0;

    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mRootReference;
    private FirebaseRecyclerAdapter<User, UserViewHolder> firebaseRecyclerAdapter;
    private   String group_id;

    private StorageReference mStorageReference;
    private Map grp1;
    private ArrayList<String> members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_new_group);

        mUsersList=(RecyclerView)findViewById(R.id.recyclerViewUsersList1);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabaseReference.keepSynced(true);
        mStorageReference = FirebaseStorage.getInstance().getReference().child("groups");
        mRootReference = FirebaseDatabase.getInstance().getReference();
        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        group_id = mRootReference.child("Groups").push().getKey();

        grp1 = new HashMap();
        members = new ArrayList<>();
        members.add(mCurrentUserId);
        count++;
        grp1.put("image", "");


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.app_bar_layout, null);
        actionBar.setCustomView(actionBarView);

        mUserName = (TextView) actionBarView.findViewById(R.id.textView3);
        mUserLastSeen = (TextView) actionBarView.findViewById(R.id.textView5);
        mUserImage = (CircleImageView) actionBarView.findViewById(R.id.circleImageView);

        mUserName.setText("New Group");
        mUserLastSeen.setText("Add Participants");

        mUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{"Change", "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(FormNewGroup.this);
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

        Button mButton = (Button) findViewById(R.id.Next);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (members.size() == 1) {
                    Toast.makeText(FormNewGroup.this, "Invite atleast 1 member", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(FormNewGroup.this);
                    alertDialog.setTitle("Group Name");
                    alertDialog.setMessage("Enter a name :");

                    final EditText input = new EditText(FormNewGroup.this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);
                    alertDialog.setView(input);

                    alertDialog.setPositiveButton("DONE",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String name = input.getText().toString();
                                    if (!name.equals("")) {
                                        // mref.child("docName").setValue(password);
                                        ArrayList<String> members1 = new ArrayList<>();
                                        members1.add(mCurrentUserId);

                                        long time = System.currentTimeMillis();
                                        grp1.put("name", name);
                                        grp1.put("admin", mCurrentUserId);
                                        grp1.put("createdOn", time);
                                        grp1.put("timestamp", time);
                                        grp1.put("members", members1);

                                        mRootReference.child("Groups").child(group_id).setValue(grp1);
                                        Toast.makeText(FormNewGroup.this, "Sending Invites!", Toast.LENGTH_SHORT).show();
                                        for (String mNewUser : members) {
                                            if (mNewUser.equals(mCurrentUserId)) {
                                                mRootReference.child("Users/" + mCurrentUserId + "/Chats/" + group_id + "/" + "seen").setValue(true);
                                                mRootReference.child("Users/" + mCurrentUserId + "/Chats/" + group_id + "/" + "timestamp").setValue(time);
                                                mRootReference.child("Users/" + mCurrentUserId + "/Chats/" + group_id + "/" + "group").setValue(true);
                                            } else {
                                                mRootReference.child("friend_request/" + mNewUser + "/" + group_id + "/request_type").setValue("join_group");
                                            }

                                        }
                                        startActivity(new Intent(FormNewGroup.this, GroupActivity.class));
                                        finish();


                                    } else {
                                        Toast.makeText(FormNewGroup.this, "Enter text!", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                    alertDialog.setNegativeButton("CANCEL",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                    alertDialog.show();
                }
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();

            StorageReference photoRef = mStorageReference.child(selectedImageUri.getLastPathSegment() + "/" + new Date().getTime());
            photoRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    String downloadUrl = urlTask.getResult().toString();

                    grp1.remove("image");
                    grp1.put("image", downloadUrl);
                    Picasso.get().load(downloadUrl).placeholder(R.drawable.user_img).into(mUserImage);

                }

            });
        }

    }

    protected void onStart() {
        super.onStart();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setQuery(mUsersDatabaseReference.orderByChild("fullName"), User.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_list_single_user, parent, false);
                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final UserViewHolder holder, int position, @NonNull final User model) {
                holder.setName(model.getFullName());
                if (!model.getImage().equals("")) holder.setImage(model.getImage());

                if (model.getUserId().equals(mCurrentUserId)) {
                    holder.mView.setBackgroundResource(R.color.colorAccent);
                    holder.setName("You");
                } else {
                    holder.setName(model.getFullName());
                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String uid = model.getUserId();

                            boolean flag = false;
                            for (int i = 0; i < members.size(); i++) {
                                if (members.get(i).equals(uid)) {
                                    members.remove(uid);
                                    flag = true;
                                    v.setBackgroundResource(R.color.white);
                                    break;
                                }
                            }
                            if (!flag) {
                                members.add(uid);
                                v.setBackgroundResource(R.color.colorAccent);
                            }
                        }
                    });
                }

            }

        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public UserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView userNameView = (TextView) mView.findViewById(R.id.textViewSingleListName);
            userNameView.setText(name);
        }

        public void setImage(String image) {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.circleImageViewUserImage);
            Picasso.get().load(image).placeholder(R.drawable.user_img).into(userImageView);
        }
    }


}
