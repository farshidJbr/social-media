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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


 public class PostActivity extends AppCompatActivity {

     private ImageView Select_postImage;
     //private ImageView postsPhoto;
     private EditText postDescription;
     private Button UpdateBtn;
     private static final int Gallery_pick = 1;
     private Uri imageUri;
     private String Date, Time, SavePostRandomName, downloadUrl, current_user_id;

     private Toolbar toolbar;
     private ProgressDialog loading;

     private String Description;
     private StorageReference PostImageRefrence;
     private DatabaseReference UserRef, PostsRef;
     private FirebaseAuth mAuth;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_post);


         //firebase storage refrence
         mAuth = FirebaseAuth.getInstance();
         current_user_id = mAuth.getCurrentUser().getUid();
         PostImageRefrence = FirebaseStorage.getInstance().getReference();
         UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
         PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

         //set Views
         Select_postImage = findViewById(R.id.imageButton_PostActivity);
         postDescription = findViewById(R.id.editTxt_post);
         UpdateBtn = findViewById(R.id.updatButton);
         // postsPhoto = findViewById(R.id.post_image);

         //set toolbar for post activity
         toolbar = findViewById(R.id.Updat_post_page_Toolbar);
         setSupportActionBar(toolbar);
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         getSupportActionBar().setDisplayShowHomeEnabled(true);
         getSupportActionBar().setTitle("Update post");

         loading = new ProgressDialog(this);

         //when click on Image Button for select post image
         Select_postImage.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {

                 Intent galleryintent = new Intent();
                 galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                 galleryintent.setType("image/*");
                 startActivityForResult(galleryintent, Gallery_pick);
             }
         });

         //when click on button to Update post
         UpdateBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {

                 ValidatePostInfo();
             }
         });
     }

     private void ValidatePostInfo() {

         Description = postDescription.getText().toString();

         if (imageUri == null) {

             Toast.makeText(PostActivity.this, "please select a image for your post...", Toast.LENGTH_LONG).show();
         } else if (TextUtils.isEmpty(Description)) {

             Toast.makeText(PostActivity.this, "please write a description about your post...", Toast.LENGTH_LONG).show();
         } else {
             loading.setTitle("add new post");
             loading.setMessage("please wait while your post updating");
             loading.show();
//             loading.setCanceledOnTouchOutside(true);
             StoringImageToFirebaseStorage();
         }
     }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data);

         if (requestCode == Gallery_pick && resultCode == RESULT_OK && data != null) {

             imageUri = data.getData();
             Select_postImage.setImageURI(imageUri);
             SharedPreferences.Editor editor = getSharedPreferences("uri1",MODE_PRIVATE).edit();
             editor.putString("post",imageUri.toString());
             editor.apply();
         }
     }


     private void StoringImageToFirebaseStorage() {

         SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
         String currentDateandTime = sdf.format(new Date());
         Time = new StringBuilder().append(currentDateandTime.substring(9,11)).append(":").append(currentDateandTime.substring(11,13)).toString();
         Date = new StringBuilder().append(currentDateandTime.substring(0,4)).append("/")
                 .append(currentDateandTime.substring(4,6)).append("/").append(currentDateandTime.substring(6,8)).toString();

         SavePostRandomName = currentDateandTime;

         final StorageReference filePath = PostImageRefrence.child("Posts").child(imageUri.getLastPathSegment() + SavePostRandomName + ".jpg");

         filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
             @Override
             public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                 filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                     @Override
                     public void onSuccess(Uri uri) {

                         Picasso.with(PostActivity.this).load(uri).into(Select_postImage);

                         SavingPostInfoToDatabase(uri);

                     }
                 }).addOnFailureListener(new OnFailureListener() {
                     @Override
                     public void onFailure(@NonNull Exception e) {
                         Toast.makeText(PostActivity.this, "aaaaaaaaaa", Toast.LENGTH_SHORT).show();
                     }
                 });

             }
         });
     }

     private void SavingPostInfoToDatabase(final Uri u) {

         UserRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                 if (dataSnapshot.exists()) {

                     String UserFullName = dataSnapshot.child("fullName").getValue().toString();
                     SharedPreferences sharedPreferences = getSharedPreferences("uri", MODE_PRIVATE);
                     Uri uri = Uri.parse(sharedPreferences.getString("u", ""));
                     if (uri.toString().equals("")){
                         uri = Uri.parse("android.resource://com.example.books/drawable/profile");
                     }

                     HashMap postsMap = new HashMap();
                     postsMap.put("uid", current_user_id);
                     postsMap.put("date", Date);
                     postsMap.put("time", Time);
                     postsMap.put("description", Description);
                     postsMap.put("postimage", u.toString());
                     postsMap.put("profileimage", uri.toString());
                     postsMap.put("fullname", UserFullName);
                     PostsRef.child(current_user_id + SavePostRandomName).updateChildren(postsMap)
                             .addOnCompleteListener(new OnCompleteListener() {
                                 @Override
                                 public void onComplete(@NonNull Task task) {

                                     if (task.isSuccessful()) {

                                         SendUserToMainActivity();
                                         Toast.makeText(PostActivity.this, " new post update successfully", Toast.LENGTH_LONG).show();
                                     } else {
                                         Toast.makeText(PostActivity.this, "error ocured while updating post", Toast.LENGTH_LONG).show();
                                     }

                                 }
                             });
                 }

             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });
     }

     @Override
     public boolean onOptionsItemSelected(@NonNull MenuItem item) {

         int id = item.getItemId();

         if (id == android.R.id.home) {
             SendUserToMainActivity();
         }

         return super.onOptionsItemSelected(item);
     }

     private void SendUserToMainActivity() {

         Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
         startActivity(mainIntent);
         finish();
     }
 }