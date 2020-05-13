package com.example.studybuddy.sbj;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.studybuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NoteDetailActivity extends AppCompatActivity {

    private DatabaseReference mNotesRef;
    TextView titleView,contentView;
    EditText titleEdit,contentEdit;
    String title,content;
    private MenuItem mEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getIntent().getStringExtra("name"));
        setContentView(R.layout.activity_note_detail);


       titleView=(TextView) findViewById(R.id.notes_title_detail);
       contentView=(TextView) findViewById(R.id.notes_content_detail);
       titleEdit=(EditText) findViewById(R.id.notes_title_edit);
       contentEdit=(EditText) findViewById(R.id.notes_content_edit);

       titleEdit.setVisibility(View.GONE);
       contentEdit.setVisibility(View.GONE);
       titleView.setVisibility(View.VISIBLE);
       contentView.setVisibility(View.VISIBLE);

        String notesKey=getIntent().getStringExtra("NotesId");
        String subKey=getIntent().getStringExtra("SubId");
        String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();

       mNotesRef= FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Subjects").child(subKey).child("notes").child(notesKey);

       mNotesRef.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               noteStore note=dataSnapshot.getValue(noteStore.class);
               titleView.setText(note.getTitle());
               contentView.setText(note.getContent());
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

       titleView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               title=titleView.getText().toString();
               titleView.setVisibility(View.GONE);
               titleEdit.setVisibility(View.VISIBLE);
               titleEdit.setText(title, TextView.BufferType.EDITABLE);
               mEdit.setTitle("SAVE");

               if(contentEdit.getVisibility()==View.VISIBLE)
               {
                   content=contentEdit.getText().toString();
                   mNotesRef.child("content").setValue(content);
                   contentEdit.setVisibility(View.GONE);
                   contentView.setVisibility(View.VISIBLE);
                   contentView.setText(content);

               }


           }
       });
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content=contentView.getText().toString();
                contentView.setVisibility(View.GONE);
                contentEdit.setVisibility(View.VISIBLE);
                contentEdit.setText(content, TextView.BufferType.EDITABLE);
                mEdit.setTitle("SAVE");

                if(titleEdit.getVisibility()==View.VISIBLE)
                {
                    title=titleEdit.getText().toString();
                    mNotesRef.child("title").setValue(title);
                    titleEdit.setVisibility(View.GONE);
                    titleView.setVisibility(View.VISIBLE);
                    titleView.setText(title);
                }

            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.notes_menu,menu);
        mEdit = menu.findItem(R.id.notes_edit);
        mEdit.setTitle("EDIT");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.notes_delete)
        {
            mNotesRef.removeValue();
            finish();
        }

        if(item.getItemId()==R.id.notes_edit)
        {
            if(mEdit.getTitle().equals("EDIT"))
            {
                title=titleView.getText().toString();
                titleView.setVisibility(View.GONE);
                titleEdit.setVisibility(View.VISIBLE);
                titleEdit.setText(title, TextView.BufferType.EDITABLE);

                mEdit.setTitle("SAVE");
            }
            else
            {
                if(titleEdit.getVisibility()==View.VISIBLE)
                {
                    title=titleEdit.getText().toString();
                    mNotesRef.child("title").setValue(title);
                    titleEdit.setVisibility(View.GONE);
                    titleView.setVisibility(View.VISIBLE);
                    titleView.setText(title);
                }
                if(contentEdit.getVisibility()==View.VISIBLE)
                {
                    content=contentEdit.getText().toString();
                    mNotesRef.child("content").setValue(content);
                    contentEdit.setVisibility(View.GONE);
                    contentView.setVisibility(View.VISIBLE);
                    contentView.setText(content);
                }
                mEdit.setTitle("EDIT");

            }
        }
        return true;
    }



}
