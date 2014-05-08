package it.raffaeletosti.collabroute.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

import it.raffaeletosti.collabroute.R;

/**
 * Created by raffaele on 04/05/14.
 */
public class CustomArrayAdapterUsersList extends ArrayAdapter<UsersListContent.UsersListItem> {
    private final List <UsersListContent.UsersListItem> list;
    private final CustomArrayAdapterUsersList arrayAdapterUsersList = this;
    public CustomArrayAdapterUsersList(Context context, int textViewResourceId, List<UsersListContent.UsersListItem> objects) {
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
            convertView = inflater.inflate(R.layout.chat_user_row, null);
            viewHolder = new ViewHolder();
            viewHolder.userName = (TextView) convertView.findViewById(R.id.usersListChatName);
            viewHolder.isOnline = (ImageView) convertView.findViewById(R.id.userChatStatus);
            viewHolder.location = (TextView) convertView.findViewById(R.id.usersListChatCoordinates);
            viewHolder.isAdministrator = (TextView) convertView.findViewById(R.id.usersListChatUserType);
            viewHolder.isSelected = (RadioButton) convertView.findViewById(R.id.userRadioButton);
            viewHolder.isSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UsersListContent.UsersListItem item = (UsersListContent.UsersListItem) viewHolder.isSelected.getTag();
                    UsersListContent.deselectOtherChoices(item.id);
                    item.isSelected = true;
                    arrayAdapterUsersList.notifyDataSetChanged();
                }
            });
            convertView.setTag(viewHolder);
            viewHolder.isSelected.setTag(list.get(position));
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        UsersListContent.UsersListItem item = getItem(position);
        viewHolder.isSelected.setChecked(item.isSelected);
        viewHolder.userName.setText(item.userName);
        return convertView;
    }

    private class ViewHolder {
        public TextView userName;
        public RadioButton isSelected;
        public ImageView isOnline;
        public TextView location;
        public TextView isAdministrator;
    }
}