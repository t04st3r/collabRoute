package it.digisin.collabroute.users;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.digisin.collabroute.R;
import it.digisin.collabroute.model.User;
import it.digisin.collabroute.travels.TravelContent;

/**
 * Created by raffaele on 10/04/14.
 */
public class CustomArrayAdapterUserList extends ArrayAdapter<UserContent.UserItem> {

    public CustomArrayAdapterUserList(Context context, int textViewResourceId, List<UserContent.UserItem> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getViewOptimize(position, convertView, parent);
    }

    public View getViewOptimize(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.userlistview_row, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView)convertView.findViewById(R.id.userName);
            viewHolder.email = (TextView)convertView.findViewById(R.id.userEmail);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        UserContent.UserItem item = getItem(position);
        viewHolder.name.setText(item.name);
        viewHolder.email.setText(item.email);
        return convertView;
    }

    private class ViewHolder {
        public TextView name;
        public TextView email;
    }
}


