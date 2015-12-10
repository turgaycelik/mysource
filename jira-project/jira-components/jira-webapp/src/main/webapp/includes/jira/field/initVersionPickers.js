(function () {

    JIRA.VersionPicker = AJS.MultiSelect.extend({

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
                    userEnteredOptionsMsg: AJS.I18n.getText("common.concepts.new.version")
                });
            } else {
                return this._super(opts);
            }
        },

        _selectionHandler: function (selected, e) {
            var allExistingVersionDescriptors = this.model.getDisplayableSelectedDescriptors().concat(this.model.getDisplayableUnSelectedDescriptors());
            var selectedDescriptor = selected.data("descriptor");
            var existingVersion = _.find(allExistingVersionDescriptors, function(descriptor) {
                return descriptor.label() === selectedDescriptor.label();
            });
            if(!existingVersion)
            {
                selectedDescriptor.properties.value = "nv_" + selectedDescriptor.value();
                JIRA.trigger("Issue.Version.new.selected", [selectedDescriptor.value()]);
            }
            this._super(selected, e);
        }
    });

    function createPicker($selectField) {
        new JIRA.VersionPicker({
            element: $selectField,
            itemAttrDisplayed: "label",
            removeOnUnSelect: false,
            submitInputVal: true,
            errorMessage: AJS.I18n.getText("jira.ajax.autocomplete.versions.error"),
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

    function findVersionSelectAndConvertToPicker(context, selector) {
        selector = selector || ".aui-field-versionspicker.frother-control-renderer";
        AJS.$(selector, context).each(function () {
            var $selectField = locateSelect(this);
            if ($selectField.length) {
                createPicker($selectField);
            }
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
            findVersionSelectAndConvertToPicker(context);
        }
    });

})();
