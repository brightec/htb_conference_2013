package org.alpha.conference2013.twitter;

import org.json.JSONException;
import org.json.JSONObject;

import org.alpha.conference2013.resources.Resource;
import org.alpha.util.JSON;


public class Tweet {
    
    public final String name;
    public final String text;
    public final String time;
    public final Resource avatar;

    
    Tweet(JSONObject o) throws JSONException {
        JSONObject user = o.getJSONObject("user");
        this.name = JSON.getString(user, "name");
        this.text = JSON.getString(o, "text");
        this.time = JSON.getLocalDateTime(o, "created_at", JSON.DateIntepretation.TWITTER, null).toString("d MMMM - H:mm");
        this.avatar = new Resource(JSON.getString(user, "screen_name"), Resource.Type.TwitterAvatar);
    }
    
}
