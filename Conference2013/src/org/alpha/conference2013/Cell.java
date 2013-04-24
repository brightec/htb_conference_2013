package org.alpha.conference2013;

import android.view.View;

public interface Cell {
    
    public View getView(View convertView);    
    public Boolean isEnabled();
}