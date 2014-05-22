package it.raffaeletosti.collabroute.routes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

import it.raffaeletosti.collabroute.R;

/**
 * Created by raffaele on 22/05/14.
 */
public class CustomArrayAdapterRoutes extends ArrayAdapter<RoutesContent.RoutesItem> {

    private final List<RoutesContent.RoutesItem> list;
    private final CustomArrayAdapterRoutes arrayAdapterRoutesList = this;

    public CustomArrayAdapterRoutes(Context context, int textViewResourceId, List<RoutesContent.RoutesItem> objects) {
        super(context, textViewResourceId, objects);
        list = objects;
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
            viewHolder.isSelected = (RadioButton) convertView.findViewById(R.id.routesRadioButton);
            viewHolder.isSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        RoutesContent.RoutesItem item = (RoutesContent.RoutesItem) viewHolder.isSelected.getTag();
                        RoutesContent.selectCurrent(item.id);
                        notifyList();
                    }
                }
            });
            convertView.setTag(viewHolder);
            viewHolder.isSelected.setTag(list.get(position));
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        RoutesContent.RoutesItem item = getItem(position);
        viewHolder.isSelected.setChecked(item.isSelected);
        viewHolder.address.setText(item.address);
        viewHolder.creator.setText("Created by: "+item.creator);
        viewHolder.latLng.setText("(LAT: "+item.latitude+" LNG: "+item.longitude+")");
        return convertView;
    }

    public void notifyList(){
        arrayAdapterRoutesList.notifyDataSetChanged();
    }

    private class ViewHolder {
        public TextView address;
        public RadioButton isSelected;
        public TextView creator;
        public TextView latLng;
    }
}

