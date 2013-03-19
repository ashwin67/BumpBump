# Function to process the data frame so that only regions where GPS speed has decreased drastically
# are retained. Each sample covers a region about 20 ms. So, sample Length of 1500 covers a region of
# about 30 seconds. However, this value is better to be derived based on data.
processGPSSpeed <- function(rawDat, sampLgth=1500, cutOff=0.75) {

  # 1. Take the mean of the velocities over a period of sampLgth
  # meanSpeed <- vector(mode="numeric", length=length(rawDat$g.spd))
  lowSpeed <- vector(mode="logical", length=length(rawDat$g.spd))
  spdCount <- 1
  meanSpeed <- 0
  for(i in 1:length(rawDat$g.spd)) {
    # This type of averaging will cause slow rise and quick fall
    meanSpeed <- (meanSpeed*(spdCount-1) + rawDat$g.spd[i])/spdCount
    if(spdCount < sampLgth) {
      spdCount <- spdCount + 1
    }
    
    #meanSpeed <- mean(rawDat$g.spd[(i-sampLgth+1):i])
    # 2. Check if raw Velocity is much lower than the mean velocity
    while(rawDat$g.spd[i] < cutOff*meanSpeed) {
      # Create a window instead of the low velocity values
      if(i > (length(rawDat$g.spd)-sampLgth/2))
        break
      lowSpeed[i:(i+sampLgth/2)] <- TRUE
      i <- i + sampLgth/2
      spdCount <- 1
    }
    if(i > length(rawDat$g.spd))
      break
  }
  
  return(lowSpeed)
}