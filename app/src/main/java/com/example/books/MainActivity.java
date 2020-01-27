package com.example.books;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Context;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postLists;
    private Toolbar toolbar;
    boolean x=true;

    private CircleImageView profileImage;
    private TextView navProfileName;
    private ImageButton AddNewPostButton;
    private ImageView imagePost;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,LikesRef,postref;
    private String currentUserId;
    private Boolean likeCheker=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //firebase database and authenticate
//        Intent intent = getIntent();
//        int k = intent.getIntExtra("a",0);
//        if (k==0){
//            Intent i=new Intent(getApplicationContext(),PostActivity.class);
//            i.putExtra("z",1);
//            startActivity(i);
//        }


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");


        //create toolbar and acreat imageButton to add new post

        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().  setTitle("Home");
        //add new post button
        AddNewPostButton = findViewById(R.id.add_new_Post_button);

        //init views
        drawerLayout = findViewById(R.id.drowable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.Drawer_open, R.string.Drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //set header layout on menu
        navigationView = findViewById(R.id.navigation_view);

        imagePost = findViewById(R.id.post_image);


        postLists = findViewById(R.id.all_users_post_list);
        postLists.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postLists.setLayoutManager(linearLayoutManager);


        View navigatView = navigationView.inflateHeaderView(R.layout.navigation_header);
        profileImage = navigatView.findViewById(R.id.nav_profile_image);
        navProfileName = navigatView.findViewById(R.id.nav_user_fullName);

        UserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    if (dataSnapshot.hasChild("fullName")) {

                        String fullname = dataSnapshot.child("fullName").getValue().toString();
                        navProfileName.setText(fullname);
                    }

                    SharedPreferences sharedPreferences = getSharedPreferences("uri", MODE_PRIVATE);
                    Uri uri = Uri.parse(sharedPreferences.getString("u", ""));

                    Picasso.with(MainActivity.this).load(uri).placeholder(R.drawable.profile).into(profileImage);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuselector(menuItem);
                return false;
            }
        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUsersToPostActivity();
            }
        });

        DisplayAllUsers();

//        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    private void DisplayAllUsers() {

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Posts");
        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(query, Posts.class)
                .build();
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final PostsViewHolder postsViewHolder, int position, @NonNull Posts posts) {

                        final String postKey = getRef(position).getKey();
                        postsViewHolder.setTime(posts.getTime());
                        postsViewHolder.setFullname(posts.getFullname());
                        postsViewHolder.setDescription(posts.getDescription());
                        postsViewHolder.setDate(posts.getDate());
                        postsViewHolder.setPostimage(posts.getPostimage());
                        postsViewHolder.setProfileimage(posts.getProfileimage());

                        postsViewHolder.setLikeButtonStatus(postKey);
                        postsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent ClickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                                ClickPostIntent.putExtra("postKey", postKey);
                                startActivity(ClickPostIntent);
                            }
                        });


                        postsViewHolder.commentButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent CommentPostIntent = new Intent(MainActivity.this, CommentActivity.class);
                                CommentPostIntent.putExtra("postKey", postKey);
                                startActivity(CommentPostIntent);
                            }
                        });

                        postsViewHolder.dislikeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                likeCheker=true;
                                LikesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                       if(likeCheker.equals(true))
                                        {

                                            if (dataSnapshot.child(postKey).hasChild(currentUserId))
                                            {
                                                LikesRef.child(postKey).child(currentUserId).removeValue();
                                                likeCheker=false;
                                            }
                                            else {

                                                LikesRef.child(postKey).child(currentUserId).setValue(true);
                                                likeCheker=false;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);

                        PostsViewHolder postsViewHolder = new PostsViewHolder(view);
                        return postsViewHolder;
                    }
                };
        postLists.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
        firebaseRecyclerAdapter.startListening();

    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageButton dislikeButton,commentButton;
        TextView NumOfLike;
        DatabaseReference LikesRef;
        String currentUserId;
        int CountLikes;

        public PostsViewHolder(View itemView) {

            super(itemView);
            mView = itemView;

            dislikeButton=mView.findViewById(R.id.dislike_Button);
            commentButton=mView.findViewById(R.id.comment_post);
            NumOfLike = mView.findViewById(R.id.Like_text);

            LikesRef=FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public  void setLikeButtonStatus(final String postKey){

            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(postKey).hasChild(currentUserId))
                    {

                    CountLikes = (int)dataSnapshot.child(postKey).getChildrenCount();
                    dislikeButton.setImageResource(R.drawable.ic_favorite_black_24dp);
                    NumOfLike.setText((Integer.toString(CountLikes) + (" Likes")));
                    }
                    else {
                        CountLikes = (int)dataSnapshot.child(postKey).getChildrenCount();
                        dislikeButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                        NumOfLike.setText((Integer.toString(CountLikes) + (" Likes")));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setFullname(String fullname) {

            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(String profileimage) {

            CircleImageView imaage = mView.findViewById(R.id.post_profile_image);
            //Uri uri = Uri.parse(profileimage);
            Picasso.with(imaage.getContext()).load(profileimage).into(imaage);
        }

        public void setTime(String time) {

            TextView postTime = (TextView) mView.findViewById(R.id.post_time);
            postTime.setText("  " + time);
        }

        public void setDate(String date) {

            TextView postDate = (TextView) mView.findViewById(R.id.post_date);
            postDate.setText("   " + date);
        }

        public void setDescription(String description) {

            TextView postDescription = (TextView) mView.findViewById(R.id.post_description);
            postDescription.setText(description);
        }

        public void setPostimage(String postimage) {

            ImageView imagePost = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(imagePost.getContext()).load(postimage).into(imagePost);

        }

    }

    private void SendUsersToPostActivity() {

        Intent postintent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(postintent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            SendUserToLoginActivity();
        } else {
            CheckUserExitence();
        }
    }

    //checking user for exitence or not!!!
    private void CheckUserExitence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(current_user_id)) {
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToSetupActivity() {
        Intent Setupintent = new Intent(MainActivity.this, SetupActivity.class);
        Setupintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(Setupintent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent Loginintent = new Intent(MainActivity.this, LoginActivity.class);
        Loginintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(Loginintent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //add items selction to do right options like go to other activity and toast name of items that we click!!:)
    private void UserMenuselector(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.newPost_add:
                SendUsersToPostActivity();
                break;

            case R.id.Home:
                Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_LONG).show();
                break;

            case R.id.profile:
                SendUsersToProfileActivity();
                break;

            case R.id.find_your_friends:
                SendUsersToFindFriendsActivity();
                Toast.makeText(MainActivity.this, "Find your friends", Toast.LENGTH_LONG).show();
                break;

            case R.id.Friends:
                Toast.makeText(MainActivity.this, "Friends", Toast.LENGTH_LONG).show();
                break;

            case R.id.message:
                Toast.makeText(MainActivity.this, "Message", Toast.LENGTH_LONG).show();
                break;

            case R.id.setting_support:
                SendUsersToSettingActivity();
                Toast.makeText(MainActivity.this, "setting and support", Toast.LENGTH_LONG).show();
                break;

            case R.id.action_logOut:

                mAuth.signOut();
                SharedPreferences settings = this.getSharedPreferences("uri", MODE_PRIVATE);
                settings.edit().clear().apply();
                SharedPreferences settings1 = this.getSharedPreferences("uri1", MODE_PRIVATE);
                settings1.edit().clear().apply();
                SendUserToLoginActivity();
                break;
        }
    }

    private void SendUsersToProfileActivity() {
        try {

        Intent Setupintent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(Setupintent);
    }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void SendUsersToFindFriendsActivity() {

        Intent Setupintent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(Setupintent);
    }


    private void SendUsersToSettingActivity() {

        Intent Setupintent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(Setupintent);
    }

}

