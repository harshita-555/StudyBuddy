package com.example.studybuddy.clsrm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studybuddy.R;
import com.example.studybuddy.User;
import com.example.studybuddy.group.Chats;
import com.example.studybuddy.group.FormNewGroup;
import com.example.studybuddy.group.GroupActivity;
import com.example.studybuddy.quizzes.Question;
import com.example.studybuddy.quizzes.Quiz;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShareTestActivity extends AppCompatActivity {

    private TextView mUserName;
    private TextView mUserLastSeen;
    private CircleImageView mUserImage;
    private String mCurrentUserId;

    private final static int RC_PHOTO_PICKER=1;
    private int count=0;

    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mRootReference;
    private FirebaseRecyclerAdapter<Chats, ShareViewHolder> firebaseRecyclerAdapter;

    private ArrayList<String> members;
    private Quiz quiz;
    private String Key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_new_group);

        mUsersList=(RecyclerView)findViewById(R.id.recyclerViewUsersList1);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));


        mRootReference = FirebaseDatabase.getInstance().getReference();
        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users/"+ mCurrentUserId+"/class");
        mUsersDatabaseReference.keepSynced(true);

        Key = getIntent().getStringExtra("Key");
        quiz = (Quiz) getIntent().getSerializableExtra("Quiz");
        quiz.setQuestions( (ArrayList<Question>) getIntent().getSerializableExtra("Questions"));
        members = new ArrayList<>();



        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.app_bar_layout, null);
        actionBar.setCustomView(actionBarView);

        mUserName = (TextView) actionBarView.findViewById(R.id.textView3);
        mUserLastSeen = (TextView) actionBarView.findViewById(R.id.textView5);
        mUserImage = (CircleImageView) actionBarView.findViewById(R.id.circleImageView);

        mUserName.setText("Share Quiz");
        mUserLastSeen.setText("Select Classrooms");
        mUserImage.setVisibility(View.GONE);


        Button mButton = (Button) findViewById(R.id.Next);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (members.size() == 0) {
                    Toast.makeText(ShareTestActivity.this, "Select atleast 1 classroom", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShareTestActivity.this);
                    alertDialog.setTitle("Share the Quiz?");

                    alertDialog.setPositiveButton("YES",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(ShareTestActivity.this, "Sharing Quizzes!", Toast.LENGTH_SHORT).show();
                                        quiz.setTimeStamp(System.currentTimeMillis());
                                        for (String mClassId : members) {

                                                addMessage(mClassId);




                                        }
                                        startActivity(new Intent(ShareTestActivity.this, ClassroomActivity.class));
                                        finish();




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



    protected void onStart() {
        super.onStart();


        Query query = mUsersDatabaseReference.orderByChild("name");
        FirebaseRecyclerOptions<Chats> options = new FirebaseRecyclerOptions.Builder<Chats>().setQuery(query, Chats.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chats, ShareViewHolder>(options) {

            @NonNull
            @Override
            public ShareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_list_single_user, parent, false);
                return new ShareViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ShareViewHolder holder, int position, @NonNull final Chats model) {

                final String classId = getRef(position).getKey();

                Query lastMessageQuery = mRootReference.child("Messages/"+mCurrentUserId+"/"+classId).limitToLast(1);
                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        String data = dataSnapshot.child("content").getValue().toString();
                        holder.setMessage(data);

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {     }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {   }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {      }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {     }

                });

                mRootReference.child("Classroom/"+classId).addListenerForSingleValueEvent(new ValueEventListener() {

                    String name , image ;
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                       name= dataSnapshot.child("name").getValue(String.class);
                       image = dataSnapshot.child("image").getValue(String.class);
                       holder.setName(name);
                       if(!image.equals("")) holder.setImage(image);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean flag = false;
                            for (int i = 0; i < members.size(); i++) {
                                if (members.get(i).equals(classId)) {
                                    members.remove(classId);
                                    flag = true;
                                    v.setBackgroundResource(R.color.white);
                                    break;
                                }
                            }
                            if (!flag) {
                                members.add(classId);
                                v.setBackgroundResource(R.color.colorAccent);
                            }
                        }
                    });


            }

        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class ShareViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ShareViewHolder(View itemView) {
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
        public void setMessage(String message) {
            TextView userStatusView = (TextView) mView.findViewById(R.id.textViewSingleListStatus);
            userStatusView.setText(message);

        }
    }
    public void addMessage(final String mClassId)
    {
        DatabaseReference user_message_push = mRootReference.child("Messages").child(mCurrentUserId).child(mClassId).push();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm ");
        String content;
        if(quiz.getAttemptTill()==-1) content = "There is no deadline!";
        else
        {
            Date date = new Date(quiz.getAttemptTill());
            content ="The Last Date is " + df.format(date) +". ";
        }

        final String push_id = user_message_push.getKey();
        final long time = System.currentTimeMillis();
        final Map messageMap = new HashMap();
        messageMap.put("content", "The Test ,"+ quiz.getName()+" , has been added ."+ content);
        messageMap.put("type", "text");
        messageMap.put("time", time);
        messageMap.put("from", mCurrentUserId);


        mRootReference.child("Classroom/" + mClassId + "/members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot members : dataSnapshot.getChildren())
                {
                    String memberId= members.getValue(String.class);
                    if(!memberId.equals(mCurrentUserId)) mRootReference.child("Users/"+ memberId+"/Tests/"+ Key).setValue(quiz);
                    mRootReference.child("Messages").child(memberId).child(mClassId).child(push_id).setValue(messageMap);
                    mRootReference.child("Users").child(memberId).child("class").child(mClassId).child("timestamp").setValue(time);
                    mRootReference.child("Users").child(memberId).child("class").child(mClassId).child("seen").setValue(false);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
