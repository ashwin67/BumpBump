#Read sensors and location values
s <- readSensors('23.18.10.50'); l <- readLocations('23.18.10.50');

bumps <- processEvents(s, madparam=19, midvalUse=TRUE)
bumpLocations <- processBumpLocation(bumps, l, s$m.time, tf=40)
par(mfrow=c(3,2), mar=c(3,2,2,2))
plot(s$m.time, s$m.ox, type='l', main="orientation along X")
plot(s$m.time, s$n.ax, type='l', main="acceleration along X")
plot(s$m.time, s$n.az, type='l', main="acceleration along Z")
plot(s$m.time, s$m.oz, type='l', main="orientation along Z")
plot(s$m.time, bumps, type='l', main="Probable Bumps")
plot(bumpLocations$l.lat, bumpLocations$l.long, main="Bump Locations")
