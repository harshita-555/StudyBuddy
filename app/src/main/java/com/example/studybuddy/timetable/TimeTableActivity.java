package com.example.studybuddy.timetable;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.studybuddy.R;
import com.example.studybuddy.StudentActivity;
import com.example.studybuddy.TeacherActivity;
import com.example.studybuddy.User;
import com.example.studybuddy.WorkActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TimeTableActivity extends AppCompatActivity {

    private RecyclerView mTasksList;
    private ImageButton mAdd;
    private DatabaseReference mUserReference;
    private FirebaseUser user;
    private FirebaseRecyclerAdapter<Task, TasksViewHolder> firebaseRecyclerAdapter;
    private DatabaseReference mUserRef;
    private String occupation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_docs);

        mTasksList = (RecyclerView) findViewById(R.id.list_docs);
        mAdd = (ImageButton) findViewById(R.id.icon_docs);

        mTasksList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager =(new LinearLayoutManager(this));
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mTasksList.setLayoutManager(linearLayoutManager);

        user = FirebaseAuth.getInstance().getCurrentUser();
        String Uid = user.getUid();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(Uid).child("Tasks");

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

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(TimeTableActivity.this, NewTask.class));
            }
        });


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

    public  class TasksViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public TasksViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void showMenu(final DatabaseReference mRef)
        {
            mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenu popup = new PopupMenu(itemView.getContext(), itemView);
                    popup.getMenuInflater().inflate(R.menu.media_menu, popup.getMenu());
                    popup.getMenu().findItem(R.id.share_media).setVisible(false);
                    popup.setGravity(Gravity.RIGHT);
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {

                                case R.id.delete_media:
                                    mRef.removeValue();
                                    Toast.makeText(TimeTableActivity.this, "deleted!", Toast.LENGTH_SHORT);
                                    return true;
                                default:
                                    return false;


                            }
                        }
                    });
                    return false;
                }


            });

        }


        public void setView(final Task task, final String key) {
            TextView taskName = (TextView) mView.findViewById(R.id.task_name);
            TextView taskMsg = (TextView) mView.findViewById(R.id.task_msg);
            TextView taskDate = (TextView) mView.findViewById(R.id.task_date);

            taskName.setText(task.getName());
            taskMsg.setText(task.getMessage());
            taskDate.setText(task.getDeadline());

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent send = new Intent(TimeTableActivity.this,NewTask.class);
                    send.putExtra("todo",task);
                    send.putExtra("Key", key);
                    startActivity(send);

                }
            });

        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Task> options = new FirebaseRecyclerOptions.Builder<Task>().setQuery(mUserReference.orderByChild("preference"), Task.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Task, TasksViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull TasksViewHolder holder, int position, @NonNull Task model) {
                holder.setView(model,getRef(position).getKey());
                holder.showMenu( getRef(position));

            }


            @NonNull
            @Override
            public TasksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_task_item, parent, false);
                return new TasksViewHolder(view);
            }
        };


        mTasksList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }


    public class Time{
        int days , hours , minutes;

        public Time(int time)
        {
            minutes = (int) time%60;
            time=time/60;
            hours = time % 24;
            time=time/24;
            days = time;
        }

    }

}
