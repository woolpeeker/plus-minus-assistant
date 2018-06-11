package gameassist.plusminus;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.content.Context;

import org.json.JSONArray;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileInputStream;
import java.io.InputStream;

import com.googlecode.tesseract.android.TessBaseAPI;

import gameassist.plusminus.Csv;
import gameassist.plusminus.CalcTime;
import gameassist.plusminus.BitmapUtil;


public class SimpleOCR {
    SimpleOCR(){
        ct=new CalcTime();
        this.tessocr=new TessOCR(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"","num");
        return;
    }


    public boolean iscorrect(Bitmap b){
        this.bitmap=b;
        String ocr_result=ocr();
        String[] all=ocr_result.split("=");
        String left=all[0], right=all[1];
        int result=Integer.valueOf(right);
        String[] left_split=left.split("[\\+\\-]");
        int lhs=Integer.valueOf(left_split[0]), rhs=Integer.valueOf(left_split[1]);
        int true_result=0;
        if(left.contains("+")){
            true_result=lhs+rhs;
        }else{
            true_result=lhs-rhs;
        }
        if(true_result==result)
            return true;
        else
            return false;
    }

    public String ocr(){
        ct.start();
        //binaryBitmap(128);
        binaryBitmap(128);
        ct.end();
        ct.start();
        tessocr.setBitmap(binary_bitmap);
        String result=tessocr.ocr();
        ct.end();
        result=result.replace("\n","");
        result=result.replace(" ","");
        this.ocr_result=result;
        return result;
    }

    public String ocr(float top, float down, float left, float right){
        int w=bitmap.getWidth();
        int h=bitmap.getHeight();
        int t=(int)(h*top);
        int d=(int)(h*down);
        int l=(int)(w*left);
        int r=(int)(w*right);
        return ocr(t,d,l,r);
    }

    private void binaryBitmap(int thres){
        int newPixel;
        int height=bitmap.getHeight(), width=bitmap.getWidth();
        binary_bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        for(int x = 0; x < bitmap.getWidth(); x++){
            for(int y = 0; y < bitmap.getHeight(); y++){
                int pixelColor = bitmap.getPixel(x, y);
                int pixelRed = (pixelColor >> 8) & 0xFF;
                if(pixelRed>thres) {
                    newPixel = 0xffffffff;
                }else{
                    newPixel=0xff000000;
                }

                binary_bitmap.setPixel(x, y, newPixel);
            }
        }
    }

    CalcTime ct;
    static int stride=6;
    private Bitmap bitmap;
    private Bitmap binary_bitmap;
    public String ocr_result;

    private TessOCR tessocr;
}

class TessOCR{
    public TessOCR(String data_path,String lang){
        baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(data_path, lang);
    }

    public void setBitmap(Bitmap b){
        this.bitmap=b;
    }

    public String ocr(){
        baseApi.setImage(bitmap);
        String recognizedText = baseApi.getUTF8Text();
        return recognizedText;
    }

    private Bitmap bitmap;
    private TessBaseAPI baseApi;
}