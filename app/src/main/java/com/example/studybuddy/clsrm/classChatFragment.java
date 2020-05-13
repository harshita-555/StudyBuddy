package com.example.studybuddy.clsrm;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.studybuddy.R;
import com.example.studybuddy.group.Chats;
import com.example.studybuddy.group.GroupChatActivity;
import com.example.studybuddy.group.SingleChatActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class classChatFragment extends Fragment {

    private RecyclerView mConvList;
    private DatabaseReference mGroupDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mMessageDatabase;
    private FirebaseAuth mAuth;
    private Menu myMenu;

    private String occupation;

    private String mCurrent_user_id;

    private View mMainView;
    private ImageButton newChat;

    public classChatFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chat, container, false);
        newChat=(ImageButton) mMainView.findViewById(R.id.imageButton_newChat);
        newChat.setVisibility(View.GONE);
        mConvList = (RecyclerView) mMainView.findViewById(R.id.chatRecycleList);

        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mGroupDatabase = FirebaseDatabase.getInstance().getReference().child("Classroom");
        mGroupDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("Messages");


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        mUsersDatabase.child(mCurrent_user_id+ "/occupation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                occupation = dataSnapshot.getValue(String.class);
                if(!occupation.equals("Teacher"))
                {
                    MenuItem item = myMenu.findItem(R.id.menu_create_group);
                    item.setTitle("New Class");
                    item.setVisible(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = mUsersDatabase.child(mCurrent_user_id).child("class").orderByChild("timestamp");
        FirebaseRecyclerOptions<Chats> options = new FirebaseRecyclerOptions.Builder<Chats>().setQuery(query, Chats.class).build();

        FirebaseRecyclerAdapter<Chats, ConvViewHolder> friendsConvAdapter = new FirebaseRecyclerAdapter<Chats,ConvViewHolder>(options) {


            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder holder, int position, @NonNull final Chats model) {

                final String group_id = getRef(position).getKey();

                Query lastMessageQuery = mMessageDatabase.child(mCurrent_user_id).child(group_id).limitToLast(1);
                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        String data = dataSnapshot.child("content").getValue().toString();
                        holder.setMessage(data, model.isSeen());

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

                });

                if(model.isGroup()==true)
                {
                    mGroupDatabase.child(group_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final String groupName = dataSnapshot.child("name").getValue().toString();
                            String groupImg = dataSnapshot.child("image").getValue().toString();

                            holder.setName(groupName);
                            if(!groupImg.equals(""))holder.setProfilePic(groupImg);

                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent chatIntent = new Intent(getContext(), classChatActivity.class);
                                    chatIntent.putExtra("group_id", group_id);
                                    chatIntent.putExtra("name", groupName);
                                    startActivity(chatIntent);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else
                {
                    mUsersDatabase.child(group_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final String userName = dataSnapshot.child("fullName").getValue().toString();
                            String userImg = dataSnapshot.child("image").getValue().toString();

                            holder.setName(userName);
                            if(!userImg.equals(""))holder.setProfilePic(userImg);

                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent chatIntent = new Intent(getContext(), SingleChatActivity.class);
                                    chatIntent.putExtra("user_id", group_id);
                                    chatIntent.putExtra("name", userName);
                                    startActivity(chatIntent);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }




            }


            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_list_single_user, parent, false);
                return new ConvViewHolder(view);
            }
        };


        mConvList.setAdapter(friendsConvAdapter);
        friendsConvAdapter.startListening();
    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ConvViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setMessage(String message, boolean isSeen) {
            TextView userStatusView = (TextView) mView.findViewById(R.id.textViewSingleListStatus);
            userStatusView.setText(message);


            if (!isSeen) {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }

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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chats, menu);
        myMenu = menu;
        MenuItem item = menu.findItem(R.id.menu_create_group);
        item.setTitle("New Class");
        item=menu.findItem(R.id.menu_invite_friends);
        item.setTitle("Invite Others");
        return;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.menu_create_group)
        {
            startActivity(new Intent(getActivity(), FormNewClass.class));
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
}
