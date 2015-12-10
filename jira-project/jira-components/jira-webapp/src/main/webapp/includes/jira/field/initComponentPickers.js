(function () {

    JIRA.ComponentPicker = AJS.MultiSelect.extend({

        init: function(options) {
            this._super(options);
            this.suggestionsHandler = new AJS.OnlyNewItemsSuggestHandler(this.options, this.model);
        },

        _getDefaultOptions: function (opts) {
            var canCreate = false;
            if(opts && opts.element) {
                canCreate = AJS.$(opts.element).data("create-permission");
            }
            if(canCreate) {
                return AJS.$.extend(true, this._super(), {
                    userEnteredOptionsMsg: AJS.I18n.getText("common.concepts.new.component")
                });
            } else {
                return this._super(opts);
            }
        },

        _selectionHandler: function (selected, e) {
            var allExistingDescriptors = this.model.getDisplayableSelectedDescriptors().concat(this.model.getDisplayableUnSelectedDescriptors());
            var selectedDescriptor = selected.data("descriptor");
            var existingDescriptor = _.find(allExistingDescriptors, function(descriptor) {
                return descriptor.label() === selectedDescriptor.label();
            });
            if(!existingDescriptor)
            {
                selectedDescriptor.properties.value = "nv_" + selectedDescriptor.value();
                JIRA.trigger("Issue.Component.new.selected", [selectedDescriptor.value()]);
            }
            this._super(selected, e);
        }
    });


    function createPicker($selectField) {
        new JIRA.ComponentPicker({
            element: $selectField,
            itemAttrDisplayed: "label",
            errorMessage: AJS.I18n.getText("jira.ajax.autocomplete.components.error"),
            maxInlineResultsDisplayed: 15,
            expandAllResults: true
        });
    }

    function locateSelect(parent) {

        var $parent = AJS.$(parent),
            $selectField;

        if ($parent.is("select")) {
            $selectField = $parent;
        } else {
            $selectField = $parent.find("select");
        }

        return $selectField;
    }

    var DEFAULT_SELECTORS = [
        "div.aui-field-componentspicker.frother-control-renderer", // aui forms
        "td.aui-field-componentspicker.frother-control-renderer", // convert to subtask and move
        "tr.aui-field-componentspicker.frother-control-renderer" // bulk edit
    ];

    function findComponentSelectAndConvertToPicker(context, selector) {

        selector = selector || DEFAULT_SELECTORS.join(", ");

        AJS.$(selector, context).each(function () {

            var $selectField = locateSelect(this);

            if ($selectField.length) {
                createPicker($selectField);
            }
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            findComponentSelectAndConvertToPicker(context);
        }
    });
})();
