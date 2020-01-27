package com.example.books;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private EditText fullname,username,countryName;
    private Button SaveInformationBtn;
    private CircleImageView ProfileImage;
    private ProgressDialog loading;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference UserProfileImageRef;

    String currentUserId;
    final static int gallery_pick=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);



        //set firebase authentication
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        //create column of Users
        userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        //create column of profile image
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("ProfileImg");

        //set views
        username = findViewById(R.id.setup_username);
        fullname = findViewById(R.id.setup_fullName);
        countryName = findViewById(R.id.setup_CountryName);
        SaveInformationBtn = findViewById(R.id.setup_information_button);
        ProfileImage = findViewById(R.id.setup_profile_image);
        loading = new ProgressDialog(this);

        //set progress dialog
        loading=new ProgressDialog(this);
        //set Button Click to save information
        SaveInformationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveAccountSetupInformation();
            }
        });
        //set image for profile from phone gallery
        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent GalleryIntent = new Intent();
                GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                GalleryIntent.setType("image/*");
                startActivityForResult(GalleryIntent,gallery_pick);
            }
        });
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

//                    if (dataSnapshot.hasChild("ProfileImg")) {
//
//                        DataSnapshot image = dataSnapshot.child("ProfileImg");
//                        Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);
//                    }
//                    else {
//                        Toast.makeText(SetupActivity.this,"please select profile image first.",Toast.LENGTH_LONG).show();
//                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //in this activity Upload image to firebase storage and check to upload and errors from upload
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==gallery_pick && resultCode==RESULT_OK && data!=null){

            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
             if (resultCode==RESULT_OK){

                 //set progressdialog title and message
                 loading.setTitle("profile Image");
                 loading.setMessage("please wait while we are saving your profile image...");
                 loading.setCanceledOnTouchOutside(true);
                 loading.show();

                 Uri resultUri = result.getUri();

                 final StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");

                 filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                     @Override
                     public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                Picasso.with(SetupActivity.this).load(uri).into(ProfileImage);

                                SharedPreferences.Editor editor  = getSharedPreferences("uri",MODE_PRIVATE).edit();
                                editor.putString("u",uri.toString());
                                editor.apply();
                                Toast.makeText(getBaseContext(), "Upload success! URL - "  , Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            }
                        });
                     }
                 }).addOnFailureListener(new OnFailureListener() {
                     @Override
                     public void onFailure(@NonNull Exception e) {
                         Log.d("TAG", e.getMessage());
                         Toast.makeText(SetupActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                     }
                 });
             }
             else {
                 Toast.makeText(SetupActivity.this,"Error occored ,Image cant be cropped. please try again...",Toast.LENGTH_LONG ).show();
                 loading.dismiss();
             }
        }
    }

    //function to get information and save it in Account
    private void SaveAccountSetupInformation() {
        String Username = username.getText().toString();
        String FullN = fullname.getText().toString();
        String nameOfcountry = countryName.getText().toString();
        if (TextUtils.isEmpty(Username)){
            Toast.makeText(SetupActivity.this,"please write your username...",Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(FullN)){
            Toast.makeText(SetupActivity.this,"please write your Full Name...",Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(nameOfcountry)){
            Toast.makeText(SetupActivity.this,"please write your Country...",Toast.LENGTH_LONG).show();
        }
        else {
            //set progress dialog
            loading.setTitle("saving information");
            loading.setMessage("please wait < while we are creating your new Account...");
            loading.show();
            loading.setCanceledOnTouchOutside(true);

            HashMap UserMap = new HashMap();
            UserMap.put("username", Username);
            UserMap.put("fullName", FullN);
            UserMap.put("country", nameOfcountry);
            UserMap.put("status","hey There im using this app...");
            UserMap.put("gender","none");
            UserMap.put("dob","none");
            UserMap.put("relationShipStatus","none");
            UserMap.put("static information","you are using this app");

            userRef.updateChildren(UserMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this,"your account created successfully....",Toast.LENGTH_LONG).show();
                        loading.dismiss();
                    }
                    else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this,"Error:" + message,Toast.LENGTH_LONG).show();
                        loading.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SetupActivity.this, "aaaaaaaaaaa", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //send user to main activity
    private void SendUserToMainActivity() {
        Intent mainintent = new Intent(SetupActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }
}
