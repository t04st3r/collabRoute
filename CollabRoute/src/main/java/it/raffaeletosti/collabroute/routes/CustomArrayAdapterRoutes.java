package it.raffaeletosti.collabroute.routes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.raffaeletosti.collabroute.R;

/**
 * Created by raffaele on 22/05/14.
 */
public class CustomArrayAdapterRoutes extends ArrayAdapter<RoutesContent.RoutesItem> {


    public CustomArrayAdapterRoutes(Context context, int textViewResourceId, List<RoutesContent.RoutesItem> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getViewOptimize(position, convertView, parent);
    }

    public View getViewOptimize(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.route_row, null);
            viewHolder = new ViewHolder();
            viewHolder.address = (TextView) convertView.findViewById(R.id.routesListAddress);
            viewHolder.creator = (TextView) convertView.findViewById(R.id.routesListCreator);
            viewHolder.latLng = (TextView) convertView.findViewById(R.id.routesListCoordinates);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        RoutesContent.RoutesItem item = getItem(position);
        viewHolder.address.setText(item.address);
        viewHolder.creator.setText("Created by: " + item.creator);
        item.latitude = item.latitude.length() > 9 ? item.latitude.substring(0, 9) : item.latitude;
        item.longitude = item.longitude.length() > 9 ? item.longitude.substring(0, 9) : item.longitude;
        viewHolder.latLng.setText("(LAT: " + item.latitude + " LNG: " + item.longitude + ")");
        return convertView;
    }

    private class ViewHolder {
        public TextView address;
        public TextView creator;
        public TextView latLng;
    }
}

