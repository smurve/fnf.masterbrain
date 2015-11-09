getStartingPoint <- function(y, min_g, min_straight = 100) {
  if(min_straight == 0) warning("no straight line as starting point found!")
  start_at <- 200
  MAX_START_AT <- 2000
  MIN_STRAIGHT_REDUCTION <- 10
  num_straight <- 0
  while(start_at < MAX_START_AT && num_straight < min_straight) {
    if(abs(y[start_at]) < min_g){
      num_straight <- num_straight + 1
    } else {
      num_straight <- 0
    }
    start_at <- start_at + 1
    if(start_at > length(y)) {
      stop("No starting point found yet. Keep driving...")
    }
  }
  if(start_at == MAX_START_AT) {
    return(getStartingPoint(y, min_g, min_straight - MIN_STRAIGHT_REDUCTION))
  } else {
    return(start_at - round(min_straight/2))
  }
}