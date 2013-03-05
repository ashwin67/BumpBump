combineAllSensors <- function(accX, accY, rawDat, maxTimeMerge=3000) {
  accFinal <- accX * accY
  
  timeDiff <- diff(rawDat$m.time)
  count <- 0     # Holds the total counts since passing a TRUE state
  timeSum <- 0   # Holds the total time since passing a TRUE state
  trueState <- 0 # Becomes 1 when we cross a value which is TRUE
  for(i in 2:length(accFinal)) {
    if((accFinal[i] == FALSE) && (trueState == 1)) {
      count <- count + 1
      timeSum <- timeSum + timeDiff[i-1]
      if(timeSum > maxTimeMerge) {
        trueState <- 0
      }
    }
    else if((accFinal[i] == TRUE) && (trueState == 1)) {
      if(timeSum < maxTimeMerge) {
        #insert TRUE values in between
        for(j in (i-count):i) {
          accFinal[j] <- TRUE
        }
      }
      trueState <- 1
      count <- 0
      timeSum <- 0
    }
    else if((accFinal[i] == TRUE) && (trueState == 0)) {
      trueState <- 1
      count <- 0
      timeSum <- 0
    }
  }
  return(accFinal)
}