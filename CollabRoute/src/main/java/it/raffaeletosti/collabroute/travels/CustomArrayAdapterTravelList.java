package it.raffaeletosti.collabroute.travels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.raffaeletosti.collabroute.R;

/**
 * Created by raffaele on 02/04/14.
 */
public class CustomArrayAdapterTravelList extends ArrayAdapter<TravelContent.TravelItem> {

    public CustomArrayAdapterTravelList(Context context, int textViewResourceId, List<TravelContent.TravelItem> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getViewOptimize(position, convertView, parent);
    }

    public View getViewOptimize(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_row, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView)convertView.findViewById(R.id.travelName);
            viewHolder.description = (TextView)convertView.findViewById(R.id.travelDescription);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        TravelContent.TravelItem item = getItem(position);
        viewHolder.name.setText(item.name);
        viewHolder.description.setText(item.description);
        return convertView;
    }

    private class ViewHolder {
        public TextView name;
        public TextView description;
    }
}

