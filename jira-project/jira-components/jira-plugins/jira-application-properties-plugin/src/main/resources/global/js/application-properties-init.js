// Initialises the application properties table on the advanced configuration page

jQuery(function(){
    var propRest = contextPath + "/rest/api/latest/application-properties";
    var appPropertyTable = jQuery("#application-properties-table");

        new AJS.RestfulTable({
            el: appPropertyTable, // table to add entries to. Entries are appended to the tables <tbody> element
            resources: {
                all: propRest+"?permissionLevel=SYSADMIN_ONLY",
                self: propRest
            },
            columns: [
                {
                    id: "key",
                    header: "Key",
                    styleClass: "application-property-key-col",
                    allowEdit: false,
                    readView: JIRA.Admin.AppProperty.KeyView
                },
                {
                    id: "value",
                    header: "Value",
                    styleClass: "application-property-value-col",
                    emptyText: AJS.I18n.getText("admin.advancedconfiguration.setvalue")
                }
            ],
            allowEdit: true,
            allowCreate: false,
            views: {
                editRow: JIRA.Admin.AppProperty.EditRow,
                row: JIRA.Admin.AppProperty.Row
            }
        });
});
