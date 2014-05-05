package it.raffaeletosti.collabroute.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.raffaeletosti.collabroute.R;

/**
 * Created by raffaele on 04/05/14.
 */
public class CustomArrayAdapterUsersList extends ArrayAdapter<UsersListContent.UsersListItem> {

    public CustomArrayAdapterUsersList(Context context, int textViewResourceId, List<UsersListContent.UsersListItem> objects) {
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
            convertView = inflater.inflate(R.layout.chat_user_row, null);
            viewHolder = new ViewHolder();
            viewHolder.userName = (TextView) convertView.findViewById(R.id.usersListChatName);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        UsersListContent.UsersListItem item = getItem(position);
        viewHolder.userName.setText(item.userName);
        return convertView;
    }

    private class ViewHolder {
        public TextView userName;
    }
}