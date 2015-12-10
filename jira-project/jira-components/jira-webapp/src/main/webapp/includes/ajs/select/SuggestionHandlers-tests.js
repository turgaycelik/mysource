AJS.test.require("jira.webresources:jira-global");
AJS.test.require("jira.webresources:select-pickers");
AJS.test.require("com.atlassian.auiplugin:ajs-underscorejs");

module("AJS.CheckboxMultiSelectSuggestHandler");

test("Does not show duplicate selected values for empty query", function () {
    var $el = jQuery("<select multiple='true'>" +
            "<optgroup label='stuff'><option value='xxx' selected='true'></option></optgroup>" +
            "<optgroup label='more'><option value='xxx' selected='true'></option></optgroup>" +
            "</select>");
    var model = new AJS.SelectModel({
        element: $el
    });
    var suggestHandler = new AJS.CheckboxMultiSelectSuggestHandler({}, model);

    var descriptors = suggestHandler.formatSuggestions([], "");

    equal(descriptors.length, 1);
    var optGroup = descriptors[0];
    equal(optGroup.items().length, 1);
});

test("Footer text only shows for empty query", function () {
    expect(2);
    var $el = jQuery("<select multiple='true'>" +
            "<optgroup label='stuff'><option value='xxx' selected='true'></option></optgroup>" +
            "</select>");
    var model = new AJS.SelectModel({
        element: $el
    });
    var suggestHandler = new JIRA.AssigneeSuggestHandler({ajaxOptions: {url: "", query: true}}, model);
    suggestHandler.execute("").done(function (descriptors) {
        equal("user.picker.ajax.short.desc", descriptors[0].footerText());
    });
    suggestHandler.execute("a").done(function (descriptors) {
        ok(!descriptors[0].footerText());
    });
});

test("Clear link shows when there are 2 or more suggestions", function () {
    expect(2);
    var $el = jQuery("<select multiple='true'>" +
            "<optgroup label='stuff'><option value='zzz' selected='true'></option><option value='xxx' selected='true'></option></optgroup>" +
            "</select>");
    var model = new AJS.SelectModel({
        element: $el
    });
    var suggestHandler = new AJS.CheckboxMultiSelectSuggestHandler({}, model);

    suggestHandler.execute("").done(function (descriptors) {
        equal(jQuery(descriptors[0].actionBarHtml()).find(".clear-all").length, 1, "expected clear all to be added because there are 2 selected");
    });

    $el = jQuery("<select multiple='true'>" +
            "<optgroup label='stuff'><option value='xxx' selected='true'></option><option value='xxx2'></optgroup>" +
            "</select>");
    model = new AJS.SelectModel({
        element: $el
    });
    suggestHandler = new AJS.CheckboxMultiSelectSuggestHandler({}, model);
    suggestHandler.execute("").done(function (descriptors) {
        equal(jQuery(descriptors[0].actionBarHtml()).find(".clear-all").length, 0, "expected clear all not to be added because there is only 1 selected");
    });
});

test("Queued requests are fired after keyInput period", function () {
    var ajaxDescriptorFetcher = new AJS.AjaxDescriptorFetcher({
        query: true,
        formatResponse: function (data) {
            return new AJS.ItemDescriptor({value: data.correctData, label: data.correctData});
        }});
    var firstRequestSpy = sinon.spy();
    var secondRequestSpy = sinon.spy();
    var clock = sinon.useFakeTimers();
    var server = sinon.fakeServer.create();
    ajaxDescriptorFetcher.execute("a").done(firstRequestSpy);
    ajaxDescriptorFetcher.execute("ab").done(secondRequestSpy);
    clock.tick(300);
    ok(server.requests[0].aborted, "first request should be aborted");
    equal(firstRequestSpy.callCount, 0, "first request should never return.");
    server.requests[1].respond(200, { "Content-Type": "application/json" }, JSON.stringify({correctData: "correctData"}));
    equal(secondRequestSpy.args[0][0].value(), "correctData");
});

test("Preserves search results items even if they already are in the suggestions", function () {
    var $el = jQuery("<select multiple='true'></select>");
    var model = new AJS.SelectModel({
        element: $el
    });

    var server = sinon.fakeServer.create();
    var clock = sinon.useFakeTimers();
    var suggestHandler = new AJS.CheckboxMultiSelectSuggestHandler({
        content: 'mixed',
        ajaxOptions: {
            url: 'fake',
            formatResponse: function (data) {
                return new AJS.ItemDescriptor({value: data.value, label: data.label});
            }
        }
    }, model);

    model.getAllDescriptors = function() {
        return [
            new AJS.ItemDescriptor({value: "val1", label: "Value 1"}),
            new AJS.ItemDescriptor({value: "val2", label: "Value 2"})
        ]
    };
    suggestHandler.execute("query").done(
        function(descriptors) {
            equal(descriptors[0].items()[0].value(), "val1");
            equal(descriptors[0].items()[0].label(), "*Value 1*");

            equal(descriptors[0].items()[1].value(), "val2");
            equal(descriptors[0].items()[1].label(), "Value 2");

            equal(descriptors[0].items().length, 2);
        }
    )
    server.requests[0].respond(200, { "Content-Type": "application/json" }, JSON.stringify({value: "val1", label: "*Value 1*"}));
    clock.tick(300);
});
