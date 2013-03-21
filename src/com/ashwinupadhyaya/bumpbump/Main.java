package com.ashwinupadhyaya.bumpbump;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.location.GpsStatus.Listener;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v4.app.FragmentActivity;

public class Main extends FragmentActivity implements OnClickListener, SensorEventListener, LocationListener, Listener{

	private GoogleMap mMap;
	static RoadStates rStates;
	CSensorStates mSenStates;
	CLocProvStates mLPStates;
	SensorManager mSenMan;
	LocationManager mLocMan;
	static int numSensors = 0;
	CLogView mLV;
	int evno=0;
	static long RefTime=-1;
	Button mbtn_event;
	private DataOutputStream[] fout = null; 
	private SimpleDateFormat dtf= new SimpleDateFormat("dd.HH.mm.ss");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mLV=(CLogView) findViewById(R.id.DLtv1);
		
		SensorManager lSenMan=(SensorManager) getSystemService(SENSOR_SERVICE);
		mSenMan=lSenMan;
		LocationManager lLocMan=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLocMan=lLocMan;

		//Get the names of all sources
		mSenStates = new CSensorStates(lSenMan.getSensorList(Sensor.TYPE_ALL));
		mSenStates.setRate(SensorManager.SENSOR_DELAY_GAME);	//Set the sensor rate to the maximum (default is UI)
		CSensorStates lSenStates=mSenStates;

		mLPStates = new CLocProvStates(lLocMan.getAllProviders());
		CLocProvStates lLPStates=mLPStates;
		
		numSensors = lSenStates.getNumAct();
		fout = new DataOutputStream[numSensors + 2]; // One extra for event file. One more for location
		
		mbtn_event = (Button) findViewById(R.id.EventButton);
		mbtn_event.setOnClickListener(this);
		
		setUpMapIfNeeded();
		
		rStates = new RoadStates((float)20.0, 1000, 100, (float)0.75, (float)1.1, (float)1.1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	 public boolean onOptionsItemSelected(MenuItem item) {
		if (item.hasSubMenu())
			return true;	//Do nothing
		
		if (item.getItemId()==R.id.menu_about){
			Intent lIntent=new Intent(this, About.class);
			startActivity(lIntent);
		}
		else if (item.getItemId()==R.id.menu_developer){
			Intent lIntent=new Intent(this, Developer.class);
			startActivity(lIntent);
		}
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private void stop_recording() {
		//Stop Recording
		mSenMan.unregisterListener(this);
		
		//Close files
		close_files();
		
	}

	private void register_listeners() {
		
		CSensorStates lSenStates=mSenStates;
		DataOutputStream[] lfout=fout;
		SensorManager lSenMan=mSenMan;
		CLocProvStates lLPStates=mLPStates;
		LocationManager lLocMan=mLocMan;
		
		//Register the sensors
		if (lfout[0]!=null) {
			for (int i=0;i<lSenStates.getNum();i++) {
				if (lSenStates.getActive(i))
					lSenMan.registerListener(this, lSenMan.getDefaultSensor(lSenStates.getType(i)), lSenStates.getRate(i));
			}

			//Register listeners for active location providers
			for (int i=0;i<lLPStates.getNum();i++) {
				if (lLPStates.getActive(i))
					lLocMan.requestLocationUpdates(lLPStates.getName(i), lLPStates.getMinTime(i), lLPStates.getMinDist(i), this);
			}
			lLocMan.addGpsStatusListener(this);
		}

	}

	private void open_files() throws FileNotFoundException{
		
		//Refs
		CSensorStates lSenStates=mSenStates;
		DataOutputStream[] lfout=fout;
		
				
		//Open the files and register the listeners
		if (numSensors>0) {
			for (int i=0;i<numSensors;i++)
			{
				lfout[i]=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file_location("_" + lSenStates.getActName(i) + ".txt"))));
			}
			lfout[numSensors] = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file_location("_" + "KeyEvent.txt"))));
			lfout[numSensors+1] = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file_location("_" + "Location.txt"))));
		}
	}

	private File file_location(String ntag) {
		
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		
		if (mExternalStorageAvailable && mExternalStorageWriteable) {
			String ftag=dtf.format(new Date());
			
			return new File(getExternalFilesDir(null), ftag+ntag);
		}
		else {
			mLV.addtext("No external Storage.");
			return null;
		}
	}
	
	private void close_files() {
		DataOutputStream[] lfout=fout;
		
		for (int i=0;i<(numSensors+2);i++) {
			if (lfout[i]!=null)
				try {
					lfout[i].close();
				} catch (IOException e) {
					mLV.addtext("File close error :" + i);
				}
		}
		
		rStates.close_files();
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		close_files();
	}

	@Override
	public void onSensorChanged(SensorEvent ev) {
		CSensorStates lSenStates = mSenStates;
		DataOutputStream file=fout[lSenStates.getActIdByTyp(ev.sensor.getType())];
		if (file==null)
			//Something is wrong
			return;
		
		int len=ev.values.length;
		long tim=System.currentTimeMillis() - RefTime;
		String str = "";
		try {
			str = str + ev.sensor.getType() + "\t" + 
					tim + "\t" + len + "\t";
			for (int i=0;i<len;i++)
				str = str + ev.values[i] + "\t";
			file.writeBytes(str + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		rStates.UpdateAccelerometerInfo(tim, ev.values[0], ev.values[2]);
	}

	@Override
	public void onClick(View arg0) {
		DataOutputStream file=fout[numSensors];
		
		if (arg0.getId()==R.id.EventButton) { //Put an event marker
			if(evno == 0)
			{
				RefTime = System.currentTimeMillis();
				try {
					FileOutputStream fos = new FileOutputStream(file_location("_" + "LatLong.txt"));
					rStates.open_files(fos);
					
					open_files();
					register_listeners();
					mLV.addtext("Started Logging");
				} catch (FileNotFoundException e) {
					mLV.addtext("File open error: Probably you do not have require permissions.");
					stop_recording();
				}

				file=fout[numSensors];
				
				if (file!=null) {
					String str = "";
					try {
						str = str +  evno + "\t" + (System.currentTimeMillis()-RefTime) + "\n";
						file.writeBytes(str + "\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			else
			{
				if (file==null)
					//Something is wrongr
					return;
				
				String str = "";
				try {
					str = str +  evno + "\t" + (System.currentTimeMillis()-RefTime) + "\n";
					file.writeBytes(str + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//mLV.addtext("Event No:"+ evno +" Time :" + (System.currentTimeMillis()-RefTime));
			evno++; 
		}
	}

	/////////Location provider callbacks
	public void onLocationChanged(Location loc) {
		DataOutputStream file=fout[numSensors+1];
		if (file==null)
			//Something is wrong
			return;
		long tim=System.currentTimeMillis() - RefTime;
		int typ=loc.getProvider().length();		//Seems a good identifier
		String str = "";
		try {
			str = str + typ + "\t" + tim + "\t" + loc.getTime() + 
					"\t" + loc.getAccuracy() + "\t" + loc.getAltitude() +
					"\t" + loc.getLatitude() + "\t" + loc.getLongitude() + 
					"\t" + loc.getBearing() + "\t" + loc.getSpeed();
			file.writeBytes(str + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		rStates.UpdateGPSInfo(loc.getAccuracy(), tim, loc.getSpeed(), 
				loc.getLatitude(), loc.getLongitude());
	}

	public void onProviderDisabled(String arg0) {
		
	}

	public void onProviderEnabled(String arg0) {
		
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		
	}

	@Override
	public void onGpsStatusChanged(int status) {

	}

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(12.849983003152731, 77.59079661631549)).title("Nice Road"));
    }
}
