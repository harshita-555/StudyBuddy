package com.example.studybuddy.timetable;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.studybuddy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewTask extends AppCompatActivity {

    private Button saveTask;
    private EditText nameText, msgText,dateText;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    String taskKey="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);



        mAuth= FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users/"+ mAuth.getUid()+"/Tasks");


        nameText = (EditText) findViewById(R.id.name_edit);
        msgText = (EditText) findViewById(R.id.msg_edit);
        dateText = (EditText) findViewById(R.id.date_edit);
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleDatePicker();
            }
        });
        saveTask = (Button) findViewById(R.id.saveTask);
        saveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(taskKey.equals("")) taskKey = mUserRef.push().getKey();

                Task todo = new Task();
                if(nameText.getText().toString().equals("")) nameText.setError("Please enter a title");
                else {
                    todo.setName(nameText.getText().toString());

                    if (msgText.getText().toString().equals("")) todo.setMessage("--");
                    else todo.setMessage(msgText.getText().toString());
                    todo.setDeadline(dateText.getText().toString());

                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put(taskKey, todo.toTaskMap());
                    mUserRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                finish();
                            }
                        }
                    });
                }
            }
        });

        if (getIntent().getExtras() != null) {
            Task task = (Task) getIntent().getSerializableExtra("todo");
            taskKey = getIntent().getStringExtra("Key");
            nameText.setText(task.getName() );
            msgText.setText(task.getMessage());
            dateText.setText(task.getDeadline());
        }


    }

    private void handleDatePicker()
    {

        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        final DatePickerDialog datePickerDialog = new DatePickerDialog(NewTask.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(Calendar.YEAR,year);
                calendar1.set(Calendar.MONTH,month);
                calendar1.set(Calendar.DATE, dayOfMonth);

                String dateString = DateFormat.format("MMM d, yyyy", calendar1).toString();
                handleTimePicker(dateString);


            }
        },YEAR,MONTH,DATE);
        datePickerDialog.show();
    }
    private void handleTimePicker(final String str)
    {

        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR);
        int MINUTES = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(Calendar.HOUR,hourOfDay);
                calendar1.set(Calendar.MINUTE,minute);

               String str1 = str +DateFormat.format("  hh : mm a", calendar1).toString();
               dateText.setText(str1);

            }
        }, HOUR,MINUTES, false);
        timePickerDialog.show();

    }
}
