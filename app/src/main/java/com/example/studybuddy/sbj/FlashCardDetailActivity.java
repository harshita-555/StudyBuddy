package com.example.studybuddy.sbj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.studybuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class FlashCardDetailActivity extends AppCompatActivity {

    static private DatabaseReference mDeckRef;
    private Deck deck;
    static private String title;
    private static ArrayList<FlashCard> flashcards;

    static String DeckKey;
    static String SubKey;

    static MyAdapter mAdapter;
    ViewPager mViewpager;
    private int position;
    private ImageButton mNext, mBack, mShuffle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_card);

        title= getIntent().getStringExtra("name");
        setTitle(title);
        DeckKey = getIntent().getStringExtra("deckId");
        SubKey = getIntent().getStringExtra("SubId");

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDeckRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Subjects").child(SubKey).child("decks").child(DeckKey);

        flashcards = new ArrayList<FlashCard>();
        mDeckRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                flashcards.clear();
                deck = dataSnapshot.getValue(Deck.class);
                if (deck.getCount() == 0) return;
                else
                {
                    for (DataSnapshot cards : dataSnapshot.child("deck").getChildren())
                        flashcards.add(cards.getValue(FlashCard.class));
                        mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mAdapter = new MyAdapter(getSupportFragmentManager());
        mViewpager = (ViewPager) findViewById(R.id.flashcard_detail_pager);
        mViewpager.setAdapter(mAdapter);



        mNext = findViewById(R.id.card_next);
        mBack = findViewById(R.id.card_back);
        mShuffle = findViewById(R.id.card_shuffle);

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewpager.getCurrentItem() == flashcards.size() - 1)
                    Toast.makeText(FlashCardDetailActivity.this, "no more flashcards..add more!", Toast.LENGTH_SHORT);
                else mViewpager.setCurrentItem(mViewpager.getCurrentItem() + 1);
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewpager.getCurrentItem() == 0)
                    Toast.makeText(FlashCardDetailActivity.this, "This is the first Flashcard!", Toast.LENGTH_SHORT);
                else mViewpager.setCurrentItem(mViewpager.getCurrentItem() - 1);
            }
        });

        mShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Collections.shuffle(flashcards);
                mAdapter.notifyDataSetChanged();

            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.flashcard_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        flashcards.add(new FlashCard());
        mDeckRef.child("count").setValue(flashcards.size());
        Intent addFlashCard = new Intent(FlashCardDetailActivity.this, AddFlashCard.class);
        addFlashCard.putParcelableArrayListExtra("flashcard", flashcards);
        addFlashCard.putExtra("DeckId", DeckKey);
        addFlashCard.putExtra("SubId", SubKey);
        addFlashCard.putExtra("add", flashcards.size()-1);
        addFlashCard.putExtra("name",title );
        startActivity(addFlashCard);
        finish();
        return true;
    }


    public static class MyAdapter extends FragmentStatePagerAdapter {
        public MyAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return ArrayListFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return flashcards.size();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }

    public static class ArrayListFragment extends Fragment {
         FlashCard flashcard1;
         int mNum;

        static ArrayListFragment newInstance(int num) {
            ArrayListFragment f = new ArrayListFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            args.putParcelable("flashcard", flashcards.get(num));
            f.setArguments(args);


            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mNum=getArguments().getInt("num");
                flashcard1 = (FlashCard) getArguments().getParcelable("flashcard");
                flashcard1.setBackSide(false);
            }

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_flashcard_detail, container, false);
            final CardView vcard = (CardView) v.findViewById(R.id.card_layout);
            final ImageView iv = v.findViewById(R.id.card_image);
            final TextView tv = v.findViewById(R.id.card_text);
            final Button vedit = v.findViewById(R.id.card_edit);
            final Button vdelete = v.findViewById(R.id.card_delete);

            if (flashcard1.getQuestionUrl().equals("")) iv.setVisibility(View.GONE);
            else {
                iv.setVisibility(View.VISIBLE);
                Glide.with(getContext()).load(flashcard1.getQuestionUrl()).transition(withCrossFade()).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(iv);
            }

            if (!flashcard1.getQuestion().equals("")) tv.setText(flashcard1.getQuestion());
            else tv.setVisibility(View.GONE);

            vcard.setCardBackgroundColor(ContextCompat.getColor(getContext(),flashcard1.getQuestionColor()));
            flashcard1.setBackSide(false);

            vcard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (flashcard1.isBackSide() == false) {
                        if (flashcard1.getAnsUrl().equals("")) iv.setVisibility(View.GONE);
                        else {
                            iv.setVisibility(View.VISIBLE);
                            Glide.with(getContext()).load(flashcard1.getAnsUrl()).transition(withCrossFade()).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(iv);
                        }

                        if (!flashcard1.getAns().equals("")) tv.setText(flashcard1.getAns());
                        else tv.setVisibility(View.GONE);


                        vcard.setCardBackgroundColor(ContextCompat.getColor(getContext(),flashcard1.getAnsColor()));
                        flashcard1.setBackSide(true);

                    } else {
                        if (flashcard1.getQuestionUrl().equals("")) iv.setVisibility(View.GONE);
                        else {
                            iv.setVisibility(View.VISIBLE);
                            Glide.with(getContext()).load(flashcard1.getQuestionUrl()).transition(withCrossFade()).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(iv);
                        }

                        if (!flashcard1.getQuestion().equals(""))
                            tv.setText(flashcard1.getQuestion());
                        else tv.setVisibility(View.GONE);

                        vcard.setCardBackgroundColor(ContextCompat.getColor(getContext(),flashcard1.getQuestionColor()));
                        flashcard1.setBackSide(false);

                    }
                }
            });

            vedit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent addFlashCard = new Intent(getActivity(), AddFlashCard.class);
                    addFlashCard.putParcelableArrayListExtra("flashcard", flashcards);
                    addFlashCard.putExtra("DeckId", DeckKey);
                    addFlashCard.putExtra("SubId", SubKey);
                    addFlashCard.putExtra("add", mNum);
                    addFlashCard.putExtra("name",title );
                    startActivity(addFlashCard);
                    getActivity().finish();
                }
            });

            vdelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flashcards.remove(mNum);
                    mDeckRef.child("count").setValue(flashcards.size());
                    if(flashcards.size()==0);
                    mDeckRef.child("deck").setValue(flashcards);
                    mAdapter.notifyDataSetChanged();


                }
            });
            return v;
        }


    }



}
