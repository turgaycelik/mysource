(function($){
    var context = AJS.namespace("JIRA.Func.Websudo");
    var ajax = $.ajax;

    var success = function(msg) {
        if (JIRA.Messages.showSuccessMsg) {
            JIRA.Messages.showSuccessMsg(msg);
        } else {
            console.log(msg);
        }
    };

    var fail = function(msg) {
        if (JIRA.Messages.showErrorMsg) {
            JIRA.Messages.showErrorMsg(msg);
        } else {
            console.log(msg);
        }
    };

    var websudoSet = function(enable) {
        var deferred = ajax({
            url:contextPath + "/rest/testkit-test/latest/websudo",
            type:"POST",
            data: JSON.stringify(enable),
            dataType: "json",
            contentType: "application/json",
            global: false
        });

        deferred.done(function() {
            success(enable && "Websudo was enabled." || "Websudo was disabled.");

        });
        deferred.fail(function() {
            fail("Unable to set websudo state.");
        });
    };

    context.enable = function() {
        websudoSet(true);
    };

    context.disable = function() {
        websudoSet(false);
    };

    context.drop = function() {
        var def = $.ajax({
            type: "DELETE",
            url: contextPath + "/rest/auth/1/websudo",
            contentType: "application/json",
            global: false
        });
        def.done(function() {
            success("Websudo access dropped.");
        });
        def.fail(function() {
            fail("Unable to drop websudo access.");
        });
    };

})(AJS.$);