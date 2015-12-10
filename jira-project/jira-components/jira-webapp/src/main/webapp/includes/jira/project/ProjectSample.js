define('jira/project/project-sample', [
    'jira/lib/class',
    'jquery'
], function(
    Class,
    jQuery
) {
    /**
     * @class ProjectSample
     * @extends Class
     */
    return Class.extend({
        init: function(options) {
            this.defaultName = "My Project";
            this.defaultKey = "MP";
            this.defaultAvatarSrc = "";

            this.nameValue = "";
            this.keyValue = "";
            this.avatarSrc = "";

            this.$container = options.element;
            this.$container.html(JIRA.Templates.CreateProject.projectSample({
                projectName: this.defaultName,
                projectKey: this.defaultKey,
                issueName: options.issueName
            }));

            this.$sampleElement = this.$container.find(".sample-project");
            this.$sampleElementName = this.$sampleElement.find("#project-name-val");
            this.$sampleElementKey = this.$sampleElement.find("#key-val");
            this.$sampleElementAvatar = this.$sampleElement.find("#project-avatar");

            this.events = options.events;
            this.events.bind("updated.Name", jQuery.proxy(function() { this.updateSampleName(arguments[1]); }, this));
            this.events.bind("updated.Key", jQuery.proxy(function() { this.updateSampleKey(arguments[1]); }, this));
            this.events.bind("updated.Avatar", jQuery.proxy(function() { this.updateSampleAvatar(arguments[1]); }, this));
        },

        _render: function() {
            var name = this.nameValue || this.defaultName;
            var key = this.keyValue || this.defaultKey;
            var avatar = this.avatarSrc || this.defaultAvatarSrc;

            key = (key || "") + "-1";
            key = key.toUpperCase();

            this.$sampleElementName.text(name);
            this.$sampleElementKey.text(key);
            this.$sampleElementAvatar.attr("src", avatar);
        },

        updateSampleName: function(name) {
            this.nameValue = jQuery.trim(name);
            this._render();
        },

        updateSampleKey: function(key) {
            this.keyValue = jQuery.trim(key);
            this._render();
        },

        updateSampleAvatar: function(src) {
            this.avatarSrc = src;
            this._render();
        }
    });
});

AJS.namespace('JIRA.ProjectSample', null, require('jira/project/project-sample'));
