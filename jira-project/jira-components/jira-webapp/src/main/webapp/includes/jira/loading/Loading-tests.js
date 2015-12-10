AJS.test.require("jira.webresources:jira-global");

(function($) {
    module("JIRA.Loading");

    test("isVisible() correctly reflects the state", function () {
        ok(!JIRA.Loading.isVisible());
        JIRA.Loading.showLoadingIndicator();
        ok(JIRA.Loading.isVisible());
        JIRA.Loading.hideLoadingIndicator();
    });

    test("supports delaying loading indicator", function () {
        ok(!JIRA.Loading.isVisible(), "Loading indicator should be hidden initially.");

        JIRA.Loading.showLoadingIndicator({
            delay: 20
        });

        stop();

        setTimeout(function () {
            start();
            ok(!JIRA.Loading.isVisible(), "Loading indicator should still be hidden, due to the delay.");
            stop();
        }, 10);

        setTimeout(function () {
            start();
            ok(JIRA.Loading.isVisible(), "Loading indicator should be visible, since the delay has elapsed.");
            JIRA.Loading.hideLoadingIndicator();
        }, 30);
    });
})(AJS.$);