package com.sms.driversapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng sliitLocation, myLocation, lastLocation;
    private float defaultZoomLevel;
    private Marker sliitMarker, meMarker;
    private Location currentLocation;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback mLocationCallback;

    private List<LatLng> getOnLatLngs = new ArrayList<>();
    private List<LatLng> getOffLatLngs = new ArrayList<>();

    private boolean mRequestingLocationUpdates;

    public void populateLatLngs(){

        //TODO: Obtain json from api
        String json_string = "{\n" +
                "\"getOn\":[\n" +
                "{\"lat\":6.909743, \"lon\":79.971311},\n" +
                "{\"lat\":6.905738, \"lon\":79.963592},\n" +
                "{\"lat\":6.903992, \"lon\":79.954715},\n" +
                "{\"lat\":6.907996, \"lon\":79.944765},\n" +
                "{\"lat\":6.904119, \"lon\":79.924223},\n" +
                "{\"lat\":6.903267, \"lon\":79.907112}\n" +
                "]\n" +
                ",\n" +
                "\"getOff\":[\n" +
                "{\"lat\":6.911490, \"lon\":79.865000},\n" +
                "{\"lat\":6.911575, \"lon\":79.858824},\n" +
                "{\"lat\":6.911950, \"lon\":79.853837},\n" +
                "{\"lat\":6.911703, \"lon\":79.852091}\n" +
                "]\n" +
                "}";

        try {
            JSONObject obj = new JSONObject(json_string);
            JSONArray getOnList = obj.getJSONArray("getOn");
            JSONArray getOffList = obj.getJSONArray("getOff");

            // getOnList
            for (int i = 0; i < getOnList.length(); i++) {
                JSONObject coordinate = getOnList.getJSONObject(i);
                double lat = coordinate.getDouble("lat");
                double lon = coordinate.getDouble("lon");
                getOnLatLngs.add(new LatLng(lat,lon));
            }

            // getOffList
            for (int i = 0; i < getOffList.length(); i++) {
                JSONObject coordinate = getOffList.getJSONObject(i);
                double lat = coordinate.getDouble("lat");
                double lon = coordinate.getDouble("lon");
                getOffLatLngs.add(new LatLng(lat,lon));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);



    }

    protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    55);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                System.out.println("########################################################################");
                System.out.println("location: " + location.toString());
                System.out.println("########################################################################");
                if (location != null) {
                    currentLocation = location;
                    lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    if(meMarker!=null) {
                        sliitMarker.remove();
                    }
                    meMarker = mMap.addMarker(new MarkerOptions().position(lastLocation).title(String.valueOf(location.getTime()) + " - " + location.getAccuracy()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, defaultZoomLevel));
                }
            }
        });

        // set sliit location as the default marker (this will be replaced on successful location update)
        sliitLocation = new LatLng(6.914704, 79.9731237);
        defaultZoomLevel = 16.0f;
        if (lastLocation != null) {
            myLocation = lastLocation;
        } else {
            myLocation = sliitLocation;
        }

        sliitMarker = mMap.addMarker(new MarkerOptions().position(myLocation).title("SLIIT"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, defaultZoomLevel));


        populateLatLngs();

        // Plant getOn marks
        for (LatLng latlng:getOnLatLngs) {
            System.out.println("getOnLatLngs ");
            System.out.println(latlng.latitude + " , " + latlng.longitude);
            mMap.addMarker(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        }

        // Plant getOff marks
        for (LatLng latlng:getOffLatLngs) {
            System.out.println("getOffLatLngs ");
            System.out.println(latlng.latitude + " , " + latlng.longitude);
            mMap.addMarker(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }

        // periodic location updates

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5*1000);
        mLocationRequest.setFastestInterval(2*1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    System.out.println("live location: " + location.toString());
                    if (location != null) {
                        currentLocation = location;
                        lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if(meMarker!=null){
                            meMarker.remove();
                        }
                        meMarker = mMap.addMarker(new MarkerOptions().position(lastLocation).title(String.valueOf(location.getTime()) + " - " + location.getAccuracy()));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, defaultZoomLevel));
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (mRequestingLocationUpdates) {
//            startLocationUpdates();
//        }
//    }

    private void startLocationUpdates() {

    }
}
