AJS.test.require("jira.webresources:util");

test("simple flat list", function() {
    deepEqual(JIRA.parseOptionsFromFieldset(jQuery("<fieldset>"
            + ' <input type="hidden" id="a" value="av"/>'
            + ' <input type="hidden" title="b" value="bv"/>'
            + "</fieldset>")),
            {a:"av", b: "bv"},
            "title and id"
    );
    deepEqual(JIRA.parseOptionsFromFieldset(jQuery("<fieldset>"
            + ' <input type="hidden" id="a" value="av"/>'
            + ' <input type="hidden" title="b" value="bv"/>'
            + ' <input type="hidden" title="c" value="cv"/>'
            + ' <input type="hidden" id="d" value="dv"/>'
            + "</fieldset>")),
            {a:"av", b: "bv", c: "cv", d: "dv"},
            "title and id multiple"
    );
});

test("nested", function() {
    deepEqual(JIRA.parseOptionsFromFieldset(jQuery("<fieldset>"
            + ' <input type="hidden" id="a" value="av"/>'
            + ' <input type="hidden" title="b" value="bv"/>'
            + ' <fieldset title="nested1">'
            + '  <input type="hidden" title="c" value="cv"/>'
            + '  <input type="hidden" id="d" value="dv"/>'
            + ' </fieldset>'
            + "</fieldset>")),
            {a:"av", b: "bv", nested1: {c:"cv", d:"dv"}},
            "nested two level"
    );
    deepEqual(JIRA.parseOptionsFromFieldset(jQuery("<fieldset>"
            + ' <input type="hidden" id="a" value="av"/>'
            + ' <input type="hidden" title="b" value="bv"/>'
            + ' <fieldset title="nested1">'
            + '  <input type="hidden" title="c" value="cv"/>'
            + '  <input type="hidden" id="d" value="dv"/>'
            + ' </fieldset>'
            + ' <input type="hidden" title="e" value="ev"/>'
            + ' <fieldset title="nested2">'
            + '  <input type="hidden" title="f" value="fv"/>'
            + '  <input type="hidden" id="d" value="dv"/>'
            + ' </fieldset>'
            + "</fieldset>")),
            {a:"av", b: "bv", nested1: {c:"cv", d:"dv"}, e:"ev", nested2: {f:"fv", d:"dv"}},
            "nested two level multiple"
    );
    deepEqual(JIRA.parseOptionsFromFieldset(jQuery("<fieldset>"
            + ' <input type="hidden" id="a" value="av"/>'
            + ' <input type="hidden" title="b" value="bv"/>'
            + ' <fieldset title="nested1">'
            + '  <input type="hidden" title="c" value="cv"/>'
            + '  <input type="hidden" id="d" value="dv"/>'
            + '  <fieldset title="nested2">'
            + '   <input type="hidden" title="f" value="fv"/>'
            + '   <input type="hidden" id="d" value="dv"/>'
            + '  </fieldset>'
            + '  <input type="hidden" title="e" value="ev"/>'
            + ' </fieldset>'
            + "</fieldset>")),
            {a:"av", b: "bv", nested1: {c:"cv", d:"dv", e:"ev", nested2: {f:"fv", d:"dv"}}},
            "nested multi level"
    );
});

test("multi-value", function() {
    deepEqual(JIRA.parseOptionsFromFieldset(jQuery("<fieldset>"
            + ' <input type="hidden" id="a" value="av"/>'
            + ' <input type="hidden" title="a" value="bv"/>'
            + "</fieldset>")),
            {a:["av", "bv"]},
            "two values"
    );
    deepEqual(JIRA.parseOptionsFromFieldset(jQuery("<fieldset>"
            + ' <input type="hidden" id="a" value="av"/>'
            + ' <input type="hidden" title="a" value="bv"/>'
            + ' <input type="hidden" title="a" value="cv"/>'
            + "</fieldset>")),
            {a:["av", "bv", "cv"]},
            "three values"
    );
    deepEqual(JIRA.parseOptionsFromFieldset(jQuery("<fieldset>"
            + ' <input type="hidden" id="a" value="av"/>'
            + ' <input type="hidden" title="a" value="bv"/>'
            + ' <input type="hidden" title="a" value="cv"/>'
            + ' <input type="hidden" title="a" value="dv"/>'
            + "</fieldset>")),
            {a:["av", "bv", "cv", "dv"]},
            "four values"
    );
    deepEqual(JIRA.parseOptionsFromFieldset(jQuery("<fieldset>"
            + ' <input type="hidden" id="a" value="av"/>'
            + ' <input type="hidden" title="a" value="av"/>'
            + ' <input type="hidden" title="a" value="cv"/>'
            + ' <input type="hidden" title="a" value="cv"/>'
            + "</fieldset>")),
            {a:["av", "av", "cv", "cv"]},
            "duplicate values"
    );
});
