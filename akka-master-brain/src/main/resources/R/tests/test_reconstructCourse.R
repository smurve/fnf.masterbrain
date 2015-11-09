context("reconstructCourse")

source("../remote/reconstructCourse.R", chdir = TRUE)

test_that("getCurveSegment works correctly", {
  current_point <- c(2,1)
  current_dir <- c(0,1)
  ang <- 90
  
  curveSegment <- getCurveSegment(current_point, current_dir, ang)
  
  expect_that(curveSegment$current_point, equals(c(1,2)))
  expect_that(curveSegment$current_dir, equals(c(-1,0)))
  expect_that(curveSegment$arc$c_x, equals(1))
  expect_that(curveSegment$arc$c_y, equals(1))
  expect_that(curveSegment$arc$ang_start, equals(0))
  expect_that(curveSegment$arc$ang_end, equals(pi/2))
})