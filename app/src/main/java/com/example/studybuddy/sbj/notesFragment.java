package com.example.studybuddy.sbj;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studybuddy.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class notesFragment extends Fragment {
    private DatabaseReference mDatabaseReference;
    private FirebaseUser user;
    private RecyclerView mConvList;
    private FirebaseRecyclerAdapter<noteStore, notesFragment.NoteViewHolder> mAdapter;



    private ImageButton mDocs;
    private String subjectKey;

    private View mMainView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_docs, container, false);
        mConvList = (RecyclerView)  mMainView.findViewById(R.id.list_docs);

        mDatabaseReference.keepSynced(true);


        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        mDocs=(ImageButton) mMainView.findViewById(R.id.icon_docs);
        mDocs.setBackgroundResource(R.drawable.round_button2);

        mDocs.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

             mDatabaseReference.push().setValue(new noteStore("Untitled",""));


            }
        });

        return mMainView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user= FirebaseAuth.getInstance().getCurrentUser();
        String Uid= user.getUid();
        subjectKey=getActivity().getIntent().getStringExtra("SubId");
        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(Uid).child("Subjects").child(subjectKey).child("notes");

    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;

        }


        public void setView(final noteStore notes1,final DatabaseReference mref)
        {



            TextView titleView=(TextView) mView.findViewById(R.id.notes_title);
            titleView.setText(notes1.getTitle());

            TextView contentView =(TextView) mView.findViewById(R.id.notes_content);
            contentView.setText(notes1.getContent());

            LinearLayout mLayout= (LinearLayout) mView.findViewById(R.id.notes_card_view);
            mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   // same hi hoga almost!
                    Intent viewNotes= new Intent(getActivity(), NoteDetailActivity.class);
                    viewNotes.putExtra("name", notes1.getTitle());
                    viewNotes.putExtra("SubId", subjectKey);
                    viewNotes.putExtra("NotesId",mref.getKey());
                    startActivity(viewNotes);

                }
            });
           mView.setOnLongClickListener(new View.OnLongClickListener() {
               @Override
               public boolean onLongClick(View v) {
                   PopupMenu popup = new PopupMenu(itemView.getContext(), itemView);
                   popup.getMenuInflater().inflate(R.menu.media_menu, popup.getMenu());
                   popup.show();
                   popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                       @Override
                       public boolean onMenuItemClick(MenuItem item) {
                           switch (item.getItemId()) {
                               case R.id.share_media:

                                   //yet to written..............................................................................................


                                   return true;
                               case R.id.delete_media:
                                   mref.removeValue();
                                   Toast.makeText(getContext(), "deleted!", Toast.LENGTH_SHORT);
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

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<noteStore> options = new FirebaseRecyclerOptions.Builder<noteStore>().setQuery(mDatabaseReference,noteStore.class).build();

        mAdapter = new FirebaseRecyclerAdapter<noteStore, notesFragment.NoteViewHolder>(options) {
            @NonNull
            @Override
            public notesFragment.NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_list_item, parent, false);
                return new notesFragment.NoteViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull notesFragment.NoteViewHolder holder, int position, @NonNull noteStore model) {


                holder.setView(model,getRef(position));



            }
        };



        mConvList.setAdapter(mAdapter);
        mAdapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }


}
