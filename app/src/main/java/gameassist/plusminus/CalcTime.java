package gameassist.plusminus;

import android.app.ProgressDialog;
import android.content.Context;
import gameassist.plusminus.CalcTime;

public class CalcTime {
    public void start(){
        this.s=System.currentTimeMillis();
    }

    public CalcTime end(){
        this.e=System.currentTimeMillis();
        this.result=e-s;
        return this;
    }
    public void show(Context c){
        new ProgressDialog.Builder(c)
                .setTitle("Time")
                .setMessage(String.valueOf(this.result))
                .setPositiveButton("OK",null)
                .show();
    }

    public long getResult(){return this.result;}

    private long s;
    private long e;
    private long result;
}
