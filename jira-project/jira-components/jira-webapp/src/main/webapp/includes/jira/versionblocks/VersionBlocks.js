JIRA.VersionBlocks = {};
JIRA.VersionBlocks.ClassNames = {
    VERSION_LIST: "versions-list",
    VERSION_CONTAINER: "version-block-container",
    EXPANDED_CONTENT_CONTAINER: "version-issue-table"
};
JIRA.VersionBlocks.init = (function () {
    var classNames = JIRA.VersionBlocks.ClassNames;
    var dataElement = AJS.$("."+classNames.VERSION_LIST).first();
    var urlEndpoint = getUrl();

    AJS.$(document).on("click", "a[data-version-block]", function(e) {
        var $a = AJS.$(this),
                $root = $a.closest("."+classNames.VERSION_CONTAINER);
        var isActive = $root.data("expanded");
        var $container = $root.find("."+classNames.EXPANDED_CONTENT_CONTAINER);
        var $spinner = AJS.$("<span class='icon loading'></span>");

        if (!$container.size()) {
            $container = AJS.$("<div/>").addClass(classNames.EXPANDED_CONTENT_CONTAINER).hide().appendTo($root);
        }

        // Tell the server about the state we want this version to be in now
        var extraParams = {};
        extraParams[(isActive) ? "collapseVersion" : "expandVersion"] = $root.find('[data-version-id]').data("version-id");

        // Make our request for data
        var request = AJS.$.ajax({
            url: urlEndpoint,
            data: getUrlParams(extraParams),
            dataType: "json",
            beforeSend: function() {
                $a.closest("ul").append(AJS.$("<li/>").append($spinner));
            }
        });

        request.always(function() {
            $spinner.parent("li").remove();
        });

        // Handle any errors in transmission or translation
        request.fail(function(jqXHR, textStatus, errorThrown) {
            console.log("Failed to load issues for version. User-facing error to do, sorry :(", arguments);
        });

        // Update our DOM with the result of the toggle
        if (!isActive) {
            request.done(function(data, textStatus, jqXHR) {
                var html = AJS.$("<div/>").html(data.content);
                var content = html.find("."+classNames.EXPANDED_CONTENT_CONTAINER);
                $container.hide().replaceWith(content).show();
            });
        } else {
            request.done(function() {
                $container.hide();
            });
        }

        // Toggle our state
        request.done(function() {
            $root.data("expanded", !isActive);
        });

        e.preventDefault();
    });

    /**
     * The URL we need to hit to get the list of issues we want, sans parameters.
     * Ideally this would be a REST endpoint and it'd return an array of issues.
     * Unfortunately I'm stuck with hitting a crappy overloaded URL that gets its
     * data crappily and renders crappy markup.
     */
    function getUrl() {
        var href;
        href = document.location.href;
        href = href.replace(document.location.hash,"");
        href = href.replace(document.location.search,"");

        return href;
    }

    function getUrlParams(opts) {
        var params = {
            decorator: "none",
            contentOnly: true,
            noTitle: true,
            selectedTab: getSelectedTab(),
            pid: dataElement.data("project-id"),
            component: dataElement.data("component-id")
        };

        return jQuery.extend(params, opts);
    }

    function getSelectedTab() {
        var selectedTab = dataElement.data("selected-tab");
        var paramString = "" + document.location.search + document.location.hash; // dhtmlHistory puts it in as a hash :(
        if (paramString.indexOf("selectedTab=") > -1) {
            selectedTab = paramString.replace(/^.*selectedTab=(.*?)(?:&.*$|$)/, "$1"); // a regex-ish substringer -- basically, discard everything before and after the value of the selectedTab param.
            selectedTab = decodeURIComponent(selectedTab);
        }
        return selectedTab;
    }
});
