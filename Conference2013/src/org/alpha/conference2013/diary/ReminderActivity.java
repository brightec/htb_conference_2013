package org.alpha.conference2013.diary;

import org.alpha.conference2013.R;
import org.alpha.conference2013.data.DataStore;
import org.alpha.conference2013.data.Room;
import org.alpha.conference2013.data.Session;
import org.alpha.conference2013.data.Venue;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ReminderActivity extends SherlockFragmentActivity {

    public static final String EXTRA_SESSION_ID = "EXTRA_SESSION_ID";
    public static final String EXTRA_REMINDER = "EXTRA_REMINDER";

    private ActionBar mActionBar;
    private Session session;
    private Room room;
    private Venue venue;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        int sessionId = getIntent().getIntExtra(EXTRA_SESSION_ID, 0);
        int reminder = getIntent().getIntExtra(EXTRA_REMINDER, 0);
        session = DataStore.session(this, sessionId);
        room = DataStore.room(this, session.roomId);
        if (room != null) {
            venue = DataStore.venue(this, room.venueId);
        }
        
        mActionBar = getSupportActionBar(); 
        mActionBar.setTitle("Session Alert");
        //mActionBar.setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.reminder_activity);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (savedInstanceState == null) {
            // First incarnation of this activity.
            mapFragment.setRetainInstance(true);
        } else {
            // Reincarnated activity. The obtained map is the same map instance in the previous
            // activity life cycle. There is no need to reinitialize it.
            mMap = mapFragment.getMap();
        }
        
        setUpMapIfNeeded();
        
        TextView name = (TextView) findViewById(R.id.session_name);
        name.setText(session.name);

        TextView location = (TextView) findViewById(R.id.session_location);
        if (room != null && venue != null) {
            location.setText(venue.name + ", " + room.name);
        }

        TextView timing = (TextView) findViewById(R.id.timing);
        timing.setText("Session starts in " + reminder + " mins");

        Button dismiss = (Button) findViewById(R.id.dismissButton);
        dismiss.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button directions = (Button) findViewById(R.id.directionsButton);
        directions.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?dirflg=w&daddr=" + venue.latitude + "," + venue.longitude));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        Log.i("setUpMapIfNeeded", "called");
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);

                LatLng pos = new LatLng(venue.latitude, venue.longitude);
                MarkerOptions options = new MarkerOptions();
                options.position(pos);
                options.title(venue.name);
                mMap.addMarker(options);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
            }
        }
    }

}
