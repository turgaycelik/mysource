AJS.$(function ($) {
    var toggleBlocks = new JIRA.ToggleBlock({
        blockSelector: ".toggle-wrap",
        triggerSelector: ".mod-header .toggle-title",
        originalTargetIgnoreSelector: "a",
        storageCollectionName: "x-i-am-not-used",
        persist: false
    });

    function clearHash(title) {
        if (history && history.replaceState) {
            history.replaceState(null, title, location.href.split('#')[0]);
        } else {
            location.hash = "";
        }
    }

    function checkWorkflow() {
        var anchor = parseUri(location).anchor;
        if (anchor && anchor.indexOf("workflowName=") === 0) {
            clearHash("Workflow Page");
            return decodeURIComponent(anchor.split("=")[1].replace(/\+/g, " "));
        }
    }

    var workflowName = checkWorkflow();
    if (workflowName) {
        var $row = $("[data-workflow-name='" + workflowName +"']").addClass("focused");
        toggleBlocks.expand($row.closest(toggleBlocks.options.blockSelector));
        $row.scrollIntoView();
    }
});
