package com.example.gpstester;

public class GpsTrack {
	
	double longitude;
	double latitude;
	
	public GpsTrack(double longitude, double latitude){
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public double getLongitude(){
		return longitude;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public String toString(){
		return "Longitude: "+longitude+" Latitude: "+latitude+"\n";
	}
}
