(function () {

    'use strict';

    angular.module('myApp.trackPlot', [])
        .directive('trackPlot', ['$window', function ($window) {

            function calcParallel(d, offset) {
                var x1 = d.x_start[0];
                var x2 = d.x_end[0];
                var y1 = -d.y_start[0];
                var y2 = -d.y_end[0];

                var L = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

                // This is the second line
                var x1p = x1 + offset * (y2 - y1) / L;
                var x2p = x2 + offset * (y2 - y1) / L;
                var y1p = y1 + offset * (x1 - x2) / L;
                var y2p = y2 + offset * (x1 - x2) / L;
                return [x1p, y1p, x2p, y2p];
            }

            function getPath(d, config, trackWidth) {
                var line1 = calcParallel(d, trackWidth);
                var line2 = calcParallel(d, -trackWidth);
                var p = "M" + config.x(line1[0]) + "," + config.y(line1[1])
                    + "L" + config.x(line1[2]) + "," + config.y(line1[3])
                    + "L" + config.x(line2[2]) + "," + config.y(line2[3])
                    + "L" + config.x(line2[0]) + "," + config.y(line2[1])
                    + "z";
                return p;
            }

            function link(scope, element, attr) {
                var el = element[0];
                var defaultConfig = {
                    size: 500,
                    margin: {
                        top: 100, right: 5, bottom: 5, left: -50
                    },
                    zoom: true,
                    zoomRange: [1, 100],
                    trackWidth: 0.3,
                    carSize: 8,
                    x: d3.scale.linear().domain([-10, 15]).range([0, 1000]),
                    y: d3.scale.linear().domain([-10, 15]).range([0, 1000]),
                    curvesGenerator: d3.svg.arc()
                        .innerRadius(function () {
                            return config.x(config.x.domain()[0] + 1 + config.trackWidth);
                        })
                        .outerRadius(function () {
                            return config.x(config.x.domain()[0] + 1 - config.trackWidth);
                        })
                        .startAngle(function (d) {
                            return d.ang_start[0] + (90 * (Math.PI / 180));
                        })
                        .endAngle(function (d) {
                            return d.ang_end[0] + (90 * (Math.PI / 180));
                        }),
                    straightsGenerator: function (d) {
                        return getPath(d, config, config.trackWidth);
                    },
                    curveTranslator: function (d) {
                        return "translate(" + config.x(d.c_x[0]) + "," + config.y(-d.c_y[0]) + ") scale(1,-1)"
                    },
                    duration: 200,
                    curbGenerator: d3.svg.arc()
                        .innerRadius(function () {
                            return config.x(config.x.domain()[0] + 1 + config.trackWidth);
                        })
                        .outerRadius(function () {
                            return config.x(config.x.domain()[0] + 1 + config.trackWidth * 1.6);
                        })
                    ,
                    curbPies: d3.layout.pie()
                        .value(function (d) {
                            return d.value;
                        })
                        .startAngle(function (d) {
                            return Number(d[0].ang_start) + (90 * (Math.PI / 180));
                        })
                        .endAngle(function (d) {
                            return Number(d[0].ang_end) + (90 * (Math.PI / 180));
                        }),
                    curbTranslator: function (d) {
                        return "translate(" + config.x(d.data.c_x[0]) + "," + config.y(-d.data.c_y[0]) + ") scale(1, -1)"
                    },
                    locationSize: d3.scale.linear().domain([0,0.003,0.01,0.03,0.1,1]).range([1,3,5,7.5,10,15]),
                    //locationOpacity: d3.scale.linear().domain([0, 1]).range([0.6, 1]),
                    powerColor: d3.scale.linear().domain([0, 300 / 10 * 5, 300 / 10 * 7, 300]).range(['green', 'yellow', 'orange', 'red'])
                };
                // Create the real config by merging passed in config with default config.
                var config = defaultConfig;
                if (scope.config !== undefined) {
                    config = _.merge(defaultConfig, scope.config);
                }
                config.x.range([0, config.size]);
                config.y.range([0, config.size]);

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
                        'height': config.size + 'px'
                    });
                var root = svg.append("g")
                    .attr("transform",
                    "translate(" + config.margin.left + "," + config.margin.top + ")");
                scope.render = function () {
                    root.selectAll('*').remove();
                    //var width = d3.select(el)[0][0].offsetWidth - config.margin.left - config.margin.right;
                    var height = 200 - config.margin.top - config.margin.bottom;
                    //width = Math.max(width, 200);
                    height = Math.max(height, 200);
                    config.size = height;
                    var data = {
                        segments: {
                            straights: [],
                            curves: []
                        }
                    };
                    if (scope.data !== undefined) {
                        data = _.merge(data, scope.data);
                    }

                    root.selectAll('.dataLine').remove();

                    if (data.segments.straights !== undefined) {
                        var s = root.selectAll('path.trackStraight');
                        s.data(data.segments.straights)
                            .enter().append('path')
                            .attr('class', 'trackStraight')
                            .attr('d', config.straightsGenerator);
                    }
                    if (data.segments.curves !== undefined) {
                        root.selectAll('path.trackCurve')
                            .data(data.segments.curves)
                            .enter()
                            .append("path")
                            .attr('class', 'trackCurve')
                            .attr("d", config.curvesGenerator)
                            .attr("transform", config.curveTranslator);

                        data.segments.curves.forEach(function (d) {
                            var angle = Math.abs(d.ang_end - d.ang_start);
                            var curbAngle = 20 * Math.PI / 180;
                            var curbs = Math.round(angle / curbAngle);
                            var values = [];
                            for (var i = 0; i < curbs; i++) {
                                values.push({
                                    value: 1,
                                    ang_start: d.ang_start,
                                    ang_end: d.ang_end,
                                    c_x: d.c_x,
                                    c_y: d.c_y
                                });
                            }
                            var id = Math.round(Math.random() * 1000000);
                            root.selectAll('#curb' + id)
                                .data(config.curbPies(values))
                                .enter()
                                .append("path")
                                .attr('class', 'curb')
                                .attr('id', 'curb' + id)
                                .attr("d", config.curbGenerator)
                                .style('fill', function (d, i) {
                                    return (i % 2 === 1) ? 'red' : 'lightgray';
                                })
                                .attr("transform", config.curbTranslator)
                        });
                    }



                    renderPowerProfile(root, data);
                    renderProbabilityBasedLocation(root, data);
                    renderCurrentLocationMarker(root, data);

                };

                function renderCurrentLocationMarker(root, data) {
                    if ((data.segments.curves !== undefined) && (data.segments.straights !== undefined) && (data.currentLocation !== undefined)) {
                        if (root.selectAll('circle.currentLocation')[0].length === 0) {
                            root.append('circle').attr('class', 'currentLocation')
                                .attr('fill', 'red');
                        }
                        root.selectAll('circle.currentLocation')
                            .attr('class', 'currentLocation')
                            .call(renderCurrentLocation);
                    }
                }

                var renderProbabilityBasedLocation = function (root, data) {
                    if ((data.segments.curves !== undefined) && (data.segments.straights !== undefined) && (data.locations !== undefined)) {
                        var circles = root.selectAll('circle.locationProb').data(data.locations);
                        circles
                            .enter().append('circle')
                            .attr('class', 'locationProb')
                            .attr('fill', 'purple')
                            .call(renderLocationProbability);
                        circles
                            .exit()
                            .transition()
                            .duration(20)
                            .remove();
                    }
                };

                var renderPowerProfile = function (root, data) {
                    if ((data.segments.curves !== undefined) && (data.segments.straights !== undefined) && (data.powerProfile !== undefined)) {
                        var circles = root.selectAll('circle.powerProfile').data(data.powerProfile);
                        circles
                            .enter().append('circle')
                            .attr('class', 'powerProfile')
                            .call(renderLocationPower);
                        circles
                            .exit()
                            .transition()
                            .duration(20)
                            .remove();
                    }
                };

                var renderLocationPower = function (s) {
                    s.attr('r', function (d) {
                        return zoom.scale() * 2
                    })
                        .attr('fill', function (d) {
                            return config.powerColor(d.f)
                        })
                        .attr('cx', function (d) {
                            return config.x(d.x)
                        })
                        .attr('cy', function (d) {
                            return config.y(-d.y)
                        });
                };

                function renderLocationProbability(s) {
                    s.attr('r', function (d) {
                        return zoom.scale() * config.locationSize(d.p);
                    })
                        .attr('cx', function (d) {
                            return config.x(d.x)
                        })
                        .attr('cy', function (d) {
                            return config.y(-d.y)
                        })
                        //.attr('opacity', function (d) {
                        //    return config.locationOpacity(d.p)
                        //})
                        .attr('fill-opacity', function (d) {
                            return 0.5;//config.locationOpacity(d.p)
                        })
                        ;
                }

                function renderCurrentLocation(s) {
                    s.attr('r', function (d) {
                        return zoom.scale() * config.carSize;
                    })
                        .attr('cx', function() { return config.x(scope.data.currentLocation[0]);})
                        .attr('cy', function() { return config.y(-1*scope.data.currentLocation[1]);})
                    ;
                }

                // reapply transformations when zoomed or padded
                function zoomed() {
                    root.selectAll('path.trackStraight')
                        .attr("d", config.straightsGenerator);

                    root.selectAll('path.trackCurve')
                        .attr("d", config.curvesGenerator)
                        .attr("transform", config.curveTranslator);

                    root.selectAll('path.curb')
                        .attr("d", config.curbGenerator)
                        .attr("transform", config.curbTranslator);

                    root.selectAll('circle.currentLocation')
                        .call(renderCurrentLocation);

                    root.selectAll('circle.locationProb')
                        .call(renderLocationProbability);

                    root.selectAll('circle.powerProfile')
                        .call(renderLocationPower);
                }


                // Force complete redraw if window size changes.
                scope.$watch(function () {
                    return angular.element($window)[0].innerWidth;
                }, function () {
                    scope.render();
                });
                // Force complete redraw if data changes.
                scope.$watch('data', function () {
                    scope.render();
                }, true);
            }

            return {
                link: link,
                restrict: 'E',
                scope: {
                    config: '=',
                    data: '='
                }
            }
        }]);
})();