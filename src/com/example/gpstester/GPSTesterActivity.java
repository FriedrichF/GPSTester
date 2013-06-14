package com.example.gpstester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.xmlpull.v1.XmlSerializer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class GPSTesterActivity extends Activity implements OnSeekBarChangeListener {
		
	Button b1, start;
	public static CheckBox gpsCheck, internetCheck;
	SeekBar meterBar, timeBar;
	public static TextView meter, time, timeBarValue, meterBarValue;
	
	// The minimum distance to change Updates in meters
    public static long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
 
    // The minimum time between updates in milliseconds
    public static long MIN_TIME_BW_UPDATES = 0;
    
    public static Handler handler = new Handler();
	
	GPSTracker gps;
	public static drawTrack view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gpstester);
		
		/*view = new drawTrack(this);
        
        ViewGroup.LayoutParams params = 
                            new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
                                                       LayoutParams.FILL_PARENT);
        addContentView(view, params);*/
        
        gpsCheck = (CheckBox) findViewById(R.id.GPSCheck);
        internetCheck = (CheckBox) findViewById(R.id.InternetCheck);
        
        meterBar = (SeekBar) findViewById(R.id.MeterBar);
        meterBar.setOnSeekBarChangeListener(this);
        
        timeBar = (SeekBar) findViewById(R.id.ZeitBar);
        timeBar.setOnSeekBarChangeListener(this);
        
        meter = (TextView) findViewById(R.id.Meter);
        time = (TextView) findViewById(R.id.Zeit);
        meterBarValue = (TextView) findViewById(R.id.meterBarValue);
        timeBarValue = (TextView) findViewById(R.id.timeBarValue);
		
		start = (Button) findViewById(R.id.start);
		
		gps = new GPSTracker(GPSTesterActivity.this);

		start.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				MIN_DISTANCE_CHANGE_FOR_UPDATES = Long.parseLong(meterBarValue.getText().toString());
				MIN_TIME_BW_UPDATES = Long.parseLong(GPSTesterActivity.timeBarValue.getText().toString())*1000;
				meterBarValue.setText("asdf");
				handler.postDelayed(gps.runnable, MIN_TIME_BW_UPDATES);
				
			}
		});
		
		b1 = (Button) findViewById(R.id.button1);

		b1.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				handler.removeCallbacks(gps.runnable);
				
				File GpsTesterFolder = new File(Environment.getExternalStorageDirectory()+"/GPSTester");
				if (!GpsTesterFolder.exists()) {
				    GpsTesterFolder.mkdir();
				}
				
				SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy-HHmmss", Locale.GERMANY);
				format.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
				File gpsFile = new File(Environment.getExternalStorageDirectory()+"/GPSTester/"+format.format(new Date())+".gpx");
				saveXML(gpsFile);
				
				
				Toast msg = Toast.makeText(getBaseContext(),
				"Saved", Toast.LENGTH_LONG);
				msg.show();
			}
		});

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
            if(GPSTracker.gpsInternetTracks.size() != 0){
	        	serializer.startTag(null, "trk");
		        	serializer.startTag(null, "name");
		        	serializer.text("GPSInternetTrack");
		        	serializer.endTag(null, "name");
		            serializer.startTag(null, "trkseg");
		            	for (GpsTrack l : GPSTracker.gpsInternetTracks) {
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
