package com.dpk.viralist.Activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.dpk.viralist.R;
import com.dpk.viralist.ViewModel.Member;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class GalleryActivity extends AppCompatActivity {

    private static final int PICK_VIDEO = 1;
    private VideoView videoView;
    private Button mPlay, mUpload;
    private CardView mProgress;
    private EditText mDesc, mTitle;
    private MediaController mediaController;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private Member member;
    private UploadTask uploadTask;
    private Uri videoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        member = new Member();
        storageReference = FirebaseStorage.getInstance().getReference("Video");
        databaseReference = FirebaseDatabase.getInstance("https://machine-test-brain-inventory-default-rtdb.firebaseio.com/").getReference("videos");

        videoView = findViewById(R.id.videoView);
        mUpload = findViewById(R.id.btnUpload);
        mPlay = findViewById(R.id.btnPlay);
        mProgress = findViewById(R.id.cvProgress);
        mDesc = findViewById(R.id.etDesc);
        mTitle = findViewById(R.id.etTitle);
        videoView.start();

        ChooseVideo();

        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadVideo();
            }
        });

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previewRecord();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO || resultCode == RESULT_OK ||
                data != null || data.getData() != null) {
            videoUri = data.getData();

            videoView.setVideoURI(videoUri);
        }

    }

    private void previewRecord() {

        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        videoView.start();

    }

    public void ChooseVideo() {

        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_VIDEO);

    }

    private String getExt(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    public void ShowVideo(View view) {

//        Intent intent = new Intent(GalleryActivity.this,Showvideo.class);
//        startActivity(intent);

    }

    private void UploadVideo() {
        String videoName = mTitle.getText().toString();
        String videoTitle = mTitle.getText().toString();
        String search = mTitle.getText().toString();
        if (videoUri != null || !TextUtils.isEmpty(videoName)) {

            mProgress.setVisibility(View.VISIBLE);
            final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getExt(videoUri));
            uploadTask = reference.putFile(videoUri);

            Task<Uri> urltask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return reference.getDownloadUrl();
                }
            })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            if (task.isSuccessful()) {
                                Uri downloadUrl = task.getResult();
                                mProgress.setVisibility(View.GONE);
                                Toast.makeText(GalleryActivity.this, "Video Upload Successfully!", Toast.LENGTH_SHORT).show();

                                member.setDesc(videoName);
                                member.setUrl(downloadUrl.toString());
                                member.setTitle(search);
                                String i = databaseReference.push().getKey();
                                databaseReference.child(i).setValue(member);

                                startActivity(new Intent(GalleryActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(GalleryActivity.this, "Video Uploading Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        } else {
            Toast.makeText(this, "All Fields are required", Toast.LENGTH_SHORT).show();
        }

    }
}