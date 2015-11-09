
getLapCandidates <- function(sampled_data, plot) {
  
  MIN_G <- 1000      # minimum magnitude of gyro sensor to be considered a curve
  NUM_VALS <- 1500  # number of sensor values used for lap detection (x20 to get seconds)
  data <- sampled_data$data
  start_at <- getStartingPoint(data$g, MIN_G)
  sampling_rate <- data$t[2] - data$t[1]
  
  plotdata <- NULL
  landmarks <- list()
  rounds <- list()
  
  i <- 1
  while(start_at <= length(data$t)-NUM_VALS) {
    end_at<-start_at + NUM_VALS
    t0<-data$t[start_at]
    t_axis<-data$t[c(start_at:end_at)] - t0
    g_axis<-data$g[c(start_at:end_at)]
    maxCorLag <- getMaxCorLag(g_axis, plot)
    end_at <- which(data$t >= t0 + maxCorLag * sampling_rate)[1] - 1
    if(isValidRegion(data$t[c(start_at,end_at)], sampled_data$invalid_regions)) {
      if(plot) {
        plotdata[[i]] <- data.frame(t=data$t[start_at:end_at]-t0 ,g=data$g[start_at:end_at])
      }
      landmarks[[i]] <- getLandmarks(data$t[start_at:end_at] - t0, data$g[start_at:end_at], MIN_G)    
      rounds[[i]] <- list(start_ind=start_at, start_t=data$t[start_at], end_ind=end_at, end_t=data$t[end_at])
      i <- i+1
    }
    start_at <- end_at + 1
  }  
  
  if(length(landmarks) == 0){
    stop("No valid lap detected. Keep driving...")  
  }
  
  return(list(landmarks=landmarks, rounds=rounds, plotdata=plotdata))
}

getLandmarks <- function(x,y,min_g) {
  direction <- ifelse(y < -min_g, -1, ifelse(y > min_g, 1, 0))
  change_dir <- direction[-1] - direction[1:length(direction)-1]
  
  mod_x <- x
  mod_change_dir <- change_dir
  mod_y <- y
  added_points <- 0
  for(i in 1:length(change_dir)) {
    if(abs(change_dir[i]) == 2) {
      mod_change_dir[i+added_points] <- change_dir[i] / 2
      mod_x <- c(mod_x[1:(i+added_points)], (mod_x[i+added_points] + mod_x[i+1+added_points])/2, mod_x[(i+1+added_points):length(mod_x)])
      mod_change_dir <- c(mod_change_dir[1:(i+added_points)], change_dir[i]/2, mod_change_dir[(i+1+added_points):length(mod_change_dir)])
      mod_y <- c(mod_y[1:(i+added_points)], 0, mod_y[(i+1+added_points):length(mod_y)])
      added_points <- added_points+1      
    }
  }
  
  change_inds <- which(abs(mod_change_dir)>0)
  
  mean_g <- mean(mod_y[1:change_inds[1]])
  i <- 1
  while(i <= length(change_inds)-1) {
    mean_g[i+1] <- mean(mod_y[(change_inds[i]+1):change_inds[i+1]])
    i <- i + 1
  }
  mean_g[length(change_inds)+1] <- mean(mod_y[(change_inds[length(change_inds)]+1):length(mod_y)])
  
  return(list(t=mod_x[c(change_inds,length(mod_x))], dir=mod_change_dir[change_inds], g=mean_g)) 
}

isValidRegion <- function(interval, invalid_regions) {
  
  if(nrow(invalid_regions) == 0) return(TRUE)
  
  for(reg in 1:nrow(invalid_regions)){
    if(interval[1] < invalid_regions$to[reg] && interval[2] > invalid_regions$from[reg]) {
      return(FALSE)
    }
  }
  return(TRUE)
}