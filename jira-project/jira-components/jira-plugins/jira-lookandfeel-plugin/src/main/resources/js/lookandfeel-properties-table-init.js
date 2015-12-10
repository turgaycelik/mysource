// Initialises the application properties table on the advanced configuration page

jQuery(function(){
    var propRest = contextPath + "/rest/api/latest/application-properties";
    var appPropertyTable = jQuery("#lookandfeel-colours-properties-table");

        new AJS.RestfulTable({
            el: appPropertyTable, // table to add entries to. Entries are appended to the tables <tbody> element
            resources: {
                all: propRest+"?permissionLevel=ADMIN&keyFilter="+encodeURIComponent("jira\\.lf\\..*colou?r"),
                self: propRest
            },
            columns: [
                {
                    id: "key",
                    header: "",
                    styleClass: "property-key-col",
                    allowEdit: false,
                    readView: JIRA.Admin.LookAndFeelProperty.KeyView,
                    editView: JIRA.Admin.LookAndFeelProperty.KeyView
                },
                {
                    id: "value",
                    header: "",
                    styleClass: "property-value-col",
                    emptyText: AJS.I18n.getText("admin.advancedconfiguration.setvalue"),
                    readView: JIRA.Admin.LookAndFeelProperty.ColourReadView,
                    editView: JIRA.Admin.LookAndFeelProperty.ColourEditView
                }
            ],
            allowEdit: true,
            allowCreate: false,
            views: {
                editRow: JIRA.Admin.LookAndFeelProperty.EditRow,
                row: JIRA.Admin.LookAndFeelProperty.Row
            }
        });
});

jQuery(function(){
    var propRest = contextPath + "/rest/api/latest/application-properties";
    var appPropertyTable = jQuery("#lookandfeel-gadget-colours-properties-table");

    new AJS.RestfulTable({
        el: appPropertyTable, // table to add entries to. Entries are appended to the tables <tbody> element
        resources: {
            all: propRest+"?permissionLevel=ADMIN&keyFilter="+encodeURIComponent("jira\\.lf\\..*gadget\\.colou?r\\d?"),
            self: propRest
        },
        columns: [

            {
                id: "key",
                header: "",
                styleClass: "property-key-col",
                allowEdit: false,
                readView: JIRA.Admin.LookAndFeelProperty.KeyView,
                editView: JIRA.Admin.LookAndFeelProperty.KeyView
            },
            {
                id: "value",
                header: "",
                styleClass: "property-value-col",
                emptyText: AJS.I18n.getText("admin.advancedconfiguration.setvalue"),
                readView: JIRA.Admin.LookAndFeelProperty.ColourReadView,
                editView: JIRA.Admin.LookAndFeelProperty.ColourEditView
            }
        ],
        allowEdit: true,
        allowCreate: false,
        views: {
            editRow: JIRA.Admin.LookAndFeelProperty.EditRow,
            row: JIRA.Admin.LookAndFeelProperty.Row
        }
    });
});

jQuery(function(){
    var propRest = contextPath + "/rest/api/latest/application-properties";
    var appPropertyTable = jQuery("#lookandfeel-date-time-properties-table");

    new AJS.RestfulTable({
        el: appPropertyTable, // table to add entries to. Entries are appended to the tables <tbody> element
        resources: {
            all: propRest+"?permissionLevel=ADMIN&keyFilter="+encodeURIComponent("jira\\.lf\\.date\\..*|jira\\.date\\.time\\.picker\\.use\\.iso8061"),
            self: propRest
        },
        columns: [
            {
                id: "key",
                header: "",
                styleClass: "property-key-col",
                allowEdit: false,
                readView: JIRA.Admin.LookAndFeelProperty.KeyView,
                editView: JIRA.Admin.LookAndFeelProperty.KeyView
            },
            {
                id: "value",
                header: "",
                styleClass: "property-value-col",
                emptyText: AJS.I18n.getText("admin.advancedconfiguration.setvalue"),
                readView: JIRA.Admin.LookAndFeelProperty.DateReadView,
                editView: JIRA.Admin.LookAndFeelProperty.DateEditView
            }
        ],
        allowEdit: true,
        allowCreate: false,
        views: {
            editRow: JIRA.Admin.LookAndFeelProperty.EditRow,
            row: JIRA.Admin.LookAndFeelProperty.Row
        }
    });
});
