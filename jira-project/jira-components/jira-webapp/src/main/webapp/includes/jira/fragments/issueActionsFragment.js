/** @deprecated since JIRA 6.2. Write Soy templates. */
AJS.namespace("JIRA.FRAGMENTS");

JIRA.FRAGMENTS.issueActionsFragment = function () {

    function addIssueIdToReturnUrl(issueId) {
        var matchSelectedIssueId = /selectedIssueId=[0-9]*/g;

        if (self != top) {
            return encodeURIComponent(window.top.location.href);
        }

        var url = window.location.href,
           newUrl = url;

        if (/selectedIssueId=[0-9]*/.test(url)) {
            newUrl = url.replace(matchSelectedIssueId, "selectedIssueId=" + issueId);
        } else {
            if (url.lastIndexOf("?") >= 0) {
                newUrl = url + "&";
            } else {
                newUrl = url + "?";
            }
            newUrl = newUrl + "selectedIssueId=" + issueId;
        }
        return encodeURIComponent(newUrl);
    }

    return function(json) {
        json.returnUrl = json.returnUrl || addIssueIdToReturnUrl(json.id);
        return AJS.$(JIRA.Templates.Dropdowns.issueActionsDropdown(json));
    }

}();
