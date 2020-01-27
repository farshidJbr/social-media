package com.example.books;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText UserEmail,UserPassword,UserConfirmPassword;
    private Button UserSaveButton;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        //set views
        UserEmail = findViewById(R.id.Register_Email);
        UserPassword = findViewById(R.id.Register_password);
        UserConfirmPassword = findViewById(R.id.Register_ConfirmPassword);
        UserSaveButton= findViewById(R.id.Register_Button);
        loadingBar=new ProgressDialog(this);

        //set click listner for Register Button
        UserSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            SendUserToMainActivity();
        }
    }

    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

    private void CreateNewAccount() {
        String Email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmPassword = UserConfirmPassword.getText().toString();
        //check to write all fields
        if (TextUtils.isEmpty(Email))
        {
            Toast.makeText(RegisterActivity.this,"please enter your email...",Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(RegisterActivity.this,"please enter your password...",Toast.LENGTH_LONG).show();

        }
        else if (TextUtils.isEmpty(confirmPassword))
        {
            Toast.makeText(RegisterActivity.this,"please confirm your password...",Toast.LENGTH_LONG).show();

        }
        else if (!password.equals(confirmPassword))
        {
            Toast.makeText(RegisterActivity.this,"your password do not match with your confirm password...",Toast.LENGTH_LONG).show();

        }
        else {
            //set titles for progress dialog...
            loadingBar.setTitle("Creating Account...");
            loadingBar.setMessage("please wait while we are creating new Account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            //creating account requireds...
            mAuth.createUserWithEmailAndPassword(Email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                SendUsersToSetupActivity();
                                Toast.makeText(RegisterActivity.this,"you are Authenticated...",Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                            else {
                                String message = task.getException().getMessage();
                                Log.d("TAG", message+"\taaaaaaaaaaaaaaaaaaaaaaaaa");
                                Toast.makeText(RegisterActivity.this,"Error: aaaaaaaaaa" + message,Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RegisterActivity.this, "bbbbbbbbbbbb", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //function for send user from register to setup activity
    private void SendUsersToSetupActivity() {
        Intent setupIntent = new Intent(RegisterActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}
