package gameassist.plusminus;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.media.projection.MediaProjectionManager;
import android.content.Context;
import android.content.Intent;

import gameassist.plusminus.ScreenShot;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestCapturePermission();

    }

    public void requestCapturePermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //5.0 之后才允许使用屏幕截图
            System.out.println("Android version must > 5.0");
            return;
        }

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:

                if (resultCode == RESULT_OK && data != null) {
                    ScreenShot.setResultData(data);
                    startService(new Intent(getApplicationContext(), ScreenShot.class));
                }
                break;
        }
    }

    public static final int REQUEST_MEDIA_PROJECTION = 18;
}
