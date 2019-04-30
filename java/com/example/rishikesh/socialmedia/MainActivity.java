package com.example.rishikesh.socialmedia;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
//import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef , PostsRef;
    private CircleImageView NavProfileImgae;
    private TextView NavProfileName;
    private ImageButton addNewPostButton;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        drawerLayout = findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        addNewPostButton = findViewById(R.id.add_new_post_button);

        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        navigationView = findViewById(R.id.navigation_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImgae = navView.findViewById(R.id.nav_profile_image);
        NavProfileName = navView.findViewById(R.id.nav_full_name);

        postList = findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("fullname")){
                        String username = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileName.setText(username);
                    }
                    if(dataSnapshot.hasChild("profile image")){
                        String profileimageo = dataSnapshot.child("profile image").getValue().toString();
                        String url = "https://goo.gl/images/HqSkd8";
                        //Picasso.get().load(profileimageo).placeholder(R.drawable.profile).into(NavProfileImgae);
                        Picasso.with(MainActivity.this).load(profileimageo).placeholder(R.drawable.profile).into(NavProfileImgae);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Profile name do not exists", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                userMenuSelector(menuItem);
                return false;
            }
        });

        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToPostActivity();
            }
        });
        DisplayAllUSersPost();
    }

    private void DisplayAllUSersPost() {

        FirebaseRecyclerAdapter<Posts , PostViewHolder> firebaseRecyclerAdapter =new FirebaseRecyclerAdapter<Posts, PostViewHolder>(
                Posts.class,
                R.layout.all_posts_layout,
                PostViewHolder.class,PostsRef

        ) {
            @Override
            protected void populateViewHolder(PostViewHolder viewHolder, Posts model, int position) {
                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setProfileimage(getApplicationContext() , model.getProfileimage());
                viewHolder.setPostimage(getApplicationContext() , model.getPostimage());
            }
        };
        postList.setAdapter(firebaseRecyclerAdapter);/*
        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(PostsRef , Posts.class).build();

        FirebaseRecyclerAdapter<Posts, PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Posts model) {
                holder.username.setText(model.getFullname());
                holder.time.setText(" " +model.getTime());
                holder.date.setText(" "+model.getDate());
                holder.description.setText(model.getDescription());
                Picasso.get().load(model.getProfileimage()).into(holder.user_image);
                Picasso.get().load(model.getPostimage()).into(holder.postImage);

            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return null;
            }
        };
        postList.setAdapter(firebaseRecyclerAdapter);*/
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setFullname(String fullname){
            TextView username = mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }
        public void setProfileimage(Context ctx , String profileimage){
            CircleImageView image = mView.findViewById(R.id.posts_profile_image);
            //Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(image);
            //Picasso.with(MainActivity.this).load(profileimage).placeholder(R.drawable.profile).into(image);
            Picasso.with(ctx).load(profileimage).into(image);
        }
        public void setTime(String time){
            TextView post_time = mView.findViewById(R.id.post_time);
            post_time.setText("   " + time);
        }
        public void setDate(String date){
            TextView post_date = mView.findViewById(R.id.post_date);
            post_date.setText(date);
        }
        public void setDescription(String description){
            TextView post_description = mView.findViewById(R.id.post_description);
            post_description.setText(description);
        }
        public void setPostimage(Context ctx1 , String postimage){
            ImageView post_image = mView.findViewById(R.id.post_image);
            //Picasso.get().load(postimage).placeholder(R.drawable.profile).into(post_image);
            //Picasso.with(MainActivity.this).load(postimage).placeholder(R.drawable.profile).into(post_image);
            Picasso.with(ctx1).load(postimage).into(post_image);
        }/*
    TextView username,date,time,description;
        CircleImageView user_image;
        ImageView postImage;
        public PostViewHolder(View itemView) {
            super(itemView);

            username=itemView.findViewById(R.id.post_user_name);
            date=itemView.findViewById(R.id.post_date);
            time=itemView.findViewById(R.id.post_time);
            description=itemView.findViewById(R.id.post_description);
            postImage=itemView.findViewById(R.id.post_image);
            user_image=itemView.findViewById(R.id.posts_profile_image);
        }*/
    }

    private void sendUserToPostActivity() {
        Intent postIntent  = new Intent(MainActivity.this,PostActivity.class);
        startActivity(postIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            //not authenticated
            sendUsertoLoginActivity();
        }
        else{
            checkUserExistance();
        }
    }

    private void checkUserExistance() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id)){
                    //means setup nhi kiya hai
                    //dp, username nhi h re uska
                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent  = new Intent(MainActivity.this,SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void sendUsertoLoginActivity() {
        Intent loginIntent  = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void userMenuSelector(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_post:
                sendUserToPostActivity();
                break;
            case R.id.nav_profile:
                Toast.makeText(this, "Want to see profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home pe hi hai", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                Toast.makeText(this, "Want to see friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_find_friends_home:
                Toast.makeText(this, "Want to add a post", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_messages:
                Toast.makeText(this, "Want to add a post", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_Logout:
                mAuth.signOut();
                sendUserToRegisterActivity();
                break;

        }

    }
    private void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(registerIntent);
    }
}




