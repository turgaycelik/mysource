define("jira/setup/setup-tracker", ["jquery"], function($){

    var isDevMode = AJS.isDevMode? AJS.isDevMode() : AJS.Meta.getBoolean("dev-mode");
    var iframeHost = isDevMode ? "https://qa-wac.internal.atlassian.com" : "https://www.atlassian.com";
    var iframeContextPath = "/pingback";

    function dataToUrl(data) {
        var queryStringParameters = [];
        for (var key in data) {
            queryStringParameters.push(key + "=" + encodeURIComponent(data[key]));
        }
        if (queryStringParameters.length) {
            return "?" + queryStringParameters.join("&");
        } else {
            return "";
        }
    }

    function insertIframe(paramsString){
        var deferred = $.Deferred();

        var $iframe = $("<iframe>")
            .css("display", "none")
            .attr("src", iframeHost + iframeContextPath + paramsString);

        $iframe.load(function(){
            deferred.resolve();
        });
        $iframe.appendTo("body");

        setTimeout(function(){
            deferred.reject();
        }, 3000);

        return deferred.promise();
    }

    /**
     * The id from metadata takes always precedence over local storage,
     * which is used only as a fallback for action which does not inherit
     * from AbstractSetupAction (VerifySMTPServerConnection)
     *
     * @returns id of current setup session
     */
    function getSetupSessionId(){
        var id = AJS.Meta.get("setup-session-id");
        var key = "jira.setup.session.id";

        if (id){
            localStorage.setItem(key, id);
        } else {
            id = localStorage.getItem(key);
        }

        return id;
    }

    function getDefaultParams(){
        return {
            "instantSetupOnStable": "true",
            "instantSetup": AJS.Meta.get("instant-setup"),
            "pg": window.location.pathname.replace(/\//g,"_"),
            "product": "jira",
            "SEN": AJS.Meta.get("SEN"),
            "setupSessionId": getSetupSessionId(),
            "sid": AJS.Meta.get("server-id"),
            "v": AJS.Meta.get("version-number")
        };
    }

    function insert(){
        return insertIframe(dataToUrl(getDefaultParams()));
    }

    function sendMailConfigurationEvent(params){
        var params = params || {};
        var extParams = $.extend(getDefaultParams(), params);

        return insertIframe(dataToUrl(extParams));
    }

    return {
        insert: insert,
        sendMailConfigurationEvent: sendMailConfigurationEvent
    };
});
