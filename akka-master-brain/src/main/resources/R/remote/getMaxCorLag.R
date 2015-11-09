getMaxCorLag <- function(y, plot) {
  MIN_CYCLE <- 200
  autoCor <- acf(y, lag.max=1100, plot=plot)
  lags <- autoCor$lag[MIN_CYCLE:length(autoCor$lag)]
  lags[which.max(autoCor$acf[MIN_CYCLE:length(autoCor$acf)])]
}