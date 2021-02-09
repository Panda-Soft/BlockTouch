package p4nd4.blocktouch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
import static android.view.KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_REWIND;
import static android.view.KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_STEP_FORWARD;

public class AlwaysOnTopService extends Service implements OnTouchListener, OnClickListener {

    private View topLeftView;

    private WindowManager wm;
    PowerManager mgr;
    PowerManager.WakeLock wakeLock;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    //private InterstitialAd mInterstitialAd;
    private static final long DOUBLE_PRESS_INTERVAL = 250; // in millis
    private long lastPressTime;
    private boolean mHasDoubleClicked = false;
    private AudioManager mAudioManager;
    @Override
    public void onCreate() {
        super.onCreate();

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        SharedPreferences AppData = PreferenceManager.getDefaultSharedPreferences(this);


            int width = getScreenWidth();
            int height = getScreenHeight();

            topLeftView = new View(this);
            topLeftView.setOnTouchListener(this);

            topLeftView.setBackgroundColor(0x00000000);
            topLeftView.setOnClickListener(this);
            WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR, //Nougat
                    WindowManager.LayoutParams.TYPE_BASE_APPLICATION, //Temporary Nougat+Oreo
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                topLeftParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            } else {
                topLeftParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }

            topLeftParams.x = 0;
            topLeftParams.y = 0;

            topLeftParams.width = width;
            topLeftParams.height = height;
            wm.addView(topLeftView, topLeftParams);
            //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = mgr.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "blocktouch:MyWakeLock");

            if(AppData.getBoolean("proximityState",false)) {
                wakeLock.acquire();
                proximity_enabled=true;
            }


        }



    @Override
    public void onDestroy() {
        super.onDestroy();

        if (proximity_enabled) {
            wakeLock.release();
        }
        if (topLeftView != null) {
            wm.removeView(topLeftView);
            topLeftView = null;
        }
    }
    private float x1,x2,y1,y2;
    public boolean proximity_enabled=false;
    //static final int MIN_DISTANCE = 200;
    //static final int MAX_DISTANCE = 100;

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        long pressTime = System.currentTimeMillis();
        int short_side;
        if (getScreenHeight()>getScreenWidth()) {short_side=getScreenWidth();} else {short_side=getScreenHeight();}
        int MIN_DISTANCE=Math.round(short_side/3);
        int MAX_DISTANCE=Math.round(short_side/6);
        if ((System.currentTimeMillis() - lastPressTime <= DOUBLE_PRESS_INTERVAL*2) || (event.getAction() == MotionEvent.ACTION_UP)) {
            //Toast.makeText(this, "Unlocked!", Toast.LENGTH_SHORT).show();
            //this.stopSelf();
            //PlayToggle();
            //
            //mHasDoubleClicked = true;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    y1 = event.getY();
                    x1 = event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    y2 = event.getY();
                    float deltaY = y1 - y2;
                    x2 = event.getX();
                    float deltaX = x2 - x1;
                    if ((deltaY > MIN_DISTANCE) && (Math.abs(deltaX) < MAX_DISTANCE)) {
                        //Toast.makeText(this, "left2right swipe " + Float.toString(y1) + " " + Float.toString(y1), Toast.LENGTH_SHORT).show();
                        MediaKey(KEYCODE_MEDIA_PLAY_PAUSE);
                    }
                    if (((y2-y1) > MIN_DISTANCE) && (Math.abs(deltaX) < MAX_DISTANCE)) {
                        //Toast.makeText(this, "PT:"+Long.toString(pressTime)+"\nLPT:"+Long.toString(lastPressTime), Toast.LENGTH_SHORT).show();
                        if (proximity_enabled) {
                            Toast.makeText(this, "Proximity sensor disabled", Toast.LENGTH_SHORT).show();
                            wakeLock.release();
                        } else {
                            Toast.makeText(this, "Proximity sensor enabled", Toast.LENGTH_SHORT).show();
                            wakeLock.acquire();
                        }
                        proximity_enabled=!(proximity_enabled);

                    }

                    x1=0;
                    x2=0;
                    y1=0;
                    y2=0;
                    /*
                    if ((deltaX > MIN_DISTANCE) && (Math.abs(deltaY) < MAX_DISTANCE)) {
                        //Toast.makeText(this, "foward " + Float.toString(y1) + " " + Float.toString(y1), Toast.LENGTH_SHORT).show();
                        MediaKey(KEYCODE_MEDIA_SKIP_FORWARD);

                    }
                    if ((deltaX < MIN_DISTANCE) && (Math.abs(deltaY) < MAX_DISTANCE)) {
                    //Toast.makeText(this, "back " + Float.toString(y1) + " " + Float.toString(y1), Toast.LENGTH_SHORT).show();
                        //MediaKey(KEYCODE_MEDIA_REWIND);
                        MediaKey(KEYCODE_MEDIA_SKIP_BACKWARD);

                    }*/
                    break;
            }
        }


        return false;

    }



    @Override
    public void onClick(View v) {

        long pressTime = System.currentTimeMillis();


        // If double click...
        if (pressTime - lastPressTime <= DOUBLE_PRESS_INTERVAL && mHasDoubleClicked) {
            // 3 click action
            Toast.makeText(this, "Unlocked!", Toast.LENGTH_SHORT).show();


            this.stopSelf();
        }
        if (pressTime - lastPressTime <= DOUBLE_PRESS_INTERVAL) {


                        mHasDoubleClicked = true;
        }
        else {     // If not double click....
            mHasDoubleClicked = false;
            Handler myHandler = new Handler() {
                public void handleMessage(Message m) {
                    if (!mHasDoubleClicked) {
                        //Toast.makeText(this, "Single", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            Message m = new Message();
            myHandler.sendMessageDelayed(m,DOUBLE_PRESS_INTERVAL);
        }
        // record the last time the menu button was pressed.
        lastPressTime = pressTime;
    }


    void MediaKey(int key){
        if (mAudioManager == null) mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);

        KeyEvent eventkey = new KeyEvent(KeyEvent.ACTION_DOWN, key);
        mAudioManager.dispatchMediaKeyEvent(eventkey);
    }
}