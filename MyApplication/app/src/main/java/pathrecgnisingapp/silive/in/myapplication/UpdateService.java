package pathrecgnisingapp.silive.in.myapplication;

        import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Shobhit Agarwal on 30-05-2015.
 */
public class UpdateService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    final static String UPDATE_MAP = "UPDATE_MAP";
    final static String latitude = "LATITUDE";
    final static String longitude = "LONGITUDE";
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(3000)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    private DB database = null;
    private GoogleApiClient mGoogleApiClient;
    private Location previousLocation = null;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        database = new DB(this);
        Toast.makeText(this,"Service Created",Toast.LENGTH_SHORT).show();
        database.open();
        setUpGoogleApiClientIfNeeded();
        mGoogleApiClient.connect();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        database.close();
        Toast.makeText(this,"Service Destroyed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (getPreviousLocation() != null) {
            if (location.distanceTo(getPreviousLocation()) >= 0.00001) {
                database.createEntry(location.getLatitude(), location.getLongitude());
                setPreviousLocation(location);
                Intent intent = new Intent();
                intent.setAction(UPDATE_MAP);
                intent.putExtra(latitude, location.getLatitude());
                intent.putExtra(longitude, location.getLongitude());
                sendBroadcast(intent);
            }
        }else{
            database.createEntry(location.getLatitude(), location.getLongitude());
            setPreviousLocation(location);
            Intent intent = new Intent();
            intent.setAction(UPDATE_MAP);
            intent.putExtra(latitude, location.getLatitude());
            intent.putExtra(longitude, location.getLongitude());
            sendBroadcast(intent);
        }
    }

    private Location getPreviousLocation() {
        return previousLocation;
    }

    private void setPreviousLocation(Location location) {
        previousLocation = location;
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                REQUEST,
                this);
    }
    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
