package com.example.googlemapdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.SearchView;
import android.widget.Button;
import android.view.View;
import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    DrawerLayout dLayout;
    GetPlaceData currentPlaceData = null;
    GetNearbyPlacesData currentPlacesData = null;
    SearchView searchView;
    GoogleMap mMap;
    private MapView mMapView;
    private static boolean isGpsEnabled = false;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private int PROXIMITY_RADIUS = 1000;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    double latitude;
    double longitude;
    Button seeNearbyCafesButton;
    TabLayout tabLayout;
    Intent recyclerListIntent;
    String rawPlacesData = " ";
    String currentRequestUrl = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        seeNearbyCafesButton = (Button) findViewById(R.id.near_cafe_button);
        seeNearbyCafesButton.setVisibility(View.GONE);
        tabLayout = (TabLayout) findViewById(R.id.simpleTabLayout);
        TabLayout.Tab firstTab = tabLayout.newTab();
        firstTab.setText("First");
        firstTab.setIcon(R.drawable.ic_launcher_background);
        tabLayout.addTab(firstTab, true);
        TabLayout.Tab secondTab = tabLayout.newTab();
        secondTab.setText("Second");
        secondTab.setIcon(R.drawable.ic_launcher_background);
        currentRequestUrl = getIntent().getStringExtra("currentUrl");
        tabLayout.addTab(secondTab, false);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        recyclerListIntent = null;
                        break;
                    case 1:
                        recyclerListIntent = new Intent(MainActivity.this, Main2Activity.class);
                        if(currentRequestUrl != null && currentRequestUrl.trim() != "")
                        {
                            rawPlacesData = currentPlacesData.rawLocationData;
                        }
                        recyclerListIntent.putExtra("rawLocationsData", rawPlacesData);
                        recyclerListIntent.putExtra("currentRequestUrl", currentRequestUrl);
                        break;
                }
                if (recyclerListIntent != null) {
                    MainActivity.this.startActivity(recyclerListIntent);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        if(getIntent().getStringExtra("currentUrl") != null)
        {
            currentRequestUrl = getIntent().getStringExtra("currentUrl");
        }
        mMapView = (MapView) findViewById(R.id.map_view);
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }


    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public boolean checkConnection() {
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (null != activeNetwork)
        {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI){
                Snackbar.make(findViewById(R.id.map_view), "Wifi Enabled", Snackbar.LENGTH_LONG).show();
                return true;
            }

            else if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
            {
                Snackbar.make(findViewById(R.id.map_view), "Data Network Enabled", Snackbar.LENGTH_LONG).show();
                return true;
            }
            else
            {
                Snackbar.make(findViewById(R.id.map_view), "No Internet Connection", Snackbar.LENGTH_LONG).show();
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

    }

    public boolean checkGPS(Context mContext) {
        LocationManager lm = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        assert lm != null;
        isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGpsEnabled) {
            return false;
        }
        return true;
    }


    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        searchView = (SearchView) findViewById(R.id.sv_location);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                              @Override
                                              public boolean onQueryTextSubmit(String query) {
                                                  String location = searchView.getQuery().toString();
                                                  if (location != null || !location.equals("")) {
                                                      String placeUrl = getPlaceUrl(latitude, longitude, location);
                                                      Object[] DataTransfer = new Object[2];
                                                      DataTransfer[0] = mMap;
                                                      DataTransfer[1] = placeUrl;
                                                      Log.d("onLoadPlace", placeUrl);
                                                      currentPlaceData = new GetPlaceData();
                                                      if(checkConnection() == true) {
                                                          currentPlaceData.execute(DataTransfer);
                                                          Snackbar.make(findViewById(R.id.map_view), "Got queried location", Snackbar.LENGTH_LONG).show();
                                                          seeNearbyCafesButton.setVisibility(View.VISIBLE);
                                                      }
                                                      else
                                                      {
                                                          Snackbar.make(findViewById(R.id.map_view), "No Internet Connection", Snackbar.LENGTH_LONG).show();
                                                      }
                                                  }
                                                    return false;
                                              }

                                              @Override
                                              public boolean onQueryTextChange(String newText) {
                                                  return false;
                                              }
                                          }

        );
        seeNearbyCafesButton.setOnClickListener(new View.OnClickListener() {
            String cafe = "cafe";

            @Override
            public void onClick(View v) {
                Log.d("onClick", "Cafe button is Clicked");
                String url = getNearbyUrl(currentPlaceData.currentPlaceLat, currentPlaceData.currentPlaceLng, cafe);
                Object[] DataTransfer = new Object[2];
                DataTransfer[0] = mMap;
                DataTransfer[1] = url;
                Log.d("onClick", url);
                currentPlacesData = new GetNearbyPlacesData();
                currentRequestUrl = url;
                if(checkConnection() == true) {
                    currentPlacesData.execute(DataTransfer);
                    Snackbar.make(findViewById(R.id.map_view), "Got nearby cafes.", Snackbar.LENGTH_LONG).show();
                }
                else
                {
                    Snackbar.make(findViewById(R.id.map_view), "No Internet Connection", Snackbar.LENGTH_LONG).show();
                }
            }
        });
        if (currentRequestUrl != null) {
            Object[] DataTransfer = new Object[2];
            DataTransfer[0] = mMap;
            DataTransfer[1] = currentRequestUrl;
            currentPlacesData = new GetNearbyPlacesData();
            if(checkConnection() == true) {
                currentPlacesData.execute(DataTransfer);
                Snackbar.make(findViewById(R.id.map_view), "Got nearby cafes.", Snackbar.LENGTH_LONG).show();
            }
            else
            {
                Snackbar.make(findViewById(R.id.map_view), "No Internet Connection", Snackbar.LENGTH_LONG).show();
            }
        }
        if(CheckGooglePlayServices() == true) {
            Snackbar.make(findViewById(R.id.map_view), "Google Play Services initialized!", Snackbar.LENGTH_LONG).show();
        }
        else
        {
            Snackbar.make(findViewById(R.id.map_view), "Google Play Services not initialized!", Snackbar.LENGTH_LONG).show();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private String getNearbyUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + 1500);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyB0vjByIt7aKRs3rj3Z_34OHfu0hnBkhbM");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    private String getPlaceUrl(double latitude, double longitude, String keywords) {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/findplacefromtext/json?");
        String[] keywordsArray = keywords.trim().split("\\s+");
        String newKeywordString = "";
        for(int i = 0; i < keywordsArray.length; i++)
        {
            newKeywordString = newKeywordString + keywordsArray[i];
            newKeywordString = newKeywordString + "%20";
        }
        googlePlaceUrl.append("input=" + newKeywordString);
        googlePlaceUrl.append("&inputtype=" + "textquery");
        googlePlaceUrl.append("&fields=" + "formatted_address" + "," + "geometry" + "," + "name" + "," + "opening_hours" + "," + "rating");
        googlePlaceUrl.append("&locationbias=" + "circle:" + PROXIMITY_RADIUS + "@" + latitude + "," + longitude);
        googlePlaceUrl.append("&key=" + "AIzaSyB0vjByIt7aKRs3rj3Z_34OHfu0hnBkhbM");
        Log.d("getUrl", googlePlaceUrl.toString());
        return (googlePlaceUrl.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Snackbar.make(findViewById(R.id.map_view), "Connection Suspended!", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged", "entered");

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        if(checkGPS(MainActivity.this) == true) {
            Snackbar.make(findViewById(R.id.map_view), "GPS enabled!", Snackbar.LENGTH_LONG).show();
        }

        else
        {
            Snackbar.make(findViewById(R.id.map_view), "GPS not enabled!", Snackbar.LENGTH_LONG).show();
        }

        Log.d("onLocationChanged", String.format("latitude:%.3f longitude:%.3f",latitude,longitude));

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d("onLocationChanged", "Removing Location Updates");
        }
        Log.d("onLocationChanged", "Exit");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Snackbar.make(findViewById(R.id.map_view), "Connection Failed!", Snackbar.LENGTH_LONG).show();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

}
