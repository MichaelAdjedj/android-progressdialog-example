package com.javacodegeeks.android.androidprogressdialogexample;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

class DownloadFileAsync extends AsyncTask<String, String, String> {

	private final static String TAG = "DownloadFileAsync";
	String result = null;

	private String server = "aproove.compilsoft.com";
	private int port = 21;
	private String user = "test_ftp";
	private String pass = "compilsoft";

	private String remoteDirPath = "/zip/test.zip";
	private String saveDirPath = Environment.getExternalStorageDirectory().getPath()+"/zip/test.zip";
	private String location = Environment.getExternalStorageDirectory().getPath()+"/www/";

	FTPClient ftpClient = new FTPClient();

	ProgressDialog myProgressDialog;
	Context ctx;

	public DownloadFileAsync(Context ctx) {
		super();
		this.ctx = ctx;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		myProgressDialog = new ProgressDialog(ctx);
		myProgressDialog.setMessage("Downloading file..");
		myProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		myProgressDialog.setCancelable(false);
		myProgressDialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		int count;

		try{
			ftpClient.connect(server, port);
			ftpClient.login(user, pass);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			File downloadFile = new File(saveDirPath);

			FTPFile ftpFile = ftpClient.mlistFile(remoteDirPath);
			int lenghtOfFile = (int) ftpFile.getSize();


			File parentDir = downloadFile.getParentFile();
			if (!parentDir.exists()) {
				parentDir.mkdir();
			}

			OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
			InputStream inputStream = ftpClient.retrieveFileStream(remoteDirPath);
			byte data[] = new byte[4096];
			long total = 0;
			while ((count = inputStream.read(data)) != -1) {
				total += count;
				publishProgress(""+(int)((total*100)/lenghtOfFile));
				outputStream.write(data, 0, count);
			}

			boolean success = ftpClient.completePendingCommand();
			if (success && myProgressDialog.getProgress()==myProgressDialog.getMax()) {
				Log.i(TAG, "Zip File has been downloaded successfully.");
			}
			outputStream.close();
			inputStream.close();
			result = "true";

		} catch (IOException ex) {
			Log.e(TAG, ex.getMessage());
			ex.printStackTrace();
			result = "false";
		} finally {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.logout();
					ftpClient.disconnect();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	protected void onProgressUpdate(String... progress) {
		Log.d("ANDRO_ASYNC",progress[0]);
		myProgressDialog.setProgress(Integer.parseInt(progress[0]));
	}

	@Override
	protected void onPostExecute(String unused) {
		if(myProgressDialog != null && myProgressDialog.isShowing()){
			myProgressDialog.dismiss();
			try{
				unzip();
			} catch (IOException e){
				Log.e(TAG, "unzip :"+e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void unzip() throws IOException {
		myProgressDialog = new ProgressDialog(ctx);
		myProgressDialog.setMessage("Please Wait...");
		myProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		myProgressDialog.setCancelable(false);
		myProgressDialog.show();
		new UnZipTask().execute(saveDirPath, location);
	}

	private class UnZipTask extends AsyncTask<String, Void, Boolean> {
		@SuppressWarnings("rawtypes")
		@Override
		protected Boolean doInBackground(String... params) {
			String filePath = params[0];
			String destinationPath = params[1];

			File archive = new File(filePath);
			try {
				ZipFile zipfile = new ZipFile(archive);
				for (Enumeration e = zipfile.entries(); e.hasMoreElements();) {
					ZipEntry entry = (ZipEntry) e.nextElement();
					unzipEntry(zipfile, entry, destinationPath);
				}

				UnzipUtil d = new UnzipUtil(saveDirPath, location); 
				d.unzip();

			} catch (Exception e) {
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			myProgressDialog.dismiss(); 

		}


		private void unzipEntry(ZipFile zipfile, ZipEntry entry,String outputDir) throws IOException {

			if (entry.isDirectory()) {
				createDir(new File(outputDir, entry.getName()));
				return;
			}

			File outputFile = new File(outputDir, entry.getName());
			if (!outputFile.getParentFile().exists()){
				createDir(outputFile.getParentFile());
			}

			// Log.v("", "Extracting: " + entry);
			BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
			BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

			try{

			}finally {
				outputStream.flush();
				outputStream.close();
				inputStream.close();
			}
		}

		private void createDir(File dir) {
			if (dir.exists()) {
				return;
			}
			if (!dir.mkdirs()) {
				throw new RuntimeException("Can not create dir " + dir);
			}
		}
	}
}