/**
 *
 * PLEASE READ!!!!!!
 *
 * These are not 'Standard Unit Tests'. They apply black box testing at a method level (http://en.wikipedia.org/wiki/Black-box_testing).
 *
 * Basically we are testing calls to the methods of this class, and asserting their output is correct. This is different
 * to conventional unit testing as we are NOT mocking out all the methods of this class.
 */


AJS.test.require("jira.webresources:select-pickers");
AJS.test.require("com.atlassian.auiplugin:ajs-underscorejs");
AJS.test.require("jira.webresources:key-commands");

module("SingleSelect", {
    setup: function() {
        this.$auiForm = jQuery("<form class='aui'></form>").css({width: 300}).appendTo("#qunit-fixture");
        this.$select = jQuery("<select><option value='1'>One</option><option selected='selected' value='2'>Two</option></select>").appendTo("#qunit-fixture");
    }
});

test("constructor", function () {


    var initializedFired = false,
        mySelect;

    this.$select.bind("initialized", function () {
        initializedFired = true;
    });

    mySelect = new AJS.SingleSelect({
        element: this.$select,
        width: 200
    });

    // events
    ok(initializedFired, "Expected initialized event to fire");

    // dom
    ok(!this.$select.is(":visible"), "Expected <select> to be hidden");
    ok(!mySelect.$errorMessage.is(":visible"), "Expected error message to be hidden");
    ok(mySelect.$dropDownIcon.hasClass("drop-menu"), "Expected dropdown menu");
    equal(mySelect.$field.val(), "Two");

});

test("width", function () {
    var mySelect = new AJS.SingleSelect({
        element: this.$select,
        width: 210
    });
    mySelect.$container.css("display","inline-block");
    equal(mySelect.$container.css("max-width"), "210px", "the container's maximum width is constrained by setting width in the constructor");
    notEqual(mySelect.$container.width(), 210, "the container will not be 210px initially");

    // Set the width to the width of the AUI form.
    var formWidth = this.$auiForm.width();
    mySelect.$container.width(formWidth + 100);
    equal(mySelect.$container.width(), 210, "the container is constrained by its max-width to 210px");

    mySelect.setFieldWidth(formWidth);
    equal(mySelect.$container.css("max-width"), formWidth + "px", "the max-width is affected by setFieldWidth");
    equal(mySelect.$container.width(), formWidth, "the container only grows as large as its max-width allows");
});


test("getSelectedDescriptor", function () {

    var mySelect = new AJS.SingleSelect({
        element: this.$select
    });

    equal(mySelect.getSelectedDescriptor().value(), "2", "Expected selected descriptor to have a value of 2");
});

asyncTest("Down arrow key opens suggestions", function () {
    var singleSelect = new AJS.SingleSelect({
        element: this.$select
    });
    setTimeout(function() {
        var keydown = new jQuery.Event("keydown");
        keydown.keyCode = 40; // Down arrow
        keydown.which = 40;
        singleSelect.$field.trigger(keydown);
        ok(singleSelect.dropdownController.isVisible(), "Dropdown layer is visible");
        start();
    }, 200);
});

test("getDisplayVal", function () {

    var mySelect = new AJS.SingleSelect({
            element: this.$select,
            itemAttrDisplayed: "value"
        }),
        descriptor = new AJS.ItemDescriptor({
            value: "Scott",
            label: "Scott's label"
        });


    equal(mySelect.getDisplayVal(descriptor), "Scott", "Expected the value to be display value");
});


test("getQueryVal", function () {

     var mySelect = new AJS.SingleSelect({
            element: this.$select,
            itemAttrDisplayed: "value"
        });

    mySelect.$field.val("test");

    equal(mySelect.getQueryVal(), "", "Expected query value to be '' when not in editing mode"); // not in editing mode

    mySelect._setEditingMode();

    equal(mySelect.getQueryVal(), "test", "Expected query value to be 'test' when in editing mode");
});


test("setSelection", function () {

    var selectedEventFired = false,
        mySelect = new AJS.SingleSelect({
            element: this.$select,
            itemAttrDisplayed: "value"
        }),
        descriptor = this.$select.find(":selected").data("descriptor");

    ok(this.$select.find(":selected").data("descriptor") == descriptor);

    descriptor = this.$select.find("option:first").data("descriptor");

    this.$select.bind("selected", function () {
        selectedEventFired = true;
    });

    mySelect.setSelection(descriptor);

    ok(!mySelect.$container.hasClass("aui-ss-editing"), "expected field to be in read mode (not editing)");

    ok(selectedEventFired, 'Expected [select] event to fire');
    ok(this.$select.find(":selected").data("descriptor") == descriptor, "Selected descriptor did not change");
});

test("setSelection - anti XSS test", function () {

    var selectedEventFired = false,
            mySelect = new AJS.SingleSelect({
                element: this.$select,
                itemAttrDisplayed: "value"
            }),
            descriptor = this.$select.find(":selected").data("descriptor");


    var xssCallApi = { xssCall : function(){} };
    var mock = sinon.mock(xssCallApi);
    mock.expects("xssCall").never();
    window.xssCall = xssCallApi.xssCall;

    descriptor.icon = function(){
        return "genericissue.png\"><script>window.xssCall()</script>";
    }

    mySelect.setSelection(descriptor);

    window.xssCall = null;
    expect(0);
});

test("clearSelection", function () {

    var unselectEventFired = false,
        mySelect = new AJS.SingleSelect({
            element: this.$select,
            itemAttrDisplayed: "value"
        });


    this.$select.bind("unselect", function () {
        unselectEventFired  = true;
    });


    mySelect.clearSelection();

    equal(this.$select.find(":selected").length, 0, "Expected no selected <option>s");

    ok(unselectEventFired, "Expected unselect event to fire");
});


test("_handleEdit", function () {

    var mySelect = new AJS.SingleSelect({
        element: this.$select,
        itemAttrDisplayed: "value"
    });

    mySelect.onEdit({});
    mySelect.$field.trigger("keyup");

    equal(this.$select.find(":selected").length, 0, "Expected any editing to unselect selected");
});

test("handleFreeInput", function () {

    var $select = jQuery("<select><option value='fred'>Fred</option><option value='fred2'>Fred</option></select>")
            .appendTo("#qunit-fixture"),
        mySelect = new AJS.SingleSelect({
        element: $select,
        itemAttrDisplayed: "value"
    });


    mySelect.$container.addClass("aui-ss-editing");
    mySelect.$field.val("Fred");

    mySelect.handleFreeInput();


    equal($select.val(), "fred");


    $select = jQuery("<select><option value='fred'>Fred</option><option value='fred2' selected='selected'>Fred</option></select>")
            .appendTo("#qunit-fixture");

    mySelect = new AJS.SingleSelect({
        element: $select,
        itemAttrDisplayed: "value"
    });

    mySelect.handleFreeInput();

    equal($select.val(), "fred2");
});

test("reversion of invalid selections when user types freely in control", function() {
    this.clock = this.sandbox.useFakeTimers();

    var $select = jQuery("<select><option value='fred' selected='selected'>Fred</option><option value='john'>Johnny</option></select>")
            .appendTo("#qunit-fixture"),
        mySelect = new AJS.SingleSelect({
        element: $select,
        revertOnInvalid: true
    });

    this.clock.tick(20); // to bind the SingleSelect's events to the field.

    equal($select.val(), "fred", "selection should initially be Fred");

    mySelect.$field.val("Johnny");
    mySelect.$field.trigger("input"); // to trigger the edit mode of SingleSelect
    mySelect.$field.trigger("blur"); // user navs away from field.

    equal($select.val(), "john", "selection changed to Johnny based on typing");
    equal(mySelect.$field.val(), "Johnny");

    mySelect.$field.val("Bob");
    mySelect.$field.trigger("input"); // to trigger the edit mode of SingleSelect
    mySelect.$field.trigger("blur"); // user navs away from field.

    equal($select.val(), "john", "The value should stay on the valid one.");
});

test("fires a change event when reverting an invalid selection", function() {
    this.clock = this.sandbox.useFakeTimers();

    var spy = this.spy();
    var $select = jQuery("<select><option value='fred' selected='selected'>Fred</option><option value='john'>John</option></select>")
            .appendTo("#qunit-fixture"),
        mySelect = new AJS.SingleSelect({
        element: $select,
        revertOnInvalid: true
    });

    this.clock.tick(20); // to bind the SingleSelect's events to the field.

    mySelect.$field.on("change", spy);

    mySelect.$field.val("Bob");
    mySelect.$field.trigger("input"); // to trigger the edit mode of SingleSelect
    mySelect.$field.trigger("blur"); // user navs away from field.

    equal($select.val(), "fred", "The value should stay on the valid one.");
    equal(spy.callCount, 1, "the change event was fired");
});

test("JRA-26756: Customware issue", function () {

    var html = "<select id=\"tempo-report-search-picker\" class=\"tempo-single-search-picker\" name=\"search_filter\" data-container-class=\"medium\" data-input-text=\"\">\n"
            + "    <optgroup id=\"tempo-report-search-suggested\"\n"
            + "              data-weight=\"0\">\n"
            + "        <option value=\"10320\"\n"
            + "                selected=\"selected\"\n"
            + "                data-field-text=\"Hermione&#39;s Ž private filter\"\n"
            + "                data-field-label=\"Hermione&#39;s Ž private filter\">Hermione&#39;s Ž private filter\n"
            + "        </option>\n"
            + "    </optgroup>\n"
            + "</select>";

    var oldAjax = jQuery.ajax;

    jQuery.ajax = function (options) {
        options.success({}, "success", {status: 200});
        options.complete({status: 200}, "success", {status: 200});
    };

    var invalidDescriptor = new AJS.ItemDescriptor({
        value: "-2" , // value of item added to select
        label: "Invalid Worklogs", // title of lozenge
        allowDuplicate: false
    });

    var $select = jQuery(html).appendTo("#qunit-fixture"),
        mySelect = new AJS.SingleSelect({
            element: $select,
            showDropdownButton: true,
            removeOnUnSelect: true,
            submitInputVal: true,
            noQueryNoRequest: false,
            ajaxOptions: {
                query: true,
                url: "blah",
                formatResponse: function () {
                    var optgroup1 = new AJS.GroupDescriptor({
                        weight: 0, // order or groups in suggestions dropdown
                        id: "tempo-report-project-search-0",
                        replace: true,
                        label: "Standard"
                    });

                    optgroup1.addItem(invalidDescriptor);

                    var optgroup2 = new AJS.GroupDescriptor({
                        weight: 1, // order or groups in suggestions dropdown
                        id: "tempo-report-project-search-1",
                        replace: true,
                        label: "Filters"
                    });

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10021",
                        label: "%3a%2f%3f%23%5b%5d%40%21%24%26%27%28%29%2a%2b%2c%3b%3d",
                        allowDuplicate: false
                    }));

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10012",
                        label: "&",
                        allowDuplicate: false
                    }));

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10020",
                        label: "<SCRIPT SRC=http://ha.ckers.org/xss.js></SCRIPT>",
                        allowDuplicate: false
                    }));

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10022",
                        label: "\x3cscript src=http://www.example.com/malicious-code.js\x3e\x3c/script\x3e",
                        allowDuplicate: false
                    }));

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10016",
                        label: "Bugs",
                        allowDuplicate: false
                    }));

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10321",
                        label: "bulk rapid",
                        allowDuplicate: false
                    }));

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10320",
                        label: "Hermione's Ž private filter",
                        allowDuplicate: false
                    }));

                    optgroup2.addItem(new AJS.ItemDescriptor({
                        value: "10121",
                        label: "Hermione_filter",
                        allowDuplicate: false
                    }));

                    return [optgroup1, optgroup2];
                }
            }
        });

    mySelect.$field.focus();
    mySelect.$dropDownIcon.click();
    mySelect.setSelection(invalidDescriptor); // Will blow up here if issue still exists
    mySelect.setSelection(invalidDescriptor); // Will blow up here if issue still exists
    //use == instead of equal because of circular JSON serialization problem
    ok(invalidDescriptor==mySelect.getSelectedDescriptor());

    jQuery.ajax = oldAjax;
});

test("triggering show/hide event shows/hide suggestions", function () {
    new AJS.SingleSelect({
        element: this.$select
    });
    this.$select.trigger("showSuggestions");
    ok(AJS.InlineLayer.current, "Expected suggestions to show");
    this.$select.trigger("hideSuggestions");
    ok(!AJS.InlineLayer.current, "Expected suggestions to hide");
});

module("SingleSelect with placeholder", {
    setup: function() {
        this.$auiForm = jQuery("<form class='aui'></form>").css({width: 300}).appendTo("#qunit-fixture");
        this.$select = jQuery("<select><option value='1'>One</option><option selected='selected' value='2'>Two</option></select>").appendTo("#qunit-fixture");
        this.$placeholderOption = jQuery("<option data-placeholder='true'>Select an option</option>");
        this.$select.append(this.$placeholderOption);
    }
});

test("the input field gets an html5 placeholder attribute", function() {
    var ss = new AJS.SingleSelect({
        element: this.$select
    });

    equal(ss.$field.attr("placeholder"), this.$placeholderOption.text(), "should use the text from the <option> marked as a placeholder");
});

test("placeholders shouldn't stay in the selectable options", function() {
    equal(this.$select.find(this.$placeholderOption).length, 1);

    new AJS.SingleSelect({
        element: this.$select
    });

    equal(this.$select.find(this.$placeholderOption).length, 0);
});

test("Reverts to empty value if placeholder value initially selected", function() {
    this.clock = this.sandbox.useFakeTimers();

    this.$select.find("option").attr("selected", false);
    this.$placeholderOption.attr("selected", true).val("invalid");

    var ss = new AJS.SingleSelect({
        element: this.$select,
        revertOnInvalid: true
    });

    this.clock.tick(20); // to bind the SingleSelect's events to the field.

    equal(this.$select.val(), null, "selection should not be set");
    equal(ss.model.getValue(), null, "value should not be set");
    equal(ss.$field.val(), "", "the field has no text in it");

    ss.$field.val("Seven");
    ss.$field.trigger("input"); // to trigger the edit mode of SingleSelect
    ss.$field.trigger("blur"); // user navs away from field.

    equal(ss.model.getValue(), "", "value should be empty");
    equal(ss.$field.val(), "", "the field has no text in it");

    // An interesting implementation detail...
    var $selected = this.$select.find(":selected");
    equal($selected.length, 1, "an interesting new empty option should appear");
    equal($selected.val(), "", "the new option has an empty value");
    equal(this.$select.find($selected).length, 1);

    // ... better make sure that disappears when we select a valid value
    ss.$field.val("One");
    ss.$field.trigger("input"); // to trigger the edit mode of SingleSelect
    ss.$field.trigger("blur"); // user navs away from field.

    equal(this.$select.find($selected).length, 0, "it's gone now");
});
