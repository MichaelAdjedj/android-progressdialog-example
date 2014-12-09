package com.javacodegeeks.android.androidprogressdialogexample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class Decompress extends AsyncTask<Void, Integer, Integer> {

	private final static String TAG = "Decompress";
	private String zipFile;   
	private String location;
	
	ProgressDialog myProgressDialog;
    Context ctx;

	public Decompress(String zipFile, String location, Context ctx) {
		super();
		this.zipFile = zipFile;     
		this.location = location;
		this.ctx = ctx;
		dirChecker("");   
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		myProgressDialog = new ProgressDialog(ctx);
		myProgressDialog.setMessage("Please Wait... Unzipping");
		myProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		myProgressDialog.setCancelable(false);
		myProgressDialog.show();
	}

	@Override
	protected Integer doInBackground(Void... params){
		int count = 0;

		try  {
			ZipFile zip = new ZipFile(zipFile);
			myProgressDialog.setMax(zip.size());
			FileInputStream fin = new FileInputStream(zipFile);       
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;       
			while ((ze = zin.getNextEntry()) != null) {

				Log.v("Decompress", "Unzipping " + ze.getName());          
				if(ze.isDirectory()) {           
					dirChecker(ze.getName());         
				} else {      
					FileOutputStream fout = new FileOutputStream(location +ze.getName());
					
					byte[] buffer = new byte[8192];
					int len;
					while ((len = zin.read(buffer)) != -1) {
						fout.write(buffer, 0, len);
						count++;
						publishProgress(count);// Here I am doing the update of my progress bar
					}
					fout.close();
					zin.closeEntry();
					
				}                
			}       
			zin.close();    
		} catch(Exception e) {       
			Log.e("Decompress", "unzip", e);    
		}    
		return null;
	}

	protected void onProgressUpdate(Integer... progress) {
		myProgressDialog.setProgress(progress[0]); //Since it's an inner class, Bar should be able to be called directly
	}

	protected void onPostExecute(Integer... result) {
		Log.i(TAG, "Completed. Total size: "+result);
		if(myProgressDialog != null && myProgressDialog.isShowing()){
			myProgressDialog.dismiss();
		}
	}
	
	private void dirChecker(String dir)
	{
		File f = new File(location + dir);
		if(!f.isDirectory())
		{
			f.mkdirs();
		}
	}
}