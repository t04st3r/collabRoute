package it.raffaeletosti.collabroute;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import it.raffaeletosti.collabroute.connection.ConnectionHandler;
import it.raffaeletosti.collabroute.connection.RoutesHandler;
import it.raffaeletosti.collabroute.model.MeetingPoint;
import it.raffaeletosti.collabroute.model.User;
import it.raffaeletosti.collabroute.routes.CustomArrayAdapterRoutes;
import it.raffaeletosti.collabroute.routes.RoutesContent;

import static android.R.layout.simple_dropdown_item_1line;

public class RoutesFragment extends Fragment {

    private static ListView routesListView;
    private static ArrayAdapter routesListAdapter;
    private static Activity thisActivity;
    private Button addRoute;
    private Button visualizeAllRoutes;
    private Button deleteRoute;
    private Button visualizeRouteOnMap;
    private Button getDirections;
    private Dialog addRouteDialog;
    private Dialog menuDialog;
    private EditText address;
    private EditText city;
    private EditText zipCode;
    private EditText region;
    private AutoCompleteTextView country;
    private HashMap<String, String> countryList;
    public static boolean isTabViolet = false;
    public MeetingPoint selected;

    private enum ResponseMSG {OK, ZERO_RESULTS, OVER_QUERY_LIMIT, REQUEST_DENIED, ROUTE_NOT_FOUND, UNKNOWN_ERROR, INVALID_REQUEST, DATABASE_ERROR, AUTH_FAILED, CONN_TIMEDOUT, CONN_REFUSED, CONN_BAD_URL, CONN_GENERIC_IO_ERROR, CONN_GENERIC_ERROR}


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RoutesFragment.
     */
    public static RoutesFragment newInstance() {
        RoutesFragment fragment = new RoutesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public RoutesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_routes, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        thisActivity = getActivity();
        routesListView = (ListView) thisActivity.findViewById(R.id.routesListView);
        routesListAdapter = new CustomArrayAdapterRoutes(thisActivity, R.layout.route_row, RoutesContent.ITEMS);
        routesListView.setAdapter(routesListAdapter);
        routesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RoutesContent.RoutesItem item = (RoutesContent.RoutesItem) routesListAdapter.getItem(position);
                selected = TravelActivity.travel.getRoutes().get(item.id);
                createMenuDialog();
                menuDialog.show();
                //TODO use the item for the actions on the route menu dialog
            }
        });
        addRoute = (Button) thisActivity.findViewById(R.id.add_route_button);
        addRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAddRouteDialog();
                cleanForms();
                addRouteDialog.show();
            }
        });
        visualizeAllRoutes = (Button) thisActivity.findViewById(R.id.visualize_all_routes_button);
        visualizeAllRoutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RoutesContent.isEmpty()) {
                    Toast.makeText(thisActivity, getString(R.string.route_empty_list), Toast.LENGTH_SHORT).show();
                    return;
                }
                TravelActivity.mViewPager.setCurrentItem(0);
                TravelActivity.map.updateCameraRoutes();
            }
        });

        fillListFromModel();
        updateRoutesList();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RoutesContent.cleanList();

    }

    private void cleanForms() {
        address.setText("");
        city.setText("");
        zipCode.setText("");
        region.setText("");
        country.setText("");
    }


    private static void updateRoutesList() {
        thisActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                routesListAdapter.notifyDataSetChanged();
            }
        });
    }


    private void fillListFromModel() {
        HashMap<String, MeetingPoint> routesList = TravelActivity.travel.getRoutes();
        if (!routesList.isEmpty()) {
            RoutesContent.cleanList();
            for (String current : routesList.keySet()) {
                MeetingPoint currentMP = routesList.get(current);
                User creator = currentMP.getIdUser() == TravelActivity.travel.getAdmin().getId() ? TravelActivity.travel.getAdmin()
                        : TravelActivity.travel.getPeople().get(String.valueOf(currentMP.getIdUser()));
                RoutesContent.RoutesItem item = new RoutesContent.RoutesItem(String.valueOf(currentMP.getId()),
                        currentMP.getAddress(), creator.getName(), String.valueOf(currentMP.getLatitude()), String.valueOf(currentMP.getLongitude()));
                RoutesContent.addItem(item);
            }
        }
    }

    public void updateModel(JSONArray routeArray) throws JSONException {
        String result = routeArray.getJSONObject(0).getString("result");
        if (!result.equals("OK")) {
            Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
            return;
        }
        JSONArray routeList = routeArray.getJSONObject(0).getJSONArray("list");
        int length = routeList.length();
        if (length > 0) {
            HashMap<String, MeetingPoint> newMap = new HashMap<String, MeetingPoint>();
            for (int i = 0; i < length; i++) {
                JSONObject item = routeList.getJSONObject(i);
                int id = item.getInt("id");
                Double latitude = item.getDouble("latitude");
                Double longitude = item.getDouble("longitude");
                int userId = item.getInt("idUser");
                String address = item.getString("address");
                MeetingPoint brandNew = new MeetingPoint();
                brandNew.setId(id);
                brandNew.setLatitude(latitude);
                brandNew.setLongitude(longitude);
                brandNew.setIdUser(userId);
                brandNew.setAddress(address);
                newMap.put(String.valueOf(id), brandNew);
            }
            TravelActivity.travel.setRoutes(newMap);
            fillListFromModel();
        } else if (length == 0) {
            TravelActivity.travel.getRoutes().clear();
            RoutesContent.cleanList();
        }
        if (addRouteDialog != null && addRouteDialog.isShowing()) {
            addRouteDialog.dismiss();
        }
        if (TravelActivity.mViewPager != null) {
            if (TravelActivity.mViewPager.getCurrentItem() != 1) {
                changeTabChatState();
            }
        }
        updateRoutesList();
    }

    private void createMenuDialog() {
        if (menuDialog == null) {
            menuDialog = new Dialog(thisActivity);
            menuDialog.setContentView(R.layout.route_menu_dialog);
            menuDialog.setTitle(R.string.menu_route_title);
            deleteRoute = (Button) menuDialog.findViewById(R.id.delete_route_button);
            visualizeRouteOnMap = (Button) menuDialog.findViewById(R.id.visualize_route_on_map_button);
            getDirections = (Button) menuDialog.findViewById(R.id.get_directions_to_route);
            deleteRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(thisActivity);
                    alertDialogBuilder.setMessage(getString(R.string.delete_route) + ":\n\n" + selected.getAddress() + "\n\n" + getString(R.string.delete_travel_dialog_message))
                            .setTitle(getString(R.string.delete_route))
                            .setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    RoutesHandler deleteRequest = new RoutesHandler(thisActivity, TravelActivity.route, String.valueOf(TravelActivity.travel.getId()), String.valueOf(selected.getId()), TravelActivity.user);
                                    deleteRequest.execute("deleteRoute");
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    if (menuDialog != null && menuDialog.isShowing()) {
                                        menuDialog.dismiss();
                                    }
                                }
                            });
                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();
                }
            });
            visualizeRouteOnMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuDialog.dismiss();
                    TravelActivity.map.updateCameraSingleRoute(selected);
                    TravelActivity.mViewPager.setCurrentItem(0);

                }
            });
            getDirections.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    private void createAddRouteDialog() {
        if (addRouteDialog == null) {
            addRouteDialog = new Dialog(thisActivity);
            addRouteDialog.setContentView(R.layout.add_route_dialog);
            addRouteDialog.setTitle(R.string.new_route_title);
            addRouteDialog.setCancelable(false);
            addRouteDialog.setCanceledOnTouchOutside(false);
            final Button currentLocation = (Button) addRouteDialog.findViewById(R.id.getCurrentLocation);
            final Button getLocationFromMap = (Button) addRouteDialog.findViewById(R.id.getFromMap);
            final Button cancel = (Button) addRouteDialog.findViewById(R.id.cancelDialogButton);
            final Button search = (Button) addRouteDialog.findViewById(R.id.newRouteSendData);
            address = (EditText) addRouteDialog.findViewById(R.id.newRouteAddress);
            city = (EditText) addRouteDialog.findViewById(R.id.newRouteCity);
            zipCode = (EditText) addRouteDialog.findViewById(R.id.newRouteZipCode);
            region = (EditText) addRouteDialog.findViewById(R.id.newRouteRegion);
            country = (AutoCompleteTextView) addRouteDialog.findViewById(R.id.newRouteAutoCompleteCountry);
            country.setThreshold(2);
            ArrayAdapter<String> adapter = createCountriesAdapter();
            if (adapter != null)
                country.setAdapter(adapter);

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addRouteDialog.dismiss();
                }
            });

            search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendAddress();
                }
            });

            currentLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int mySelfId = TravelActivity.user.getId();
                    final User mySelf = mySelfId == TravelActivity.travel.getAdmin().getId() ?
                            TravelActivity.travel.getAdmin() : TravelActivity.travel.getPeople().get(String.valueOf(mySelfId));
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(thisActivity);
                    alertBuilder.setTitle(R.string.new_route_title);
                    alertBuilder.setNegativeButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (addRouteDialog != null && addRouteDialog.isShowing()) {
                                addRouteDialog.dismiss();
                            }
                        }
                    });
                    if (mySelf.getLatitude() == 0.0d && mySelf.getLongitude() == 0.0d) {
                        alertBuilder.setMessage(R.string.geocode_dialog_negative);
                    } else {
                        String address = mySelf.getAddress() + "\nLAT: " +
                                String.valueOf(mySelf.getLatitude()) + "\nLNG: " + String.valueOf(mySelf.getLongitude());
                        alertBuilder.setMessage(String.format(getString(R.string.geocode_dialog_positive), address));
                        alertBuilder.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    JSONArray toSend = prepareRequest(mySelf.getLatitude(), mySelf.getLongitude(), mySelf.getAddress());
                                    RoutesHandler sendRoutes = new RoutesHandler(thisActivity, TravelActivity.user,
                                            String.valueOf(TravelActivity.travel.getId()), RoutesFragment.this, toSend);
                                    sendRoutes.execute("addRoute");
                                } catch (JSONException e) {
                                    System.err.println(e);
                                }
                            }
                        });
                    }
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                }
            });
        }
    }

    private void fillMapFromJSONObject(HashMap<String, String> map, JSONArray array) {
        try {
            int length = array.length();
            for (int i = 0; i < length; i++) {
                JSONObject item = array.getJSONObject(i);
                String countryCode = item.getString("code");
                String countryName = item.getString("name");
                map.put(countryCode, countryName);
            }

        } catch (JSONException e) {

        }
    }

    private String getKey(String value) {
        for (String current : countryList.keySet()) {
            if (countryList.get(current).equals(value))
                return current;
        }
        return null;
    }


    private ArrayAdapter<String> createCountriesAdapter() {
        if (countryList == null)
            countryList = new HashMap<String, String>();
        else
            countryList.clear();
        try {
            InputStream countriesFile = thisActivity.getResources().openRawResource(R.raw.countries);
            String jsonStringCountries = ConnectionHandler.inputToString(countriesFile);
            JSONArray jsonArrayCountries = new JSONArray(jsonStringCountries);
            fillMapFromJSONObject(countryList, jsonArrayCountries);
            int length = countryList.size();
            int index = 0;
            String[] countryArrayString = new String[length];
            for (String current : countryList.keySet()) {
                String currentCountry = countryList.get(current);
                countryArrayString[index++] = currentCountry;
            }
            return new ArrayAdapter<String>(thisActivity, simple_dropdown_item_1line, countryArrayString);
        } catch (FileNotFoundException e) {
            System.err.println(e);
        } catch (JSONException e) {
            System.err.println(e);
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }

    private void sendAddress() {
        String cityString = city.getText().toString();
        String countryString = country.getText().toString();
        if (cityString.equals("") || countryString.equals("")) {
            Toast.makeText(thisActivity, getString(R.string.new_route_data_missing), Toast.LENGTH_SHORT).show();
            return;
        }
        String zipString = zipCode.getText().toString();
        if (!zipString.equals("")) {
            try {
                Integer.parseInt(zipString);
            } catch (NumberFormatException e) {
                Toast.makeText(thisActivity, getString(R.string.new_route_zip_not_numeric), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (!countryList.containsValue(countryString)) {
            Toast.makeText(thisActivity, getString(R.string.new_route_unknown_country), Toast.LENGTH_SHORT).show();
            return;
        }
        String countryCode = getKey(countryString);
        String addressString = address.getText().toString();
        String regionString = region.getText().toString();
        final String spaceRemoverRegExp = "\\s++"; //find one or more occurrences of space
        String formattedAddress = "address=";
        if (!addressString.equals(""))
            formattedAddress += addressString.replaceAll(spaceRemoverRegExp, "+") + ",";
        if (!zipString.equals(""))
            formattedAddress += zipString + ",+";
        formattedAddress += cityString.replaceAll(spaceRemoverRegExp, "+") + ",";
        if (!regionString.equals(""))
            formattedAddress += regionString.replaceAll(spaceRemoverRegExp, "+");
        formattedAddress += "&component=country:" + countryCode + "&sensor=true";
        RoutesHandler addressRequest = new RoutesHandler(thisActivity, this);
        addressRequest.execute("geocoding", formattedAddress);

    }

    public void checkGeocodingResponse(JSONObject response) {
        try {
            String resultString = response.getString("status");
            ResponseMSG responseEnum = ResponseMSG.valueOf(resultString);
            switch (responseEnum) {
                case CONN_REFUSED:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_BAD_URL:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_IO_ERROR:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_ERROR:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_TIMEDOUT:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                    return;
                case DATABASE_ERROR:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                default:
                    geocodeHandle(response);
            }
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    private void geocodeHandle(JSONObject response) throws JSONException {
        if (!response.getString("status").equals("OK")) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(thisActivity);
            alertDialogBuilder.setTitle(getString(R.string.geocode_dialog_title));
            alertDialogBuilder.setNegativeButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialogBuilder.setMessage(getString(R.string.geocode_dialog_negative));
            alertDialogBuilder.show();
        } else if (response.getJSONArray("results").length() == 1) {
            final String address = response.getJSONArray("results").getJSONObject(0).getString("formatted_address");
            final double lat = response.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            final double lng = response.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(thisActivity);
            alertDialogBuilder.setTitle(getString(R.string.geocode_dialog_title));
            alertDialogBuilder.setNegativeButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialogBuilder.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        JSONArray toSend = prepareRequest(lat, lng, address);
                        RoutesHandler sendRoutes = new RoutesHandler(thisActivity, TravelActivity.user,
                                String.valueOf(TravelActivity.travel.getId()), RoutesFragment.this, toSend);
                        sendRoutes.execute("addRoute");
                    } catch (JSONException e) {
                        System.err.println(e);
                    }
                }
            });
            alertDialogBuilder.setMessage(String.format(getString(R.string.geocode_dialog_positive), address + "\nLAT: "
                    + String.valueOf(lat) + "\nLNG: " + String.valueOf(lng)));
            alertDialogBuilder.show();
        } else {
            System.err.println(response.toString());
            //TODO build a dialog with a list view showing all the addresses found
        }

    }

    private JSONArray prepareRequest(double lat, double lng, String address) throws JSONException {
        JSONArray array = new JSONArray();
        JSONObject item = new JSONObject()
                .put("latitude", String.valueOf(lat))
                .put("longitude", String.valueOf(lng))
                .put("address", address);
        array.put(item);
        return array;
    }

    public void routeAddedResponse(JSONObject response) {
        try {
            String resultString = response.getString("result");
            ResponseMSG responseEnum = ResponseMSG.valueOf(resultString);
            switch (responseEnum) {
                case CONN_REFUSED:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_BAD_URL:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_IO_ERROR:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_ERROR:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_TIMEDOUT:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                    return;
                case DATABASE_ERROR:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case AUTH_FAILED:
                    TravelActivity.chat.socketClient.disconnect();
                    TravelActivity.closeEverything();
                    final Intent intent = new Intent(thisActivity, LoginActivity.class);
                    startActivityForResult(intent, thisActivity.RESULT_OK);
                    thisActivity.finish();
                case OK: //response do not bring array with new routes added because socket.io will provide to broadcast them to all users connected
                    return;
            }
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    public void routeDeletedResponse(JSONObject response) {
        if (menuDialog.isShowing())
            menuDialog.dismiss();
        try {
            String resultString = response.getString("result");
            ResponseMSG responseEnum = ResponseMSG.valueOf(resultString);
            switch (responseEnum) {
                case ROUTE_NOT_FOUND:
                    Toast.makeText(thisActivity, getString(R.string.delete_route_not_found), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_REFUSED:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_BAD_URL:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_IO_ERROR:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_ERROR:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_TIMEDOUT:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                    return;
                case DATABASE_ERROR:
                    Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case AUTH_FAILED:
                    TravelActivity.chat.socketClient.disconnect();
                    TravelActivity.closeEverything();
                    final Intent intent = new Intent(thisActivity, LoginActivity.class);
                    startActivityForResult(intent, thisActivity.RESULT_OK);
                    thisActivity.finish();
                case OK:
                    return;
            }
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    public void changeTabChatState() {
        thisActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (TravelActivity.routeTab != null) {
                    TextView tabView = (TextView) TravelActivity.routeTab.getCustomView();
                    tabView.setTextColor(Color.parseColor("#ffcc0eff"));
                    TravelActivity.routeTab.setCustomView(tabView);
                    isTabViolet = true;
                }
            }
        });
    }
}
