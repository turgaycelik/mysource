AJS.test.require("jira.webresources:jira-global");
AJS.test.require("jira.webresources:jquery-livestamp");

test("moment.js date formatting", function () {
    var m = moment.utc("2001-07-04T12:08:56.235");

    //No support for era (G) or general time zone (z)
    //equal(m.format(JIRA.translateSimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")), "2001.07.04 AD at 12:08:56 PDT");
    equal(m.format(JIRA.translateSimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss")), "2001.07.04 at 12:08:56");

    equal(m.format(JIRA.translateSimpleDateFormat("EEE, MMM d, ''yy")), "Wed, Jul 4, '01");
    equal(m.format(JIRA.translateSimpleDateFormat("h:mm a")), "12:08 PM");

    //No support for general time zone (z)
    //equal(m.format(JIRA.translateSimpleDateFormat("hh 'o''clock' a, zzzz")), "12 o'clock PM, Pacific Daylight Time");
    equal(m.format(JIRA.translateSimpleDateFormat("hh 'o''clock' a")), "12 o'clock PM");

    //No support for 0-11 hour (K) or general time zone (z)
    //equal(m.format(JIRA.translateSimpleDateFormat("K:mm a, z")), "0:08 PM, PDT");

    //No support for era (G) or five-digit year (yyyyy), "aaa" is equivalent to "a", "MMMMM" is equivalent to "MMMM"
    //equal(m.format(JIRA.translateSimpleDateFormat("yyyyy.MMMMM.dd GGG hh:mm aaa")), "02001.July.04 AD 12:08 PM");
    equal(m.format(JIRA.translateSimpleDateFormat("yyyy.MMMM.dd hh:mm a")), "2001.July.04 12:08 PM");

    equal(m.format(JIRA.translateSimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z")), "Wed, 4 Jul 2001 12:08:56 +0000");
    equal(m.format(JIRA.translateSimpleDateFormat("yyMMddHHmmssZ")), "010704120856+0000");
    equal(m.format(JIRA.translateSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")), "2001-07-04T12:08:56.235+0000");
    equal(m.format(JIRA.translateSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")), "2001-07-04T12:08:56.235+00:00");
    equal(m.format(JIRA.translateSimpleDateFormat("YYYY-'W'ww-u")), "2001-W27-3");

    //JIRA's defaults
    equal(m.format(JIRA.translateSimpleDateFormat("h:mm a")), "12:08 PM");
    equal(m.format(JIRA.translateSimpleDateFormat("EEEE h:mm a")), "Wednesday 12:08 PM");
    equal(m.format(JIRA.translateSimpleDateFormat("dd/MMM/yy h:mm a")), "04/Jul/01 12:08 PM");
    equal(m.format(JIRA.translateSimpleDateFormat("dd/MMM/yy")), "04/Jul/01");
});
