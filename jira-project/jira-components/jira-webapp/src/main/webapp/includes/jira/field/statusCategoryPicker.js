(function($) {
    function getStatusCategoryIcon(descriptor) {
        var $icon = $(), statusCategory;

        if (descriptor && descriptor.model) {
            statusCategory = $.extend({},descriptor.model().data());
            delete statusCategory['descriptor'];

            $icon = $(JIRA.Template.Util.Issue.Status.issueStatus({
                issueStatus: {
                    name: descriptor.label(),
                    statusCategory: statusCategory
                },
                isCompact: true
            })).removeClass("jira-issue-status-lozenge-tooltip").removeAttr("title").removeAttr("data-tooltip");
        }

        return $icon;
    }

    var StatusCategorySingleSelect = AJS.SingleSelect.extend({
        _hasIcon: function() {
            return (this.$field.val() && this.$field.val() !== this.model.placeholder);
        },
        setSelection: function(descriptor) {
            this._super(descriptor);
            this.$container.find(".fake-ss-icon").remove();
            this.$container.append(getStatusCategoryIcon(descriptor).addClass("fake-ss-icon aui-ss-entity-icon"));
        },
        init: function(options) {
            this._super(options);
            var oldRenderer = this.listController._renders.suggestion;
            this.listController._renders.suggestion = function hackyStatusCategorySuggestionRenderer(descriptor) {
                var listElem = oldRenderer.apply(this, arguments);
                listElem.find("a").prepend("&nbsp;").prepend(getStatusCategoryIcon(descriptor));
                return listElem;
            };
        },
        _renders: {
            entityIcon: function hackyStatusCategoryEntityIcon(descriptor) {
                return $(); // no-op.
            }
        }
    });

    JIRA.StatusCategorySingleSelect = StatusCategorySingleSelect;
})(AJS.$);
