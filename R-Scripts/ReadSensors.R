readSensors <- function(timeStamp) {
  
  # Local variable
  ts = toString(timeStamp)
  
  # Column names for the data frames (j1, j2 etc are junk which will be discarded)
  orinames <- c('j1', 'time', 'j2', 'ox', 'oy', 'oz')
  accnames <- c('j1', 'time', 'j2', 'ax', 'ay', 'az')
  eventnames <- c('idx', 'time')
  
  # Read the tables
  ori <- read.table(paste(ts, "ORIENTATION.txt", sep="_"), col.names = orinames, sep="")
  acc <- read.table(paste(ts, "ACCELEROMETER.txt", sep="_"), col.names = accnames, sep="")
  evt <- read.table(paste(ts, "KeyEvent.txt", sep="_"), col.names = eventnames, sep="")
  
  # Merge the orientation and acceleration tables so that they are close in time
  m <- ori[(abs(ori$time-acc$time)<10),]
  n <- acc[(abs(ori$time-acc$time)<10),]
  x <- data.frame(I(m$time),m$ox,m$oy,m$oz,n$ax,n$ay,n$az)
  
#   # Finally, make the obtained data equally spaced in time - BUGGY
#   diffx <- vector(mode="numeric", length=length(x$m.time))
#   for(i in 2:length(diffx)) {
#     diffx[i] <- x$m.time[i] - x$m.time[i-1]
#   }
#   diffx[1] = diffx[2]
#   modX <- x[diffx<=mean(diffx), ]
#   
#   return(modX);
  return(x)
}