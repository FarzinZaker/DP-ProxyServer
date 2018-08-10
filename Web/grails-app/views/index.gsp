<%@ page import="proxy.SystemConfig" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Proxy Server</title>
    <asset:javascript src="jquery-1.9.0.min.js"/>
    <asset:javascript src="highcharts/highcharts.js"/>
    <style>
    .stats {
        position: absolute;
        top: 10px;
        right: 10px;
        z-index: 1000;
        color: white;
        background-color: #4D8618;
        padding: 5px 0 5px 10px;
    }

    .stats span {
        display: inline-block;
        padding: 2px 10px;
    }

    .stats span span {
        background-color: #4cae4c;
        font-weight: bold;
    }

    .stats span span + span {
        margin-right: 20px;
    }
    </style>
</head>

<body>

<div class="stats">
    ADAPTATIONS:
    <span><span id="adaptationsCount">0</span> <span id="adaptationsPercent">0</span></span>
    SLA VIOLATIONS:
    <span><span id="violationsCount">0</span> <span id="violationsPercent">0</span></span>
    WAITING REQUESTS:
    <span><span id="waitingRequests">0</span></span>
    REQUESTS SERVED:
    <span><span id="requests">0</span></span>
</div>

<div>
    <div id="bandwidthChartContainer" style="display:inline-block"></div>

    %{--<div id="bandwidthWeightsContainer" style="display:inline-block"></div>--}%

    <div id="utilizationChartContainer" style="display:inline-block"></div>

    <div id="responseTimesChartContainer" style="display:inline-block"></div>

    <div id="arrivalRatesChartContainer" style="display:inline-block"></div>

    <div id="adaptationsContainer" style="display:inline-block"></div>

    <div id="violationsContainer" style="display:inline-block"></div>
</div>
<script type="text/javascript">
    var bandwidthChart;
    //    var bandwidthWeightsChart;
    var responseTimesChart;
    var arrivalRatesChart;
    var utilizationChart;
    var adaptations;
    var violations;
    $(window).on('resize', function () {
        $('#bandwidthChartContainer').height($(window).height() / 2 - 40).width($(window).width() / 2 - 20);
//        $('#bandwidthWeightsContainer').height($(window).height() / 2 - 40).width($(window).width() / 2 - 20);
        $('#adaptationsContainer').height($(window).height() / 2 - 40).width($(window).width() / 2 - 20);
        $('#violationsContainer').height($(window).height() / 2 - 40).width($(window).width() / 2 - 20);
        $('#responseTimesChartContainer').height($(window).height() / 2 - 40).width($(window).width() / 2 - 20);
        $('#arrivalRatesChartContainer').height($(window).height() / 2 - 40).width($(window).width() / 2 - 20);
        $('#utilizationChartContainer').height($(window).height() / 2 - 40).width($(window).width() / 2 - 20);
    });
    $(document).ready(function () {
        $('#bandwidthChartContainer').height($(window).height() / 2 - 40).width($(window).width() / 2 - 20);
//        $('#bandwidthWeightsContainer').height($(window).height() / 2 - 40).width($(window).width() / 2 - 20);
        $('#adaptationsContainer').height($(window).height() / 2 - 40).width($(window).width() / 2 - 20);
        $('#violationsContainer').height($(window).height() / 2 - 40).width($(window).width() / 2 - 20);
        $('#responseTimesChartContainer').height($(window).height() / 2 - 40).width($(window).width() / 2 - 20);
        $('#arrivalRatesChartContainer').height($(window).height() / 2 - 40).width($(window).width() / 2 - 20);
        $('#utilizationChartContainer').height($(window).height() / 2 - 40).width($(window).width() / 2 - 20);
        bandwidthChart = new Highcharts.Chart({
            chart: {
                renderTo: 'bandwidthChartContainer',
                type: 'spline'
            },
            title: {
                text: 'Bandwidth',
                x: -20 //center
            },
            plotOptions: {
                series: {
                    marker: {
                        enabled: false
                    }
                }
            },
            xAxis: {
                title: {
                    text: 'Time'
                }
            },
            yAxis: {
                title: {
                    text: 'Bandwidth'
                },
//                plotLines: [{
//                    value: 0,
//                    width: 1,
//                    color: '#808080'
//                }]
            },
            tooltip: {
                valueSuffix: '',
                shared: true
            },
            legend: {
                layout: 'horizontal',
                align: 'center',
                horizontalAlign: 'center',
                borderWidth: 0
            },
            series: [
                <g:each in="${SystemConfig.scenarios?.sort{it.key}}" var="scenario">
                {
                    name: '${scenario.key}',
                    data: (function () {
                        // generate an array of random data
                        var data = [];
                        return data;
                    })()
                },
                </g:each>
            ]
        });
        %{--bandwidthWeightsChart = new Highcharts.Chart({--}%
        %{--chart: {--}%
        %{--renderTo: 'bandwidthWeightsContainer',--}%
        %{--type: 'spline'--}%
        %{--},--}%
        %{--title: {--}%
        %{--text: 'Bandwidth Weights',--}%
        %{--x: -20 //center--}%
        %{--},--}%
        %{--plotOptions: {--}%
        %{--series: {--}%
        %{--marker: {--}%
        %{--enabled: false--}%
        %{--}--}%
        %{--}--}%
        %{--},--}%
        %{--xAxis: {--}%
        %{--title: {--}%
        %{--text: 'Time'--}%
        %{--}--}%
        %{--},--}%
        %{--yAxis: {--}%
        %{--title: {--}%
        %{--text: 'Bandwidth Weights'--}%
        %{--},--}%
        %{--//                plotLines: [{--}%
        %{--//                    value: 0,--}%
        %{--//                    width: 1,--}%
        %{--//                    color: '#808080'--}%
        %{--//                }]--}%
        %{--},--}%
        %{--tooltip: {--}%
        %{--valueSuffix: '',--}%
        %{--shared: true--}%
        %{--},--}%
        %{--legend: {--}%
        %{--layout: 'horizontal',--}%
        %{--align: 'center',--}%
        %{--horizontalAlign: 'center',--}%
        %{--borderWidth: 0--}%
        %{--},--}%
        %{--series: [--}%
        %{--<g:each in="${SystemConfig.scenarios?.sort{it.key}}" var="scenario">--}%
        %{--{--}%
        %{--name: '${scenario.key}',--}%
        %{--data: (function () {--}%
        %{--// generate an array of random data--}%
        %{--var data = [];--}%
        %{--return data;--}%
        %{--})()--}%
        %{--},--}%
        %{--</g:each>--}%
        %{--]--}%
        %{--});--}%
        responseTimesChart = new Highcharts.Chart({
            chart: {
                renderTo: 'responseTimesChartContainer',
                type: 'spline'
            },
            title: {
                text: 'Response Time',
                x: -20 //center
            },
            plotOptions: {
                series: {
                    marker: {
                        enabled: false
                    }
                }
            },
            xAxis: {
                title: {
                    text: 'Time'
                }
            },
            yAxis: {
                title: {
                    text: 'ResponseTime'
                },
//                plotLines: [{
//                    value: 0,
//                    width: 1,
//                    color: '#808080'
//                }]
            },
            tooltip: {
                valueSuffix: '',
                shared: true
            },
            legend: {
                layout: 'horizontal',
                align: 'center',
                horizontalAlign: 'center',
                borderWidth: 0
            },
            series: [
                <g:each in="${SystemConfig.scenarios?.sort{it.key}}" var="scenario">
                {
                    name: '${scenario.key}',
                    data: (function () {
                        // generate an array of random data
                        var data = [];
                        return data;
                    })()
                },
                </g:each>
            ]
        });
        arrivalRatesChart = new Highcharts.Chart({
            chart: {
                renderTo: 'arrivalRatesChartContainer',
                type: 'spline'
            },
            title: {
                text: 'Arrival Rate',
                x: -20 //center
            },
            plotOptions: {
                series: {
                    marker: {
                        enabled: false
                    }
                }
            },
            xAxis: {
                title: {
                    text: 'Time'
                }
            },
            yAxis: {
                title: {
                    text: 'Arrival Rate'
                },
//                plotLines: [{
//                    value: 0,
//                    width: 1,
//                    color: '#808080'
//                }]
            },
            tooltip: {
                valueSuffix: '',
                shared: true
            },
            legend: {
                layout: 'horizontal',
                align: 'center',
                horizontalAlign: 'center',
                borderWidth: 0
            },
            series: [
                <g:each in="${SystemConfig.scenarios?.sort{it.key}}" var="scenario">
                {
                    name: '${scenario.key}',
                    data: (function () {
                        // generate an array of random data
                        var data = [];
                        return data;
                    })()
                },
                </g:each>
            ]
        });
        utilizationChart = new Highcharts.Chart({
            chart: {
                renderTo: 'utilizationChartContainer',
                type: 'spline'
            },
            title: {
                text: 'Servers Utilization',
                x: -20 //center
            },
            plotOptions: {
                series: {
                    marker: {
                        enabled: false
                    }
                }
            },
            xAxis: {
                title: {
                    text: 'Time'
                }
            },
            yAxis: {
                title: {
                    text: 'Utilization'
                },
//                plotLines: [{
//                    value: 0,
//                    width: 1,
//                    color: '#808080'
//                }]
            },
            tooltip: {
                valueSuffix: '',
                shared: true
            },
            legend: {
                layout: 'horizontal',
                align: 'center',
                horizontalAlign: 'center',
                borderWidth: 0
            },
            series: [
                {
                    name: 'Load Balancer',
                    data: (function () {
                        // generate an array of random data
                        var data = [];
                        return data;
                    })()
                },
                {
                    name: 'Backend 1',
                    data: (function () {
                        // generate an array of random data
                        var data = [];
                        return data;
                    })()
                },
                {
                    name: 'Backend 2',
                    data: (function () {
                        // generate an array of random data
                        var data = [];
                        return data;
                    })()
                },
                {
                    name: 'Backend 3',
                    data: (function () {
                        // generate an array of random data
                        var data = [];
                        return data;
                    })()
                },
                {
                    name: 'Data Host',
                    data: (function () {
                        // generate an array of random data
                        var data = [];
                        return data;
                    })()
                },
                {
                    name: 'Proxy',
                    data: (function () {
                        // generate an array of random data
                        var data = [];
                        return data;
                    })()
                }
            ]
        });
        adaptations = new Highcharts.Chart({
            chart: {
                renderTo: 'adaptationsContainer',
                type: 'spline'
            },
            title: {
                text: 'Adaptations',
                x: -20 //center
            },
            plotOptions: {
                series: {
                    marker: {
                        enabled: false
                    }
                }
            },
            xAxis: {
                title: {
                    text: 'Time'
                }
            },
            yAxis: {
                title: {
                    text: 'Adaptations'
                },
//                plotLines: [{
//                    value: 0,
//                    width: 1,
//                    color: '#808080'
//                }]
            },
            tooltip: {
                valueSuffix: '',
                shared: true
            },
            legend: {
                layout: 'horizontal',
                align: 'center',
                horizontalAlign: 'center',
                borderWidth: 0
            },
            series: [
                <g:each in="${SystemConfig.scenarios?.sort{it.key}}" var="scenario">
                {
                    name: '${scenario.key}',
                    data: (function () {
                        // generate an array of random data
                        var data = [];
                        return data;
                    })()
                },
                </g:each>
            ]
        });
        violations = new Highcharts.Chart({
            chart: {
                renderTo: 'violationsContainer',
                type: 'spline'
            },
            title: {
                text: 'Violations',
                x: -20 //center
            },
            plotOptions: {
                series: {
                    marker: {
                        enabled: false
                    }
                }
            },
            xAxis: {
                title: {
                    text: 'Time'
                }
            },
            yAxis: {
                title: {
                    text: 'Violations'
                },
//                plotLines: [{
//                    value: 0,
//                    width: 1,
//                    color: '#808080'
//                }]
            },
            tooltip: {
                valueSuffix: '',
                shared: true
            },
            legend: {
                layout: 'horizontal',
                align: 'center',
                horizontalAlign: 'center',
                borderWidth: 0
            },
            series: [
                <g:each in="${SystemConfig.scenarios?.sort{it.key}}" var="scenario">
                {
                    name: '${scenario.key}',
                    data: (function () {
                        // generate an array of random data
                        var data = [];
                        return data;
                    })()
                },
                </g:each>
            ]
        });
        loadData()
    });

    var indexer = -1;
    function loadData() {
        $.ajax({
            type: "POST",
            url: '${createLink(controller:'proxy', action:'report')}'
        }).done(function (response) {
            if (response.time > indexer) {
                indexer = response.time;
                <g:each in="${SystemConfig.scenarios?.sort{it.key}}" var="scenario" status="i">
                bandwidthChart.series[${i}].addPoint([indexer, response.bandwidth['${scenario.key}']], true, indexer > 100);
                %{--bandwidthWeightsChart.series[${i}].addPoint([indexer, response.bandwidthWeights['${scenario.key}']], true, indexer > 100);--}%
                responseTimesChart.series[${i}].addPoint([indexer, response.responseTimes['${scenario.key}']], true, indexer > 100);
                arrivalRatesChart.series[${i}].addPoint([indexer, response.arrivalRates['${scenario.key}']], true, indexer > 100);
                adaptations.series[${i}].addPoint([indexer, response.adaptationsCount['${scenario.key}']], true, indexer > 100);
                violations.series[${i}].addPoint([indexer, response.violationsCount['${scenario.key}']], true, indexer > 100);
                </g:each>
                utilizationChart.series[0].addPoint([indexer, response.utilization.web], true, indexer > 100);
                utilizationChart.series[1].addPoint([indexer, response.utilization.backend1], true, indexer > 100);
                utilizationChart.series[2].addPoint([indexer, response.utilization.backend2], true, indexer > 100);
                utilizationChart.series[3].addPoint([indexer, response.utilization.backend3], true, indexer > 100);
                utilizationChart.series[4].addPoint([indexer, response.utilization.data], true, indexer > 100);
                utilizationChart.series[5].addPoint([indexer, response.utilization.balancer], true, indexer > 100);

                //stat
                var stat = response.stat;
                $('#requests').html(stat.requests);
                $('#waitingRequests').html(stat.waitingRequests);
                var requests = stat.requests;
                if (requests === 0)
                    requests = 1;
                $('#adaptationsCount').html(stat.adaptations);
                $('#adaptationsPercent').html(stat.adaptations * 100 / requests + ' %');
                $('#violationsCount').html(stat.violations);
                $('#violationsPercent').html(stat.violations * 100 / requests + ' %');
            }
            setTimeout(loadData, 3000);
        });

    }

</script>
</body>
</html>
