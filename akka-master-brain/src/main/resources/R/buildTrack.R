library("ggplot2")
library("plyr")
library("gridExtra")

source("remote/getGyroData.R")
source("remote/sampleGyroData.R")
source("remote/getLapCandidates.R")
source("remote/getStartingPoint.R")
source("remote/getMaxCorLag.R")
source("remote/getMostReliableTemplate.R")
source("remote/reconstructCourse.R")
source("remote/resampleLaps.R")
source("remote/mapProfileToCourse.R")
source("remote/initializeLocalization.R")

SAMPLING_RATE <- 20
PLOT <- TRUE

datafile<-"../data/hollywood-real-1.json"

gyro_data <- getGyroData(datafile, TRUE, FALSE)
sampled_data <- sampleGyroData(gyro_data, SAMPLING_RATE, PLOT)

candidates <- getLapCandidates(sampled_data, PLOT)
template <- getMostReliableTemplate(candidates, PLOT)
course <- reconstructCourse(template, PLOT)
gyro_profile <- resampleLaps(gyro_data, candidates, template, SAMPLING_RATE, PLOT)
track <- mapProfileToCourse(course$sectors, gyro_profile)
initializeLocalization(gyro_profile)
