(function (moment) {

    "use strict";

    var meridiem = JIRA.getDateFormatSymbol("amPmStrings"),
        map = {
            d: "D",       // day
            y: "Y",       // year
            a: "A",       // meridiem
            E: "d",       // day name of week
            u: "d",       // day number of week
            Z: "ZZ",      // RFC 822 time zone
            z: "[GMT]ZZ", // replacing time zone name with offset
            XX: "ZZ",     // ISO 8601 time zone
            XXX: "Z"      // ISO 8601 time zone
        };

    JIRA.translateSimpleDateFormat = function (pattern) {
        var inQuote = false,
            skip = false,
            tmpBuffer = "",
            actuallyTranslate = function (tmpBuffer) {
                return map[tmpBuffer] || _.reduce(tmpBuffer, function (memo, value) {
                    return memo + (map[value] || value);
                }, "");
            },
            reduction = _.reduce(pattern, function (memo, value, index, list) {
                if (skip) {
                    skip = false;
                } else if (value === '\'') {
                    if (tmpBuffer) {
                        memo += actuallyTranslate(tmpBuffer);
                        tmpBuffer = "";
                    }
                    if (list[index + 1] === '\'') {
                        memo += value;
                        skip = true;
                    } else {
                        memo += !inQuote ? "[" : "]";
                        inQuote = !inQuote;
                    }
                } else if (inQuote) {
                    memo += value;
                } else if (!/[a-zA-Z]/.test(value)) {
                    if (tmpBuffer) {
                        memo += actuallyTranslate(tmpBuffer);
                        tmpBuffer = "";
                    }
                    memo += value;
                } else if (!tmpBuffer || tmpBuffer[tmpBuffer.length - 1] === value) {
                    tmpBuffer += value;
                } else {
                    memo += actuallyTranslate(tmpBuffer);
                    tmpBuffer = value;
                }
                return memo;
            }, "");
        if (tmpBuffer) {
            reduction += actuallyTranslate(tmpBuffer);
        }
        return reduction;
    };

    moment.lang("jira", {
        months: JIRA.getDateFormatSymbol("months"),
        monthsShort: JIRA.getDateFormatSymbol("shortMonths"),
        weekdays: JIRA.getDateFormatSymbol("weekdays"),
        weekdaysShort: JIRA.getDateFormatSymbol("shortWeekdays"),
        weekdaysMin: JIRA.getDateFormatSymbol("shortWeekdays"),
        longDateFormat: {
            LT: JIRA.translateSimpleDateFormat(AJS.Meta.get("date-time")),
            L: JIRA.translateSimpleDateFormat(AJS.Meta.get("date-day")),
            LL: JIRA.translateSimpleDateFormat(AJS.Meta.get("date-dmy")),
            LLL: JIRA.translateSimpleDateFormat(AJS.Meta.get("date-complete"))
        },
        meridiem: function (hours) {
            return meridiem[+(hours > 11)];
        },

        calendar: {
            sameDay:  "LLL",
            nextDay:  "LLL",
            nextWeek: "LLL",
            lastDay:  "LLL",
            lastWeek: "LLL",
            sameElse: "LLL"
        },

        // TODO Deprecate?
        relativeTime: {
            future: AJS.I18n.getText("common.date.relative.time.future", "%s"),
            past: AJS.I18n.getText("common.date.relative.time.past", "%s"),
            s: AJS.I18n.getText("common.date.relative.time.seconds"),
            m: AJS.I18n.getText("common.date.relative.time.minute"),
            mm: AJS.I18n.getText("common.date.relative.time.minutes", "%d"),
            h: AJS.I18n.getText("common.date.relative.time.hour"),
            hh: AJS.I18n.getText("common.date.relative.time.hours", "%d"),
            d: AJS.I18n.getText("common.date.relative.time.day"),
            dd: AJS.I18n.getText("common.date.relative.time.days", "%d"),
            M: AJS.I18n.getText("common.date.relative.time.month"),
            MM: AJS.I18n.getText("common.date.relative.time.months", "%d"),
            y: AJS.I18n.getText("common.date.relative.time.year"),
            yy: AJS.I18n.getText("common.date.relative.time.years", "%d")
        }
    });

}(moment));
