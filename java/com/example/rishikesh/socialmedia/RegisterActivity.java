package com.example.rishikesh.socialmedia;

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

public class RegisterActivity extends AppCompatActivity {
    private EditText userEmail,userPassword,userConfirmPassword;
    private Button createAccountButton;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userEmail = findViewById(R.id.register_email);
        userPassword = findViewById(R.id.register_password);
        userConfirmPassword = findViewById(R.id.register_confirm_password);
        createAccountButton = findViewById(R.id.register_create_account);

        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //not authenticated
            sendUsertoMainActivity();
        }
    }

    private void createNewAccount() {
        final String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String confirm_password = userConfirmPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Enter the Email", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Enter the password", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(confirm_password)){
            Toast.makeText(this, "Enter confirm password", Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(confirm_password)){
            Toast.makeText(this, "Password and confirm passwords dont match re", Toast.LENGTH_SHORT).show();
        }
        else{
            //create account
            loadingBar.setTitle("Creating New Account re");
            loadingBar.setMessage("Please Wait while we are creating your new Account !");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Account Created man", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                        sendUserTosetupActivity();
                    }
                    else{
                        String error = task.getException().getMessage();
                        loadingBar.dismiss();
                        Toast.makeText(RegisterActivity.this, "Error: "+error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendUserTosetupActivity() {
        Intent setupActivity = new Intent(RegisterActivity.this,SetupActivity.class);
        setupActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupActivity);
        finish();
    }
    private void sendUsertoMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
