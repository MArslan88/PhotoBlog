package com.example.photoblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmailText;
    private EditText loginPassText;
    private Button loginBtn;
    private Button loginRegBtn;

    private FirebaseAuth mAuth;

    //private ProgressBar loginProgress;
    private ProgressDialog loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        loginEmailText = (EditText)findViewById(R.id.reg_email);
        loginPassText = (EditText)findViewById(R.id.reg_confirm_pass);
        loginBtn = (Button) findViewById(R.id.reg_btn);
        loginRegBtn = (Button) findViewById(R.id.reg_login_btn);
        //loginProgress = (ProgressBar)findViewById(R.id.login_progress);
        loginProgress = new ProgressDialog(this);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loginEmail = loginEmailText.getText().toString();
                String loginPass = loginPassText.getText().toString();

                if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPass)){
                    //loginProgress.setVisibility(View.VISIBLE);
                    // now we Sign In to the account...
                    loginProgress.setTitle("Sign In");
                    loginProgress.setMessage("Please wait...");
                    loginProgress.setCanceledOnTouchOutside(true);
                    loginProgress.show();

                    mAuth.signInWithEmailAndPassword(loginEmail,loginPass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        sendToMain();
                                        loginProgress.dismiss();
                                    }else {
                                        String errorMessage = task.getException().getMessage();
                                        Toast.makeText(LoginActivity.this, "Error: "+ errorMessage, Toast.LENGTH_SHORT).show();
                                        loginProgress.dismiss();
                                    }
                                    //loginProgress.setVisibility(View.INVISIBLE);
                                }
                            });
                }
            }
        });

        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToRegister();
            }
        });
    }

    private void sendToRegister() {
        Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
