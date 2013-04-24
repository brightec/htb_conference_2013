package org.alpha.conference2013.faqs;

import java.util.ArrayList;
import java.util.List;

import org.alpha.conference2013.AlphaAdapter;
import org.alpha.conference2013.R;
import org.alpha.conference2013.Row;
import org.alpha.conference2013.Row.OnClickListener;
import org.alpha.conference2013.data.DataStore;
import org.alpha.conference2013.data.FAQ;
import org.alpha.conference2013.page.PageActivity;
import org.alpha.conference2013.rows.DetailRow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockListActivity;

public class FaqsActivity extends SherlockListActivity {
    private AlphaAdapter mAdapter;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list);
        
        mAdapter = new AlphaAdapter();
        setListAdapter(mAdapter);
        
        getListView().setOnItemClickListener(mAdapter);        
    }
    
    
    private void populate() {
        List<FAQ> faqs = DataStore.faqs(this);        
        List<Row> rows = new ArrayList<Row>();
        final Context context = this;
        
        for (final FAQ faq : faqs) {
            Row row = new DetailRow(faq.question, null, null, this);
            row.setOnClickListener(new OnClickListener() {                
                @Override
                public void onRowClicked() {
                    Intent intent = new Intent(context, PageActivity.class);
                    intent.putExtra(PageActivity.TITLE, faq.question);
                    intent.putExtra(PageActivity.BODY, faq.answer);
                    context.startActivity(intent);
                }
            });
            rows.add(row);
        }
        
        mAdapter.setRows(rows, this);
    }
    
    
    @Override
    protected void onResume() {
        super.onResume();
        populate();        
    }
}