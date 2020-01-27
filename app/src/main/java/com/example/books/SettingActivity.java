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

import androidx.appcompat.widget.Toolbar;

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

public class SettingActivity extends AppCompatActivity {

    private EditText username , profilename,country,DateOfBirth,Relationship,Gander,profileStatuse;
    private Button accountsettingBtn;
    private CircleImageView userProfImg;
    private String currentUserId;
    private StorageReference UserProfileImageRef;


    private Toolbar mtoolbar;
    private ProgressDialog loading;
    final static int gallery_pick=1;


    private DatabaseReference settingUserRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //set firebase databases and Auth
        mAuth=FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        settingUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("ProfileImg");


        //set toolbar
        mtoolbar = (Toolbar)findViewById(R.id.settings_Toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //set views
        username =(EditText)findViewById(R.id.setting_profile_usernam);
        profilename =(EditText)findViewById(R.id.setting_profile_name);
        country =(EditText)findViewById(R.id.setting_profile_country);
        DateOfBirth =(EditText)findViewById(R.id.setting_profile_DateOfBirth);
        Relationship =(EditText)findViewById(R.id.setting_profile_RelationShip);
        Gander =(EditText)findViewById(R.id.setting_profile_Gender);
        profileStatuse =(EditText)findViewById(R.id.setting_profile_statuse);

        accountsettingBtn =(Button) findViewById(R.id.setting_profile_Button);
        userProfImg=(CircleImageView)findViewById(R.id.setting_profile_image);
        loading = new ProgressDialog(this);

        settingUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                 if (dataSnapshot.exists())
                 {
                     String myprofilename = dataSnapshot.child("fullName").getValue().toString();
                     String myUserName = dataSnapshot.child("username").getValue().toString();
                     String myCountry = dataSnapshot.child("country").getValue().toString();
                     String myGender = dataSnapshot.child("gender").getValue().toString();
                     String myRelationship = dataSnapshot.child("relationShipStatus").getValue().toString();
                     String myDateOfBirth = dataSnapshot.child("dob").getValue().toString();
                     String mystatuse = dataSnapshot.child("status").getValue().toString();

                     userProfImg.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View view) {
                             Intent GalleryIntent = new Intent();
                             GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                             GalleryIntent.setType("image/*");
                             startActivityForResult(GalleryIntent,gallery_pick);
                         }
                     });

                     SharedPreferences sharedPreferences  = getSharedPreferences("uri",MODE_PRIVATE);
                     Uri uri = Uri.parse(sharedPreferences.getString("u",""));

                     try {
                         Picasso.with(getApplicationContext()).load(uri).into(userProfImg);
                     }catch (Exception e){
                         Log.d("TAG", e+"");
                         Toast.makeText(SettingActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                         Toast.makeText(SettingActivity.this, uri+"", Toast.LENGTH_SHORT).show();
                     }
                    username.setText(myUserName);
                    profilename.setText(myprofilename);
                    country.setText(myCountry);
                    DateOfBirth.setText(myDateOfBirth);
                    Relationship.setText(myRelationship);
                    Gander.setText(myGender);
                    profileStatuse.setText(mystatuse);

                 }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        accountsettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidateAccountInfo();
            }
        });
    }

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

                                Picasso.with(SettingActivity.this).load(uri).into(userProfImg);

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
                        Toast.makeText(SettingActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
            else {
                Toast.makeText(SettingActivity.this,"Error occored ,Image cant be cropped. please try again...",Toast.LENGTH_LONG ).show();
                loading.dismiss();
            }
        }
    }

    private void ValidateAccountInfo() {

        String validateUsername = username.getText().toString();
        String validateProfilename = profilename.getText().toString();
        String validaterelationShip = Relationship.getText().toString();
        String validateCountry = country.getText().toString();
        String validateDoB = DateOfBirth.getText().toString();
        String validateGender = Gander.getText().toString();
        String validatePorofileStatuse = profileStatuse.getText().toString();

        if (TextUtils.isEmpty(validateUsername))
        {
            Toast.makeText(SettingActivity.this,"please write your username...",Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(validateProfilename))
        {
            Toast.makeText(SettingActivity.this,"please write your profile name...",Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(validaterelationShip))
        {
            Toast.makeText(SettingActivity.this,"please write your RelationShip...",Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(validateCountry))
        {
            Toast.makeText(SettingActivity.this,"please write your country...",Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(validateDoB))
        {
            Toast.makeText(SettingActivity.this,"please write your Date OF Birth...",Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(validateGender))
        {
            Toast.makeText(SettingActivity.this,"please write your Gender...",Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(validatePorofileStatuse))
        {
            Toast.makeText(SettingActivity.this,"please write your profile Status...",Toast.LENGTH_LONG).show();
        }
        else {
            //set progressdialog title and message
            loading.setTitle("profile Setting");
            loading.setMessage("please wait while we are saving your profile setting...");
            loading.setCanceledOnTouchOutside(true);
            loading.show();
            UpdateAccountInfo(validateCountry,validateDoB,validateGender,validatePorofileStatuse,validateProfilename,validaterelationShip,validateUsername);
        }
    }

    private void UpdateAccountInfo(String validateCountry, String validateDoB, String validateGender, String validatePorofileStatuse, String validateProfilename, String validaterelationShip, String validateUsername) {

        HashMap SettingUserMap = new HashMap();
        SettingUserMap.put("username",validateUsername);
        SettingUserMap.put("country",validateCountry);
        SettingUserMap.put("dob",validateDoB);
        SettingUserMap.put("gender",validateGender);
        SettingUserMap.put("relationShipStatus",validaterelationShip);
        SettingUserMap.put("status",validatePorofileStatuse);
        SettingUserMap.put("fullName",validateProfilename);

        settingUserRef.updateChildren(SettingUserMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if (task.isSuccessful()){

                    SendUserToMainActivity();
                    Toast.makeText(SettingActivity.this,"Settings Updated successfully...",Toast.LENGTH_LONG).show();
                    loading.dismiss();
                }
                else {
                    Toast.makeText(SettingActivity.this,"Error occored...",Toast.LENGTH_LONG).show();
                    loading.dismiss();
                }

            }
        });
    }

    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(SettingActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }

}
