package com.example.books;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentActivity extends AppCompatActivity {

    private RecyclerView CommentList;
    private EditText CommentInputText;
    private ImageView SendCommentButton;
    private String post_key , current_user_id;

    private DatabaseReference UsersRef , PostsRef;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        CommentInputText = findViewById(R.id.CommentInputblock_Activity);
        SendCommentButton = findViewById(R.id.imageSend_Comment_activity);

        //get post key from main activity
        post_key = getIntent().getExtras().get("postKey").toString();

        //firebase database refrence

        mAuth= FirebaseAuth.getInstance();
        current_user_id= mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("comment").child(post_key);

//        SendCommentButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(CommentInputText.getText().toString().equals(""))
//                    Toast.makeText(CommentActivity.this, "Comment shouldnt be empty!", Toast.LENGTH_SHORT).show();
//                else{
//                HashMap commentMap = new HashMap();
//                commentMap.put("comment", CommentInputText.getText().toString());
//                PostsRef.updateChildren(commentMap).addOnCompleteListener(new OnCompleteListener() {
//                    @Override
//                    public void onComplete(@NonNull Task task) {
//                        if (task.isSuccessful()){
//                           Toast.makeText(CommentActivity.this,"your comment successfully added....",Toast.LENGTH_LONG).show();
//                            CommentInputText.setText("");
//                        }
//                        else {
//                            String message = task.getException().getMessage();
//                            Toast.makeText(CommentActivity.this,"Error:" + message,Toast.LENGTH_LONG).show();
//                        }
//                    }
//               });
//            }}
//        });

        //set Recycler view settings
        CommentList = findViewById(R.id.RecyclerView_commentActivity);
        CommentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentList.setLayoutManager(linearLayoutManager);



        //set views
        CommentInputText= findViewById(R.id.CommentInputblock_Activity);
        SendCommentButton = findViewById(R.id.imageSend_Comment_activity);

        SendCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UsersRef.addValueEventListener(new ValueEventListener() {
                   @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists())
                        {
                            String UserNamee = dataSnapshot.child("username").getValue().toString();

                            ValidateComment(UserNamee);
                            CommentInputText.setText("");
                        }
                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
               });

            }
       });

    }

    private void ValidateComment(String userNamee) {

        String commenttextwriten= CommentInputText.getText().toString();

        if (TextUtils.isEmpty(commenttextwriten)){

            Toast.makeText(CommentActivity.this,"please Write Text to The Comment",Toast.LENGTH_LONG).show();
        }
        else
        {
           Calendar calforDate = Calendar.getInstance();
           SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
           final String SavecurrentDate = currentDate.format(calforDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:MM");
            final String SavecurrentTime = currentTime.format(calFordTime.getTime());

            final String RandomKey = current_user_id + SavecurrentDate + SavecurrentTime;

            HashMap commentMap = new HashMap();
            commentMap.put("uid" , current_user_id);
            commentMap.put("comment" , commenttextwriten);
            commentMap.put("date" , SavecurrentDate);
            commentMap.put("time" , SavecurrentTime);
            commentMap.put("username" , userNamee);

            PostsRef.child(RandomKey).updateChildren(commentMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful())
                    {
                        Toast.makeText(CommentActivity.this,"your comment create successfully...",Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(CommentActivity.this,"error occurd...",Toast.LENGTH_LONG).show();

                    }
                }
           });
        }
   }
}
