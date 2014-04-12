package it.digisin.collabroute.users;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import it.digisin.collabroute.R;

/**
 * Created by raffaele on 10/04/14.
 */
public class CustomArrayAdapterUserList extends ArrayAdapter<UserContent.UserItem> {
    private final List<UserContent.UserItem> list;

    public CustomArrayAdapterUserList(Context context, int textViewResourceId, List<UserContent.UserItem> objects) {
        super(context, textViewResourceId, objects);
        list = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getViewOptimize(position, convertView, parent);
    }

    public View getViewOptimize(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.userlistview_row, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView)convertView.findViewById(R.id.userName);
            viewHolder.email = (TextView)convertView.findViewById(R.id.userEmail);
            viewHolder.selected = (CheckBox)convertView.findViewById(R.id.userCheckBox);
            viewHolder.selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UserContent.UserItem item = (UserContent.UserItem) viewHolder.selected.getTag();
                    item.selected = buttonView.isChecked();
                }
            });
            convertView.setTag(viewHolder);
            viewHolder.selected.setTag(list.get(position));
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            ((ViewHolder) convertView.getTag()).selected.setTag(list.get(position));
        }
        UserContent.UserItem item = getItem(position);
        viewHolder.name.setText(item.name);
        viewHolder.email.setText(item.email);
        viewHolder.selected.setChecked(list.get(position).selected);
        return convertView;
    }

    private static class ViewHolder {
        public TextView name;
        public TextView email;
        public CheckBox selected;
    }
}


