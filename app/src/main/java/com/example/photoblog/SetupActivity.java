package com.example.photoblog;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView setupImage;
    private Uri mainImageURI = null;

    private ProgressDialog reg_progress;

    private EditText setupName;
    private Button setupBtn;
    private String user_id;
    private boolean isChanged = false;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        final Toolbar setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");

        reg_progress = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        setupImage = findViewById(R.id.setup_image);
        setupName = findViewById(R.id.setup_name);
        setupBtn = findViewById(R.id.setup_btn);

        setupBtn.setEnabled(false);  // disable the btn

        firebaseFirestore.collection("Users").document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().exists()){
                                String name = task.getResult().getString("name");
                                String image = task.getResult().getString("image");

                                mainImageURI = Uri.parse(image);

                                setupName.setText(name);

                                RequestOptions placeholderRequest = new RequestOptions();
                                placeholderRequest.placeholder(R.drawable.default_image);
                                Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);
                            }
                        }else {
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(SetupActivity.this, "FIRESTORE Retrieve Error: "+errorMessage, Toast.LENGTH_SHORT).show();
                        }
                        reg_progress.dismiss();
                        setupBtn.setEnabled(true);  // enabled the btn
                    }
                });



        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String user_name = setupName.getText().toString();

                if (!TextUtils.isEmpty(user_name) && mainImageURI != null) {
                // now we Sign In to the account...
                reg_progress.setTitle("Updating");
                reg_progress.setMessage("Please wait...");
                reg_progress.setCanceledOnTouchOutside(true);
                reg_progress.show();

                if(isChanged) {


                        String user_id = firebaseAuth.getCurrentUser().getUid();

                        StorageReference image_path = storageReference.child("profile_image").child(user_id + ".jpg");
                        image_path.putFile(mainImageURI)
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                        if (task.isSuccessful()) {
                                            storeFirestore(task, user_name);

                                        } else {
                                            String errorMessage = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this, "IMAGE Error: " + errorMessage, Toast.LENGTH_SHORT).show();

                                            reg_progress.dismiss();
                                        }
                                    }
                                });
                    }else {
                    storeFirestore(null,user_name);
                }
                }
            }
        });


        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ //if user is using MARSHMELLO or greater version then it will required the permission

                    //if permission is not granted
                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }else {
                        BringImagePicker();
                    }
                }else {
                    BringImagePicker();
                }


            }
        });
    }

    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String user_name) {

        Uri download_uri;
        if(task != null){
            download_uri = task.getResult().getDownloadUrl();
        }else {
            download_uri = mainImageURI;
        }


//                                      Uri download_uri = mainImageURI;  //this will work but locally
        Toast.makeText(SetupActivity.this, "The Image is Uploaded...", Toast.LENGTH_SHORT).show();

        Map<String,String> userMap = new HashMap<>();
        userMap.put("name", user_name);
        userMap.put("image",download_uri.toString());

        firebaseFirestore.collection("Users").document(user_id).set(userMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            sendToMain();
                            Toast.makeText(SetupActivity.this, "Users Settings are updated...", Toast.LENGTH_SHORT).show();
                        }else{
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(SetupActivity.this, "FIRESTORE Error: "+errorMessage, Toast.LENGTH_SHORT).show();
                        }
                        reg_progress.dismiss();
                    }
                });
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void BringImagePicker() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1) // this will crop the image into 1:1 size
                .start(SetupActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageURI = result.getUri();
                setupImage.setImageURI(mainImageURI); // this will set the image into the circle view

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
