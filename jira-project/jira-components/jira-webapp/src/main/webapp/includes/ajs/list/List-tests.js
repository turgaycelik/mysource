AJS.test.require("jira.webresources:list", function() {

require([
    'jira/ajs/list/list',
    'jira/ajs/list/item-descriptor',
    'jquery'
], function(
    List,
    ItemDescriptor,
    jQuery
) {

    module("AJS.List", {
        buildList: function() {
            var $fixture = jQuery("#qunit-fixture");
            var options = {
                containerSelector: $fixture,
                delegateTarget: $fixture,
                stallEventBind: false,
                expandAllResults: true
            };

            var clientOptions = {};
            var postActions = [];

            return {
                withMaxResultsDisplayed: function(max) {
                    clientOptions.maxInlineResultsDisplayed = max;
                    return this;
                },
                withExpandAllResults: function(expandAllResults) {
                    clientOptions.expandAllResults = expandAllResults;
                    return this;
                },
                withTotalResults: function(total) {
                    postActions.push(function(list){
                        var data = [];
                        for (var i =0; i<total; i++) {
                            data.push(new ItemDescriptor({highlighted: true, label: "Label #"+i, title: "Title #"+i }));
                        }
                        list.generateListFromJSON(data, "Label");
                    });
                    return this;
                },
                withData: function(data) {
                    postActions.push(function(list){
                        list.generateListFromJSON(data, "Label");
                    });
                    return this;
                },
                withEventsEnabled: function() {
                    postActions.push(function(list) {
                        list.enable();
                    });
                    return this;
                },
                withScrollAtItem: function(item) {
                    postActions.push(function() {
                        var keyEvent = jQuery.Event("keydown");
                        keyEvent.which = jQuery.ui.keyCode.DOWN;

                        for (var i = 0; i<item; i++) {
                            $fixture.trigger(keyEvent);
                        }
                    });
                    return this;
                },
                build: function() {
                    var list = new List(_.defaults(clientOptions, options));
                    _.each(postActions, function(action) {
                        action(list);
                    });
                    return list;
                }
            }
        },

        pressDownArrowOn: function(element) {
            var keyEvent = jQuery.Event("keydown");
            keyEvent.which = jQuery.ui.keyCode.DOWN;
            element.trigger(keyEvent);
        }
    });

    test("List should use title from AJS.ItemDescriptor", function () {
        var data = [
            new ItemDescriptor({label: "Label with title", title: "some tile"}),
            new ItemDescriptor({label: "Label without title"}),
            new ItemDescriptor({label: "Label with empty tile", title: ""})
        ];
        this.buildList()
            .withData(data)
            .build();

        var links = jQuery("#qunit-fixture").find(".aui-list-item-link");
        equal(links.length, 3, "There should be three links rendered");
        equal(jQuery(links.get(0)).prop('title'), data[0].title(), "Link have valid title");
        equal(jQuery(links.get(1)).prop('title'), data[1].label(), "Link without title should have title defined as text");
        equal(jQuery(links.get(2)).prop('title'), data[2].label(), "Link with empty title should have title defined as text");
        equal(jQuery(links.get(0)).text(), data[0].label(), "Link have valid text");
        equal(jQuery(links.get(1)).text(), data[1].label(), "Link have valid text");
        equal(jQuery(links.get(2)).text(), data[2].label(), "Link have valid text");
    });

    test("Protect against XSS vulnerability injected in descriptor icon", function () {
        var xssCallApi = { xssCall : function(){} };
        var mock = sinon.mock(xssCallApi);
        mock.expects("xssCall").never();
        window.xssCall = xssCallApi.xssCall;

        var data = [
            new AJS.ItemDescriptor({
                label: "Label with title",
                title: "some tile",
                icon: function(){
                    return "genericissue.png\"><script>window.xssCall()</script>";
                }
            })
        ];

        this.buildList()
            .withData(data)
            .build();

        window.xssCall = null;
        expect(0);
    });

    test("It limits the number of results displayed", function() {
        this.buildList()
            .withMaxResultsDisplayed(10)
            .withTotalResults(20)
            .build();

        var listContainer = jQuery("#qunit-fixture");
        equal(listContainer.find(".aui-list-item-link").length, 10, "Only 10 links are rendered");
    });

    test("It displays a 'View more' link when there is too many results if expandAllResults is true", function() {
        this.buildList()
            .withMaxResultsDisplayed(10)
            .withExpandAllResults(true)
            .withTotalResults(20)
            .build();

        var listContainer = jQuery("#qunit-fixture");
        equal(listContainer.find("button.view-all").length, 1, "There is one 'view-all' button");
    });

    test("It does not display a 'View more' link when there is too many results if expandAllResults is false", function() {
        this.buildList()
            .withMaxResultsDisplayed(10)
            .withExpandAllResults(false)
            .withTotalResults(20)
            .build();

        var listContainer = jQuery("#qunit-fixture");
        equal(listContainer.find("button.view-all").length, 0, "There is not 'view-all' button");
    });

    test("'View more' expand the list so it contains all the results", function() {
        this.buildList()
            .withMaxResultsDisplayed(10)
            .withTotalResults(20)
            .build();

        var listContainer = jQuery("#qunit-fixture");
        listContainer.find("button.view-all").click();

        equal(listContainer.find(".aui-list-item-link").length, 20, "All the results are rendered");
    });

    test("In a limited list, scrolling down using arrow keys expand all the list when reaching the last item if expandAllResults is true", function() {
        this.buildList()
            .withMaxResultsDisplayed(3)
            .withTotalResults(6)
            .withEventsEnabled()
            .withScrollAtItem(2)
            .build();

        var listContainer = jQuery("#qunit-fixture");
        this.pressDownArrowOn(listContainer);

        equal(listContainer.find(".aui-list-item-link").length, 6, "All the results are rendered");
        equal(listContainer.find("button.view-all").length, 0, "The 'view-all' link is removed");
    });

    test("In a limited list, scrolling down using arrow keys does not expand the list when reaching the last item if expandAllResults is false", function() {
        this.buildList()
            .withMaxResultsDisplayed(3)
            .withTotalResults(6)
            .withExpandAllResults(false)
            .withEventsEnabled()
            .withScrollAtItem(2)
            .build();

        var listContainer = jQuery("#qunit-fixture");
        this.pressDownArrowOn(listContainer);

        equal(listContainer.find(".aui-list-item-link").length, 3, "Three links are rendered");
    });

});
});