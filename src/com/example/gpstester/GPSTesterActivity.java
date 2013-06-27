package com.example.gpstester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.xmlpull.v1.XmlSerializer;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class GPSTesterActivity extends Activity implements OnSeekBarChangeListener {

	Handler handler = new Handler();
	Context contextGps = this;
	
	private PowerManager.WakeLock wl;
	private PowerManager pm;
	
	public int zeit;
	
	// flag for network status
    boolean isNetworkEnabled = false;
    double latitude; // latitude
    double longitude; // longitude
    public static ArrayList<GpsTrack> gpsInternetTracks = new ArrayList<GpsTrack>();
    public static ArrayList<Integer> batteryLog = new ArrayList<Integer>();
    protected LocationManager locationManager;
	
	Button b1, start;
	public static CheckBox gpsCheck, internetCheck;
	SeekBar meterBar, timeBar;
	public static TextView meter, time, timeBarValue, meterBarValue, counter;
	public static EditText timeToRun;

	GPSTracker gps;
	public static boolean getGps = false;
	Handler timeHandler = new Handler();
	Runnable run;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gpstester);
        
        gpsCheck = (CheckBox) findViewById(R.id.GPSCheck);
        internetCheck = (CheckBox) findViewById(R.id.InternetCheck);
        
        timeToRun = (EditText) findViewById(R.id.timeToRun);
        
        meterBar = (SeekBar) findViewById(R.id.MeterBar);
        meterBar.setOnSeekBarChangeListener(this);
        
        timeBar = (SeekBar) findViewById(R.id.ZeitBar);
        timeBar.setOnSeekBarChangeListener(this);
        
        meter = (TextView) findViewById(R.id.Meter);
        time = (TextView) findViewById(R.id.Zeit);
        meterBarValue = (TextView) findViewById(R.id.meterBarValue);
        timeBarValue = (TextView) findViewById(R.id.timeBarValue);
        
        counter = (TextView) findViewById(R.id.counter);

		start = (Button) findViewById(R.id.start);

		gps = new GPSTracker(GPSTesterActivity.this);

		final Runnable r = new Runnable()
		{			
		    public void run() 
		    {
		    	if(internetCheck.isChecked())
		    		getNetworkFix();
		    	
		    	try{
					IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
					Intent batteryStatus = contextGps.registerReceiver(null, ifilter);
					
					int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
					batteryLog.add(level);
		    	} catch (Exception e) {
					Toast msg = Toast.makeText(getBaseContext(),
		    				e.getMessage(), Toast.LENGTH_LONG);
		    				msg.show();
				}
		    	
		    	counter.setText(--zeit + "s");
		    	
		        handler.postDelayed(this, 1000);
		    }
		};
		
		
		run = new Runnable() {
		        @Override
		        public void run() {
		        	handler.removeCallbacks(r);
		        	timeHandler.removeCallbacks(run);
					save();
		        }
		};
		
		start.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{				
				start.setEnabled(false);
				b1.setEnabled(true);
				internetCheck.setEnabled(false);
				gpsCheck.setEnabled(false);
				timeToRun.setEnabled(false);
				meterBar.setEnabled(false);
				timeBar.setEnabled(false);
				try {
					pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
					wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
					"GPSTester");
					wl.acquire();
				} catch (Exception ex) {
					Log.e("exception", ex.getMessage());
				}
				
				GPSTracker.MIN_DISTANCE_CHANGE_FOR_UPDATES = Long.parseLong(meterBarValue.getText().toString());
				GPSTracker.MIN_TIME_BW_UPDATES = Long.parseLong(GPSTesterActivity.timeBarValue.getText().toString())*1000;
				getGps = true;
				
				//Zeit wie lange gemessen werden soll
				zeit = Integer.parseInt(timeToRun.getText().toString())*60000;
				timeHandler.postDelayed(run, zeit); // 900000 = 15 Minuten; 1200000 = 20min
				
				zeit = zeit/1000;
				
				counter.setText(""+zeit+"s");
				
				handler.postDelayed(r, 1000);
			}
		});
		

		b1 = (Button) findViewById(R.id.button1);
		b1.setEnabled(false);
		b1.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				handler.removeCallbacks(r);
				timeHandler.removeCallbacks(run);
				save();
			}
		});

	}
	
	private void save(){
		b1.setEnabled(false);
		start.setEnabled(true);
		internetCheck.setEnabled(true);
		gpsCheck.setEnabled(true);
		timeToRun.setEnabled(true);
		meterBar.setEnabled(true);
		timeBar.setEnabled(true);
		try{
			wl.release();
		}catch(Exception e){
			Log.e("exception", e.getMessage());
		}
		
		getGps = false;

		File GpsTesterFolder = new File(Environment.getExternalStorageDirectory()+"/GPSTester");
		if (!GpsTesterFolder.exists()) {
		    GpsTesterFolder.mkdir();
		}

		SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy-HHmmss", Locale.GERMANY);
		format.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
		String path = Environment.getExternalStorageDirectory()+"/GPSTester/"+format.format(new Date());
		File gpsFile = new File(path+".gpx");
		File batteryFile = new File(path+"-Battery.txt");
		saveXML(gpsFile);
		Toast msg = Toast.makeText(getBaseContext(),
				"GPX Saved", Toast.LENGTH_LONG);
		msg.show();
		saveBattery(batteryFile);

		Toast msg1 = Toast.makeText(getBaseContext(),
				"Battery Log Saved", Toast.LENGTH_LONG);
		msg1.show();
	}
	
	private void getNetworkFix(){
		
		locationManager = (LocationManager) contextGps
                .getSystemService(LOCATION_SERVICE);
		
		// getting network status
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        
		if (isNetworkEnabled && GPSTesterActivity.internetCheck.isChecked()) {
			locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    GPSTracker.MIN_TIME_BW_UPDATES,
                    GPSTracker.MIN_DISTANCE_CHANGE_FOR_UPDATES, gps);
            Log.d("Network", "Network");
            if (locationManager != null) {
                Location location = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
		
			if(longitude != 0.0 && latitude != 0.0){
	        	gpsInternetTracks.add(new GpsTrack(longitude, latitude));
	        	/*Toast msg = Toast.makeText(getBaseContext(),
	    				""+longitude, Toast.LENGTH_SHORT);
	    				msg.show();*/
			}
		}
	}
	
	private void saveBattery(File newfile){
			try {
				newfile.createNewFile();
			
				FileOutputStream fOut = new FileOutputStream(newfile);
				OutputStreamWriter myOutWriter = 
										new OutputStreamWriter(fOut);
				int i = 1;
				for(int batlvl : batteryLog)
					myOutWriter.append(i++ + "\t" + batlvl + "\n");
				
				myOutWriter.close();
				fOut.close();  
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private void saveXML(File newxmlfile){
		try {
        	newxmlfile.createNewFile();
		} catch (IOException e) {

		}
        FileOutputStream fileos = null;        
        try {
        	fileos = new FileOutputStream(newxmlfile);
		} catch (FileNotFoundException e) {
			Toast msg = Toast.makeText(getBaseContext(),
    				e.getMessage(), Toast.LENGTH_LONG);
    				msg.show();
		}

        XmlSerializer serializer = Xml.newSerializer();
        try {
        	serializer.setOutput(fileos, "UTF-8"); //$NON-NLS-1$
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true); //$NON-NLS-1$
            serializer.startDocument("UTF-8", true); //$NON-NLS-1$
            serializer.startTag(null, "gpx"); //$NON-NLS-1$
            serializer.attribute(null, "version", "1.1"); //$NON-NLS-1$ //$NON-NLS-2$
            serializer.attribute(null, "creator", "FriedrichF"); //$NON-NLS-1$
            serializer.attribute("xmlns", "xsi", "http://www.w3.org/2001/XMLSchema-instance"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            serializer.attribute("xsi", "schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            serializer.attribute(null, "xmlns", "http://www.topografix.com/GPX/1/1"); //$NON-NLS-1$ //$NON-NLS-2$
            //Wenn GPS nicht ausgewählt wurde
            if(GPSTracker.gpsTracks.size() != 0){
	            serializer.startTag(null, "trk");
	            	serializer.startTag(null, "name");
	            	serializer.text("GPSTrack");
	            	serializer.endTag(null, "name");
	            	serializer.startTag(null, "trkseg");
		            	for (GpsTrack l : GPSTracker.gpsTracks) {
			                serializer.startTag(null, "trkpt"); //$NON-NLS-1$
			                serializer.attribute(null, "lat", l.latitude + ""); //$NON-NLS-1$ //$NON-NLS-2$
			                serializer.attribute(null, "lon", l.longitude + ""); //$NON-NLS-1$ //$NON-NLS-2$
			                serializer.endTag(null, "trkpt"); //$NON-NLS-1$
			            }
	            	serializer.endTag(null, "trkseg");
	            serializer.endTag(null, "trk");
            }
            
            //Wenn Internet aufzeichnung nicht ausgewählt wurde
            if(gpsInternetTracks.size() != 0){
	        	serializer.startTag(null, "trk");
		        	serializer.startTag(null, "name");
		        	serializer.text("GPSInternetTrack");
		        	serializer.endTag(null, "name");
		            serializer.startTag(null, "trkseg");
		            	for (GpsTrack l : gpsInternetTracks) {
			                serializer.startTag(null, "trkpt"); //$NON-NLS-1$
			                serializer.attribute(null, "lat", l.latitude + ""); //$NON-NLS-1$ //$NON-NLS-2$
			                serializer.attribute(null, "lon", l.longitude + ""); //$NON-NLS-1$ //$NON-NLS-2$
			                serializer.endTag(null, "trkpt"); //$NON-NLS-1$
			              }
		            serializer.endTag(null, "trkseg");
	            serializer.endTag(null, "trk");
            }
            serializer.endTag(null, "gpx");
            serializer.endDocument();
            serializer.flush();
            fileos.close();
        }
        catch (Exception ex)
        {
        	Toast msg = Toast.makeText(getBaseContext(),
    				ex.getMessage(), Toast.LENGTH_LONG);
    				msg.show();
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gpstester, menu);
		return true;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		Log.v("", "" + seekBar);

		switch (seekBar.getId()) {

	    case R.id.MeterBar:
	    	meterBarValue.setText(""+progress);
	        break;

	    case R.id.ZeitBar:
	    	timeBarValue.setText(""+progress);
	        break;
	    }

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}	

}