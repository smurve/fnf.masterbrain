library("grid")

source("remote/getGyroData.R")
source("remote/sampleGyroData.R")
source("remote/initializeLocalization.R")
source("remote/localize.R")

initializeLocalization(track$gyro_profile)
gyro_profile <- track$gyro_profile

datafolder <- "../data/2015-09-03/"
locFiles <- list.files(datafolder)

jpeg("../mov/foo%04d.jpg", width = 400, height = 800)

for(l in locFiles) {

  real_t <- as.numeric(strsplit(strsplit(l, "_")[[1]][3], "\\.")[[1]][1])
  fileName <- paste(datafolder, l, sep="/")
  jsonInput <- readChar(fileName, file.info(fileName)$size)
  result <- localize(jsonInput, real_t)
  
  if(!is.null(glob_sampled_scaled_t)) {
    gyro_profile$prob <- result$location$data
    p_track <- ggplot(gyro_profile, aes(x=x, y=y)) + geom_point(aes(color=log(50*prob+1), size=log(50*prob+1)))
    p_track <- p_track + scale_colour_gradient(limits=c(0, 3)) + scale_size_continuous(limit=c(0,3), range = c(1, 8))
    p_track <- p_track + geom_point(data=subset(gyro_profile, prob==max(gyro_profile$prob)), color="red", size=5)
  
    plotDF <- data.frame(t=glob_sampled_scaled_t, g=glob_sampled_scaled_g, force=glob_sampled_force[1:length(glob_sampled_scaled_g)]*20)
    mDF <- melt(plotDF, id.vars = "t")
    p_gyro <- ggplot(mDF, aes(x=t,y=value,col=variable)) +
      scale_y_continuous(limits = c(-6000, 6000)) +
      geom_path()
  
    grid.newpage()
    pushViewport(viewport(layout = grid.layout(5, 1)))
    print(p_gyro, vp = viewport(layout.pos.row = 1, layout.pos.col = 1))
    print(p_track, vp = viewport(layout.pos.row = 2:5, layout.pos.col = 1))
  }
}
dev.off()

