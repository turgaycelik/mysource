define('jira/focus/set-focus', [
    'jira/dialog/dialog',
    'jquery'
], function(
    Dialog,
    jQuery
) {

    var _defaultExcludeParentSelector = 'form.dont-default-focus',
        _defaultFocusElementSelector = 'input:not(#issue-filter-submit), select, textarea, button, a.cancel',
        _defaultParentElementSelectors = ["."+Dialog.ClassNames.CONTENT_AREA, 'form.aui', 'form'],
        _configurationStack = [];

    var _focusIn = function (context, parentSelector, excludeParentSelector, elementSelector) {
        var found = false;
        jQuery(parentSelector, context).not(excludeParentSelector).find(elementSelector).each(function() {
            var elem = jQuery(this);
            if (elem.is(":enabled, a") && elem.is(":visible")) {
                elem.focus();
                if (elem.is(":text, :password, textarea")) {
                    if (elem.is(".focus-select-end")) {
                        elem.setCaretToPosition(elem[0].value.length);
                    } else {
                        elem.setSelectionRange(0, elem[0].value.length);
                    }
                }
                found = true;
                return false; // break loop, we're done
            }
        });
        return found;
    },

    _defaultFocusNow = function() {
        var i = 0,
            currentConfig = _configurationStack[_configurationStack.length-1];
        while (!_focusIn(currentConfig.context, currentConfig.parentElementSelectors[i],currentConfig.excludeParentSelector,currentConfig.focusElementSelector)
                && i < currentConfig.parentElementSelectors.length) {
            i++;
        }
    };


    return {

        FocusConfiguration: function() {
            this.context = document;
            this.excludeParentSelector = _defaultExcludeParentSelector;
            this.focusElementSelector = _defaultFocusElementSelector;
            this.parentElementSelectors = _defaultParentElementSelectors.slice(0);
            this.focusNow = _defaultFocusNow;
        },

        triggerFocus : function() {
            if (_configurationStack.length == 0) {
                _configurationStack.push(new this.FocusConfiguration());
            }
            _configurationStack[_configurationStack.length-1].focusNow();
        },

        pushConfiguration : function(configuration) {
            _configurationStack.push(configuration);
        },

        popConfiguration : function() {
            _configurationStack.pop();
        }
    };
});

AJS.namespace('JIRA.setFocus', null, require('jira/focus/set-focus'));
