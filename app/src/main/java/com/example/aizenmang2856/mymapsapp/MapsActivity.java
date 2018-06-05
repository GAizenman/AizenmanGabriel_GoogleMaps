package com.example.aizenmang2856.mymapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText locationSearch;
    private LocationManager locationManager;
    private Location myLocation;

    private boolean gotMyLocationOneTime;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;

    private static final long MIN_TIME_BW_UPDATES = 1000*5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATE = 0.0f;
    private static final int MY_LOC_ZOOM_FACTOR = 17;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //add a marker on the map showing place of birth and born here when tapped

        LatLng laJolla = new LatLng(33, -117);
        mMap.addMarker(new MarkerOptions().position(laJolla).title("born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(laJolla));


        gotMyLocationOneTime = false;
        getLocation();



    }


    public void onSearch(View v){
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        //use locationmanager for user location info
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);

        Log.d("MyMappsApp", "onSearch: location= " + location);
        Log.d("MyMappsApp", "onSearch: location= " + provider);

        LatLng userLocation = null;

        try {

            //Check the last known location, need to specifically list the provider(network or gps)
            if (locationManager != null){
                if ( (myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null){
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using NETWORK_PROVIDER user location is: " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "Userloc: " + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                }else if ( (myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null){
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using GPS_PROVIDER user location is: " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "Userloc: " + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                }else{
                    Log.d("MyMapsApp", "onSearch: myLocation is null");
                }
            }

        }catch (SecurityException | IllegalArgumentException e){
            Log.d("MyMapsApp", "Exception on getLastKnownLocation" );
        }

        if (!location.matches("")){

            //Create geocoder
            Geocoder geocoder = new Geocoder(this, Locale.US);

            try {

                //get a list of addresses
                addressList = geocoder.getFromLocationName(location, 100,
                        userLocation.latitude - (5.0/60.),
                        userLocation.longitude - (5.0/60.),
                        userLocation.latitude + (5.0/60.),
                        userLocation.longitude + (5.0/60.));

                Log.d("MyMapsApp", "created addressList" );


            }catch(IOException e) {
                e.printStackTrace();
            }

            if(!addressList.isEmpty()){
                Log.d("MyMapsApp", "Address List size: " + addressList.size() );

                for (int i = 0; i < addressList.size(); i++){

                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(i + ": " + address.getSubThoroughfare()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                }

            }

        }

    }

    //method getLocaton to place a marker at current location
    public void getLocation(){

        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            //isProviderEnabled returns tru if user has enabled gps on phone
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) Log.d("MyMapsApp", "getLocation: GPS is enabled");

            //get network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) Log.d("MyMapsApp", "getLocation: Network is enabled");

            if (!isGPSEnabled && !isNetworkEnabled){
                Log.d("MyMapsApp", "getLocation: no provider is enabled")
            } else {
                if(isNetworkEnabled) {
                    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED))
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATE, locationListenerNetwork);

            }
            if(isGPSEnabled) {
                //launch locationListenerGPS
                //Code here.....
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            }

        }catch (Exception e){
            Log.d("MyMapsApp", "getLocation: caught exception");
            e.printStackTrace();
        }
    }

    //LocationListener is an anonymous inner class
    //Setup for callbacks from the requestLocationUpdates
    LocationListener locationListenerNetwork =  new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            dropAmarker(LocationManager.NETWORK_PROVIDER);

            //Check if doing one time via our onMapReady, if so remove updates to both gps and network
            if (gotMyLocationOneTime == false){
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGps);
            }else {
                //If here then tracking so relaunch request for network
                if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED))
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATE, locationListenerNetwork);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp", "locationListenerNetwork; status change");
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }


    public void changeView(View v){

        Log.d("MyMaps", "changing view");
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }

}
