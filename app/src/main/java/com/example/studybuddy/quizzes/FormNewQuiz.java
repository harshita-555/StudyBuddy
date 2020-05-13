package com.example.studybuddy.quizzes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.studybuddy.R;
import com.example.studybuddy.clsrm.ClassroomActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class FormNewQuiz extends AppCompatActivity {

    EditText question;
    EditText aText;
    EditText bText;
    EditText cText;
    EditText dText;
    RadioButton aRadio;
    RadioButton bRadio;
    RadioButton cRadio;
    RadioButton dRadio;

    int currentQuestion = 1;
    int previousQuestion = 0;
    TextView questionNumber;

    ArrayList<Question> ques;
    JSONArray jsonArray;
    String selectedOption = "";

    Button save_button;
    AlertDialog alertDialog;
    private View dialogvView;
    String fileName = "file";
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private DatabaseReference myRef;
    CardView fabN,fabP,fl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_new_quiz);

        auth = FirebaseAuth.getInstance();

        jsonArray = new JSONArray();
        question = findViewById(R.id.questionView);
        question =  findViewById(R.id.questionView);
        aText =  findViewById(R.id.aText);
        bText =  findViewById(R.id.bText);
        cText =  findViewById(R.id.cText);
        dText =  findViewById(R.id.dText);
        questionNumber =  findViewById(R.id.questionNumber);
        aRadio =  findViewById(R.id.aRadio);
        bRadio =  findViewById(R.id.bRadio);
        cRadio =  findViewById(R.id.cRadio);
        dRadio =  findViewById(R.id.dRadio);
        auth = FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance();
        myRef=database.getReference();
        selectedOption = "";
        currentQuestion = 1;
        setListeners();

        ques = new ArrayList<>();

        alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        dialogvView = inflater.inflate(R.layout.dialog_custom,null);



        fabN = findViewById(R.id.nextfab);
        fl = findViewById(R.id.fab2);//save button
        fabP = findViewById(R.id.update_card);

        fabP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(previousQuestion>0) {
                    boolean cont = getEnteredQuestionsValue();
                        previousQuestion--;
                        currentQuestion--;
                        clearAllData();
                        setAllData(previousQuestion);


                }
                if(previousQuestion==0)
                    fabP.setVisibility(View.INVISIBLE);
                //Question question1 = new Question();
                Toast.makeText(FormNewQuiz.this, String.valueOf(currentQuestion), Toast.LENGTH_SHORT).show();
            }
        });

        fabN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean cont = getEnteredQuestionsValue();
                if (cont)
                {
                    previousQuestion++;
                    currentQuestion++;
                    if(ques.size() ==previousQuestion) {

                        clearAllData();
                        questionNumber.setText(String.valueOf(currentQuestion));
                    }
                    else
                    {
                        Log.w("fabn else", String.valueOf(previousQuestion));
                        setAllData(previousQuestion);
                        fabP.setVisibility(View.VISIBLE);
                    }

                    Toast.makeText(FormNewQuiz.this, "QUESTION " + currentQuestion, Toast.LENGTH_SHORT).show();
                    questionNumber.setText(String.valueOf(currentQuestion));
                    fabP.setVisibility(View.VISIBLE);
                }

            }
        });

        fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean cont = getEnteredQuestionsValue();

                if(cont) {
                    for(int i =0 ; i< ques.size();i++)
                    {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            Question q = ques.get(i);
                            jsonObject.put("answer",q.getAnswer());
                            jsonObject.put("opt_A",q.getOpt_A());
                            jsonObject.put("opt_B",q.getOpt_B());
                            jsonObject.put("opt_C",q.getOpt_C());
                            jsonObject.put("opt_D",q.getOpt_D());
                            jsonObject.put("question",q.getQuestion());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        jsonArray.put(jsonObject);
                    }


                    if (jsonArray.length() != 0) {
                        final JSONObject tempObject = new JSONObject();
                        // get dialog_custom.xml view
                        LayoutInflater li = LayoutInflater.from(FormNewQuiz.this);
                        View promptsView = li.inflate(R.layout.dialog_custom, null);
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FormNewQuiz.this);

                        // set dialog_custom.xml to alertdialog builder
                        alertDialogBuilder.setView(promptsView);
                        final EditText userInput = promptsView.findViewById(R.id.editTextDialogUserInput);
                        final EditText userTime = promptsView.findViewById(R.id.editTextDialogUserInput1);
                        final ToggleButton boolAttempt = promptsView.findViewById(R.id.editTextDialogUserInput2);
                        final EditText deadline = promptsView.findViewById(R.id.editTextDialogUserInput3);


                        // set dialog message
                        alertDialogBuilder.setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        String temp1 = userInput.getText().toString().trim();
                                        String temp2 = userTime.getText().toString().trim();
                                        if (temp2.equals("") || temp1.equals(""))
                                        {
                                            Toast.makeText(FormNewQuiz.this, "Enter the details!", Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                        {
                                            boolean multipleAttempt = boolAttempt.isChecked();
                                            Long timeStamp = null;
                                            if (!deadline.getText().toString().trim().equals("")) {
                                                SimpleDateFormat crunchifyFormat = new SimpleDateFormat("dd/MM/yyyy");
                                                try {
                                                    Date date = crunchifyFormat.parse(deadline.getText().toString().trim());
                                                    timeStamp = date.getTime()+ 86400000;
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            try {
                                                tempObject.put("attemptOnce", !multipleAttempt);

                                                if (timeStamp != null)  tempObject.put("attemptTill", timeStamp);
                                                else tempObject.put("attemptTill", -1);
                                                tempObject.put("timeStamp",System.currentTimeMillis());
                                                tempObject.put("questions", jsonArray);
                                                final String TIME = userTime.getText().toString().trim();
                                                tempObject.put("time", Integer.parseInt(temp2));
                                                tempObject.put("name", temp1);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            final String jsonStr = tempObject.toString();
                                            Map<String, Object> result = new Gson().fromJson(jsonStr, Map.class);
                                            fileName = myRef.child("Users/" + auth.getUid() + "/Tests").push().getKey();
                                            if (!TextUtils.isEmpty(fileName))   myRef.child("Users/" + auth.getUid() + "/Tests").child(fileName).setValue(result);
                                            startActivity(new Intent(FormNewQuiz.this, ClassroomActivity.class));
                                            finish();


                                        }
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Incomplete Question format", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(FormNewQuiz.this);
        builder.setMessage("Exit without saving?");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(FormNewQuiz.this, ClassroomActivity.class));
                finish();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void updateData(int position) {
        Question question1 = new Question();
        question1 = ques.get(position-1);
    }

    public void setAllData(int position) {
        clearAllData();
        Question question1 ;
        question1 = ques.get(position);
        question.setText(question1.getQuestion());
        selectedOption =question1.getAnswer();
        aText.setText(question1.getOpt_A());
        bText.setText(question1.getOpt_B());
        cText.setText(question1.getOpt_C());
        dText.setText(question1.getOpt_D());
        switch (question1.getAnswer()){
            case "A":
                aRadio.setChecked(true);
                break;
            case "B":
                bRadio.setChecked(true);
                break;
            case "C":
                cRadio.setChecked(true);
                break;
            case "D":
                dRadio.setChecked(true);
                break;
        }
    }

    private void clearAllData() {

        aRadio.setChecked(false);
        bRadio.setChecked(false);
        cRadio.setChecked(false);
        dRadio.setChecked(false);
        aText.setText(null);
        bText.setText(null);
        cText.setText(null);
        dText.setText(null);
        question.setText(null);
        selectedOption = "";
    }

    private boolean getEnteredQuestionsValue() {

        boolean cont = false;
        if (TextUtils.isEmpty(question.getText().toString().trim())) {
            question.setError("Please fill in a question");
        }
        else if (TextUtils.isEmpty(aText.getText().toString().trim())) {
            aText.setError("Please fill in option A");
        }
        else if (TextUtils.isEmpty(bText.getText().toString().trim())) {
            bText.setError("Please fill in option B");
        }
        else if (TextUtils.isEmpty(cText.getText().toString().trim())) {
            cText.setError("Please fill in option C");
        }
        else if (TextUtils.isEmpty(dText.getText().toString().trim())) {
            dText.setError("Please fill in option D");
        }
        else if (selectedOption.equals("")) {
            Toast.makeText(this, "Please select the correct answer", Toast.LENGTH_SHORT).show();
        }
        else {

            Question quest = new Question();
            quest.setQuestion(question.getText().toString());
            quest.setOpt_A(aText.getText().toString());
            quest.setOpt_B(bText.getText().toString());
            quest.setOpt_C(cText.getText().toString());
            quest.setOpt_D(dText.getText().toString());
            quest.setAnswer(selectedOption);
            if(ques.size()==previousQuestion) ques.add(quest);
            else {ques.set(previousQuestion, quest);}
            cont = true;



        }
        return cont;
    }

    private void setListeners() {
        aRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedOption = "A";
                bRadio.setChecked(false);
                cRadio.setChecked(false);
                dRadio.setChecked(false);
            }
        });
        bRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedOption = "B";
                aRadio.setChecked(false);
                cRadio.setChecked(false);
                dRadio.setChecked(false);
            }
        });
        cRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedOption = "C";
                bRadio.setChecked(false);
                aRadio.setChecked(false);
                dRadio.setChecked(false);
            }
        });
        dRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedOption = "D";
                bRadio.setChecked(false);
                cRadio.setChecked(false);
                aRadio.setChecked(false);
            }
        });

    }

}
