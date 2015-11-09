mapProfileToCourse <- function(course, gyro_profile){
  MIN_G <- 1000
  
  seg_type <- ifelse(gyro_profile$g < -MIN_G, -1, ifelse(gyro_profile$g > MIN_G, 1, 0))
  remaining_seg <- seg_type
  seg_pair_ind <- 1
  x_pos <- NULL
  y_pos <- NULL
  ind <- NULL
  while(length(remaining_seg) > 0) {
    current_type <- remaining_seg[1]
    seg_end <- which(remaining_seg != current_type)[1] - 1
    if(is.na(seg_end)) {
      seg_end <- length(remaining_seg)
    }
    if(current_type == 0) { # straight
      x_positions_in_straight <- approx(c(0, seg_end), c(course$straights$x_start[seg_pair_ind], course$straights$x_end[seg_pair_ind]), c(1:seg_end))
      x_pos <- c(x_pos, x_positions_in_straight$y)
      y_pos <- c(y_pos, approx(c(0, seg_end), c(course$straights$y_start[seg_pair_ind], course$straights$y_end[seg_pair_ind]), c(1:seg_end))$y)
      ind <- c(ind, arrayOfN(length(x_positions_in_straight$y),seg_pair_ind))
    } else { # curve
      if(seg_pair_ind > nrow(course$arcs)) {
        stop("Couldn't map gyro profile to reconstructed course.")
      }
      angs <- approx(c(0, seg_end), c(course$arcs$ang_start[seg_pair_ind], course$arcs$ang_end[seg_pair_ind]), c(1:seg_end))$y
      x_pos <- c(x_pos, cos(angs) + course$arcs$c_x[seg_pair_ind])
      y_pos <- c(y_pos, sin(angs) + course$arcs$c_y[seg_pair_ind])
      seg_pair_ind <- seg_pair_ind + 1
      ind <- c(ind, arrayOfN(length(angs),seg_pair_ind))
    }
    if(seg_end == length(remaining_seg)) {
      remaining_seg <- c() 
    } else {
      remaining_seg <- remaining_seg[(seg_end+1):length(remaining_seg)]
    }
  }

  gyro_profile$x <- x_pos
  gyro_profile$y <- y_pos
  gyro_profile$ind <- ind
    
  return(list(course=course, gyro_profile=gyro_profile))
}

arrayOfN <- function(n, content){
    return(rep(content, n))
}