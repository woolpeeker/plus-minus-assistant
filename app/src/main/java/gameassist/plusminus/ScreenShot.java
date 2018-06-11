package gameassist.plusminus;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.graphics.Matrix;


import java.nio.ByteBuffer;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


public class ScreenShot extends Service {
    public void onCreate() {
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);
        ct= new CalcTime();

        try{
            left_order=this.toByteArray("/sdcard/left.order");
            right_order=this.toByteArray("/sdcard/right.order");
        }catch(Exception e){
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority (android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                work_thread();
            }
        }).start();

    }

    public void work_thread(){
        Bitmap bitmap;
        SimpleOCR ocr;
        try {
            Thread.currentThread().sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ocr = new SimpleOCR();

        String last_ocr_result="";
        try {
            int count = 10000;
            CalcTime total_ct=new CalcTime();
            while (count-- > 0) {
                ct.start();
                bitmap = this.startCapture();
                boolean result=false;
                boolean flag_exception=false;
                ct.end();
                System.out.println("Capture time: "+ct.getResult());

                ct.start();
                try{result = ocr.iscorrect(bitmap);}catch(Exception e){e.printStackTrace(); flag_exception=true;}
                ct.end();
                System.out.println("OCR time: "+ct.getResult());

                ct.start();
                String ocr_result = ocr.ocr_result;
                if(ocr_result.equals(last_ocr_result) || flag_exception) {
                    continue;
                }else{
                    last_ocr_result=ocr_result;
                }

                if (result)
                    exec(dd_true);
                else
                    exec(dd_false);
                ct.end();
                System.out.println("Exec time: "+ct.getResult());
                System.out.println("Total time: "+total_ct.getResult());
                //Thread.currentThread().sleep(200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void builder_exec(String cmd){
        try{
            ProcessBuilder pb = new ProcessBuilder("su");
            Process p = pb.start();
            OutputStream out = p.getOutputStream();
            out.write(cmd.getBytes());
            out.flush();
        }catch(Exception e){
            e.printStackTrace();
        }
    }



    private void exec(String cmd){
        try {
            CalcTime ct=new CalcTime();
            ct.start();
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            process.waitFor();
            ct.end();
            return;
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public static Intent getResultData() {
        return mResultData;
    }

    public static void setResultData(Intent mResultData) {
        ScreenShot.mResultData = mResultData;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Bitmap startCapture() {
        //ct.start();
        Image image = mImageReader.acquireNextImage();
        while (image == null) {
            if (mMediaProjection == null) {
                setUpMediaProjection();
            }
            virtualDisplay();
            image = mImageReader.acquireNextImage();
        }
        Bitmap bitmap = getBitmap(image);
        //ct.end();
        return bitmap;
    }

    public void setUpMediaProjection() {
        if (mResultData == null) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(intent);
        } else {
            mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, mResultData);
        }
    }

    private MediaProjectionManager getMediaProjectionManager() {
        return (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private void virtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    protected Bitmap getBitmap(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        //每个像素的间距
        int pixelStride = planes[0].getPixelStride();
        //总的间距
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        Matrix matrix = new Matrix();
        matrix.setScale(1f/8,1f/8);
        bitmap = Bitmap.createBitmap(bitmap, (int)(0.15*width), (int)(3.5f/10*height), (int)(0.7*width), (int)(0.2*height),matrix,false);
        image.close();
        return bitmap;
    }
    private static byte[] toByteArray(String filename) throws Exception{
        File f = new File(filename);
        if(!f.exists()){
            throw new Exception(filename);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream((int)f.length());
        BufferedInputStream in = null;
        try{
            in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while(-1 != (len = in.read(buffer,0,buf_size))){
                bos.write(buffer,0,len);
            }
            return bos.toByteArray();
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally{
            try{
                in.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
            bos.close();
        }
    }

    private void writeToEvent(byte[] data){
        try{
            Process process = Runtime.getRuntime().exec("su");
        FileOutputStream output = new FileOutputStream(new File("/dev/input/event1"));
        output.write(data);
        output.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }



    private ImageReader mImageReader;
    private WindowManager mWindowManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    private static Intent mResultData = null;

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private CalcTime ct;

    static private final String dd_true="dd if=/sdcard/left.order  of=/dev/input/event1\n";
    static private final String dd_false="dd if=/sdcard/right.order  of=/dev/input/event1\n";

    static private byte[] left_order=null;
    static private byte[] right_order=null;


}
