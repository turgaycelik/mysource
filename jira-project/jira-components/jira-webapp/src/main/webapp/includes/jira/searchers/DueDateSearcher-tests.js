AJS.test.require("jira.webresources:jira-global");
AJS.test.require("jira.webresources:searchers");

module('JIRA.DueDateSearcher', {
    setup: function() {
        var that = this,
            $el,
            $from,
            $to,
            $dateStart,
            $dateEnd;

        function setup(from, to, startDate, endDate) {
            $el && $el.remove(); // cleanup
            that.$el = $el = jQuery("<div data-field-name=\"my-date\" class=\"js-date-searcher\">\n"
                    + "    <input type=\"text\" class=\"js-date-picker-start-date\" name=\"duedate:after\" />\n"
                    + "    <input type=\"text\" class=\"js-date-picker-end-date\" name=\"duedate:before\"/>\n"
                    + "    <input type=\"text\" class=\"js-date-picker-from\" name=\"duedate:previous\"/>\n"
                    + "    <input type=\"text\" class=\"js-date-picker-to\" name=\"duedate:next\"/>\n"
                    + "</div>").appendTo("body");

            $from = $el.find(".js-date-picker-from").val("");
            $to = $el.find(".js-date-picker-to").val("");
            $dateStart = $el.find(".js-date-picker-start-date").val("");
            $dateEnd = $el.find(".js-date-picker-end-date").val("");

            if (typeof from !== "undefined") {
                $from.val(from);
            }
            if (typeof to !== "undefined") {
                $to.val(to);
            }
            if (typeof startDate !== "undefined") {
                $dateStart.val(startDate);
            }
            if (typeof endDate !== "undefined") {
                $dateEnd.val(endDate);
            }
        }

        this.DATE_TYPES = {
            nowOverdue: "nowOverdue",
            dueInNext: "dueInNext",
            dateBetween: "dateBetween",
            moreThan: "moreThan",
            inRange: "inRange",
            withinTheLast: "withinTheLast"
        };

        this.tester = {
            setupDueDate: function (from, to, startDate, endDate) {
                setup(from, to, startDate, endDate);
                JIRA.DateSearcher.createDueDateSearcher($el[0]);
            },
            setup: function (from, to, startDate, endDate) {
                setup(from, to, startDate, endDate);
                JIRA.DateSearcher.createCustomDateSearcher($el[0]);
            },
            setFields: function (type) {
                var values = Array.prototype.slice.call(arguments);
                values = values.slice(1, values.length);
                var $group = $el.find(".field-group[data-date-type='" + type + "']");
                $group.find(":input:not(:radio)").each(function (i) {
                    jQuery(this).val(values[i]);
                });
                $group.find(":radio").prop("checked", true).trigger("change");
            }
        };

        this.assert = {
            noRadioChecked: function () {
                ok($el.find(":radio[checked]").length === 0,
                        "Expected no radio button to be checked");
            },
            noTextValues: function () {
                $el.find(":text").each(function () {
                    if (jQuery(this).val()) {
                        equal("", jQuery(this).val());
                    }
                });
            },
            fieldValues: function (type) {
                var args = Array.prototype.slice.call(arguments);
                args = args.slice(1, args.length);
                var values = [];
                $el.find(".field-group[data-date-type='" + type + "']").find(":input:not(:radio)").each(function () {
                    values.push(this.value);
                });
                equal(JSON.stringify(values), JSON.stringify(args));

            },
            radioChecked: function (type) {
                ok($el.find(".field-group[data-date-type='" + type + "']").find(":radio[checked]").length === 1,
                        "Expected radio for dat type [" + type + "] to be checked");
                ok($el.find(":radio[checked]").length === 1, "No other radios should be checked at the same time")
            },
            submitVals: function (from, to, startDate, endDate) {
                equal($from.val(), from, "Expected 'from' value to be [" + from + "] but was [" + $from.val() + "]");
                equal($to.val(), to, "Expected 'to' value to be [" + to + "] but was [" + $to.val() + "]");
                equal($dateStart.val(), startDate, "Expected 'dateStart' value to be [" + startDate + "] but was [" + $dateStart.val() + "]");
                equal($dateEnd.val(), endDate, "Expected 'dateStart' value to be [" + endDate + "] but was [" + $dateEnd.val() + "]");
            }
        };

        sinon.stub(AJS, "format", function () {
            return Array.prototype.join.call(arguments, "")
        });

    },
    teardown: function () {
        this.$el.remove();
        AJS.format.restore();
    }
});

test("Restoring: no state", function () {
    this.tester.setup();
    this.assert.noRadioChecked();
    this.assert.noTextValues();
});

test("Restoring: more than", function () {
    this.tester.setup(undefined, "-5d");
    this.assert.radioChecked(this.DATE_TYPES.moreThan);
    this.assert.fieldValues(this.DATE_TYPES.moreThan, "5", "d");
    this.tester.setup(undefined, "-10w");
    this.assert.fieldValues(this.DATE_TYPES.moreThan, "10", "w");
    this.tester.setup(undefined, "-fdgsfdmm");
    this.assert.fieldValues(this.DATE_TYPES.moreThan, "fdgsfdm", "m");
    this.tester.setup(undefined, "--10w");
    this.assert.fieldValues(this.DATE_TYPES.moreThan, "-10", "w");
    this.tester.setup(undefined, "-mmm");
    this.assert.fieldValues(this.DATE_TYPES.moreThan, "mm", "m");
});

test("Restoring: withinTheLast", function () {
    this.tester.setup("-5d");
    this.assert.radioChecked(this.DATE_TYPES.withinTheLast);
    this.assert.fieldValues(this.DATE_TYPES.withinTheLast, "5", "d");
});

test("Restoring: date between", function () {
    this.tester.setup(undefined, undefined, "14/Dec/12", "15/Dec/12");
    this.assert.radioChecked(this.DATE_TYPES.dateBetween);
    this.assert.fieldValues(this.DATE_TYPES.dateBetween, "14/Dec/12", "15/Dec/12");
    this.tester.setup(undefined, undefined, undefined, "16/Dec/12");
    this.assert.fieldValues(this.DATE_TYPES.dateBetween, "", "16/Dec/12");
    this.tester.setup(undefined, undefined, "17/Dec/12");
    this.assert.fieldValues(this.DATE_TYPES.dateBetween, "17/Dec/12", "");
});

test("Restoring: range", function () {
    this.tester.setup("-5h", "5h");
    this.assert.radioChecked(this.DATE_TYPES.inRange);
    this.assert.fieldValues(this.DATE_TYPES.inRange, "-5h", "5h");
    this.tester.setup("3m");
    this.assert.radioChecked(this.DATE_TYPES.inRange);
    this.assert.fieldValues(this.DATE_TYPES.inRange, "3m", "");
});

test("Setting: more than", function () {
    this.tester.setup();
    this.tester.setFields(this.DATE_TYPES.moreThan, 5, "d");
    this.assert.submitVals("", "-5d", "", "");
    this.tester.setFields(this.DATE_TYPES.moreThan, 10, "w");
    this.assert.submitVals("", "-10w", "", "");
});

test("Setting: withinTheLast", function () {
    this.tester.setup();
    this.tester.setFields(this.DATE_TYPES.withinTheLast, 5, "d");
    this.assert.submitVals("-5d", "", "", "");
    this.tester.setFields(this.DATE_TYPES.withinTheLast, 10, "w");
    this.assert.submitVals("-10w", "", "", "");
});

test("Setting: date between", function () {
    this.tester.setup(undefined, undefined, "14/08/2012", "15/08/2012");
    this.tester.setFields(this.DATE_TYPES.dateBetween, "16/08/2012", "17/08/2012");
    this.assert.submitVals("", "", "16/08/2012", "17/08/2012");
    this.tester.setFields(this.DATE_TYPES.dateBetween, "", "19/08/2012");
    this.assert.submitVals("", "", "", "19/08/2012");
    this.tester.setFields(this.DATE_TYPES.dateBetween, "20/08/2012", "");
    this.assert.submitVals("", "", "20/08/2012", "");
});

test("Setting: range", function () {
    this.tester.setup("-5h", "5h");
    this.tester.setFields(this.DATE_TYPES.inRange, "-20h", "17w");
    this.assert.submitVals("-20h", "17w", "", "");
});





// Due Date Picker
test("Restoring Due Date: no state", function () {
    this.tester.setupDueDate();
    this.assert.noRadioChecked();
    this.assert.noTextValues();
});

test("Restoring Due Date: now overdue", function () {
    this.tester.setupDueDate(undefined, 0);
    this.assert.radioChecked(this.DATE_TYPES.nowOverdue);
});

test("Restoring Due Date: more than", function () {
    this.tester.setupDueDate(undefined, "-5d");
    this.assert.radioChecked(this.DATE_TYPES.moreThan);
    this.assert.fieldValues(this.DATE_TYPES.moreThan, "5", "d");
    this.tester.setupDueDate(undefined, "-10w");
    this.assert.fieldValues(this.DATE_TYPES.moreThan, "10", "w");
    this.tester.setupDueDate(undefined, "-fdgsfdm");
    this.assert.fieldValues(this.DATE_TYPES.moreThan, "fdgsfd", "m");
});

test("Restoring Due Date: due in next", function () {
    this.tester.setupDueDate(undefined, "5d");
    this.assert.radioChecked(this.DATE_TYPES.dueInNext);
    this.assert.fieldValues(this.DATE_TYPES.dueInNext, "5", "d", "orIs");

    this.tester.setupDueDate(undefined, "10w");
    this.assert.fieldValues(this.DATE_TYPES.dueInNext, "10", "w", "orIs");

    this.tester.setupDueDate("0", "10w");
    this.assert.fieldValues(this.DATE_TYPES.dueInNext, "10", "w", "andNot");

    this.tester.setupDueDate(undefined, "fdgsfdsm");
    this.assert.fieldValues(this.DATE_TYPES.dueInNext, "fdgsfds", "m", "orIs");

});

test("Restoring Due Date: date between", function () {
    this.tester.setupDueDate(undefined, undefined, "14/Dec/12", "15/Dec/12");
    this.assert.radioChecked(this.DATE_TYPES.dateBetween);
    this.assert.fieldValues(this.DATE_TYPES.dateBetween, "14/Dec/12", "15/Dec/12");
    this.tester.setupDueDate(undefined, undefined, undefined, "16/Dec/12");
    this.assert.fieldValues(this.DATE_TYPES.dateBetween, "", "16/Dec/12");
    this.tester.setupDueDate(undefined, undefined, "17/Dec/12");
    this.assert.fieldValues(this.DATE_TYPES.dateBetween, "17/Dec/12", "");
});

test("Restoring Due Date: range", function () {
    this.tester.setupDueDate("-5h", "5h");
    this.assert.radioChecked(this.DATE_TYPES.inRange);
    this.assert.fieldValues(this.DATE_TYPES.inRange, "-5h", "5h");
    this.tester.setupDueDate("3m");
    this.assert.radioChecked(this.DATE_TYPES.inRange);
    this.assert.fieldValues(this.DATE_TYPES.inRange, "3m", "");
});

test("Setting Due Date: now overdue", function () {
    this.tester.setupDueDate(undefined, "-5d");
    this.tester.setFields(this.DATE_TYPES.nowOverdue);
    this.assert.submitVals("", 0, "", "");
});

test("Setting Due Date: more than", function () {
    this.tester.setupDueDate();
    this.tester.setFields(this.DATE_TYPES.moreThan, 5, "d");
    this.assert.submitVals("", "-5d", "", "");
    this.tester.setFields(this.DATE_TYPES.moreThan, 10, "w");
    this.assert.submitVals("", "-10w", "", "");
});

test("Setting: due in next", function () {
    this.tester.setupDueDate(undefined, undefined, "2012/08/14", "2012/08/15");
    this.tester.setFields(this.DATE_TYPES.dueInNext, 5, "d");
    this.assert.submitVals("", "5d", "", "");
    this.tester.setFields(this.DATE_TYPES.dueInNext, 10, "w", "andNot");
    this.assert.submitVals("0", "10w", "", "");
});

test("Setting Due Date: date between", function () {
    this.tester.setupDueDate(undefined, undefined, "14/08/2012", "15/08/2012");
    this.tester.setFields(this.DATE_TYPES.dateBetween, "16/08/2012", "17/08/2012");
    this.assert.submitVals("", "", "16/08/2012", "17/08/2012");
    this.tester.setFields(this.DATE_TYPES.dateBetween, "", "19/08/2012");
    this.assert.submitVals("", "", "", "19/08/2012");
    this.tester.setFields(this.DATE_TYPES.dateBetween, "20/08/2012", "");
    this.assert.submitVals("", "", "20/08/2012", "");
});


test("Setting Due Date: range", function () {
    this.tester.setupDueDate("-5h", "5h");
    this.tester.setFields(this.DATE_TYPES.inRange, "-20h", "17w");
    this.assert.submitVals("-20h", "17w", "", "");
});