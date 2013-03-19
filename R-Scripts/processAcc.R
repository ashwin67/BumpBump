# Function to process the accelerometer values and find out locations where
# Acc-x clearly decreases and then increases
# sampLgth is 250 means that we use a 10 second moving window which compares
# current deviation with overall deviation.
processAcc <- function(rawDat, gpsWindow, sampLgth=500, cutOff=1.5) {

  # Filter the X asis data
  # 2nd order butterworth filter
  #bfX <- butter(3, 0.005)
  #filtAx <- filter(bfX,rawDat)

  iAxDev <- vector(mode="logical", length=length(rawDat))
  #iAxDev <- vector(mode="numeric", length=length(rawDat))
  
  meanCntL <- 1
  meanValL <- 0
  varValL <- 0
  varCntL <- 1
  meanCntS <- 1
  meanValS <- 0
  varValS <- 0
  varCntS <- 1
  
  # Using a slightly flawed method of standard deviation
  for(i in 1:length(rawDat)) {
    if(gpsWindow[i] == TRUE) {
      # Reset all variables for the FALSE case
#       meanValL <- 0
#       meanCntL <- 1
#       varValL <- 0
#       varCntL <- 1
      # All processing here for the period when GPS indicates drop in velocity
      # This type of averaging will cause slow rise and quick fall
      meanValS <- (meanValS*(meanCntS-1) + rawDat[i])/meanCntS
      diffValS <- rawDat[i] - meanValS
      if(varCntS >= 2)
        varValS <- (diffValS*diffValS + varValS*(varCntS-2))/varCntS
      else
        varValS <- (diffValS*diffValS + varValS*(varCntS-1))/varCntS
      
      if(meanCntS < sampLgth) {
        meanCntS <- meanCntS + 1
        varCntS <- varCntS + 1
      }
      if(sqrt(varValS) > cutOff*sqrt(varValL)) {
        iAxDev[i] <- 1
      }
      #iAxDev[i] <- sqrt(varValS)
    }
    else {
      # All processing here
      # This type of averaging will cause slow rise and quick fall
      meanValL <- (meanValL*(meanCntL-1) + rawDat[i])/meanCntL
      diffValL <- rawDat[i] - meanValL
      if(varCntL >= 2)
        varValL <- (diffValL*diffValL + varValL*(varCntL-2))/varCntL
      else
        varValL <- (diffValL*diffValL + varValL*(varCntL-1))/varCntL
      meanCntL <- meanCntL + 1
      varCntL <- varCntL + 1
      #iAxDev[i] <- sqrt(varValL)
      
      # Reset all variables for the TRUE case
      meanValS <- meanValL
      meanCntS <- sampLgth
      varValS <- varValL
      varCntS <- sampLgth
    }
  }
  
  return(iAxDev)
}