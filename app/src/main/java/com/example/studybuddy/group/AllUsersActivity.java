package com.example.studybuddy.group;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.studybuddy.R;
import com.example.studybuddy.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabaseReference;
    private FirebaseRecyclerAdapter<User, UserViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mUsersList=(RecyclerView)findViewById(R.id.recyclerViewUsersList);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        mUsersDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabaseReference.keepSynced(true);
    }


    @Override
    protected void onStart() {
        super.onStart();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setQuery(mUsersDatabaseReference.orderByChild("fullName"), User.class).build();
        firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_list_single_user, parent, false);
                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final UserViewHolder holder, int position, @NonNull final User model) {

                if(uid.equals(model.getUserId()))  holder.setName("You",model.getOccupation());
                else holder.setName(model.getFullName(),model.getOccupation());

                if(!model.getImage().equals(""))holder.setImage(model.getImage());
                final String user_id=getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!user_id.equals(uid)){

                        mUsersDatabaseReference.child(uid).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                boolean flag = true;

                                for(DataSnapshot friend: dataSnapshot.getChildren())
                                {
                                    if(friend.getValue().equals(user_id))
                                    {
                                        flag=false;
                                        Intent chatIntent = new Intent(AllUsersActivity.this, SingleChatActivity.class);
                                        chatIntent.putExtra("user_id", user_id);
                                        chatIntent.putExtra("name", model.getFullName());
                                        startActivity(chatIntent);
                                        finish();
                                    }

                                }

                                if(flag==true)
                                {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(AllUsersActivity.this);
                                    alertDialog.setTitle("Friend Request");
                                    alertDialog.setMessage("Send request to "+ model.getFullName());

                                    alertDialog.setPositiveButton("Yes",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {


                                                    Map requestMap = new HashMap();
                                                    requestMap.put("friend_request/"+uid+ "/"+user_id + "/request_type","personal_sent");
                                                    requestMap.put("friend_request/"+user_id+"/"+uid+"/request_type","personal_received");

                                                    FirebaseDatabase.getInstance().getReference().updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            if(databaseError==null){

                                                                Toast.makeText(AllUsersActivity.this, "Friend Request sent successfully", Toast.LENGTH_SHORT).show();

                                                            }
                                                            else{

                                                                Toast.makeText(AllUsersActivity.this, "Some error in sending friend Request", Toast.LENGTH_SHORT).show();
                                                            }

                                                        }
                                                    });
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

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });}


                    }
                });

            }

        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chats, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.menu_create_group)
        {
            startActivity(new Intent(AllUsersActivity.this, FormNewGroup.class));
            finish();
            return true;


        }
        else if(item.getItemId()==R.id.menu_invite_friends)
        {
            //yet to be written
            return true;
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public UserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name,String occupation) {
            TextView userNameView=(TextView)mView.findViewById(R.id.textViewSingleListName);
            userNameView.setText(name);
            TextView userOccView=(TextView)mView.findViewById(R.id.textViewSingleListStatus);
            userOccView.setText(occupation);

        }

        public void setImage(String image) {
            CircleImageView userImageView = (CircleImageView)mView.findViewById(R.id.circleImageViewUserImage);
            Picasso.get().load(image).placeholder(R.drawable.user_img).into(userImageView);
        }
    }

}
