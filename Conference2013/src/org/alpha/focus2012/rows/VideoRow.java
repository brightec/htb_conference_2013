package org.alpha.focus2012.rows;

import org.alpha.focus2012.Cell;
import org.alpha.conference2013.R;
import org.alpha.focus2012.Row;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class VideoRow extends Row implements Cell {
    private final int resourceId;
    private final String videoUrl;
    private final Context context;
    
    public VideoRow(int resourceId, String videoUrl, Context context) {
        super(context);
        this.resourceId = resourceId;
        this.videoUrl = videoUrl;
        this.context = context;
    }
    
    
    @Override
    public Boolean isEnabled() {
        return false;
    }    

    
    @Override
    public View getView(View convertView) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = mInflater.inflate(R.layout.video_list_item, null);
            
            ImageView imageView = (ImageView)rowView.findViewById(R.id.imageView);
            rowView.setTag(imageView);
        }
        
        ImageView imageView = (ImageView)rowView.getTag();
        imageView.setImageResource(resourceId);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)));
            }
        });
        
        return rowView;        
    }

    
}
