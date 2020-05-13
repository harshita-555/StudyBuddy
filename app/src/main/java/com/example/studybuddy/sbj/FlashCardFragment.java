package com.example.studybuddy.sbj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;


public class FlashCardFragment extends BaseFragment {
    private DatabaseReference mDatabaseReference;
    private FirebaseUser user;
    private RecyclerView mConvList;
    private FirebaseRecyclerAdapter<Deck, FlashCardFragment.DeckViewHolder> mAdapter;


    private ImageButton mDeck;
    private String subjectKey;
    static private Button mChange;
    static private EditText mDeckname;


    private View mMainView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();
        String Uid = user.getUid();
        subjectKey = getActivity().getIntent().getStringExtra("SubId");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(Uid).child("Subjects").child(subjectKey).child("decks");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_flash_card, container, false);
        mConvList = (RecyclerView) mMainView.findViewById(R.id.deck_recycler_view);

        mDatabaseReference.keepSynced(true);

        mDeckname = (EditText) mMainView.findViewById(R.id.deck_name_edit_text);
        mChange = (Button) mMainView.findViewById(R.id.change_ass3);

        mDeckname.setVisibility(View.GONE);
        mChange.setVisibility(View.GONE);


        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        mDeck = (ImageButton) mMainView.findViewById(R.id.add_deck);
        mDeck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mDeckname.setVisibility(View.VISIBLE);
                mChange.setVisibility(View.VISIBLE);
                mChange.setText("ADD");
                mDeckname.setText("", TextView.BufferType.EDITABLE);
                mChange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (user != null && !mDeckname.getText().toString().trim().equals("")) {

                            Deck deck1 = new Deck(mDeckname.getText().toString().trim());
                            mDatabaseReference.push().setValue(deck1)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // mUserReference.child(key).child("SubId").setValue(key);
                                                Toast.makeText(getContext(), "New deck added!", Toast.LENGTH_SHORT).show();
                                                mDeckname.setVisibility(View.GONE);
                                                mChange.setVisibility(View.GONE);
                                            } else {
                                                Toast.makeText(getContext(), "Failed to add..try Again!", Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });
                        } else if (mDeckname.getText().toString().trim().equals("")) {
                            mDeckname.setError("Enter some name");

                        }
                    }
                });
            }
        });

        return mMainView;
    }






    public class DeckViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setView(final Deck deck1) {
            TextView deckName = (TextView) mView.findViewById(R.id.deck_name);
            deckName.setText(deck1.getTitle());

        }

        public void selectDeck(final DatabaseReference mRef) {
            final TextView nxtDeck = (TextView) mView.findViewById(R.id.deck_name);
            nxtDeck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent deckIntent = new Intent(getActivity(), FlashCardDetailActivity.class);
                    deckIntent.putExtra("deckId", mRef.getKey());
                    deckIntent.putExtra("SubId", subjectKey);
                    deckIntent.putExtra("name", nxtDeck.getText());
                    startActivity(deckIntent);
                   // getActivity().finish();

                }
            });

        }

        public void setMenu(final DatabaseReference mRef) {

            ImageButton optmenu = (ImageButton) mView.findViewById(R.id.deck_menu);

            optmenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View itemView) {
                    mDeckname.setVisibility(View.GONE);
                    mChange.setVisibility(View.GONE);

                    PopupMenu popup = new PopupMenu(itemView.getContext(), itemView);
                    popup.getMenuInflater().inflate(R.menu.subjects_menu, popup.getMenu());
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.edit_name:
                                    mDeckname.setVisibility(View.VISIBLE);
                                    mChange.setVisibility(View.VISIBLE);
                                    mChange.setText("UPDATE");
                                    mDeckname.setText("", TextView.BufferType.EDITABLE);
                                    mChange.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            if (user != null && !mDeckname.getText().toString().trim().equals("")) {

                                                mRef.child("name").setValue(mDeckname.getText().toString().trim())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                    mDeckname.setVisibility(View.GONE);
                                                                    mChange.setVisibility(View.GONE);
                                                                }
                                                            }
                                                        });
                                            } else if (mDeckname.getText().toString().trim().equals("")) {
                                                mDeckname.setError("Enter subject name");

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
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Deck> options = new FirebaseRecyclerOptions.Builder<Deck>().setQuery(mDatabaseReference, Deck.class).build();

        mAdapter = new FirebaseRecyclerAdapter<Deck, FlashCardFragment.DeckViewHolder>(options) {
            @NonNull
            @Override
            public FlashCardFragment.DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deck_list_item, parent, false);
                return new FlashCardFragment.DeckViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FlashCardFragment.DeckViewHolder holder, int position, @NonNull Deck model) {

                holder.setView(model);
                holder.selectDeck(getRef(position));
                holder.setMenu(getRef(position));


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


        @Override
        public boolean onBackPressed() {

            Log.w("hfiughi","pppppppppppppppppppppppppppp");
            if (mChange.getVisibility()==View.VISIBLE) {
                Log.w("hfiughi","pppppppppppppppppppppppppppp");
                mDeckname.setVisibility(View.GONE);
                mChange.setVisibility(View.GONE);
                return true;
            } else {
                return false;
            }
        }


}

class BaseFragment extends Fragment {

    public boolean onBackPressed() {
        return false;
    }
}


