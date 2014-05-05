package it.raffaeletosti.collabroute.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.raffaeletosti.collabroute.R;
import it.raffaeletosti.collabroute.users.UserContent;

/**
 * Created by raffaele on 02/04/14.
 */
public class CustomArrayAdapterChatList extends ArrayAdapter<ChatContent.ChatItem> {

    public CustomArrayAdapterChatList(Context context, int textViewResourceId, List<ChatContent.ChatItem> objects) {
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
            convertView = inflater.inflate(R.layout.chat_row, null);
            viewHolder = new ViewHolder();
            viewHolder.userName = (TextView)convertView.findViewById(R.id.chatUserName);
            viewHolder.text = (TextView)convertView.findViewById(R.id.chatText);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ChatContent.ChatItem item = getItem(position);
        viewHolder.userName.setText(item.userName);
        viewHolder.text.setText(item.text);
        return convertView;
    }

    private class ViewHolder {
        public TextView userName;
        public TextView text;
    }
}

