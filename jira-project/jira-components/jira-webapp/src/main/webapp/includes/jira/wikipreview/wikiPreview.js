define('jira/wikipreview/wiki-preview', [
    'jquery'
], function(
    jQuery
) {
    /**
     * @param {Object} prefs
     * @param {HTMLElement=} ctx (optional)
     */
    return function wikiPreview(prefs, ctx)
    {

        var field, editField, trigger, inPreviewMode = false, origText,

        /**
         * Gets and sets fields as jQuery objects
         *
         * @method setFields
         * @private
         */
        setFields = function ()
        {
            field = jQuery("#" + prefs.fieldId, ctx),
            editField = jQuery("#" + prefs.fieldId + "-wiki-edit", ctx),
            trigger = jQuery("#" + prefs.trigger, ctx);
        },

        /**
         *  Prevents scroll flicker from happending when at the bottom of the page
         *
         * @method  scrollSaver
         * @private
         * @return {Object}
         * @... {Function} show - reveals scrollSaver
         * @... {Function} hide - hides scrollSaver
         */
        scrollSaver = function ()
        {
            var elem;
            return {
                show: function ()
                {
                    if (!elem)
                    {
                        elem = jQuery("<div>").html("&nbsp;").css({height: "300px"}).insertBefore(editField);
                    }
                    elem.css({display: "block"});
                },
                hide: function ()
                {
                    elem.css({display: "none"});
                }
            };
        }(),

        /**
         *
         * If preview not present, uses REST to get preview of rendered wiki markup. Otherwise restores original state.
         * @method toggleRenderPreview
         * @private
         *
         */
        toggleRenderPreview = function ()
        {
            if (!inPreviewMode)
            {
                editField.find(".content-inner").css({
                    maxHeight: field.css("maxHeight")
                });
                this.showPreview();
            }
            else
            {
                editField.find(".content-inner").css({
                    maxHeight: ""
                });
                this.showInput();
            }
        },

        /**
         * This function replaces the input with the renderered content.
         *
         * @method renderPreviewCallback
         * @param {String} data from the AJAX call
         */
        renderData =  function(data)
        {
            editField.originalHeight = editField.height();
            scrollSaver.show();
            editField.addClass("previewClass");
            origText = field.val();
            field.hide();
            trigger.removeClass("loading").addClass("selected");
            changePreviewAccessibleTextTo(AJS.I18n.getText('renderer.preview.close', prefs.fieldId));
            editField.find(".content-inner").html(data);
            scrollSaver.hide();
            inPreviewMode = true;
            jQuery(document).trigger("showWikiPreview", [editField]);
            // IE!!! - I will get to the bottom of this one day but for now work around.
            setTimeout(function() {
                trigger.focus();
            },0);
        },

        handleError = function(previewer){
            return function(XMLHttpRequest, textStatus, errorThrown)
            {
                trigger.removeClass("loading");
                origText = field.val();
                /* [alert] */
                if (textStatus){
                    alert(textStatus);
                }
                if (errorThrown){
                    alert(errorThrown);
                }
                /* [alert] end */
                previewer.showInput();

            };
        },

        changePreviewAccessibleTextTo = function(accessibleText)
        {
            trigger.find('.wiki-renderer-icon').text(accessibleText);
        };

        return {

            /**
             * Make a request using the textarea/input value and displays the response (rendered wiki content)
             * @method showPreview
             */
            showPreview: function () {
                var that = this;

                var pid = jQuery("#pid", ctx).val(),
                    issueType = jQuery("#issuetype", ctx).val();

                // Handle case where project is a frother control
                if (jQuery.isArray(pid)) {
                    pid = pid[0];
                }

                // Handle case where issue type is a frother control
                if (jQuery.isArray(issueType)) {
                    issueType = issueType[0];
                }

                jQuery("#" + prefs.trigger, ctx).addClass("loading");
                jQuery.ajax({
                    url: contextPath + "/rest/api/1.0/render",
                    contentType: "application/json",
                    type:'POST',
                    data: JSON.stringify({
                        rendererType: prefs.rendererType,
                        unrenderedMarkup: field.val(),
                        issueKey: prefs.issueKey,
                        projectId: pid,
                        issueType: issueType
                    }),
                    dataType: "html",
                    success: renderData,
                    error: handleError(that)
                });
            },

            /**
             * This restores the input field to allow the user to enter wiki text.
             * @method showInput
             */
            showInput: function (e) {
                if (editField) {
                    scrollSaver.show();
                    // clear the height before we reset
                    editField.css({height: ""});
                    editField.removeClass("previewClass").find(".content-inner").empty();
                    field = jQuery("#" + prefs.fieldId, ctx);
                    field.val(origText);
                    field.show();
                    field.focus();
                    trigger.removeClass("selected");
                    changePreviewAccessibleTextTo(AJS.I18n.getText('renderer.preview', prefs.fieldId));
                    scrollSaver.hide();

                    inPreviewMode = false;
                    jQuery(document).trigger("showWikiInput", [editField]);
                }
            },

            /**
             * Applies click handler to trigger and associated behaviour.
             * @method init
             */
            init: function ()
            {
                var that = this, $trigger;

                prefs = jQuery.readData(prefs);

                $trigger = jQuery("#" + prefs.trigger, ctx);
                $trigger.click(function(e) {
                    if (!$trigger.hasClass("loading")) {
                        setFields();
                        toggleRenderPreview.call(that);
                    }
                    e.preventDefault();
                });
            }
        };

    };

});

/** Preserve legacy namespace
    @deprecated jira.app.wikiPreview */
AJS.namespace("jira.app.wikiPreview", null, require('jira/wikipreview/wiki-preview'));
AJS.namespace('JIRA.wikiPreview', null, require('jira/wikipreview/wiki-preview'));
