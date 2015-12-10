define('jira/ajs/select/checkbox-multi-select-status-lozenge', [
    'jira/ajs/select/checkbox-multi-select',
    'jquery'
], function(
    CheckboxMultiSelect,
    jQuery
) {
    /**
     * This is a multiselect list specialized in issue status lozenges.
     *
     * @class CheckboxMultiSelectStatusLozenge
     * @extends CheckboxMultiSelect
     */
    return CheckboxMultiSelect.extend({
        _getCustomRenders: function() {
            var renders = this._super();

            renders.suggestionItemStatusLozenge = this._renders.suggestionItemStatusLozenge;
            return renders;
        },

        _renders: {
            suggestionItemStatusLozenge: function(descriptor) {
                var $checkbox = jQuery("<input type='checkbox' tabindex='-1' />").val(descriptor.value()),
                        $listElem = jQuery('<li class="check-list-item">'),
                        $label = jQuery("<label class='item-label' />");

                if (descriptor.styleClass()) {
                    $listElem.addClass(descriptor.styleClass());
                }

                $label.html(JIRA.Template.Util.Issue.Status.issueStatusResolver({
                    issueStatus: descriptor.model().data("simple-status"),
                    isSubtle: true
                }));

                if (descriptor.selected()) {
                    $checkbox.attr("checked", "checked");
                }

                if (descriptor.title()) {
                    $label.attr("data-descriptor-title", descriptor.title()); // Used by KickAss' page object for finding values in the dropdown!
                }

                if (descriptor.disabled()) {
                    $listElem.addClass("disabled");
                    $checkbox.attr("disabled", "disabled");
                }

                $label.prepend($checkbox);
                $listElem.append($label);

                return $listElem;
            },
            suggestionItemResolver: function(descriptor, replacementText) {
                if (descriptor.model().data("simple-status")) {
                    return this.suggestionItemStatusLozenge(descriptor);
                } else {
                    return this.suggestionItemElement(descriptor, replacementText);
                }
            }
        }
    });

});

AJS.namespace('AJS.CheckboxMultiSelectStatusLozenge', null, require('jira/ajs/select/checkbox-multi-select-status-lozenge'));
