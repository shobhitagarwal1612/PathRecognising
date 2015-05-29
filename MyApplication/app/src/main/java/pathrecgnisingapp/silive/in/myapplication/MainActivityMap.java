package pathrecgnisingapp.silive.in.myapplication;
/**
 * Created by Kartikay on 29-May-15.
 */
import android.app.Dialog;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
public class MainActivityMap extends FragmentActivity implements
        LocationListener,View.OnTouchListener {
    private static final int GPS_ERRORDIALOG_REQUEST = 9001;
    @SuppressWarnings("unused")
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9002;
    GoogleMap mMap;
    Button track, stop, retrack, view;
    private Criteria criteria;
    private String provider = null;
    SupportMapFragment mapFrag;
    boolean s1 = false;
    boolean s2 = false;
    boolean flag = true;
    MediaPlayer mp;
    LocationManager locationManager;
    double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
    int x = 0;
    private static final float DEFAULTZOOM = 15;
    @SuppressWarnings("unused")
    private static final String LOGTAG = "Maps";
    /*LocationClient mLocationClient;
    */
    DB entry = new DB(MainActivityMap.this);
    /* DB info = new DB(this); */
    Polyline line;

	/* MarkerOptions options1, options2, options3; */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (servicesOK()) {
            setContentView(R.layout.activity_map);

            if (initMap()) {
initialise();
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                criteria = new Criteria();
                provider = locationManager.getBestProvider(criteria, true);
                mp= MediaPlayer.create(this, R.raw.click);

            } else {
                Toast.makeText(this, "Map not available!", Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            Toast.makeText(this, "Google Play Services not available", Toast.LENGTH_SHORT).show();
        }
track.setOnTouchListener(this);
        stop.setOnTouchListener(this);
        retrack.setOnTouchListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.splash, menu);
        return true;
    }
public void initialise(){
    track = (Button) findViewById(R.id.button1);
    stop = (Button) findViewById(R.id.button2);
    retrack = (Button) findViewById(R.id.button3);

}
    public boolean servicesOK() {
        int isAvailable = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable,
                    this, GPS_ERRORDIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to Google Play services",
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    private boolean initMap() {
        if (mMap == null) {
            mapFrag = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mMap = mapFrag.getMap();
        }
        return (mMap != null);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapTypeNone:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStop() {
        super.onStop();
        MapStateManager mgr = new MapStateManager(this);
        mgr.saveMapState(mMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MapStateManager mgr = new MapStateManager(this);
        CameraPosition position = mgr.getSavedCameraPosition();
        if (position != null) {
            CameraUpdate update = CameraUpdateFactory
                    .newCameraPosition(position);
            mMap.moveCamera(update);
            // This is part of the answer to the code challenge
            mMap.setMapType(mgr.getSavedMapType());
        }

    }

    @Override
    protected void onDestroy() {
        if (locationManager.isProviderEnabled(provider)) {
            locationManager.removeUpdates(MainActivityMap.this);
        }
        super.onDestroy();
    }

    protected void gotoCurrentLocationStart() {

        Location currentLocation = locationManager.getLastKnownLocation(provider);
        if (currentLocation == null) {
            Toast.makeText(this, "Current location isn't available",
                    Toast.LENGTH_SHORT).show();
        } else {

            LatLng ll = new LatLng(currentLocation.getLatitude(),
                    currentLocation.getLongitude());
            x1=currentLocation.getLatitude();
            y1=currentLocation.getLongitude();
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,
                    DEFAULTZOOM);
            mMap.animateCamera(update);
            String Start = "Start";
            MarkerOptions options1 = new MarkerOptions()
                    .title(Start)
                    .position(ll)
                    .anchor(.5f, .5f)
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.start));
            mMap.addMarker(options1);
        }
    }

    protected void gotoCurrentLocation() {
        Location currentLocation = locationManager.getLastKnownLocation(provider);

        if (currentLocation == null) {
            Toast.makeText(this, "Current location isn't available",
                    Toast.LENGTH_SHORT).show();
        } else {
            LatLng ll = new LatLng(currentLocation.getLatitude(),
                    currentLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,
                    DEFAULTZOOM);
            mMap.animateCamera(update);

            if (x1 == 0) {
                x1 = (double) currentLocation.getLatitude();
                y1 = (double) currentLocation.getLongitude();
            }
            x2 = (double) currentLocation.getLatitude();
            y2 = (double) currentLocation.getLongitude();
            if (Math.abs(x2 - x1) > 0.00001 || Math.abs(y2 - y1) > 0.5) {
                entry.createEntry(x2, y2);
                LatLng ll2 = new LatLng(x1, y1);
                PolylineOptions line2 = new PolylineOptions().add(ll).add(ll2)
                        .color(Color.BLUE);
                line = mMap.addPolyline(line2);
                x1 = x2;
                y1 = y2;
            }
        }
    }

    protected void gotoCurrentLocationStop() {
        flag = false;
        Location currentLocation = locationManager.getLastKnownLocation(provider);
        if (currentLocation == null) {
            Toast.makeText(this, "Current location isn't available",
                    Toast.LENGTH_SHORT).show();
        } else {
            LatLng ll = new LatLng(currentLocation.getLatitude(),
                    currentLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll,
                    DEFAULTZOOM);
            mMap.animateCamera(update);
            String Stop = "Stop";
            MarkerOptions options3 = new MarkerOptions()
                    .title(Stop)
                    .position(ll)
                    .anchor(.5f, .5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.stop));
            mMap.addMarker(options3);

            LatLng ll2 = new LatLng(x1, y1);
            PolylineOptions line2 = new PolylineOptions().add(ll).add(ll2);
            line = mMap.addPolyline(line2);
        }
    }


    @Override
    public void onLocationChanged(Location loc) {
        if (x == 0) {
            gotoCurrentLocation();
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onProviderDisabled(String provider) {
    }
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(MainActivityMap.this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(v.getId()){
            case R.id.button1:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (s2 == false) {
                        track.setBackground(getResources().getDrawable(
                                R.drawable.track2));
                    }
                    // Button Pressed
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (s2 == false) {
                        x = 1;
                        track.setBackground(getResources().getDrawable(
                                R.drawable.button_stop));
                        stop.setBackground(getResources().getDrawable(
                                R.drawable.paused));
                        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mp.start();
                        gotoCurrentLocationStart();
                        s1 = true;
                        entry.open();
                    }
                }
                break;
            case R.id.button2:
                if(event.getAction()==MotionEvent.ACTION_UP)
                {
                    s2 = true;
                    if (s1 == true) {
                        mp.start();
                        mp.stop();
                        track.setBackground(getResources().getDrawable(
                                R.drawable.ic_button));
                        stop.setBackground(getResources().getDrawable(
                                R.drawable.pause_pressed));
                        gotoCurrentLocationStop();
                        s1 = false;
                        s2 = false;
                        entry.close();
                        locationManager.removeUpdates((android.location.LocationListener) MainActivityMap.this);
                    }

                }
                break;
            case R.id.button3:
                if(event.getAction()==MotionEvent.ACTION_UP) {
                mMap.clear();
                    gotoCurrentLocation();
                }
                    break;
        }
        return true;
    }
}
