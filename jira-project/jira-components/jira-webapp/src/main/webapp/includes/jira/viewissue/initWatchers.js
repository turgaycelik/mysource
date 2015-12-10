AJS.$(function () {

    function getView(collection) {
        if (collection.isReadOnly) {
            return JIRA.WatchersReadOnly;
        } else if (collection.canBrowseUsers) {
            return JIRA.WatchersView;
        } else {
            return JIRA.WatchersNoBrowseView;
        }
    }

    // Wire up inline dialog to our Backbone view
    var dialog = AJS.InlineDialog("#view-watcher-list", "watchers", function (contents, trigger, doShowPopup) {
        var loadingIcon = AJS.$('#watching-toggle').next('.icon');
        var collection = new JIRA.WatchersUsersCollection(JIRA.Issue.getIssueKey());
        loadingIcon.addClass("loading");
        var view = getView(collection);
        new view({
            collection: collection
        }).render().done(function (viewHtml) {
            contents.html(viewHtml);
            contents.find(".cancel").click(function (e) {
                dialog.hide();
                e.preventDefault();
            });
            loadingIcon.removeClass('loading');
            doShowPopup();
        });
        collection.on("errorOccurred", function () {
            dialog.hide();
        });
    },
    {
        width: 240,
        useLiveEvents: true,
        items: "#view-watcher-list",
        preHideCallback: function () {
            return !AJS.InlineLayer.current; // Don't close if we have inline layer shown
        }
    });

    AJS.$(document).bind("keydown", function (e) {
        // special case for when user hover is open at same time
        if (e.keyCode === 27 && AJS.InlineDialog.current != dialog && dialog.is(":visible")) {
            if (AJS.InlineDialog.current) {
                AJS.InlineDialog.current.hide();
            }
            dialog.hide();
        }
    });

    // JRA-28786 Clicking any whitespace outside of the Watch dialog should dismiss the dialog
    AJS.$(document).click(function (e) {
        var currentDialog = AJS.InlineDialog.current;
        if (currentDialog && currentDialog.id === "watchers") {
            if (!jQuery(e.target).closest("#inline-dialog-watchers, #watchers-suggestions").length) {
                // I am not a child of the inline dialog
                currentDialog.hide();
            }
        }
    });

    //this is a hack, but it's necessary to stop click on the multi-select autocomplete from closing the
    //inline dialog. See JRADEV-8136
    AJS.$(document).bind("showLayer", function(e, type, hash) {
        if(type && type === "inlineDialog" && hash && hash.id && hash.id === "watchers") {
            AJS.$("body").unbind("click.watchers.inline-dialog-check");
        }
    });
});
