library("plotrix")

RADIUS <- 1

reconstructCourse <- function(template, plot) {
  FUDGE_FACTORS <- c(1,1.05,1.1,0.95,1.15)
  CURVE_ELEMENT_ANG <- 360 / 8
  
  # reconstruct curve angles
  g_per_sector <- template$dt * template$g
  ang_per_g <- -360 / sum(g_per_sector)
  ang_per_sector <- g_per_sector * ang_per_g
  small_curve_correction <- 15*(2.2-log10(abs(ang_per_sector))) * sign(ang_per_sector)
  small_curve_correction[abs(ang_per_sector) < 10] <- 0
  for(f in FUDGE_FACTORS) {
    rounded_ang_per_sector <- round(f * (ang_per_sector + small_curve_correction) / CURVE_ELEMENT_ANG) * CURVE_ELEMENT_ANG
    if(abs(sum(rounded_ang_per_sector))==360) {
      break
    }
    if(f == FUDGE_FACTORS[length(FUDGE_FACTORS)]) {
      warning("No appropriate fudge factor found to build 360 degree track")
      rounded_ang_per_sector <- round((ang_per_sector + small_curve_correction) / CURVE_ELEMENT_ANG) * CURVE_ELEMENT_ANG
    }
  }
  
  nof_segments <- length(rounded_ang_per_sector)
  corDist <- data.frame(x=numeric(nof_segments), y=numeric(nof_segments))
  sectors <- buildSectors(template, rounded_ang_per_sector, corDist)
  corDist <- getCorFactors(sectors)
  if(is.null(corDist)) {
    course <- list(sectors=sectors, status="instable")
  } else {
    sectors <- buildSectors(template, rounded_ang_per_sector, corDist)
    course <- list(sectors=sectors, status="ok")
  }
  if(plot) {
    plotCourse(sectors$straights, sectors$arcs)
  }
  
  return(course)
}

getCurveSegment <- function(current_point, current_dir, ang_deg) {
  ang_rad <- ang_deg * pi / 180
  rotToCenter <- rotMat(pi/2 * sign(ang_rad))
  rotCurve <- rotMat(ang_rad)
  center <- current_point + rotToCenter %*% current_dir
  center_to_current <- (current_point-center)
  ang_start <- atan2(center_to_current[2], center_to_current[1])
  arc <- data.frame(c_x=center[1], c_y=center[2], ang_start=ang_start, ang_end=ang_start + ang_rad)
  current_point <- center + rotCurve %*% center_to_current
  current_dir <- rotCurve %*% current_dir
  return(list(current_point=as.vector(current_point), current_dir=as.vector(current_dir), arc=arc))
}

rotMat <- function(ang) {
  matrix(c(cos(ang), sin(ang), -sin(ang), cos(ang)), 2, 2)
}

buildSectors <- function(template, rounded_ang_per_sector, corDist) {
  STRAIGHT_SPEED_FAC <- 1.6
  STRAIGHT_ACC <- 1.1
  
  # estimate velocity
  mean_ang_per_ms <- mean(abs(rounded_ang_per_sector / template$dt))
  dist_per_ms <- STRAIGHT_SPEED_FAC * RADIUS * mean_ang_per_ms * pi / 180
  
  curve_sec <- which(rounded_ang_per_sector != 0)
  nof_sectors <- length(template$dt)
  current_point <- c(0,0)
  current_dir <- c(0,1)
  arcs <- data.frame(c_x=numeric(), c_y=numeric(), ang_start = numeric(), ang_sec = numeric(), sector_ind=integer())
  straights <- data.frame(x_start=numeric(),y_start=numeric(), x_end=numeric(), y_end=numeric(), x_dir=numeric(), y_dir=numeric(), sector_ind=integer())
  
  for(s in 1:nof_sectors) {
    if(s %in% curve_sec) {
      curveSegment <- getCurveSegment(current_point, current_dir, rounded_ang_per_sector[s])
      curveSegment$arc$sector_ind <- s
      current_point <- curveSegment$current_point
      current_dir <- curveSegment$current_dir
      arcs <- rbind(arcs, curveSegment$arc)
    } else {
      straight_dist <- (dist_per_ms * template$dt[s]) * STRAIGHT_ACC ^ sqrt(template$dt[s]/100)
      new_point <- current_point + current_dir * straight_dist + c(corDist$x[s], corDist$y[s])
      straights <- rbind(straights, data.frame(x_start=current_point[1],y_start=current_point[2],x_end=new_point[1],y_end=new_point[2], x_dir=current_dir[1], y_dir=current_dir[2],sector_ind=s))
      current_point <- new_point
    }
  }
  return(list(straights=straights, arcs=arcs))
}

getCorFactors <- function(sectors) {
  straights <- sectors$straights
  delta_pos <- straights[nrow(straights), c("x_end", "y_end")]
  
  if(max(abs(delta_pos)) > 3 && min(abs(delta_pos)) > 1.5) {
    return(NULL)
  }
  
  x_straights <- which(approxEqual(abs(straights$x_dir), 1))
  y_straights <- which(approxEqual(abs(straights$y_dir), 1))
  x_length <- straights$x_end - straights$x_start
  y_length <- straights$y_end - straights$y_start
  tot_x_length <- sum(abs(x_length[x_straights]))
  tot_y_length <- sum(abs(y_length[y_straights]))
  
  nof_segments <- nrow(straights) + nrow(sectors$arcs)
  corDist <- data.frame(x=numeric(nof_segments), y=numeric(nof_segments))
  for(s in x_straights) {
    corDist$x[straights$sector_ind[s]] <- -abs(x_length[s]) / tot_x_length * delta_pos$x_end
  }
  for(s in y_straights) {
    corDist$y[straights$sector_ind[s]] <- -abs(y_length[s]) / tot_y_length * delta_pos$y_end
  }
  
  return(corDist)
}

approxEqual <- function(x,y,epsilon=1e-4) {
  return(abs(x-y) < epsilon)
}

plotCourse <- function(straights, arcs) {
  x_min <- min(c(straights$x_start, straights$x_end))-RADIUS-0.5
  x_max <- max(c(straights$x_start, straights$x_end))+RADIUS+0.5
  y_min <- min(c(straights$y_start, straights$y_end))-RADIUS-0.5
  y_max <- max(c(straights$y_start, straights$y_end))+RADIUS+0.5
  min_val <- min(x_min, y_min)
  max_val <- max(x_max, y_max)
  plot(x=NULL,y=NULL,xlim=c(min_val-0.2,max_val+0.2), ylim=c(min_val-0.2,max_val+0.2), asp=1)
  rect(x_min,y_min,x_max,y_max)
  
  draw.arc(x=arcs$c_x, y=arcs$c_y, radius=RADIUS, angle1=arcs$ang_start, angle2=arcs$ang_end)
  segments(x0=straights$x_start, y0=straights$y_start, x1=straights$x_end, y1=straights$y_end)
}