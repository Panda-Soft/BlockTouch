package p4nd4.blocktouch;


import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;



public class BlockTouchTile extends TileService {



    public void collapseNow() {


        if (collapseNotificationHandler == null) {
            collapseNotificationHandler = new Handler();
        }


            collapseNotificationHandler.postDelayed(new Runnable() {

                @Override
                public void run() {



                    Object statusBarService = getSystemService("statusbar");
                    Class<?> statusBarManager = null;

                    try {
                        statusBarManager = Class.forName("android.app.StatusBarManager");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    Method collapseStatusBar = null;

                    try {

                        // Prior to API 17, the method to call is 'collapse()'
                        // API 17 onwards, the method to call is `collapsePanels()`

                        if (Build.VERSION.SDK_INT > 16) {
                            collapseStatusBar = statusBarManager .getMethod("collapsePanels");
                        } else {
                            collapseStatusBar = statusBarManager .getMethod("collapse");
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    collapseStatusBar.setAccessible(true);

                    try {
                        collapseStatusBar.invoke(statusBarService);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }


                }
            }, 300L);

    }
    static public int OVERLAY_PERMISSION_REQ_CODE = 1;
      Handler collapseNotificationHandler;
        @Override
        public void onClick() {

            super.onClick();
            collapseNow();

            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Start app and configure permissions first!", Toast.LENGTH_SHORT).show();
            } else {
                Intent svc = new Intent(this, AlwaysOnTopService.class);
                startService(svc);
            }

        }
    }
