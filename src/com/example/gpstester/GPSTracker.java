package com.example.gpstester;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener {
	private final Context mContext;
	 
    // flag for GPS status
    boolean isGPSEnabled = false;
 
    // flag for network status
    boolean isNetworkEnabled = false;
 
    boolean canGetLocation = false;
 
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    double gpslatitude; // latitude
    double gpslongitude; // longitude
    
    public static ArrayList<GpsTrack> gpsTracks = new ArrayList<GpsTrack>();
    public static ArrayList<GpsTrack> gpsInternetTracks = new ArrayList<GpsTrack>();
    public static ArrayList<GpsTrack> oldGpsTracks = new ArrayList<GpsTrack>();
    
    boolean isfirstMove = true;
 
    // The minimum distance to change Updates in meters
    public static long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
 
    // The minimum time between updates in milliseconds
    public static long MIN_TIME_BW_UPDATES = 0;
 
    // Declaring a Location Manager
    protected LocationManager locationManager;
 
    public GPSTracker(Context context) {
        this.mContext = context;
        
        try {
	        locationManager = (LocationManager) mContext
	                .getSystemService(LOCATION_SERVICE);
	        
	        locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        } catch (Exception e) {
	        e.printStackTrace();
	    }
    }

	@Override
	public void onLocationChanged(Location location) {
		try {
	        locationManager = (LocationManager) mContext
	                .getSystemService(LOCATION_SERVICE);
	
	        // getting GPS status
	        isGPSEnabled = locationManager
	                .isProviderEnabled(LocationManager.GPS_PROVIDER);
	
	        // getting network status
	        isNetworkEnabled = locationManager
	                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	
	        if (!isGPSEnabled && !isNetworkEnabled && !GPSTesterActivity.getGps) {
	            // no network provider is enabled
	        } else {
	            this.canGetLocation = true;
	            // First get location from Network Provider
	            if (isNetworkEnabled && GPSTesterActivity.internetCheck.isChecked()) {
	                locationManager.requestLocationUpdates(
	                        LocationManager.NETWORK_PROVIDER,
	                        MIN_TIME_BW_UPDATES,
	                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	                Log.d("Network", "Network");
	                if (locationManager != null) {
	                    location = locationManager
	                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	                    if (location != null) {
	                        latitude = location.getLatitude();
	                        longitude = location.getLongitude();
	                    }
	                }
	            }
	            if(longitude != 0.0 && latitude != 0.0)
	            	gpsInternetTracks.add(new GpsTrack(longitude, latitude));
	            // if GPS Enabled get lat/long using GPS Services
	            if (isGPSEnabled && GPSTesterActivity.gpsCheck.isChecked()) {
	                //if (location == null) {
	                    locationManager.requestLocationUpdates(
	                            LocationManager.GPS_PROVIDER,
	                            MIN_TIME_BW_UPDATES,
	                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	                    Log.d("GPS Enabled", "GPS Enabled");
	                    if (locationManager != null) {
	                        location = locationManager
	                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
	                        if (location != null) {
	                            gpslatitude = location.getLatitude();
	                            gpslongitude = location.getLongitude();
	                        }
	                    }
	                //}
	            }
	        }
	        if(gpslongitude != 0.0 && gpslatitude != 0.0)
	        	gpsTracks.add(new GpsTrack(gpslongitude, gpslatitude));
	
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
		
	@Override
	public void onProviderDisabled(String provider) {
	}
	
	@Override
	public void onProviderEnabled(String provider) {
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
	    return null;
	}
	
	/**
     * Function to get latitude
     * */
    public float getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
         
        // return latitude
        return (float) latitude;
    }
     
    /**
     * Function to get longitude
     * */
    public float getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
         
        // return longitude
        return (float) longitude;
    }
    
    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
     
    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
      
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
  
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
  
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
  
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
    }
}
