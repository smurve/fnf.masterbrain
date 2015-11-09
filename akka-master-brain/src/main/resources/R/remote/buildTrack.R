library("ggplot2")
library("plyr")
library("gridExtra")

#source("getMaxCorLag.R")
#source("getStartingPoint.R")#source("getGyroData.R")
#source("sampleGyroData.R")
#source("getLapCandidates.R")
#source("getMostReliableTemplate.R")
#source("reconstructCourse.R")
#source("resampleLaps.R")
#source("mapProfileToCourse.R")


SAMPLING_RATE <- 20
PLOT <- FALSE

# datafile<-"../data/first_layout/racedata.json"
# datafile<-"../data/second_layout/hollywood_160.json"

buildTrack <- function(data){
  tryCatch({
    gyro_data <- getGyroData(data, TRUE)
    sampled_data <- sampleGyroData(gyro_data, SAMPLING_RATE, PLOT)
    
    candidates <- getLapCandidates(sampled_data, PLOT)
    template <- getMostReliableTemplate(candidates, PLOT)
    course <- reconstructCourse(template, PLOT)
    gyro_profile <- resampleLaps(gyro_data, candidates, template, SAMPLING_RATE, PLOT)
    track <- mapProfileToCourse(course$sectors, gyro_profile)
    initializeLocalization(gyro_profile)
    if(course$status=="ok") {
      track$status <- list(code='ok', message='ok')
    } else {
      track$status <- list(code='warning', message='instable')
    }
    return(track)
  }, error = function(e) {
    return(list(status=list(code='error', message=conditionMessage(e))))
  })
    
    
}

