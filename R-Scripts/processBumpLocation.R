processBumpLocation <- function(bump, loc, tim, tf=200) {
  
  # Get a vector of all the times at which a bump occurred
  timeBumps <- as.vector(tim[bump == TRUE], "integer")
  
  # Parse through the location vector and set all locations that are bumps as TRUE
  bp <- vector(mode="logical", length(loc$l.time))
  tb <- vector(mode="integer", length(loc$l.time))
  lbp <- cbind(loc, bp, tb);
  k <- 1;
  for (i in 1:length(bp)) {
    for(j in k:(length(timeBumps)-1)) {
      if(abs(lbp$l.time[i]-timeBumps[j]) < tf) {
        lbp$bp[i] = TRUE;
        lbp$tb[i] = timeBumps[j];
        k <- j + 1;
        break;
      }
      else if((lbp$l.time[i]-timeBumps[j]) > tf) {
        j <- j + 1;
      }
      else if((timeBumps[j] - lbp$l.time[i]) > tf) {
        k <- j;
        break;
      }
    }
  }
  
  return(lbp[lbp$bp == TRUE,])
}