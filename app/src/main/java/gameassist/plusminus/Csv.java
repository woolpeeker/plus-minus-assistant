package gameassist.plusminus;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;

public class Csv {
    public static void write(int[][] data, String fname){
        String path_fname= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator+fname;
        StringBuffer str=new StringBuffer();
        int height=data.length;
        int width=data[0].length;
        for(int i=0;i<height;i++){
            for(int j=0;j<width;j++){
                if(j==width-1) {
                    str.append(data[i][j]);
                }else{
                    str.append(data[i][j]);
                    str.append(',');
                }
            }
            str.append('\n');
        }
        try {
            FileWriter file = new FileWriter(path_fname);
            file.write(str.toString());
            file.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void write(byte[][] data, String fname){
        String path_fname= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator+fname;
        StringBuffer str=new StringBuffer();
        int height=data.length;
        int width=data[0].length;
        for(int i=0;i<height;i++){
            for(int j=0;j<width;j++){
                if(j==width-1) {
                    str.append(data[i][j]);
                }else{
                    str.append(data[i][j]);
                    str.append(',');
                }
            }
            str.append('\n');
        }
        try {
            FileWriter file = new FileWriter(path_fname);
            file.write(str.toString());
            file.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
