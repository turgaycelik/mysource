AJS.namespace("JIRA.WorkflowTransitionProperties");

JIRA.WorkflowTransitionProperties.KeyView = AJS.RestfulTable.CustomReadView.extend({
    render: function(args) {
        return JIRA.Templates.WorkflowTransitionPropertiesTemplates.key(args);
    }
});

JIRA.WorkflowTransitionProperties.ValueView = AJS.RestfulTable.CustomReadView.extend({
    render: function(args) {
        return JIRA.Templates.WorkflowTransitionPropertiesTemplates.value(args);
    }
});

JIRA.WorkflowTransitionProperties.CreateKeyView = AJS.RestfulTable.CustomReadView.extend({
    render: function(args) {
        return JIRA.Templates.WorkflowTransitionPropertiesTemplates.createKey(args);
    }
});

JIRA.WorkflowTransitionProperties.EditKeyView = AJS.RestfulTable.CustomEditView.extend({
    render: function(args) {
        return JIRA.Templates.WorkflowTransitionPropertiesTemplates.key(args);
    }
});