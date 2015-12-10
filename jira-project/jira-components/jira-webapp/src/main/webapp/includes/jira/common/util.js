/*
 * These are defined here for IE8. IE8 contains a bug where the assignment of window.global in one script tag !== global
 * from another. For example consider:
 *     <script>
 *          window.global = true;
 *          console.log(global);
 *      </script>
 *      <script>
 *          if (global === undefined) { var global = false; }
 *          console.log(global);
 *      </script>
 * IE8 will output [true, false] while every other browser outputs [true, true] (including IE9 and IE10). This
 * example is a simplification of what happens when JIRA calls AJS.namespace to define an object that is later added to
 * by soy (e.g. AJS.namespace("jira.app", null, ...) defined in JIRA followed by plugin using soy namespace
 * of {namespace jira.anything}). In this case the soy template will overwrite the object created by AJS.namespace.
 * The simple fix is to define the global variable before it is defined on the window.
 *
 * REF: http://stackoverflow.com/questions/6581566/have-you-ever-seen-this-weird-ie-javascript-behaviour-bug
 * REF: https://extranet.atlassian.com/x/gAAkg
 */
var JIRA = window.JIRA || {};
var jira = window.jira || {}; //Legacy namespace.

(function() {
    var Events = require('jira/util/events');
    // Bind only the legacy so that the JIRA object doesn't implicitly get new methods.
    AJS.namespace('JIRA.bind', null, Events.bind);
    AJS.namespace('JIRA.unbind', null, Events.unbind);
    AJS.namespace('JIRA.one', null, Events.one);
    AJS.namespace('JIRA.trigger', null, Events.trigger);
})();

(function() {
    var User = require('jira/util/users/logged-in-user');
    AJS.namespace('JIRA.isAdmin', null, User.isAdmin);
    AJS.namespace('JIRA.isSysadmin', null, User.isSysadmin);
})();

(function() {
    var E = require('jira/util/elements');
    AJS.namespace('AJS.elementIsFocused', null, E.elementIsFocused);
    AJS.namespace('AJS.consumesKeyboardEvents', null, E.consumesKeyboardEvents);
})();

(function() {
    var B = require('jira/util/browser');
    AJS.namespace('AJS.canAccessIframe', null, B.canAccessIframe);
    AJS.namespace('AJS.isSelenium', null, B.isSelenium);
    AJS.namespace('AJS.reloadViaWindowLocation', null, B.reloadViaWindowLocation);
    AJS.namespace('AJS.enableKeyboardScrolling', null, B.enableKeyboardScrolling);
    AJS.namespace('AJS.disableKeyboardScrolling', null, B.disableKeyboardScrolling);
})();

(function() {
    var O = require('jira/util/objects');
    AJS.namespace("begetObject", window, O.begetObject);
})();

(function() {
    var F = require('jira/util/forms');
    AJS.namespace('submitOnEnter', window, F.submitOnEnter);
    AJS.namespace('submitOnCtrlEnter', window, F.submitOnCtrlEnter);
    AJS.namespace('getMultiSelectValues', window, F.getMultiSelectValues);
    AJS.namespace('getMultiSelectValuesAsArray', window, F.getMultiSelectValuesAsArray);
})();

// Cookie handling functions
(function() {
    var Cookie = require('jira/data/cookie');
    AJS.namespace('saveToConglomerateCookie', window, Cookie.saveToConglomerate);
    AJS.namespace('readFromConglomerateCookie', window, Cookie.readFromConglomerate);
    AJS.namespace('eraseFromConglomerateCookie', window, Cookie.eraseFromConglomerate);
    AJS.namespace('getValueFromCongolmerate', window, Cookie.getValueFromCongolmerate);
    AJS.namespace('addOrAppendToValue', window, Cookie.addOrAppendToValue);
    AJS.namespace('getCookieValue', window, Cookie.getValue);
    AJS.namespace('saveCookie', window, Cookie.save);
    AJS.namespace('readCookie', window, Cookie.read);
    AJS.namespace('eraseCookie', window, Cookie.erase);
})();


// This is here so that we find bugs like JRA-19245 ASAP
jQuery.noConflict();
jQuery.ajaxSettings.traditional = true;

// constants
contextPath = typeof contextPath === "undefined" ? "" : contextPath;
AJS.LEFT = "left";
AJS.RIGHT = "right";
AJS.ACTIVE_CLASS = "active";
AJS.BOX_SHADOW_CLASS = "box-shadow";
AJS.LOADING_CLASS = "loading";
AJS.INTELLIGENT_GUESS = "Intelligent Guess";

(function() {
    var SPECIAL_CHARS = /[.*+?|^$()[\]{\\]/g;
    // Note: This escapes str for regex literal sequences -- not within character classes
    RegExp.escape = function(str) {
        return str.replace(SPECIAL_CHARS, "\\$&");
    };
})();

/**
 * A bunch of useful utilitiy javascript methods available to all jira pages
 */
(function($)
{
    /**
     * Reads data from a structured HTML, such as definition list and build a data object.
     * Even children represent names, odd children represent values.
     *
     * @param s jQuery selector
     * @deprecated
     */
    $.readData = function(s)
    {
        var r = {}, n = "";

        $(s).children().each(function(i)
        {   
            if (i % 2)
            {
                r[n] = jQuery.trim($(this).text());
            }
            else
            {
                n = jQuery.trim($(this).text());
            }
        }).remove();
        $(s).remove();
        return r;
    };
})(jQuery);

/** @deprecated wat */
String.prototype.escapejQuerySelector = function () {
    return this.replace(/([:.])/g, "\\$1");
};

/**
 * This function can extract the BODY tag text from a HttpRequest if its there are otherwise
 * return all the response text.
 * 
 * @param text the AJAX request text returned
 */
AJS.extractBodyFromResponse = function(text)
{
    var fragment = text.match(/<body[^>]*>([\S\s]*)<\/body[^>]*>/);
    if (fragment && fragment.length > 0)
    {
        return fragment[1];
    }
    return text;
};

/** @deprecated */
AJS.escapeHTML = AJS.escapeHtml; // preserve case of JIRA's old name for this function

AJS.isDevMode = function() {
    return AJS.Meta.get("dev-mode");
};

/**
 * Tries to run a function and return its value and if it throws an exception, returns the default value instead
 *
 * @param f the function to try
 * @param defaultVal the default value to return in case of an error
 * @deprecated
 */
function tryIt(f, defaultVal) {
    try {
        return f();
    } catch (ex) {
        return defaultVal;
    }
}

/**
 * Returns true if the value has a truthy equivalent in the array.
 * @deprecated use {@link jQuery.inArray}
 */
function arrayContains(array, value) {
    for (var i = 0, ii = array.length; i < ii; i++) {
        if (array[i] == value) {
            return true;
        }
    }
    return false;
}

/** @deprecated use {@link jQuery.addClass} */
function addClassName(elementId, classNameToAdd) {
    jQuery('#'+elementId).addClass(classNameToAdd);
}

/** @deprecated use {@link jQuery.removeClass} */
function removeClassName(elementId, classNameToRemove) {
    jQuery('#'+elementId).removeClass(classNameToRemove);
}

/**
 * Returns the field as an encoded string (assuming that the id == the field name
 * @deprecated since 6.2
 */
function getEscapedFieldValue(id)
{

    var e = document.getElementById(id);

    if (e.value)
    {
        return id + '=' + encodeURIComponent(e.value);
    }
    else
    {
        return '';
    }
}

/**
 * Returns a concatenated version of getEscapedFieldValue
 * @deprecated since 6.2
 */
function getEscapedFieldValues(ids)
{
    var s = '';
    for (var i = 0; i < ids.length; i++)
    {
        s = s + '&' + getEscapedFieldValue(ids[i]);
    }
    return s;
}

/* Manages Gui Preferences and stores them in the user's cookie. */
var GuiPrefs = {
    toggleVisibility: function(elementId)
    {
        var elem = document.getElementById(elementId);
        if (elem)
        {
            if (readFromConglomerateCookie("jira.conglomerate.cookie", elementId, '1') == '1')
            {
                elem.style.display = "none";
                removeClassName(elementId + 'header', 'headerOpened');
                addClassName(elementId + 'header', 'headerClosed');
                saveToConglomerateCookie("jira.conglomerate.cookie", elementId, '0');
            }
            else
            {
                elem.style.display = "";
                removeClassName(elementId + 'header', 'headerClosed');
                addClassName(elementId + 'header', 'headerOpened');
                eraseFromConglomerateCookie("jira.conglomerate.cookie", elementId);
            }
        }
    }
};

/**
 * Toggles hide / unhide an element. Also attemots to change the "elementId + header" element to have the headerOpened / headerClosed class.
 * Also saves the state in a cookie
 * @deprecated use @see GuiPrefs.toggleVisibility
 */
function toggle(elementId)
{
    GuiPrefs.toggleVisibility(elementId);
}

/** @deprecated */
function toggleDivsWithCookie(elementShowId, elementHideId)
{
    var elementShow = document.getElementById(elementShowId);
    var elementHide = document.getElementById(elementHideId);
    if (elementShow.style.display == 'none')
    {
        elementHide.style.display = 'none';
        elementShow.style.display = 'block';
        saveToConglomerateCookie("jira.viewissue.cong.cookie", elementShowId, '1');
        saveToConglomerateCookie("jira.viewissue.cong.cookie", elementHideId, '0');
    }
    else
    {
        elementShow.style.display = 'none';
        elementHide.style.display = 'block';
        saveToConglomerateCookie("jira.viewissue.cong.cookie", elementHideId, '1');
        saveToConglomerateCookie("jira.viewissue.cong.cookie", elementShowId, '0');
    }
}

/**
 * Similar to toggle. Run this on page load.
 * @deprecated
 */
function restoreDivFromCookie(elementId, cookieName, defaultValue)
{
    if (defaultValue == null)
        defaultValue = '1';

    var elem = document.getElementById(elementId);
    if (elem)
    {
        if (readFromConglomerateCookie(cookieName, elementId, defaultValue) != '1')
        {
            elem.style.display = "none";
            removeClassName(elementId + 'header', 'headerOpened');
            addClassName(elementId + 'header', 'headerClosed');
        }
        else
        {
            elem.style.display = "";
            removeClassName(elementId + 'header', 'headerClosed');
            addClassName(elementId + 'header', 'headerOpened');
        }
    }
}

/**
 * Similar to toggle. Run this on page load.
 * @deprecated
 */
function restore(elementId)
{
    restoreDivFromCookie(elementId, "jira.conglomerate.cookie", '1');
}

// Table colouring functions

/** @deprecated */
function recolourSimpleTableRows(tableId)
{
    recolourTableRows(tableId, "rowNormal", "rowAlternate", tableId + "_empty");
}

/** @deprecated */
function recolourTableRows(tableId, rowNormal, rowAlternate, emptyTableId)
{
    var tbl = document.getElementById(tableId);
    var emptyTable = document.getElementById(emptyTableId);

    var alternate = false;
    var rowsFound = 0;
    var rows = tbl.rows;
    var firstVisibleRow = null;
    var lastVisibleRow = null;

    if (AJS.$(tbl).hasClass('aui')) {
        rowNormal = '';
        rowAlternate = 'zebra';
    }

    for (var i = 1; i < rows.length; i++)
    {
        var row = rows[i];
        if (row.style.display != "none")
        {
            if (!alternate)
            {
                row.className = rowNormal;
            }
            else
            {
                row.className = rowAlternate;
            }
            rowsFound++;
            alternate = !alternate;
        }

        if (row.style.display != "none")
        {
            if (firstVisibleRow == null)
            {
                firstVisibleRow = row;
            }
            lastVisibleRow = row;
        }
    }
    if (firstVisibleRow != null)
    {
        firstVisibleRow.className = firstVisibleRow.className + " first-row";
    }
    if (lastVisibleRow != null)
    {
        lastVisibleRow.className = lastVisibleRow.className + " last-row";
    }

    if (emptyTable)
    {
        if (rowsFound == 0)
        {

            tbl.style.display = "none";
            emptyTable.style.display = "";
        }
        else
        {
            tbl.style.display = "";
            emptyTable.style.display = "none";
        }
    }
}

/** @deprecated use {@see AJS.escapeHtml} */
function htmlEscape(str)
{
    var divE = document.createElement('div');
    divE.appendChild(document.createTextNode(str));
    return divE.innerHTML;
}

/**
 * Returns the meta tag that contains the XSRF atlassian token
 * or undefined if not on page
 */
function atl_token() {
    return jQuery("#atlassian-token").attr('content');
}
