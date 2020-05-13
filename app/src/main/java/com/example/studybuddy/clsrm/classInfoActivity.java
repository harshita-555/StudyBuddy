package com.example.studybuddy.clsrm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studybuddy.R;
import com.example.studybuddy.StudentActivity;
import com.example.studybuddy.TeacherActivity;
import com.example.studybuddy.group.GetTimeAgo;
import com.example.studybuddy.group.GroupInfoActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.studybuddy.group.GetTimeAgo.getTimeAgo;

public class classInfoActivity extends AppCompatActivity {

    private DatabaseReference mGroupRef;
    private  DatabaseReference mRootReference;
    private ImageView grpImage;
    private TextView grpName;
    private RecyclerView grpMembers;
    private int code;
    private  String mGroupId;
    private List<String> memberlist;
    private myAdapter mAdapter;
    final  static int RC_PHOTO_PICKER=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        grpImage= findViewById(R.id.info_pic);
        grpName = (TextView) findViewById(R.id.info_name);
        grpMembers = (RecyclerView) findViewById(R.id.info_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(classInfoActivity.this);

        grpMembers.setHasFixedSize(true);
        grpMembers.setLayoutManager(linearLayoutManager);

        memberlist=new ArrayList<>();
        mAdapter= new myAdapter(memberlist);
        grpMembers.setAdapter(mAdapter);

        code = getIntent().getIntExtra("code",0);
        mGroupId=getIntent().getStringExtra("group_id");

        mRootReference= FirebaseDatabase.getInstance().getReference();
        mGroupRef= FirebaseDatabase.getInstance().getReference().child("Classroom");
        mGroupRef.child(mGroupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("image").getValue().toString().equals(""))
                {
                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.user_img).into(grpImage);
                }
                grpName.setText(dataSnapshot.child("name").getValue().toString());
                for(DataSnapshot members : dataSnapshot.child("members").getChildren())
                {
                    memberlist.add(members.getValue().toString());
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        if (code == 1) {
            grpName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(classInfoActivity.this);
                    alertDialog.setTitle("Change Name");
                    alertDialog.setMessage("Enter new name :");

                    final EditText input = new EditText(classInfoActivity.this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);
                    alertDialog.setView(input);

                    alertDialog.setPositiveButton("DONE",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String name = input.getText().toString();
                                    if (!name.equals("")) {
                                        mGroupRef.child(mGroupId).child("name").setValue(name);
                                    } else {
                                        Toast.makeText(classInfoActivity.this, "Enter text!", Toast.LENGTH_SHORT).show();
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

            });
            grpImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    CharSequence options[] = new CharSequence[]{"Change", "Cancel"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(classInfoActivity.this);
                    builder.setTitle("Change Class Pic");
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

        }
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(classInfoActivity.this, classChatActivity.class);
        intent.putExtra("group_id", mGroupId);
        intent.putExtra("name", grpName.getText().toString());
        finish();
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
        Intent intent = new Intent(classInfoActivity.this, classChatActivity.class);
        intent.putExtra("group_id", mGroupId);
        intent.putExtra("name", grpName.getText().toString());
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==RC_PHOTO_PICKER)
        {
            Uri selectedImageUri = data.getData();

            StorageReference photoRef = FirebaseStorage.getInstance().getReference().child("classes").child(mGroupId+ ".jpg");
            photoRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    String downloadUrl = urlTask.getResult().toString();

                    mRootReference.child("Classroom/"+ mGroupId+"/image").setValue(downloadUrl);
                    Picasso.get().load(downloadUrl).placeholder(R.drawable.user_img).into(grpImage);

                }

            });
        }
    }

    public class myAdapter extends RecyclerView.Adapter<myAdapter.mViewHolder>{

        private List<String> memberList;

        public myAdapter(List<String> memberList)
        {
            this.memberList=memberList;
        }
        @NonNull
        @Override
        public myAdapter.mViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().from(parent.getContext()).inflate(R.layout.recycle_list_single_user, parent, false);
            return  new myAdapter.mViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final myAdapter.mViewHolder holder, int position) {

            String user_id= memberList.get(position);
            mRootReference.child("Users/"+user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    holder.setName(dataSnapshot.child("fullName").getValue().toString());

                    if(dataSnapshot.child("online").getValue().toString().equals("true")) holder.setMessage1();

                    else holder.setMessage(Long.parseLong(dataSnapshot.child("online").getValue().toString()));

                    if(!dataSnapshot.child("image").getValue().toString().equals(""))holder.setProfilePic(dataSnapshot.child("image").getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        @Override
        public int getItemCount() {
            return memberList.size();
        }

        public class mViewHolder extends RecyclerView.ViewHolder {
            private View mView;

            public mViewHolder(@NonNull View itemView) {
                super(itemView);
                mView = itemView;
            }


            public void setMessage(Long message) {
                TextView userStatusView = (TextView) mView.findViewById(R.id.textViewSingleListStatus);
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                String lastSeen = getTimeAgo(message);
                userStatusView.setText(lastSeen);

            }
            public void setMessage1() {
                TextView userStatusView = (TextView) mView.findViewById(R.id.textViewSingleListStatus);
                userStatusView.setText("online");

            }

            public void setName(String name) {
                TextView userNameView = (TextView) mView.findViewById(R.id.textViewSingleListName);
                userNameView.setText(name);
            }


            public void setProfilePic(String userImg) {

                CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.circleImageViewUserImage);
                Picasso.get().load(userImg).placeholder(R.drawable.user_img).into(userImageView);
            }
        }
    }
}
