package com.example.studybuddy.sbj;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SubjectsActivity extends AppCompatActivity {

    private RecyclerView mSubjectsList;
    static private Button mChange;
    static private EditText mSubjectname;
    private ImageButton mNewSubject;
    static private DatabaseReference mUserReference;
    static private FirebaseUser user;
    private FirebaseRecyclerAdapter<Subjects, SubjectsViewHolder> firebaseRecyclerAdapter;
    private DatabaseReference mUserRef;
    private String occupation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects);


        mSubjectname = (EditText) findViewById(R.id.subjects_name_edit_text);
        mNewSubject = (ImageButton) findViewById(R.id.add_subjects_name);
        mSubjectsList = (RecyclerView) findViewById(R.id.subjects_recycler_view);
        mChange = (Button) findViewById(R.id.change_ass2);

        mSubjectname.setVisibility(View.GONE);
        mChange.setVisibility(View.GONE);

        mSubjectsList.setHasFixedSize(true);
        mSubjectsList.setLayoutManager(new LinearLayoutManager(this));

        user = FirebaseAuth.getInstance().getCurrentUser();
        String Uid = user.getUid();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(Uid).child("Subjects");

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






        mNewSubject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubjectname.setVisibility(View.VISIBLE);
                mChange.setVisibility(View.VISIBLE);
                mChange.setText("ADD");
                mSubjectname.setText("", TextView.BufferType.EDITABLE);
                mChange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (user != null && !mSubjectname.getText().toString().trim().equals("")) {

                            Subjects sub1= new Subjects(mSubjectname.getText().toString().trim());

                            mUserReference.push().setValue(sub1)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                               // mUserReference.child(key).child("SubId").setValue(key);
                                                Toast.makeText(SubjectsActivity.this, "New subject added!", Toast.LENGTH_SHORT).show();
                                                mSubjectname.setVisibility(View.GONE);
                                                mChange.setVisibility(View.GONE);
                                            } else {
                                                Toast.makeText(SubjectsActivity.this, "Failed to add..try Again!", Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });
                        } else if (mSubjectname.getText().toString().trim().equals("")) {
                            mSubjectname.setError("Enter subject name");

                        }
                    }
                });

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

   public  class SubjectsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public SubjectsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;



                }
        public void selectSubject(final DatabaseReference mRef){
            final TextView nxtSubject = (TextView) mView.findViewById(R.id.subjects_name);
            nxtSubject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent subjectIntent= new Intent(SubjectsActivity.this, SubjectActivity.class);
                    subjectIntent.putExtra("SubId", mRef.getKey());
                    subjectIntent.putExtra("name", nxtSubject.getText());
                    startActivity(subjectIntent);
                    finish();

                }
            });

        }


        public void showMenu(final DatabaseReference mRef)
        {
            ImageButton optmenu = (ImageButton) mView.findViewById(R.id.subjects_menu);

            optmenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View itemView) {
                    mSubjectname.setVisibility(View.GONE);
                    mChange.setVisibility(View.GONE);

                    PopupMenu popup = new PopupMenu(itemView.getContext(), itemView);
                    popup.getMenuInflater().inflate(R.menu.subjects_menu, popup.getMenu());
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.edit_name:
                                    mSubjectname.setVisibility(View.VISIBLE);
                                    mChange.setVisibility(View.VISIBLE);
                                    mChange.setText("UPDATE");
                                    mSubjectname.setText("", TextView.BufferType.EDITABLE);
                                    mChange.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            if (user != null && !mSubjectname.getText().toString().trim().equals("")) {

                                                mRef.child("name").setValue(mSubjectname.getText().toString().trim())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                    mSubjectname.setVisibility(View.GONE);
                                                                    mChange.setVisibility(View.GONE);
                                                                }
                                                            }
                                                        });
                                            } else if (mSubjectname.getText().toString().trim().equals("")) {
                                                mSubjectname.setError("Enter subject name");

                                            }
                                        }
                                    });

                                    return true;
                                case R.id.delete_subject:
                                    mRef.removeValue();
                                    return true;
                                default:
                                    return false;

                            }
                        }
                    });
                }
            });


        }

        public void setText(Subjects sub) {
            TextView sbjName = (TextView) mView.findViewById(R.id.subjects_name);
            sbjName.setText(sub.getName());

        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Subjects> options = new FirebaseRecyclerOptions.Builder<Subjects>().setQuery(mUserReference.orderByValue(), Subjects.class).build();

       // Log.w("Subjects Activity","daaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaamn22222");
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Subjects, SubjectsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SubjectsViewHolder holder, int position, @NonNull Subjects model) {
                holder.setText(model);
                holder.showMenu( getRef(position));
                holder.selectSubject(getRef(position));
            }


            @NonNull
            @Override
            public SubjectsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subjects_list_item, parent, false);
                return new SubjectsViewHolder(view);
            }
        };


        mSubjectsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }
}
