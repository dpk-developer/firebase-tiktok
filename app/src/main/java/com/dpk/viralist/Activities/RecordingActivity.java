package com.dpk.viralist.Activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
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

public class RecordingActivity extends AppCompatActivity {

    private int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 100;
    private VideoView videoView;
    private Uri videoUri;
    private Button mPlay, mUpload, mGallery;
    private EditText mDesc, mTitle;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private Member member;
    private CardView mProgress;
    private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        init();
        windowFlags();

        member = new Member();
        storageReference = FirebaseStorage.getInstance().getReference("Video");
        databaseReference = FirebaseDatabase.getInstance("https://machine-test-brain-inventory-default-rtdb.firebaseio.com/").getReference("videos");

        mDesc = findViewById(R.id.etDesc);
        mTitle = findViewById(R.id.etTitle);
        videoView = findViewById(R.id.videoView);
        mProgress = findViewById(R.id.cvProgress);

        mPlay = findViewById(R.id.btnPlay);
        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previewRecord();
            }
        });

        mGallery = findViewById(R.id.btnGallery);
        mGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RecordingActivity.this, GalleryActivity.class));
                finish();
            }
        });

        mUpload = findViewById(R.id.btnUpload);
        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadVideo();
            }
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        windowFlags();
    }

    private void windowFlags() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportActionBar().hide();
    }

    private void init() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {

            videoUri = intent.getData();
            previewRecord();
        }
    }

    private void previewRecord() {

        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        videoView.start();

    }

    private String getExt(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void UploadVideo() {
        mProgress.setVisibility(View.VISIBLE);
        String videoDesc = mTitle.getText().toString();
        String videoTitle = mTitle.getText().toString();

//        Log.e("Info: ", videoDesc + " " + videoTitle + " " + videoUri.toString());

        if (videoUri != null && !TextUtils.isEmpty(videoTitle)) {

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
                                mDesc.setText(null);
                                mTitle.setText(null);
                                mProgress.setVisibility(View.GONE);
                                Toast.makeText(RecordingActivity.this, "Video Upload Successfully", Toast.LENGTH_SHORT).show();

                                member.setUrl(downloadUrl.toString());
                                member.setTitle(videoTitle);
                                member.setDesc(videoDesc);

                                String i = databaseReference.push().getKey();
                                databaseReference.child(i).setValue(member);

                                startActivity(new Intent(RecordingActivity.this, MainActivity.class));
                                finish();
                            } else {
                                mProgress.setVisibility(View.GONE);
                                Toast.makeText(RecordingActivity.this, "Video Uploading Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        } else {
            mProgress.setVisibility(View.GONE);
            Toast.makeText(this, "All Fields are required", Toast.LENGTH_SHORT).show();
        }

    }
}