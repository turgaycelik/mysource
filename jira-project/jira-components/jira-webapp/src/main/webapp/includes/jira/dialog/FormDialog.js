define('jira/dialog/form-dialog', [
    'jira/dialog/dialog',
    'jira/message',
    'jira/ajs/ajax/smart-ajax',
    'jira/focus/set-focus',
    'jira/util/browser',
    'jira/util/events',
    'jira/util/navigator',
    'jquery'
], function(
    Dialog,
    Messages,
    SmartAjax,
    SetFocus,
    Browser,
    Events,
    Navigator,
    jQuery
) {
    /**
     * @class FormDialog
     * @extends Dialog
     */
    return Dialog.extend({

        _getDefaultOptions: function () {
            return jQuery.extend(this._super(), {
                autoClose: false,
                targetUrl: "",
                handleRedirect: false,
                onUnSuccessfulSubmit : function() {},
                onSuccessfulSubmit : function() {},

                /**
                 * By default if we have been told by the markup to go to a specific URL, then we do
                 * otherwise we reload the current page by going to it again.
                 */
                onDialogFinished : function() {

                    // Always close the dialog before attempting to unload the page, in case
                    // dirty-form or some other onunload check blocks it. Be sure to get the target ftirst,
                    // though, because it's stored in the dialog's DOM and is lost when the dialog is hidden.
                    var targetUrl = this._getTargetUrlValue();

                    this.hide();

                    if (targetUrl) {
                        Events.trigger('page-unload.location-change.from-dialog', [this.$popup]);
                        window.location.href = targetUrl;
                    } else {
                        Events.trigger('page-unload.refresh.from-dialog', [this.$popup]);
                        Browser.reloadViaWindowLocation();
                    }
                },

                submitAjaxOptions: {
                    type: "post",
                    data: {
                        inline: true,
                        decorator: "dialog"
                    },
                    dataType: "html"
                },

                isIssueDialog: false
            });
        },

        _getFormDataAsObject: function ()
        {
            var fieldValues = {};
            // save form configuration to user prefs
            jQuery(this.$form.serializeArray()).each(function(){
                var fieldVal = fieldValues[this.name];
                if (!fieldVal) {
                    fieldVal = this.value;
                } else if (jQuery.isArray(fieldVal)) {
                    fieldVal.push(this.value);
                } else {
                    fieldVal = [fieldVal, this.value];
                }
                fieldValues[this.name] = fieldVal;
            });
            return fieldValues;
        },

        _getRelativePath: function () {
            var ajaxOptions = this._getRequestOptions();
            return parseUri(ajaxOptions.url).directory;
        },

        _getPath: function (action) {
            var relPath = this._getRelativePath();
            if (action.indexOf(relPath)==0) {
                return action;
            }  else {
                return relPath + action;
            }
        },

        _submitForm: function (e) {
            this.cancelled = false;
            this.xhr = null;
            this.redirected = false;
            this.serverIsDone = false;

            var instance = this, defaultRequestOptions = jQuery.extend(true, {}, this.options.submitAjaxOptions),
                requestOptions = jQuery.extend(true, defaultRequestOptions, {
                    url: this._getPath(this.$form.attr("action")),
                    data: this._getFormDataAsObject(),
                    complete: function (xhr, textStatus, smartAjaxResult)
                    {
                        instance.hideFooterLoadingIndicator();

                        if (! instance.cancelled)
                        {
                            if (smartAjaxResult.successful)
                            {
                                instance.$form.trigger("fakesubmit");
                                instance._handleServerSuccess(smartAjaxResult.data, xhr, textStatus, smartAjaxResult);
                                //
                                // if we have already been redirected then the page is asynchronously unloading and going elsewhere
                                // and hence we should not do the complete processing since its pointless and could only do harm
                                //
                                if (! instance.redirected)
                                {
                                    instance._handleSubmitResponse(smartAjaxResult.data, xhr, smartAjaxResult);
                                }
                            }
                            else
                            {
                                instance._handleServerError(xhr, textStatus, smartAjaxResult.errorThrown, smartAjaxResult);
                            }
                        }
                        //The form may have changed. Lets just make sure there us no 'submitting' class to be extra sure.
                        instance.$form.removeClass("submitting");
                    }
                });
                // and since we're a dialog, and since web actions don't offer responses tailored to the accepts header, we should always set these request options:
                requestOptions.data.decorator = "dialog";
                requestOptions.data.inline = true;

            this.showFooterLoadingIndicator();

            this.xhr = SmartAjax.makeRequest(requestOptions);

            e.preventDefault();
        },

        /**
         * This is called when the AJAX 'error' code path is taken.  It takes the response text and plonks it into
         * the dialog as content.
         *
         * @param xhr the AJAX bad boy
         * @param textStatus the status
         * @param errorThrown the error in play
         * @param smartAjaxResult the smart AJAX result object we need
         */
        _handleServerError: function (xhr, textStatus, errorThrown, smartAjaxResult)
        {
            if (this.options.onUnSuccessfulSubmit)
            {
                this.options.onUnSuccessfulSubmit.call(xhr, textStatus, errorThrown, smartAjaxResult);
            }
            // we stick this in as an error message at the top of the content if we can otherwise
            // we replace the content.  The former allows the form details to be saved via copy and paste
            // handy for really large comments say.  We only do this if we dont have any data to display
            var errorContent = SmartAjax.buildDialogErrorContent(smartAjaxResult, true);
            var content$ = this.get$popupContent().find(".form-body");
            if(content$.length !== 1) {
                content$ = this.get$popupContent();
            }
            var insertErrMsg = content$.length === 1 && ! smartAjaxResult.hasData;
            if (insertErrMsg) {
                content$.prepend(errorContent);
            } else {
                this.setSubmitResponseContent(errorContent);
            }
        },

        /**
         * Appends content recieved after submission of form to dialog.
         *
         * @param content
         */
        setSubmitResponseContent: function (content) {

            if (this.options.formatSubmitResponseContent) {
                content = this.options.formatSubmitResponseContent.call(this, content);
            }
            this._setContent(content);
        },

        isIssueDialog: function() {
            return !!this.options.isIssueDialog;
        },

        /**
         * This is called on the AJAX 'success' code path.  At this stage its a 200'ish message.
         *
         * If there is content and its has the magic 'redirect' we handle the redirect
         * then we will redirect to the specified place!
         *
         * @param xhr the AJAX bad boy
         * @param data the response body
         * @param textStatus the status
         * @param smartAjaxResult the smart AJAX result object we need
         */
        _handleServerSuccess: function (data, xhr, textStatus, smartAjaxResult) {

            // Gets instructions for a message to show after page has been reloaded/redirected
            var msgInstructions = this._detectMsgInstructions(xhr);

            if (msgInstructions) {
                Messages.showMsgOnReload(msgInstructions.msg, {
                    type: msgInstructions.type,
                    closeable: msgInstructions.closeable,
                    target: msgInstructions.target
                });
            }

            //
            // Check the status of the X-Atlassian-Dialog-Control header to see if we are done
            //
            var instructions = this._detectRedirectInstructions(xhr);
            this.serverIsDone = instructions.serverIsDone;

            if (instructions.redirectUrl) {
                if (this.options.onSuccessfulSubmit) {
                    this.options.onSuccessfulSubmit.call(this, data, xhr, textStatus, smartAjaxResult);
                }
                this._performRedirect(instructions.redirectUrl);
            } else if (!this.serverIsDone) {
                this.setSubmitResponseContent(data);
            }

        },

        _detectMsgInstructions: function (xhr) {
            var instructions = {
                    msg: xhr.getResponseHeader("X-Atlassian-Dialog-Msg-Html")
                };

            if (instructions.msg) {
                instructions.type = xhr.getResponseHeader("X-Atlassian-Dialog-Msg-Type").toUpperCase();
                instructions.closeable = (xhr.getResponseHeader("X-Atlassian-Dialog-Msg-Closeable") === "true");
                instructions.target = xhr.getResponseHeader("X-Atlassian-Dialog-Msg-Target");
                return instructions;
            }
        },

        /**
         * This is called when the original AJAX 'complete' code path is taken with a serverIsDone = true.
         *
         * @param data the response body
         * @param xhr the AJAX bad boy
         * @param smartAjaxResult the smart AJAX result object we need
         */
        _handleInitialDoneResponse: function (data, xhr, smartAjaxResult) {
            this._handleSubmitResponse(data, xhr, smartAjaxResult);
        },

        /**
         * This is called when the AJAX 'complete' code path is taken.
         *
         * @param data the response body
         * @param xhr the AJAX bad boy
         * @param smartAjaxResult the smart AJAX result object we need
         */
        _handleSubmitResponse: function (data, xhr, smartAjaxResult) {
            if (this.serverIsDone) {
                if (this.options.onSuccessfulSubmit) {
                    this.options.onSuccessfulSubmit.call(this, data, xhr, smartAjaxResult);
                }
                if (this.options.autoClose) {
                    this.hide();
                }
                if (this.options.onDialogFinished) {
                    this.options.onDialogFinished.call(this, data, xhr, smartAjaxResult);
                }
            }
        },

        /**
         * This will hide the dialog and redirect the page to the specified url
         * @param url {String} the url to redirect to
         */
        _performRedirect: function (url) {
            if (!this.options.stayVisibleOnRedirect) {
                this.hide();
            }
            this.redirected = true;
            this._super(url);
        },

        _hasTargetUrl: function() {
            return this._getTargetUrlHolder().length > 0;
        },

        _getTargetUrlHolder: function() {
            return jQuery(this.options.targetUrl);
        },

        _getTargetUrlValue: function() {
            return this._getTargetUrlHolder().val();
        },

        decorateContent: function () {

            var instance = this, $buttons, $cancel, $buttonContainer, $footerContainer, $closeLink;

            this.$form = jQuery("form", this.get$popupContent());
            this._constructHeading();

            this.$form.submit(function (e) {

                var event = new jQuery.Event("before-submit");
                instance.$form.trigger(event, [e, instance]);

                if (!event.isDefaultPrevented()) {
                    instance.$form.addClass("submitting");
                    var submitButtons = jQuery(':submit', instance.$form);
                    submitButtons.attr('disabled','disabled');
                    if (instance.options.submitHandler) {
                        instance.showFooterLoadingIndicator();
                        instance.options.submitHandler.call(instance, e, function () {
                            if (instance.isCurrent()){
                                instance.hideFooterLoadingIndicator();
                                //The form may have changed. Lets just make sure there us no 'submitting' class to be extra sure.
                                instance.$form.removeClass("submitting");
                            }
                        });
                    } else {
                        instance._submitForm(e);
                    }
                } else {
                    // still need to prevent submit
                    e.preventDefault();
                }
            });

            $cancel = jQuery(".cancel", this.get$popupContent());
            $cancel.click(function (e) {
                if (instance.xhr)
                {
                    instance.xhr.abort();
                }
                instance.xhr = null;
                instance.cancelled = true;
                instance.hide(true, {reason:Dialog.HIDE_REASON.cancel});
                e.preventDefault();
            });

            // We want people to cancel forms like they used to when cancel was a button.
            // JRADEV-1823 - Alt-` does not work in IE
            // TODO JDEV-28437 - Bind this behaviour via keyboard shortcut.
            if (Navigator.isIE() && Navigator.majorVersion() < 12) {
                $cancel.focus(function(e){
                    if (e.altKey){
                        $cancel.click();
                    }
                });
            }

            //if there's no buttons (i.e. when there's an error) then add a close link!
            $buttons = this.get$popupContent().find(".button, .aui-button");
            $buttonContainer = this.get$popupContent().find("div.buttons");
            if($cancel.length === 0 && $buttons.length === 0) {
                if($buttonContainer.length === 0) {
                    $footerContainer = jQuery('<div class="buttons-container form-footer"><div class="buttons"/></div>').appendTo(this.get$popupContent());
                    $buttonContainer = $footerContainer.find(".buttons");
                }

                AJS.populateParameters();
                $closeLink = jQuery("<button class='aui-button aui-button-link cancel' id='aui-dialog-close'>" + AJS.I18n.getText("admin.common.words.close") + "</button>");
                $buttonContainer.append($closeLink);

                $closeLink = jQuery(".cancel", this.get$popupContent());
                $closeLink.click(function(e) {
                    instance.hide(true, {reason:Dialog.HIDE_REASON.cancel});
                    e.preventDefault();
                });
            }
            $buttonContainer.prepend(jQuery("<span class='icon throbber'/>"));

            // Allow for opening of the keyboard shortcut dialog via help links in dialog contents.
            this.get$popupContent().on("click", ".shortcut-tip-trigger", function(e) {
                e.preventDefault();
                if(!instance.get$popupContent().isDirty() || confirm(AJS.I18n.getText("common.forms.dirty.dialog.message"))) {
                    instance.hide();
                    jQuery("#keyshortscuthelp").click();
                }
            });

            // Find the footer container
            if (!($footerContainer instanceof jQuery)) {
                $footerContainer = $buttonContainer.closest(".buttons-container, .form-footer");
                if (!$footerContainer.size()) $footerContainer = $buttonContainer;
            }

            // Returning footer container, as it's the one with the styles in dialogs.
            this.$buttonContainer = $footerContainer;
        },

        getButtonsContainer: function () {
            return this.$buttonContainer;
        },

        _constructHeading: function() {
            var $formHeading, $formHeadingContainer;
            $formHeading = jQuery(":header:first", this.get$popupContent());
            if($formHeading.length > 0) {
                // append to heading but retain event handlers
                this.addHeading($formHeading.contents());
                $formHeadingContainer = $formHeading.parent();
                $formHeading.remove();

                // Cull empty parent elements.
                while($formHeadingContainer.is(":empty")) {
                    $formHeading = $formHeadingContainer.parent();
                    $formHeadingContainer.remove();
                    $formHeadingContainer = $formHeading;
                }
            }
        },

        _setContent: function (content,decorate) {
            this._super(content,decorate);

            if (content && Dialog.current === this) {
                //Hitting enter on MSIE input forms will not submit when:
                //
                //  {quote: http://www.thefutureoftheweb.com/blog/submit-a-form-in-ie-with-enter}
                //      There is more than one text/password field, but no <input type="submit"/> or <input type="image"/>
                //      is visible when the page loads.
                //  {quote}
                //
                // This seems to be roughly correct. When we initially load the dialog we do it offscreen which means that
                // enter on text input may not work. To get it to work we explicity listen for enter key.
                // TODO JDEV-28437 - Bind this behaviour via keyboard shortcut.
                if (Navigator.isIE() && Navigator.majorVersion() < 11) {
                    this.$form.bind("keypress", function (e) {
                        if (e.keyCode === 13 && jQuery(e.target).is("input")) {
                            e.preventDefault();
                            jQuery(this).submit();
                        }
                    });
                }

                if (Dialog.current === this) {
                    this._focusFirstField();
                }
            }
        },

        _focusFirstField: function (focusElementSelector) {

            var triggerConfig = new SetFocus.FocusConfiguration();

            if (focusElementSelector) {
                triggerConfig.focusElementSelector = focusElementSelector;
            } else if (this.$activeTrigger && this.$activeTrigger.attr("data-field")) {
                triggerConfig.focusElementSelector =  "[name='" + this.$activeTrigger.attr("data-field") + "']";
            }
            triggerConfig.parentElementSelectors.unshift('.aui-dialog-content');

            /**
             * The below snippet is to fix a bug in Internet Explorer. The bug is as follows:
             *
             * 1. Open a FormDialog that has a <select> as the first focused field
             * 2. Tab to a text field with in the same FormDialog
             * 3. Submit dialog
             * 4. VALIDATION ERRORS RETURN FROM SERVER CORRECTLY BUT FIRST FIELD IS NOT FOCUSED.
             *
             * In Internet Explorer programatically focus a <select> after navigating to a text field that no longer
             * exists in the DOM Internet Explorers tab ordering gets all messed up.
             *
             * It seems the only fix is to focus a random field/link.
             */
            triggerConfig.context = this.get$popup()[0];

            if (Navigator.isIE() && Navigator.majorVersion() < 11) {
                var $focusHack = jQuery(".trigger-hack", triggerConfig.context );
                if ($focusHack.length === 0){
                    $focusHack = jQuery("<input Class='trigger-hack' type='text' value=''/>").css({
                        position: "absolute",
                        left: -9000,
                        top: -9000
                    }).appendTo(triggerConfig.context);
                }
                $focusHack.focus();
            }

            SetFocus.pushConfiguration(triggerConfig);
            SetFocus.triggerFocus();
            SetFocus.triggerFocus();
        },

        hide: function (undim, options) {
            if (this._super(undim, options) === false) {
                return false;
            }

            SetFocus.popConfiguration();
        }
    });


});

/** Preserve legacy namespace
    @deprecated AJS.FormPopup */
AJS.namespace("AJS.FormPopup", null, require('jira/dialog/form-dialog'));
AJS.namespace('JIRA.FormDialog', null, require('jira/dialog/form-dialog'));
