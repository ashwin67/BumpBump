readData <- function(timeStamp) {
  
  # Local variable
  ts = toString(timeStamp)
  
  # Column names for the data frames (j1, j2 etc are junk which will be discarded)
  orinames <- c('j1', 'time', 'j2', 'ox', 'oy', 'oz')
  accnames <- c('j1', 'time', 'j2', 'ax', 'ay', 'az')
  eventnames <- c('idx', 'time')
  locnames <- c('typ', 'time', 'j1', 'acy', 'alt', 'lat', 'long', 'brg', 'spd')
  
  # Read the tables
  ori <- read.table(paste(ts, "ORIENTATION.txt", sep="_"), col.names = orinames, sep="")
  acc <- read.table(paste(ts, "ACCELEROMETER.txt", sep="_"), col.names = accnames, sep="")
  evt <- read.table(paste(ts, "KeyEvent.txt", sep="_"), col.names = eventnames, sep="")
  loc <- read.table(paste(ts, "Location.txt", sep="_"), col.names = locnames, sep="")
  
  # clean up location data
  l <- loc[loc$acy <= 20, ]
  
  # Prepare the orientation and acceleration tables so that they are close in time
  m <- ori[(abs(ori$time-acc$time)<10),]
  n <- acc[(abs(ori$time-acc$time)<10),]
  
  # We need to merge GPS location and the above data
  # So, we create a vector which tells which GPS times we need to take
  g.alt <- vector(mode="numeric",length=length(m$time))
  g.lat <- vector(mode="numeric",length=length(m$time))
  g.lon <- vector(mode="numeric",length=length(m$time))
  g.brg <- vector(mode="numeric",length=length(m$time))
  g.spd <- vector(mode="numeric",length=length(m$time))
  
  j <- 1;
  for(i in 1:length(g.alt)) {

    if(j >= length(l$time))
      break
    
    while(l$time[j+1] <= l$time[j])
    {
      j <- j + 1
      if(j > length(l$time))
        break
    }
    if(j > length(l$time))
      break
    
    distJplus1 <- abs(l$time[j+1] - m$time[i])
    distJ <- abs(l$time[j] - m$time[i])
    
    if((distJplus1 < distJ)&&(distJplus1 <= 500)) {
      g.alt[i] <- l$alt[j+1]
      g.lat[i] <- l$lat[j+1]
      g.lon[i] <- l$long[j+1]
      g.brg[i] <- l$brg[j+1]
      g.spd[i] <- l$spd[j+1]
    }
    else if((distJplus1 < distJ)&&(distJplus1 > 500)) {
      g.alt[i] <- l$alt[j+1]
      g.lat[i] <- l$lat[j+1]
      g.lon[i] <- l$long[j+1]
      g.brg[i] <- l$brg[j+1]
      g.spd[i] <- l$spd[j+1]
      j <- j + 1
    }
    else if((distJ <= distJplus1)&&(distJ <= 500)) {
      g.alt[i] <- l$alt[j]
      g.lat[i] <- l$lat[j]
      g.lon[i] <- l$long[j]
      g.brg[i] <- l$brg[j]
      g.spd[i] <- l$spd[j]
    }
    else if((distJ <= distJplus1)&&(distJ > 500)) {
      g.alt[i] <- NA
      g.lat[i] <- NA
      g.lon[i] <- NA
      g.brg[i] <- NA
      g.spd[i] <- NA
    }
    
    if(j > length(l$time))
      break
  }

  x <- data.frame(I(m$time),m$ox,m$oy,m$oz,n$ax,n$ay,n$az,g.alt,g.lat,g.lon,g.brg,g.spd)
  
  x_notNA <- x[!is.na(x$g.alt), ]
  
  return(x_notNA)
}