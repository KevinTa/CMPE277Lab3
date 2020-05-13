package com.example.googlemapdemo;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.List;


public class GetPlaceData extends AsyncTask<Object, String, String> {

    String googlePlaceData;
    GoogleMap mMap;
    String url;
    double currentPlaceLat;
    double currentPlaceLng;

    @Override
    protected String doInBackground(Object... params) {
        try {
            Log.d("GetPlaceData", "doInBackground entered");
            mMap = (GoogleMap) params[0];
            url = (String) params[1];
            DownloadUrl downloadUrl = new DownloadUrl();
            googlePlaceData = downloadUrl.readUrl(url);
            Log.d("GooglePlaceReadTask", "doInBackground Exit");
        } catch (Exception e) {
            Log.d("GooglePlaceReadTask", e.toString());
        }
        return googlePlaceData;
    }

    @Override
    protected void onPostExecute(String result) {

        try {
            Log.d("Places", "parse");
            JSONObject jsonObject = new JSONObject((String) result);
            JSONArray jsonArray = jsonObject.getJSONArray("candidates");
            JSONObject jsonResult = jsonArray.getJSONObject(0);
            Log.d("onPostExecute","Entered into showing locations");
            MarkerOptions markerOptions = new MarkerOptions();

            currentPlaceLat = Double.parseDouble(jsonResult.getJSONObject("geometry").getJSONObject("location").getString("lat"));
            currentPlaceLng = Double.parseDouble(jsonResult.getJSONObject("geometry").getJSONObject("location").getString("lng"));
            String placeName = jsonResult.getString("name");
            String address = jsonResult.getString("formatted_address");
            LatLng latLng = new LatLng(currentPlaceLat, currentPlaceLng);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            markerOptions.position(latLng);
            markerOptions.title(placeName + " : " + address);
            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        } catch (JSONException e) {
            Log.d("Places", "parse error");
            e.printStackTrace();
        }
        Log.d("GooglePlacesReadTask", "onPostExecute Entered");
        Log.d("GooglePlacesReadTask", "onPostExecute Exit");
    }

    private void ShowPlace(List<HashMap<String, String>> nearbyPlacesList) {
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            Log.d("onPostExecute","Entered into showing locations");
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
            currentPlaceLat = Double.parseDouble(googlePlace.get("lat"));
            currentPlaceLng = Double.parseDouble(googlePlace.get("lng"));
            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            LatLng latLng = new LatLng(currentPlaceLat, currentPlaceLng);
            markerOptions.position(latLng);
            markerOptions.title(placeName + " : " + vicinity);
            mMap.addMarker(markerOptions);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }
    }
    private String getNearbyUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + 1000);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyB0vjByIt7aKRs3rj3Z_34OHfu0hnBkhbM");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }
}
