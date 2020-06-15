package com.example.smalldots.Activities;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.transition.Transition;
import androidx.viewpager.widget.ViewPager;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.example.smalldots.MainActivity;
import com.example.smalldots.R;
import com.example.smalldots.adapter.ProfileViewPagerAdapter;
import com.example.smalldots.model.User;
import com.example.smalldots.rest.ApiClient;
import com.example.smalldots.rest.services.UserInterface;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

public class profileActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {

//    @BindView(R.id.profile_cover)
//    ImageView profileCover;
//    @BindView(R.id.profile_image)
//    CircleImageView profileImage;
//    @BindView(R.id.profile_option_btn)
//    Button profileOptionBtn;
//    @BindView(R.id.toolbar)
//    Toolbar toolbar;
//    @BindView(R.id.collapsing_toolbar)
//    CollapsingToolbarLayout collapsingToolbar;
//    @BindView(R.id.appbar)
//    AppBarLayout appbar;
//    @BindView(R.id.ViewPager_profile)
//    ViewPager ViewPagerProfile;

    int current_state = 0;
    String uid = "0", imageUploadURL = "";
    ProfileViewPagerAdapter profileViewPagerAdapter;
    ViewPager viewPager;
    Toolbar toolbar;
    Button profile_btn;
    String profileURL = "", coverURL;
    CollapsingToolbarLayout collapsingToolbarLayout;
    CircleImageView profileImage;
    ImageView profileCover;
    ProgressDialog progressDialog;
    int imageuploadtype = 0;
    File commpressImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //for hidding status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile);

        uid = getIntent().getStringExtra("uid");
        viewPager = findViewById(R.id.ViewPager_profile);
        profile_btn = findViewById(R.id.profile_option_btn);
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        profileImage = findViewById(R.id.profile_image);
        profileCover = findViewById(R.id.profile_cover);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Loading....");
        progressDialog.show();

        // ButterKnife.bind(this);
        profileViewPagerAdapter = new ProfileViewPagerAdapter(getSupportFragmentManager(), 1);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(profileActivity.this, MainActivity.class));
            }
        });
        viewPager.setAdapter(profileViewPagerAdapter);
  /*

    0 = profile is still loading
    1=  two people are friends ( unfriend )
    2 = this person has sent friend request to another friend ( cancel sent requeset )
    3 = this person has received friend request from another friend  (  reject or accept request )
    4 = people are unkown ( you can send requeset )
    5 = own profile
     */
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(uid)) {
            // uid matched , means its our own profile
            current_state = 5;
            profile_btn.setText("Edit Profile");
            loadownprofile();
        } else {


            // load others profile unknown
            loadOthersProfile();
        }
        profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile_btn.setEnabled(false);
                if (current_state == 5) {
                    CharSequence options[] = {"Change Cover profile", "Change Profile Picture", "View Cover Picture", "View Profiel Picture"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(profileActivity.this);
                    builder.setOnDismissListener(profileActivity.this);
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {

                            if (position == 0) {
                                //CHange cover pic
                                imageuploadtype = 1;
                                ImagePicker.create(profileActivity.this)
                                        .folderMode(true)
                                        .single().toolbarFolderTitle("Choose a Folder").toolbarImageTitle("Select a Image")
                                        .start();
                            } else if (position == 1) {
                                //CHange  Profile pic
                                imageuploadtype = 0;
                                ImagePicker.create(profileActivity.this)
                                        .folderMode(true)
                                        .single().toolbarFolderTitle("Choose a Folder").toolbarImageTitle("Select a Image")
                                        .start();
                            } else if (position == 2) {
                                //view Profile Cover
                                viewImage(profileCover, coverURL);
                            } else {
                                //view Profile pic
                                viewImage(profileImage, profileURL);
                            }
                        }
                    });
                    builder.show();
                } else if (current_state == 4) {
                    CharSequence options[] = {"Send Friend Request"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(profileActivity.this);
                    builder.setOnDismissListener(profileActivity.this);
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {

                            if (position == 0) {
                                performAction(current_state);
                                profile_btn.setText("Processing ....");
                            }
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    private void performAction(int i) {
        // Toast.makeText(this, ""+uid, Toast.LENGTH_SHORT).show();
        Toast.makeText(profileActivity.this, "here is" + i, Toast.LENGTH_SHORT).show();
        UserInterface userInterface = ApiClient.getApiClient().create(UserInterface.class);
        Call<Integer> call = userInterface.performAction(new PerformAction(i + "", FirebaseAuth.getInstance().getCurrentUser().getUid(), uid));
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                profile_btn.setEnabled(true);
                if (response.body() == 1) {
                    Toast.makeText(profileActivity.this, "this is ...", Toast.LENGTH_SHORT).show();
                    if (i == 4) {
                        current_state = 2;
                        profile_btn.setText("Request Sent");
                        Toast.makeText(profileActivity.this, "request Send", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    profile_btn.setEnabled(false);
                    profile_btn.setText("Error...");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(profileActivity.this, "" + t, Toast.LENGTH_SHORT).show();
                Log.d("error", "" + t);
            }
        });
    }

    private void loadOthersProfile() {
        UserInterface userInterface = ApiClient.getApiClient().create(UserInterface.class);

        Map<String, String> params = new HashMap<String, String>();
        params.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        params.put("profileId", uid);

        Call<User> call = userInterface.loadOtherProfile(params);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull final Response<User> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    showUserData(response.body());

                    if (response.body().getState().equalsIgnoreCase("1")) {
                        profile_btn.setText("Friends");
                        current_state = 1;
                    } else if (response.body().getState().equalsIgnoreCase("2")) {
                        profile_btn.setText("Cancel Request");
                        current_state = 2;
                    } else if (response.body().getState().equalsIgnoreCase("3")) {
                        current_state = 3;
                        profile_btn.setText("Accept Request");
                    } else if (response.body().getState().equalsIgnoreCase("4")) {
                        current_state = 4;
                        profile_btn.setText("Send Request");
                    } else {
                        current_state = 0;
                        profile_btn.setText("Error");
                    }

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(profileActivity.this, "Something went wrong ... Please try later", Toast.LENGTH_SHORT).show();
            }
        });
        // Toast.makeText(Pro
    }

    private void showUserData(User user) {
        profileURL = user.getProfileURL();
        coverURL = user.getCoverURL();
        collapsingToolbarLayout.setTitle(user.getName());
        if (!profileURL.isEmpty()) {
            Picasso.with(profileActivity.this).load(profileURL).into(profileImage, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(profileActivity.this).load(profileURL).into(profileImage);
                }
            });

            if (!coverURL.isEmpty()) {
                Picasso.with(profileActivity.this).load(coverURL).into(profileCover, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(profileActivity.this).load(coverURL).into(profileCover);
                    }
                });
            }

            addImageCoverClick();
        }
    }

    private void addImageCoverClick() {
        profileCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFullImage(profileCover, coverURL);
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFullImage(profileImage, profileURL);
            }
        });
    }

    private void viewFullImage(View view, String link) {
        Intent intent = new Intent(profileActivity.this, ViewImage.class);
        intent.putExtra("imageUrl", link);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Pair[] pairs = new Pair[1];
            pairs[0] = new Pair<View, String>(view, "shared");
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(profileActivity.this, pairs);
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }

    }

    private void viewImage(View view, String URL) {
        Intent intent = new Intent(profileActivity.this, ViewImage.class);
        intent.putExtra("imageURL", URL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Pair[] pairs = new Pair[1];
            pairs[0] = new Pair<View, String>(view, "shared");
            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(profileActivity.this, pairs);
            startActivity(intent, activityOptions.toBundle());
        } else {
            startActivity(intent);
        }

    }

    private void loadownprofile() {
        UserInterface userInterface = new ApiClient().getApiClient().create(UserInterface.class);
        Map<String, String> params = new HashMap<>();

        params.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        Call<User> call = userInterface.loadownprofile(params);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                progressDialog.dismiss();
                if (response.body() != null) {
                    showUserData(response.body());
                } else {
                    Toast.makeText(profileActivity.this, "SOmething went wrong...Please try again later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(profileActivity.this, "SOmething went wrong...Please try again later", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void imageCoverClick() {
        profileCover.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                viewImage(profileCover, coverURL);
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewImage(profileImage, profileURL);
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        profile_btn.setEnabled(true);
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

                uploadfile(commpressImageFile);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("error", "" + e);
            }

        }
    }

    private void uploadfile(File commpressImageFile) {
        progressDialog.setTitle("Loading...");
        progressDialog.show();

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);

        builder.addFormDataPart("postUserId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        builder.addFormDataPart("imageUploadType", imageuploadtype + "");
        builder.addFormDataPart("file", commpressImageFile.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), commpressImageFile));
        MultipartBody multipartBody = builder.build();

        UserInterface userInterface = ApiClient.getApiClient().create(UserInterface.class);
        Call<Integer> call = userInterface.uploadImage(multipartBody);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                progressDialog.dismiss();
                if (response.body() != null && response.body() == 1) {

                    if (imageuploadtype == 0) {
                        Picasso.with(profileActivity.this).load(commpressImageFile).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image_placeholder).into(profileImage, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(profileActivity.this).load(commpressImageFile).placeholder(R.drawable.default_image_placeholder).into(profileImage);
                            }
                        });
                        Toast.makeText(profileActivity.this, "Profile Picture Changed Successfully", Toast.LENGTH_LONG).show();
                    } else {
                        Picasso.with(profileActivity.this).load(commpressImageFile).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image_placeholder).into(profileCover, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(profileActivity.this).load(commpressImageFile).placeholder(R.drawable.default_image_placeholder).into(profileCover);
                            }
                        });
                        Toast.makeText(profileActivity.this, "Cover Picture Changed Successfully", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(profileActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();

                }


            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(profileActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });

    }

    public class PerformAction {
        String operationType, userId, profileId;

        public PerformAction(String operationType, String userId, String profileId) {
            this.operationType = operationType;
            this.userId = userId;
            this.profileId = profileId;
        }

        public String getOperationType() {
            return operationType;
        }

        public String getUserId() {
            return userId;
        }

        public String getProfileId() {
            return profileId;
        }
    }
}
