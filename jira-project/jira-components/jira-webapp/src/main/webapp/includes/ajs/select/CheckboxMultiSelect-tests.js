AJS.test.require("jira.webresources:jira-global");
AJS.test.require("jira.webresources:select-pickers");
AJS.test.require("com.atlassian.auiplugin:ajs-underscorejs");

module("AJS.CheckboxMultiSelect", {
    setup: function () {

        jQuery.fn.tipsy = function () {};

        var $select = jQuery("<select class=\"select\" data-placeholder-text=\"Find Projects...\" id=\"searcher-pid\" multiple=\"multiple\" name=\"pid\" size=\"5\" style=\"display: none; \">\n"
                + "    <optgroup label=\"All Projects\">\n"
                + "        <option selected=\"selected\" data-icon=\"/jira/secure/projectavatar?pid=10021&amp;size=small\" title=\"Billabong\" value=\"10021\">Billabong</option>\n"
                + "        <option selected=\"selected\" data-icon=\"/jira/secure/projectavatar?pid=10020&amp;size=small\" title=\"Hurley\" value=\"10020\">Hurley</option>\n"
                + "        <option data-icon=\"/jira/secure/projectavatar?pid=10023&amp;size=small\" title=\"Insight\" value=\"10023\">Insight</option>\n"
                + "        <option data-icon=\"/jira/secure/projectavatar?pid=10026&amp;size=small\" title=\"Nike 6.0\" value=\"10026\">Nike 6.0</option>\n"
                + "        <option data-icon=\"/jira/secure/projectavatar?pid=10022&amp;size=small\" title=\"Quiksilver\" value=\"10022\">Quiksilver</option>\n"
                + "        <option data-icon=\"/jira/secure/projectavatar?pid=10028&amp;size=small\" title=\"Ripcurl\" value=\"10028\">Ripcurl</option>\n"
                + "        <option data-icon=\"/jira/secure/projectavatar?pid=10025&amp;size=small\" title=\"Stussy\" value=\"10025\">Stussy</option>\n"
                + "        <option data-icon=\"/jira/secure/projectavatar?pid=10027&amp;size=small\" title=\"Surfrider Foundation\" value=\"10027\">Surfrider Foundation</option>\n"
                + "        <option data-icon=\"/jira/secure/projectavatar?pid=10024&amp;size=small\" title=\"Volcom\" value=\"10024\">Volcom</option>\n"
                + "        <option data-icon=\"/jira/secure/projectavatar?pid=10024&amp;size=small\" title=\"Volcom\" value=\"10088\">Volly</option>\n"
                + "    </optgroup>\n"
                + "</select>").appendTo("body");

        var control = new AJS.CheckboxMultiSelect({
            element: $select,
            stallEventBind: false
        });




        function getAllSuggestions(group) {
            if (!group) {
                return jQuery("#searcher-pid-suggestions li.check-list-item");
            } else {
                return jQuery("#searcher-pid-suggestions").find(group).find("li.check-list-item");
            }
        }

        function getSuggestions(text, group) {
            return getAllSuggestions(group).filter(":contains(" + text + ")");
        }

        function getNoSuggestions() {
            return jQuery("#searcher-pid-suggestions li.no-suggestions");
        }

        function getOption(text) {
            return $select.find("option:contains(" + text + ")");
        }

        function getQueryVal() {
            return control.getQueryVal();
        }

        function simulateKey (key) {
            var specialKeys = {38: "Up", 40: "Down", 13: "Return", 8: "Backspace"},
                isChar = typeof key === "string";

            if (!isChar) {
                key = specialKeys[key];
            }

            var event = new jQuery.Event("aui:keydown");
            event.key = key;
            control.$field.trigger(event);

            if (isChar || key === "Backspace") {
                event = new jQuery.Event("aui:keypress");
                event.key = key;
                control.$field.trigger(event);
            }
            if (isChar) {
                control.$field.val(control.$field.val() + key);
            }
            if (key === "Backspace") {
                control.$field.val(control.$field.val().substring(0, control.$field.val().length-1));
            }
            control.$field.trigger("keyup");
        }

        this.tester = {
            type: function (str) {
                control.$field.focus().val(str).trigger("input");
                return this;
            },
            down: function () {
                control.$field.focus();
                simulateKey(40);
                return this;
            },
            up: function () {
                control.$field.focus();
                simulateKey(38);
                return this;
            },
            accept: function () {
                control.$field.focus();
                simulateKey(13);
                return this;
            },
            clear: function () {
                return this.type("");
            },
            selectByText: function(text) {
                getSuggestions(text).find("input").trigger("change");
            },
            clearSelection: function() {
                $select.trigger("clear");
            },
            clearQueryField: function() {
                control.$dropDownIcon.click();
            }
        };

        this.GROUPS = {
            selectedProjects: ".selected-group"
        };

        this.assert = {
            noneSelected: function() {
                equal(getAllSuggestions().find(":checked").length, 0, "No suggestion is checked");
                equal($select.val(), null, "<select> has empty value");
            },
            selected: function (group) {
                var expectedVal = [];
                _.each(arguments, function (option) {
                    var $li = getSuggestions(option);
                    if ($li.length) {
                        ok($li.find(":checked").length === 1, "Expected suggestion \"" + option + "\" to be checked");
                    }
                    var $option = getOption(option);
                    ok($option.is(":selected"), "Expected option \"" + option + "\" to be selected in <select>");
                    expectedVal.push($option.val());
                });
                equal(JSON.stringify($select.val()), JSON.stringify(expectedVal));
            },
            selectedInGroup: function (group, options) {
                var expectedVal = [];
                _.each(options, function (option) {
                    var $li = getSuggestions(option, group);
                    if ($li.length) {
                        ok($li.find(":checked").length === 1, "Expected suggestion \"" + option + "\" to be checked");
                    }
                    var $option = getOption(option);
                    ok($option.is(":selected"), "Expected option \"" + option + "\" to be selected in <select>");
                    expectedVal.push($option.val());
                });
                equal(JSON.stringify($select.val()), JSON.stringify(expectedVal));
            },
            suggestions: function () {
                equal(getAllSuggestions().length, arguments.length, "Expected " + arguments.length + " suggestions present");
                _.each(arguments, function (option) {
                    ok(getSuggestions(option).length === 1, "Expected suggestion \"" + option + "\" to be present");
                });
            },
            noSuggestions: function () {
                equal(getAllSuggestions().length, 0, "Expected no suggestions to be present");
                equal(getNoSuggestions().length, 1, "Expected the 'No Matches' hint to be present");
            },
            bolded: function () {
                var $bolded = getAllSuggestions().find("em");
                equal($bolded.length, arguments.length, "Expected " + arguments.length + " to be bolded");
                _.each(arguments, function (boldedText, i) {
                    equal(AJS.$.trim($bolded.eq(i).text()), boldedText, "Expected text [" + boldedText + "] to be bolded");
                });

            },
            allSuggestions: function () {
                this.suggestions("Billabong", "Hurley", "Quiksilver", "Insight", "Nike 6.0", "Ripcurl", "Stussy", "Surfrider Foundation", "Volcom", "Volly");
            },
            clearQueryIcon: function(visible) {
                equal(!!getQueryVal(), visible, "Expected clear query icon to be visible when the query field has text in it");
            },
            emptyQueryField: function() {
                ok(!getQueryVal(), "Expected query field to be empty");
            }
        };
    },
    teardown: function () {
        jQuery("#searcher-pid-multi-select").remove();
        jQuery("#searcher-pid").remove();
    }
});

test("correct init state", function () {
    this.assert.selectedInGroup(this.GROUPS.selectedProjects, ["Billabong", "Hurley"]);
    this.assert.allSuggestions();
});

test("selecting/unselecting options", function () {
    this.tester.down().accept().down().accept();
    this.assert.selected("Billabong", "Insight");
    this.tester.accept().down().accept().down().accept();
    this.assert.selected("Billabong", "Nike 6.0", "Quiksilver");
});

test("Querying", function () {
    this.tester.type("vol");
    this.assert.suggestions("Volcom", "Volly");
    this.assert.bolded("Vol", "Vol");
    this.tester.type("xxx");
    this.assert.noSuggestions();
    this.assert.bolded(); // assert non bolded
    this.tester.clear();
    this.assert.allSuggestions();
    this.assert.bolded(); // assert non bolded
});

test("Selecting other than highlighted selects only selected", function () {
    this.tester.down().down().down();
    this.tester.selectByText("Insight");
    this.assert.selected("Billabong", "Hurley", "Insight");
});


test("Selected group is restored when no input value", function () {
    this.tester.type("blah").clear();
    this.assert.selectedInGroup(this.GROUPS.selectedProjects, ["Billabong", "Hurley"]);
});

test("Clearing all selections", function() {
    this.tester.clearSelection();
    this.assert.noneSelected();
});

test("Clicking clear query icon when there is text removes all text", function() {
    this.assert.clearQueryIcon(false);
    this.tester.type("vol");
    this.assert.clearQueryIcon(true);
    this.tester.clearQueryField();
    this.assert.clearQueryIcon(false);
    this.assert.emptyQueryField();
});
