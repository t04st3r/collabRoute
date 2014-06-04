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

    private static int counter = 0;

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
            viewHolder.durations = (TextView) convertView.findViewById(R.id.durationTextView);
            viewHolder.travelMode = (TextView) convertView.findViewById(R.id.directionTravelMode);
            viewHolder.distance = (TextView) convertView.findViewById(R.id.distanceTextView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        DirectionsContent.DirectionsItem item = getItem(position);
        viewHolder.wayPointId.setText(viewHolder.wayPointId.getText()+item.id);
        String html = encodeHTML(item.HTMLInstructions);
        if(html != null) {
            viewHolder.instructions.loadData(html, "text/html", null);
        }
        viewHolder.durations.setText(viewHolder.durations.getText()+item.duration);
        viewHolder.travelMode.setText(viewHolder.travelMode.getText()+item.travelMode);
        viewHolder.distance.setText(viewHolder.distance.getText()+item.distance);
        return convertView;
    }

    String encodeHTML(String toEncode){
        try{
            byte[] utf8 = toEncode.getBytes("UTF-8");
            String bodyEncoded = new String(utf8, "UTF-8");
            String toReturn = "<html><head></head><body>"+bodyEncoded+"</body></html>";
            System.err.println(toReturn);
            return toReturn;
        }catch(UnsupportedEncodingException e){
            System.err.println(e);
        }
    return null;
    }

    private class ViewHolder {
        public TextView wayPointId;
        public WebView instructions;
        public TextView durations;
        public TextView travelMode;
        public TextView distance;
    }
}
