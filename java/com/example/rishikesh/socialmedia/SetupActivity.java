package com.example.rishikesh.socialmedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private EditText username, fullname, countryname;
    private Button saveinformation_button;
    private CircleImageView profile_image;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private ProgressDialog loadingBar;

    String currentUser_id;
    private final static int gallery_pick = 1;
    private StorageReference UserProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        username = findViewById(R.id.setup_username);
        fullname = findViewById(R.id.setup_fullname);
        countryname = findViewById(R.id.setup_country_name);
        saveinformation_button = findViewById(R.id.setup_information_button);
        profile_image = findViewById(R.id.setup_profile_image);

        loadingBar = new ProgressDialog(this);


        mAuth = FirebaseAuth.getInstance();
        currentUser_id = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser_id);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Images");
        //ye andar gaya userid ke

        saveinformation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInfo();
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                //image hi mangta
                startActivityForResult(galleryIntent, gallery_pick);
            }
        });
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    if(dataSnapshot.hasChild("profile image")){
                        String imagei = dataSnapshot.child("profile image").getValue().toString();
                        Toast.makeText(SetupActivity.this, "Image h re:"+imagei, Toast.LENGTH_SHORT).show();
                        //Picasso.get().load(imagei).placeholder(R.drawable.profile).into(profile_image);
                        Picasso.with(SetupActivity.this).load(imagei).placeholder(R.drawable.profile).into(profile_image);
                    }
                    else{
                        Toast.makeText(SetupActivity.this, "Plz select a profile image", Toast.LENGTH_SHORT).show();
                    }
                    //Glide.with(SetupActivity.this).load(imagei).into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == gallery_pick && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();

            //cropping daala
            CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1,1)
            .start(this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //crop button clcked
            if(resultCode == RESULT_OK){
                loadingBar.setTitle("Profile image");
                loadingBar.setMessage("Please Wait while we are updating your dp !");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                final Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImageRef.child(currentUser_id + ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String DownloadUrl = uri.toString();
                                UsersRef.child("profile image").setValue(DownloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Intent selfIntent = new Intent(SetupActivity.this,SetupActivity.class);
                                            startActivity(selfIntent);
                                            Toast.makeText(SetupActivity.this, "Image also stored", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else{
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
                /*
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SetupActivity.this, "Profile Image set hua", Toast.LENGTH_SHORT).show();
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();
                            UsersRef.child("profile image").setValue(downloadUrl)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent selfIntent = new Intent(SetupActivity.this,SetupActivity.class);
                                        startActivity(selfIntent);
                                        Toast.makeText(SetupActivity.this, "Image also stored", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else{
                                        String message = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                        }
                    }
                }); */
            }
            else{
                Toast.makeText(SetupActivity.this, "Error :try again as image cant be cropped", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void saveAccountSetupInfo() {
        String full_name,user_name,country_name;
        user_name = username.getText().toString();
        full_name = fullname.getText().toString();
        country_name = countryname.getText().toString();

        if(TextUtils.isEmpty(user_name)){
            Toast.makeText(this, "Enter Username", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(full_name)){
            Toast.makeText(this, "Enter Fullname na", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country_name)){
            Toast.makeText(this, "Enter country re", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Setting Account re");
            loadingBar.setMessage("Please Wait while we are see=tting up ur account !");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username",user_name);
            userMap.put("fullname",full_name);
            userMap.put("countryname",country_name);

            userMap.put("status","sweeper");
            userMap.put("Gender","None");
            userMap.put("dob","30-02-2000");
            userMap.put("relationship status","single");

            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {   //ye pura hua toh..
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        sendUsertoMainActivity();
                        Toast.makeText(SetupActivity.this, "Uour account is created successfully", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error:"+message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }
    private void sendUsertoMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
