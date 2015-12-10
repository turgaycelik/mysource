define('jira/autocomplete/user-autocomplete', [
    'jira/autocomplete/rest-autocomplete',
    'jira/data/parse-options-from-fieldset',
    'jira/util/browser',
    'jira/util/elements',
    'jira/util/objects',
    'jquery'
], function(
    RESTAutoComplete,
    parseOptionsFromFieldset,
    Browser,
    Elements,
    Objects,
    jQuery
) {
    /**
     * User picker - converted from YUI based autocomplete. There is some code in here that probably isn't necessary,
     * if removed though selenium tests would need to be re-written.
     * @class UserAutoComplete
     * @extends RESTAutoComplete
     * @param options
     * @returns {Object}
     */
    var UserAutoComplete = function(options) {

        // prototypical inheritance (http://javascript.crockford.com/prototypal.html)
        var that = Objects.begetObject(RESTAutoComplete);

        that.getAjaxParams = function(){
            return {
                url: contextPath + "/rest/api/1.0/users/picker",
                data: {
                    fieldName: options.fieldID,
                    fieldConfigId: options.fieldConfigID,
                    projectId: options.projectId
                },
                dataType: "json",
                type: "GET"
            };
        };

        /**
         * Returns true if the field's containing form has the 'submitted' class.
         *
         * @param field The reference to the field whose form to check for the 'submitted' class.
         * @return {Boolean}
         */
        function fieldsFormHasBeenSubmitted(field) {
            var submitting = false,
                form = field.closest("form");

            if (form.length && form.hasClass("submitting")) {
                submitting = true;
            }

            return submitting;
        }

        /**
         * Create html elements from JSON object
         * @method renderSuggestions
         * @param {Object} response - JSON object
         * @returns {Array} Multidimensional array, one column being the html element and the other being its
         * corresponding complete value.
         */
        that.renderSuggestions = function(response) {

            if (fieldsFormHasBeenSubmitted(this.field) || !Browser.isSelenium() && !Elements.elementIsFocused(this.field)) {
                return false;
            }

            var resultsContainer, suggestionNodes = [];

            // remove previous results
            this.clearResponseContainer();


            if (response && response.users && response.users.length > 0) {

                resultsContainer = jQuery("<ul/>").appendTo(this.responseContainer);

                jQuery(response.users).each(function() {

                    // add html element and corresponding complete value  to sugestionNodes Array
                    suggestionNodes.push([jQuery("<li/>")
                    .html(this.html)
                    .appendTo(resultsContainer), this.name]);

                });
            }

            if (response.footer) {
                this.responseContainer.append(jQuery("<div/>")
                .addClass("yui-ac-ft")
                .html(response.footer)
                .css("display","block"));
            }

            if (suggestionNodes.length > 0) {
                that.addSuggestionControls(suggestionNodes);
                jQuery('.atlassian-autocomplete div.yad, .atlassian-autocomplete .labels li').textOverflow({
                    autoUpdate: true
                });
            }

            return suggestionNodes;

        };

        // Use autocomplete only once the field has at least 2 characters
        options.minQueryLength = 2;

        // wait 1/4 of after someone starts typing before going to server
        options.queryDelay = 0.25;

        that.init(options);

        return that;

    };

    UserAutoComplete.init = function(parent){
        jQuery("fieldset.user-picker-params", parent).each(function(){
            var params = parseOptionsFromFieldset(jQuery(this)),
                field = (params.fieldId || params.fieldName),
                $container = jQuery("#" + field + "_container");


            $container.find("a.popup-trigger").click(function(e){
                var url = contextPath,
                    vWinUsers;

                e.preventDefault();

                if (!params.formName)
                {
                    params.formName = $container.find("#" + field).parents("form").attr("name");
                }

                if (params.actionToOpen) {
                    url = url + params.actionToOpen;
                } else {
                    url = url + '/secure/popups/UserPickerBrowser.jspa';
                }
                url += '?formName=' + params.formName + '&';
                url += 'multiSelect=' + params.multiSelect + '&';
                url += 'decorator=popup&';
                url += 'element=' + field;

                if (params.fieldConfigId) {
                    url += '&fieldConfigId=' + params.fieldConfigId;
                }
                if (params.projectId) { // an array of project ids
                    if (jQuery.isArray(params.projectId)) {
                        for (var projectId in params.projectId) {
                            url += '&projectId=' + projectId;
                        }
                    } else {
                        url += '&projectId=' + params.projectId;
                    }
                }

                vWinUsers = window.open(url, 'UserPicker', 'status=yes,resizable=yes,top=100,left=100,width=800,height=750,scrollbars=yes');
                vWinUsers.opener = self;
                vWinUsers.focus();
            });


            if (params.userPickerEnabled === true ){
                UserAutoComplete({
                    field: parent ? parent.find("#" + field) : null,
                    fieldID: field,
                    fieldConfigID: params.fieldConfigId,
                    projectId: params.projectId,
                    delimChar: params.multiSelect === false ? undefined : ",",
                    ajaxData: {
                        fieldName: params.fieldName
                    }
                });
            }
        });
    };

    return UserAutoComplete;
});

/** Preserve legacy namespace
    @deprecated jira.widget.autocomplete.Users */
AJS.namespace("jira.widget.autocomplete.Users", null, require('jira/autocomplete/user-autocomplete'));
AJS.namespace('JIRA.UserAutoComplete', null, require('jira/autocomplete/user-autocomplete'));
