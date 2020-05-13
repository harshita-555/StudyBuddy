package com.example.studybuddy.quizzes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studybuddy.R;
import com.example.studybuddy.User;
import com.example.studybuddy.group.AllUsersActivity;
import com.example.studybuddy.group.FormNewGroup;
import com.example.studybuddy.group.SingleChatActivity;
import com.example.studybuddy.sbj.Subjects;
import com.example.studybuddy.sbj.SubjectsActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class QuizResult extends AppCompatActivity {

    private RecyclerView mUsersList;
    private DatabaseReference mReference;
    private FirebaseRecyclerAdapter<Integer, UserViewHolder> firebaseRecyclerAdapter;
    private  String quizKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        setTitle(getIntent().getStringExtra("Name"));

        mUsersList=(RecyclerView)findViewById(R.id.recyclerViewUsersList);
        mUsersList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        mUsersList.setLayoutManager(linearLayoutManager);

        quizKey = getIntent().getStringExtra("Key");

        mReference= FirebaseDatabase.getInstance().getReference();
        mReference.keepSynced(true);
    }


    @Override
    protected void onStart() {
        super.onStart();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        FirebaseRecyclerOptions<Integer> options = new FirebaseRecyclerOptions.Builder<Integer>().setQuery(mReference.child("Results/"+quizKey).orderByValue(), Integer.class).build();
        firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Integer, UserViewHolder>(options) {

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.quiz_list_item, parent, false);
                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final UserViewHolder holder, int position, @NonNull final Integer model) {

                String mUserId = getRef(position).getKey();
                holder.setView(mUserId, model);


            }

        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public UserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setView(final String mUserId, final Integer marks)
        {
            mReference.child("Users/"+mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    TextView tv1 = mView.findViewById(R.id.quiz_item_name);
                    tv1.setText(user.getFullName());

                    Button btn = mView.findViewById(R.id.quiz_item_button);
                    btn.setText(marks.toString());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


    }

}
