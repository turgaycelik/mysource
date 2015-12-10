define('jira/dialog/labels-dialog', [
    'jira/dialog/form-dialog',
    'jira/issue',
    'jira/issuenavigator/issue-navigator',
    'jira/util/events',
    'jira/util/events/types',
    'jquery'
], function(
    FormDialog,
    Issue,
    IssueNavigator,
    Events,
    Types,
    jQuery
) {
    /**
     * @class LabelsDialog
     * @extends FormDialog
     */
    return FormDialog.extend({
        init: function (options) {
            this._super(options);
            this.issueId = null;
            this.customFieldId = null;
            this.labelsProvider = this.initLabelsProvider();
            this.labelPicker = null;
        },

        initLabelsProvider: function() {
            if (this.options.labelsProvider && jQuery.isFunction(this.options.labelsProvider)) {
                return this.options.labelsProvider;
            } else if (this.options.labels) {
                return this._getLabelsFromOptions;
            } else {
                return this._getLabelsFromTrigger;
            }
        },

        _getLabelsFromOptions: function() {
            return jQuery(this.options.labels);
        },

        _getLabelsFromTrigger: function() {
            return this.$activeTrigger.closest('.labels-wrap');
        },

        decorateContent: function () {
            this._super();

            var $content = this.get$popupContent();

            this.issueId = $content.find('input[name=id]').val();
            var $customFieldId = $content.find('input[name=customFieldId]');
            if ($customFieldId.length === 1) {
                this.customFieldId = $customFieldId.val();
            } else {
                this.customFieldId = null;
            }
        },

        focusLabelPicker: function () {
            this.get$popupContent().find('textarea').focus();
        },

        show: function () {
            if(this._super()) {
                this.focusLabelPicker();
            }
        },

        _handleSubmitResponse: function (data, xhr, smartAjaxResult) {
            if (this.serverIsDone) {
                if (this.options.onSuccessfulSubmit) {
                    this.options.onSuccessfulSubmit.call(this, data, xhr, smartAjaxResult);
                }
                //need to set this *before* we hide the dialog because hide() clears their values!
                var issueIdVal = this.get$popupContent().find('input[name=id]').val(),
                    noLinkVal = this.get$popupContent().find('input[name=noLink]').val();

                if (this.options.autoClose) {
                    this.hide();
                }
                IssueNavigator.Shortcuts.flashIssueRow(this.issueId);

                var jsonpData = {
                    id: issueIdVal,
                    decorator: 'none',
                    noLink: noLinkVal
                };
                if (this.customFieldId) {
                    jsonpData.customFieldId = this.customFieldId;
                }
                var instance = this;
                var $labelsWrap = instance.labelsProvider(this);

                jQuery(jQuery.ajax({
                    url: contextPath + '/secure/EditLabels!viewLinks.jspa',
                    data: jsonpData,
                    success: function (html) {
                        var $newLabelsWrap = jQuery('<div>').html(html).find('.labels-wrap');
                        //don't show the edit icon on the issuenavigator.
                        if(IssueNavigator.isNavigator()) {
                            $newLabelsWrap.find("a.edit-labels").remove();
                        }
                        $labelsWrap.html($newLabelsWrap.html());
                        if (Types.REFRESH_ISSUE_PAGE) {
                            // JRADEV-11575: If we are in kickass we should refresh after we show the new labels.
                            // We still show the new labels first to give the most immediate feedback.
                            Events.trigger(Types.REFRESH_ISSUE_PAGE, [Issue.getIssueId()]);
                        }

                    }
                })).throbber({ target: $labelsWrap });
            }
        },

        handleCancel: function () {
            this._super();
            // Clear the content of the dialog so that it is retrieved from the server the next time it is opened.
            this.$content = null;
        }
    });
});

/** Preserve legacy namespace
    @deprecated AJS.LabelsPopup */
AJS.namespace("AJS.LabelsPopup", null, require('jira/dialog/labels-dialog'));
AJS.namespace('JIRA.LabelsDialog', null, require('jira/dialog/labels-dialog'));
