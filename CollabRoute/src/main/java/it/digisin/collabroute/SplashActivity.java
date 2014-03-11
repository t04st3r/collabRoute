package it.digisin.collabroute;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import java.lang.ref.WeakReference;
import android.util.Log;


/**
 * Created by raffaele on 10/03/14.
 */
public class SplashActivity extends Activity {

    private static final String TAG_LOG = SplashActivity.class.getName();

    private static final long MIN_WAIT_INTERVAL = 1500L;

    private static final long MAX_WAIT_INTERVAL = 3000L;

    private static final int GO_AHEAD_WHAT = 1;

    private long mStartTime;

    private boolean mIsDone;

    /* Defining an anonymous class can cause Memory Leak because of internal reference to the outer class
    private Handler mHandler = new Handler() {
     */

    private static class UiHandler extends Handler {

        private WeakReference<SplashActivity> mActivityRef;

        public UiHandler(final SplashActivity srcActivity){
            this.mActivityRef = new WeakReference<SplashActivity>(srcActivity);
        }
            @Override
        public void handleMessage(Message msg){
                final SplashActivity srcActivity = this.mActivityRef.get();
                if(srcActivity == null) {
                    Log.d(TAG_LOG, "Reference to SplashActivity lost!");
                    return;
                }
                switch(msg.what) {
                    case GO_AHEAD_WHAT:
                        long elapsedTime = SystemClock.uptimeMillis();
                        if(elapsedTime >= MIN_WAIT_INTERVAL && !srcActivity.mIsDone){
                            srcActivity.mIsDone = true;
                            srcActivity.goAhead();
                }
                break;
            }
        }
    };

    private UiHandler mHandler;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mHandler = new UiHandler(this);
    }
    @Override
    protected void onStart(){
        super.onStart();
        mStartTime = SystemClock.uptimeMillis();
        final Message goAheadMessage = mHandler.obtainMessage(GO_AHEAD_WHAT);
        mHandler.sendMessageAtTime(goAheadMessage, mStartTime + MAX_WAIT_INTERVAL);
    }

    private void goAhead(){
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
