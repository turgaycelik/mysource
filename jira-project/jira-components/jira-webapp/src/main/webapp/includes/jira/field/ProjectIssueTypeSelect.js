    JIRA.ProjectIssueTypeSelect = Class.extend({

        init: function (options) {

            var val,
                instance = this;

            this.$project = options.project;

            // JRA-25760 Bloody IE8, see  http://support.microsoft.com/kb/925014 , but essentially
            // adding and removing elements from the DOM when they have inline background image styles
            // causes mixed security warnings
            this.backgroundImages = this.removeBackgroundImages(options.issueTypeSelect);
            this.$refIssueTypeSelect = jQuery(options.issueTypeSelect).clone(true, true);
            this.$issueTypeSelect = options.issueTypeSelect;
            this.$projectIssueTypesSchemes = options.projectIssueTypesSchemes;
            this.$issueTypeSchemeIssueDefaults = options.issueTypeSchemeIssueDefaults;
            this.projectIssueTypeSchemes = this.getProjectIssueTypeSchemesFromDom();
            this.issueTypesSchemeDefaults = this.getIssueTypeSchemeIssueDefaults();

            //may not have a project select on the edit issue page!
            if(instance.$project.length > 0) {
                val = instance.$project.val();
                instance.setIssueTypeScheme(instance.getIssueTypeSchemeForProject(val));

                if (this.$project.is("select")) {
                    this.$project.change(function () {
                        var val = instance.$project.val();
                        instance.lastIssueType = instance.$issueTypeSelect.find(":selected:first").val();
                        // When we change forget what we previously had selected
                        instance.$refIssueTypeSelect.val("");
                        instance.setIssueTypeScheme(instance.getIssueTypeSchemeForProject(val));
                        instance.$issueTypeSelect.trigger("reset");
                    });
                }
            }
        },

        getIssueTypeSchemeForProject: function (projectId) {
            return this.projectIssueTypeSchemes[projectId];
        },

        getDefaultIssueTypeForScheme: function (issueTypeSchemeId) {
            return this.issueTypesSchemeDefaults[issueTypeSchemeId];
        },

        removeBackgroundImages: function(issueTypeSelect) {
            var map = {};
            jQuery(issueTypeSelect).find("option[style]").each(function(){
                map[jQuery(this).attr('id')] = jQuery(this).css('background-image');
                // For IE you can't do this in one step, you have to set the style attribute to a blank string before removing it
                // see http://davidovitz.blogspot.com.au/2006/09/https-bug-in-ie.html for a description
                jQuery(this).attr('style','');
                jQuery(this).removeAttr('style');
            });
            return map;
        },

        addBackgroundImages: function() {
            var that=this;
            this.$issueTypeSelect.find("option").each(function(){
                jQuery(this).css('background-image', that.backgroundImages[jQuery(this).attr('id')]);
            });
        },

        setIssueTypeScheme: function (issueTypeSchemeId) {

            // Only remember the currently selected issue type if there's an explicit selected="selected" attribute
            // on the <option> element.
            var selectedIssueType,
                instance = this,
                $toSelect,
                $placeholder = jQuery("<span class='hidden' />"),
                $optgroups = this.$refIssueTypeSelect.find("optgroup");

            // Removing from DOM for performance and because manipulating selected element in IE9 does not actually show
            // the selected element in the UI unless we reappend it.
            $placeholder.insertAfter(this.$issueTypeSelect);

            this.$issueTypeSelect.detach().empty();

            // <optgroup>s exist when there is more than 1 issue type scheme, in this case we need to only want to append
            // the <option>s from the associated <optgroup>.  Otherwise we can append all of the <option>s.
            if ($optgroups.length) {
                $optgroups.each(function () {
                    var $optgroup = jQuery(this);
                    if ($optgroup.is("[data-scheme-id='" + issueTypeSchemeId + "']")) {
                        instance.$issueTypeSelect.append($optgroup.clone(true).children());
                        return false;
                    }
                });
            } else {
                this.$issueTypeSelect.append(this.$refIssueTypeSelect.children());
            }

            selectedIssueType  = this.$issueTypeSelect.find("option[data-selected]").val() || "";

            if (instance.lastIssueType) {
                $toSelect = this.$issueTypeSelect.find("option[value='" + instance.lastIssueType + "']");
            } else if (selectedIssueType) {
                $toSelect = this.$issueTypeSelect.find("option[value='" + selectedIssueType + "']");
            }

            if ($toSelect && $toSelect.length !== 0) {
                $toSelect.attr("selected", "selected");
                this.$issueTypeSelect.val($toSelect.val()); // JRA-27128 IE needs the value to be explictly set also
            } else {
                // There's no <option> with value={selectedIssueType} so set default.
                this.setDefaultIssueType(issueTypeSchemeId);
            }

            this.$issueTypeSelect.insertAfter($placeholder);
            this.addBackgroundImages();
            this.$issueTypeSelect.attr("data-project", this.$project.val());
            $placeholder.remove();
        },

        setDefaultIssueType: function (issueTypeSchemeId) {

            // Set the assigned default issue type if there is one, otherwise the first.

            var defaultIssueType = this.getDefaultIssueTypeForScheme(issueTypeSchemeId);

            if (defaultIssueType !== "") {
                this.$issueTypeSelect.find("option[value='" + defaultIssueType + "']"  ).attr("selected", "selected");
            } else {
                this.$issueTypeSelect.find("option").each(function (i, option) {
                    if (this.value && this.value !== "") {
                        jQuery(option).attr("selected", "selected");
                        return false;
                    }
                });
            }
        },

        getProjectIssueTypeSchemesFromDom: function () {

            var projectIssueTypes = {};

            this.$projectIssueTypesSchemes.find("input").each(function (i, input) {
                var $input = jQuery(input),
                    project = $input.attr("title"),
                    issueTypes = $input.val();

                projectIssueTypes[project] = issueTypes;
            });

            return projectIssueTypes;
        },

        getIssueTypeSchemeIssueDefaults: function () {
            var issueTypesSchemeDefaults = {};

            this.$issueTypeSchemeIssueDefaults.find("input").each(function (i, input) {
                var $input = jQuery(input),
                    issueTypeScheme = $input.attr("title"),
                    defaultIssueType = $input.val();

                issueTypesSchemeDefaults[issueTypeScheme] = defaultIssueType;
            });

            return issueTypesSchemeDefaults;
        }

    });