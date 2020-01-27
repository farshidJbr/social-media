package com.example.books;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

   private ImageView postimage;
   private TextView descriptionnn;
   private Button DeletPostBtn , EditPostbtn;
   private String postKeyy , CurrentUserIdi , databaseUserId,descriptionPost,imaaagge;


   private DatabaseReference ClickPostRef;
   private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserIdi = mAuth.getCurrentUser().getUid();

        postKeyy = getIntent().getExtras().get("postKey").toString();
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKeyy);

        //set Views
        postimage = (ImageView)findViewById(R.id.clickPost_imageView);
        descriptionnn= (TextView)findViewById(R.id.ClickPost_Textview);
        EditPostbtn=(Button)findViewById(R.id.ClickPost_Edit);
        DeletPostBtn=(Button)findViewById(R.id.clickPost_Delet);

        DeletPostBtn.setVisibility(View.INVISIBLE);
        EditPostbtn.setVisibility(View.INVISIBLE);

        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    descriptionPost = dataSnapshot.child("description").getValue().toString();
                    imaaagge = dataSnapshot.child("postimage").getValue().toString();
                    databaseUserId = dataSnapshot.child("uid").getValue().toString();

                    descriptionnn.setText(descriptionPost);
                    Picasso.with(ClickPostActivity.this).load(imaaagge).into(postimage);

                    if (CurrentUserIdi.equals(databaseUserId)){

                        DeletPostBtn.setVisibility(View.VISIBLE);
                        EditPostbtn.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DeletPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DeleteCurrentPost();
            }
        });

        EditPostbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditCurrentPost(descriptionPost);
            }
        });
    }

    private void EditCurrentPost(String descriptionPost) {

        AlertDialog.Builder alert = new AlertDialog.Builder(ClickPostActivity.this);
        alert.setTitle("Edit Post : ");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(descriptionPost);
        alert.setView(inputField);

        alert.setPositiveButton("update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ClickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this,"post Edited Successfully...",Toast.LENGTH_LONG).show();
            }
        });
        alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        Dialog dialog = alert.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_blue_dark);
    }

    private void DeleteCurrentPost() {

        ClickPostRef.removeValue();
        SendUserToMainActivity();

        Toast.makeText(ClickPostActivity.this,"post has been Deleted...",Toast.LENGTH_LONG).show();
    }


    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(ClickPostActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}
