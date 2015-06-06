package pathrecgnisingapp.silive.in.myapplication;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class Map extends FragmentActivity implements View.OnClickListener {
    private static final int GPS_ERRORDIALOG_REQUEST = 9001;
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9002;
    private static final float DEFAULTZOOM = 15;
    private static final String LOGTAG = "Maps";
    private static String Start = "Start", Stop = "Stop";
    private final String TAG = "PRA";
    GoogleMap mMap;
    Button track, stop, retrack, view;
    Receiver entryDetectReceiver;
    IntentFilter intentFilter;
    Intent intent;
    private PolylineOptions line;
    private int wait;//A semaphore for putting Start Marker in map wait is 1 until marker is put and then again 0 so to continue adding polyline
    private String entries, entriesArray[];
    private LatLng previousLocation = null;//Variable for getting last known location LatLng variables....
    private DB database = null;
    private boolean istrack = false, isretrack = false, isstop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (servicesOK()) {
            setContentView(R.layout.activity_map);

            if (initMap()) {
                initialise();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().isZoomControlsEnabled();
                mMap.getUiSettings().isCompassEnabled();
                mMap.getUiSettings().isMyLocationButtonEnabled();
                mMap.getUiSettings().isMapToolbarEnabled();
            } else {
                Toast.makeText(this, "Map not available!", Toast.LENGTH_SHORT)
                        .show();
            }
        }
//        final MediaPlayer mp = MediaPlayer.create(this, R.raw.click);
//        manager = new MapStateManager(this);
        track.setOnClickListener(this);
        stop.setOnClickListener(this);
        retrack.setOnClickListener(this);

    }

    public void initialise() {
        line = new PolylineOptions();
        track = (Button) findViewById(R.id.button1);
        stop = (Button) findViewById(R.id.button2);
        retrack = (Button) findViewById(R.id.button3);
        database = new DB(this);
        entryDetectReceiver = new Receiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(UpdateService.UPDATE_MAP);
        intent = new Intent(Map.this,
                pathrecgnisingapp.silive.in.myapplication.UpdateService.class);
    }

    @Override
    protected void onStop() {
        //    MapStateManager mgr = new MapStateManager(this);
        //  mgr.saveMapState(mMap);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.splash, menu);
        return true;
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
            SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager()
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
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void readFromDatabase() {
        entries = database.getData();
        entriesArray = new String[entries.split(" ").length];
        entriesArray = entries.split(" ");
        Toast.makeText(this, "Length of entries" + entriesArray.length, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                if (!isstop) {
                    istrack = true;
                    mMap.clear();
                    database.upgradeDatabase();
                    wait = 1;
                    startService(intent);
                    registerReceiver(entryDetectReceiver, intentFilter);
                    Toast.makeText(this, "Track Clicked", Toast.LENGTH_SHORT).show();
                    istrack = false;
                    isstop = true;
                }
                break;
            case R.id.button2:
                if (!istrack && isstop) {
                    unregisterReceiver(entryDetectReceiver);
                    stopService(intent);
                    MarkerOptions options3 = new MarkerOptions()
                            .title(Stop)
                            .position(previousLocation)
                            .anchor(.5f, .5f)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.stop));
                    mMap.addMarker(options3);
                    previousLocation = null;
                    //did null beacase if after one cycle of start and stop previous location will be having a finite value and next time during start or retrack it would start polyline from previous location
                    Toast.makeText(this, "Location Request Stopped", Toast.LENGTH_SHORT).show();
                    isstop = false;
                    istrack = true;
                }
                break;
            case R.id.button3:
                readFromDatabase();
                break;
        }
    }

    private class Receiver extends BroadcastReceiver {
        private Polyline pLine;

        @Override
        public void onReceive(Context context, Intent intent) {
            double latitude = intent.getDoubleExtra(UpdateService.latitude, 0);
            double longitude = intent.getDoubleExtra(UpdateService.longitude, 0);
            if (wait == 1) {
                setCurrentLocation(new LatLng(latitude, longitude));

                MarkerOptions options3 = new MarkerOptions()
                        .title("Start")
                        .position(previousLocation())
                        .anchor(.5f, .5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.start));
                mMap.addMarker(options3);
                wait = 0;
            }
            if (wait == 0) {
                if (previousLocation() != null) {
                    if (mMap == null)
                        Toast.makeText(Map.this, "onLocation change" + latitude, Toast.LENGTH_SHORT).show();
                    line = new PolylineOptions().add(new LatLng(latitude, longitude)).add(previousLocation)
                            .color(Color.BLUE);
                    mMap.addPolyline(line);
                }
                setCurrentLocation(new LatLng(latitude, longitude));
            }
        }

        private LatLng previousLocation() {
            return previousLocation;
        }

        private void setCurrentLocation(LatLng location) {
            previousLocation = location;
        }
    }
}