package org.alpha.focus2012.diary;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.alpha.focus2012.R;
import org.alpha.focus2012.data.DataStore;
import org.alpha.focus2012.data.Session;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class RateSessionActivity extends SherlockFragmentActivity {

    public static final String SESSION_ID = "SESSION_ID";
    private ActionBar mActionBar;
    private Session session;

    SeekBar venueBar, speakerBar, contentBar;
    TextView venueText, speakerText, contentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ratesession_activity);

        int sessionId = getIntent().getIntExtra(SESSION_ID, 0);
        session = DataStore.session(this, sessionId);

        mActionBar = getSupportActionBar();
        mActionBar.setTitle(session.name);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        venueBar = (SeekBar) findViewById(R.id.venueSeekBar);
        venueText = (TextView) findViewById(R.id.venueText);
        venueBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                if (progress <= 20) {
                    venueText.setText("Very Poor");
                } else if (progress <= 40) {
                    venueText.setText("Poor");
                } else if (progress <= 60) {
                    venueText.setText("OK");
                } else if (progress <= 80) {
                    venueText.setText("Good");
                } else {
                    venueText.setText("Very Good");
                }
            }
        });

        speakerBar = (SeekBar) findViewById(R.id.speakerSeekBar);
        speakerText = (TextView) findViewById(R.id.speakerText);
        speakerBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                if (progress <= 20) {
                    speakerText.setText("Very Poor");
                } else if (progress <= 40) {
                    speakerText.setText("Poor");
                } else if (progress <= 60) {
                    speakerText.setText("OK");
                } else if (progress <= 80) {
                    speakerText.setText("Good");
                } else {
                    speakerText.setText("Very Good");
                }
            }
        });

        contentBar = (SeekBar) findViewById(R.id.contentSeekBar);
        contentText = (TextView) findViewById(R.id.contentText);
        contentBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                if (progress <= 20) {
                    contentText.setText("Very Poor");
                } else if (progress <= 40) {
                    contentText.setText("Poor");
                } else if (progress <= 60) {
                    contentText.setText("OK");
                } else if (progress <= 80) {
                    contentText.setText("Good");
                } else {
                    contentText.setText("Very Good");
                }
            }
        });

        Button send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final int[] ratings = {
                        (int) ((venueBar.getProgress() - 1) / 20f),
                        (int) ((speakerBar.getProgress() - 1) / 20f),
                        (int) ((contentBar.getProgress() - 1) / 20f) };
                new SubmitRatings(v.getContext(), ratings).execute();
            }
        });
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

    private class SubmitRatings extends AsyncTask<Void, Void, JSONObject> {

        private static final String TAG = "SubmitRatings";
        private Context context;
        private int[] ratings;

        public SubmitRatings(Context c, int[] r) {
            context = c;
            ratings = r;
        }

        @Override
        protected void onPreExecute() {
            // TODO check for internet
        }

        @Override
        protected JSONObject doInBackground(Void... v) {
            Thread.currentThread().setName("SubmitRatings (AsyncTask)");
            URL apiURL;
            HttpURLConnection apiConnection = null;
            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("session", session.sessionId);
                jsonBody.put("venue", ratings[0]);
                jsonBody.put("speakers", ratings[1]);
                jsonBody.put("content", ratings[2]);
                byte[] byteBody = jsonBody.toString().getBytes();

                // Connect to the API
                apiURL = new URL(
                        "http://acs.alpha.org/api/rest/v1/conferences/setSessionRating");
                apiConnection = (HttpURLConnection) apiURL.openConnection();
                apiConnection.setDoOutput(true);
                apiConnection.setFixedLengthStreamingMode(byteBody.length);

                // Send the JSON body
                OutputStream out = apiConnection.getOutputStream();
                out.write(byteBody);
                out.close();

                // Read the response
                BufferedInputStream in = new BufferedInputStream(
                        apiConnection.getInputStream());
                return readStream(in);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (apiConnection != null) {
                    apiConnection.disconnect();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (result == null || result.has("error")) {
                Toast.makeText(
                        context,
                        "Sorry we were unable to submit your feedback. Please try again later.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context,
                        "Thank you, your feedback has been saved.",
                        Toast.LENGTH_LONG).show();
                ((SherlockFragmentActivity) context).finish();
            }
        }

        private JSONObject readStream(BufferedInputStream bufferedStream)
                throws IOException, JSONException {
            BufferedReader bufferedReader = null;
            try {
                InputStreamReader reader = new InputStreamReader(bufferedStream);
                bufferedReader = new BufferedReader(reader);
                StringBuilder builder = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null) {
                    builder.append(line);
                    line = bufferedReader.readLine();
                }

                return new JSONObject(builder.toString());
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        }
    }

}
