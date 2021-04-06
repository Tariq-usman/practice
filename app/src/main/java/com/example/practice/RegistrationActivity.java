package com.example.practice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {
    EditText mEmail, mPass, mName;
    ProgressBar progressBar;
    private ImageView mIgameView;
    public static final int PICK_IMAGE = 1;
    Uri imageUri;
    FirebaseStorage storage;
    // Create a Cloud Storage reference from the app
    StorageReference storageRef;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        storageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        progressBar = findViewById(R.id.progress_horizontal);
        mIgameView = findViewById(R.id.ivUserReg);
        mName = findViewById(R.id.etNameReg);
        mEmail = findViewById(R.id.etEmailReg);
        mPass = findViewById(R.id.etPassReg);


        mIgameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });


        findViewById(R.id.btnCreateNewReg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    createAccount();
                }
            }
        });
        findViewById(R.id.btnGoBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            mIgameView.setImageURI(imageUri);
        }
    }

    private void createAccount() {
        progressBar.setVisibility(View.VISIBLE);
        String name = mName.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String pass = mPass.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String user_id = mAuth.getCurrentUser().getUid();
                    StorageReference imageRef = storageRef.child("Images/" + user_id + ".jpg");
                    imageRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                String download_url = task.getResult().getStorage().getDownloadUrl().toString();

                                mAuth.getCurrentUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                                    @Override
                                    public void onSuccess(GetTokenResult getTokenResult) {
                                        String token_id = getTokenResult.getToken();
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("userId", user_id);
                                        map.put("name", name);
                                        map.put("email", email);
                                        map.put("image", download_url);
                                        map.put("token_id", token_id);
                                        databaseReference.child("Users").child("Students").child(user_id).setValue(map);
                                        progressBar.setVisibility(View.INVISIBLE);
                                        sendToMain();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RegistrationActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                /*.addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        sendToMain();
                                    }
                                });*/
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(RegistrationActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.i("error", task.getException().getMessage());
                            }
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(RegistrationActivity.this, "Error :" + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.i("error", e.getMessage());

            }
        });
    }

    private void sendToMain() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}