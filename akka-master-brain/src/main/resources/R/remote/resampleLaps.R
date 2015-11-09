resampleLaps <- function(data, candidates, template, sampling_rate, plot) {
  MIN_CROSS_COR <- 0.93
  
  roundIds <- template$roundIds
  lap_times <- sapply(candidates$rounds[roundIds], function(x) x$end_t - x$start_t)
  lap_length <- median(lap_times)
  proto_lap <- which(lap_length == lap_times)
  lap_t <- data.frame(t=seq(0,lap_length,sampling_rate))  
  lap_data <- list()
  selectedRounds <- list()
  
  for(r in roundIds) {
    current_round <- candidates$rounds[[r]]
    lap_inds <- which(data$t > current_round$start_t & data$t < current_round$end_t)
    sample_g <- approx(x=data$t[lap_inds]-current_round$start_t, y=data$g[lap_inds], xout=lap_t$t, rule=2)[[2]]
    sample_force <- approx(x=data$t[lap_inds]-current_round$start_t, y=data$force[lap_inds], xout=lap_t$t, rule=2)[[2]]
    
    added <- FALSE
    i <- 1
    for(ld in lap_data) {
      cross_cor <- ccf(ld$sample_g[,1], sample_g, lag.max = 0, plot=FALSE)$acf[1]
      if(cross_cor >= MIN_CROSS_COR){
        lap_data[[i]]$sample_g <- cbind(ld$sample_g, sample_g)
        lap_data[[i]]$sample_force <- cbind(ld$sample_force, sample_force)
        selectedRounds[[i]] <- c(selectedRounds[[i]], r)
        added <- TRUE
        break
      }
      i <- i+1
    }
    if(!added) {
      lap_data[[length(lap_data)+1]] <- list(lap_t=lap_t, sample_g=data.frame(sample_g), sample_force=data.frame(sample_force))  
      selectedRounds[[length(selectedRounds)+1]] <- r
    }    
  }
  
  reliable_ind <- which.max(sapply(lap_data, function(x) ncol(x$lap_t)))
  
  if(ncol(lap_data[[reliable_ind]]$sample_g) >= 2) {
    result <- data.frame(t=lap_t, g=rowMeans(lap_data[[reliable_ind]]$sample_g), force=rowMeans(lap_data[[reliable_ind]]$sample_force))
  } else {
    result <- data.frame(t=lap_t, g=lap_data[[reliable_ind]]$sample_g$sample_g, force=lap_data[[reliable_ind]]$sample_force$sample_force)
  }
  
  if(plot) {
    plot(ggplot(result, aes(x=t,y=g)) + geom_path())
  }
  
  return(result)
}