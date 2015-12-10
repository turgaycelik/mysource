/**
 * Used on text type input fields will select a range of characters in the field
 * If the values are equal will put the typeing caret before that number of characters
 *
 * usage jQuery('#textfield').setSelectionRange(3,5)
 *
 * @param selectionStart - the starting character to select
 * @param selectionEnd - the final character to select
 */
jQuery.fn.setSelectionRange =  function( selectionStart, selectionEnd) {
    if (this.length == 0)
        return;
    if (this[0].setSelectionRange) {
        this[0].focus();
        this[0].setSelectionRange(selectionStart, selectionEnd);
    }
    else if (this[0].createTextRange) {
        var range = this[0].createTextRange();
        range.collapse(true);
        range.moveEnd('character', selectionEnd);
        range.moveStart('character', selectionStart);
        range.select();
    }
},

/**
 * Used on text type input fields will place the typing caret x number of characters into the textfield
 *
 * usage jQuery('#textfield').setCaretToPosition(10)
 *
 * @param position - the number of characters into the text field to try to move the caret
 */
jQuery.fn.setCaretToPosition = function(position) {
    this.setSelectionRange(position, position);
};
