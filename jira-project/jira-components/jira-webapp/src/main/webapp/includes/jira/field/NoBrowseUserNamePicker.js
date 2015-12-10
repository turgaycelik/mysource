    /**
     * Caters for addition of users in frotherized MultiSelect inputs when the user
     * does not have the Browse User permission.
     *
     * Whilst this control could potentially give away valid usernames it is not new in this behaviour.
     * The old-style user picker used in the Edit Issue form will reveal if a particular username is invalid or not.
     */
    AJS.NoBrowseUserNamePicker = AJS.MultiSelect.extend({

        /**
         * Use the User REST interface to attempt to get a user by username.
         */
        _getDefaultOptions: function () {
            return AJS.$.extend(true, this._super(), {
                errorMessage: AJS.I18n.getText("admin.project.people.nobrowse.user.doesntexist"),
                showDropdownButton: false,
                removeOnUnSelect: true,
                itemAttrDisplayed: "label"
            });
        },

        /**
         * Override to prevent requesting per keypress.
         *
         * NoBrowseUserNamePicker does not send a request per keypress.
         * Instead it will request for validity when enter or space is pressed
         * or when the field is blurred.
         */
        _handleCharacterInput: function() {
            //this.hideErrorMessage();
        },

        /**
         * Prevents the display of Suggestions for this control.
         *
         * We don't want any suggestions for the NoBrowseUserNamePicker
         * as the user using doesn't have access to see a list of users.
         * Also, using this REST enpoint will not retrieve a list of users anyway.
         */
        _setSuggestions: function() {},

        /**
         * Handles an error from the REST endpoint.
         *
         * The REST endpoint used for this operation returns a 404 if the user requested
         * does not exist. This situation is handled here.
         *
         * If any other error is returned the parent's error handler will be used.
         *
         * @param smartAjaxResult The error.
         */
        _handleServerError: function(smartAjaxResult) {
            if (smartAjaxResult.status === 404) {
                this.showErrorMessage();
            } else {
                this._super();
            }
        },

        /**
         * Called when the field is blurred.
         *
         * When the field is deactivated (i.e. blurred) we want to issue a
         * request to check if the currently entered username (if any) is valid or not.
         */
        _deactivate: function() {
            this.validateAndAdd();
        },

        /**
         * Issues a request to the User REST endpoint with the current field value.
         *
         * Hides any existing error messages before issuing a request to the User endpoint
         * to determine the validity of the current input.
         */
        validateAndAdd: function() {
            var instance = this;
            if (AJS.$.trim(this.$field.val()) === "") {
                this.hideErrorMessage();
            } else {
                jQuery.ajax({
                    url: contextPath + "/rest/api/2/user",
                    data: {
                        username: AJS.$.trim(instance.getQueryVal())
                    },
                    success: function (user) {
                        instance.hideErrorMessage();
                        instance.$field.val("");
                        instance.addItem(new AJS.ItemDescriptor({
                            label: user.displayName,
                            value: user.name
                        }));
                    },
                    error: function () {
                        instance.showErrorMessage();
                    }
                });
            }
        },

        /**
         * Sends a request to the REST endpoint using the currently entered username (if any)
         * when space is pressed.
         *
         * This allows for quick entry of usernames.
         *
         * If the username is not valid the space keypress event is prevented and an error message
         * displayed.
         */
        _handleSpace: function() {
            this.validate();
        },

        /**
         * Transforms the successfully returned username into a Lozenge.
         *
         * @param data The successfully selected username.
         */
        _handleServerSuggestions: function() {
            this.hideErrorMessage();
            this.handleFreeInput();
        },

        /**
         * Adds the current user input as a lozenge.
         *
         * By this time the input has been validated as a username.
         * If the input is not a valid username the response comes back as a
         * 404 triggering _handleServerError.
         */
        handleFreeInput: function() {
            var value = AJS.$.trim(this.$field.val());

            if (value !== "") {
                this.addItem({ value: value, label: value });
                this.model.$element.trigger("change");
            }

            this.$field.val("");
        },

        keys: {
            /**
             * Issue a request for the currently entered username when Return is pressed.
             *
             * @param event The aui:keypress event.
             */
            "Return": function(event) {
                event.preventDefault();
                this.validateAndAdd();
            },

            /**
             * Issue a request for the currently entered username when the Spacebar is pressed.
             *
             * @param event The aui:keypress event.
             */
            "Spacebar": function(event) {
                event.preventDefault();
                this.validateAndAdd();
            }
        }
    });
