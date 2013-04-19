package org.alpha.focus2012.diary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.text.StrBuilder;

import org.alpha.focus2012.AlphaAdapter;
import org.alpha.focus2012.R;
import org.alpha.focus2012.Row;
import org.alpha.focus2012.Section;
import org.alpha.focus2012.data.DataStore;
import org.alpha.focus2012.data.Room;
import org.alpha.focus2012.data.Session;
import org.alpha.focus2012.data.Speaker;
import org.alpha.focus2012.data.Venue;
import org.alpha.focus2012.map.VenueDetailActivity;
import org.alpha.focus2012.rows.ButtonBarRow;
import org.alpha.focus2012.rows.DetailRow;
import org.alpha.focus2012.rows.HTMLRow;
import org.alpha.focus2012.rows.SpeakerRow;
import org.alpha.focus2012.speakers.SpeakerDetailActivity;
import org.joda.time.LocalDateTime;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class SessionDetailActivity extends SherlockFragmentActivity {
    
    public static final String EXTRA_SESSION_ID = "EXTRA_SESSION_ID";
    
    private ActionBar mActionBar;
    private Session session;
    private GoogleMap mMap;
    private ListView list;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sessiondetail_activity);
        
        int sessionId = getIntent().getIntExtra(EXTRA_SESSION_ID, 0);
        session = DataStore.session(this, sessionId);
        
        mActionBar = getSupportActionBar(); 
        mActionBar.setTitle(session.name);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        
        AlphaAdapter adapter = new AlphaAdapter();
        list = (ListView) findViewById(R.id.listView1);
        list.setAdapter(adapter);
        list.setOnItemClickListener(adapter);
        
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (savedInstanceState == null) {
            // First incarnation of this activity.
            mapFragment.setRetainInstance(true);
        } else {
            // Reincarnated activity. The obtained map is the same map instance in the previous
            // activity life cycle. There is no need to reinitialize it.
            mMap = mapFragment.getMap();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        populate();
    }
    
    private void populate() {
        List<Row> detailRows = new ArrayList<Row>();
        List<Row> speakerRows = new ArrayList<Row>();
        
        Room room = DataStore.room(this, session.roomId);
        final Venue venue;
        if (room != null) {
            venue = DataStore.venue(this, room.venueId);
        }
        else {
            venue = null;
        }
        
        if (mMap == null) {
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
                mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
            }
        }
        
        // details
        StrBuilder subtitle = new StrBuilder();
        if (venue != null) {
            subtitle.append(venue.name + ", " + room.name);
        }
        subtitle.appendSeparator("\n");
        subtitle.append(session.startDateTime.toString("HH:mm") + " - "+session.endDateTime.toString("HH:mm"));
        subtitle.append(", ").append(session.startDateTime.toString("d MMMM yyyy"));
        detailRows.add(new DetailRow(session.name, subtitle.toString(), null, this));

        // buttons
        OnClickListener venueButtonHandler = null;
        if (venue != null) {
            venueButtonHandler = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), VenueDetailActivity.class);
                    intent.putExtra(VenueDetailActivity.VENUE_ID, venue.venueId);
                    startActivity(intent);    
                }
            };
        }
        
        boolean bookmarked = DiaryChoices.isSessionBookmarked(this, session);

        String errorMsg;
        if (bookmarked) {
        	errorMsg = "TRUE";
        } else {
        	errorMsg = "FALSE";
        }
        
        String bookmarkButtonTitle = bookmarked ? getString(R.string.session_bookmarked_button_title) : getString(R.string.session_bookmark_button_title);
        
		Log.d("SessionDetailActivity", errorMsg);
		
		OnClickListener bookmarkButtonHandler = null;
        
		if (!bookmarked) {
            bookmarkButtonHandler = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    DiaryChoices.bookmarkSession(SessionDetailActivity.this, session);
                    
                    AlertDialog.Builder builder = new AlertDialog.Builder(SessionDetailActivity.this);
                    builder.setTitle("Would you like a reminder?")
                           .setItems(R.array.reminder_items, new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {
                                   int reminder;
                                   switch (which) {
                                       case 1:
                                           reminder = 5;
                                           break;
                                       case 2:
                                           reminder = 10;
                                           break;
                                       case 3:
                                           reminder = 15;
                                           break;
                                       case 4:
                                           reminder = 20;
                                           break;
                                       case 5:
                                           reminder = 30;
                                           break;
                                       default:
                                           reminder = -1;
                                           break;
                                   }

                                   if (reminder > 0) {
                                       LocalDateTime sessionStart = session.startDateTime;
                                       LocalDateTime reminderTime = sessionStart.minusMinutes(reminder);

                                       Intent intent = new Intent(SessionDetailActivity.this, ReminderReceiver.class);
                                       intent.putExtra("title", session.name);
                                       intent.putExtra("note", "Starts in " + reminder + " mins");
                                       intent.putExtra("sessionId", session.sessionId);
                                       intent.putExtra("reminder", reminder);
                                       PendingIntent sender = PendingIntent.getBroadcast(SessionDetailActivity.this, session.sessionId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                       AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                                       //am.set(AlarmManager.RTC_WAKEUP, reminderTime.toDateTime().getMillis(), sender);
                                       am.set(AlarmManager.RTC_WAKEUP, new Date().getTime()+5000, sender);
                                   }

                                   setResult(RESULT_OK);
                                   finish();
                               }
                           });
                    builder.create().show();
                }
            };
        } else {
        	bookmarkButtonHandler = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    DiaryChoices.unbookmarkSession(SessionDetailActivity.this, session);
                    
                    Intent intent = new Intent(SessionDetailActivity.this, ReminderReceiver.class);
                    intent.putExtra("sessionId", session.sessionId);
                    PendingIntent sender = PendingIntent.getBroadcast(SessionDetailActivity.this, session.sessionId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    
                    AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                    am.cancel(sender);
                    
                    setResult(RESULT_OK);
                    finish();
                }
            };
        }
        
		//Set Alarm
		OnClickListener alarmButtonHandler = null;
        if (venue != null) {
            alarmButtonHandler = new OnClickListener() {
                @Override
                public void onClick(View v) {
                	DiaryChoices.setAlarmSession(SessionDetailActivity.this, session);
                    setResult(RESULT_OK);
                    finish();   
                }
            };
        }
		
        OnClickListener rateButtonHandler = null;
        rateButtonHandler = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), RateSessionActivity.class);
                intent.putExtra(RateSessionActivity.SESSION_ID, session.sessionId);
                startActivity(intent);    
            }
        };
		
        OnClickListener notesButtonHandler = null;
        notesButtonHandler = new OnClickListener() {
            @Override
            public void onClick(View v) {
                String notesKey = session.notesKey;
                if (notesKey == null) {
                    Toast.makeText(SessionDetailActivity.this, "Sorry, no notes are available for this session", Toast.LENGTH_LONG).show();
                    return;
                }

                File file = new File(getExternalFilesDir(null), notesKey + ".pdf");

                // If the file doesn't exist, download it
                if (!file.exists()) {
                    new DownloadNotes(SessionDetailActivity.this, notesKey).execute(file);
                } else {
                    showNotes(file);
                }
            }
        };
        
        ButtonBarRow buttons = new ButtonBarRow(this);
        buttons.setButton1("Notes", notesButtonHandler);
        buttons.setButton2("Rate Session", rateButtonHandler);
        buttons.setButton3(bookmarkButtonTitle, bookmarkButtonHandler);
        //detailRows.add(buttons);
        LinearLayout buttonRow = (LinearLayout) findViewById(R.id.buttonRow);
        buttonRow.removeAllViews();
        buttonRow.addView(buttons.getView(null));
        
        //buttons.setButton1("Add Alarm", alarmButtonHandler);
        //detailRows.add(buttons);
        
        // description
        detailRows.add(new HTMLRow(session.text, this));

        // speakers
        
        for (int speakerId : session.speakerIds) {
            final Speaker speaker = DataStore.speaker(this, speakerId);
            if (speaker != null) {
                SpeakerRow row = new SpeakerRow(speaker, this);
                row.setOnClickListener(new Row.OnClickListener() {
                    @Override
                    public void onRowClicked() {
                        Intent intent = new Intent(SessionDetailActivity.this, SpeakerDetailActivity.class);
                        intent.putExtra(SpeakerDetailActivity.EXTRA_SPEAKER_ID, speaker.speakerId);
                        startActivity(intent);
                    }
                });
                speakerRows.add(row);
            }
        }
       
        List<Section> sections = new ArrayList<Section>();
        sections.add(new Section(null, detailRows, this));
        
        if (!speakerRows.isEmpty()) {
          Section speakersSection = new Section(getString(R.string.session_speakers_title), speakerRows, this);
          speakersSection.mSectionBackgroundColourResource = R.color.fixed_section_header;
          sections.add(speakersSection);
        }
        ((AlphaAdapter) list.getAdapter()).setSections(sections);
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void showNotes(File file) {
        Uri path = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(path, "application/pdf");

        try {
            startActivity(intent);
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(SessionDetailActivity.this, "No application available to view PDF", Toast.LENGTH_SHORT).show();
        }
    }
    
    private class DownloadNotes extends AsyncTask<File, Void, File> {

        private static final String TAG = "DownloadNotes";
        private ProgressDialog spinner;
        private Context context;
        private String notesKey;

        public DownloadNotes(Context c, String notesKey) {
            this.context = c;
            this.notesKey = notesKey;
            
            spinner = new ProgressDialog(SessionDetailActivity.this, ProgressDialog.STYLE_SPINNER);
            spinner.setMessage("Downloading PDF");
            spinner.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPreExecute() {
            spinner.show();
        }

        @Override
        protected File doInBackground(File... files) {
            File file = files[0];
            try {
                InputStream is = (InputStream) new URL("http://static.alpha.org/acs/conferences/notes/" + notesKey + ".pdf").getContent();
                FileOutputStream fos = new FileOutputStream(file);
                try {
                    byte[] b = new byte[100];
                    int l = 0;
                    while ((l = is.read(b)) != -1) {
                        fos.write(b, 0, l);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    is.close();
                    fos.close();
                }
                
                return file;
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            return null;
        }
        
        @Override
        protected void onPostExecute(File result) {
            spinner.dismiss();
            if (result != null) {
                showNotes(result);
            } else {
                Toast.makeText(
                        context,
                        "Unable to download the notes. Please try again later.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
