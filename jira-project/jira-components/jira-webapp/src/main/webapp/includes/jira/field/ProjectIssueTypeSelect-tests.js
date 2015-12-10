AJS.test.require("jira.webresources:jira-global");
AJS.test.require("jira.webresources:jira-fields");

module('JIRA.ProjectIssueTypeSelect', {
    setup: function() {
        this.project = AJS.$("<select>\n"
                + "            <optgroup label=\"Recent Projects\">\n"
                + "                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10000&amp;size=small)\" selected=\"selected\" value=\"10000\">\n"
                + "                    Bulk Move 1\n"
                + "                </option>\n"
                + "                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10020&amp;size=small)\" value=\"10020\">\n"
                + "                    &lt;iframe src=\"http://www.google.com\"&gt;&lt;/iframe&gt;\n"
                + "                </option>\n"
                + "                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10340&amp;size=small)\" value=\"10340\">\n"
                + "                    SecURItY\n"
                + "                </option>\n"
                + "                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10010&amp;size=small)\" value=\"10010\">\n"
                + "                    QA\n"
                + "                </option>\n"
                + "                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10040&amp;size=small)\" value=\"10040\">\n"
                + "                    Demon\n"
                + "                </option>\n"
                + "                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10440&amp;size=small)\" value=\"10440\">\n"
                + "                    \"&gt;'&gt;&lt;script&gt;alert('markup editor 4ever');&lt;/script&gt;\n"
                + "                </option>\n"
                + "                    </optgroup>\n"
                + "                <optgroup label=\"All Projects\">                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10440&amp;size=small)\" value=\"10440\">\n"
                + "                    \"&gt;'&gt;&lt;script&gt;alert('markup editor 4ever');&lt;/script&gt;\n"
                + "                </option>\n"
                + "                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10041&amp;size=small)\" value=\"10041\">\n"
                + "                    &amp;quot;&amp;gt;&amp;lt;script&amp;gt;document.write(12*12)&amp;lt;/script&amp;gt;\n"
                + "                </option>\n"
                + "                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10240&amp;size=small)\" value=\"10240\">\n"
                + "                    &lt;A HREF=\"http://66.102.7.147/\"&gt;UNIQUE-XSS&lt;/A&gt;\n"
                + "                </option>\n"
                + "                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10020&amp;size=small)\" value=\"10020\">\n"
                + "                    &lt;iframe src=\"http://www.google.com\"&gt;&lt;/iframe&gt;\n"
                + "                </option>\n"
                + "                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10000&amp;size=small)\" selected=\"selected\" value=\"10000\">\n"
                + "                    Bulk Move 1\n"
                + "                </option>\n"
                + "                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10001&amp;size=small)\" value=\"10001\">\n"
                + "                    Bulk Move 2\n"
                + "                </option>\n"
                + "                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10040&amp;size=small)\" value=\"10040\">\n"
                + "                    Demon\n"
                + "                </option>\n"
                + "                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10010&amp;size=small)\" value=\"10010\">\n"
                + "                    QA\n"
                + "                </option>\n"
                + "                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10340&amp;size=small)\" value=\"10340\">\n"
                + "                    SecURItY\n"
                + "                </option>\n"
                + "                            <option style=\"background-image:url(/jira/secure/projectavatar?pid=10031&amp;size=small)\" value=\"10031\">\n"
                + "                    ?????????\n"
                + "                </option>\n"
                + "                    </optgroup></select>").appendTo("body");

        this.issueType = AJS.$("<select>\n"
                + "    <optgroup data-scheme-id=\"10000\" label=\"Default scheme (unlisted projects)\">\n"
                + "        <option class=\"10000\" id=\"issuetype100001\" value=\"1\">\n"
                + "            Bug\n"
                + "        </option>\n"
                + "        <option class=\"10000\" id=\"issuetype100002\" value=\"2\">\n"
                + "            New Feature\n"
                + "        </option>\n"
                + "        <option class=\"10000\" id=\"issuetype100003\" value=\"3\">\n"
                + "            Task\n"
                + "        </option>\n"
                + "        <option class=\"10000\" id=\"issuetype100004\" value=\"4\">\n"
                + "            Improvement\n"
                + "        </option>\n"
                + "        <option class=\"10000\" id=\"issuetype100006\" value=\"6\">\n"
                + "            UNQ-ISSUES\n"
                + "        </option>\n"
                + "        <option class=\"10000\" id=\"issuetype100009\" value=\"9\">\n"
                + "            Epic\n"
                + "        </option>\n"
                + "        <option class=\"10000\" id=\"issuetype1000010\" value=\"10\">\n"
                + "            Story\n"
                + "        </option>\n"
                + "    </optgroup>\n"
                + "    <optgroup data-scheme-id=\"10452\" label=\"&lt;A HREF=&quot;http://66.102.7.147/&quot;&gt;UNIQUE-XSS&lt;/A&gt;\">\n"
                + "        <option class=\"10452\" id=\"issuetype104524\" value=\"4\">\n"
                + "            Improvement\n"
                + "        </option>\n"
                + "        <option class=\"10452\" id=\"issuetype104523\" value=\"3\">\n"
                + "            Task\n"
                + "        </option>\n"
                + "    </optgroup>\n"
                + "</select>").appendTo("body");


        this.issueTypeSchemes = AJS.$("<fieldset />").html("<input type=\"hidden\" title=\"10440\" value=\"10000\">\n"
                + "            <input type=\"hidden\" title=\"10041\" value=\"10000\">\n"
                + "            <input type=\"hidden\" title=\"10240\" value=\"10452\">\n"
                + "            <input type=\"hidden\" title=\"10020\" value=\"10000\">\n"
                + "            <input type=\"hidden\" title=\"10000\" value=\"10000\">\n"
                + "            <input type=\"hidden\" title=\"10001\" value=\"10000\">\n"
                + "            <input type=\"hidden\" title=\"10040\" value=\"10000\">\n"
                + "            <input type=\"hidden\" title=\"10010\" value=\"10000\">\n"
                + "            <input type=\"hidden\" title=\"10340\" value=\"10000\">\n"
                + "            <input type=\"hidden\" title=\"10031\" value=\"10000\">").appendTo("body");

        this.issueTypeDefaults = AJS.$("<fieldset />").html("<input type=\"hidden\" title=\"10000\" value=\"\">\n"
                + "    <input type=\"hidden\" title=\"10452\" value=\"4\">").appendTo("body");



        this.projectIssueTypeSelect = new JIRA.ProjectIssueTypeSelect({
            project: this.project,
            issueTypeSelect: this.issueType,
            projectIssueTypesSchemes: this.issueTypeSchemes,
            issueTypeSchemeIssueDefaults: this.issueTypeDefaults
        });

    },
    teardown: function () {
        this.project.remove();
        this.issueType.remove();
        this.issueTypeSchemes.remove();
        this.issueTypeDefaults.remove();
    }
});

test("schemes are correct for projects", function () {
    equal(this.projectIssueTypeSelect.getIssueTypeSchemeForProject("10240"), "10452",
            "Project with [id=10240] should be mapped to scheme with [id=10452]");

    equal(this.projectIssueTypeSelect.getIssueTypeSchemeForProject("10000"), "10000",
            "Project with [id=10000] should be mapped to scheme with [id=10000]");
});

test("issueType defaults are correct for schemes", function () {
    equal(this.projectIssueTypeSelect.getDefaultIssueTypeForScheme("10452"), "4",
            "Issue type scheme with id [10452] should have a default issue type of [id=4]");

    equal(this.projectIssueTypeSelect.getDefaultIssueTypeForScheme("10000"), "",
            "Issue type scheme with id [10000], has no default issue type so should return empty string");
});

test("Selecting issue type scheme WITHOUT a default issue type selects first", function () {
    this.projectIssueTypeSelect.setIssueTypeScheme("10452");
    equal(this.projectIssueTypeSelect.$refIssueTypeSelect.val(), "1");
});

test("Selecting issue type scheme WITH a default issue type, selects it", function () {
    this.projectIssueTypeSelect.setIssueTypeScheme("10452");
    equal(this.projectIssueTypeSelect.$issueTypeSelect.val(), "4");
});

test("If an issue type is already selected it takes priority over default", function () {
    this.projectIssueTypeSelect.$refIssueTypeSelect.find("optgroup[data-scheme-id='10000'] option[value=9]").attr("data-selected", "selected");
    this.projectIssueTypeSelect.$refIssueTypeSelect.find("optgroup[data-scheme-id='10452'] option[value=3]").attr("data-selected", "selected");
    this.projectIssueTypeSelect.setIssueTypeScheme("10000");

    equal(this.projectIssueTypeSelect.$issueTypeSelect.val(), "9",
            "issuetype with [id=9] should be selected, not the first issuetype");

    this.projectIssueTypeSelect.setIssueTypeScheme("10452");

    equal(this.projectIssueTypeSelect.$issueTypeSelect.val(), "3",
            "issuetype with [id=3] should be selected, not the default issuetype");

    this.projectIssueTypeSelect.setIssueTypeScheme("10000");
    this.projectIssueTypeSelect.setIssueTypeScheme("10452");

    equal(this.projectIssueTypeSelect.$issueTypeSelect.val(), "3",
            "issuetype with [id=3] should still be selected");
});

test("Selecting project displays correct issue types", function () {

    options = this.issueType.find("option");
    equal(this.issueType.attr("data-project"), "10000");
    equal(options.eq(0).val(), "1");
    equal(options.eq(1).val(), "2");
    equal(options.eq(2).val(), "3");
    equal(options.eq(3).val(), "4");
    equal(options.eq(4).val(), "6");
    equal(options.eq(5).val(), "9");
    equal(options.eq(6).val(), "10");
    equal(this.issueType.val(), "1");
    equal(options.length, 7);

    this.project.val("10240").trigger("change");
    var options = this.issueType.find("option");
    equal(this.issueType.data().project, "10240");
    equal(options.eq(0).val(), "4");
    equal(options.eq(1).val(), "3");
    equal(this.issueType.val(), "4");
    equal(options.length, 2);

});

test("If user has selected an issuetype and changes project, the same issue type remains selected", function () {
    this.issueType.val("10").trigger("change");
    this.project.val("10240").trigger("change");
    equal(this.issueType.val(), "4", "project [id=10240] doesn't have an issue type [id=10] so the default should be selected");
    this.project.val("10000").trigger("change");
    equal(this.issueType.val(), "4", "project [id=10000] has an issue type [id=4] so should be selected");
});
