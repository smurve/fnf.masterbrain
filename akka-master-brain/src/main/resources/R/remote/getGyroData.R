library("RJSONIO")

getGyroData <- function(datafile, timeshift, asText = TRUE) {

  jsondata <- fromJSON(content = datafile, asText = asText)

  if(length(jsondata) == 0){
    return(data.frame(t=numeric(0), g=numeric(0), force=numeric(0)))
  }
  
  full_t_axis <- sapply(jsondata$sensorEvents, function(x) as.numeric(x$t[[1]]))
  
  if(timeshift) {
	  t0 <- full_t_axis[1]
	  full_t_axis <- full_t_axis - t0
  }
  
  full_g_axis <- sapply(jsondata$sensorEvents, function(x) x$g[[1]])
  full_force_axis <- sapply(jsondata$sensorEvents, function(x) x$force[[1]])  
  
  return(data.frame(t=full_t_axis, g=full_g_axis, force=full_force_axis))
}
