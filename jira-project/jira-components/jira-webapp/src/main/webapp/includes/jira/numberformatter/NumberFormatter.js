JIRA.NumberFormatter = {
    // Only supports formatting integers to match the user locale.
    // Can be extended to support more complex formatting, and decimals, if necessary.
    format: function(integer) {
        var groupSeparator = AJS.Meta.get('user-locale-group-separator') || '';
        // Replace every character boundary that is not a word boundary, and is followed by a multiple of 3 of digits,
        // with the groupSeparator
        return integer.toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, groupSeparator);
    }
};
