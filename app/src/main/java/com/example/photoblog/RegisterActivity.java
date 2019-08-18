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

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {

    private EditText reg_email_field ,reg_pass_field, reg_confirm_pass_field;
    private Button reg_btn, reg_login_btn;

    private ProgressDialog reg_progress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        reg_progress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        reg_email_field = (EditText)findViewById(R.id.reg_email);
        reg_pass_field = (EditText)findViewById(R.id.reg_pass);
        reg_confirm_pass_field = (EditText)findViewById(R.id.reg_confirm_pass);
        reg_btn = (Button)findViewById(R.id.reg_btn);
        reg_login_btn = (Button) findViewById(R.id.reg_login_btn);

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = reg_email_field.getText().toString();
                String pass = reg_pass_field.getText().toString();
                String confirm_pass =  reg_confirm_pass_field.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirm_pass)){
                    if(pass.equals(confirm_pass)){

                        //loginProgress.setVisibility(View.VISIBLE);
                        // now we Sign In to the account...
                        reg_progress.setTitle("Sign In");
                        reg_progress.setMessage("Please wait...");
                        reg_progress.setCanceledOnTouchOutside(true);
                        reg_progress.show();

                        mAuth.createUserWithEmailAndPassword(email,pass)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){
                                            sendToSetup();
                                            reg_progress.dismiss();
                                        }else{
                                            String errorMessage = task.getException().getMessage();
                                            Toast.makeText(RegisterActivity.this, "Error: "+errorMessage, Toast.LENGTH_SHORT).show();
                                            reg_progress.dismiss();
                                        }
                                    }
                                });
                    }else {
                        Toast.makeText(RegisterActivity.this, "Confirm Password is not match...", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });

        reg_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                senToLogin();
            }
        });

    }

    private void senToLogin() {
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void sendToSetup() {
        Intent setupIntent = new Intent(RegisterActivity.this,SetupActivity.class);
        startActivity(setupIntent);
        finish();
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
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
