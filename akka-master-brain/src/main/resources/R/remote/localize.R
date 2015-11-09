library("ggplot2")
library("reshape2")
library("RJSONIO")

EPSILON_PROB <- 0.000001
LOCATION_BLUR <- 0.5
FORWARD_T_STEP <- 100

localize <- function(data, time) {
  tryCatch({
    sensor_data <- getGyroData(data, FALSE)
    
    if(!glob_localization_aktiv) {
      glob_localization_aktiv <<- fillSensorBuffer(sensor_data, time)
    } else {
      updateLocation(sensor_data, time)
    }
    return(list(location=list(data=glob_prob_pos), status=list(code='ok', message='ok')))
  }, error = function(e) {
    return(list(status=list(code='error', message=conditionMessage(e))))
  })
}

fillSensorBuffer <- function(sensor_data, time) {
  if(is.null(glob_t0)){  
    if(nrow(sensor_data) == 0) {  # Waiting for first call with sensor data
      return(FALSE)
    } else {                      # First call to localize with sensor data
      glob_t0 <<- sensor_data$t[1]
    }
  }
  
  t <- time-glob_t0
  global_last_call_time <<- t
  
  glob_scaled_t_hist <<- c(glob_scaled_t_hist, sensor_data$t - glob_t0)
  glob_scaled_g_hist <<- c(glob_scaled_g_hist, sensor_data$g)
  if(nrow(sensor_data) > 0) {
    glob_last_force <<- sensor_data$force[nrow(sensor_data)]
  }
    
  if(t >= LOOK_BACK*SAMPLING_RATE) {
    glob_sampled_force <<- rep(glob_last_force, LOOK_BACK+1)
    glob_last_real_t <<- glob_scaled_t_hist[length(glob_scaled_t_hist)]
	return(TRUE)
  }
  return(FALSE)
}

updateLocation <- function(sensor_data, time) {
  t <- time-glob_t0
  delta_t_call <<- t - global_last_call_time
  global_last_call_time <<- t
  
  # calculate current car speed to course speed ratio
  current_force_fact <- getForceFactor(glob_sampled_force)
  
  addScaledSensorData(sensor_data, current_force_fact)
  
  sampleSignal(t, current_force_fact)
  
  performLocalization(current_force_fact)
}

addScaledSensorData <- function(sensor_data, current_force_fact) {
  # scale and add new samples accordingly
  if(nrow(sensor_data)>0) {
    for(i in 1:nrow(sensor_data)) {
      if(i==1) {
        delta_t <- ((sensor_data$t[1] - glob_t0) - glob_last_real_t) * current_force_fact
      } else {
        delta_t <- (sensor_data$t[i] - sensor_data$t[i-1]) * current_force_fact
      }
      glob_scaled_t_hist <<- c(glob_scaled_t_hist, glob_scaled_t_hist[length(glob_scaled_t_hist)] + delta_t)
      glob_scaled_g_hist <<- c(glob_scaled_g_hist, sensor_data$g[i])
      glob_last_force <<- sensor_data$force[i] 
      glob_last_real_t <<- sensor_data$t[i] - glob_t0
    }
  }
}

sampleSignal <- function(t, current_force_fact) {
  # sample gyro signal at scaled t
  delta_real_t <- t - glob_last_real_t
  new_t <- glob_scaled_t_hist[length(glob_scaled_t_hist)] + (delta_real_t) * current_force_fact
  from_t <- new_t - LOOK_BACK * SAMPLING_RATE
  keep_inds <- which(glob_scaled_t_hist >= from_t)
  if(length(keep_inds)==0){
    keep_inds <- length(glob_scaled_t_hist) 
  }
  
  glob_scaled_t_hist <<- glob_scaled_t_hist[keep_inds]
  glob_scaled_g_hist <<- glob_scaled_g_hist[keep_inds]
  
  forward_t <- glob_scaled_t_hist[length(glob_scaled_t_hist)] + FORWARD_T_STEP
    
  # @todo: this should be local variables...
  glob_sampled_scaled_t <<- seq(from_t, new_t, SAMPLING_RATE)
  glob_sampled_scaled_g <<- approx(x=c(glob_scaled_t_hist, forward_t), y=c(glob_scaled_g_hist, 0), xout=glob_sampled_scaled_t, rule=2)[[2]]
  
  # sample force signal  
  glob_sampled_force <<- c(glob_sampled_force[-1], glob_last_force)
}

performLocalization <- function(current_force_fact){
  if(length(which(abs(glob_sampled_scaled_g) > MIN_G_LOC)) >= 1) {
    g_decay <- seq(length(glob_sampled_scaled_g)+1, 2*length(glob_sampled_scaled_g)) / (2*length(glob_sampled_scaled_g))
    g_filter <- c(rep(0,glob_track_length-LOOK_BACK-1), g_decay*glob_sampled_scaled_g)
    cross_cor <- convolve(glob_gyro_profile$g, g_filter) / glob_profile_variance
    cross_cor[cross_cor < 0] <- 0
    cross_cor <- log(cross_cor+glob_cc_shift)-log(glob_cc_shift*0.8)
    prob <- cross_cor / sum(cross_cor)
  } else {
    prob <- glob_straight_prob
  }
  
  delta_t_ratio <- delta_t_call / SAMPLING_RATE
  glob_speed_correction <<- glob_speed_correction + current_force_fact * delta_t_ratio
  
  speed_steps <- floor(glob_speed_correction)
  glob_speed_correction <<- glob_speed_correction - speed_steps
  if(speed_steps >= 1) {
    shifted_prob <- glob_prob_pos[c((glob_track_length-speed_steps+1):glob_track_length, 1:(glob_track_length-speed_steps))]    
  } else {
    shifted_prob <- glob_prob_pos
  }
  
  location_blur_filter <- c(dnorm(0:floor((glob_track_length-1)/2), sd = LOCATION_BLUR*delta_t_ratio),dnorm(floor((-glob_track_length+1)/2):-1, sd = LOCATION_BLUR*delta_t_ratio))
  blurred_prob <- convolve(shifted_prob, location_blur_filter)
  prob <- (blurred_prob + EPSILON_PROB) * prob
  glob_prob_pos <<- prob / sum(prob)
}

getForceFactor <- function(force_hist) {
  # todo: this needs to be replaced with some proper calculation
  nof_force_hist <- length(force_hist)
  acc_delay <- min(nof_force_hist-1, 3)
  force_factor <- mean(force_hist[(nof_force_hist-acc_delay):nof_force_hist]) / mean(glob_gyro_profile$force)
  return(force_factor)
}
