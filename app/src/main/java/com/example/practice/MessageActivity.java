package com.example.practice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {
    EditText mMessage;
    TextView mSentTo;
    Button mSendBtn;
    ProgressBar progressBar;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    String sender_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        sender_id = FirebaseAuth.getInstance().getUid();
        String receiver_id = getIntent().getStringExtra("user_id");
        String user_name = getIntent().getStringExtra("user_name");

        mSentTo = findViewById(R.id.tvSendTo);
        mSentTo.setText("Send to "+user_name);
        mMessage = findViewById(R.id.etMessage);
        mSendBtn = findViewById(R.id.btnSendMsg);
        progressBar = findViewById(R.id.progress_horizontal_message);


        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String message = mMessage.getText().toString().trim();
                Map<String, Object> map = new HashMap<>();
                map.put("message", message);
                map.put("from", MessageActivity.this.sender_id);

                databaseReference.child("Users/"+"Students/"+receiver_id+"/Notifications").push().setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.INVISIBLE);
                        mMessage.setText("");
                        Toast.makeText(MessageActivity.this, "Notification sent..", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MessageActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }
}