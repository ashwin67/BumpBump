package com.ashwinupadhyaya.bumpbump;

import android.os.Parcel;
import android.os.Parcelable;

public class RoadStates implements Parcelable {

	private static int locAcc = 20;
	private static int gpsAccTimDiffMax = 1000;
	
	private int totalBumps = 0;
	private int[] bumpLat = null;
	private int[] bumpLon = null;
	private int[] resultTime = null;
	
	// GPS variables
	private long currGpsTime = 0;
	private float currGpsVel = 0;
	private float currGpsLat = 0;
	private float currGpsLon = 0;
	
	// Accelerometer variables
	private long currAccTime = 0;

	// Accelerometer double sliding window variables
	private class AccInternals {
		private float accMean;
		private int accMeanCount;
		private float accVar;
		private int accVarCount;
		private float diffVal;
		private int accSampLgth;
		
		private AccInternals(int sampLgth) {
			accSampLgth = sampLgth;
		}
		
		private void UpdateMeanVar(float acc) {
			accMean = (accMean * accMeanCount + acc)/(accMeanCount + 1);
			diffVal = acc - accMean;
			
			if(accVarCount >= 2) {
				accVar = (diffVal*diffVal + accVar*(accVarCount - 2)) / accVarCount;
			}
			else {
				accVar = (diffVal*diffVal + accVar*(accVarCount - 1)) / accVarCount;
			}
			
			if(accMeanCount < accSampLgth) {
				accMeanCount++;
				accVarCount++;
			}
		}
		
		private void InitializeVars(float mVal, int mCnt, float vVal, int vCnt) {
			accMean = mVal;
			accMeanCount = mCnt;
			accVar = vVal;
			accVarCount = vCnt;
		}
		
		private float getSD() {
			return (float)(Math.sqrt((double)accVar));
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
	public RoadStates(int lAcc, int gpsAccDiff, int gpsSampLength, float gpsCOff,
			float cOx, float cOz) {
		locAcc = lAcc;
		gpsAccTimDiffMax = gpsAccDiff;
		gpsSampLgth = gpsSampLength;
		gpsCutOff = gpsCOff;
		axS = new AccInternals(500);
		axL = new AccInternals(0xFFFFFFFF);
		azS = new AccInternals(300);
		azL = new AccInternals(0xFFFFFFFF);
		accCutOffX = cOx;
		accCutOffZ = cOz;
	}
	
	// Update the GPS information
	public void UpdateGPSInfo(int gpsAcc, long gpsTime, float gpsVel, float gpsLat, float gpsLon) {
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
				}
			}
			// Following calculations will give an estimate of noise in the system (at normal speeds)
			else {
				axL.UpdateMeanVar(accX);
				azL.UpdateMeanVar(accZ);
				
				axS.InitializeVars(axL.accMean, axL.accMeanCount, axL.accVar, axL.accVarCount);
				azS.InitializeVars(azL.accMean, azL.accMeanCount, azL.accVar, azL.accVarCount);
			}
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}

}
