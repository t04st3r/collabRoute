package it.raffaeletosti.collabroute;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.JSONCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.koushikdutta.async.http.socketio.StringCallback;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import it.raffaeletosti.collabroute.connection.ConnectionHandler;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class ChatFragment extends Fragment {


    protected SocketIOClient socketClient;

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
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        (new ChatThread()).start();


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
        if (socketClient != null) {
            if (socketClient.isConnected()) {
                socketClient.disconnect();
                socketClient = null;
            }
        }
    }

    public class ChatThread extends Thread{
        public void run(){
            if (socketClient == null || !socketClient.isConnected()) {
                String[] socketData = loadChatServerData();

                SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), "http://" + socketData[0] + ":" + socketData[1], new ConnectCallback() {

                    @Override
                    public void onConnectCompleted(Exception ex, SocketIOClient client) {
                        if(ex != null){
                            System.err.println(ex);
                            return;
                        }
                        socketClient = client;
                        client.setJSONCallback(new JSONCallback() {
                            @Override
                            public void onJSON(JSONObject jsonObject, Acknowledge acknowledge) {
                                System.err.println(jsonObject.toString());
                            }
                        });

                        client.setStringCallback(new StringCallback() {

                            @Override
                            public void onString(String s, Acknowledge acknowledge) {
                                System.err.println(s);
                            }
                        });

                        client.on("message", new EventCallback() {

                            @Override
                            public void onEvent(JSONArray jsonArray, Acknowledge acknowledge) {
                                System.err.println("RECIEVED: "+jsonArray.toString());
                            }
                        });

                        try {
                            client.emit(new JSONObject().put("message" , "HelloWorld"));
                        } catch (JSONException jsonEx) {
                            jsonEx.printStackTrace();
                        }
                    }
                });
            }
        }
    }

}
