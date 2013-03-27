# degrees to radians
latR1 <- lat1 * pi / 180.0
latR2 <- lat2 * pi / 180.0
lonR1 <- lon1 * pi / 180.0
lonR2 <- lon2 * pi / 180.0

idx <- vector('numeric', length(latR2))

lsdPoints <- c(100000:(100000-1+length(latR2)))
lat3 <- vector('numeric', length(latR2))
lon3 <- vector('numeric', length(latR2))

# For all values, find the closest match between the two
for(i in 1:length(latR2)) {
  lon3[i] <- NA
  lat3[i] <- NA
  for(j in 1:length(latR1)) {
    tempDiffLat <- (latR2[i]-latR1[j])
    tempDiffLon <- (lonR2[i]-lonR1[j]) #*cos((latR1[j] + latR2[i])/2)
    # lsd between the points
    #lsd <- 6371*sqrt(tempDiffLat*tempDiffLat + tempDiffLon*tempDiffLon)
    a <- sin(tempDiffLat/2)*sin(tempDiffLat/2) + sin(tempDiffLon/2) * sin(tempDiffLon/2) * cos(latR1[j]) * cos(latR2[i])
    c <- 2 * atan2(sqrt(a), sqrt(1-a))
    lsd <- 6371 * c
    if(lsd < lsdPoints[i]) {
      if(lsd <= 0.25) {
        lat3[i] <- lat1[j]
        lon3[i] <- lon1[j]
      }
      lsdPoints[i] <- lsd
      idx[i] <- j
    }
  }
  if(lsdPoints[i] > 0.25) {
    lsdPoints[i] <- NA
    idx[i] <- NA
  }
}

# Plot the values on a map
par(mfrow=c(1,2), mar=c(3,2,2,2))
plot(lon2, lat2, main='Lat Lon 2')
plot(lon3[!is.na(lon3)], lat3[!is.na(lat3)], main='Lat Lon 3')

rm(i); rm(j); rm(tempDiffLat); rm(tempDiffLon); rm(a); rm(c);
rm(lsd); rm(latR1); rm(latR2); rm(lonR1); rm(lonR2); 