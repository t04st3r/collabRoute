package it.digisin.collabroute;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import it.digisin.collabroute.travel.TravelContent;


/**
 * An activity representing a list of travels. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link travelDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link travelListFragment} and the item details
 * (if present) is a {@link travelDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link travelListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class travelListActivity extends FragmentActivity
        implements travelListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private  Dialog logoutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_list);
        TravelContent.addItem(new TravelContent.TravelItem("1" , "prova"));
        if (findViewById(R.id.travel_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((travelListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.travel_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link travelListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(travelDetailFragment.ARG_ITEM_ID, id);
            travelDetailFragment fragment = new travelDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.travel_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, travelDetailActivity.class);
            detailIntent.putExtra(travelDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.travel_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            createLogOutDialog();
            logoutDialog.show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void createLogOutDialog(){
        if(logoutDialog == null){
            logoutDialog = new Dialog(this);
            logoutDialog.setContentView(R.layout.logout_dialog);
            logoutDialog.setTitle(R.string.logout_title);
            final Button ok = (Button) logoutDialog.findViewById(R.id.Okbutton);
            final Button cancel = (Button) logoutDialog.findViewById(R.id.Cancelbutton);

            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logOut();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logoutDialog.dismiss();
                }
            });
        }
    }

    public void logOut(){
        final Intent intent = new Intent(this, LoginActivity.class);
        logoutDialog.dismiss();
        startActivityForResult(intent, RESULT_OK);
        finish();
    }
}
