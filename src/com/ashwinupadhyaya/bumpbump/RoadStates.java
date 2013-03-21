package com.ashwinupadhyaya.bumpbump;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;

import android.os.Environment;

public class RoadStates {

	private static float locAcc = (float)20.0;
	private static int gpsAccTimDiffMax = 1000;
	
	private int totalBumps = 0;
	
	// GPS variables
	private long currGpsTime = 0;
	private float currGpsVel = 0;
	private double currGpsLat = 0;
	private double currGpsLon = 0;
	private static float currGPSacc = 0;
	
	// Accelerometer variables
	private long currAccTime = 0;
	
	// Write to file
	private DataOutputStream fout = null;

	// Accelerometer double sliding window variables
	private class AccInternals {
		private float accMean;
		private int accMeanCount;
		private float accVar;
		private int accVarCount;
		private float diffVal;
		private int accSampLgth;
		
		private AccInternals(int sampLgth) {
			this.accSampLgth = sampLgth;
			this.accMean = 0;
			this.accMeanCount = 0;
			this.accVar = 0;
			this.accVarCount = 0;
		}
		
		private void UpdateMeanVar(float acc) {
			this.accMean = (this.accMean * this.accMeanCount + acc)/(this.accMeanCount+1);
			diffVal = acc - this.accMean;
			
			if(this.accVarCount >= 1) {
				this.accVar = (diffVal*diffVal + this.accVar*(this.accVarCount - 1)) / (this.accVarCount+1);
			}
			else {
				this.accVar = (diffVal*diffVal + this.accVar*this.accVarCount) / (this.accVarCount+1);
			}
			
			if(this.accMeanCount < this.accSampLgth) {
				this.accMeanCount++;
				this.accVarCount++;
			}
			
		}
		
		private void InitializeVars(float mVal, int mCnt, float vVal, int vCnt) {
			this.accMean = mVal;
			this.accMeanCount = mCnt;
			this.accVar = vVal;
			this.accVarCount = vCnt;
		}
		
		private float getSD() {
			return (float)(Math.sqrt((double)this.accVar));
		}
		
		private int getAccCount() {
			return this.accMeanCount;
		}
		
		private float getAccVal() {
			return this.accMean;
		}
		
		private int getVarCount() {
			return this.accVarCount;
		}
		
		private float getVarVal() {
			return this.accVar;
		}
	}
	
	private static AccInternals axS, axL, azS, azL;
	
	
	// GPS sliding window variables
	private static float gpsVelMeanL = 0;
	private static int gpsVelMeanCountL = 0;
	private static int gpsSampLgth = 100;
	private static int gpsLowSpeedSectionCount = 0;
	private static float gpsCutOff = (float) 0.75;
	private static float accCutOffX = (float) 1.1;
	private static float accCutOffZ = (float) 1.1;
	
	// Constructor for the class.
	// 1. lAcc = Location accuracy to be considered
	// 2. gpsAccDiff = Maximum time to consider for GPS data interpolation (to match with Acc data)
	public RoadStates(float lAcc, int gpsAccDiff, int gpsSampLength, float gpsCOff,
			float cOx, float cOz) {
		locAcc = lAcc;
		gpsAccTimDiffMax = gpsAccDiff;
		gpsSampLgth = gpsSampLength;
		gpsCutOff = gpsCOff;
		axS = new AccInternals(500);
		axL = new AccInternals(0x7FFFFFFF);
		azS = new AccInternals(300);
		azL = new AccInternals(0x7FFFFFFF);
		accCutOffX = cOx;
		accCutOffZ = cOz;
		
	}
	
	// Update the GPS information
	public void UpdateGPSInfo(float gpsAcc, long gpsTime, float gpsVel, double gpsLat, double gpsLon) {
		
		currGPSacc = gpsAcc;
		
		// Consider data only if the accuracy is good
		if((gpsAcc < locAcc) && (gpsTime > currGpsTime)){
			currGpsTime = gpsTime;
			currGpsVel = gpsVel;
			currGpsLat = gpsLat;
			currGpsLon = gpsLon;
			
		    // When we have detected a region of low speeds, we do not calculate the following
			if(gpsLowSpeedSectionCount == 0) {
				// This type of averaging will cause slow rise and quick fall
				gpsVelMeanL = (gpsVelMeanL*gpsVelMeanCountL + currGpsVel)/(gpsVelMeanCountL+1);
			    if(gpsVelMeanCountL <= gpsSampLgth) {
			    	gpsVelMeanCountL++;
			    }
			    
			    // If the current velocity is much below the mean velocity
			    if(currGpsVel < gpsCutOff*gpsVelMeanL) {
			    	gpsLowSpeedSectionCount = 12;
			    	gpsVelMeanCountL = 0;
			    }
			}
		    // Start closing this window if it exists
		    if(gpsLowSpeedSectionCount > 0) {
		    	gpsLowSpeedSectionCount--;
		    }
		}
	}

	// Update the Accelerometer information	
	public void UpdateAccelerometerInfo(long accTime, float accX, float accZ) {
		
		Boolean bumpDetected = false;
		// Consider the data only if there is a GPS data also close by. Else, discard.
		if(((accTime-currGpsTime) <= gpsAccTimDiffMax) && 
				(accTime >= currAccTime) && (accTime >= currGpsTime)) {
			currAccTime = accTime;
			
			// If a GPS low velocity window is active at the moment,
			if(gpsLowSpeedSectionCount > 0) {
				axS.UpdateMeanVar(accX);
				azS.UpdateMeanVar(accZ);
				// If both Z and X standard deviation are high, then this must be a bump
				if((azS.getSD() > accCutOffZ*azL.getSD()) && (axS.getSD() > accCutOffX*axL.getSD())) {
					// Bump Detected!!
					bumpDetected = true;
				}
			}
			// Following calculations will give an estimate of noise in the system (at normal speeds)
			else {
				axL.UpdateMeanVar(accX);
				azL.UpdateMeanVar(accZ);
				
				axS.InitializeVars(axL.getAccVal(), axL.getAccCount(), axL.getVarVal(), axL.getVarCount());
				azS.InitializeVars(azL.getAccVal(), azL.getAccCount(), azL.getVarVal(), azL.getVarCount());
			}
		}
		try {
			fout.writeBytes("" + currGPSacc + " " + accTime + " " + currGpsTime + " " + currAccTime + 
					" " + axL.getVarVal() + " " + azL.getVarVal() + " " + axL.getAccVal() +	
					" " + azL.getAccVal() + " " + currGpsLat + " " + currGpsLon + 
					" " + bumpDetected + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Get latitude and longitude of bump
	
	// Create file
	public void open_files(FileOutputStream fo) throws FileNotFoundException{
		fout = new DataOutputStream(new BufferedOutputStream(fo));
		try {
			fout.writeBytes("## Bump Latitude and Longitude\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close_files() {

		try {
			fout.close();
		} catch (IOException e) {
		}
	}
}
