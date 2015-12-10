AJS.test.require("jira.webresources:jira-global");

(function() {

    test("ToggleBlock.checkIsPermlink", function() {

        var toggleBlock = new JIRA.ToggleBlock();

        var urlBase = "http://localhost:8090/jira/browse/HSP-1";

        ok(toggleBlock.checkIsPermlink(urlBase + "?focusedCommentId=xxx"));
        ok(toggleBlock.checkIsPermlink(urlBase + "?focusedWorklogId=xxx"));
        ok(toggleBlock.checkIsPermlink(urlBase + "?focusedCommentId=xxx#zzz"));
        ok(toggleBlock.checkIsPermlink(urlBase + "?focusedCommentId=10000&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-10000"));

        ok(!toggleBlock.checkIsPermlink(urlBase));
        ok(!toggleBlock.checkIsPermlink(urlBase + "?page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-10000"));
    });
})();
