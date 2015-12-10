AJS.test.require("jira.webresources:jira-global");

(function () {

    var baseURL = window.location.href.replace(/(.*)qunit.*/, "$1");
    AJS.params.baseURL = baseURL;

    test("Access Granted", function () {
        var incorrectProtocal = baseURL.replace("http:", "https:");
        
        ok(!AJS.canAccessIframe(jQuery("<iframe src='http://www.realsurf.com' />")), "http://www.realsurf.com - Access Refused");
        ok(!AJS.canAccessIframe(jQuery("<iframe src='" + baseURL.replace(/(.*)(:\d+)(.*)/, "$1:9999$2") + "' />")), "Expected incorrect port to refuse access");
        ok(!AJS.canAccessIframe(jQuery("<iframe src='" + incorrectProtocal + "' />")), incorrectProtocal + " (incorrect protocal)");
    });

    test("Access Refused", function () {
        ok(AJS.canAccessIframe(jQuery("<iframe src='" + baseURL + "' />")), baseURL);
        ok(AJS.canAccessIframe(jQuery("<iframe src='/test.html' />")), "/test.html");
        ok(AJS.canAccessIframe(jQuery("<iframe src='test.html' />")), "test.html");
    });

})();