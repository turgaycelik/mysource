AJS.test.require("jira.webresources:jira-global");
AJS.test.require("jira.webresources:select-pickers");
AJS.test.require("com.atlassian.auiplugin:ajs-underscorejs");
AJS.test.require("jira.webresources:issue-statuses");

module("AJS.CheckboxMultiSelectStatusLozenge", {
    setup: function () {

        jQuery.fn.tipsy = function () {};

        var $select = jQuery("<select class=\"select js-default-checkboxmultiselectstatuslozenge\" data-placeholder-text=\"Find Statuses...\" id=\"searcher-status\" multiple=\"multiple\" name=\"status\" size=\"4\" data-status-lozenge=\"true\">"
                + "<option class=\"imagebacked\" data-icon=\"/jira/images/icons/statuses/open.png\" value=\"1\" title=\"Open\" data-simple-status=\"{&quot;id&quot;:&quot;1&quot;,&quot;name&quot;:&quot;Open&quot;,&quot;description&quot;:&quot;The issue is open and ready for the assignee to start work on it.&quot;,&quot;iconUrl&quot;:&quot;/jira/images/icons/statuses/open.png&quot;,&quot;statusCategory&quot;:{&quot;id&quot;:2,&quot;key&quot;:&quot;new&quot;,&quot;colorName&quot;:&quot;blue-gray&quot;}}\">Open</option>"
                + "<option class=\"imagebacked\" data-icon=\"/jira/images/icons/statuses/inprogress.png\" value=\"3\" title=\"In Progress\" data-simple-status=\"{&quot;id&quot;:&quot;3&quot;,&quot;name&quot;:&quot;In Progress&quot;,&quot;description&quot;:&quot;This issue is being actively worked on at the moment by the assignee.&quot;,&quot;iconUrl&quot;:&quot;/jira/images/icons/statuses/inprogress.png&quot;,&quot;statusCategory&quot;:{&quot;id&quot;:4,&quot;key&quot;:&quot;indeterminate&quot;,&quot;colorName&quot;:&quot;yellow&quot;}}\">In Progress</option>"
                + "<option class=\"imagebacked\" data-icon=\"/jira/images/icons/statuses/reopened.png\" value=\"4\" title=\"Reopened\" data-simple-status=\"{&quot;id&quot;:&quot;4&quot;,&quot;name&quot;:&quot;Reopened&quot;,&quot;description&quot;:&quot;This issue was once resolved, but the resolution was deemed incorrect. From here issues are either marked assigned or resolved.&quot;,&quot;iconUrl&quot;:&quot;/jira/images/icons/statuses/reopened.png&quot;,&quot;statusCategory&quot;:{&quot;id&quot;:4,&quot;key&quot;:&quot;indeterminate&quot;,&quot;colorName&quot;:&quot;yellow&quot;}}\">Reopened</option>"
                + "<option class=\"imagebacked\" data-icon=\"/jira/images/icons/statuses/resolved.png\" value=\"5\" title=\"Resolved\" data-simple-status=\"{&quot;id&quot;:&quot;5&quot;,&quot;name&quot;:&quot;Resolved&quot;,&quot;description&quot;:&quot;A resolution has been taken, and it is awaiting verification by reporter. From here issues are either reopened, or are closed.&quot;,&quot;iconUrl&quot;:&quot;/jira/images/icons/statuses/resolved.png&quot;,&quot;statusCategory&quot;:{&quot;id&quot;:3,&quot;key&quot;:&quot;done&quot;,&quot;colorName&quot;:&quot;green&quot;}}\">Resolved</option>"
                + "<option class=\"imagebacked\" data-icon=\"/jira/images/icons/statuses/closed.png\" value=\"6\" title=\"Closed\" data-simple-status=\"{&quot;id&quot;:&quot;6&quot;,&quot;name&quot;:&quot;Closed&quot;,&quot;description&quot;:&quot;The issue is considered finished, the resolution is correct. Issues which are closed can be reopened.&quot;,&quot;iconUrl&quot;:&quot;/jira/images/icons/statuses/closed.png&quot;,&quot;statusCategory&quot;:{&quot;id&quot;:3,&quot;key&quot;:&quot;done&quot;,&quot;colorName&quot;:&quot;green&quot;}}\">Closed</option>"
                + "<option class=\"imagebacked\" data-icon=\"/jira/images/icons/statuses/otherstatus.png\" value=\"7\" title=\"Other Status\">Other Status</option>"
                + "</select>").appendTo("#qunit-fixture");

        var control = new AJS.CheckboxMultiSelectStatusLozenge({
            element: $select,
            stallEventBind: false
        });

        function getAllSuggestions(group) {
            if (!group) {
                return jQuery("#searcher-status-suggestions li.check-list-item");
            } else {
                return jQuery("#searcher-status-suggestions").find(group).find("li.check-list-item");
            }
        }

        function getSuggestions(text, group) {
            return getAllSuggestions(group).filter(":contains(" + text + ")");
        }

        this.tester = {
            type: function (str) {
                control.$field.focus().val(str).trigger("input");
                return this;
            }
        };

        this.assert = {
            suggestions: function () {
                equal(getAllSuggestions().length, arguments.length, "Expected " + arguments.length + " suggestions present");
                _.each(arguments, function (option) {
                    ok(getSuggestions(option).length === 1, "Expected suggestion \"" + option + "\" to be present");
                });
            },
            allSuggestions: function () {
                this.suggestions("Open", "In Progress", "Reopened", "Resolved", "Closed", "Other Status");
            },
            lozengesNumber: function(number) {
                var $lozenges = getAllSuggestions().find(".aui-lozenge");
                equal($lozenges.length, number, "Expected "+number+" lozenges to be present");
            },
            nonLozengesItemsNumber: function(number) {
                var $items = getAllSuggestions().find("input[type=checkbox] ~ :not(.aui-lozenge)")
                equal($items.length, number, "Expected "+number+" non-lozenge items to be present");
            }
        };
    },
    teardown: function () {
        jQuery("#searcher-status-multi-select").remove();
        jQuery("#searcher-status").remove();
    }
});

test("correct init state", function () {
    this.assert.allSuggestions();
    this.assert.lozengesNumber(5);
    this.assert.nonLozengesItemsNumber(1);
});

test("basic querying", function () {
    this.tester.type("O");
    this.assert.suggestions("Open", "Other Status");
});
