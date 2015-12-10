define('jira/field/create-project-field', [
    'jira/lib/class',
    'jira/ajs/dark-features',
    'jira/project/project-key-generator',
    'jira/project/project-sample',
    'aui/inline-dialog',
    'jquery'
], function(
    Class,
    DarkFeatures,
    ProjectKeyGenerator,
    ProjectSample,
    InlineDialog,
    jQuery
) {
    /**
     * Hooks up the input controls for the create project fields.
     *
     * @class CreateProjectField
     * @extends Class
     */
    return Class.extend({

        TIMEOUT_MS: 100,

        projectNames: [],

        // TODO get this from the form so that it is only defined in one place
        maxNameLength: 80,

        init: function (options) {
            this.$element = options.element;
            this.$nameElement = this.$element.find("input.text[name='name']");
            this.$keyElement = this.$element.find("input.text[name='key']");
            this.$keyEditedElement = this.$element.find("input[name='keyEdited']");
            this.$avatarElement = this.$element.find(".jira-inline-avatar-picker-trigger");

            this.eventBus = {
                src: jQuery(this),
                trigger: function (name, args) {
                    this.src.trigger(name, args);
                },
                bind: function (name, func) {
                    this.src.bind(name, func);
                }
            };

            var maxKeyLength = this.$keyElement.attr("maxlength");
            if (!maxKeyLength) {
                // The maxlength attribute should be set, if not though, use a sensible default
                maxKeyLength = 10;
            }
            this.keygen = new ProjectKeyGenerator({desiredKeyLength: 4, maxKeyLength: maxKeyLength});
            this.lastKeyValidated = "";
            if (DarkFeatures.isEnabled("addproject.project.sample")) {
                this.sample = new ProjectSample({element: this.$element.find("#sample-project-container"), events: this.eventBus});
            }

            // Input restrictions
            this.$keyElement.attr("style", "text-transform: uppercase");

            // Show any existing errors as inline errors
            this.initialName = this.$nameElement.val();
            this.initialKey = this.$keyElement.val();
            this.$element.find(".error").addClass("description initial-error");
            this.showInitialError(this.$nameElement);
            this.showInitialError(this.$keyElement);

            // Poll the name field for updates
            this.$nameElement.focus(jQuery.proxy(this._bindNameHook, this));
            this.$nameElement.blur(jQuery.proxy(this._unbindHook, this));

            // Poll the key field for updates
            this.$keyElement.focus(jQuery.proxy(this._bindKeyHook, this));
            this.$keyElement.blur(jQuery.proxy(this._unbindHook, this));
            this.$keyElement.blur(jQuery.proxy(this.autofillKeyIfNeeded, this));

            // Hook up help icon
            var $keyHelpElement = this.$keyElement.parent().find("#add-project-key-icon").removeAttr("target data-helplink");
            if ($keyHelpElement.length) {
                new InlineDialog($keyHelpElement, "project-key-help-popup",
                    function(contents, trigger, show) {
                        contents.html(JIRA.Templates.CreateProject.keyHelp());
                        show();
                    }, {
                        width: 330,
                        offsetX: -30
                });
            }

            if (this.$avatarElement.size()) {
                // Keep the sample avatar in sync with the selection
                this.eventBus.trigger("updated.Avatar", this.$avatarElement.attr("src"));
                this.$avatarElement.bind("AvatarSelected", jQuery.proxy(function() {
                    this.eventBus.trigger("updated.Avatar", this.$avatarElement.attr("src"));
                }, this));
            }

            this._loadExistingProjects();
        },

        _loadExistingProjects: function() {
            var instance = this;
            // Get the list of existing project keys and names
            jQuery.ajax({
                url: contextPath + "/rest/api/latest/project",
                success: function(projects) {
                    for (var x in projects) {
                        instance.projectNames.push(projects[x].name.toUpperCase());
                    }
                }
            });
        },

        _bindNameHook: function(e) {
            this._bindHook(e, this.onNameTimeout);
        },

        _bindKeyHook: function(e) {
            var el = jQuery(e.target);
            el.data("lastValue", el.val());
            this._bindHook(e, this.onKeyTimeout);
        },

        _bindHook: function(e, func) {
            var instance = this, el = jQuery(e.target), hook;
            hook = function() {
                instance._unbindHook(e);
                func.apply(instance);
                if (el.is(":visible")) {
                    el.data("checkHook", setTimeout(hook, instance.TIMEOUT_MS));
                }
            }
            if (!el.data("checkHook")) {
                el.data("checkHook", setTimeout(hook, 0));
            }
        },

        _unbindHook: function(e) {
            var el = jQuery(e.target);
            clearTimeout(el.data("checkHook"));
            el.removeData("checkHook");
        },

        shouldUpdateKey: function() {
            return (this.$keyEditedElement.val() != "true");
        },

        setKeyEdited: function(key) {
            // If the key is manually edited, do not suggest automatically generated keys anymore
            // If the key field is cleared, resume suggesting automatically generated keys
            if (this.$keyElement.data("lastValue") !== key) {
                this.$keyEditedElement.val((key) ? "true" : "false");
            }
            this.$keyElement.data("lastValue", key);
        },

        updateKey: function(key) {
            this.$keyElement.val(key);
            this.validateKey(key);
            this.eventBus.trigger("updated.Key",key);
        },

        autofillKeyIfNeeded: function() {
            if (this.shouldUpdateKey()) {
                var key = this.keygen.generateKey(this.$nameElement.val());
                // JRADEV-10797 - Rather than validate the key,
                // we'll pretend that a key is always invalid if it's less than 1 character long.
                if (key.length > 1) {
                    this.updateKey(key);
                } else {
                    // Blank the key without validation.
                    this.$keyElement.val("");
                }
            }
        },

        onNameTimeout: function() {
            var name = this.$nameElement.val();
            this.validateName(name);
            this.eventBus.trigger("updated.Name",name);
            this.autofillKeyIfNeeded();
        },

        onKeyTimeout: function() {
            var key = this.$keyElement.val();
            this.setKeyEdited(key);
            this.validateKey(key);
            this.eventBus.trigger("updated.Key",key);
        },

        validateName: function(name) {
            if (name == this.initialName && this.$nameElement.parent().find(".error").size()) {
                return; // leave the error on this field until its value is changed.
            } else {
                if (name.length > this.maxNameLength) {
                    this.showInlineError(this.$nameElement, this.initialName, AJS.I18n.getText("admin.errors.project.name.too.long", this.maxNameLength));
                    return;
                }
                var x;
                for (x in this.projectNames) {
                    if (name.toUpperCase() == this.projectNames[x]) {
                        this.showInlineError(this.$nameElement, this.initialName, AJS.I18n.getText("admin.errors.project.with.that.name.already.exists"));
                        return;
                    }
                }
            }
            this.hideInlineError(this.$nameElement);
        },

        validateKey: function(key) {
            var instance = this;

            // Only validate the key if it has changed since the last time we validated it
            var changed = (instance.lastKeyValidated !== key);
            this.lastKeyValidated = key;
            if (!changed) {
                return;
            }

            if (key) {
                jQuery.ajax({
                    url: contextPath + "/rest/api/latest/projectvalidate/key?key=" + key.toUpperCase(),
                    success: function(errors) {
                        if (errors.errors && errors.errors["projectKey"]) {
                            instance.showInlineError(instance.$keyElement, instance.initialKey, errors.errors["projectKey"]);
                        } else {
                            instance.hideInlineError(instance.$keyElement);
                        }
                    }
                });
            } else {
                instance.hideInlineError(instance.$keyElement);
            }
        },

        /**
         * Show an error for an input element, in the place of its description.
         *
         * @param $element
         * @param msg
         */
        showInlineError: function($element, initialVal, msg) {
            var $initialErrorElement = $element.parent().find(".initial-error");

            // Don't show an inline error if the field holds the initial value and there is an initial error present
            if ($element.val() === initialVal && $initialErrorElement.length) {
                this.showInitialError($element);
            } else {
                var $errorElement = $element.parent().find(".error.description");
                if (!$errorElement.length) {
                    $errorElement = jQuery("<div class='error description'></div>");
                    $element.parent().append($errorElement);
                }
                $errorElement.text(msg);

                $element.parent().find(".description").hide();
                $initialErrorElement.hide();
                $errorElement.show();
            }
        },

        /**
         * Hide any errors for an input field shown by showInlineError, and its description.
         *
         * @param $element
         */
        hideInlineError: function($element) {
            $element.parent().find(".description").show();
            $element.parent().find(".error.description").hide();
        },

        showInitialError: function($element) {
            var $initialErrorElement = $element.parent().find(".initial-error");
            if ($initialErrorElement.length) {
                $element.parent().find(".description").hide();
                $element.parent().find(".error.description").hide();
                $initialErrorElement.show();
            }
        }

    });

});

AJS.namespace('JIRA.CreateProjectField', null, require('jira/field/create-project-field'));
