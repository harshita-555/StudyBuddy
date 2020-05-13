package com.example.studybuddy.sbj;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.studybuddy.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class AddFlashCard extends AppCompatActivity {

    String SubKey,DeckKey;
    ArrayList<FlashCard> flashcards;
    FlashCard flashCard1;
    private DatabaseReference mRef;
    private StorageReference mDeckStorageReference;
    int position;
    private int RC_PHOTO_PICKER1=1,RC_PHOTO_PICKER2=2;
    private EditText textFront,textBack;
    private ImageView picFront,picBack;
    private TextView imgFront,imgBack;
    private CardView cardFront,cardBack;
    private MenuItem menuEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flashcard);

        flashcards=getIntent().getParcelableArrayListExtra("flashcard");
        DeckKey=getIntent().getStringExtra("DeckId");;
        SubKey=getIntent().getStringExtra("SubId");
        position=getIntent().getIntExtra("add",0);
        flashCard1=flashcards.get(position);

        String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        mRef= FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Subjects").child(SubKey).child("decks").child(DeckKey).child("deck");
        mDeckStorageReference=FirebaseStorage.getInstance().getReference().child("decks");

        textFront=(EditText) findViewById(R.id.card_text_front);
        textFront.setText(flashCard1.getQuestion(), TextView.BufferType.EDITABLE);

        textBack=(EditText) findViewById(R.id.card_text_back);
        textBack.setText(flashCard1.getAns(), TextView.BufferType.EDITABLE);

        picFront=(ImageView) findViewById(R.id.card_image_front);
        imgFront=(TextView) findViewById(R.id.pic_front);
        if(!flashCard1.getQuestionUrl().equals(""))
        {
            Glide.with(this).load(flashCard1.getQuestionUrl()).transition(withCrossFade()).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(picFront);
            imgFront.setText("DELETE IMG");
        }
        else
        {
            picFront.setImageResource(R.drawable.add_photo_512);
            imgFront.setText("UPLOAD IMG");
        }

        picBack=(ImageView) findViewById(R.id.card_image_back);
        imgBack=(TextView) findViewById(R.id.pic_back);
        if(!flashCard1.getAnsUrl().equals(""))
        {
            Glide.with(this).load(flashCard1.getAnsUrl()).transition(withCrossFade()).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(picBack);
            imgBack.setText("DELETE IMG");
        }
        else {
            picBack.setImageResource(R.drawable.add_photo_512);
            imgBack.setText("UPLOAD IMG");
        }

        cardFront=(CardView) findViewById(R.id.card_layout_front);
        cardFront.setCardBackgroundColor(ContextCompat.getColor(AddFlashCard.this,flashCard1.getQuestionColor()));
        cardFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardFront.setCardBackgroundColor(getRandomColor(0));
            }
        });

        cardBack=(CardView) findViewById(R.id.card_layout_back);
        cardBack.setCardBackgroundColor(ContextCompat.getColor(AddFlashCard.this,flashCard1.getAnsColor()));
        cardBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardBack.setCardBackgroundColor(getRandomColor(1));
            }
        });
        imgFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imgFront.getText().equals("DELETE IMG"))
                {
                    picFront.setImageResource(R.drawable.add_photo_512);
                    imgFront.setText("UPLOAD IMG");
                }
                else
                {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER1);

                }
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imgBack.getText().equals("DELETE IMG"))
                {
                    picBack.setImageResource(R.drawable.add_photo_512);
                    imgBack.setText("UPLOAD IMG");
                }
                else
                {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER2);

                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER1 && resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();

            StorageReference photoRef = mDeckStorageReference.child(selectedImageUri.getLastPathSegment()+"/"+  new Date().getTime());
            photoRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();
                    flashCard1.setQuestionUrl(downloadUrl.toString());

                    Glide.with(AddFlashCard.this).load(flashCard1.getQuestionUrl()).transition(withCrossFade()).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(picFront);
                    imgFront.setText("DELETE IMG");

                }

            });
        }

        if (requestCode == RC_PHOTO_PICKER2 && resultCode == RESULT_OK) {

            Uri selectedImageUri = data.getData();

            StorageReference photoRef = mDeckStorageReference.child(selectedImageUri.getLastPathSegment()+"/"+  new Date().getTime());
            photoRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();
                    flashCard1.setAnsUrl(downloadUrl.toString());

                    Glide.with(AddFlashCard.this).load(flashCard1.getAnsUrl()).transition(withCrossFade()).thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.ALL).into(picBack);
                    imgBack.setText("DELETE IMG");

                }

            });
        }
    }

    public int getRandomColor(int m)
    {

        Random rand = new Random();
        int i = rand.nextInt(10);
        int magnitudeColorResourceId;
        switch (i) {
            case 0:
                magnitudeColorResourceId = R.color.color0;
                break;
            case 1:
                magnitudeColorResourceId = R.color.color1;
                break;
            case 2:
                magnitudeColorResourceId = R.color.color2;
                break;
            case 3:
                magnitudeColorResourceId = R.color.color3;
                break;
            case 4:
                magnitudeColorResourceId = R.color.color4;
                break;
            case 5:
                magnitudeColorResourceId = R.color.color5;
                break;
            case 6:
                magnitudeColorResourceId = R.color.color6;
                break;
            case 7:
                magnitudeColorResourceId = R.color.color7;
                break;
            case 8:
                magnitudeColorResourceId = R.color.color8;
                break;
            case 9:
                magnitudeColorResourceId = R.color.color9;
                break;
            default:
                magnitudeColorResourceId = R.color.color1;
                break;
        }
        if(m==0) flashCard1.setQuestionColor(magnitudeColorResourceId);
        else flashCard1.setAnsColor(magnitudeColorResourceId);
        int color= ContextCompat.getColor(AddFlashCard.this,magnitudeColorResourceId);
        return color;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.flashcard_menu, menu);
        menuEdit= menu.findItem(R.id.flashcard_menu_edit);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.flashcard_menu_edit)
        {
            flashCard1.setQuestion(textFront.getText().toString());
            flashCard1.setAns(textBack.getText().toString());
            flashcards.set(position, flashCard1);

            mRef.setValue(flashcards);
            Intent deckIntent = new Intent(AddFlashCard.this, FlashCardDetailActivity.class);
            deckIntent.putExtra("deckId", DeckKey);
            deckIntent.putExtra("SubId", SubKey);
            deckIntent.putExtra("name", getIntent().getStringExtra("name"));
            startActivity(deckIntent);
            finish();
        }

        return true;
    }
}
