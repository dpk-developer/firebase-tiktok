package com.dpk.viralist.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dpk.viralist.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mUsername, mEmail, mPassword;
    private Button mRegister;
    private TextView mSignIn;
    private android.widget.ProgressBar ProgressBar;
    private FirebaseAuth auth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findViewById();
        getSupportActionBar().hide();
        auth = FirebaseAuth.getInstance();

    }

    private void findViewById() {
        ProgressBar = findViewById(R.id.ProgressBar);
        mUsername = findViewById(R.id.name);
        mEmail = findViewById(R.id.email);
        mSignIn = findViewById(R.id.tvLogin);
        mPassword = findViewById(R.id.password);
        mRegister = findViewById(R.id.btn_register);
        mRegister.setOnClickListener(this);
        mSignIn.setOnClickListener(this);
    }

    private void register(final String mUsername, String mEmail, String mPassword) {

        auth.createUserWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            String userid = firebaseUser.getUid();

                            reference = FirebaseDatabase.getInstance("https://machine-test-brain-inventory-default-rtdb.firebaseio.com/").getReference("Users").child(userid);

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", mUsername);
                            hashMap.put("imageURL", "default");

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Register Successfully!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivity.this, "You can't register with this email or password", Toast.LENGTH_SHORT).show();
                            ProgressBar.setVisibility(View.GONE);
                        }
                    }
                });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                String txt_username = mUsername.getText().toString();
                String txt_email = mEmail.getText().toString();
                String txt_password = mPassword.getText().toString();
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

                if (TextUtils.isEmpty(txt_username)) {
                    Toast.makeText(this, "Enter Full Name", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(txt_email)) {
                    Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
                } else if (!txt_email.matches(emailPattern) && txt_email.length() > 0) {
                    Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
                } else if (txt_password.length() < 6) {
                    Toast.makeText(this, "Password at least 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    ProgressBar.setVisibility(View.VISIBLE);
                    register(txt_username, txt_email, txt_password);
                }
                break;

            case R.id.tvLogin:
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
        }
    }
}
