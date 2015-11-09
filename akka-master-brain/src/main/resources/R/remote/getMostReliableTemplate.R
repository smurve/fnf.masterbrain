library("reshape2")

getMostReliableTemplate <- function(candidates, plot) {
  templates <- getTemplates(candidates$landmarks)
  template <- averageMostReliableTemplate(templates)
  if(plot) {
    plotTemplates(candidates, template)
  }
  return(template)
}

getTemplates <- function(landmarks) {
  templates <- list()
  i <- 1
  for(l in landmarks) {
    existing_temp <- unlist(lapply(templates, FUN=function(x){identical(x$dir, l$dir)}))
    if(any(existing_temp)) {
      template_ind <- which(existing_temp)
      templates[[template_ind]]$count <- templates[[template_ind]]$count + 1
      templates[[template_ind]]$t <- rbind(templates[[template_ind]]$t, l$t)
      templates[[template_ind]]$g <- rbind(templates[[template_ind]]$g, l$g)
      templates[[template_ind]]$roundIds <- c(templates[[template_ind]]$roundIds, i)
    } else {
      templates[[length(templates)+1]] <- c(l, list(count=1, roundIds=i))
    }
    i <- i+1
  }
  templates
}

averageMostReliableTemplate <- function(templates) {
  maxCount <- 0
  bestTemplate <- NULL
  for(t in templates) {
    if(maxCount < t$count) {
      maxCount <- t$count
      bestTemplate <- t
    }
  }
  
  if(maxCount == 1) {
    warning("No matching course rounds found. Instable results!")
    bestTemplate <- templates[[round((length(templates)+1.5)/2)]]
    mean_t <- bestTemplate$t
    extended_t <- c(0, bestTemplate$t)
    dt <- extended_t[-1] - extended_t[0:(length(extended_t)-1)]
    mean_g <- bestTemplate$g
  } else {
    mean_t <- colMeans(bestTemplate$t)
  
    # time weighted mean of g
    extended_t <- cbind(array(0,c(dim(bestTemplate$t)[1],1)), bestTemplate$t)
    delta_t <- extended_t[,-1] - extended_t[,0:(dim(extended_t)[2]-1)]
    dt <- colMeans(delta_t)
    mean_g <- apply(delta_t * bestTemplate$g, 2, sum) / apply(delta_t,2,sum)
  }
  
  return(list(t=mean_t, dt=dt, dir=bestTemplate$dir, g=mean_g, roundIds=bestTemplate$roundIds))
}

plotTemplates <- function(candidates, template) {
  reliablePlotdata <- candidates$plotdata[template$roundIds]
  roundLengths <- sapply(reliablePlotdata, nrow)
  maxNofRow <- max(roundLengths)
  longestRound <- which.max(roundLengths)
  plotDF <- data.frame(matrix(NA, maxNofRow, length(template$roundIds)+1))
  colnames(plotDF) <- c("time", paste("Round", template$roundIds))
  plotDF$time <- reliablePlotdata[[longestRound]]$t
  for(i in 1:length(template$roundIds)) {
    plotDF[1:roundLengths[i],i+1] <- reliablePlotdata[[i]]$g
  }
  meltDF <- melt(plotDF, id.vars = "time")
  suppressWarnings(plot(ggplot(meltDF, aes(x=time, y=value, col=variable)) + geom_line()))
}
