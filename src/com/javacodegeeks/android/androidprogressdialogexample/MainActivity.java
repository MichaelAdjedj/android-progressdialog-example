package com.javacodegeeks.android.androidprogressdialogexample;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	
	private Button startBtn;
	private DownloadFileAsync downloadFileAsync;
	private String saveDirPath = Environment.getExternalStorageDirectory().getPath()+"/zip/test.zip";
	private String location = Environment.getExternalStorageDirectory().getPath()+"/www/";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		startBtn = (Button)findViewById(R.id.btn1);
		
        startBtn.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v) {
        		if (downloadFileAsync != null) {
        			AsyncTask.Status asyncStatus = downloadFileAsync.getStatus();
        			Log.v("doClick", "diTask status is " + asyncStatus);
				}
        		new DownloadFileAsync(MainActivity.this).execute();
        		
//        		DownloadFileAsync downloadFileAsync = new DownloadFileAsync(MainActivity.this);
//        		downloadFileAsync.execute();
//        		
//        		if (downloadFileAsync.result.equalsIgnoreCase("true")) {
//        			new Decompress(saveDirPath, location, MainActivity.this).execute();
//				}
            }
        });
	}

}