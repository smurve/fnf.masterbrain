(function () {
    'use strict';

    angular.module('myApp.sensorPlot', [])
        .directive('sensorPlot', SensorPlot);

    SensorPlot.$inject = ['$window'];

    function SensorPlot($window) {
        var link = function (scope, element, attr) {
            var el = element[0];
            var defaultConfig = {
                margin: {
                    top: 10, right: 5, bottom: 30, left: 55
                },
                zoom: true,
                dataXScale: 1,
                dataYScale: 1,
                zoomRange: [1, 10],
                xAxis: {orientation: 'bottom', ticks: 5, dynamic: true},
                yAxis: {orientation: 'left', ticks: 5, dynamic: true},
                yExtractor: function (d) {
                    return d.g * config.dataYScale
                },
                xExtractor: function (d) {
                    return d.t > 1435000000000 /*looks like a timestamp since 1970 */ ? d.t : d.t * config.dataXScale
                },
                lineGenerator: d3.svg.line()
                    .x(function (d) {
                        return config.x(config.xExtractor(d));
                    })
                    .y(function (d) {
                        return config.y(config.yExtractor(d));
                    })
                    .interpolate('linear'),

                lapRectGenerator: function (d) {
                    var w = config.x(d.end_t * config.dataXScale) - config.x(d.start_t * config.dataXScale);
                    return "M" + config.x(d.start_t * config.dataXScale) + "," + 0
                        + "h" + w
                        + "v" + 400
                        + "h" + -w
                        + "z";
                },
                //x: d3.scale.linear().domain([0,110]).range([0, 100]),
                x: d3.time.scale().range([0, 100]),
                y: d3.scale.linear().domain([-10000, 10000]).range([100, 0])

            };
            // Create the real config by merging passed in config with default config.
            var config = defaultConfig;
            if (scope.config !== undefined) {
                config = _.merge(defaultConfig, scope.config);
            }

            // Define the axes
            var xAxis = d3.svg.axis().scale(config.x)
                .orient(config.xAxis.orientation).ticks(config.xAxis.ticks)
                .tickFormat(d3.time.format('%M:%S'));

            var yAxis = d3.svg.axis().scale(config.y)
                .orient(config.yAxis.orientation).ticks(config.yAxis.ticks);

            var zoom = d3.behavior.zoom()
                .x(config.x)
                .y(config.y)
                .scaleExtent(config.zoomRange)
                .on("zoom", zoomed);
            if (!config.zoom) {
                // null object that does nothing
                zoom = function () {
                };
                zoom.x = function () {
                };
                zoom.y = function () {
                };
            }

            var svg = d3.select(el).append("svg")
                .call(zoom)
                .style({
                    'width': '100%',
                    'height': '200px'
                })
                .append("g")
                .attr("transform",
                "translate(" + config.margin.left + "," + config.margin.top + ")");


            svg.append("g").attr("class", "x axis");
            svg.append("g").attr("class", "y axis");

            var clipId = 'clip-' + Math.random() * 100000;
            var clip = svg.append("clipPath")
                .attr("id", clipId)
                .append("rect");

            function render() {
                var data = {
                    lineData: [],
                    lapData: []
                }
                if (scope.data !== undefined) {
                    data = _.merge(data, scope.data);
                }
                var measures = data.lineData;

                var laps = data.lapData;

                var width = d3.select(el)[0][0].offsetWidth - config.margin.left - config.margin.right;
                var height = 200 - config.margin.top - config.margin.bottom;
                width = Math.max(width, 200);
                height = Math.max(height, 100);

                var updateDomain = function (data, mirror, domainFun, extractFun) {
                    var minFun = function (d) {
                        return extractFun(d)
                    };
                    var maxFun = function (d) {
                        return extractFun(d)
                    };
                    var min = d3.min(data, minFun);
                    min = (min === undefined) ? 0 : min;
                    var max = d3.max(data, maxFun);
                    max = (max === undefined) ? 0 : max;
                    if (mirror) {
                        if (min < 0) {
                            max = Math.max(Math.abs(min), max);
                            min = -max;
                        }
                    }
                    domainFun([min, max]);
                }

                if (config.xAxis.dynamic) {
                    updateDomain(measures, false, config.x.domain, config.xExtractor);
                }
                if (config.yAxis.dynamic) {
                    updateDomain(measures, true, config.y.domain, config.yExtractor);
                }

                // update x/y ranges and domain to the new element size.
                // Don't forget to update every object that has a reference to x and y!
                config.x.range([0, width]);
                config.y.range([height, 0]);


                // update the axis. Change number of ticks if needed
                var ticks = Math.max(3, Math.floor(width / 100));
                xAxis.ticks(ticks);
                xAxis.scale(config.x);
                yAxis.scale(config.y);

                // as we've updated x and y we need to update zoom
                zoom.x(config.x);
                zoom.y(config.y);

                var selectionColorGenerator = d3.scale.linear()
                    .domain([0, laps.length])
                    .range([0.1, 0.6]);

                // Render detected lap sections
                svg.selectAll('.lapMatch').data(laps)
                    .attr('d', config.lapRectGenerator)
                    .enter()
                    .append('path')
                    .attr('class', 'lapMatch')
                    .attr("clip-path", "url(#" + clipId + ")")
                    .style("opacity", 0)
                    .attr('d', config.lapRectGenerator)
                    .transition()
                    .style("opacity", function (d, i) {
                        return selectionColorGenerator(i)
                    })
                    .duration(2000);

                // Render the data line
                svg.selectAll('.dataLine').remove();
                svg.append("path").datum(measures)
                    .attr("class", "line dataLine")
                    .attr("clip-path", "url(#" + clipId + ")")
                    .attr("d", config.lineGenerator);

                // Update the X Axis
                svg.selectAll('.x.axis')
                    .attr("transform", "translate(0," + height + ")")
                    .call(xAxis);

                // Update the Y Axis
                svg.selectAll('.y.axis')
                    .call(yAxis);

                // Clipping used for zoom and pan
                clip
                    .attr("width", width)
                    .attr("height", height);
            }

            //************************************************************
            // Zoom specific updates
            //************************************************************
            function zoomed() {
                var duration = 200;
                svg.select(".x.axis").transition().call(xAxis).duration(duration);
                svg.select(".y.axis").transition().call(yAxis).duration(duration);
                svg.selectAll('path.line').attr('d', config.lineGenerator);
                svg.selectAll(".lapMatch").attr('d', config.lapRectGenerator);
            }


            // Force complete redraw if window size changes.
            scope.$watch(function () {
                return angular.element($window)[0].innerWidth;
            }, function () {
                render();
            });

            scope.$watch('data', function () {
                render();
            }, true);


        };

        return {
            link: link,
            restrict: 'E',
            scope: {
                config: '=',
                data: '='
            }
        }
    }
})();