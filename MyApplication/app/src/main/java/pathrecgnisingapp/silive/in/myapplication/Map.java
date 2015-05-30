package pathrecgnisingapp.silive.in.myapplication;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
/**
 * Created by Kartikay on 30-May-15.
 */
public class Map extends FragmentActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMyLocationButtonClickListener, View.OnClickListener {
    private Button track, stop, retrack;
    private GoogleMap mMap;
    private Polyline line;
    private MapStateManager manager = null;
    private CameraPosition previousposition;
    private String start = "Start", end = "End";
    private boolean istrack = false, isretrack = false, isstop = true;
    private DB database = null;
    private double x1 = 0, y1 = 0, x2 = 0, y2 = 0; //Last known location and present location coordinates
    private LatLng previousLatLng = null, presentLatLng = null;//Considering present location and last known location
    private Location presentLocation = null;
    private GoogleApiClient mGoogleApiClient;
    private TextView mMessageView;
    // These settings are the same as the settings for the map. They will in fact give you updates
    // at the maximal rates currently possible.
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(3000)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initialise();
        setUpGoogleApiClientIfNeeded();
        mGoogleApiClient.connect();
        manager = new MapStateManager(this);
        track.setOnClickListener(this);
        stop.setOnClickListener(this);
        retrack.setOnClickListener(this);
    }

    private void initialise() {
        database = new DB(this);
        track = (Button) findViewById(R.id.button1);
        stop = (Button) findViewById(R.id.button2);
        retrack = (Button) findViewById(R.id.button3);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if (istrack || isretrack) {
            setUpGoogleApiClientIfNeeded();
            mGoogleApiClient.connect();
        }
        restoreMapState();
    }

    public void restoreMapState() {
        previousposition = manager.getSavedCameraPosition();
        if (previousposition != null) {
            CameraUpdate update = CameraUpdateFactory
                    .newCameraPosition(previousposition);
            mMap.moveCamera(update);
            // This is part of the answer to the code challenge
            mMap.setMapType(manager.getSavedMapType());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    public void storeCurrentLocation(Location location) {
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
                mMap.setOnMyLocationButtonClickListener(this);
            }
        }
    }

    private void setUpGoogleApiClientIfNeeded() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }
    /**
     * Button to get current Location. This demonstrates how to get the current Location as required
     * without needing to register a LocationListener.
     */
    /**
     * Implementation of {@link LocationListener}.
     */
    public LatLng getLatLng(double x, double y) {
        return new LatLng(x, y);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (istrack || isretrack) {
            if (getPreviousLatLng() != null) {
                LatLng now = getLatLng(location.getLatitude(), location.getLongitude());
                PolylineOptions polylineOptions = new PolylineOptions().add(now).add(getPreviousLatLng()).color(Color.BLUE);
                line=mMap.addPolyline(polylineOptions);
                setPreviousLatLng(getLatLng(location.getLatitude(), location.getLongitude()));
            }
            Toast.makeText(this, "Receievd Location Change", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                REQUEST,
                Map.this);  // LocationListener
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveMapState();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void requestLocationRequest() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                REQUEST,
                Map.this);
    }

    public void stopLocationRequest() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();

    }

    public void setPreviousLatLng(LatLng ll) {
        previousLatLng = ll;
    }

    public LatLng getPreviousLatLng() {
        if (previousLatLng != null)
            return previousLatLng;
        return null;
    }

    public void saveMapState() {
        manager.saveMapState(mMap);
    }

    public CameraPosition getSavedPosition() {
        return manager.getSavedCameraPosition();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:

                if (isstop) {
                    Toast.makeText(this, "Track Cliecked", Toast.LENGTH_SHORT).show();
                    mMap.clear();
                    database.open();
                    istrack = true;
                    isstop = false;
                    presentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (presentLocation != null) {
                        setPreviousLatLng(new LatLng(presentLocation.getLatitude(), presentLocation.getLongitude()));
                        MarkerOptions options1 = new MarkerOptions()
                                .title(start)
                                .position(new LatLng(presentLocation.getLatitude(), presentLocation.getLongitude()))
                                .anchor(.5f, .5f)
                                .icon(BitmapDescriptorFactory
                                        .fromResource(R.drawable.start));
                        mMap.addMarker(options1);
                    } else {
                        Toast.makeText(this, "Present Location Null", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            case R.id.button2:
                Toast.makeText(this, "Location Request Stopped", Toast.LENGTH_SHORT).show();
                if (istrack) {
                    database.close();
                    istrack = false;
                    isstop = true;
                    presentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (presentLocation != null) {
                        PolylineOptions polylineOptions = new PolylineOptions().add(getLatLng(presentLocation.getLatitude(), presentLocation.getLongitude()))
                                .add(getPreviousLatLng()).
                                color(Color.BLUE);
                        line=mMap.addPolyline(polylineOptions);
                        MarkerOptions options1 = new MarkerOptions()
                                .title(end)
                                .position(new LatLng(presentLocation.getLatitude(), presentLocation.getLongitude()))
                                .anchor(.5f, .5f)
                                .icon(BitmapDescriptorFactory
                                        .fromResource(R.drawable.stop));
                        mMap.addMarker(options1);
                    }
                    stopLocationRequest();
                    Toast.makeText(this, "Location Request Stopped", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button3:
                break;
        }
    }
}