package com.example.smalldots;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.smalldots.Activities.Search_activity;
import com.example.smalldots.Activities.UploadActivity;
import com.example.smalldots.Activities.profileActivity;
import com.example.smalldots.Fragments.FriendsFragment;
import com.example.smalldots.Fragments.NewsFeedFragment;
import com.example.smalldots.Fragments.NotificationFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;
    NewsFeedFragment newsFeedFragment;
    FriendsFragment friendsFragment;
    NotificationFragment notificationFragment;
    FloatingActionButton fab;
    ImageView search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);
        fab = findViewById(R.id.fab);
//        bottomNavigation.inflateMenu(R.menu.bottom_navigation_main);
//        bottomNavigation.setItemBackgroundResource(R.color.colorPrimary);
        // bottomNavigation.setItemTextColor(ContextCompat.getColorStateList(bottomNavigation.getContext(), R.color.nav_item_colors));
        // bottomNavigation.setItemIconTintList(ContextCompat.getColorStateList(bottomNavigation.getContext(), R.color.nav_item_colors));
        //BottomNavigationViewHelper.removeShiftMode(bottomNavigation);
        search = findViewById(R.id.searchbtn);
        newsFeedFragment = new NewsFeedFragment();
        friendsFragment = new FriendsFragment();
        notificationFragment = new NotificationFragment();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Search_activity.class));
            }
        });
        setFragments(newsFeedFragment);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.newsfeed_fragment:
                        setFragments(newsFeedFragment);
                        break;
                    case R.id.profile_fragment:
                        startActivity(new Intent(MainActivity.this, profileActivity.class).putExtra("uid", FirebaseAuth.getInstance().getCurrentUser().getUid()));
                        break;

                    case R.id.profile_friends:

                        setFragments(friendsFragment);
                        break;

                    case R.id.profile_notification:
                        // Toast.makeText(MainActivity.this, "yes it is", Toast.LENGTH_SHORT).show();
                        setFragments(notificationFragment);
                        break;


                }
                return false;
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, UploadActivity.class));
            }
        });

    }

    public void setFragments(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.framelayout, fragment);
        fragmentTransaction.commit();

    }

}
