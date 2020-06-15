package com.example.smalldots.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smalldots.MainActivity;
import com.example.smalldots.R;
import com.example.smalldots.adapter.Search_Adapter;
import com.example.smalldots.model.User;
import com.example.smalldots.rest.ApiClient;
import com.example.smalldots.rest.services.UserInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Search_activity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search_recy)
    RecyclerView searchRecy;

    Search_Adapter search_adapter;
    List<User> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_activity);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back_white);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Search_activity.this, MainActivity.class));
            }
        });
        search_adapter = new Search_Adapter(Search_activity.this, users);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Search_activity.this);
        searchRecy.setLayoutManager(layoutManager);
        searchRecy.setAdapter(search_adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchview_menu,menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.Search_icon).getActionView();
        searchView.setIconified(true);
//        ((EditText) searchView.findViewById(R.id.search_src_text)).setTextColor(getResources().getColor(R.color.hint_color));
//        ((EditText) searchView.findViewById(R.id.search_src_text)).setHintTextColor(getResources().getColor(R.color.hint_color));
//        ((ImageView) searchView.findViewById(R.id.search_close_btn)).setImageResource(R.drawable.icon_clear);
        searchView.setQueryHint("Search People ");
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFromDb(query, true);
            //    Toast.makeText(Search_activity.this, ""+query, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() > 2) {
                    searchFromDb(query, false);
                } else {
                    users.clear();
                   search_adapter.notifyDataSetChanged();
                }


                return true;
            }
        });

        return true;
    }
    private void searchFromDb(String query, boolean b) {
        UserInterface userInterface = ApiClient.getApiClient().create(UserInterface.class);
        Map<String, String> params = new HashMap<String, String>();
        params.put("keyword", query);

        Call<List<User>> call = userInterface.search(params);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {

                users.clear();
                users.addAll(response.body());
                search_adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

            }
        });
    }
}
