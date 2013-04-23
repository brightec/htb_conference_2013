package org.alpha.focus2012.home;

import org.alpha.focus2012.Constants;
import org.alpha.focus2012.DownloadableImageView;
import org.alpha.focus2012.R;
import org.alpha.focus2012.data.Conference;
import org.alpha.focus2012.data.DataStore;
import org.alpha.focus2012.resources.Resource;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class HomeVideoFragment extends SherlockFragment {

    private static final String TAG = "HomeVideoFragment";

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.DATA_WAS_UPDATED_INTENT.equals(intent.getAction())) {
                Log.d(TAG, "Populate");
                populate();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.homevideo_fragment, container, false);
        return view;
    }

    @Override
    public void onResume () {
        super.onResume();
        populate();
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(receiver, new IntentFilter(Constants.DATA_WAS_UPDATED_INTENT));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(receiver);
    }

	private void populate() {
    	final Context context = getSherlockActivity();
        final Conference conference = DataStore.conference(context, Constants.CONFERENCE_ID);
        
        if (conference == null) {
            return;
        }
        
        final String videoUrl = conference.youtubeUrl;

        DownloadableImageView imageView = (DownloadableImageView) getSherlockActivity().findViewById(R.id.imageView);
        ImageView video = (ImageView) getSherlockActivity().findViewById(R.id.video);
        TextView descriptionTextView = (TextView) getSherlockActivity().findViewById(R.id.description);
        Button bookButton = (Button) getSherlockActivity().findViewById(R.id.bookButton);

        Resource imageResource = new Resource(conference.imageKey, Resource.Type.ConferenceImage);
        imageView.setUrl(imageResource.url(), imageResource.cacheFilename());

        video.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)));
            }
        });

        descriptionTextView.setText(Html.fromHtml(conference.text));

        bookButton.setVisibility((conference.bookingUrl.length() > 0? View.VISIBLE : View.GONE));
        bookButton.setOnClickListener(new OnClickListener() {            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(conference.bookingUrl));
                startActivity(intent);
            }
        });
    }

}
