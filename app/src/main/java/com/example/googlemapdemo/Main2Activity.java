package com.example.googlemapdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.content.Intent;

import com.google.android.material.tabs.TabLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import android.view.Window;

import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import java.util.HashMap;

public class Main2Activity extends AppCompatActivity {
    TabLayout tabLayout;
    Intent mapIntent;
    private List<Cafe> cafeLocationList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CafeAdapter cAdapter;
    private String rawPlacesData = " ";
    private String currentUrl = " ";
    private List<HashMap<String, String>> placesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main2);
        currentUrl = getIntent().getStringExtra("currentRequestUrl");
        rawPlacesData = getIntent().getStringExtra("rawLocationsData");
        tabLayout = (TabLayout) findViewById(R.id.simpleTabLayout);
        TabLayout.Tab firstTab = tabLayout.newTab();
        firstTab.setText("First");
        firstTab.setIcon(R.drawable.ic_launcher_background);
        tabLayout.addTab(firstTab, false);
        TabLayout.Tab secondTab = tabLayout.newTab();
        secondTab.setText("Second");
        secondTab.setIcon(R.drawable.ic_launcher_background);
        tabLayout.addTab(secondTab, true);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        mapIntent = new Intent(Main2Activity.this, MainActivity.class);
                        mapIntent.putExtra("currentUrl", currentUrl);
                        break;
                    case 1:
                        mapIntent = null;
                        break;
                }
                if(mapIntent != null)
                {
                    Main2Activity.this.startActivity(mapIntent);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        cAdapter = new CafeAdapter(cafeLocationList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cAdapter);
            if(currentUrl != null) {
                DataParser placeParser = new DataParser();
                placesList = placeParser.parse(rawPlacesData);
                prepareCafeData(placesList);
            }
        }

    private void prepareCafeData(List<HashMap<String, String>> cafeList) {
        for (int i = 0; i < cafeList.size(); i++) {
            Log.d("onPostExecute", "Entered into cafe recycler view");
            String name = "";
            String vicinity = "";
            String rating = "";
            HashMap<String, String> googlePlace = cafeList.get(i);
            name = googlePlace.get("place_name");
            vicinity = googlePlace.get("vicinity");
            if(googlePlace.get("rating") != null)
            {
                rating = googlePlace.get("rating");
            }
            Cafe cafe = new Cafe(vicinity, name, "rating: " + rating);
            cafeLocationList.add(cafe);
        }
        cAdapter.notifyDataSetChanged();
    }

}
