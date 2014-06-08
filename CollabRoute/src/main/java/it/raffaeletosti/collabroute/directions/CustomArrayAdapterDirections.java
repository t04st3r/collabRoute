package it.raffaeletosti.collabroute.directions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.List;

import it.raffaeletosti.collabroute.R;
import it.raffaeletosti.collabroute.routes.RoutesContent;


/**
 * Created by raffaele on 04/06/14.
 */
public class CustomArrayAdapterDirections extends ArrayAdapter<DirectionsContent.DirectionsItem>{

    public static int counter = 1;

    public CustomArrayAdapterDirections(Context context, int textViewResourceId, List<DirectionsContent.DirectionsItem> objects) {
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
            convertView = inflater.inflate(R.layout.directions_row, null);
            viewHolder = new ViewHolder();
            viewHolder.wayPointId = (TextView) convertView.findViewById(R.id.waypoint_id);
            viewHolder.instructions = (WebView) convertView.findViewById(R.id.instructionsWebView);
            //webViews make conflict with OnItemClickListeners method on the ArrayAdapter without these two following commands
            viewHolder.instructions.setClickable(false);
            viewHolder.instructions.setFocusable(false);
            viewHolder.durations = (TextView) convertView.findViewById(R.id.directionDuration);
            viewHolder.travelMode = (TextView) convertView.findViewById(R.id.directionTravelMode);
            viewHolder.distance = (TextView) convertView.findViewById(R.id.directionDistance);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        DirectionsContent.DirectionsItem item = getItem(position);
        viewHolder.wayPointId.setText("Waypoint "+item.id);
        String html = "<html><body style='background-color:black;color:white;'>"+item.HTMLInstructions+"</body></html>";
        viewHolder.instructions.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
        viewHolder.durations.setText("Approximate duration: "+item.duration);
        viewHolder.travelMode.setText("Travel mode: "+item.travelMode);
        viewHolder.distance.setText("Distance: "+item.distance);
        return convertView;
    }

    private class ViewHolder {
        public TextView wayPointId;
        public WebView instructions;
        public TextView durations;
        public TextView travelMode;
        public TextView distance;
    }
}
