define('jira/jql/jql-grammar', function() {
    "use strict";
    var grammar = {};

    /** @type {JqlObject[]} */
    grammar.jql_order_by = [
        {value: "ORDER BY", displayName: "ORDER BY"}
    ];
    /** @type {JqlObject[]} */
    grammar.jql_operators = [
        {value: "=", displayName: "="},
        {value: "!=", displayName: "!="},
        {value: "~", displayName: "~"},
        {value: "<=", displayName: "&lt;="},
        {value: ">=", displayName: "&gt;="},
        {value: ">", displayName: "&gt;"},
        {value: "<", displayName: "&lt;"},
        {value: "!~", displayName: "!~"},
        {value: "is not", displayName: "is not"},
        {value: "is", displayName: "is"},
        {value: "not in", displayName: "not in"},
        {value: "in", displayName: "in"},
        {value: "was", displayName: "was"},
        {value: "was not", displayName: "was not"},
        {value: "was in", displayName: "was in"},
        {value: "was not in", displayName: "was not in"},
        {value: "changed", displayName: "changed"}
    ];
    /** @type {JqlObject[]} */
    grammar.jql_logical_operators = [
        {value: "AND", displayName: "AND"},
        {value: "OR", displayName: "OR"}
    ];
    /** @type {JqlObject[]} */
    grammar.jql_logical_operators_and_order_by = grammar.jql_logical_operators.concat(grammar.jql_order_by);
    /** @type {JqlObject[]} */
    grammar.jql_order_by_direction = [
        {value: "ASC", displayName: "ASC"},
        {value: "DESC", displayName: "DESC"}
    ];
    /** @type {JqlObject[]} */
    grammar.empty_operand = [
        {value: "EMPTY", displayName: "EMPTY", types: ["java.lang.Object"]}
    ];
    /** @type {JqlObject[]} */
    grammar.jql_not_logical_operator = [
        {value: "NOT", displayName: "NOT"}
    ];
    /** @type {JqlObject[]} */
    grammar.jql_was_predicates = [
        {value: "AFTER", displayName: "AFTER", type: "java.util.Date", supportsList: "false"},
        {value: "BEFORE", displayName: "BEFORE", type: "java.util.Date", supportsList: "false"},
        {value: "BY", displayName: "BY", type: "com.atlassian.crowd.embedded.api.User", supportsList: "false", auto: "true"},
        {value: "DURING", displayName: "DURING", type: "java.util.Date", supportsList: "true"} ,
        {value: "ON", displayName: "ON", type: "java.util.Date", supportsList: "false"}
    ];
    /** @type {JqlObject[]} */
    grammar.jql_changed_predicates = grammar.jql_was_predicates.concat([
        {value: "FROM", displayName: "FROM", type: "java.lang.String", supportsList: "true", auto: "true"},
        {value: "TO", displayName: "TO", type: "java.lang.String", supportsList: "true", auto: "true"}
    ]);
    /** @type {JqlObject[]} */
    grammar.jql_was_predicates_and_order_by = grammar.jql_was_predicates.concat(grammar.jql_logical_operators_and_order_by);
    /** @type {JqlObject[]} */
    grammar.jql_was_predicates_and_logical_operators = grammar.jql_was_predicates.concat(grammar.jql_logical_operators);
    /** @type {JqlObject[]} */
    grammar.jql_changed_predicates_and_order_by = grammar.jql_changed_predicates.concat(grammar.jql_logical_operators_and_order_by);
    /** @type {JqlObject[]} */
    grammar.jql_changed_predicates_and_logical_operators = grammar.jql_changed_predicates.concat(grammar.jql_logical_operators);

    grammar.REGEXP_ANDS = /^AND\s/i;
    grammar.REGEXP_ORS = /^OR\s/i;
    grammar.REGEXP_NOTS = /^NOT\s/i;
    grammar.REGEXP_SNOT = /^\s+not/i;
    grammar.REGEXP_SIN = /^\s+in/i;
    grammar.REGEXP_SNOT_IN = /^\s+(not\s+in|not|in)/i;
    grammar.REGEXP_COMMA_DELIMITER = /^\s*,/;
    grammar.REGEXP_ASCENDING = /^\s+asc/i;
    grammar.REGEXP_DESCENDING = /^\s+desc/i;
    grammar.REGEXP_ORDER_BY = /^order\s+by/i;
    grammar.REGEXP_WHITESPACE = /^\s/;
    grammar.REGEXP_UNICODE = /^u[a-fA-F0-9]{4}/;
    grammar.REGEXP_FIELD_NAME = /^\s-\scf\[\d\d\d\d\d\]/;
    grammar.REGEXP_NEW_LINE = /[\r\n]/;
    grammar.REGEXP_NUMBER = /\d/;
    grammar.REGEXP_TOKEN_CHAR = /[^=!~<>(),\s&|\]]/;
    grammar.REGEXP_SPECIAL_CHAR = /[{}*\/%+$#@?;\][]/;
    grammar.REGEXP_SPACE_OR_ELSE = /[\s(]/;
    grammar.REGEXP_CHARS_TO_ESCAPE = /[^trn"'\\\s]/;
    grammar.REGEXP_NOTSTART = /^NO?$/i;
    grammar.REGEXP_INSTART = /^IN?$/i;
    grammar.REGEXP_PREDICATE = /^(after|before|by|during|from|on|to)/i;

    return grammar;
});

define('jira/autocomplete/jql-autocomplete', [
    'jira/autocomplete/autocomplete',
    'jira/jql/jql-grammar',
    'jira/jql/util',
    'jira/jql/operator-util',
    'jira/util/data/meta',
    'jira/util/objects',
    'jquery'
], function(
    AutoComplete,
    grammar,
    Util,
    OperatorUtil,
    Meta,
    Objects,
    jQuery
) {
    /*jshint bitwise:true, curly:true, eqeqeq:false, eqnull:true, forin:true, noarg:true, noempty:true, nonew:true, undef:true, indent:4, browser:true, jquery:true */

    /**
     * @class JqlObject
     * @extends Object
     * @property {String} value - raw string value for the JQL piece.
     * @property {String} displayName - essentially the HTML escaped equivalent of the {@link JqlObject#value}.
     */

    /**
     * @class JqlFunctionName
     * @extends JqlObject
     * @property isList - "true" or "false"
     * @property {String[]} types - Java class(es) this function object can be type cast to (or was cast from?)
     */

    /**
     * @class JqlFieldName
     * @extends JqlObject
     * @property {String} cfid - typically of the format cf[12345]
     * @property {String[]} operators - list of valid operators that can be used with field operand (e.g., "=", "in", "is", "is not", etc.)
     * @property {String[]} types - Java class(es) this function object can be type cast to (or was cast from?)
     * @property auto - "true" or "false"
     * @property orderable - "true" or "false"
     * @property searchable - "true" or "false"
     */

    /**
     * Makes an input field capable of offering suggestions for JQL syntax and values one might fill in for fields in JQL.
     *
     * @class JQLAutoComplete
     * @extends AutoComplete
     * @param options
     * @param {JQLAutoComplete.MyParser} options.parser - the parser to use for parsing JQL
     * @param {JqlFunctionName[]} options.jqlFunctionNames - Array of of JQL function names
     * @param {JqlFieldName[]} options.jqlFieldNames - Array of JQL fields
     * @param {String} options.autoCompleteUrl - REST endpoint where value suggestions will come from
     * @param {String} options.errorID - ID of the DOM element to output JQL error messages to
     * @returns {Object}
     * @constructor
     */
    var JQLAutoComplete = function(options) {

        var that = Objects.begetObject(AutoComplete);
        var parser = options.parser;
        var result;
        var jql_field_names = jQuery.grep(options.jqlFieldNames, function(arrValue) {
            // We only want the searchable fields
            return arrValue.searchable;
        });
        var jql_order_by_field_names = jQuery.grep(options.jqlFieldNames, function(arrValue) {
            // We only want the orderable fields
            return arrValue.orderable;
        });
        var jql_function_names = options.jqlFunctionNames;
        var PARSE_INDICATOR = jQuery("#" + options.errorID);
        var suggestionCount = 0;
        var jqlcolumnnum = jQuery("#jqlcolumnnum");
        var jqlrownum = jQuery("#jqlrownum");
        var autoCompleteUrl = options.autoCompleteUrl || contextPath + "/rest/api/1.0/jql/autocomplete";

        that.textToSuggestionCursorPosition = function() {
            return that.field.selectionRange().textBefore;
        };


        that.pushSuggestionsOnHtmlStack = function(suggestions, suggestionNodes, htmlParts, listItemIdentifier, mayNeedParenthesis) {
            var length = Math.min(15, suggestions.length);
            for (var i = 0; i < length; i++) {
                var actualValueSug;
                var displayNameSug;
                // We may have an object with displayName and value OR it may just be a string
                if (suggestions[i].value) {
                    var resultSug = suggestions[i];
                    if (result && mayNeedParenthesis) {
                        // This is a hack that adds an initial ( when the value is the first completed in a list
                        // We only do this for suggestions and not moreSuggestions since we know that this is
                        // the position that the values will take, moreSuggestions will be function suggestions
                        actualValueSug = ((result.getNeedsOpenParen()) ? "(" : "") + resultSug.value;
                    }
                    else {
                        actualValueSug = resultSug.value;
                    }
                    displayNameSug = resultSug.displayName;
                }
                else {
                    displayNameSug = suggestions[i];
                    actualValueSug = displayNameSug;
                }
                suggestionNodes.push(actualValueSug);
                htmlParts.push(
                    '<li id="',
                    listItemIdentifier,
                    i,
                    '">',
                    displayNameSug,
                    '</li>'
                );
            }
        };

        /**
         * Order of rendering:
         * 1. Operator suggestions
         * 2. "normal" suggestions
         * 3. More suggestions
         *
         * @param {JqlObject[]} suggestions
         * @param {JqlObject[]} moreSuggestions
         * @param {JqlObject[]} operatorSuggestions
         * @returns {Array}
         */
        that.renderSuggestions = function(suggestions, moreSuggestions, operatorSuggestions) {
            var suggestionNodes = [];

            if (suggestions instanceof Array) {
                if (suggestions.length < 1 && (moreSuggestions && moreSuggestions.length < 1) && (operatorSuggestions && operatorSuggestions.length < 1)) {
                    return suggestionNodes;
                }

                var htmlParts = ['<ul>'];
                if (operatorSuggestions && operatorSuggestions.length > 0) {
                    that.pushSuggestionsOnHtmlStack(operatorSuggestions, suggestionNodes, htmlParts, "jql_operator_suggest_", false);
                }
                that.pushSuggestionsOnHtmlStack(suggestions, suggestionNodes, htmlParts, "jql_value_suggest_", true);
                if (moreSuggestions && moreSuggestions.length > 0) {
                    that.pushSuggestionsOnHtmlStack(moreSuggestions, suggestionNodes, htmlParts, "jql_function_suggest_", false);
                }
                htmlParts.push('</ul>');

                var syntaxHelpLink = jQuery('<a class="syntax-help" target="_jiraHelp"></a>')
                    .attr('title', Meta.get('advanced-search-help-title'))
                    .attr('href', Meta.get('advanced-search-help-url'))
                    .text(AJS.I18n.getText('navigator.advanced.query.syntax'));
                var syntaxHelpContainer = jQuery('<div class="syntax-help-container">').append(syntaxHelpLink);

                that.responseContainer
                    .html(htmlParts.join(''))
                    .append(syntaxHelpContainer)
                    .find('li').each(function(i) {
                        suggestionNodes[i] = [jQuery(this), suggestionNodes[i]];
                    });

                that.addSuggestionControls(suggestionNodes);
            }

            return suggestionNodes;
        };

        that.completeField = function(value) {
            var start = that.getReplacementStartIndex(result, value);

            var end = that.getReplacementEndIndex(result, start);

            that.replaceValue(start, end, value);

            // Parse the whole thing again with the full string so we can set the parse/not parse indicator correctly, possibly a third parse, this is starting to get crazy
            var newToken = parser.parse(that.field.val());
            that.updateParseIndicator(newToken);
        };

        that.prepareOperandSuggestions = function(canAutoComplete, fieldName, functionSuggestions, operatorSuggestions, suggestedValue) {
            if (canAutoComplete) {
                var currentSuggestionCount = suggestionCount;
                var fieldValue = (suggestedValue == null) ? ( (result.getLastOperand() === null) ? "" : Util.stripEscapeCharacters(result.getLastOperand())) : suggestedValue;
                var fieldNameValueKey = fieldName + ":" + fieldValue;
                var data = {fieldName: Util.stripEscapeCharacters(fieldName)};
                if (result.getLastOperand() !== null || suggestedValue !== null) {
                    data.fieldValue = fieldValue;
                }
                var includeEmpty = OperatorUtil.isWasOperator(result.getLastOperator());
                if (!that.getSavedResponse(fieldNameValueKey)) {
                    // We only delay the AJAX request, if it comes back and we have already suggested something else then the results will be dropped because of suggestionCount
                    that.dropdownController.dropdown.removeClass("dropdown-ready");
                    that.delay(function() {
                        that._makeRequest({
                            url: autoCompleteUrl,
                            dataType: "json",
                            data: data,
                            success: function(response) {
                                var results;
                                if (response !== null && response.results !== null) {
                                    results = response.results;
                                }
                                else {
                                    results = [];
                                }
                                // Cache the results
                                //append empty
                                if (includeEmpty) {
                                    var suggestion = data.fieldValue;
                                    results = that.appendEmpty(suggestion, results);
                                }
                                that.saveResponse(fieldNameValueKey, results);
                                that.renderSuggestionsForOperands(fieldValue, results, functionSuggestions, operatorSuggestions, currentSuggestionCount);
                            },
                            error: function() {
                                that.renderSuggestionsForOperands(fieldValue, [], functionSuggestions, operatorSuggestions, currentSuggestionCount);
                            }
                        });
                    }, that.queryDelay);
                }
                else {
                    that.renderSuggestionsForOperands(fieldValue, that.getSavedResponse(fieldNameValueKey), functionSuggestions, operatorSuggestions, currentSuggestionCount);
                }
            }
            else {
                // We should at least render the function suggestions
                that.renderSuggestionsForOperands(Util.stripEscapeCharacters(result.getLastOperand()), [], functionSuggestions, operatorSuggestions, suggestionCount);
            }
        };

        that.preparePredicateSuggestions = function(predicateName, suggestedValue) {
            var currentSuggestionCount = suggestionCount;
            if (typeof suggestedValue == "undefined") {
                suggestedValue = null;
            }
            var functionSuggestions = that.slimListForPredicates(suggestedValue, jql_function_names, predicateName);
            that.boldMatchingString(suggestedValue, functionSuggestions);
            var predicateValue = (suggestedValue == null) ? ( (result.getLastOperand() === null) ? "" : Util.stripEscapeCharacters(result.getLastOperand())) : suggestedValue;
            var predicateNameValueKey = predicateName + ":" + predicateValue;
            var data = {predicateName: Util.stripEscapeCharacters(predicateName)};
            if (result.getLastOperand() !== null || suggestedValue !== null) {
                data.predicateValue = predicateValue;
            }
            if (result.getLastFieldName() !== null) {
                data.fieldName = result.getLastFieldName();
                predicateNameValueKey = result.getLastFieldName() + ":" + predicateNameValueKey;
            }
            if (that.predicateSupportsAutoComplete(predicateName)) {
                if (!that.getSavedResponse(predicateNameValueKey)) {
                    // We only delay the AJAX request, if it comes back and we have already suggested something else then the results will be dropped because of suggestionCount
                    that.dropdownController.dropdown.removeClass("dropdown-ready");
                    that.delay(function() {
                        that._makeRequest({
                            url: autoCompleteUrl,
                            dataType: "json",
                            data: data,
                            success: function(response) {
                                var results;
                                if (response !== null && response.results !== null) {
                                    results = response.results;
                                }
                                else {
                                    results = [];
                                }
                                // Cache the results
                                //append empty
                                var suggestion = data.predicateValue;
                                results = that.appendEmpty(suggestion, results);
                                that.saveResponse(predicateNameValueKey, results);
                                that.renderSuggestions(results, functionSuggestions);
                            },
                            error: function() {
                                that.renderSuggestionsForOperands(predicateValue, []);
                            }
                        });
                    }, that.queryDelay);
                }
                else {
                    that.renderSuggestions(that.getSavedResponse(predicateNameValueKey), functionSuggestions);
                }
            }
            else {
                // simply render functions
                that.renderSuggestions([], functionSuggestions);
            }
        };

        that.incompleteOperator = function() {
            var tokens = result.getTokens();
            if (tokens == null || tokens.length < 2) {
                return "";
            }
            else {
                tokens = that.textToSuggestionCursorPosition().split(" ");
                if (tokens.length > 2) {
                    return tokens[tokens.length - 1];
                }
            }
            return "";
        };

        that.incompletePredicateValue = function() {
            var tokens = result.getTokens();
            if (tokens == null || tokens.length < 3) {
                return "";
            }
            else {
                tokens = that.textToSuggestionCursorPosition().split(/[\s(,]+/);
                if (tokens.length > 3) {
                    return tokens[tokens.length - 1];
                }
            }
            return "";
        };

        that.appendEmpty = function(suggestion, results) {
            var empty_list = that.slimListForMapResults(suggestion, grammar.empty_operand, true);
            if (suggestion != null) {
                that.boldMatchingString(suggestion, empty_list);
            }
            if (results.length < 15) {
                results = results.concat(empty_list);
            }
            else {
                results[14] = empty_list[0];
            }
            return results;
        };

        that.dispatcher = function(val) {

            var selectionRange = jQuery(that.field).selectionRange();
            var parseValue = val.substring(0, selectionRange.start);

            var value;
            var functionSuggestions;

            var token = parser.parse(parseValue);

            result = token.getResult();
            // We will always make a suggestion from here so lets increment the count
            suggestionCount++;
            // In this case we suggest operators
            if (result.getNeedsField()) {
                that.renderSuggestionsFromMap(Util.stripEscapeCharacters(result.getLastFieldName()), jql_field_names, grammar.jql_not_logical_operator, true);
            }
            // for was queries you msay need either an operator or operand
            else if (result.getNeedsOperatorOrOperand()) {
                functionSuggestions = (OperatorUtil.isEmptyOnlyOperator(result.getLastOperator())) ? grammar.empty_operand : jql_function_names;

                token.calcLastOperandStartIndex();
                var parsedOperator = token.getParsedOperator();
                var operatorSuggestions = that.getSuggestionsForOperators(parsedOperator, grammar.jql_operators);

                if (operatorSuggestions && operatorSuggestions.length > 0) {
                    var operandSuggestion = that.incompleteOperator();
                    that.prepareOperandSuggestions(true, result.getLastFieldName(), functionSuggestions, operatorSuggestions, operandSuggestion);
                }
            }
            else if (result.getNeedsOperator()) {
                that.renderSuggestionsForOperators(result.getLastOperator(), grammar.jql_operators);
            }
            else if (result.getNeedsPredicateOperand()) {
                var predicate = result.getLastWasPredicate();
                that.preparePredicateSuggestions(predicate, Util.stripEscapeCharacters(result.getLastOperand()));
            }
            else if (result.getNeedsLogicalOperator()) {
                if (result.getNeedsWasPredicate()) {
                    if (result.getNeedsOrderBy()) {
                        value = (result.getLastOrderBy() === null) ? result.getLastWasPredicate() : result.getLastOrderBy();
                        if (result.getLastOperator() == 'was') {
                            that.renderSuggestionsFromMap(value, grammar.jql_was_predicates_and_order_by, false);
                        }
                        else {
                            that.renderSuggestionsFromMap(value, grammar.jql_changed_predicates_and_order_by, false);
                        }
                    }
                    else {
                        if (result.getLastOperator() == 'was') {
                            that.renderSuggestionsFromMap(result.getLastWasPredicate(), grammar.jql_was_predicates_and_logical_operators, false);
                        }
                        else {
                            that.renderSuggestionsFromMap(result.getLastWasPredicate(), grammar.jql_changed_predicates_and_logical_operators, false);
                        }
                    }

                }
                else {
                    if (result.getNeedsOrderBy()) {
                        value = (result.getLastOrderBy() === null) ? result.getLastLogicalOperator() : result.getLastOrderBy();
                        that.renderSuggestionsFromMap(value, grammar.jql_logical_operators_and_order_by, false);
                    }
                    else {
                        that.renderSuggestionsFromMap(result.getLastLogicalOperator(), grammar.jql_logical_operators, false);
                    }
                }
            }
            else if (result.getNeedsOrderByField()) {
                that.renderSuggestionsFromMap(Util.stripEscapeCharacters(result.getLastOrderByFieldName()), jql_order_by_field_names, true);
            }
            else if (result.getNeedsOrderByDirection()) {
                that.renderSuggestionsFromMap(result.getLastOrderByDirection(), grammar.jql_order_by_direction, false);
            }
            else if (result.getNeedsOperand()) {
                var fieldName = result.getLastFieldName();

                var canAutoComplete = false;
                // If we know that we are only suggesting empty then we do not need to suggest values
                if (!OperatorUtil.isEmptyOnlyOperator(result.getLastOperator())) {
                    for (var i = 0; i < jql_field_names.length; i++) {
                        if (Util.equalsIgnoreCase(result.getUnquotedString(jql_field_names[i].value), fieldName) ||
                            (jql_field_names[i].cfid && Util.equalsIgnoreCase(jql_field_names[i].cfid, fieldName))) {
                            canAutoComplete = jql_field_names[i].auto;
                            break;
                        }
                    }
                }

                functionSuggestions = (OperatorUtil.isEmptyOnlyOperator(result.getLastOperator())) ? grammar.empty_operand : jql_function_names;
                that.prepareOperandSuggestions(canAutoComplete, fieldName, functionSuggestions, {});
            }
            else if (result.getNeedsOrderBy()) {
                that.renderSuggestionsFromMap(result.getLastOrderBy(), grammar.jql_logical_operators_and_order_by, false);
            }
            else {
                that.dropdownController.hideDropdown();
            }

            // Need to update the parse/not parse indicator
            that.parse(val);
        };

        that.parse = function(val) {
            var newToken = parser.parse(val);
            that.updateParseIndicator(newToken);
            that.updateColumnLineCount();
            return newToken.getResult();
        };

        that.renderSuggestionsFromMap = function(stringVal, list, otherSuggestions, showFull) {
            if (!otherSuggestions) {
                otherSuggestions = {};
            }
            var suggestions = that.slimListForMapResults(stringVal, list, showFull);
            var relevantOtherSuggestions = that.slimListForMapResults(stringVal, otherSuggestions, showFull);
            that.boldMatchingString(stringVal, relevantOtherSuggestions);
            that.boldMatchingString(stringVal, suggestions);

            that.renderSuggestions(suggestions, relevantOtherSuggestions);
            if (suggestions.length === 0 && relevantOtherSuggestions.length === 0) {
                that.dropdownController.hideDropdown();
            }
        };

        that.getSuggestionsForOperators = function(stringVal, list) {
            var suggestions = that.slimListForMapResults(stringVal, list, false);

            var fieldName = result.getLastFieldName();
            // Find the current field, if we know about it and get the supported operators
            var supportedOperators;
            for (var i = 0; i < jql_field_names.length; i++) {
                if (Util.equalsIgnoreCase(result.getUnquotedString(jql_field_names[i].value), fieldName) ||
                    (jql_field_names[i].cfid && Util.equalsIgnoreCase(jql_field_names[i].cfid, fieldName))) {
                    supportedOperators = jql_field_names[i].operators;
                    break;
                }
            }

            // Now lets run through the remaining list and if we can identify the field we can see if that field
            // supports which operators
            if (supportedOperators) {
                suggestions = jQuery.grep(suggestions, function(arrValue) {
                    return jQuery.inArray(arrValue.value, supportedOperators) > -1;
                });
            }
            that.boldMatchingString(stringVal, suggestions);
            return suggestions;
        };

        that.renderSuggestionsForOperators = function(stringVal, list) {
            var suggestions = that.getSuggestionsForOperators(stringVal, list);
            that.renderSuggestions(suggestions);
            if (suggestions.length === 0) {
                that.dropdownController.hideDropdown();
            }
        };

        that.renderSuggestionsForOperands = function(stringVal, ajaxSuggestions, functions, operatorSuggestions, providedSuggestionCount) {
            // Only render the suggestions if we are the current suggestion
            if (providedSuggestionCount === suggestionCount) {
                // Don't need to slim the ajaxSuggestions since they came from the server slimmed down
                var functionsSuggestions = that.slimListForFunctionResults(stringVal, functions, result.getLastOperator());
                that.boldMatchingString(stringVal, functionsSuggestions);
                that.renderSuggestions(ajaxSuggestions, functionsSuggestions, operatorSuggestions);

                if (ajaxSuggestions.length === 0 && functionsSuggestions.length === 0 && operatorSuggestions.length == 0) {
                    that.dropdownController.hideDropdown();
                }
            }
        };

        // This bolds the beginning portion of the matching string and converts the list to be value/displayName. This
        // assumes that all strings in the list have already been confirmed to match the incomming stringVal
        that.boldMatchingString = function(stringVal, list) {
            if (stringVal == null || list.length === 0) {
                return;
            }

            var boldLength = stringVal.length;
            // Run through all the characters looking for html escape characters so we can include their extra length in
            // the bold length
            for (var j = 0, n = boldLength; j < n; j++) {
                switch (stringVal.charAt(j)) {
                    case "<":
                    case ">":
                        // We have one character representing this already, lets add the other 3 for &lt; or &gt;
                        boldLength += 3;
                        break;
                    case "&":
                        // We have one character representing this already, lets add the other 4 for &amp;
                        boldLength += 4;
                        break;
                    case '"':
                        // We have one character representing this already, lets add the other 5 for &quot;
                        boldLength += 5;
                        break;
                }
            }

            for (var i = 0; i < list.length; i++) {
                if (list[i].displayName) {
                    var origVal = list[i].displayName;
                    // Create a new copy of the object so we don't mess up the original list
                    list[i] = {value: list[i].value, displayName: "<b>" + origVal.substring(0, boldLength) + "</b>" + origVal.substring(boldLength)};
                }
                else {
                    // Add a displayName so we don't mess up the value
                    list[i] = {value: list[i], displayName: "<b>" + list[i].substring(0, boldLength) + "</b>" + list[i].substring(boldLength)};
                }
            }
        };

        that.getReplacementStartIndex = function(result, value) {
                var jQueryReference = jQuery(that.field);
                var start;
                if (result.getNeedsField()) {
                    start = result.getLastFieldNameStartIndex();
                }
                else if (result.getNeedsOperatorOrOperand()) {
                    // was presents difficulties - it may be followed by either an operand or an operator
                    if (result.getNeedsOperator() && OperatorUtil.isWasOperator(value)) {
                        start = result.getLastOperatorStartIndex();
                    }
                    else {
                        start = result.getLastOperandStartIndex();
                    }
                }
                else if (result.getNeedsOperand()) {
                    start = result.getLastOperandStartIndex();
                }
                else if (result.getNeedsOperator()) {
                    start = result.getLastOperatorStartIndex();
                }
                else if (result.getNeedsOrderByField()) {
                    start = result.getLastOrderByFieldNameStartIndex();
                }
                else if (result.getNeedsOrderByDirection()) {
                    start = result.getLastOrderByDirectionStartIndex();
                }
                else if (result.getNeedsPredicateOperand()) {
                    start = result.getLastOperandStartIndex();
                }
                else if (result.getNeedsLogicalOperator()) {
                    if (result.getLastLogicalOperatorStartIndex() !== null && result.getLastLogicalOperatorStartIndex() !== 0) {
                        start = result.getLastLogicalOperatorStartIndex();
                    }
                    else {
                        if (result.getLastOrderBy() !== null && result.getLastOrderByStartIndex() !== 0) {
                            start = result.getLastOrderByStartIndex();
                        }
                        else if (result.getLastWasPredicate() != null && result.getLastWasPredicateStartIndex() !== 0) {
                            start = result.getLastWasPredicateStartIndex();
                        }
                        else if (result.getMustBeOperatorOrPredicate() === true) {
                            start = jQueryReference.selectionRange().start;
                        }
                        else {
                            start = jQueryReference.selectionRange().start - 1;
                        }
                    }
                }
                else if (result.getNeedsWasPredicate()) {
                    start = result.getLastWasPredicateStartIndex();
                }
                else if (result.getNeedsOrderBy()) {
                    start = result.getLastOrderByStartIndex();
                }
                else {
                    start = jQueryReference.selectionRange().start - 1;
                }
                // sanity check
                return start != null ? start : jQueryReference.selectionRange().start;
            };

        that.getReplacementEndIndex = function(result, start) {
            var jQueryReference = jQuery(that.field);
            var selectionRange = jQueryReference.selectionRange();
            var end = null;
            // We only need to do a second parse if we have no highlighted selection AND we are not at the end of the input string
            // otherwise we just use the selectionEnd
            if (selectionRange.start === selectionRange.end && selectionRange.end !== that.field.val().length) {
                // Lets get the token number from the first parse, this is the token we are currently on
                var currentTokenIdx = result.getTokens().length - 1;

                // Parse it again, but this time the full string so we know what the full token is that we are trying to replace
                // with the selected completion
                var token = parser.parse(that.field.val());
                if (!token.getParseError()) {
                    // The user has not highlighted text so lets assume we are completing to the end of the current token
                    // Ask the newly parsed result for the complete token we are in the middle of
                    var fullTokenValue = token.getResult().getTokens()[currentTokenIdx];
                    // This is a special case, we don't want to replace the '(' or ')' instead we want to add inside
                    if (fullTokenValue !== null && fullTokenValue !== '(' && fullTokenValue !== ')') {
                        var fullTextVal = that.field.val();
                        // Lets get the start position in the string
                        var remainingString = fullTextVal.substring(start, fullTextVal.length);
                        // We know that we are going to see the fullTokenValue next, but there may be some whitespace between
                        // here and there, lets make sure we ditch the whitespace as well.
                        var remainingStringArr = remainingString.split("");
                        var whitespaceCount = 0;
                        for (var i = 0; i < remainingStringArr.length; i++) {
                            if (grammar.REGEXP_WHITESPACE.test(remainingStringArr[i])) {
                                whitespaceCount++;
                            }
                            else {
                                // Stop as soon as we no longer see whitespace
                                break;
                            }
                        }
                        end = start + fullTokenValue.length + whitespaceCount;
                    }
                }
            }

            if (end === null) {
                end = selectionRange.end;
            }
            // As the start position could have been incremented to account for spaces in operators
            // e.g. was not in, it is feasible that end may be less than start so a quick check is in order.
            // If start is less than end pad with a space (one is sufficient)
            if (end < start) {
                that.replaceValue(start, end, " ");
                end = start;
            }
            return end;
        };

        that.replaceValue = function(start, end, newValue) {
            var jQueryReference = jQuery(that.field);
            // Lets reset the selection range to include the characters that the user has already typed
            jQueryReference.selectionRange(start, end);
            // Lets replace the value with the autocomplete selected value
            jQueryReference.selection(newValue);
            // Lets stop the replaced bit from being highlighted
            var newEnd = jQueryReference.selectionRange().end;
            jQueryReference.selectionRange(newEnd, newEnd);
        };

        that.slimListForMapResults = function(stringVal, list, showFull) {
            var escString = Util.htmlEscape(stringVal);
            var slimedList = jQuery.grep(list, function(arrValue) {
                return Util.startsWithIgnoreCaseNullsMeanAll(escString, arrValue.displayName);
            });

            if (!showFull) {
                // We only want to show the value when the user has fully typed it in IF there are more than one suggestion
                // with this prefix.
                if (slimedList.length === 1 && !Util.startsWithNotEqualsIgnoreCaseNullMeansAll(escString, slimedList[0].displayName)) {
                    return {};
                }
            }
            return slimedList;
        };

        that.slimListForFunctionResults = function(stringVal, list, operator) {
            var fieldName = result.getLastFieldName();
            // Find the current field, if we know about it and get the supported types
            var supportedTypes;
            for (var i = 0; i < jql_field_names.length; i++) {
                if (Util.equalsIgnoreCase(result.getUnquotedString(jql_field_names[i].value), fieldName) ||
                    (jql_field_names[i].cfid && Util.equalsIgnoreCase(jql_field_names[i].cfid, fieldName))) {
                    supportedTypes = jql_field_names[i].types;
                    break;
                }
            }

            var slimedList = jQuery.grep(list, function(arrValue) {
                // For functions we only want to show the is list ones with list operators and vice versa
                if ((arrValue.isList && !OperatorUtil.isListSupportingOperator(operator)) ||
                    (!arrValue.isList && OperatorUtil.isListSupportingOperator(operator))) {
                    return false;
                }
                if (supportedTypes) {
                    // Need to check for Object since this means we always fit
                    var supportsFunction = jQuery.inArray("java.lang.Object", arrValue.types) > -1 || jQuery.inArray("java.lang.Object", supportedTypes) > -1;
                    for (var i = 0; i < supportedTypes.length && !supportsFunction; i++) {
                        supportsFunction = jQuery.inArray(supportedTypes[i], arrValue.types) !== -1;
                    }
                    if (!supportsFunction) {
                        return false;
                    }
                }
                else {
                    // Can't find the field so we know that no functions will work with it
                    return false;
                }
                return Util.startsWithIgnoreCaseNullsMeanAll(stringVal, arrValue.value) || Util.startsWithIgnoreCaseNullsMeanAll(stringVal, arrValue.displayName);
            });

            // We only want to show the value when the user has fully typed it in IF there are more than one suggestion
            // with this prefix.
            if (slimedList.length === 1 && !Util.startsWithNotEqualsIgnoreCaseNullMeansAll(stringVal, slimedList[0].displayName)) {
                return {};
            }
            return slimedList;
        };

        that.slimListForPredicates = function(stringVal, list, predicate) {
            var supportedType;
            var supportsList;
            for (var i = 0; i < grammar.jql_changed_predicates.length; i++) {
                if (Util.equalsIgnoreCase(result.getUnquotedString(grammar.jql_changed_predicates[i].value), predicate)) {
                    supportedType = grammar.jql_changed_predicates[i].type;
                    supportsList = grammar.jql_changed_predicates[i].supportsList;
                    break;
                }
            }

            var slimedList = jQuery.grep(list, function(arrValue) {
                // For functions we only want to show the is list ones with list operators and vice versa

                if (typeof arrValue.isList != "undefined") {
                    if ((arrValue.isList && !!supportsList) || (!arrValue.isList && supportsList)) {
                        return false;
                    }
                }
                var supportsFunction = jQuery.inArray(supportedType, arrValue.types) !== -1;
                return supportsFunction ? Util.startsWithIgnoreCaseNullsMeanAll(stringVal, arrValue.value) || Util.startsWithIgnoreCaseNullsMeanAll(stringVal, arrValue.displayName) : false;
            });

            // We only want to show the value when the user has fully typed it in IF there are more than one suggestion
            // with this prefix.
            if (slimedList.length === 1 && !Util.startsWithNotEqualsIgnoreCaseNullMeansAll(stringVal, slimedList[0].displayName)) {
                return {};
            }
            return slimedList;
        };

        that.predicateSupportsAutoComplete = function(predicate) {
            var auto;
            for (var i = 0; i < grammar.jql_changed_predicates.length; i++) {
                if (Util.equalsIgnoreCase(result.getUnquotedString(grammar.jql_changed_predicates[i].value), predicate)) {
                    auto = grammar.jql_changed_predicates[i].auto;
                    break;
                }
            }
            return auto;

        };

        /**
         * Gets cached response from <em>requested</em> object
         * @method {public} getSavedResponse
         * @param {String} val
         * @returns {Object}
         */
        that.getSavedResponse = function(val) {
            if (!this.requested) {
                this.requested = {};
            }
            return this.requested[val];
        };

        /**
         * Saves response to <em>requested</em> object
         * @method {public} saveResponse
         * @param {String} val
         * @param {Object} response
         */
        that.saveResponse = function(val, response) {
            if (typeof val === "string" && typeof response === "object") {
                if (!this.requested) {
                    this.requested = {};
                }
                this.requested[val] = response;
            }
        };

        that.startsWithIgnoreCase = function(startStr, str) {
            if (str === null || startStr === null || str.length < startStr.length) {
                return false;
            }
            else {
                return startStr.toLowerCase() == str.substr(0, startStr.length).toLowerCase();
            }
        };

        that.updateParseIndicator = function(token) {
            if (token.getParseError()) {
                PARSE_INDICATOR.attr("title", token.getResult().getParseErrorMsg()).removeClass("jqlgood").addClass("jqlerror");
            }
            else {
                PARSE_INDICATOR.attr("title", "").removeClass("jqlerror").addClass("jqlgood");
            }
        };

        that.updateColumnLineCount = function() {
            var jQueryReference = jQuery(that.field);
            var totalCharCountToCursor = 0;

            if (that.field[0] === document.activeElement) {
                var selectionRange = jQueryReference.selectionRange();
                totalCharCountToCursor = selectionRange.start;
            }
            else {
                totalCharCountToCursor = that.field[0].value.length;
            }
            var rowCount = 1;
            var colCount = 1;

            var fieldValue = that.field.val();

            for (var i = 0; i < totalCharCountToCursor; i++) {
                if (grammar.REGEXP_NEW_LINE.test(fieldValue.charAt(i))) {
                    rowCount++;
                    colCount = 1;
                }
                else {
                    colCount++;
                }
            }

            // Update our counts for where our cursor is at the moment
            jqlcolumnnum.text(colCount);
            jqlrownum.text(rowCount);
        };

        that.init(options);

        return that;
    };

    return JQLAutoComplete;
});

define('jira/jql/util', [
    'jira/jql/jql-grammar'
], function(
    grammar
) {
    "use strict";
    var JQLAutoCompleteUtil = {};

    JQLAutoCompleteUtil.htmlEscape = function(stringVal) {
        if (stringVal == null) {
            return null;
        }
        var escapedVal = "";
        var strArr = stringVal.split("");
        // Run through all the characters looking for html escape characters so we can include their extra length in
        // the bold length
        for (var j = 0; j < strArr.length; j++) {
            if (strArr[j] === "<") {
                escapedVal += "&lt;";
            }
            else if (strArr[j] === ">") {
                escapedVal += "&gt;";
            }
            else if (strArr[j] === "&") {
                // We have one character representing this already, lets add the other 4 for &amp;
                escapedVal += "&amp;";
            }
            else if (strArr[j] === "\"") {
                // We have one character representing this already, lets add the other 5 for &quot;
                escapedVal += "&quot;";
            }
            else {
                escapedVal += strArr[j];
            }
        }
        return escapedVal;
    };

    JQLAutoCompleteUtil.stripEscapeCharacters = function(val) {

        if (val == null) {
            return val;
        }
        var newVal = "";
        var strArr = val.split("");
        for (var i = 0; i < strArr.length; i++) {
            if (strArr[i] == '\\') {
                // If we are a unicode string then we just consume it like normal
                if (!grammar.REGEXP_UNICODE.test(val.substring(i, val.length))) {
                    // Just chew past the escape and use the next char, the parser has already made sure this is cool
                    i++;
                    if (i >= val.length) {
                        break;
                    }
                }
            }
            newVal += strArr[i];
        }
        return newVal;
    };

    JQLAutoCompleteUtil.equalsIgnoreCase = function(str1, str2) {
        if (str1 === null && str2 === null) {
            return true;
        }
        else if (str1 === null || str1 === null) {
            return false;
        }
        else {
            return str1.toLowerCase() === str2.toLowerCase();
        }
    };

    JQLAutoCompleteUtil.startsWithIgnoreCaseNullsMeanAll = function(startStr, str) {
        // In this case we want all elements of the list included
        if (str === null || startStr === null) {
            return true;
        }
        if (str.length < startStr.length) {
            return false;
        }
        else {
            return startStr.toLowerCase() == str.substr(0, startStr.length).toLowerCase();
        }
    };

    JQLAutoCompleteUtil.startsWithNotEqualsIgnoreCaseNullMeansAll = function(startStr, str) {
        // In this case we want all elements of the list included
        if (str === null || startStr === null) {
            return true;
        }
        if (str.length < startStr.length) {
            return false;
        }
        else {
            var subStrEquals = startStr.toLowerCase() == str.substr(0, startStr.length).toLowerCase();
            if (subStrEquals) {
                // HACK!! This is a hack so that the custom field display values will not show up as a suggestion when they
                // are completely typed in.
                var equalsString = null;
                if (grammar.REGEXP_FIELD_NAME.test(str.substr(startStr.length, str.length))) {
                    equalsString = str.substr(0, startStr.length);
                }
                else {
                    equalsString = str;
                }
                return startStr.toLowerCase() != equalsString.toLowerCase();
            }
            return false;
        }
    };

    return JQLAutoCompleteUtil;
});

define('jira/jql/operator-util', function() {
    "use strict";

    var that = {};

    that.isListSupportingOperator = function(operator) {
        return operator === 'in' || operator === 'not in' || operator == 'was not in' || operator == 'was in';
    };

    that.isEmptyOnlyOperator = function(operator) {
        return operator === 'is' || operator === 'is not';
    };

    that.isWasOperator = function(operator) {
        return operator === 'was' || operator === 'was in' || operator == 'was not in' || operator == 'was not';
    };

    that.isChangedOperator = function(operator) {
        return operator === 'changed';
    };

    that.isHistoryOperator = function(operator) {
        return that.isWasOperator(operator) || that.isChangedOperator(operator);
    };

    return that;
});

define('jira/jql/jql-parser', [
    'jira/jql/jql-grammar',
    'jira/jql/jql-parse-token',
    'jira/jql/operator-util',
    'jquery'
], function(
    grammar,
    Token,
    OperatorUtil,
    jQuery
) {
    /*jshint bitwise:true, curly:true, eqeqeq:false, eqnull:true, forin:true, noarg:true, noempty:true, nonew:true, undef:true, indent:4 */

    /**
     * @param {String[]} jqlReservedWords - list of words that have special meaning in the JQL language
     * @returns {JQLAutoComplete.MyParser}
     * @constructor
     */
    return function(jqlReservedWords) {

        var jql_reserved_words = jqlReservedWords;

        /**
         * @class JQLAutoComplete.MyParser
         */
        return {
            parse: function(input) {
                var token = Token();

                token.init(input);
                this.jql(token);
//            token.toString();
                return token;
            },

            orderByClause: function(token) {
                var remainingString = token.remainingString();
                // Lets consume the 'order' token
                var matchArray = remainingString.match(grammar.REGEXP_ORDER_BY);

                if (matchArray) {
                    var orderByString = remainingString.substring(0, matchArray[0].length);
                    token.consumeCharacters(orderByString.length);
                    token.getResult().setLastOrderBy(orderByString, token);

                    if (!token.isComplete()) {
                        // We must have some space in order to parse an order by fields
                        remainingString = token.remainingString();
                        if (grammar.REGEXP_WHITESPACE.test(remainingString)) {
                            this.chewWhitespace(token);
                            // Look for order by fields
                            this.orderByFields(token);
                        }
                        else {
                            token.getResult().resetLogicalOperators();
                            token.setParseError();
                        }
                    }
                    else {
                        token.getResult().resetLogicalOperators();
                        token.setParseError();
                    }
                }
                else {
                    // Consume the remaining string
                    token.consumeCharacters(remainingString.length);
                    token.getResult().setLastOrderBy(remainingString, token);
                    token.setParseError();
                }
            },

            orderByFields: function(token) {
                // Look for fields followed by 'asc' or 'desc' or commas
                this.orderByField(token);
                this.chewWhitespace(token);
                if (token.isComplete()) {
                    // We always need to check to see if we need to put in a place-holder for the order by direction
                    if (!token.getResult().getNeedsOrderByField()) {
                        token.getResult().setLastOrderByDirection("", token);
                        // This is not a parse error, it is just a token place holder so we will complete correctly
                    }
                }
                else {
                    // Look for a comma
                    var remainingString = token.remainingString();
                    if (this.startsWithIgnoreCase(",", remainingString)) {
                        token.consumeCharacter();
                        // Lets recurse back looking for more order by fields
                        this.orderByFields(token);
                        this.chewWhitespace(token);
                    }
                    else {
                        remainingString = token.remainingString();
                        if (remainingString !== null) {
                            token.consumeCharacters(remainingString.length);
                        }
                        token.getResult().setNeedsOrderByDirection();
                        token.getResult().setLastOrderByDirection(remainingString, token);
                        token.setParseError();
                    }
                }
            },

            orderByField: function(token) {
                this.chewWhitespace(token);
                var fieldName = this.fieldName(token);
                if (fieldName.length !== 0) {
                    token.getResult().setLastOrderByFieldName(fieldName, token);

                    // If we see a comma then we don't have an order by direction
                    var remainingString = token.remainingString();

                    if (!token.isComplete() && !grammar.REGEXP_COMMA_DELIMITER.test(remainingString)) {
                        // Better look for a direction, but it is optional so no big deal if we do not find it
                        // We need a space between the order by field name and the direction
                        if (grammar.REGEXP_ASCENDING.test(remainingString)) {
                            this.chewWhitespace(token);
                            // consume and move on
                            token.consumeCharacters(3);
                            token.getResult().setLastOrderByDirection("asc", token);
                            token.getResult().setNeedsOrderByComma();
                        }
                        else if (grammar.REGEXP_DESCENDING.test(remainingString)) {
                            this.chewWhitespace(token);
                            // consume and move on
                            token.consumeCharacters(4);
                            token.getResult().setLastOrderByDirection("desc", token);
                            token.getResult().setNeedsOrderByComma();
                        }
                        else {
                            token.getResult().setNeedsOrderByDirection();
                        }
                    }
                    else {
                        token.getResult().setNeedsOrderByField();
                        this.chewWhitespace(token);
                    }
                }
                else {
                    token.getResult().setLastOrderByFieldName("", token);
                    token.getResult().setNeedsOrderByField();
                    token.setParseError();
                }
            },

            jql: function(token) {
                this.orClause(token);
                var remainingString = token.remainingString();
                if (this.startsWithIgnoreCase("ord", remainingString)) {
                    this.orderByClause(token);
                }
            },

            orClause: function(token) {

                while (!token.isComplete() && !this.startsWithIgnoreCase(")", token.remainingString())) {
                    this.chewWhitespace(token);
                    var remainingString = token.remainingString();
                    if (this.startsWithIgnoreCase("ord", remainingString)) {
                        // lets give control back to the jql function so we can end up in the orderByClause
                        break;
                    }
                    if (token.getResult().getMustBeOperatorOrPredicate()) {
                        token.getResult().resetLastPredicates();
                    }
                    else {
                        token.getResult().resetLogicalOperators();
                        this.andClause(token);
                    }
                    if (!token.isComplete() && token.getResult().getLastLogicalOperator() === null) {
                        if (token.getResult().getNeedsWasPredicate()) {
                            this.predicateClause(token);
                        }
                        // we may as well exit early if the predicate clause has been handled
                        // and the token is complete
                        if (token.isComplete()) {
                            return;
                        }
                        // Look for an OR clause
                        remainingString = token.remainingString();
                        if (this.startsWithIgnoreCase("ord", remainingString)) {
                            // lets give control back to the jql function so we can end up in the orderByClause
                            break;
                        }
                        else if (remainingString !== null && (grammar.REGEXP_ORS.test(remainingString) || this.startsWithIgnoreCase("|", remainingString) || this.startsWithIgnoreCase("||", remainingString))) {
                            if (this.startsWithIgnoreCase("||", remainingString)) {
                                token.getResult().setLastLogicalOperator("||", token.getTokenStringIdx());
                                token.consumeCharacters(2);
                            }
                            else if (grammar.REGEXP_ORS.test(remainingString)) {
                                token.getResult().setLastLogicalOperator("OR", token.getTokenStringIdx());
                                token.consumeCharacters(3);
                            }
                            else {
                                token.getResult().setLastLogicalOperator("|", token.getTokenStringIdx());
                                token.consumeCharacters(1);
                            }

                            token.getResult().resetTerminalClause();
                            if (token.isComplete()) {
                                token.setParseError();
                            }
                            token.getResult().setNeedsField();
                        }
                        // This block of code is very confusing. The reason it is here is that when we are in a nested
                        // block of '('s it is the terminalClause that handles the parens and it calls off to this orClause.
                        // The orClause needs to ignore the close paren so that the terminalClause can handle it, BUT only
                        // when we are currently in a set of parens. So, ff the remaining character is a close paren and
                        // we are not in parens then we are NOT in error, otherwise we are
                        else if (!this.startsWithIgnoreCase(")", remainingString) || !token.getInParens()) {
                            this.chewWhitespace(token);
                            var errorIdx = (remainingString === null) ? token.getMaxTokenStringIdx() : token.getMaxTokenStringIdx() - remainingString.length;
                            // let's tokenise this to simplify space handling
                            var tokens;
                            if (remainingString != null) {
                                tokens = remainingString.split(" ");
                                token.getResult().setLastLogicalOperator(tokens[0], errorIdx);
                                token.getResult().setLastWasPredicate(tokens[0], token);
                                token.getResult().setNeedsLogicalOperator(token);
                            }

                            // may need to go around again to check for predicate
                            if (token.getResult().getLastWasOperator() == null) {
                                token.setParseError();
                            }
                            else {
                                if (tokens != null) {
                                    // JRADEV-6053 fails after and
                                    if (grammar.REGEXP_PREDICATE.test(tokens[0])) {
                                        token.getResult().setLastLogicalOperator(null);
                                        // JRADEV-7204 deleting back to predicate still leaves suggestions
                                        // in place, check this condition and reset
                                        token.getResult().resetNeedsPredicateOperand();
                                    }
                                    else {
                                        if (grammar.REGEXP_ANDS.test(remainingString)) {
                                            token.consumeCharacters(4);
                                            token.getResult().resetTerminalClause();
                                            token.getResult().setNeedsField();
                                        }
                                        if (token.isComplete() || tokens.length == 1) {
                                            token.setParseError();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },

            andClause: function(token) {
                this.chewWhitespace(token);
                this.notClause(token);
                if (!token.isComplete() && token.getResult().getLastLogicalOperator() === null) {
                    // Look for an AND clause
                    this.chewWhitespace(token);

                    var remainingString = token.remainingString();
                    if (remainingString !== null && (grammar.REGEXP_ANDS.test(remainingString) || this.startsWithIgnoreCase("&", remainingString) || this.startsWithIgnoreCase("&&", remainingString))) {
                        if (this.startsWithIgnoreCase("&&", remainingString)) {
                            token.getResult().setLastLogicalOperator("&&", token.getTokenStringIdx());
                            token.consumeCharacters(2);
                        }
                        else if (this.startsWithIgnoreCase("&", remainingString)) {
                            token.getResult().setLastLogicalOperator("&", token.getTokenStringIdx());
                            token.consumeCharacters(1);
                        }
                        else {
                            token.getResult().setLastLogicalOperator("AND", token.getTokenStringIdx());
                            token.consumeCharacters(4);
                        }
                        token.getResult().resetTerminalClause();
                        if (token.isComplete()) {
                            token.setParseError();
                        }

                        token.getResult().setNeedsField();
                    }
                    else {
                        token.getResult().setNeedsLogicalOperator(token);
                        // This is a special case for when we are about to place a logical operator into an existing string
                        // so that we will have a null place-holder token for the logical operator we expect.
                        if (token.isComplete()) {
                            token.getResult().setLastLogicalOperator(null, token.getMaxTokenStringIdx());
                        }
                    }
                }
            },

            notClause: function(token) {
                this.chewWhitespace(token);
                if (!token.isComplete()) {
                    // Look for a NOT clause
                    this.chewWhitespace(token);
                    var remainingString = token.remainingString();
                    if (remainingString !== null && (grammar.REGEXP_NOTS.test(remainingString) || this.startsWithIgnoreCase("!", remainingString))) {
                        // This is a bit of a hack that makes it so that you do not get a suggestion for a field until
                        // after you have typed a space after the NOT
                        token.getResult().needsField = false;
                        if (this.startsWithIgnoreCase("!", remainingString)) {
                            token.getResult().setLastLogicalOperator("!", token.getTokenStringIdx());
                            token.consumeCharacters(1);
                        }
                        else {
                            token.getResult().setLastLogicalOperator("NOT", token.getTokenStringIdx());
                            token.consumeCharacters(4);
                        }
                        token.getResult().resetTerminalClause();
                        if (token.isComplete()) {
                            token.setParseError();
                        }

                        token.getResult().setNeedsField();
                    }
                    else {
                        this.terminalClause(token);
                    }
                }
                else {
                    // Let the terminal clause set all the error conditions and needs
                    this.terminalClause(token);
                    token.setParseError();
                }
            },

            predicateClause: function(token) {
                this.chewWhitespace(token);
                if (!token.isComplete()) {
                    var remainingString = token.remainingString();
                    var tokens = remainingString.split(/[\s(]+/);
                    if (remainingString !== null) {
                        token.getResult().setMustBeOperatorOrPredicate(false);
                        // if the string starts with logical operator - return control to the
                        // not clause, however check if this is the last token, and if it is set the parse error flag
                        if (this.startsWithLogicalOperator(remainingString)) {
                            if (tokens.length == 1) {
                                token.getResult().setLastWasPredicate(tokens[0], token);
                                token.setParseError();
                                return;
                            }
                            return;
                        }
                        token.getResult().resetNeedsPredicateOperand();
                        token.consumeCharacters(tokens[0].length);
                        token.getResult().setLastWasPredicate(tokens[0], token);
                        // if there is only 1 token then there is no whitespace, so get out of Dodge
                        if (tokens.length == 1) {
                            token.setParseError();
                            return;
                        }
                        if (this.wasPredicateComplete(tokens[0])) {
                            token.getResult().setNeedsPredicateOperand();
                            token.getResult().setLastOperand(null, token);
                            this.predicateOperand(token);
                        }
                        else {
                            token.setParseError();
                        }
                    }
                }
                else {
                    // Let the terminal clause set all the error conditions and needs
                    token.setParseError();
                }

            },

            predicateOperand: function(token) {
                this.chewWhitespace(token);
                var predicateOperand = this.collectPredicateOperands(token);
                if (predicateOperand == null || predicateOperand == "") {
                    token.getResult().setLastOperandStartIndex(token.getTokenStringIdx());
                    token.setParseError();
                }
                // need a workaround to ensure you reset the needsPredicateOperand
                // easiest way is to check if you have entered a space after the
                // operand - meaning you want to go on parsing
                if (token.remainingString() != null) {
                    token.getResult().resetNeedsPredicateOperand();
                    token.getResult().resetLastPredicates();
                    token.getResult().setMustBeOperatorOrPredicate(true);
                    token.getResult().setNeedsLogicalOperator(token);
                    this.chewWhitespace(token);
                }

            },

            collectPredicateOperands: function(token) {
                // If we have and in or not in operator then we need either a function or an open paren
                if (token.getResult().getLastWasPredicate() === 'DURING') {
                    token.getResult().setNeedsOpenParen(true);
                }

                var predicateOperand = this.listOperand(token, true);
                if (predicateOperand.length === 0) {
                    predicateOperand = this.functionOperand(token);
                    if (predicateOperand.length === 0) {
                        predicateOperand = this.singleValueOperand(token);
                    }
                    else {
                        // We were handled by a function so we no longer need the open paren
                        token.getResult().setNeedsOpenParen(false);
                    }
                }
                else {
                    // We were handled by a list so we no longer need the open paren
                    token.getResult().setNeedsOpenParen(false);
                }
                return token.getResult().getUnquotedString(predicateOperand);
            },

            terminalClause: function(token) {
                // Lets always reset the last field variables
                token.getResult().resetTerminalClause();
                this.chewWhitespace(token);

                var remainingString = token.remainingString();
                // If we see a ( then we need to hand off to the or clause again
                if (this.startsWithIgnoreCase("(", remainingString)) {
                    token.getResult().addToken('(');
                    token.setInParens();
                    token.consumeCharacter();
                    this.orClause(token);
                    this.chewWhitespace(token);
                    remainingString = token.remainingString();
                    if (this.startsWithIgnoreCase(")", remainingString)) {
                        token.getResult().addToken(')');
                        token.consumeCharacter();
                        token.setOutOfParens();
                        if (token.isComplete()) {
                            // We don't want to suggest a logical operator to the user if the query parses
                            token.getResult().resetLogicalOperators();
                            token.getResult().resetLastPredicates();
                        }
                    }
                    else {
                        token.setParseError();
                    }
                }
                else {
                    this.field(token);
                    if (!token.isComplete()) {
                        this.oper(token);
                        if (!token.isComplete()) {
                            if (token.getResult().getLastWasOperator() === 'changed') {
                                token.getResult().setNeedsLogicalOperator(token);
                                return;
                            }
                            this.operand(token);

                            // This will only happen when we have seen a list operand that is properly closed
                            if (token.getResult().getOperandComplete()) {
                                // JRADEV-6372 Autocomplete does not parse after was in (or indeed was not in)
                                var lastWasOperator = token.getResult().getLastWasOperator();
                                token.getResult().setNeedsNothing();
                                if (lastWasOperator) {
                                    token.getResult().setLastWasOperator(lastWasOperator);
                                }
                            }
                            else if (token.isComplete() && !token.getResult().getNeedsListComma()) {
                                // This is not a parse error since we have found everything we were looking for
                                token.getResult().setNeedsOperand();
                            }
                        }
                        else {
                            //if (token.getResult().get that.ge token.getResult().setNeedsOperator();
                            if (token.getResult().getLastOperator() == "was" || token.getResult().getLastOperator() == "was not") {
                                token.getResult().setNeedsOperandOrOperator();
                            }
                            else if (token.getResult().getLastWasOperator() === 'changed') {
                                // JRADEV-7179 Need to make sure that there is a space before calling for an autocomplete
                                var currentChar = token.currentCharacter();
                                if (currentChar !== null && grammar.REGEXP_SPACE_OR_ELSE.test(currentChar)) {
                                    token.getResult().setNeedsLogicalOperator(token);
                                }
                                else {
                                    token.getResult().resetLogicalOperators();
                                    token.getResult().setNeedsOrderBy(false);
                                }
                                return;
                            }
                            else {
                                token.getResult().setNeedsOperator();
                            }
                            token.setParseError();
                        }
                    }
                    else {
                        token.getResult().setNeedsField();
                        token.setParseError();
                    }
                }
            },

            field: function(token) {
                var fieldName = this.fieldName(token);
                if (fieldName.length !== 0) {
                    token.getResult().setLastFieldName(fieldName, token);
                }
                else if (token.getResult().getLastFieldName() === null) {
                    var remainingString = token.remainingString();
                    token.getResult().setNeedsField();
                    token.getResult().setLastFieldName(remainingString, token);
                    token.setParseError();
                }
            },

            fieldName: function(token) {
                this.chewWhitespace(token);

                // Field is either a string or cf[12345]
                var remainingString = token.remainingString();
                if (this.startsWithIgnoreCase("cf", remainingString)) {
                    var origIdx = token.getTokenStringIdx();
                    var origString = token.remainingString();
                    token.consumeCharacters(2);
                    this.chewWhitespace(token);
                    remainingString = token.remainingString();
                    // Now we must find a [ or the show is off
                    if (this.startsWithIgnoreCase("[", remainingString)) {
                        token.consumeCharacter();
                        // We must find a number in here until we encounter a ]
                        remainingString = token.remainingString();
                        var custFieldId = this.numberValue(token);
                        if (custFieldId.length !== 0) {
                            // Lets find our closing ]
                            this.chewWhitespace(token);
                            remainingString = token.remainingString();
                            if (this.startsWithIgnoreCase("]", remainingString)) {
                                token.consumeCharacter();
                                return origString.substring(0, token.getTokenStringIdx() - origIdx);
                            }
                        }
                    }
                    token.setParseError();
                    return origString;
                }
                else if (this.startsWithIgnoreCase("issue.property", remainingString)) {
                    return this.entityPropertyName("issue.property", token);
                }
                else {
                    return this.fieldOrFunctionName(token);
                }
            },
            entityPropertyName: function(propertyPrefix, token) {
                token.consumeCharacters(propertyPrefix.length);
                this.chewWhitespace(token);
                var remainingString = token.remainingString();
                // Now we must find a [ or the show is off
                if (this.startsWithIgnoreCase("[", remainingString)) {
                    token.consumeCharacter();
                    // Now the string is propertyKey

                    var propertyKeys = [this.stringValue(token)];

                    while (!token.parseError && this.startsWithIgnoreCase('.', token.remainingString())) {
                        token.consumeCharacter();
                        propertyKeys[propertyKeys.length] = this.stringValue(token);
                    }
                    if (propertyKeys.length !== 0 && propertyKeys[0].length !== 0 && !this.startsWithIgnoreCase('.', propertyKeys[0]) /*we don't want to allow .key as keys*/) {
                        // Lets find our closing ]
                        this.chewWhitespace(token);
                        remainingString = token.remainingString();
                        if (this.startsWithIgnoreCase("]", remainingString)) {
                            token.consumeCharacter();
                            //now we expect property path the key after ]
                            var propertyPaths = [this.stringValue(token)];

                            while (!token.parseError && this.startsWithIgnoreCase('.', token.remainingString())) {
                                token.consumeCharacter();
                                propertyPaths[propertyPaths.length] = this.stringValue(token);
                            }
                            return propertyPrefix + '[' + propertyKeys.join('.') + ']' + propertyPaths.join('.');
                        }
                    }
                }
                token.setParseError();
                return "";
            },
            oper: function(token) {
                this.chewWhitespace(token);
                var remainingString = token.remainingString();
                var operator = this.getLongestOperatorMatch(remainingString, grammar.jql_operators);
                if (operator !== null) {
                    // We found an operator, record it and consume the right amount of characters
                    token.getResult().setLastOperator(operator, token.getTokenStringIdx());
                    if (OperatorUtil.isWasOperator(operator) || OperatorUtil.isChangedOperator(operator)) {
                        token.getResult().setLastWasOperator(operator);
                    }
                    token.consumeCharacters(operator.length);
                    // If we are one of the word operators we need to enforce a space here
                    if (operator == "in" || operator == "is" || operator == "is not" || operator == "not in" || operator == "was" || operator == "was not" || operator == "was in" || operator == "was not in") {
                        // We need a space or else
                        var currentChar = token.currentCharacter();
                        if (currentChar !== null && !grammar.REGEXP_SPACE_OR_ELSE.test(currentChar)) {
                            token.setParseError();
                        }
                    }
                    // was may be followed by not, so if it ihas a n or a no then the parser is still
                    // in error

                    if (operator == "was") {
                        token.getResult().setNeedsOperandOrOperator();
                        this.chewWhitespace(token);
                        remainingString = token.remainingString();

                        if (remainingString != null && (grammar.REGEXP_NOTSTART.test(remainingString) || grammar.REGEXP_INSTART.test(remainingString))) {
                            token.setParseError();
                        }
                    }
                    else if (operator == "was not") {
                        token.getResult().setNeedsOperandOrOperator();
                        this.chewWhitespace(token);
                        remainingString = token.remainingString();

                        if (remainingString != null && (grammar.REGEXP_INSTART.test(remainingString))) {
                            token.setParseError();
                        }
                    }
                    else if (operator == "changed") {
                        token.getResult().setNeedsLogicalOperator(token);
                    }
                    else {
                        token.getResult().setNeedsOperand();
                    }
                }
                else if (token.getResult().getLastOperator() === null) {
                    var errorIdx = (remainingString === null) ? token.getMaxTokenStringIdx() : token.getMaxTokenStringIdx() - remainingString.length;
                    token.getResult().setLastOperator(remainingString, errorIdx);
                    token.getResult().setNeedsOperator();
                    token.setParseError();
                }
            },

            operand: function(token) {
                this.chewWhitespace(token);

                // If we have and in or not in operator then we need either a function or an open paren
                if (token.getResult().getLastOperator() === 'in' || token.getResult().getLastOperator() === 'not in' || token.getResult().getLastOperator() === 'was not in' || token.getResult().getLastOperator() === 'was in') {
                    token.getResult().setNeedsOpenParen(true);
                }

                var operand = this.listOperand(token, true);
                if (operand.length === 0) {
                    operand = this.functionOperand(token);
                    if (operand.length === 0) {
                        operand = this.singleValueOperand(token);
                    }
                    else {
                        // We were handled by a function so we no longer need the open paren
                        token.getResult().setNeedsOpenParen(false);
                    }
                }
                else {
                    // We were handled by a list so we no longer need the open paren
                    token.getResult().setNeedsOpenParen(false);
                }

                if (operand === null || operand.length === 0) {
                    var remainingString = token.remainingString();
                    token.getResult().setLastOperand(remainingString, token);
                    token.getResult().setNeedsOperand();
                    token.setParseError();
                }
                if (operand.length !== 0) {
                    return operand;
                }
                return "";
            },

            singleValueOperand: function(token) {
                var operand = this.stringValue(token);
                if (operand.length !== 0) {
                    token.getResult().setLastOperand(operand, token);
                    // Lets exclude empty and null, even though it is reserved
                    if (operand.toLowerCase() != "empty" && operand.toLowerCase() != "null" && this.isReservedWord(operand.toLowerCase())) {
                        // These are reserved words
                        token.setParseError();
                    }
                    return operand;
                }
                return "";
            },

            functionOperand: function(token) {
                var startIdx = token.getTokenStringIdx();
                var functionName = this.fieldOrFunctionName(token);
                // There can be whitespace between function name and arguments
                this.chewWhitespace(token);
                var listArguments = this.listOperand(token, false);
                if (functionName.length !== 0 && listArguments.length !== 0) {
                    var operand = functionName + listArguments;
                    // read in the whole value until we reach a close )
                    token.getResult().setLastOperand(operand, token);
                    return operand;
                }
                else {
                    // back track
                    token.backTrackToIdx(startIdx);
                    return "";
                }
            },

            listOperand: function(token, treatAsOperands) {
                if (token.currentCharacter() == '(') {
                    token.consumeCharacter();
                    var listValue = this.collectListValues(token, treatAsOperands);
                    var operandVal = "(" + listValue;

                    this.chewWhitespace(token);
                    if (token.currentCharacter() == ')') {
                        token.consumeCharacter();
                        operandVal += ")";
                        if (operandVal == "()" && treatAsOperands) {
                            // Special case of an empty list which is still valid
                            token.getResult().setLastOperand(operandVal, token);
                        }
                        if (treatAsOperands) {
                            token.getResult().setOperandComplete();
                            token.getResult().resetNeedsPredicateOperand();
                        }
                    }
                    else {
                        token.setParseError();
                    }
                    return operandVal;
                }
                else {
                    return "";
                }
            },

            collectListValues: function(token, treatAsOperands) {
                if (treatAsOperands) {
                    token.getResult().setNeedsOperand();
                }
                this.chewWhitespace(token);

                // grab the contents of the list, they should be singleValueOperands separated by commas, we only
                // need to keep track of the last encountered operand.
                var currentOperand = (treatAsOperands) ? this.operand(token) : this.stringValue(token);

                if (currentOperand.length !== 0) {
                    // If there is whitespace then lets remember we need a comma
                    if (this.chewWhitespace(token) && treatAsOperands) {
                        token.getResult().setNeedsListComma();
                    }
                    if (token.currentCharacter() == ',') {
                        // Consume the comma and recurse so we can collect the other values
                        token.consumeCharacter();
                        var nextValue = this.collectListValues(token, treatAsOperands);
                        if (nextValue.length === 0) {
                            token.setParseError();
                        }
                        return currentOperand + ", " + nextValue;
                    }
                    else {
                        return currentOperand;
                    }
                }
                else {
                    return "";
                }
            },

            startsWithIgnoreCase: function(startStr, str) {
                if (str === null || startStr === null || str.length < startStr.length) {
                    return false;
                }
                else {
                    return startStr.toLowerCase() == str.substr(0, startStr.length).toLowerCase();
                }
            },

            startsWithLogicalOperator: function(str) {
                if (str === null) {
                    return false;
                }
                else {
                    return this.startsWithOr(str) || this.startsWithAnd(str) || this.startsWithNot(str) || this.startsWithBraces(str);
                }
            },

            startsWithOr: function(str) {
                return this.startsWithIgnoreCase("|", str) || this.startsWithIgnoreCase("||", str) ||
                    this.startsWithIgnoreCase("or", str);
            },

            startsWithAnd: function(str) {
                return this.startsWithIgnoreCase("&", str) || this.startsWithIgnoreCase("&&", str) ||
                    this.startsWithIgnoreCase("and", str);
            },

            startsWithNot: function(str) {
                return this.startsWithIgnoreCase("!", str) ||
                    this.startsWithIgnoreCase("not", str);
            },

            startsWithBraces: function(str) {
                return this.startsWithIgnoreCase("(", str) ||
                    this.startsWithIgnoreCase(")", str);
            },

            chewWhitespace: function(token) {
                var foundWhiteSpace = false;
                var currentChar = token.currentCharacter();
                while (currentChar !== null && grammar.REGEXP_WHITESPACE.test(currentChar)) {
                    token.consumeCharacter();
                    currentChar = token.currentCharacter();
                    foundWhiteSpace = true;
                }
                return foundWhiteSpace;
            },

            getLongestOperatorMatch: function(value, listOfValues) {
                var longestMatch = null;
                var matchArray;
                // These first three cases are special since they might have more to them
                if (this.startsWithIgnoreCase("is", value)) {
                    // Look ahead for NOT, if we don't find it next then we must just be is
                    matchArray = value.substring(2).match(grammar.REGEXP_SNOT);

                    if (matchArray) {
                        longestMatch = value.substring(0, matchArray[0].length + 2);
                    }
                    else {
                        longestMatch = "is";
                    }
                }
                if (this.startsWithIgnoreCase("was", value)) {
                    // Look ahead for NOT or IN , if we don't find it next then we must just be was
                    matchArray = value.substring(3).match(grammar.REGEXP_SNOT_IN);

                    if (matchArray) {
                        longestMatch = value.substring(0, matchArray[0].length + 3);
                    }
                    else {
                        longestMatch = "was";
                    }
                }
                else if (this.startsWithIgnoreCase("not", value)) {
                    // Look ahead for IN, if we don't find it next then we are not an operator
                    var matchArrayNot = value.substring(3).match(grammar.REGEXP_SIN);

                    if (matchArrayNot) {
                        longestMatch = value.substring(0, matchArrayNot[0].length + 3);
                    }
                }
                else {
                    for (var i = 0; i < listOfValues.length; i++) {
                        if (this.startsWithIgnoreCase(listOfValues[i].value, value)) {
                            // We found a match
                            if (longestMatch === null || grammar.jql_operators[i].value.length > longestMatch) {
                                longestMatch = grammar.jql_operators[i].value;
                            }
                        }
                    }
                }
                return longestMatch;
            },

            getValueMinusExtraWhitespace: function(value) {
                if (value === null) {
                    return null;
                }
                var newValue = "";
                var firstWhitespace = true;
                var valueArr = value.split("");
                for (var i = 0; i < valueArr.length; i++) {
                    var currentChar = valueArr[i];
                    if (grammar.REGEXP_WHITESPACE.test(currentChar)) {
                        // We want to ignore extra whitespace, keeping only the first
                        if (firstWhitespace) {
                            firstWhitespace = false;
                            newValue = newValue + currentChar;
                        }
                    }
                    else {
                        // If we encounter a non-whitespace then we want to reset our firstWhitespace test
                        firstWhitespace = true;
                        newValue = newValue + currentChar;
                    }
                }
                return newValue;
            },

            fieldOrFunctionName: function(token) {
                var stringValue = this.stringValue(token);
                // Field or function names can not be the empty string
                if (stringValue === "\"\"" || stringValue === "''") {
                    token.setParseError();
                }
                if (this.isReservedWord(stringValue.toLowerCase())) {
                    // These are reserved words
                    token.setParseError();
                }
                return stringValue;
            },

            isReservedWord: function(word) {
                return jQuery.inArray(word, jql_reserved_words) !== -1;
            },

            stringValue: function(token) {
                var stringValue = "";

                var inQuote = false;
                var inSingleQuote = false;
                var currentChar = token.currentCharacter();
                while (currentChar !== null && (inQuote || inSingleQuote || grammar.REGEXP_TOKEN_CHAR.test(currentChar))) {
                    // Read the escape character into the string
                    stringValue = stringValue + currentChar;
                    token.consumeCharacter();
                    // Handle the escape character
                    if (currentChar == '\\') {
                        // Just consume the next char as well
                        currentChar = token.currentCharacter();
                        if (currentChar === null) {
                            token.setParseError();
                            break;
                        }
                        // These are the only valid characters to escape
                        else if (grammar.REGEXP_CHARS_TO_ESCAPE.test(currentChar)) {
                            // Check for unicode escapes
                            var remainingString = token.remainingString();
                            if (!grammar.REGEXP_UNICODE.test(remainingString)) {
                                token.setParseError();
                                break;
                            }
                        }

                        stringValue = stringValue + currentChar;
                        token.consumeCharacter();
                    }
                    // Check for illegal characters and kill the whole parse
                    else if (grammar.REGEXP_SPECIAL_CHAR.test(currentChar) && !(inQuote || inSingleQuote)) {
                        token.setParseError();
                        break;
                    }
                    // We need to keep track if we are in a quote or not
                    else if (currentChar == '"' && !inSingleQuote) {
                        inQuote = !inQuote;
                    }
                    else if (currentChar == "'" && !inQuote) {
                        inSingleQuote = !inSingleQuote;
                    }
                    currentChar = token.currentCharacter();
                }
                // We should never get left in a quote or single quote
                if (token.isComplete() && (inQuote || inSingleQuote)) {
                    token.setParseError();
                }
                return stringValue;
            },

            numberValue: function(token) {
                var numberVal = "";

                this.chewWhitespace(token);
                var currentChar = token.currentCharacter();
                while (currentChar !== null) {
                    if (grammar.REGEXP_NUMBER.test(currentChar)) {
                        numberVal = numberVal + currentChar;
                        token.consumeCharacter();
                    }
                    else {
                        // found not a number time to return
                        break;
                    }
                    currentChar = token.currentCharacter();
                }
                return numberVal;
            },

            wasPredicateComplete: function(predicate) {
                var foundPredicate = false;
                var arLen = grammar.jql_changed_predicates.length;
                for (var i = 0; i < arLen; ++i) {
                    if (predicate.toLowerCase() == grammar.jql_changed_predicates[i].value.toLowerCase()) {
                        foundPredicate = true;
                        break;
                    }

                }
                return foundPredicate;
            }

        };
    };
});

define('jira/jql/jql-parse-result', function() {
    "use strict";

    /**
     * @returns {JQLAutoComplete.ParseResult}
     * @constructor
     */
    return function() {

        var tokens = [];
        var tokenIdx = 0;

        /**
         * @class JQLAutoComplete.ParseResult
         */
        return {

            getTokens: function() {
                return tokens;
            },

            addToken: function(token) {
                tokens[tokenIdx++] = token;
            },

            setLastFieldName: function(lastFieldName, token) {
                this.fieldNameStartIndex = (lastFieldName === null) ? token.getMaxTokenStringIdx() : (token.getTokenStringIdx() - lastFieldName.length);
                // Get rid of quotes if we need to
                this.lastFieldName = this.getUnquotedString(lastFieldName);
                tokens[tokenIdx++] = lastFieldName;
            },

            getLastFieldName: function() {
                return this.lastFieldName;
            },

            getLastFieldNameStartIndex: function() {
                return this.fieldNameStartIndex;
            },

            setLastOrderByFieldName: function(lastFieldName, token) {
                this.orderByFieldNameStartIndex = (lastFieldName === null) ? token.getMaxTokenStringIdx() : (token.getTokenStringIdx() - lastFieldName.length);
                // Get rid of quotes if we need to
                this.lastOrderByFieldName = this.getUnquotedString(lastFieldName);
                this.lastOrderByDirection = null;
                tokens[tokenIdx++] = lastFieldName;
            },

            getLastOrderByFieldName: function() {
                return this.lastOrderByFieldName;
            },

            getLastOrderByFieldNameStartIndex: function() {
                return this.orderByFieldNameStartIndex;
            },

            setLastOrderByDirection: function(lastDirection, token) {
                this.orderByDirectionStartIndex = (lastDirection === null) ? token.getMaxTokenStringIdx() : (token.getTokenStringIdx() - lastDirection.length);
                this.lastOrderByDirection = lastDirection;
                tokens[tokenIdx++] = lastDirection;
            },

            getLastOrderByDirection: function() {
                return this.lastOrderByDirection;
            },

            getLastOrderByDirectionStartIndex: function() {
                return this.orderByDirectionStartIndex;
            },

            setNeedsField: function() {
                this.needsOperator = false;
                this.needsOperand = false;
                this.needsLogicalOperator = false;
                this.needsOrderBy = false;
                this.needsField = true;
                this.needsOrderByField = false;
                this.needsOrderByDirection = false;
                this.needsListComma = false;
                this.needsWasPredicate = false;
                this.lastWasOperator = null;
            },

            getNeedsField: function() {
                return this.needsField;
            },

            setNeedsOrderByField: function() {
                this.needsOperator = false;
                this.needsOperand = false;
                this.needsLogicalOperator = false;
                this.needsOrderBy = false;
                this.needsField = false;
                this.needsOrderByField = true;
                this.needsOrderByDirection = false;
                this.lastOrderByDirection = null;
                this.needsListComma = false;
            },

            getNeedsOrderByField: function() {
                return this.needsOrderByField;
            },

            setNeedsOrderByDirection: function() {
                this.needsOperator = false;
                this.needsOperand = false;
                this.needsLogicalOperator = false;
                this.needsOrderBy = false;
                this.needsField = false;
                this.needsOrderByField = false;
                this.needsOrderByDirection = true;
                this.needsListComma = false;
            },

            getNeedsOrderByDirection: function() {
                return this.needsOrderByDirection;
            },

            setNeedsOrderByComma: function() {
                this.needsOperator = false;
                this.needsOperand = false;
                this.needsLogicalOperator = false;
                this.needsOrderBy = false;
                this.needsField = false;
                this.needsOrderByField = false;
                this.needsOrderByDirection = false;
                this.needsListComma = false;
            },

            setNeedsListComma: function() {
                this.needsOperator = false;
                this.needsOperand = false;
                this.needsLogicalOperator = false;
                this.needsOrderBy = false;
                this.needsField = false;
                this.needsOrderByField = false;
                this.needsOrderByDirection = false;
                this.needsListComma = true;
            },

            getNeedsListComma: function() {
                return this.needsListComma;
            },

            setLastOperator: function(lastOperator, startIndex) {
                this.lastOperator = lastOperator;
                this.operatorStartIndex = startIndex;
                tokens[tokenIdx++] = lastOperator;
            },

            setLastWasOperator: function(lastWasOperator) {
                this.lastWasOperator = lastWasOperator;
            },

            getLastWasOperator: function() {
                return this.lastWasOperator;
            },

            getLastOperator: function() {
                return this.lastOperator;
            },

            getLastOperatorStartIndex: function() {
                return this.operatorStartIndex;
            },

            setLastOperand: function(lastOperand, token) {
                this.operandStartIndex = (lastOperand === null) ? token.getMaxTokenStringIdx() : (token.getTokenStringIdx() - lastOperand.length);
                // Get rid of quotes if we need to
                this.lastOperand = this.getUnquotedString(lastOperand);
                tokens[tokenIdx++] = lastOperand;
            },

            getLastOperand: function() {
                return this.lastOperand;
            },

            setNeedsOperand: function() {
                this.needsField = false;
                this.needsOperator = false;
                this.needsLogicalOperator = false;
                this.needsOperand = true;
                this.needsOrderBy = false;
                this.needsOrderByField = false;
                this.needsOrderByDirection = false;
                this.needsListComma = false;
            },

            setNeedsOperandOrOperator: function() {
                this.needsField = false;
                this.needsOperator = true;
                this.needsLogicalOperator = false;
                this.needsOperand = true;
                this.needsOrderBy = false;
                this.needsOrderByField = false;
                this.needsOrderByDirection = false;
                this.needsListComma = false;
            },

            getNeedsOperand: function() {
                return this.needsOperand;
            },

            setNeedsPredicateOperand: function() {
                this.needsPredicateOperand = true;
            },

            resetNeedsPredicateOperand: function() {
                this.needsPredicateOperand = false;
                this.needsOperand = false;
            },

            getNeedsPredicateOperand: function() {
                return this.needsPredicateOperand;
            },

            getLastOperandStartIndex: function() {
                return this.operandStartIndex;
            },

            setLastOperandStartIndex: function(index) {
                this.operandStartIndex = index;
            },

            setLastLogicalOperator: function(lastLogicalOperator, startIndex) {
                this.lastLogicalOperator = lastLogicalOperator;
                this.logicalOperatorStartIndex = startIndex;
                tokens[tokenIdx++] = lastLogicalOperator;
            },

            setNeedsOperator: function() {
                this.needsField = false;
                this.needsOperand = false;
                this.needsLogicalOperator = false;
                this.needsOrderBy = false;
                this.needsOperator = true;
                this.needsOrderByField = false;
                this.needsOrderByDirection = false;
                this.needsListComma = false;
            },

            getNeedsOperatorOrOperand: function() {
                return this.needsOperator && this.needsOperand;
            },

            getNeedsOperator: function() {
                return this.needsOperator;
            },

            getLastLogicalOperator: function() {
                return this.lastLogicalOperator;
            },

            getLastLogicalOperatorStartIndex: function() {
                return this.logicalOperatorStartIndex;
            },

            setNeedsLogicalOperator: function(token) {
                this.needsLogicalOperator = true;
                this.needsOperator = false;
                this.needsOperand = false;
                this.needsField = false;
                this.needsOrderByField = false;
                this.needsOrderByDirection = false;
                this.needsListComma = false;
                // Every time we need a logical operator we also could need an order by as long as we are not in parens
                this.needsOrderBy = !token.getInParens();
                // Every time a logical operator is needed you may need a predicate, but only if the last clause
                // was a was clause
                if (this.lastWasOperator != null) {
                    this.needsWasPredicate = true;
                }
            },

            setNeedsOpenParen: function(value) {
                this.needsOpenParen = value;
            },

            getNeedsOpenParen: function() {
                return this.needsOpenParen;
            },

            getNeedsLogicalOperator: function() {
                return this.needsLogicalOperator;
            },

            setNeedsOrderBy: function(value) {
                this.needsOrderBy = value;
            },

            getNeedsOrderBy: function() {
                return this.needsOrderBy;
            },

            getNeedsWasPredicate: function() {
                return this.needsWasPredicate;
            },

            setLastWasPredicate: function(lastWasPredicate, token) {
                this.wasPredicateStartIndex = (lastWasPredicate === null) ? token.getMaxTokenStringIdx() : (token.getTokenStringIdx() - lastWasPredicate.length);
                this.lastWasPredicate = lastWasPredicate;
                tokens[tokenIdx++] = lastWasPredicate;
            },

            getLastWasPredicate: function() {
                return this.lastWasPredicate;
            },

            resetLastPredicates: function() {
                this.lastLogicalOperator = null;
                this.lastWasPredicate = null;
                this.lastOperand = null;
                this.wasPredicateStartIndex = null;
                this.operandStartIndex = null;
            },

            getLastWasPredicateStartIndex: function() {
                return this.wasPredicateStartIndex;
            },

            setLastOrderBy: function(lastOrderBy, token) {
                this.orderByStartIndex = (lastOrderBy === null) ? token.getMaxTokenStringIdx() : (token.getTokenStringIdx() - lastOrderBy.length);
                this.lastOrderBy = lastOrderBy;
                tokens[tokenIdx++] = lastOrderBy;
            },

            getLastOrderBy: function() {
                return this.lastOrderBy;
            },

            getLastOrderByStartIndex: function() {
                return this.orderByStartIndex;
            },

            resetLogicalOperators: function() {
                this.lastLogicalOperator = null;
                this.logicalOperatorStartIndex = null;
                this.needsLogicalOperator = null;
            },

            getUnquotedString: function(value) {
                // We only remove the last quote if it is NOT preceeded by an escape character
                var secondToLastNotEsacape = value != null && value.length >= 3 && value.charAt(value.length - 2) != '\\';

                if (value != null && value.charAt(0) == '"') {
                    value = value.substring(1, value.length);

                    if (value.charAt(value.length - 1) == '"' && secondToLastNotEsacape) {
                        value = value.substring(0, value.length - 1);
                    }
                }
                else if (value != null && value.charAt(0) == "'") {
                    value = value.substring(1, value.length);

                    if (value.charAt(value.length - 1) == "'" && secondToLastNotEsacape) {
                        value = value.substring(0, value.length - 1);
                    }
                }

                return value;
            },

            setParseError: function(message) {
                this.parseError = true;
                this.parseErrorMsg = message;
            },

            getParseError: function() {
                return this.parseError;
            },

            getParseErrorMsg: function() {
                return this.parseErrorMsg;
            },

            setNeedsNothing: function() {
                this.needsOperator = false;
                this.needsOperand = false;
                this.needsLogicalOperator = false;
                this.needsOrderBy = false;
                this.needsField = false;
                this.needsOrderByField = false;
                this.needsOrderByDirection = false;
                this.needsOpenParen = false;
                this.needsListComma = false;
                this.needsWasPredicate = null;
                this.needsPredicateOperand = null;
                this.mustBeOperatorOrPredicate = null;
                this.lastWasOperator = null;
            },

            setOperandComplete: function() {
                this.operandComplete = true;
            },

            getOperandComplete: function() {
                return this.operandComplete;
            },

            setMustBeOperatorOrPredicate: function(state) {
                this.mustBeOperatorOrPredicate = state;
            },

            getMustBeOperatorOrPredicate: function() {
                return this.mustBeOperatorOrPredicate;
            },

            resetTerminalClause: function() {
                this.lastFieldName = null;
                this.fieldNameStartIndex = null;
                this.needsField = null;
                this.lastOperator = null;
                this.operatorStartIndex = null;
                this.needsOperator = null;
                this.lastOperand = null;
                this.operandStartIndex = null;
                this.needsOperand = null;
                this.operandComplete = null;
                this.needsOpenParen = null;
                this.needsListComma = false;
                this.mustBeOperatorOrPredicate = null;
            },

            init: function() {
                this.lastFieldName = null;
                this.fieldNameStartIndex = null;
                this.needsField = null;
                this.lastOperator = null;
                this.operatorStartIndex = null;
                this.needsOperator = null;
                this.lastOperand = null;
                this.operandStartIndex = null;
                this.needsOperand = null;
                this.lastLogicalOperator = null;
                this.logicalOperatorStartIndex = null;
                this.lastOrderByFieldName = null;
                this.lastOrderByFieldNameStartIndex = null;
                this.lastOrderByDirection = null;
                this.lastOrderByDirectionStartIndex = null;
                this.orderByStartIndex = null;
                this.lastOrderBy = null;
                this.needsOrderBy = null;
                this.needsOrderByField = null;
                this.needsOrderByDirection = null;
                this.operandComplete = null;
                this.needsOpenParen = null;
                this.needsListComma = null;
                this.wasPredicateStartIndex = null;
                this.lastWasPredicate = null;
                this.needsWasPredicate = null;
                this.needsPredicateOperand = null;
                this.mustBeOperatorOrPredicate = null;
                this.lastWasOperator = null;
            }
        };
    };
});

define('jira/jql/jql-parse-token', [
    'jira/jql/jql-parse-result'
], function(
    ParseResult
) {
    "use strict";

    /**
     * @returns {JQLAutoComplete.Token}
     * @constructor
     */
    return function() {
        /**
         * @class JQLAutoComplete.Token
         */
        return {

            init: function(tokenString) {
                this.tokenStringIdx = 0;
                this.tokenString = tokenString;
                this.parseError = false;
                this.parseErrorMsg = null;
                this.result = ParseResult();
                this.result.init();
                this.inParens = 0;
            },

            consumeCharacter: function() {
                this.tokenStringIdx++;
            },

            consumeCharacters: function(numChars) {
                this.tokenStringIdx = this.tokenStringIdx + numChars;
            },

            backTrackToIdx: function(backTrackIdx) {
                this.tokenStringIdx = backTrackIdx;
                // Lets clear any parse errors that might have occurred as well
                this.parseError = false;
                this.parseErrorMsg = null;
                this.result.parseError = false;
                this.result.parseErrorMsg = null;
            },

            getTokenStringIdx: function() {
                return this.tokenStringIdx;
            },

            currentCharacter: function() {
                if (this.tokenStringIdx >= this.tokenString.length) {
                    return null;
                }
                return this.tokenString.charAt(this.tokenStringIdx);
            },

            remainingString: function() {
                if (this.tokenStringIdx >= this.tokenString.length) {
                    return null;
                }
                return this.tokenString.substr(this.tokenStringIdx, this.tokenString.length);
            },

            getMaxTokenStringIdx: function() {
                return this.tokenString.length;
            },

            isComplete: function() {
                if (this.parseError) {
                    return true;
                }
                return this.tokenStringIdx >= this.tokenString.length;
            },

            setInParens: function() {
                this.inParens++;
            },

            setOutOfParens: function() {
                // Lets never go into negative here
                if (this.inParens !== 0) {
                    this.inParens--;
                }
            },

            getInParens: function() {
                return this.inParens !== 0;
            },

            setParseError: function() {
                this.parseError = true;
                var preFixIdx = ((this.tokenStringIdx - 9) < 0) ? 0 : this.tokenStringIdx - 9;
                var errorPrefix = this.tokenString.substring(preFixIdx, this.tokenStringIdx - 1);
                this.result.setParseError("..." + errorPrefix + "^" + this.tokenString.substring(this.tokenStringIdx, this.tokenString.length));
            },

            getParseError: function() {
                return this.parseError;
            },

            calcLastOperandStartIndex: function() {
                var result = this.getResult();
                var operator = result.getLastOperator();
                if (operator.length > 0) {
                    var numSpaces = operator.length - (operator.replace(/\s+/g, '')).length;
                    result.setLastOperandStartIndex(result.getLastOperatorStartIndex() + operator.length + numSpaces + 1);
                }
            },

            getParsedOperator: function() {
                var result = this.getResult();
                return (result.getLastOperator().length > 0) ? this.tokenString.substr(result.getLastOperatorStartIndex()) : null;
            },

            getResult: function() {
                return this.result;
            }

        };

    };
});

/** Preserve legacy namespace
 @deprecated jira.widget.autocomplete.JQL */
AJS.namespace("jira.widget.autocomplete.JQL", null, require('jira/autocomplete/jql-autocomplete'));
AJS.namespace("JIRA.JQLAutoComplete", null, require('jira/autocomplete/jql-autocomplete'));
AJS.namespace("JIRA.JQLAutoComplete.MyParser", null, require('jira/jql/jql-parser'));
AJS.namespace("JIRA.JQLAutoComplete.ParseResult", null, require('jira/jql/jql-parse-result'));
AJS.namespace("JIRA.JQLAutoComplete.Token", null, require('jira/jql/jql-parse-token'));
