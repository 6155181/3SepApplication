package com.example.a3sepapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Button btnChoose, btnUpload;
    private ImageView imageView;
    private LinearLayout linearPictures;
    private Uri filePath;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private EditText etPictureName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);

        imageView = findViewById(R.id.imageView);
        linearPictures = findViewById(R.id.linearPictures);
        etPictureName = findViewById(R.id.etPictureName);
        // fireBase storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        downLoadList();
    }

    private void chooseImage() {
        Intent intent =new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void uploadImage() {

        String pictureName = etPictureName.getText().toString().trim();
        if(pictureName.length()==0) {
            Toast.makeText(this,"Enter name...", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Wait...");
        progressDialog.show();

        //StorageReference ref = storageReference.child("images/"+UUID.randomUUID().toString());
        StorageReference ref = storageReference.child("images/"+pictureName); // +UUID.randomUUID()
        ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot snapshot) {
                progressDialog.dismiss();
                etPictureName.setText("");
                imageView.setImageResource(R.drawable.baseline_image_search_24);
                downLoadList();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Failed",Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                int progress = (int)(100*(float)snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                progressDialog.setMessage(progress+"%");
            }
        });
    }
    public void downLoadList(){
        StorageReference storageListReference = storage.getReference().child("images");
        storageListReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                linearPictures.removeAllViews();
                for(StorageReference item: listResult.getItems()){
                    item.getBytes(999999999).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            ImageView iv = new ImageView(MainActivity.this);
                            iv.setImageBitmap(bitmap);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 500);
                            layoutParams.setMargins(0, 35, 0 ,0);
                            iv.setLayoutParams(layoutParams);
                            linearPictures.addView(iv);

                            TextView tv = new TextView(MainActivity.this);
                            tv.setText(item.getName().trim());
                            tv.setGravity(Gravity.CENTER);
                            tv.setTextSize(20);
                            linearPictures.addView(tv);

                            View viewLine = new View(MainActivity.this);
                            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,  35);
                            lineParams.setMargins(0, 35, 0, 0);
                            viewLine.setLayoutParams(lineParams);
                            viewLine.setBackgroundColor(Color.parseColor("#eeeecc"));
                            linearPictures.addView(viewLine);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }


}