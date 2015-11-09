sampleGyroData <- function(data, sampling_rate, plot) {
  
  # Bound sensor data to remove drift effects
#  upper_bound <- mean(data$g[data$g>2000]) + sqrt(var(data$g[data$g>2000]))
#  lower_bound <- mean(data$g[data$g< -2000]) - sqrt(var(data$g[data$g< -2000]))
  upper_bound <- 1.05*median(data$g[data$g>2500])
  lower_bound <- 1.05*median(data$g[data$g< -2500])
  
  data$g[data$g > upper_bound] <- upper_bound
  data$g[data$g < lower_bound] <- lower_bound
  
  # Check for invalid regions (long period without signal samples)
  # Laps with invalid regions will be discarded from the analysis
  
  MAX_SAMPLE_LAG <- 150   # maximum allowed period of time (ms) in which no signal is sampled
  
  invalid_regions <- data.frame(from=numeric(), to=numeric())
  invalid_inds <- which(data$t[-1] - data$t[1:(length(data$t)-1)] > MAX_SAMPLE_LAG)
  for(i in invalid_inds) {
    reg <- data.frame(from=data$t[i],to=data$t[i+1])
    warning(paste("invalid region from", reg$from, "to", reg$to))
    invalid_regions <- rbind(invalid_regions, reg)
  }
  
  # Sample signal with constant sampling rate  
  sample_t <- seq(0, data$t[length(data$t)], sampling_rate)
  sample_g <- approx(x=data$t, y=data$g, xout=sample_t)[[2]]
  
  data <- data.frame(t=sample_t, g=sample_g)
  
  if(plot) {
    plot(ggplot(data, aes(x=t,y=g)) + geom_path())
  }
  
  return(list(data=data,invalid_regions=invalid_regions))
}