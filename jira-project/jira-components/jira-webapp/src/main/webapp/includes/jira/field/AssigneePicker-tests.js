AJS.test.require("jira.webresources:jira-global");

module("JIRA.AssigneePicker", {
    setup : function () {
        var fixture = jQuery("#qunit-fixture");

        this.pickerSelect = jQuery('<select id="assignee" name="assignee" class="single-user-picker js-assignee-picker aui-ss-select" data-show-dropdown-button="true" data-user-type="assignee" data-container-class="long-field" multiple="multiple" style="display: none;">'
                + '<optgroup id="assignee-group-suggested" label="Suggestions" data-weight="0">'
                + '  <option value="admin" data-field-text="admin" data-field-label="admin - admin@localhost (admin)" data-icon="/jira/secure/useravatar?size=xsmall&amp;avatarId=10122">admin</option><option value="" data-field-text="Unassigned" data-field-label="Unassigned" data-icon="/jira/secure/useravatar?size=xsmall&amp;avatarId=10123">Unassigned</option>'
                + '  <option value="-1" data-field-text="Automatic" data-field-label="Automatic" data-icon="/jira/secure/useravatar?size=xsmall&amp;avatarId=10123">Automatic</option>'
                + '</optgroup>'
                + '</select>').appendTo(fixture);
    }

});


test("Selecting invalid Automatic assignee", function() {
    var assigneePicker = new JIRA.AssigneePicker({
        element: this.pickerSelect,
        editValue: "-1"
    });

    ok(!assigneePicker.$container.hasClass("aui-ss-editing"), 'input should not be in edit mode');
    equal(assigneePicker.$field.val(), "Automatic", '"Automatic" assignee should be displayed as string label');

});

