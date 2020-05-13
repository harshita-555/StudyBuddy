package com.example.studybuddy.quizzes;

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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.clsrm.ShareTestActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class QuizTeacherFragment extends Fragment {

    private DatabaseReference mDatabaseReference;
    private FirebaseUser user;
    private RecyclerView mConvList;
    private FirebaseRecyclerAdapter<Quiz, QuizViewHolder> mAdapter;
    private  ImageButton mDocs;
    private View mMainView;



    public QuizTeacherFragment() {
        // Required empty public constructor
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
        mDocs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getActivity(), FormNewQuiz.class));
                getActivity().finish();

            }
        });

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

            Button btn = mView.findViewById(R.id.quiz_item_button);
            if(quiz.getAttemptTill()!=-1 && System.currentTimeMillis()>quiz.getAttemptTill()) btn.setText("Inactive");
            else btn.setText("active");

        }
        public void setMenu(final Quiz quiz, final DatabaseReference mRef)
        {
            mView.findViewById(R.id.quiz_item_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View itemView) {


                    PopupMenu popup = new PopupMenu(itemView.getContext(), itemView);
                    popup.getMenuInflater().inflate(R.menu.teacher_quiz_menu, popup.getMenu());
                    popup.setGravity(Gravity.END);
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.share_quiz:
                                  sharequiz(quiz,mRef.getKey());

                                    return true;
                                case R.id.view_result :
                                    viewResult(quiz,mRef.getKey());
                                    return true;
                                case R.id.delete_quiz:
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
        public void sharequiz(Quiz quiz,String key)
        {
            Intent send = new Intent(getActivity(), ShareTestActivity.class);
            send.putExtra("Key", key);
            send.putExtra("Quiz",quiz);
            send.putExtra("Questions", quiz.getQuestions());
            startActivity(send);

        }
        public void viewResult(Quiz quiz,String key)
        {
            Intent send = new Intent(getActivity(),QuizResult.class);
            send.putExtra("Name", quiz.getName());
            send.putExtra("Key", key);
            startActivity(send);
        }
    }


}
