readLocations <- function(timeStamp) {
  
  # Local variable
  ts = toString(timeStamp)
  
  # Column names for the data frames (j1, j2 etc are junk which will be discarded)
  locnames <- c('typ', 'time', 'j1', 'acy', 'alt', 'lat', 'long', 'brg', 'spd')
  
  # Read the tables
  loc <- read.table(paste(ts, "Location.txt", sep="_"), col.names = locnames, sep="")
  
  # clean up location data
  l <- loc[loc$acy <= 20, ]
  y <- data.frame(l$time, l$alt, l$lat, l$long)
  
  return(y)
}