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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private Button login_btn;
    private EditText UserEmail,UserPassword;
    private TextView NeedNewAccount;
     ImageView GoogleSignIn;
     private GoogleApiClient mGoogleSignInClient;
     private  static final int RC_SIGN_IN=1;
     private static final String TAG="LoginActivity";
    private ProgressDialog loading;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();

        //set views
        login_btn =(Button)findViewById(R.id.Login_Button);
        UserEmail =(EditText)findViewById(R.id.Login_Email);
        UserPassword =(EditText)findViewById(R.id.Login_password);
        NeedNewAccount = (TextView)findViewById(R.id.txt_haveAccount);
        GoogleSignIn =(ImageView)findViewById(R.id.login_google);
        loading = new ProgressDialog(this);

        //set click on text Need New Account?
        NeedNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToRegisterActivity();
            }
        });
        //set login button click to get information
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowingUserToLogin();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(this).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                Toast.makeText(LoginActivity.this,"conection to google dign in failed",Toast.LENGTH_LONG).show();

            }
        })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
        GoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent( mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            loading.setTitle("google sign in");
            loading.setMessage("please wait while we are login using your google account...");
            loading.show();

            GoogleSignInResult result= Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()){

                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(LoginActivity.this,"please wait while we are getting you auth result...",Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(LoginActivity.this,"cant getting you auth result...",Toast.LENGTH_LONG).show();

            }
        }
    }



    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            SendUserToMainActivity();
                        } else {

                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message  = task.getException().getMessage();
                            SendUsersToLoginActivity();
                            Toast.makeText(LoginActivity.this,"not Authenticate , try Again..." + message ,Toast.LENGTH_LONG).show();

                        }
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

    //check and get email and password and allowing to login
    private void AllowingUserToLogin() {
        //get amail and passwords
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        //check fields to not empty and authenticating usewrs
        if (TextUtils.isEmpty(email)){
            Toast.makeText(LoginActivity.this,"please write your Email...",Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(LoginActivity.this,"please write your password...",Toast.LENGTH_LONG).show();
        }
        else {
            //set progress dialog title and message
            loading.setTitle(" login...");
            loading.setMessage("please wait,while you allowing login into your account...");
            loading.show();
            loading.setCanceledOnTouchOutside(true);

            //sign in with email and password
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(LoginActivity.this,"you are loged in...",Toast.LENGTH_LONG).show();
                                loading.dismiss();
                            }
                            else {
                                String message = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Error: " + message,Toast.LENGTH_LONG).show();
                                loading.dismiss();
                            }
                        }
                    });
        }
    }
     //send user to main activity
    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(LoginActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

    private void SendUsersToLoginActivity(){
        Intent MainIntent = new Intent(LoginActivity.this , LoginActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();

    }
     //send user to Register Activity
    private void SendUserToRegisterActivity() {
        Intent Registerintent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(Registerintent);
    }
}
