AJS.test.require("jira.webresources:jira-global");

AJS.test.require("jira.webresources:jquery-livestamp", function() {

    'use strict';
    
    function assertRelativeUsesFormatStringForDate(now, date, type) {
        type = type || "timestamp";
        try {
            equal(
                JIRA.Time.formatDateWithRelativeAge(date.clone(), JIRA.Time.FormatType.types[type], now.clone()),
                JIRA.Time.formatDateWithFormatString(date.clone(), JIRA.Time.FormatType.types[type]),
                'Relative matches format string for ' + date.toDate().toUTCString());
        } catch(e) {
            ok(false, 'Error checking relative == format string with args ' + date.toDate().toUTCString() + ' and ' + type + '.\nCause: ' + e.toString());
        }
    }

    module('time.js Test Suite', {
        setup : function() {
            sinon.spy(AJS, "format");
        },
        teardown : function() {
            AJS.format.restore();
        }
    });

    test('format date with relative age', function() {

        function assertRelativeAge(now, date, expectedString) {
            equal(JIRA.Time.formatDateWithRelativeAge(date.clone(), JIRA.Time.FormatType.types.shortAge, now.clone()), expectedString, 'Formatted date is ' + expectedString);
        }

        // Sinon.spy() on getText() to get params.
        var midnight = moment("2012-02-16T00:00:00.000");
        var now = midnight.clone().add('d', 1).subtract('ms', 1); // 11:59pm 16 Feb 2012

        assertRelativeAge(now, now, "common.date.relative.a.moment.ago");
        assertRelativeAge(now, now.clone().subtract('s', 59), "common.date.relative.a.moment.ago");

        assertRelativeAge(now, now.clone().subtract('s', 60).add('ms', 1), "common.date.relative.a.moment.ago");
        assertRelativeAge(now, now.clone().subtract('s', 60), "common.date.relative.one.minute.ago");
        assertRelativeAge(now, now.clone().subtract('s', 60).subtract('ms', 1), "common.date.relative.one.minute.ago");

        assertRelativeAge(now, now.clone().subtract('m', 1).subtract('s', 29), "common.date.relative.one.minute.ago");
        assertRelativeAge(now, now.clone().subtract('m', 1).subtract('s', 30), "common.date.relative.one.minute.ago");
        assertRelativeAge(now, now.clone().subtract('m', 1).subtract('s', 31), "common.date.relative.one.minute.ago");

        assertRelativeAge(now, now.clone().subtract('m', 2).add('s', 1), "common.date.relative.one.minute.ago");
        assertRelativeAge(now, now.clone().subtract('m', 2), "common.date.relative.x.minutes.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.minutes.ago", 2);

        assertRelativeAge(now, now.clone().subtract('m', 2).subtract('s', 1), "common.date.relative.x.minutes.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.minutes.ago", 2);

        assertRelativeAge(now, now.clone().subtract('m', 2).subtract('s', 29), "common.date.relative.x.minutes.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.minutes.ago", 2);
        assertRelativeAge(now, now.clone().subtract('m', 2).subtract('s', 30), "common.date.relative.x.minutes.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.minutes.ago", 2);
        assertRelativeAge(now, now.clone().subtract('m', 2).subtract('s', 31), "common.date.relative.x.minutes.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.minutes.ago", 2);

        assertRelativeAge(now, now.clone().subtract('m', 29), "common.date.relative.x.minutes.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.minutes.ago", 29);
        assertRelativeAge(now, now.clone().subtract('m', 30), "common.date.relative.x.minutes.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.minutes.ago", 30);
        assertRelativeAge(now, now.clone().subtract('m', 31), "common.date.relative.x.minutes.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.minutes.ago", 31);

        assertRelativeAge(now, now.clone().subtract('m', 50).add('s', 1), "common.date.relative.x.minutes.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.minutes.ago", 49);
        assertRelativeAge(now, now.clone().subtract('m', 50), "common.date.relative.one.hour.ago");
        assertRelativeAge(now, now.clone().subtract('m', 50).subtract('s', 1), "common.date.relative.one.hour.ago");

        assertRelativeAge(now, now.clone().subtract('h', 1).subtract('m', 29), "common.date.relative.one.hour.ago");
        assertRelativeAge(now, now.clone().subtract('h', 1).subtract('m', 30), "common.date.relative.x.hours.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.hours.ago", 2);
        assertRelativeAge(now, now.clone().subtract('h', 1).subtract('m', 31), "common.date.relative.x.hours.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.hours.ago", 2);

        assertRelativeAge(now, now.clone().subtract('d', 1).add('ms', 1), "common.date.relative.x.hours.ago"); // Midnight
        AJS.format.lastCall.calledWith("common.date.relative.x.hours.ago", 24);
        assertRelativeAge(now, now.clone().subtract('d', 1), "common.date.relative.one.day.ago");

        assertRelativeAge(midnight, midnight.clone().subtract('h', 1), "common.date.relative.one.hour.ago");
        assertRelativeAge(midnight, midnight.clone().subtract('h', 5).add('s', 1), "common.date.relative.x.hours.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.hours.ago", 5);
        assertRelativeAge(midnight, midnight.clone().subtract('h', 5), "common.date.relative.x.hours.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.hours.ago", 5);

        assertRelativeAge(midnight, midnight.clone().subtract('h', 5).subtract('s', 1), "common.date.relative.one.day.ago");

        assertRelativeAge(midnight, midnight.clone().subtract('d', 1).add('ms', 1), "common.date.relative.one.day.ago");
        assertRelativeAge(midnight, midnight.clone().subtract('d', 1), "common.date.relative.one.day.ago");
        assertRelativeAge(midnight, midnight.clone().subtract('d', 1).subtract('ms', 1), "common.date.relative.x.days.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.days.ago", 2);

        var morning = midnight.clone().add('h', 1); // 1:00am 16 Feb 2012
        var eveningThreeDays = morning.clone().subtract('h', 50);
        assertRelativeAge(morning, eveningThreeDays, "common.date.relative.x.days.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.days.ago", 3); // three days ago!

        var morningTwoDays = morning.clone().subtract('h', 48);
        assertRelativeAge(morning, morningTwoDays, "common.date.relative.x.days.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.days.ago", 2); // two days ago!

        var evening = midnight.clone().add('h', 23); // 11pm 16 Feb 2012
        var morningTwoDaysLong = evening.clone().subtract('h', 60);
        assertRelativeAge(evening, morningTwoDaysLong, "common.date.relative.x.days.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.days.ago", 2); // two days ago!

        assertRelativeAge(now, now.clone().subtract('w', 1).add('ms', 1), "common.date.relative.x.days.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.days.ago", 6);
        assertRelativeAge(now, now.clone().subtract('w', 1), "common.date.relative.one.week.ago");
        assertRelativeAge(now, now.clone().subtract('w', 1).subtract('ms', 1), "common.date.relative.one.week.ago");

        assertRelativeAge(now, now.clone().subtract('w', 1).add('ms', 1), "common.date.relative.x.days.ago");
        AJS.format.lastCall.calledWith("common.date.relative.x.days.ago", 6);
        assertRelativeAge(now, now.clone().subtract('w', 1), "common.date.relative.one.week.ago");
        assertRelativeAge(now, now.clone().subtract('w', 1).subtract('d', 1).add('ms', 1), "common.date.relative.one.week.ago");

        //Future dates are also relative.
        assertRelativeAge(now, now.clone().add('w', 1), "common.date.relative.in.one.week");

        var str = JIRA.Time.formatDateWithRelativeAge(now.clone().subtract('w', 1).subtract('d', 1), JIRA.Time.FormatType.types.shortAge, now);
        ok(str);
    });


    test('format date with relative age in the future', function() {

        function assertRelativeAge(now, date, expectedString) {
            equal(JIRA.Time.formatDateWithRelativeAge(date.clone(), JIRA.Time.FormatType.types.shortAge, now.clone()), expectedString, 'Formatted date is ' + expectedString);
        }

        // Sinon.spy() on getText() to get params.
        var midnight = moment("2012-02-16T00:00:00.000");
        var now = midnight.clone().add('ms', 1); // 00:01am 16 Feb 2012

        assertRelativeAge(now, now.clone().add('s', 59), "common.date.relative.in.a.moment");

        assertRelativeAge(now, now.clone().add('s', 60).subtract('ms', 1), "common.date.relative.in.a.moment");
        assertRelativeAge(now, now.clone().add('s', 60), "common.date.relative.in.one.minute");
        assertRelativeAge(now, now.clone().add('s', 60).add('ms', 1), "common.date.relative.in.one.minute");

        assertRelativeAge(now, now.clone().add('m', 1).add('s', 29), "common.date.relative.in.one.minute");
        assertRelativeAge(now, now.clone().add('m', 1).add('s', 30), "common.date.relative.in.one.minute");
        assertRelativeAge(now, now.clone().add('m', 1).add('s', 31), "common.date.relative.in.one.minute");

        assertRelativeAge(now, now.clone().add('m', 2).subtract('s', 1), "common.date.relative.in.one.minute");
        assertRelativeAge(now, now.clone().add('m', 2), "common.date.relative.in.x.minutes");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.minutes", 2);

        assertRelativeAge(now, now.clone().add('m', 2).add('s', 1), "common.date.relative.in.x.minutes");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.minutes", 2);

        assertRelativeAge(now, now.clone().add('m', 2).add('s', 29), "common.date.relative.in.x.minutes");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.minutes", 2);
        assertRelativeAge(now, now.clone().add('m', 2).add('s', 30), "common.date.relative.in.x.minutes");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.minutes", 2);
        assertRelativeAge(now, now.clone().add('m', 2).add('s', 31), "common.date.relative.in.x.minutes");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.minutes", 2);

        assertRelativeAge(now, now.clone().add('m', 29), "common.date.relative.in.x.minutes");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.minutes", 29);
        assertRelativeAge(now, now.clone().add('m', 30), "common.date.relative.in.x.minutes");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.minutes", 30);
        assertRelativeAge(now, now.clone().add('m', 31), "common.date.relative.in.x.minutes");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.minutes", 31);

        assertRelativeAge(now, now.clone().add('m', 50).subtract('s', 1), "common.date.relative.in.x.minutes");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.minutes", 49);
        assertRelativeAge(now, now.clone().add('m', 50), "common.date.relative.in.one.hour");
        assertRelativeAge(now, now.clone().add('m', 50).add('s', 1), "common.date.relative.in.one.hour");

        assertRelativeAge(now, now.clone().add('h', 1).add('m', 29), "common.date.relative.in.one.hour");
        assertRelativeAge(now, now.clone().add('h', 1).add('m', 30), "common.date.relative.in.x.hours");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.hours", 2);
        assertRelativeAge(now, now.clone().add('h', 1).add('m', 31), "common.date.relative.in.x.hours");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.hours", 2);

        assertRelativeAge(now, now.clone().add('d', 1).subtract('ms', 1), "common.date.relative.in.one.day"); // Midnight
        assertRelativeAge(now, now.clone().add('d', 1).subtract('m', 1), "common.date.relative.in.x.hours"); // 11:59
        AJS.format.lastCall.calledWith("common.date.relative.in.x.hours", 24);
        assertRelativeAge(now, now.clone().add('d', 1), "common.date.relative.in.one.day");

        assertRelativeAge(midnight, midnight.clone().add('h', 1), "common.date.relative.in.one.hour");
        assertRelativeAge(midnight, midnight.clone().add('h', 5).subtract('s', 1), "common.date.relative.in.x.hours");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.hours", 5);
        assertRelativeAge(midnight, midnight.clone().add('h', 5), "common.date.relative.in.x.hours");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.hours", 5);

        assertRelativeAge(midnight.clone().subtract('s', 1), midnight.clone().add('h', 5), "common.date.relative.in.one.day");

        assertRelativeAge(midnight, midnight.clone().add('d', 1).add('ms', 1), "common.date.relative.in.one.day");
        assertRelativeAge(midnight, midnight.clone().add('d', 1), "common.date.relative.in.one.day");
        assertRelativeAge(midnight.clone().subtract('ms', 1), midnight.clone().add('d', 1), "common.date.relative.in.x.days");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.days", 2);

        var night = midnight.clone().subtract('h', 1); // 11:00pm 15 Feb 2012
        var morningThreeDays = night.clone().add('h', 50);
        assertRelativeAge(night, morningThreeDays, "common.date.relative.in.x.days");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.days", 3); // in three days!

        var morningTwoDays = night.clone().add('h', 48);
        assertRelativeAge(night, morningTwoDays, "common.date.relative.in.x.days");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.days", 2); // in two days

        var morning = midnight.clone().add('h', 1); // 11pm 16 Feb 2012
        var eveningTwoDaysLong = morning.clone().add('h', 60);
        assertRelativeAge(morning, eveningTwoDaysLong, "common.date.relative.in.x.days");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.days", 2); // in two days

        assertRelativeAge(now, now.clone().add('w', 1).subtract('ms', 1), "common.date.relative.in.x.days");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.days", 6);
        assertRelativeAge(now, now.clone().add('w', 1), "common.date.relative.in.one.week");
        assertRelativeAge(now, now.clone().add('w', 1).add('ms', 1), "common.date.relative.in.one.week");

        assertRelativeAge(now, now.clone().add('w', 1).subtract('ms', 1), "common.date.relative.in.x.days");
        AJS.format.lastCall.calledWith("common.date.relative.in.x.days", 6);
        assertRelativeAge(now, now.clone().add('w', 1), "common.date.relative.in.one.week");
        assertRelativeAge(now, now.clone().add('w', 1).add('d', 1).subtract('ms', 1), "common.date.relative.in.one.week");

        //Future dates are also relative.
        assertRelativeAge(now, now.clone().add('w', 1), "common.date.relative.in.one.week");

        var str = JIRA.Time.formatDateWithRelativeAge(now.clone().add('w', 1).add('d', 1), JIRA.Time.FormatType.types.shortAge, now);
        ok(str);
    });


    function assertFormatString(date, expected, type) {
        type = type || "timestamp";
        try {
            equal(JIRA.Time.formatDateWithFormatString(date.clone(), JIRA.Time.FormatType.types[type]), expected, 'formatted date is correct for ' + date.toDate().toUTCString());
        } catch(e) {
            ok(false, 'formatDateWithFormatString failed with args ' + date.toDate().toUTCString() + ' and ' + type + '.\nCause: ' + e.toString());
        }
    }

    test('format date with format string - format is correct (short, long, full, timestamp)', function() {
        var midnight = moment(new Date("2012-02-16T00:00:00.000+03:00"));
        var now = midnight.clone().add('d', 1).subtract('ms', 1); // 11:59pm 16 Feb 2012 +03:00

        assertFormatString(now.clone(), "LLL", "timestamp");
        assertFormatString(now.clone(), "LLL", "full");
        assertFormatString(now.clone(), "LL", "long");
        assertFormatString(now.clone(), "ll", "short");
    });

});

