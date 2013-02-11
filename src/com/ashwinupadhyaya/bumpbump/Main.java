package com.ashwinupadhyaya.bumpbump;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
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

public class Main extends Activity implements OnClickListener, SensorEventListener{

	CSensorStates mSenStates;
	SensorManager mSenMan;
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

		//Get the names of all sources
		mSenStates = new CSensorStates(lSenMan.getSensorList(Sensor.TYPE_ALL));
		mSenStates.setRate(SensorManager.SENSOR_DELAY_FASTEST);	//Set the sensor rate to the maximum (default is UI)
		CSensorStates lSenStates=mSenStates;
		
		fout = new DataOutputStream[lSenStates.getNumAct() + 1]; // One extra for event file
		
		mbtn_event = (Button) findViewById(R.id.EventButton);
		mbtn_event.setOnClickListener(this);

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
		
		//Register the sensors
		if (lfout[0]!=null) {
			for (int i=0;i<lSenStates.getNum();i++) {
				if (lSenStates.getActive(i))
					lSenMan.registerListener(this, lSenMan.getDefaultSensor(lSenStates.getType(i)), lSenStates.getRate(i));
			}
		}
	}

	private void open_files() throws FileNotFoundException{
		
		//Refs
		CSensorStates lSenStates=mSenStates;
		DataOutputStream[] lfout=fout;
		
				
		//Open the files and register the listeners
		if (lSenStates.getNumAct()>0) {
			for (int i=0;i<lSenStates.getNumAct();i++)
			{
				lfout[i]=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file_location("_" + lSenStates.getActName(i) + ".txt"))));
			}
			lfout[lSenStates.getNumAct()] = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file_location("_" + "KeyEvent.txt"))));
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
		CSensorStates lSenStates = mSenStates;
		
		for (int i=0;i<=lSenStates.getNumAct();i++) {
			if (lfout[i]!=null)
				try {
					lfout[i].close();
				} catch (IOException e) {
					mLV.addtext("File close error :" + i);
				}
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
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
//		long tim = ev.timestamp;
		String str = "";
		try {
//			file.writeInt(ev.sensor.getType());
//			file.writeLong(tim);
//			file.writeLong(ev.timestamp);
//			file.writeInt(len);
//			for (int i=0;i<len;i++)
//				file.writeFloat(ev.values[i]);
			str = str + ev.sensor.getType() + "\t" + 
					tim + "\t" + len + "\t";
			for (int i=0;i<len;i++)
				str = str + ev.values[i] + "\t";
			file.writeBytes(str + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View arg0) {
		CSensorStates lSenStates = mSenStates;
		DataOutputStream file=fout[lSenStates.getNumAct()];
		
		if (arg0.getId()==R.id.EventButton) { //Put an event marker
			if(evno == 0)
			{
				RefTime = System.currentTimeMillis();
				try {
					open_files();
					register_listeners();
					mLV.addtext("Started Logging");
				} catch (FileNotFoundException e) {
					mLV.addtext("File open error: Probably you do not have require permissions.");
					stop_recording();
				}
				
				file=fout[lSenStates.getNumAct()];
				
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

}
