AJS.$(function () {

    // Wire up inline dialog to our Backbone view
    var dialog = AJS.InlineDialog("#view-voter-list", "voters", function (contents, trigger, doShowPopup) {
                var loadingIcon = AJS.$('#vote-toggle').next('.icon');
                var collection = new JIRA.VotersUsersCollection(JIRA.Issue.getIssueKey());
                loadingIcon.addClass("loading");
                new JIRA.VotersView({
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
                items: "#view-voters-list",
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

    // Clicking any whitespace outside of the dialog should dismiss the dialog
    AJS.$(document).click(function (e) {
        var currentDialog = AJS.InlineDialog.current;
        if (currentDialog && currentDialog.id === "voters") {
            if (!jQuery(e.target).closest("#inline-dialog-voters").length) {
                // I am not a child of the inline dialog
                currentDialog.hide();
            }
        }
    });
});