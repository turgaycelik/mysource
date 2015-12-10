(function($) {
    var legacyPage = {
        relatedContentCssSelector: ".content-container > .content-related",
        mainContentCssSelector: ".content-container > .content-body"
    };

    var auiPage = {
        relatedContentCssSelector: ".aui-page-panel > .aui-page-panel-inner > .aui-page-panel-nav",
        mainContentCssSelector: ".aui-page-panel > .aui-page-panel-inner > .aui-page-panel-content"
    };

    var findEl = function (prop) {
        var $el = $(legacyPage[prop]);
        if ($el.size()) {
            AJS.log("This page is using a deprecated page layout markup pattern. It should be updated to use AUI page layout.");
        } else {
            $el = $(auiPage[prop]);
        }

        return $el;
    };

    JIRA.Page = $.extend({}, JIRA.Page, legacyPage, auiPage, {
        relatedContentElement: function() {
            return findEl('relatedContentCssSelector');
        },

        mainContentElement: function() {
            return findEl('mainContentCssSelector');
        }
    });
})(AJS.$);
