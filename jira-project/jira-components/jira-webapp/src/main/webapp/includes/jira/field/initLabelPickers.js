define('jira/field/label-picker-factory', [
    'jira/field/label-picker',
    'jira/data/parse-options-from-fieldset',
    'jquery',
    'exports'
], function(
    LabelPicker,
    parseOptionsFromFieldset,
    $,
    exports
) {
    exports.createPicker = function($fieldset, context) {

        var opts = parseOptionsFromFieldset($fieldset),
            $select = $('#' + opts.id, context),
            issueId = opts.issueId,
            data = {};

        if (/customfield_\d/.test(opts.id)) {
            data.customFieldId = parseInt(opts.id.replace('customfield_', ''), 10);
        }

        new LabelPicker({
            element: $select,
            ajaxOptions: {
                url: contextPath + '/rest/api/1.0/labels' + (issueId ? '/' + issueId : '') + '/suggest',
                data: data
            }
        });
    }

});

(function() {
    var LabelPickerFactory = require('jira/field/label-picker-factory');
    var Events = require('jira/util/events');
    var Types = require('jira/util/events/types');
    var Reasons = require('jira/util/events/reasons');
    var $ = require('jquery');

    var FIELDSET_SELECTOR = "fieldset.labelpicker-params";

    function locateFieldset(parent) {
        var $parent = $(parent),
            $fieldset;

        if ($parent.is(FIELDSET_SELECTOR)) {
            $fieldset = $parent;
        } else {
            $fieldset = $parent.find(FIELDSET_SELECTOR);
        }

        return $fieldset;
    }

    function findLabelsFieldsetAndConvertToPicker(context, selector) {
        selector = selector || ".aui-field-labelpicker";

        $(selector, context).each(function () {
            var $fieldset = locateFieldset(this);

            if ($fieldset.length > 0) {
                LabelPickerFactory.createPicker($fieldset, context);
            }
        });
    }

    Events.bind(Types.NEW_CONTENT_ADDED, function (e, context, reason) {
        if (reason !== Reasons.panelRefreshed) {
            findLabelsFieldsetAndConvertToPicker(context);
        }
    });
})();
