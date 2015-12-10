;(function() {
    jQuery(function() {
        AJS.describeBrowser();
    });
    
    AJS.$(document).bind('preferencesLoaded.streams', function() {

        moment.lang("jira", {
            longDateFormat: {
                LT: JIRA.translateSimpleDateFormat(ActivityStreams.getTimeFormat()),
                L: JIRA.translateSimpleDateFormat(ActivityStreams.getDateFormat()),
                LL: JIRA.translateSimpleDateFormat(ActivityStreams.getDateFormat()),
                LLL: JIRA.translateSimpleDateFormat(ActivityStreams.getDateTimeFormat())
            },
            calendar: {
                sameDay:  ActivityStreams.getDateRelativize() ? AJS.I18n.getText("common.date.relative.day.same", "[", "]", "LT") : "LLL",
                nextDay:  ActivityStreams.getDateRelativize() ? AJS.I18n.getText("common.date.relative.day.next", "[", "]", "LT") : "LLL",
                lastDay:  ActivityStreams.getDateRelativize() ? AJS.I18n.getText("common.date.relative.day.last", "[", "]", "LT") : "LLL",
                nextWeek: "LLL",
                lastWeek: "LLL",
                sameElse: "LLL"
            }
        });
    });

})();
