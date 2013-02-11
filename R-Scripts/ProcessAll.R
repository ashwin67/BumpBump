# Plot everything about z
#z <- readSensors('08.18.06.45');  
#z <- readSensors('08.18.23.06');  
z <- readSensors('08.18.57.08');
bumps <- processEvents(z, madparam=20, midvalUse=TRUE)
par(mfrow=c(3,2), mar=c(3,2,2,2))
plot(z$m.time, z$m.ox, type='l', main="orientation along X")
plot(z$m.time, z$n.ax, type='l', main="acceleration along X")
plot(z$m.time, z$n.az, type='l', main="acceleration along Z")
plot(z$m.time, z$m.oz, type='l', main="orientation along Z")
plot(z$m.time, bumps, type='l', main="Probable Bumps")
#plot(x$m.time, type='l', main="time")
