readData <- function(timeStamp) {
  
  # Local variable
  ts = toString(timeStamp)
  
  # Column names for the data frames (j1, j2 etc are junk which will be discarded)
  accnames <- c('j1', 'time', 'j2', 'ax', 'j3', 'az')
  locnames <- c('j0', 'time', 'j1', 'acy', 'j2', 'lat', 'long', 'j3', 'spd')
  
  # Read the tables
  m <- read.table(paste(ts, "ACCELEROMETER.txt", sep="_"), col.names = accnames, sep="")
  loc <- read.table(paste(ts, "Location.txt", sep="_"), col.names = locnames, sep="")
  
  # clean up location data
  l <- loc[loc$acy <= 20, ]
  
  # We need to merge GPS location and the above data
  # So, we create a vector which tells which GPS times we need to take
  g.lat <- vector(mode="numeric",length=length(m$time))
  g.lon <- vector(mode="numeric",length=length(m$time))
  g.spd <- vector(mode="numeric",length=length(m$time))
  
  j <- 1;
  for(i in 1:length(g.lat)) {
    # If all GPS values are exhausted, break
    if(j >= length(l$time))
      break
    
    # This condition should not happen but it is possible due to improper logging
    while(l$time[j+1] <= l$time[j])
    {
      j <- j + 1
      if(j > length(l$time))
        break
    }
    
    # If all GPS values are exhausted, break
    if(j >= length(l$time))
      break
    
    # Distance between current accelerometer time and next GPS time
    distJplus1 <- l$time[j+1] - m$time[i]
    # Distance between current accelerometer time and current GPS time
    distJ <- l$time[j] - m$time[i]
    
    # If acceleration time value is greater than the next time value of GPS, use those
    if(m$time[i] >= l$time[j+1]) {
      # Also, use these only if it is close by
      if(distJplus1 <= 1000) {
        g.lat[i] <- l$lat[j+1]
        g.lon[i] <- l$long[j+1]
        g.spd[i] <- l$spd[j+1]
      }
      else {
        g.lat[i] <- NA
        g.lon[i] <- NA
        g.spd[i] <- NA
      }
      # Increment this index so that "next" become "current" from next iteration
      j <- j + 1
    }
    # If acceleration time value is greater than current value and is not too far
    else if((m$time[i] >= l$time[j]) && (distJ <= 1000)) {
      g.lat[i] <- l$lat[j]
      g.lon[i] <- l$long[j]
      g.spd[i] <- l$spd[j]
    }
    # Following condition can happen during beginning of logging
    else if(m$time[i] < l$time[j]) {
      g.lat[i] <- NA
      g.lon[i] <- NA
      g.spd[i] <- NA
    }
    # Other unknown cases
    else {
      g.lat[i] <- NA
      g.lon[i] <- NA
      g.spd[i] <- NA
    }
    
    # If all values exhausted, exit
    if(j >= length(l$time))
      break
  }

  x <- data.frame(I(m$time),m$ax,m$az,g.lat,g.lon,g.spd)
  
  x_notNA <- x[!is.na(x$g.lat), ]
  x_notNZ <- x_notNA[x_notNA$g.lat != 0, ]
  
  return(x_notNZ)
}