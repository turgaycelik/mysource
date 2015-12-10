/**
 * JIRA.Issue.editIssueTypeScheme
 * @author Scott Harwood
 *
 * Functionality for Issue Types Scheme page
 * - handles drag and drop sortable
 * - serialising form action
 */
(function(jQuery) {

    var PARAMS = [
        {selector: "#issue-type-scheme-name", name: "schemeName"},
        {selector: "#issue-type-scheme-description", name: "schemeDescription"},
        {selector: "input[name='fieldId']", name: "fieldId"},
        {selector: "input[name='schemeId']", name: "schemeId"},
        {selector: "input[name='projectId']", name: "projectId"},
        {selector: "#default-issue-type-select", name: "defaultOption"}
    ];

    var isEditMode = undefined;

    /**
     * Serialises selected issue types and their order into a valid POST string
     * @method {private} getSelectedOptions
     * @returns {String}
     */
    var getSelectedOptions = function() {
        var s = jQuery("#selectedOptions").sortable("serialize", {key: "selectedOptions"});
        if (s === '') {
            return 'selectedOptions=';
        } else {
            return s;
        }
     };

    /**
     * Sets form action to include serialised selected issue types
     * @method {private} submitForm
     * @returns {Boolean} lets form continue with submission
     */
     var submitForm = function (eButton)  {
         eButton.form.action = 'ConfigureOptionSchemes.jspa?' + getSelectedOptions();
         return true;
     };

    /**
     * Sets the URL on the add issue type link.
     */
    var updateAddIssueTypeUrl = function () {
        var s = 'AddNewIssueTypeToScheme!input.jspa?' + getSelectedOptions();

        var values = {};
        for (var i = 0; i < PARAMS.length; i++) {
            var $el = jQuery(PARAMS[i].selector);
            values[PARAMS[i].name] = $el.val();
        }

        s = s + "&" + jQuery.param(values);
        jQuery("#add-new-issue-type-to-scheme").attr("href", s);
    };

    /**
     * Updates default issue type select box options
     * @method {private} restrictOptions
     */
    var restrictOptions = function () {

        var queryString = getSelectedOptions().replace(/selectedOptions=/g,"");
        var selectedOptions = queryString.split('&');
        var sel2 = document.getElementById("default-issue-type-select");

        for (var i = 0; i < sel2.options.length; i++) {

            if (sel2.options[i].value === "" || arrayContains(selectedOptions, sel2.options[i].value)) {
                jQuery(sel2.options[i])
                .css({display: "", color: "#000", textDecoration: "none"}).removeAttr("disabled");
            } else {

                if (sel2.options[i].selected) {
                    sel2.options[i].selected = false;
                    sel2.options[0].selected = true;
                }

                jQuery(sel2.options[i])
                .css({display: "none", color: "#ffcccc", textDecoration: "line-through"}).attr("disabled","disabled");

            }
        }
        updateAddIssueTypeUrl();
    };

    /**
     * Will move all list nodes from one list to another
     * @method {private} moveAll
     * @param {String} fromList - id of list to move all child list nodes from
     * @param {String} toList - id of list to move all child list nodes to
     */
    var moveAll = function (fromList, toList) {
        jQuery("#" + fromList).find("li").appendTo(document.getElementById(toList));
        restrictOptions();
    };

    var sendAnalyticsEvent = function(name) {
        if (_.isUndefined(isEditMode)) {
            isEditMode = AJS.$("input[name=schemeId]").val();
            if (!isEditMode) {
                isEditMode = false;
            }
        }
        // Only fire analytics if we're editing an Issue Type Scheme
        if (AJS.EventQueue && isEditMode) {
            AJS.EventQueue.push({
                name: "administration.issuetypeschemes.issuetype." + name + ".global"
            });
        }
    };

    AJS.namespace("JIRA.Issue.editIssueTypeScheme", null, function() {

        // add handler to remove all buttons
        jQuery("#selectedOptionsRemoveAll").click(function(e){
            moveAll("selectedOptions", "availableOptions");
            // don't follow link
            e.preventDefault();
        });

        // add handler to add all buttons
        jQuery("#selectedOptionsAddAll").click(function(e){
            moveAll("availableOptions", "selectedOptions");
            e.preventDefault();
        });

        // add sortable behaviour
        jQuery("#selectedOptions").sortable({
            update: restrictOptions,
            opacity: 0.7,
            // allow dragging between ul#availableOptions also
            connectWith: [document.getElementById("availableOptions")],
            update: function(event, ui) {
                if (!ui.sender && ui.item.parent()[0].id !== "availableOptions") {
                    sendAnalyticsEvent("reordered");
                }
            },
            receive: function(event, ui) {
                sendAnalyticsEvent("added");
            },
            remove: function(event, ui) {
                sendAnalyticsEvent("removed");
            }
        });

        // add sortable behaviour
        if (AJS.params.allowEditOptions) {
            jQuery("#availableOptions").sortable({
                update: restrictOptions,
                opacity: 0.7,
                // allow dragging between ul#selectedOptions also
                connectWith: [document.getElementById("selectedOptions")]
            });
        }

        jQuery("#submitSave").click(function(){
            submitForm(this);
        });

        jQuery("#submitReset").click(function(e) {
            if (AJS.params.resetUrl) {
                jQuery(this).removeDirtyWarning();
                window.location = AJS.params.resetUrl;
            }
            e.preventDefault();
        });

        jQuery("#issue-type-scheme-name, #issue-type-scheme-description, #default-issue-type-select")
                .change(updateAddIssueTypeUrl);

        new JIRA.FormDialog({
            trigger: "#add-new-issue-type-to-scheme",
            id: "add-new-issue-type-to-scheme-dialog",
            ajaxOptions: {
                data: {
                    decorator: "dialog",
                    inline: "true"
                }
            },
            onSuccessfulSubmit: function(data) {
                var $template = jQuery("<div>").html(data).find("#add-issue-type-template");
                $template.find("li").appendTo("#selectedOptions");

                var $newSelect = $template.find("select");
                if ($newSelect.length > 0) {
                    jQuery("#default-issue-type-select").unbind().replaceWith($newSelect);
                    $newSelect.attr("id", "default-issue-type-select").change(updateAddIssueTypeUrl);
                }

                restrictOptions();

                if (AJS.EventQueue) {
                    AJS.EventQueue.push({
                        name: "administration.issuetypeschemes.issuetype.created.global"
                    });
                }
            },
            onDialogFinished: function() {
                this.hide();
            }
        });

        restrictOptions();
    });

    // initialise onload to be sure that all html nodes are available
    jQuery(JIRA.Issue.editIssueTypeScheme);
})(AJS.$);

/** Preserve legacy namespace
    @deprecated jira.app.editIssueTypeScheme */
AJS.namespace("jira.app.editIssueTypeScheme", null, JIRA.Issue.editIssueTypeScheme);
