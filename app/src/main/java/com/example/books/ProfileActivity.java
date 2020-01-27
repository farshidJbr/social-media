package com.example.books;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView username , profilename,country,DateOfBirth,Relationship,Gander,profileStatuse;
    private CircleImageView userProfImg;
    private DatabaseReference profileUserref;
    private FirebaseAuth mAuth;

    private String currentUserId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUserref = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);


        //set views
        username =(TextView) findViewById(R.id.myusername);
        profilename =(TextView) findViewById(R.id.myporofile_name);
        country =findViewById(R.id.mycountry);
        DateOfBirth =findViewById(R.id.myDateOfBirth);
        Gander =(TextView)findViewById(R.id.myGender);
        profileStatuse =(TextView)findViewById(R.id.myprofile_status);
        Relationship =(TextView)findViewById(R.id.myrelation);
        userProfImg=(CircleImageView)findViewById(R.id.profileimageactivity);

        profileUserref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    //String myprofileImage = dataSnapshot.child("ProfileImg").getValue().toString();
                    String myprofilename = dataSnapshot.child("fullName").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationship = dataSnapshot.child("relationShipStatus").getValue().toString();
                    String myDateOfBirth = dataSnapshot.child("dob").getValue().toString();
                    String mystatuse = dataSnapshot.child("status").getValue().toString();

                    SharedPreferences sharedPreferences  = getSharedPreferences("uri",MODE_PRIVATE);
                    Uri uri = Uri.parse(sharedPreferences.getString("u",""));
                    if (uri.toString().equals("")){
                        uri = Uri.parse("android.resource://com.example.books/drawable/profile");
                    }

                    try {
                        Picasso.with(getApplicationContext()).load(uri).into(userProfImg);
                    }catch (Exception e){
                        Log.d("TAG", e+"");
                        Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        Toast.makeText(ProfileActivity.this, uri+"", Toast.LENGTH_SHORT).show();
                    }

                    username.setText("@" + myUserName);
                    profilename.setText(myprofilename);
                    country.setText("country: "+myCountry);
                    DateOfBirth.setText("BirthDay:"+myDateOfBirth);
                    Relationship.setText("Relation: "+myRelationship);
                    Gander.setText("Gender:"+myGender);
                    profileStatuse.setText(mystatuse);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
