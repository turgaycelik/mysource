AJS.$(document).ready(function () {
    var $table = AJS.$("#custom-content-links-admin-content");
    var entityKey = $table.data("entitykey");
    var customContentLinksTable = new AJS.RestfulTable({
        autoFocus: true,
        el: $table,
        allowReorder: true,
        createPosition: "bottom",
        resources: {
            all: AJS.contextPath() + "/rest/custom-content-links/1.0/customcontentlinks/" + entityKey + "/list",
            self: AJS.contextPath() + "/rest/custom-content-links/1.0/customcontentlinks/"  + entityKey
        },
        columns: [
            {
                id: "linkLabel",
                styleClass: "custom-content-link-label",
                header: AJS.I18n.getText("common.words.displayName")
            },
            {
                id: "linkUrl",
                styleClass: "custom-content-link-url",
                header: AJS.I18n.getText("common.words.url")
            }
        ]
    });
});