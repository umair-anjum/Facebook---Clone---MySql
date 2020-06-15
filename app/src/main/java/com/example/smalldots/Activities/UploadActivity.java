package com.example.smalldots.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.example.smalldots.MainActivity;
import com.example.smalldots.R;
import com.example.smalldots.rest.ApiClient;
import com.example.smalldots.rest.services.UserInterface;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadActivity extends AppCompatActivity {
    public static final int PICK_IMAGE = 1;
    String uploadURL = "";
    boolean isImageSelected = false;
    ProgressDialog progressDialog;
    File commpressImageFile = null;
    int pricavylevel = 0;
    @BindView(R.id.privacy_spinner)
    Spinner privacySpinner;
    @BindView(R.id.postBtnTxt)
    TextView postBtnTxt;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.dialogAvatar)
    CircleImageView dialogAvatar;
    @BindView(R.id.status_edit)
    EditText statusEdit;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.add_image)
    Button addImage;

    /**
     * 0: friends
     * 1: only me
     * 2: public
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back_white);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading...");

        privacySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pricavylevel = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                pricavylevel = 0;
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.create(UploadActivity.this)
                        .folderMode(true)
                        .single().start();
//                Intent intent = new Intent(Intent.ACTION_PICK,
//                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);
//                intent.setType("image/*");
//                intent.putExtra("crop", "true");
//                intent.putExtra("scale", true);
//                intent.putExtra("outputX", 256);
//                intent.putExtra("outputY", 256);
//                intent.putExtra("aspectX", 1);
//                intent.putExtra("aspectY", 1);
//                intent.putExtra("return-data", true);
//                startActivityForResult(intent, 1);
            }
        });
        postBtnTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPost();
            }
        });
    }

    private void uploadPost() {
        String status = statusEdit.getText().toString();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (status.trim().length() > 0 || isImageSelected) {
            progressDialog.show();

            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);

            builder.addFormDataPart("post", status);
            builder.addFormDataPart("postUserId", userId);
            builder.addFormDataPart("privacy", pricavylevel + "");

            if (isImageSelected) {
                builder.addFormDataPart("isImageSelected", "1");
                builder.addFormDataPart("file", commpressImageFile.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), commpressImageFile));
            } else {
                builder.addFormDataPart("isImageSelected", "0");
            }

            MultipartBody multipartBody = builder.build();

            UserInterface userInterface = ApiClient.getApiClient().create(UserInterface.class);
            Call<Integer> call = userInterface.uploadStatus(multipartBody);
            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    progressDialog.dismiss();
                    if (response.body() != null && response.body() == 1) {
                        Toast.makeText(UploadActivity.this, "Post is Successfull", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UploadActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(UploadActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Toast.makeText(UploadActivity.this, "Something went wrong !!!", Toast.LENGTH_SHORT).show();
                    Log.d("this", "" + t);
                    Log.d("this1", "" + call);
                    progressDialog.dismiss();
                }
            });

        } else {
            Toast.makeText(UploadActivity.this, "Please write your post first", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image selectedImage = ImagePicker.getFirstImageOrNull(data);

            try {
                commpressImageFile = new Compressor(this)
                        .setQuality(75)
                        .compressToFile(new File(selectedImage.getPath()));

                isImageSelected = true;

                Picasso.with(UploadActivity.this).load(new File(selectedImage.getPath())).placeholder(R.drawable.default_image_placeholder).into(image);

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("error",""+e);
            }

        }

    }



//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode != RESULT_OK) {
//            return;
//        }
//        if (requestCode == 1) {
//            final Bundle extras = data.getExtras();
//            if (extras != null) {
//                //Get image
//                Bitmap newProfilePic = extras.getParcelable("data");
//                image.setImageBitmap(newProfilePic);
//            }
//        }
//    }

}