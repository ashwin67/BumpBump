# Function to process the accelerometer values and find out locations where
# Acc-x clearly decreases and then increases
# sampLgth is 250 means that we use a 10 second moving window which compares
# current deviation with overall deviation.
processAcc <- function(rawDat, gpsWindow, sampLgth=500) {

  # Filter the X asis data
  # 2nd order butterworth filter
  bfX <- butter(3, 0.005)
  filtAx <- filter(bfX,rawDat)

  # Take a standard deviation of filtAx over a section where GPS speed has varied heavily
  AxNoiseVect <- filtAx[gpsWindow==TRUE]
  AxNoiseDev <- mad(AxNoiseVect)

  iAxDev <- vector(mode="numeric", length=length(filtAx))
  for(i in (sampLgth/2):(length(filtAx)-(sampLgth/2))) {
    # If we are outside the GPS window which says that speed is constant, then skip
    if(gpsWindow[i] == FALSE)
      next
    # instantaneous deviation within the window
    iAxDev[i] <- mad(filtAx[(i-(sampLgth/2)+1):(i+(sampLgth/2))])
    # If this is about the same as average, 
    if(iAxDev[i] <= AxNoiseDev){
      iAxDev[i] <- 0
    }
  }
  return(iAxDev>0)
}