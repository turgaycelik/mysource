// Initialises the Workflow Transition Properties table

;(function($, _, JIRA) {

    $(function(){
        var transitionPropertiesResource = contextPath + "/rest/api/2/workflow/transitions/";
        var propertyTable = $("#workflow-transition-properties-table");
        var workflowTransition = propertyTable.attr('data-workflowTransition');
        var workflowNameEncoded = encodeURIComponent(propertyTable.attr('data-workflowName'));
        var workflowModeEncoded = encodeURIComponent(propertyTable.attr('data-workflowMode'));
        var keyHeaderText = propertyTable.attr('data-key-header');
        var valueHeaderText = propertyTable.attr('data-value-header');
        var isEditable = propertyTable.attr('data-workflow-editable') === "true";

        var safeTrim = function(obj) {
            return _.isString(obj) ? $.trim(obj) : obj;
        };

        var TransitionPropertyModel = AJS.RestfulTable.EntryModel.extend({

            //Overwrite so we can use OUR URL which is not RESTFul. (i.e. the properties ain't in the URL). We need this
            //in the COTR because restful table calls TransitionPropertyModel.extend({url:...}) when it creates
            //the model for the create row.
            initialize: function () {
                this.url = function () {
                    var key = this.get("key") || "";
                    return transitionPropertiesResource + workflowTransition + "/properties?workflowName=" + workflowNameEncoded + "&workflowMode=" + workflowModeEncoded + "&key=" + key;
                };
                AJS.RestfulTable.EntryModel.prototype.initialize.apply(this, arguments);
            },

            destroy: function(options) {
                //This needs to be empty so that params don't get added to the URL in the RESTFul table
                options = _.extend({}, options, {
                    data: {}
                });

                AJS.RestfulTable.EntryModel.prototype.destroy.call(this, options);
            },

            changedAttributes: function(newAttributes) {

                var result = {};

                _.each(newAttributes, function(item, key) {
                    item = safeTrim(item);
                    if (this.get(key) !== item) {
                        result[key] = item;
                    }
                }, this);

                return !_.isEmpty(result) ? result : false;
            },

            defaults: {
                value: ""
            }
        });

        new AJS.RestfulTable({
            autoFocus: true,
            el: propertyTable,
            resources: {
                all: transitionPropertiesResource + workflowTransition + "/properties?workflowName=" + workflowNameEncoded + "&workflowMode=" + workflowModeEncoded,
                self: transitionPropertiesResource + workflowTransition + "/properties?workflowName=" + workflowNameEncoded + "&workflowMode=" + workflowModeEncoded
            },
            columns: [
                {
                    id: "key",
                    header: keyHeaderText,
                    styleClass: "workflow-transition-properties-key-col",
                    readView: JIRA.WorkflowTransitionProperties.KeyView,
                    createView: JIRA.WorkflowTransitionProperties.CreateKeyView,
                    editView: JIRA.WorkflowTransitionProperties.EditKeyView,
                    allowEdit: false
                },
                {
                    id: "value",
                    header: valueHeaderText,
                    styleClass: "workflow-transition-properties-value-col",
                    readView: JIRA.WorkflowTransitionProperties.ValueView
                }
            ],
            model: TransitionPropertyModel,
            allowEdit: isEditable,
            allowCreate: isEditable,
            allowDelete: isEditable,
            reverseOrder: true,
            noEntriesMsg: AJS.I18n.getText("admin.workflowtransition.no.entries.set")
        });

        /**
         * Global error handler because RESTFul table does *NOT* trigger errors for AJAX requests in any way that is
         * useful.
         */
        $(document).ajaxError(function (e, xhr, ajaxOptions) {
            //- 400 errors are handled in the RESTFul table so ignore them.
            //- Only handle errors for our resource (we are listening globally).
            if (xhr.status !== 400 && ajaxOptions.url.indexOf(transitionPropertiesResource) >= 0) {
                if (JIRA.Ajax.isWebSudoFailure(xhr)) {
                    //We have a websudo error. Pop open the websudo dialog and retry the request.
                    JIRA.SmartAjax.handleWebSudoError(ajaxOptions);
                } else {
                    JIRA.ErrorDialog.openErrorDialogForXHR(xhr);
                }
            }
        });
    });
}(AJS.$, _, JIRA));
