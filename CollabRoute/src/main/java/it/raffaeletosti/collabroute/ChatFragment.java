package it.raffaeletosti.collabroute;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.ReconnectCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;


import it.raffaeletosti.collabroute.chat.ChatContent;
import it.raffaeletosti.collabroute.chat.CustomArrayAdapterChatList;
import it.raffaeletosti.collabroute.connection.ConnectionHandler;
import it.raffaeletosti.collabroute.model.Travel;
import it.raffaeletosti.collabroute.model.User;
import it.raffaeletosti.collabroute.model.UserHandler;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class ChatFragment extends Fragment {


    public static SocketIOClient socketClient;
    private Button send;
    private ListView chatList;
    private ArrayAdapter chatAdapter;
    private EditText text;
    private Travel travel;
    private UserHandler user;
    protected Activity thisActivity;
    protected ChatThread chatThread;
    public static boolean isTabViolet = false;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        text = (EditText) thisActivity.findViewById(R.id.chatEditText);
        send = (Button) thisActivity.findViewById(R.id.chatButton);
        if (chatList == null) {
            chatList = (ListView) thisActivity.findViewById(R.id.chatListView);
            chatAdapter = new CustomArrayAdapterChatList(thisActivity, R.layout.chat_row, ChatContent.ITEMS);
            chatList.setAdapter(chatAdapter);
        }
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendText();
                } catch (JSONException e) {
                    System.err.println(e);
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        thisActivity = getActivity();
        travel = TravelActivity.travel;
        user = TravelActivity.user;
        if (chatThread == null) {
            chatThread = new ChatThread();
            chatThread.start();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    private String[] loadChatServerData() {
        try {
            InputStream input = getActivity().getResources().openRawResource(R.raw.config);
            String jsonString = ConnectionHandler.inputToString(input);
            JSONObject object = new JSONObject(jsonString);
            String serverUrl = object.getString("SERVER_ADDRESS");
            String serverChatPort = String.valueOf(object.getInt("CHAT_PORT"));
            return new String[]{serverUrl, serverChatPort};
        } catch (FileNotFoundException e) {
            System.err.println("Missing JSON config file: " + e);
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.err.println("SOCKET IO SERVICE STOPPED");
        chatAdapter = null;
        chatList = null;
        ChatContent.cleanList();
        if (socketClient != null) {
            if (socketClient.isConnected()) {
                socketClient.disconnect();
                socketClient = null;
            }
        }
        chatThread = null;
    }

    private void sendText() throws JSONException {
        String textToSend = text.getText().toString();
        if (textToSend.equals("")) {
            return;
        }
        text.setText("");
        JSONArray messageArray = new JSONArray().put(new JSONObject()
                .put("text", textToSend)
                .put("userId", TravelActivity.user.getId())
                .put("travelId", TravelActivity.travel.getId()));
        socketClient.emit("text", messageArray);
    }

    private String getUserName(String id) {
        if (travel != null) {
            if (travel.getAdmin().getId() == Integer.parseInt(id))
                return travel.getAdmin().getName();
            if (user.getId() == Integer.parseInt(id))
                return user.getName();
            HashMap<String, User> people = travel.getPeople();
            for (String current : people.keySet()) {
                if (people.get(current).getId() == Integer.parseInt(id)) {
                    return people.get(current).getName();
                }
            }
        }
        return null;
    }

    public class ChatThread extends Thread {

        @Override
        public void run() {

            final String[] socketData = loadChatServerData();
            SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), "http://" + socketData[0] + ":" + socketData[1], new ConnectCallback() {


                @Override
                public void onConnectCompleted(Exception ex, SocketIOClient client) {
                    if (ex != null) {
                        System.err.println(ex);
                        return;
                    }
                    socketClient = client;
                    socketClient.setReconnectCallback(new ReconnectCallback() {
                        @Override
                        public void onReconnect() {
                            try {
                                JSONArray array = new JSONArray();
                                array.put(new JSONObject().put("userId", TravelActivity.user.getId())
                                        .put("travelId", TravelActivity.travel.getId()));
                                socketClient.emit("adduser", array);
                            } catch (JSONException jsonEx) {
                                System.err.println(jsonEx);
                            }
                        }
                    });

                    socketClient.on("disconnect", new EventCallback() {
                        @Override
                        public void onEvent(JSONArray jsonArray, Acknowledge acknowledge) {
                            socketClient.disconnect();
                            TravelActivity.closeEverything();
                            final Intent intent = new Intent(thisActivity, LoginActivity.class);
                            startActivityForResult(intent, thisActivity.RESULT_OK);
                            thisActivity.finish();
                        }
                    });
                    socketClient.on("text", new EventCallback() {

                        @Override
                        public void onEvent(JSONArray jsonArray, Acknowledge acknowledge) {
                            try {
                                String userId = jsonArray.getJSONObject(0).getString("id");
                                String text = jsonArray.getJSONObject(0).getString("text");
                                String userName = getUserName(userId);
                                ChatContent.addItem(new ChatContent.ChatItem(userName, text));
                                //if I'm not on the chat wiew
                                if (TravelActivity.mViewPager != null) {
                                    if (TravelActivity.mViewPager.getCurrentItem() != 3) {
                                        changeTabChatState();
                                    }
                                }
                                updateChatList();
                            } catch (JSONException e) {
                                System.err.println(e);
                            }
                        }
                    });
                    socketClient.on("clientList", new EventCallback() {
                        @Override
                        public void onEvent(JSONArray jsonArray, Acknowledge acknowledge) {
                            UsersFragment.fillUsersStatus(jsonArray);
                            if (TravelActivity.map.MarkerHandlerThread != null)
                                TravelActivity.map.MarkerHandlerThread.post(TravelActivity.map.run);
                        }
                    });
                    try {
                        JSONArray array = new JSONArray();
                        array.put(new JSONObject().put("userId", TravelActivity.user.getId())
                                .put("travelId", TravelActivity.travel.getId()));
                        socketClient.emit("adduser", array);
                    } catch (JSONException jsonEx) {
                        System.err.println(jsonEx);
                    }

                }
            });

        }
    }

    public static void updateStatus() {
        if (socketClient != null) {
            try {
                JSONArray array = new JSONArray();
                array.put(new JSONObject().put("userId", TravelActivity.user.getId())
                        .put("travelId", TravelActivity.travel.getId()));
                socketClient.emit("update_request", array);
            } catch (JSONException e) {
                System.err.println(e);
            }
        }
    }

    private void updateChatList() {
        thisActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatAdapter.notifyDataSetChanged();
            }
        });
    }

    public void changeTabChatState() {
        thisActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (TravelActivity.chatTab != null) {
                    TextView tabView = (TextView) TravelActivity.chatTab.getCustomView();
                    tabView.setTextColor(Color.parseColor("#ffcc0eff"));
                    TravelActivity.chatTab.setCustomView(tabView);
                    isTabViolet = true;
                }
            }
        });

    }


}