context("getStartingPoint")

source("../remote/getStartingPoint.R", chdir = TRUE)
MIN_G <- 800

test_that("getStartingPoint finds correct starting point (simple)", {
  
  g <- c(rep(2*MIN_G, 999), rep(0, 200), rep(-2*MIN_G, 1000))
  start_at <- getStartingPoint(g, MIN_G)
  expect_that(start_at, equals(1050))
  
})

test_that("getStartingPoint finds correct starting point (reduced length)", {
  
  g <- c(rep(2*MIN_G, 999), rep(0, 55), rep(-2*MIN_G, 1000))
  start_at <- getStartingPoint(g, MIN_G)
  expect_that(start_at, equals(1025))
  
})

test_that("getStartingPoint finds no straight line, returns start at 500", {
  
  g <- c(rep(2*MIN_G, 2000))
  expect_that(start_at <- getStartingPoint(g, MIN_G), gives_warning("no straight line as starting point found!"))
  expect_that(start_at, equals(500))
  
  
})