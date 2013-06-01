package com.example.gpstester;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class drawTrack extends View {
	
	
	public drawTrack(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void draw(Canvas canvas) {
        
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(6);

        for(int i = 0; i < GPSTracker.gpsTracks.size(); i++){
        	canvas.drawLine(
        			(float) GPSTracker.oldGpsTracks.get(i).latitude, 
        			(float) GPSTracker.oldGpsTracks.get(i).longitude, 
        			(float) GPSTracker.gpsTracks.get(i).latitude, 
        			(float) GPSTracker.gpsTracks.get(i).longitude, 
        			paint);
        }
    }
}
