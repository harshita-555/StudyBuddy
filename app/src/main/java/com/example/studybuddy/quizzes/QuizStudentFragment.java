package com.example.studybuddy.quizzes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.studybuddy.R;
import com.example.studybuddy.clsrm.ShareTestActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class QuizStudentFragment extends Fragment {

    private DatabaseReference mDatabaseReference;
    private FirebaseUser user;
    private RecyclerView mConvList;
    private FirebaseRecyclerAdapter<Quiz, QuizViewHolder> mAdapter;
    private ImageButton mDocs;
    private View mMainView;

    public QuizStudentFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user= FirebaseAuth.getInstance().getCurrentUser();
        String Uid= user.getUid();
        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Users/"+ Uid +"/Tests");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_docs, container, false);
        mConvList = (RecyclerView)  mMainView.findViewById(R.id.list_docs);

        mDatabaseReference.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        mDocs=(ImageButton) mMainView.findViewById(R.id.icon_docs);
        mDocs.setVisibility(View.GONE);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Quiz> options = new FirebaseRecyclerOptions.Builder<Quiz>().setQuery(mDatabaseReference.orderByChild("timeStamp"),Quiz.class).build();

        mAdapter = new FirebaseRecyclerAdapter<Quiz, QuizViewHolder>(options) {
            @NonNull
            @Override
            public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.quiz_list_item, parent,false);
                return new QuizViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull QuizViewHolder holder, int position, @NonNull Quiz model) {

                holder.setView(model);
                holder.setMenu(model,getRef(position));

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

    public class QuizViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setView(Quiz quiz)
        {
            TextView tv1 = mView.findViewById(R.id.quiz_item_name);
            tv1.setText(quiz.getName() + " : " + quiz.getTime()+ " Min");
            quiz.getAttemptTill();

            Button btn = mView.findViewById(R.id.quiz_item_button);
            if(quiz.getAttemptTill()!=-1 && System.currentTimeMillis()>quiz.getAttemptTill()) btn.setBackgroundResource(R.drawable.button_circular_dark);
            else btn.setBackgroundResource(R.drawable.button_circular);
            btn.setText("DETAILS");

        }
        public void setMenu(final Quiz quiz, final DatabaseReference mRef)
        {
           mView.findViewById(R.id.quiz_item_button).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {

                   View alertView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_quiz_details, null);
                   final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                   alertDialogBuilder.setView(alertView);
                   final AlertDialog alertDialog = alertDialogBuilder.create();

                   TextView tv1 = (TextView) alertView.findViewById(R.id.quiz_name);
                   TextView tv2 = (TextView) alertView.findViewById(R.id.quiz_duration);
                   TextView tv3 = (TextView) alertView.findViewById(R.id.quiz_numOfQues);
                   TextView tv4 = (TextView) alertView.findViewById(R.id.quiz_numOfAttemp);
                   TextView tv5 = (TextView) alertView.findViewById(R.id.quiz_deadline);
                   Button btn1 = (Button) alertView.findViewById(R.id.quiz_attempt);
                   Button btn2 = (Button) alertView.findViewById(R.id.quiz_result);

                   String str;

                   str ="Name : "+ quiz.getName();   tv1.setText(str);
                   str ="Duration : " + quiz.getTime() + " Min";  tv2.setText(str);
                   str ="Num of Questions : " + quiz.getQuestions().size(); tv3.setText(str);
                   if(!quiz.attemptOnce) {str ="Num of Attempts left : inf"; tv4.setText(str);}
                   else if (quiz.getAttempted()){str ="Num of Attempts left : 0"; tv4.setText(str);}
                   else {str ="Num of Attempts left : 1"; tv4.setText(str);}
                   if (quiz.getAttemptTill()==-1)  {str ="Deadline : nil"; tv5.setText(str);}
                   else { str ="Deadline : " + new SimpleDateFormat("dd/MM/yyyy HH:mm ").format(new Date(quiz.getAttemptTill())); tv5.setText(str);}

                   if((!quiz.getAttempted() || !quiz.getAttemptOnce())&& (quiz.getAttemptTill()==-1 || quiz.getAttemptTill()>System.currentTimeMillis()) )
                   {
                       btn2.setVisibility(View.GONE);
                       btn1.setVisibility(View.VISIBLE);
                       btn1.setText("Attempt!");
                       btn1.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {

                               alertDialog.dismiss();

                               Intent attemptTest = new Intent(getActivity(),AttemptQuiz.class);
                               attemptTest.putExtra("Quiz", quiz);
                               attemptTest.putExtra("Questions", quiz.getQuestions());
                               attemptTest.putExtra("Key", mRef.getKey());
                               startActivity(attemptTest);
                               getActivity().finish();
                           }
                       });
                   }
                   else btn1.setVisibility(View.GONE);
                   if(quiz.getAttempted())
                   {
                       btn2.setVisibility(View.VISIBLE);
                       btn2.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               alertDialog.dismiss();

                               Intent send = new Intent(getActivity(),QuizResult.class);
                               send.putExtra("Name", quiz.getName());
                               send.putExtra("Key", mRef.getKey());
                               startActivity(send);
                           }
                       });
                   }

                   alertDialogBuilder.setCancelable(true);
                   alertDialog.show();



               }
           });


        }


        public void sharequiz(Quiz quiz,String key)
        {
            Intent send = new Intent(getActivity(), ShareTestActivity.class);
            send.putExtra("Key", key);
            send.putExtra("Quiz",quiz);
            send.putExtra("Questions", quiz.getQuestions());
            startActivity(send);

        }
    }




}
