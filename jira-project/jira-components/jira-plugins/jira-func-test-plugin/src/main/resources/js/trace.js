
(function($) {

    var list = [];

    /**
     * first argument is the key of this trace (usually a dot-qualified identifier)
     * the remaining arguments are arbitrary, but preserved.
     */
    JIRA.trace = function(id) {
        if (JIRA.trace.callback) {
            JIRA.trace.callback.apply(this, arguments);
        }
        var rest = Array.prototype.slice.call(arguments);
        rest.shift();
        list.push({
            ts: (new Date).getTime(),
            id: id,
            args: rest
        });
    };

    JIRA.trace.drain = function() {
        var r = list;
        list = [];
        return r;
    };

    JIRA.trace.inspect = function() {
        return list;
    };

    $(document).ajaxComplete(function(event, request, settings) {
        JIRA.trace('AJS.$.ajaxComplete', settings.url);
    });

    $(document).ajaxSuccess(function(event, request, settings) {
        JIRA.trace('AJS.$.ajaxSuccess', settings.url);
    });

    $(document).ajaxError(function(event, request, settings) {
        JIRA.trace('AJS.$.ajaxError', settings.url);
    });

})(AJS.$);