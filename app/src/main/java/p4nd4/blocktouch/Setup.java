package p4nd4.blocktouch;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class Setup extends Activity{
    final public static int OVERLAY_PERMISSION_REQ_CODE = 1;
public boolean CheckedForPermissions=false;
    Button btn2;
    Button btn3;

    Switch sw1;
    boolean proximityState;
    SharedPreferences AppData;
    SharedPreferences.Editor editor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup);



        WebView webView = (WebView) findViewById(R.id.gif1);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.getSettings().setLoadsImagesAutomatically(true);

        webView.loadUrl("file:///android_asset/howto.htm");

        sw1 = (Switch) findViewById(R.id.ProximitySwitch);

        AppData = PreferenceManager.getDefaultSharedPreferences(this);
        proximityState = AppData.getBoolean("proximityState", false);



        sw1.setChecked(proximityState);
        sw1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                proximityState=!proximityState;
                editor = AppData.edit();
                editor.putBoolean("proximityState", proximityState);
                editor.commit();


            }
        });


        Button btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });

        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://play.google.com/store/apps/dev?id=5847423621940926942"));
                startActivity(i);

            }
        });

        btn3 = (Button) findViewById(R.id.btn3);
        btn3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://github.com/Panda-Soft");
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(i);
            }
        });

    }




    @Override
    protected void onResume() {
        super.onResume();
        checkPermissionOverlay();


    }
    @TargetApi(Build.VERSION_CODES.M)
    public void checkPermissionOverlay() {
        TextView PermissionsText;

        Log.d("Status", Boolean.toString(Settings.canDrawOverlays(this)));
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && (!Settings.canDrawOverlays(this))) {

            Toast.makeText(this, "Please select "+getString(R.string.app_name)+" and allow permissions first!", Toast.LENGTH_LONG).show();
            Intent intentSettings = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);

            startActivityForResult(intentSettings, OVERLAY_PERMISSION_REQ_CODE);

            PermissionsText = (TextView) findViewById(R.id.permissions);
            PermissionsText.setText(R.string.perm);
            PermissionsText.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);

        } else {
            PermissionsText = (TextView) findViewById(R.id.permissions);
            PermissionsText.setText("");
            PermissionsText.setTextSize(TypedValue.COMPLEX_UNIT_SP,0);
        }


    }

}
