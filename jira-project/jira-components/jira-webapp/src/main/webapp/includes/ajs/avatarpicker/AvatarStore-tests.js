AJS.test.require("jira.webresources:avatar-picker");

module("JIRA.AvatarStore", {
    teardown: function() {
        this.sandbox.restore();
    },
    setup: function() {
        this.sandbox = sinon.sandbox.create();
    }
});

test("buildCompleteUrl should work for URLs with or without query params", function() {
    this.sandbox.stub(window, "atl_token").returns('TOKEN');

    var restUrl = "http://localhost:8090/jira/rest/api/latest/project/HSP-1";
    var projAvatarStore = new JIRA.AvatarStore({
        restQueryUrl: "blah",
        restCreateTempUrl: "blah",
        restUpdateTempUrl: "blah",
        defaultAvatarId: 1000
    });

    equal(projAvatarStore._buildCompleteUrl(restUrl), "http://localhost:8090/jira/rest/api/latest/project/HSP-1?atl_token=TOKEN", "URL for project avatar");

    restUrl = "http://localhost:8090/jira/rest/api/latest/user";
    var userAvatarStore = new JIRA.AvatarStore({
        restQueryUrl: "blah",
        restCreateTempUrl: "blah",
        restUpdateTempUrl: "blah",
        restParams: { username: "fred" },
        defaultAvatarId: 1000
    });
    equal(userAvatarStore._buildCompleteUrl(restUrl), "http://localhost:8090/jira/rest/api/latest/user?username=fred&atl_token=TOKEN", "URL for user avatar");
});