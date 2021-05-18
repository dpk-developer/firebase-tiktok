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
import androidx.cardview.widget.CardView;

import com.dpk.viralist.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEmail, mPassword;
    private Button mLogin;
    private CardView mProgress;
    private TextView mRegister;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById();
        getSupportActionBar().hide();
        auth = FirebaseAuth.getInstance();

    }

    private void findViewById() {
        mEmail = findViewById(R.id.email);
        mProgress = findViewById(R.id.cvProgress);
        mPassword = findViewById(R.id.password);
        mLogin = findViewById(R.id.btn_login);
        mLogin.setOnClickListener(this);
        mRegister = findViewById(R.id.tvSignUp);
        mRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                mProgress.setVisibility(View.VISIBLE);
                String txt_email = mEmail.getText().toString();
                String txt_password = mPassword.getText().toString();
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

                if (!txt_email.matches(emailPattern) && txt_email.length() > 0) {
                    mProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(txt_email)) {
                    mProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(txt_password)) {
                    mProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
                } else {
                    auth.signInWithEmailAndPassword(txt_email, txt_password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        mProgress.setVisibility(View.GONE);
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        mProgress.setVisibility(View.GONE);
                                        Toast.makeText(LoginActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                }
                break;

            case R.id.tvSignUp:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

                break;
        }
    }
}
