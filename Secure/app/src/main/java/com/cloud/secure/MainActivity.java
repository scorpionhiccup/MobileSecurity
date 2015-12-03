package com.cloud.secure;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.widget.FrameLayout;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
	private Camera cam;
	private CameraPreview camPrev;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			cam = getCameraInstance();
			camPrev = new CameraPreview(this, cam);
			FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
			preview.addView(camPrev);

			String startTime, endTime;
			startTime = new SimpleDateFormat(getString(R.string.date_format), Locale.ENGLISH).format(new Date());
			endTime = new SimpleDateFormat(getString(R.string.date_format), Locale.ENGLISH).format(new Date().getTime() + 1000 * 60 * 40);

			String input = "{\"userId\":\"" + "himalaya" + "\",\"startTime\":\""
					+ startTime + "\",\"endTime\":\"" + endTime
					+ "\",\"location\":\"" + "east-iiith" + "\"}";
			try {

				new PostTask("http://ec2-52-74-222-81.ap-southeast-1.compute.amazonaws.com:8080/RestService/post/createSessionInfo",
						input).execute();
				String sessionInfo="It's done";
				/*postCall(
						"http://ec2-52-74-222-81.ap-southeast-1.compute.amazonaws.com:8080/RestService/post/createSessionInfo",
						input);*/
				//Log.d("info", sessionInfo );
				JSONParser parser = new JSONParser();
				JSONObject obj = (JSONObject) parser.parse(sessionInfo);
				sessionId = (String) obj.get("sessionId");

				new AlertDialog.Builder(this).setTitle(sessionId)
						.setMessage(sessionId).show();

			} catch (Exception e) {
				e.printStackTrace();

				new AlertDialog.Builder(this).setTitle(e.getMessage())
						.setMessage(e.getMessage()).show();
			}

			TimerTask task = new TakePicTask();
			Timer timer = new Timer();
			timer.schedule(task, 3000, 120000);
		}
	}

	private static final String user = "himalaya";
	private static String sessionId = null;

	private class TakePicTask extends TimerTask {
		@Override
		public void run() {
			cam.takePicture(null, null, jpgCallBack);
		}
	}

	private Camera getCameraInstance() {
		Camera camera = null;
		try {
			camera = Camera.open();
			camera.setDisplayOrientation(90);
		} catch (Exception e) {
			Log.e("MainActivity",
					"getCameraInstance() failed due to " + e.getMessage());
		}
		return camera;
	}

	private final PictureCallback jpgCallBack = new PictureCallback() {
		@SuppressWarnings("deprecation")
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			final String base64Data = Base64.encodeToString(data, 0);
			try {
				new PostTask("http://ec2-52-74-222-81.ap-southeast-1.compute.amazonaws.com:8080/RestService/post/imageInfo",
						"{\"userId\":\"" + user + "\",\"sessionId\":\""
								+ sessionId + "\",\"snapedAt\":\"" + new Date()
								+ "\",\"data\":\"" + base64Data + "\"}").execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
			camera.startPreview();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class PostTask extends AsyncTask<Void, String, String> {
		private final String mUrlStr, mInput;

		PostTask(String urlStr, String input){
			mUrlStr=urlStr;
			mInput=input;

		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				URL url = new URL(mUrlStr);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");

				OutputStream os = conn.getOutputStream();

				os.write(mInput.getBytes());
				//Log.d("info", mInput.getBytes().toString());
				os.flush();
				InputStreamReader read = new InputStreamReader(conn.getInputStream());
				BufferedReader br = new BufferedReader(read);
				String output;
				// pw.println("Output from Server .... \n");
				StringBuilder buff = new StringBuilder();
				while ((output = br.readLine()) != null) {
					buff.append(output);
					// pw.println(output);
				}
				conn.disconnect();
				return buff.toString();
			}
			catch (Exception e) {
				Log.e("info", e.getMessage());
				e.printStackTrace();
				return "";
			}
		}

		protected void onPostExecute(String result) {
			Log.d("info", "Result:" + result);
			//showDialog("Downloaded " + result + " bytes");
		}
	}

}
