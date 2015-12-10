function executeReport(dataAll) {

	var $ = AJS.$;

	var tooltipRenderer = function (key, x, y, e) {
        if (e.point.missing) {
            return '<h3>' + key + '</h3><p>Missing value for this point.</p>';
        }

        var commits_html = _.map(e.point.commits, function(commit){
            return commit.hash + ' @' + commit.commiter;
        }).join("<br>");

        var date_formatted = d3.time.format('%Y/%m/%d %H:%M')(new Date(e.point.date * 1000));
        return '<h3>' + key + '</h3>' +
				'<p class="tooltip-value">' + y + '</p>'
				+ '<p><span class="tooltip-detail-label">build</span>' + e.point.build + '<br>'
                + '<span class="tooltip-detail-label">date</span>' + date_formatted + '<br>'
				+ '<span class="tooltip-detail-label">commits</span>' + commits_html + '</p>'
	};

	var configureChart = function (chart, chartSelector, dataForChart) {
        var xLabels = {};
        // Let's collect labes. Note that some points may be 'missing' and we need to ignore them,
        // as they don't have the build_number set.
        // All metrics will have the same length of values.
        for (var x = 0; x < dataForChart[0].values.length; x++) {
            for (var m = 0; m < dataForChart.length; m++) {
                var valueInPoint = dataForChart[m].values[x];
                if (!valueInPoint.missing) {
                    // Leave only number from build number key
                    xLabels[x] = valueInPoint.build.replace(/.*-/, '');
                    break;
                }
            }
        }

		chart.tooltipContent(tooltipRenderer)
				.x(function (d) {
					return d.x
				})
				.y(function (d) {
					return d.y
				}).clipEdge(true);

		chart.xAxis
                .showMaxMin(false)
                .tickFormat(function(x) {
                    return xLabels[x];
                })
                .axisLabel('Build Number');

		chart.yAxis.tickFormat(d3.format('d'));

		d3.select(chartSelector)
				.datum(dataForChart)
				.transition().duration(500).call(chart);

		nv.utils.windowResize(chart.update);
		return chart;
	};

	var addChart = function (chartSelector, dataForChart, chartModel) {
        var chart = chartModel();
        nv.addGraph(function () {
            return configureChart(chart, chartSelector, dataForChart);
		});
        return chart;
	};

	var chartsFactory = {
		createAreaChart: function (chartSelector, dataForChart) {
			addChart(chartSelector, dataForChart, nv.models.stackedAreaChart);
		},
		createBarChart: function (chartSelector, dataForChart) {
			addChart(chartSelector, dataForChart, nv.models.multiBarChart);
		},
		createLineChart:function (chartSelector, dataForChart) {
			addChart(chartSelector, dataForChart, nv.models.lineChart);
		},
		createLineWithFocusChart:function (chartSelector, dataForChart) {
			addChart(chartSelector, dataForChart, nv.models.lineWithFocusChart);
		}
	};

	var transformDataForChart = function (rawData, metrics) {
		var data = {};
		var chosenMetrics = metrics || null;
        var pointPos = 0;
		_.each(rawData.points, function (p) {
			_.each(p.metrics, function (metric, metricId) {
				if (chosenMetrics == null || _.contains(chosenMetrics, metricId)) {
					var destMetric = data[metricId] || (data[metricId] = {
						values: []
					});
                    destMetric.key = metric.description;
                    //noinspection JSUnresolvedVariable
                    destMetric.values[pointPos] = {
                        x: pointPos,
                        y: metric.value,
                        date: p.date,
                        build: p.build_number,
                        commits: p.commits
                    };
				}
			});
            pointPos++;
		});

        // nvd3 doesn't like missing points, so we need to add some fake points
        var numPoints = pointPos;
        _.each(data, function(metric){
            for (var i = 0; i < numPoints; i++) {
                if (!(i in metric.values)) {
                    // missing value
                    metric.values[i] = {
                        x: i,
                        y: -1,
                        date: 0,
                        commits: [],
                        missing: true
                    };
                }
            }
        });
        return _.toArray(data);
	};


    var largeMetrics = findAllMetrics(dataAll, function (size) {return size >= 500000 },[]);
    var mediumMetrics = findAllMetrics(dataAll, function (size) {return size < 500000 && size >= 400},largeMetrics);
    var smallMetrics = findAllMetrics(dataAll, function (size) {return size < 400 && size >= 50},[].concat(mediumMetrics).concat(largeMetrics));
    var verySmallMetrics = findAllMetrics(dataAll, function (size) {return size < 50 && size > 0},[].concat(smallMetrics).concat(mediumMetrics).concat(largeMetrics));
    var metricGroups = [
		{ name: 'Files size', metrics: largeMetrics },
		{ name: 'Medium', metrics: mediumMetrics },
		{ name: 'Small', metrics: smallMetrics },
		{ name: 'Very Small', metrics: verySmallMetrics },
	];

	var projections = [
		{ name: 'Bar', func: chartsFactory.createBarChart },
		{ name: 'Area', func: chartsFactory.createAreaChart },
		{ name: 'Line', func: chartsFactory.createLineChart },
		{ name: 'Line(Focus)', func: chartsFactory.createLineWithFocusChart }
	];

	// -------- Multi charts
	var drawMultiCharts = function (makeChartFunction) {
		var $chartsMulti = $('#chartsMulti');
		$chartsMulti.empty();
		var chartNum = 0;
		_.each(metricGroups, function (group) {
			// add node to DOM
			var chartId = 'chartsMulti_' + (++chartNum);
			$chartsMulti.append('<div id="' + chartId + '"><h2>Group: ' + group.name + '</h2><svg></svg></div>');

			makeChartFunction('#' + chartId + ' svg', transformDataForChart(dataAll, group.metrics));
		});
	};
	drawMultiCharts(chartsFactory.createAreaChart);
	var $chartsMultiActions = $('#chartsMultiActions').addClass("aui-buttons");

	var getChangeChartDisplayClickFun = function (drawFunction, createChartFunction, $actions) {
		return function (e) {
			drawFunction(createChartFunction);
			$actions.find('a').removeClass('active');
			$(this).addClass('active');
			e.stopPropagation();
			return false;
		}
	};
	_.each(projections, function (projection) {
		var button = $(aui.buttons.button({text: projection.name, tagName: "a"}));
		if (projection.name == 'Area') {
			button.addClass('active');
		}
		$(this).addClass('active');
		$chartsMultiActions.append(button.click(getChangeChartDisplayClickFun(drawMultiCharts, projection.func, $chartsMultiActions)));
	});

	// -------- Separate chars
	var drawSeparateCharts = function (makeChartFunction) {
		var $chartsSeparate = $('#chartsSeparate');
		$chartsSeparate.empty();
		var chartNum = 0;

       _.each(findAllMetrics(dataAll,function(){return true},{}), function (metric) {
			// add node to DOM
			var chartId = 'chartsSeparate_' + (++chartNum);
			$chartsSeparate.append('<div id="' + chartId + '"><h2>Metric: ' + metric + '</h2><svg></svg></div>');

			makeChartFunction('#' + chartId + ' svg', transformDataForChart(dataAll, [metric]));
		});
	};

    function findAllMetrics(dataAll,sizeFilter,doNotAdd)
    {
        var doNotAddMetrics = {};
        _.each(doNotAdd, function (metric) {doNotAddMetrics[metric] = true});
        var metrics_names = {};
        _.each(dataAll.points, function (series){
            _.each(series.metrics, function (value, metric){
                if(sizeFilter(value.value) && !doNotAddMetrics[metric]){
                    metrics_names[metric] = true;
                }

            })
        });
        return _.map(metrics_names, function(v,k){return k;})

    }
	drawSeparateCharts(chartsFactory.createBarChart);
	var $chartsSeparateActions = $('#chartsSeparateActions').addClass("aui-buttons");
	_.each(projections, function (projection) {
		var button = $(aui.buttons.button({text: projection.name, tagName: "a"}));
		if (projection.name == 'Bar') {
			button.addClass('active');
		}
		$chartsSeparateActions.append(button.click(getChangeChartDisplayClickFun(drawSeparateCharts, projection.func, $chartsSeparateActions)));
	});

}