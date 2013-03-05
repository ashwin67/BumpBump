# Function to process the data frame so that only regions where GPS speed has decreased drastically
# are retained. Each sample covers a region about 20 ms. So, sample Length of 1500 covers a region of
# about 30 seconds. However, this value is better to be derived based on data.
processGPSSpeed <- function(rawDat, sampLgth=1500, cutOff=0.5) {

  # 1. Take the mean of the velocities over a period of sampLgth
  # meanSpeed <- vector(mode="numeric", length=length(rawDat$g.spd))
  lowSpeed <- vector(mode="logical", length=length(rawDat$g.spd))
  for(i in (sampLgth/2):(length(rawDat$g.spd)-(sampLgth/2))) {
    meanSpeed <- mean(rawDat$g.spd[(i-(sampLgth/2)+1):(i+(sampLgth/2))])
    # 2. Check if raw Velocity is much lower than the mean velocity
    while(rawDat$g.spd[i] < cutOff*meanSpeed) {
      # Create a window instead of the low velocity values
      j <- i
      if(i > (length(rawDat$g.spd)-(sampLgth/2)))
        break;
      lowSpeed[(j+1-sampLgth/2):(j+sampLgth/2)] <- TRUE
      i <- i + sampLgth/2
    }
  }
  
  return(lowSpeed)
}