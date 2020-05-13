package com.example.studybuddy.group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestFragment extends Fragment {

    private RecyclerView mReqList;
    private ImageButton newRequest;

    private List<String> requestList = new ArrayList<>();

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mGroupDatabase;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private View mMainView;

    private RequestAdapter mRequestAdapter;



    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chat, container, false);
        mReqList = (RecyclerView)mMainView.findViewById(R.id.chatRecycleList);
        newRequest=(ImageButton) mMainView.findViewById(R.id.imageButton_newChat);
        mAuth= FirebaseAuth.getInstance();

        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("friend_request");
        mDatabaseReference.keepSynced(true);
        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        mGroupDatabase=FirebaseDatabase.getInstance().getReference().child("Groups");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mReqList.setHasFixedSize(true);
        mReqList.setLayoutManager(linearLayoutManager);

        requestList.clear();
        mRequestAdapter = new RequestAdapter(requestList);
        mReqList.setAdapter(mRequestAdapter);

        mDatabaseReference.child(mCurrentUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String userId = dataSnapshot.getKey();
                requestList.add(userId);
                mRequestAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String userId = dataSnapshot.getKey();
                requestList.remove(userId);
                mRequestAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();
        newRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AllUsersActivity.class));


            }
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chats, menu);
        return;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.menu_create_group)
        {
            startActivity(new Intent(getActivity(), FormNewGroup.class));
            getActivity().finish();
            return true;


        }
        else if(item.getItemId()==R.id.menu_invite_friends)
        {
            //yet to be written
            return true;
        }
        return false;
    }

    public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder>{

        private List<String> requestList;

        private Context ctx;

        public RequestAdapter(List<String> requestList) {
            this.requestList = requestList;
        }

        @Override
        public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request,parent,false);
            return new RequestAdapter.RequestViewHolder(view);

        }

        public class RequestViewHolder extends RecyclerView.ViewHolder {

            public TextView displayName;
            public Button positive,negative;
            public CircleImageView displayImage;
            public ImageView imageView;

            public RequestViewHolder(View itemView) {
                super(itemView);

                ctx = itemView.getContext();

                displayName = (TextView)itemView.findViewById(R.id.RequestName);
                displayImage = (CircleImageView)itemView.findViewById(R.id.RequestImage);
                positive = (Button) itemView.findViewById(R.id.positive_response);
                negative = (Button) itemView.findViewById(R.id.negative_response);
            }

            public void setUserDetails(String userId)
            {
                mUsersDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userName = dataSnapshot.child("fullName").getValue().toString();
                        String userImage = dataSnapshot.child("image").getValue().toString();

                        displayName.setText(userName);
                        if(!userImage.equals("")) Picasso.get().load(userImage).placeholder(R.drawable.user_img).into(displayImage);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            public void setGroupDetails(String userId)
            {
                mGroupDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userName = dataSnapshot.child("name").getValue().toString();
                        String userImage = dataSnapshot.child("image").getValue().toString();

                        displayName.setText(userName);
                        if(!userImage.equals("")) Picasso.get().load(userImage).placeholder(R.drawable.user_img).into(displayImage);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

        @Override
        public void onBindViewHolder(final RequestViewHolder holder, final int position) {

            final String user_id = requestList.get(position);

            mDatabaseReference.child(mCurrentUserId).child(user_id).child("request_type").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.getValue().toString().equals("personal_received"))
                    {
                        holder.setUserDetails(user_id);
                        holder.positive.setText("accept");
                        holder.positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mUsersDatabase.child(mCurrentUserId+"/friends").push().setValue(user_id);
                                mUsersDatabase.child(user_id+"/friends").push().setValue(mCurrentUserId);
                                mDatabaseReference.child(mCurrentUserId).child(user_id).removeValue();
                                mDatabaseReference.child(user_id).child(mCurrentUserId).removeValue();
                                requestList.remove(user_id);
                                mRequestAdapter.notifyDataSetChanged();

                                long time = System.currentTimeMillis();

                                Map chatAddMap = new HashMap();
                                chatAddMap.put("seen", true);
                                chatAddMap.put("timestamp", time);
                                chatAddMap.put("group", false);

                                Map chatUserMap = new HashMap();
                                 chatUserMap.put( user_id + "/Chats/" + mCurrentUserId, chatAddMap);
                                 chatUserMap.put(mCurrentUserId + "/Chats/" + user_id, chatAddMap);

                                mUsersDatabase.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            Toast.makeText(getActivity(), "Successfully Added new chat", Toast.LENGTH_SHORT).show();
                                        } else
                                            Toast.makeText(getActivity(), "Cannot Add chat", Toast.LENGTH_SHORT).show();
                                    }


                                });









                            }
                        });
                        holder.negative.setText("reject");
                        holder.negative.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDatabaseReference.child(mCurrentUserId).child(user_id).removeValue();
                                mDatabaseReference.child(user_id).child(mCurrentUserId).removeValue();
                                requestList.remove(user_id);
                                mRequestAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                    else if(dataSnapshot.getValue().toString().equals("personal_sent"))
                    {
                        holder.setUserDetails(user_id);
                        holder.positive.setText("sent");
                        holder.negative.setText("unsend");
                        holder.negative.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                mDatabaseReference.child(mCurrentUserId).child(user_id).removeValue();
                                mDatabaseReference.child(user_id).child(mCurrentUserId).removeValue();
                                requestList.remove(user_id);
                                mRequestAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                    else
                    {
                        holder.setGroupDetails(user_id);
                        holder.positive.setText("join");
                        holder.positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                mDatabaseReference.child(mCurrentUserId).child(user_id).removeValue();
                                Intent chatIntent = new Intent(getContext(), GroupChatActivity.class);
                                chatIntent.putExtra("group_id", user_id);
                                chatIntent.putExtra("name", holder.displayName.getText().toString());
                                startActivity(chatIntent);
                                getActivity().finish();

                            }
                        });
                        holder.negative.setText("decline");
                        holder.negative.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                mDatabaseReference.child(mCurrentUserId).child(user_id).removeValue();
                                requestList.remove(user_id);
                                mRequestAdapter.notifyDataSetChanged();

                            }
                        });
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent viewGroup = new Intent(getActivity(), GroupInfoActivity.class);
                                viewGroup.putExtra("code", 0);
                                viewGroup.putExtra("group_id", user_id);
                                startActivity(viewGroup);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        @Override
        public int getItemCount() {
            return requestList.size();
        }

    }


}

