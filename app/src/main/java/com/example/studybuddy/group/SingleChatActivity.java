package com.example.studybuddy.group;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SingleChatActivity extends AppCompatActivity {

    private String mChatUser;
    TextView mUserName;
    TextView mUserLastSeen;
    CircleImageView mUserImage;
    private FirebaseAuth mAuth;

    String mCurrentUserId;

    private DatabaseReference mRootReference;
    private DatabaseReference mMessageReference;
    private ImageButton mChatSendButton, mChatAddButton;
    private EditText mMessageView;

    private RecyclerView mMessagesList;


    private FirebaseRecyclerAdapter<Messages, ConvViewHolder1> mMessageAdapter;
    private static final int RC_MEDIA_PICKER = 1;
    StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);

        mChatAddButton = (ImageButton) findViewById(R.id.chatAddButton);
        mChatSendButton = (ImageButton) findViewById(R.id.chatSendButton);
        mMessageView = (EditText) findViewById(R.id.chatMessageView);

        mChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("name");


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.app_bar_layout, null);
        actionBar.setCustomView(actionBarView);

        mUserName = (TextView) actionBarView.findViewById(R.id.textView3);
        mUserLastSeen = (TextView) actionBarView.findViewById(R.id.textView5);
        mUserImage = (CircleImageView) actionBarView.findViewById(R.id.circleImageView);
        mUserName.setText(userName);

        mRootReference = FirebaseDatabase.getInstance().getReference();
        mMessageReference = FirebaseDatabase.getInstance().getReference().child("Messages");
        mStorage = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();


        mMessagesList = (RecyclerView) findViewById(R.id.messageListView);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(linearLayoutManager);


        mRootReference.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String onlineValue = dataSnapshot.child("online").getValue().toString();
                //String onlineValue = "true";
                String imageValue = dataSnapshot.child("image").getValue().toString();

                if (!imageValue.equals(""))
                    Picasso.get().load(imageValue).placeholder(R.drawable.user_img).into(mUserImage);
                if (onlineValue.equals("true")) {
                    mUserLastSeen.setText("online");
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(onlineValue);
                    String lastSeen = GetTimeAgo.getTimeAgo(lastTime);
                    mUserLastSeen.setText(lastSeen);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootReference.child("Users").child(mCurrentUserId).child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser)) {

                    long time = System.currentTimeMillis();

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", true);
                    chatAddMap.put("timestamp", time);
                    chatAddMap.put("group", false);

                    Map chatUserMap = new HashMap();
                   // chatUserMap.put("Users/" + mChatUser + "/Chats/" + mCurrentUserId, chatAddMap);
                    //chatUserMap.put("Users/" + mCurrentUserId + "/Chats/" + mChatUser, chatAddMap);

                    mRootReference.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                Toast.makeText(getApplicationContext(), "Successfully Added new chat", Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(getApplicationContext(), "Cannot Add chat", Toast.LENGTH_SHORT).show();
                        }


                    });

                } else {
                    mRootReference.child("Users").child(mCurrentUserId).child("Chats").child(mChatUser).child("seen").setValue(true);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mChatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message2 = mMessageView.getText().toString().trim();
                if (!TextUtils.isEmpty(message2)) {

                    String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUser;
                    String chat_user_ref = "Messages/" + mChatUser + "/" + mCurrentUserId;

                    DatabaseReference user_message_push = mRootReference.child("Messages").child(mCurrentUserId).child(mChatUser).push();

                    String push_id = user_message_push.getKey();
                    final long time = System.currentTimeMillis();

                    Messages msg1 = new Messages(message2, "text", time, mCurrentUserId);

                    Map messageUserMap = new HashMap();
                    messageUserMap.put(current_user_ref + "/" + push_id, msg1);
                    messageUserMap.put(chat_user_ref + "/" + push_id, msg1);

                    mRootReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {

                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.e("CHAT_ACTIVITY", "Cannot add message to database");
                            } else {

                                mRootReference.child("Users").child(mCurrentUserId).child("Chats").child(mChatUser).child("timestamp").setValue(time);
                                mRootReference.child("Users").child(mChatUser).child("Chats").child(mCurrentUserId).child("timestamp").setValue(time);
                                mRootReference.child("Users").child(mChatUser).child("Chats").child(mCurrentUserId).child("seen").setValue(false);
                                mRootReference.child("Users").child(mChatUser).child("Chats").child(mCurrentUserId).child("group").setValue(false);
                                mRootReference.child("Users").child(mCurrentUserId).child("Chats").child(mChatUser).child("group").setValue(false);
                                Toast.makeText(SingleChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                                mMessageView.setText("");
                            }

                        }
                    });


                }

            }
        });

        mChatAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String[] mimeTypes = {"image/jpeg", "video/*"};

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_MEDIA_PICKER);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_MEDIA_PICKER && resultCode == RESULT_OK) {

            Uri uri = data.getData();
            String mimeType = getContentResolver().getType(uri);

            final String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "Messages/" + mChatUser + "/" + mCurrentUserId;
            DatabaseReference user_message_push = mRootReference.child("Messages").child(mCurrentUserId).child(mChatUser).push();
            final String push_id = user_message_push.getKey();

            if (mimeType.equals("image/jpeg"))
            {
                StorageReference filepath = mStorage.child("message_images").child(uri.getLastPathSegment()+"/"+  new Date().getTime());
                filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful()) ;
                        String downloadUrl = urlTask.getResult().toString();

                        final long time = System.currentTimeMillis();

                        Map messageMap = new HashMap();
                        messageMap.put("content", downloadUrl);
                        messageMap.put("type", "image");
                        messageMap.put("time", time);
                        messageMap.put("from", mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mRootReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {

                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.e("CHAT_ACTIVITY", "Cannot add message to database");
                                } else {
                                    mRootReference.child("Users").child(mCurrentUserId).child("Chats").child(mChatUser).child("timestamp").setValue(time);
                                    mRootReference.child("Users").child(mChatUser).child("Chats").child(mCurrentUserId).child("timestamp").setValue(time);
                                    mRootReference.child("Users").child(mChatUser).child("Chats").child(mCurrentUserId).child("seen").setValue(false);
                                    mRootReference.child("Users").child(mChatUser).child("Chats").child(mCurrentUserId).child("group").setValue(false);
                                    mRootReference.child("Users").child(mCurrentUserId).child("Chats").child(mChatUser).child("group").setValue(false);
                                    Toast.makeText(SingleChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();

                                }

                            }
                        });
                    }
                });
            }
            else
                {
                StorageReference filepath = mStorage.child("message_videos").child(uri.getLastPathSegment()+"/"+  new Date().getTime());
                filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful()) ;
                        String downloadUrl = urlTask.getResult().toString();

                        final long time = System.currentTimeMillis();

                        Map messageMap = new HashMap();
                        messageMap.put("content", downloadUrl);
                        messageMap.put("type", "video");
                        messageMap.put("time", time);
                        messageMap.put("from", mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mRootReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {

                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.e("CHAT_ACTIVITY", "Cannot add message to database");
                                } else {
                                    mRootReference.child("Users").child(mCurrentUserId).child("Chats").child(mChatUser).child("timestamp").setValue(time);
                                    mRootReference.child("Users").child(mChatUser).child("Chats").child(mCurrentUserId).child("timestamp").setValue(time);
                                    mRootReference.child("Users").child(mChatUser).child("Chats").child(mCurrentUserId).child("seen").setValue(false);
                                    mRootReference.child("Users").child(mChatUser).child("Chats").child(mCurrentUserId).child("group").setValue(false);
                                    mRootReference.child("Users").child(mCurrentUserId).child("Chats").child(mChatUser).child("group").setValue(false);
                                    Toast.makeText(SingleChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();

                                }

                            }
                        });
                    }
                });

            }


        }


    }

    public class ConvViewHolder1 extends RecyclerView.ViewHolder {

        private View mView;


        public ConvViewHolder1(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setView(Messages message, final DatabaseReference mRef) {

            ImageView messageImageView = (ImageView) mView.findViewById(R.id.msgImage);
            TextView messageTextView = (TextView) mView.findViewById(R.id.msgText);
            VideoView messageVideoView = (VideoView) mView.findViewById(R.id.msgVideo);
            TextView messageTimeView = (TextView) mView.findViewById(R.id.msgTime);

            if (!message.getFrom().equals(mCurrentUserId)) {
                final TextView fromTextView = (TextView) mView.findViewById(R.id.msgFrom);
                mRootReference.child("Users").child(message.getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        fromTextView.setText(dataSnapshot.child("fullName").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(SingleChatActivity.this);
                    alertDialog.setTitle("Delete this message");
                    alertDialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mRef.removeValue();
                        }
                    });

                    alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    alertDialog.show();

                    return true;
                }
            });


            if (message.getType().equals("text")) {

                messageImageView.setVisibility(View.GONE);
                messageVideoView.setVisibility(View.GONE);
                messageTextView.setVisibility(View.VISIBLE);
                messageTextView.setText(message.getContent());

            } else if (message.getType().equals("image")) {

                messageImageView.setVisibility(View.VISIBLE);
                messageVideoView.setVisibility(View.GONE);
                messageTextView.setVisibility(View.GONE);
                Picasso.get().load(message.getContent()).placeholder(R.drawable.user_img).into(messageImageView);

            } else if (message.getType().equals("video")) {

                messageImageView.setVisibility(View.GONE);
                messageVideoView.setVisibility(View.VISIBLE);
                messageTextView.setVisibility(View.GONE);


                MediaController mc = new MediaController(SingleChatActivity.this);
                messageVideoView.setVideoURI(Uri.parse(message.getContent()));
                messageVideoView.setMediaController(mc);
                //messageVideoView.start();

            }

            Date dateObject = new Date(message.getTime());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
            messageTimeView.setText(timeFormat.format(dateObject));


        }
    }

    ;

    @Override
    protected void onStart() {
        super.onStart();
        Query query = mMessageReference.child(mCurrentUserId).child(mChatUser).orderByChild("time");
        FirebaseRecyclerOptions<Messages> options = new FirebaseRecyclerOptions.Builder<Messages>().setQuery(query, Messages.class).build();

        mMessageAdapter = new FirebaseRecyclerAdapter<Messages, ConvViewHolder1>(options) {


            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder1 holder, int position, @NonNull final Messages model) {
                holder.setView(model, getRef(position));
            }

            @NonNull
            @Override
            public ConvViewHolder1 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().from(parent.getContext()).inflate(R.layout.item_message_left, parent, false);
                if (viewType == 1) {
                    view = getLayoutInflater().from(parent.getContext()).inflate(R.layout.item_message_right, parent, false);
                }

                return new ConvViewHolder1(view);
            }

            @Override
            public int getItemViewType(int position) {

                Messages msg = getItem(position);
                if (msg.getFrom().equals(mCurrentUserId)) return 1;
                else return 2;
            }
        };


        mMessageAdapter.startListening();
        mMessagesList.setAdapter(mMessageAdapter);


    }

    @Override
    protected void onStop() {
        super.onStop();
        mMessageAdapter.stopListening();
    }
}
