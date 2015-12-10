AJS.test.require("jira.webresources:viewissue");

module("Voters", {
    setup: function() {
        this.server = sinon.fakeServer.create();
    }
});

test("Voter construnction", function() {
    try{
        new JIRA.VotersUsersCollection();
        ok(false, "should have thrown an error");
    }catch(e) {
        ok(/issue key/.test(e.message), "Error should relate to the issue key");
    }

    var collection = new JIRA.VotersUsersCollection("test-123")
    ok(/test-123/.test(collection.url()), "collection url contains our issue key");
});

test("Can retrieve voters from server", function() {
    var resp = generateVoteResponse(AJS.contextPath(), ["admin"], "HSP-3");

    this.server.respondWith("GET", new RegExp(resp[0] + "*"),
            [200, { "Content-Type": "application/json" }, JSON.stringify(resp[1]) ]);

    var collection = new JIRA.VotersUsersCollection("HSP-3");
    collection.fetch();

    this.server.respond();

    equal(collection.size(), 1);
    equal(collection.at(0).get("name"), "admin", "the admin voted for this issue");
    equal(collection.get("admin"), collection.at(0), "getting by id vs. getting by index");
});

test("Can retrieve voters for multiple issues", function() {
    var resp1 = generateVoteResponse(AJS.contextPath(), ["admin"], "HSP-3");
    var resp2 = generateVoteResponse(AJS.contextPath(), ["cleese"], "SOMETHINGDIFFERENT-1");

    this.server.respondWith("GET", new RegExp(resp1[0] + "*"),
            [200, { "Content-Type": "application/json" }, JSON.stringify(resp1[1]) ]);
    this.server.respondWith("GET", new RegExp(resp2[0] + "*"),
            [200, { "Content-Type": "application/json" }, JSON.stringify(resp2[1]) ]);


    var collectionOne = new JIRA.VotersUsersCollection("HSP-3");
    var collectionTwo = new JIRA.VotersUsersCollection("SOMETHINGDIFFERENT-1");

    collectionOne.fetch();
    collectionTwo.fetch();

    this.server.respond();

    equal(collectionOne.at(0).get("name"), "admin");
    equal(collectionTwo.at(0).get("name"), "cleese");

});

test("Can vote for an issue", function() {
    var resp = generateVoteResponse(AJS.contextPath(), ["admin"], "HSP-3");

    this.server.respondWith("GET", new RegExp(resp[0] + "*"),
            [200, { "Content-Type": "application/json" }, JSON.stringify(resp[1]) ]);
    this.server.respondWith("POST", new RegExp(resp[0] + "*"),
            [204, {}, ""]);

    var collection = new JIRA.VotersUsersCollection("HSP-3");
    collection.vote();
    this.server.respond();

    ok(collection.get("admin") !== undefined, "admin should be in the collection now");
});

test("Can unvote for an issue", function() {
    var resp = generateVoteResponse(AJS.contextPath(), ["admin"], "HSP-3");

    this.server.respondWith("GET", new RegExp(resp[0] + "*"), function(xhr) {
        xhr.respond(200, { "Content-Type": "application/json" }, JSON.stringify(resp[1]))
    });
    this.server.respondWith("DELETE", new RegExp(resp[0] + "*"),
            [204, {}, ""]);

    var collection = new JIRA.VotersUsersCollection("HSP-3");

    collection.fetch();
    this.server.respond();

    ok(collection.get("admin") !== undefined, "admin should be in the collection to start with");

    // change issue repose that is used in the respondWith call
    resp = generateVoteResponse(AJS.contextPath(), [], "HSP-3");

    collection.unvote();
    this.server.respond();

    ok(collection.get("admin") === undefined, "admin should not be in the collection now");
});

/**
 * Generates a fake response to be used with sinon fakeXHR
 *
 * @param baseURL {String} - the base URL (probably ASJ.contextPath())
 * @param usernames {String[]} - a list of usernames to
 * @param issueKey {String} - The issue key to use
 * @returns {[{String}, {Object}]} - A list containing two things, the URL to respond to and the response to give
 */
function generateVoteResponse(baseURL, usernames, issueKey) {
    var url = baseURL + "/rest/api/2/issue/" + issueKey + "/votes";
    var issueResponse = {self: url, votes: 1, hasVoted: true, voters: []};
    usernames.forEach(function(username) {
        issueResponse.voters.push({ self: baseURL + "/rest/api/2/user?username=" + username, key: username, name: username, avatarUrls: {
            "16x16": baseURL + "/secure/useravatar?size=xsmall&avatarId=10052",
            "24x24": baseURL + "/secure/useravatar?size=small&avatarId=10052",
            "32x32": baseURL + "/secure/useravatar?size=medium&avatarId=10052",
            "48x48": baseURL + "/secure/useravatar?avatarId=10052"},
            displayName: username, active: true });
    });
    return [url, issueResponse];
}

module("Watchers", {
    setup: function() {
        this.server = sinon.fakeServer.create();
    }
});

test("Watcher construnction", function() {
    try{
        new JIRA.WatchersUsersCollection();
        ok(false, "should have thrown an error");
    }catch(e) {
        ok(/issue key/.test(e.message), "Error should relate to the issue key");
    }

    var collection = new JIRA.WatchersUsersCollection("test-123")
    ok(/test-123/.test(collection.url()), "collection url contains our issue key");
});

test("Can get watchers", function() {
    var resp = generateWatchersResponse(AJS.contextPath(), ["admin"], "HSP-3");

    this.server.respondWith("GET", new RegExp(resp[0] + "*"),
            [200, { "Content-Type": "application/json" }, JSON.stringify(resp[1]) ]);

    var collection = new JIRA.WatchersUsersCollection("HSP-3");
    collection.fetch();

    this.server.respond();

    equal(collection.size(), 1);
    equal(collection.at(0).get("name"), "admin", "the admin is watching this issue");
    equal(collection.get("admin"), collection.at(0), "getting by id vs. getting by index");
});

test("Can add watchers", function () {
    var resp = generateWatchersResponse(AJS.contextPath(), [], "HSP-3");

    this.server.respondWith("GET", new RegExp(resp[0] + "*"), function(xhr) {
        xhr.respond(200, { "Content-Type": "application/json" }, JSON.stringify(resp[1]))
    });
    this.server.respondWith("POST", new RegExp(resp[0] + "*"),
            [204, {}, ""]);

    var collection = new JIRA.WatchersUsersCollection("HSP-3");
    collection.fetch();
    this.server.respond();

    resp = generateWatchersResponse(AJS.contextPath(), ["admin"], "HSP-3");

    collection.addWatcher("admin");
    this.server.respond();

    equal(collection.get("admin").get("name"), "admin", "the admin is watching this issue");
});

test("Can remove watchers", function () {
    var resp = generateWatchersResponse(AJS.contextPath(), ["admin"], "HSP-3");

    this.server.respondWith("GET", new RegExp(resp[0] + "*"), function(xhr) {
        xhr.respond(200, { "Content-Type": "application/json" }, JSON.stringify(resp[1]))
    });
    this.server.respondWith("DELETE", new RegExp(resp[0] + "*"),
            [204, {}, ""]);

    var collection = new JIRA.WatchersUsersCollection("HSP-3");
    collection.fetch();

    this.server.respond();
    resp = generateWatchersResponse(AJS.contextPath(), [], "HSP-3");

    collection.removeWatcher("admin");

    this.server.respond();

    equal(collection.size(), 0, "there should be no one left now");
});


/**
 * Generates a fake response to be used with sinon fakeXHR
 *
 * @param baseURL {String} - the base URL (probably ASJ.contextPath())
 * @param usernames {String[]} - a list of usernames to
 * @param issueKey {String} - The issue key to use
 * @returns {[{String}, {Object}]} - A list containing two things, the URL to respond to and the response to give
 */

function generateWatchersResponse(baseURL, usernames, issueKey) {
    var url = baseURL + "/rest/api/2/issue/" + issueKey + "/watchers";
    var issueResponse = { self: url, watchCount: 1, isWatching: true, watchers: [] };
    usernames.forEach(function(username) {
        issueResponse.watchers.push({ self: baseURL + "/rest/api/2/user?username=" + username, key: username, name: username, avatarUrls: {
            "16x16": baseURL + "/secure/useravatar?size=xsmall&avatarId=10052",
            "24x24": baseURL + "/secure/useravatar?size=small&avatarId=10052",
            "32x32": baseURL + "/secure/useravatar?size=medium&avatarId=10052",
            "48x48": baseURL + "/secure/useravatar?avatarId=10052"},
            displayName: username, active: true });
    });
    return [url, issueResponse];
}