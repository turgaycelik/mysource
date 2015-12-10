(function ($, moment) {

    "use strict";

    var timeout,

        updateInterval = 6000,

        relativize = AJS.Meta.getBoolean("date-relativize"),

        livestamps = [],

        prep = function ($el, timestamp) {
            $el.data("livestampdata", timestamp);
            livestamps.push($el);
        },

        run = function () {
            clearTimeout(timeout);
            update();
            timeout = setTimeout(run, updateInterval);
        },

        update = function () {
            livestamps = $.grep(livestamps, function ($el) {
                var timestamp = $el.data("livestampdata"),
                    forceRelativize = $el.data('relativize'),
                    from,
                    to;
                if (!moment.isMoment(timestamp) || !$el.closest("html").length) {
                    $el.removeData("livestampdata");
                } else {
                    from = $el.text();
                    var tsFormat = $el.data("datetime-format");

                    tsFormat = tsFormat ? tsFormat : "fullAge"; // TODO We should transition to longAge.
                    if (!(relativize || forceRelativize)) {
                        // We should try not to have any tsFormats like 'AgeAge'
                        tsFormat = tsFormat.replace("Age", "");
                    }

                    to = JIRA.Time.formatDate(timestamp, JIRA.Time.FormatType.types[tsFormat], forceRelativize);

                    if (from !== to) {
                        $el.text(to);
                    }
                }
                return !!$el.data("livestampdata");
            });
        },

        add = function ($el) {
            $el.each(function () {
                var $this = $(this),
                    timestamp = $this.attr("datetime");
                if (timestamp) {
                    //reset the timezone to what's specified by the timestamp
                    var timestampMoment = isNaN(timestamp) ?
                        // assuming iso8601 timestamp, let moment#zone() handle it
                        moment(timestamp).zone(timestamp) :
                        // unix epoch timestamp (in milli-seconds)
                        moment(parseInt(timestamp, 10));
                    prep($this, timestampMoment);
                }
            });
            run();
            return $el;
        };

    $.fn.livestamp = function () {
        return add(this);
    };

}(jQuery, moment));
