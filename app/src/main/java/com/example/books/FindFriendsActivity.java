package com.example.books;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    private EditText searchBoxText;
    private ImageButton searchButton;
    private RecyclerView searchResultList;
    private DatabaseReference AllUsersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        //set firebase settings like Auth and database and storage
        AllUsersDatabaseRef= FirebaseDatabase.getInstance().getReference().child("Users");

        //set toolbar
        mToolbar =(Toolbar)findViewById(R.id.find_friends_appBar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("update posts");

        //set layout manager settings
        searchResultList=(RecyclerView)findViewById(R.id.search_result_list);
        searchResultList.setHasFixedSize(true);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));


        //set views
        searchBoxText=(EditText) findViewById(R.id.search_editText_Box);
        searchButton=(ImageButton) findViewById(R.id.search_people_friends_button);


        //set click listener for search button
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchBoxInput = searchBoxText.getText().toString();
                
                SearchPeopleAndFriends(searchBoxInput);

            }
        });
    }

    private void SearchPeopleAndFriends(String searchBoxInput)
    {
        Toast.makeText(FindFriendsActivity.this,"searching",Toast.LENGTH_LONG).show();
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users").orderByChild("fullName").startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");
        FirebaseRecyclerOptions<findfriends> options = new FirebaseRecyclerOptions.Builder<findfriends>()
                .setQuery(query,findfriends.class)
                .build();
        FirebaseRecyclerAdapter<findfriends,FindFriendsViewHolder> recyclerAdapter =
                new FirebaseRecyclerAdapter<findfriends, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull findfriends model) {

                holder.setFullname(model.getFullname());
                holder.setStatuse(model.getStatuse());
                holder.setProfileimage(model.getProfileimage());
            }
            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent,false);


                FindFriendsViewHolder findFriendsViewHolder = new FindFriendsViewHolder(view);
                return findFriendsViewHolder;
            }
        };
        searchResultList.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public FindFriendsViewHolder(View itemView)
        {
            super(itemView);

            mView=itemView;
        }

        public void setProfileimage(String profileimage)
        {
            CircleImageView mypicture = (CircleImageView)mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(mypicture.getContext()).load(profileimage).placeholder(R.drawable.profile).into(mypicture);
        }

        public void setFullname(String fullname)
        {
            TextView myname = (TextView)mView.findViewById(R.id.all_users_profile_fullname);
            myname.setText(fullname);
        }

        public void setStatuse(String statuse)
        {
            TextView mystatus = (TextView)mView.findViewById(R.id.all_users_profile_status);
            mystatus.setText(statuse);
        }

    }
}
