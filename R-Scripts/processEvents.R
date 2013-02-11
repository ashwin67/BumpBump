# Following are the main results of the analysis of accelerometer and orientation sensors:
# * Orientation along 'x' provides the direction of the car along the road
# * Orientation along 'y' provides the lateral variation of the car along the road (ideally NIL)
# * Orientation along 'z' provides the altitude variation of the car along the road
# * Acceleration along 'x' provides the acceleration/deceleration of the car along the road
# * Acceleration along 'y' provides the lateral acceleration/deceleration of the car (ideally NIL)
# * Acceleration along 'z' provides the vertical motion of the car (clear indicates road unevenness)
processEvents <- function (x, madparam=10, midval=median(x$n.az), midvalUse=TRUE) {
  # If direction (filtered to remove noise) stays same and the acceleration along z-axis has a large 
  # disturbance and acceleration along x-axis has decreased and then slowly increased, this indicates
  # a possible bad-road condition. We create a vector of this event  
  
  # Filter the direction data

  # Find standard deviation of acceleration along z-axis
  mdx <- vector(mode="numeric", length=length(x$m.time))
  if(midvalUse == TRUE) {
    for(i in 1:length(mdx)) {
      if(i < madparam)
        mdx[i] <- mad(x$n.az[1:i],center=midval)
      else
        mdx[i] <- mad(x$n.az[(i-madparam+1):i], center=midval)
    }
  }
  else
  {
    for(i in 1:length(mdx)) {
      if(i < madparam)
        mdx[i] <- mad(x$n.az[1:i])
      else
        mdx[i] <- mad(x$n.az[(i-madparam+1):i])
    }
  }
  
  # Compute velocity along x-axis
  
  # Create a bump-vector based on the above data
  bvec = vector(mode="logical", length=length(mdx))
  mad_mdx = mad(mdx)
  for(i in 2:length(mdx)) {
    if(abs(mdx[i]-mdx[i-1]) > mad_mdx) {
      bvec[i] = TRUE
    }
  }
  bvec[1] = FALSE
  
  # Return final vector
  return (bvec)
}