package com.example.rishikesh.socialmedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton selectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private static final int gallery_pick = 1;
    private Uri ImageUri;
    private String Description;
    private StorageReference PostImageRef;

    private String saveCurrentdate ,saveCurrentTime, postRandomName, currentUserId;
    private String downloadUrl;
    private DatabaseReference Usersref , PostsRef;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        selectPostImage = findViewById(R.id.selectPostImage);
        UpdatePostButton = findViewById(R.id.update_post_button);
        PostDescription = findViewById(R.id.Post_description);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        PostImageRef = FirebaseStorage.getInstance().getReference();
        Usersref = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        loadingBar = new ProgressDialog(this);

        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });
        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });
        mToolbar = (Toolbar)findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setTitle("Update Post");

    }

    private void ValidatePostInfo() {
       Description = PostDescription.getText().toString();
        if(ImageUri == null){
            Toast.makeText(this, "Plz select Post Image", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Description)){
            Toast.makeText(this, "Enter Post Decription", Toast.LENGTH_SHORT).show();
        }
        else
        {
            StoringImageToFirebaseStorage();
            loadingBar.setTitle("Add new post");
            loadingBar.setMessage("Please Wait while we are updating your new post !");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
        }
    }

    private void StoringImageToFirebaseStorage() {
        /*
        Calendar callFordate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentdate = currentDate.format(callFordate.getTime());

        Calendar callFortime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(callFordate.getTime());

        postRandomName = saveCurrentdate + saveCurrentTime;

        StorageReference filePath = PostImageRef.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");
        //upar ka image name leta + image ka poted time
        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    DownloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
                    SavingPostInformationToDatbase();
                    Toast.makeText(PostActivity.this, "Image Uploadeed re", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(PostActivity.this, "Image not uploaded", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentdate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calFordDate.getTime());

        postRandomName = saveCurrentdate + saveCurrentTime;


        StorageReference filePath = PostImageRef.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    downloadUrl = task.getResult().getDownloadUrl().toString();
                    Toast.makeText(PostActivity.this, "image uploaded successfully to Storage...", Toast.LENGTH_SHORT).show();

                    SavingPostInformationToDatabase();

                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Image upload hoke bhi andar nhi", Toast.LENGTH_SHORT).show();
                    Toast.makeText(PostActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void SavingPostInformationToDatabase() {
        Usersref.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profile image").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid" , currentUserId);
                    postMap.put("date" , saveCurrentdate);
                    postMap.put("time" , saveCurrentTime);
                    postMap.put("description" , Description);
                    postMap.put("post image" , downloadUrl);
                    postMap.put("profile image" , userProfileImage);
                    postMap.put("fullname" , userFullName);

                    PostsRef.child(currentUserId + postRandomName).updateChildren(postMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                sendUserToMainActivity();
                                loadingBar.dismiss();
                                Toast.makeText(PostActivity.this, "Post is updated re", Toast.LENGTH_SHORT).show();
                            } else{
                                loadingBar.dismiss();
                                Toast.makeText(PostActivity.this, "Post not updated", Toast.LENGTH_SHORT).show();
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

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        //image hi mangta
        startActivityForResult(galleryIntent, gallery_pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        //no crop image
        if(requestCode == gallery_pick && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();
            selectPostImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if( id == android.R.id.home){
            sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);

    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this,MainActivity.class);
        startActivity(mainIntent);
    }
}
