context("getMaxCorLag")

source("../remote/getMaxCorLag.R", chdir = TRUE)

test_that("getMaxCorLag correct auto-correlation lag", {
  
  g <- rnorm(500)
  g <- c(g,g)
  lag <- getMaxCorLag(g, FALSE)
  expect_that(lag, equals(500))
  
})
