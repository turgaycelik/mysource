AJS.test.require("jira.webresources:select-pickers");

module("QueryableDropdownSelect");

test("Should get results before rendering dropdown (such that positioning won't be borked)", function() {
    var sandbox = sinon.sandbox.create();
    var qdds = new AJS.QueryableDropdownSelect({
        element: AJS.$("<ul></ul>")
    });

    sandbox.stub(qdds, "getQueryVal", function() {
        return "one";
    });
    sandbox.stub(qdds, 'requestSuggestions', function() {
        return jQuery.Deferred().resolve(["one","two"]).promise();
    });

    var genList = sandbox.spy(qdds.listController, "generateListFromJSON");
    var showDropdown = sandbox.spy(qdds.dropdownController, "show");
    var positionDropdown = sandbox.spy(qdds.dropdownController, "setPosition");

    qdds.onEdit();

    ok(genList.calledBefore(showDropdown), "should have results before we render a dropdown with them in it (to prevent things like TF-39)");
    ok(genList.calledBefore(positionDropdown), "should have results before we calc and position a dropdown with them in it (to prevent things like TF-39)");
});
