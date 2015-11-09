SAMPLING_RATE <- 20
MIN_G_LOC <- 500
LOOK_BACK <- 50

initializeLocalization <- function(gyro_profile) {
  
  glob_gyro_profile <<- gyro_profile
  
  glob_track_length <<- nrow(glob_gyro_profile)
  glob_prob_pos <<- rep(1/glob_track_length, glob_track_length)
  
  glob_straight_prob <<- ifelse(abs(glob_gyro_profile$g) > MIN_G_LOC, 1, 1.5)
  glob_straight_prob <<- glob_straight_prob / sum(glob_straight_prob)
  glob_cc_shift <<- convolve(glob_gyro_profile$g, glob_gyro_profile$g)[1] / (10*mean(glob_gyro_profile$g)) * LOOK_BACK / glob_track_length
  
  ext_profile <- abs(c(glob_gyro_profile$g[(glob_track_length-LOOK_BACK):glob_track_length], glob_gyro_profile$g))
  profile_variance <- rep(mean(ext_profile), glob_track_length)
  for(i in 1:glob_track_length) {
    look_back_profile <- ext_profile[i:(i+LOOK_BACK)]
    if(any(abs(look_back_profile) > MIN_G_LOC)) {
      profile_variance[i] <- mean(look_back_profile)
    } 
  }
  glob_profile_variance <<- profile_variance
  
  glob_t0 <<- NULL
  
  glob_localization_aktiv <<- FALSE
  glob_scaled_t_hist <<- NULL
  glob_scaled_g_hist <<- NULL
  glob_sampled_scaled_t <<- NULL
  glob_sampled_scaled_g <<- NULL

  glob_last_force <<- NULL
  glob_speed_correction <<- 0
}