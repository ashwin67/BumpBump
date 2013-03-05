# Read sensors and location values
rawData <- readData('01.19.29.28');

# Process the GPS data and discard cases where velocity is clearly high enough for
# a speed bump to be non-existent. This brings down the number of values that we would have to check
gpsSpeedfltData <- processGPSSpeed(rawData, sampLgth=40000/median(diff(rawData$m.time)), cutOff=0.75)

# Process acceleration data and find out regions where:
# 1. Acc-x clearly decreases and then increases
accFltDataX <- processAcc(rawData$n.ax, gpsWindow=gpsSpeedfltData, sampLgth=500)
# 2. Acc-z has a large standard deviation
accFltDataZ <- processAcc(rawData$n.az, gpsWindow=gpsSpeedfltData, sampLgth=300)
# 3. Process all values together to arrive at final value
accFinal <- combineAllSensors(accFltDataX, accFltDataZ, rawData, maxTimeMerge=3000)

# Plot the values on a map
par(mfrow=c(1,1), mar=c(3,2,2,2))
plot(rawData$g.lat, rawData$g.lon, type='l', col='blue')
points(rawData$g.lat[accFinal==TRUE], rawData$g.lon[accFinal==TRUE], col='red', pch=18)

#Plot Labels
text(12.849983003152731, 77.59079661631549, 'nice road', col='black', pos=4, cex=0.7)
text(12.902565728838395, 77.46963546678155, 'kengeri Jn', col='black', pos=2, cex=0.7)
text(12.79650676855705,  77.3836745312368, 'bidadi', col='black', pos=4, cex=0.7)
text(12.722814406819062, 77.275389093116, 'ramanagara', col='black', pos=4, cex=0.7)
text(12.652771384935015, 77.20310051483659, 'chennapattana', col='black', pos=4, cex=0.7)
text(12.587295341881337, 77.0426357613674, 'maddur', col='black', pos=4, cex=0.7)
text(12.527664432415808, 76.89442630208757, 'mandya', col='black', pos=4, cex=0.7)
text(12.416736826107627, 76.6865427434141, 'srirangapattana', col='black', pos=4, cex=0.7)
text(12.350410903584846, 76.66100958843643, 'mysore', col='black', pos=1, cex=0.7)

# Plot the Derived values
sx <- 0*length(rawData$m.time) + 1
ex <- 1*length(rawData$m.time)
par(mfrow=c(4,1), mar=c(3,2,2,2))
plot(rawData$m.time[sx:ex], rawData$g.spd[sx:ex], type='l', main="Velocity")
plot(rawData$m.time[sx:ex], accFltDataX[sx:ex], type='l', main="Deviation of Acceleration along X was high")
plot(rawData$m.time[sx:ex], accFltDataZ[sx:ex], type='l', main="Deviation of Acceleration along Z was high")
plot(rawData$m.time[sx:ex], accFinal[sx:ex], type='l', main="Possible bumps")