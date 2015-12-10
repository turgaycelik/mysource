AJS.test.require("jira.webresources:jira-global");

(function() {

    var $ = jQuery,
        DIRTY_WARNING_EXEMPT = "ajs-dirty-warning-exempt";
        DIRTY_WARNING_BY_DEFAULT = "ajs-dirty-warning-by-default";

    module("Dirty Forms", {
        teardown: function() {
            $("form").remove();
        }
    });

    function getEmptyForm() {
        return $("<form action='javascript:void(0)'></form>").appendTo("body");
    }

    function getFormWithHtmlContent() {
        var $form = $("<form/>").appendTo("body");
        for(var i = 0, ii = arguments.length; i < ii; i++) {
            $form.append(arguments[i]);
        }
        return $form;
    }

    test("An empty form cannot be dirty", function() {
        var form = getEmptyForm(),
            result = form.isDirty();

        equal(result, false, "Empty form cannot be dirty");
    });

    test("A form with class 'ajs-dirty-warning-exempt' cannot be dirty, even if it has dirty fields", function() {
        var form = getEmptyForm();
        form.addClass(DIRTY_WARNING_EXEMPT);
        form.find("input").val("another value");
        var result = form.isDirty();

        equal(result, false, "Form with class 'ajs-dirty-warning-exempt' cannot be dirty");
    });

    test("A form can be rendered as dirty by default", function() {
        var form = getFormWithHtmlContent("<input type='text' name='valid' value=''>");
        form.addClass(DIRTY_WARNING_BY_DEFAULT);

        equal(form.isDirty(), true, "Form should be dirty if specific class was added");
    });

    test("The form of an input with class 'ajs-dirty-warning-exempt' cannot be dirty", function() {
        var form = getFormWithHtmlContent("<input type='text' name='input1' value='someValue'>"),
            input = form.find("input").addClass(DIRTY_WARNING_EXEMPT);
        input.val("another value");
        var result = input.isDirty();

        equal(result, false, "Form with an element with class 'ajs-dirty-warning-exempt' cannot be dirty");
    });

    test("The form of an input with class 'ajs-dirty-warning-exempt' is dirty if it contains a dirty field without the class 'ajs-dirty-warning-exempt'", function() {
        var form = getFormWithHtmlContent("<input type='text' name='cleanInput' value='someValue'><input type='text' name='dirtyInput' value='anotherValue'/>");

        form.find("input[name=cleanInput]").addClass(DIRTY_WARNING_EXEMPT);
        form.find("input[name=dirtyInput]").val("changed value");

        var result = form.isDirty();

        equal(result, true, "A form containing a clean field with excempt class and a dirty field without exempt class is dirty.");
    });

    test("A clean field is clean even if its form contains a dirty field", function() {
        var form = getFormWithHtmlContent("<input type='text' name='cleanInput' value='someValue'><input type='text' name='dirtyInput' value='anotherValue'/>"),
            cleanInput = form.find("input[name=cleanInput]")

        form.find("input[name=dirtyInput]").val("changed value");

        var result = cleanInput.isDirty();

        equal(result, false, "A clean field is clean even if its form contains a dirty field");
    });

    test("A form containing a dirty, but invisible, field is not dirty", function() {
        var dirtyInvisibleField = $("<input style='display:none' type='text' name='dirtyHiddenField' value='someValue'>");
        var form = getFormWithHtmlContent(dirtyInvisibleField);

        dirtyInvisibleField.val("changed value");

        var result = form.isDirty();

        equal(result, false, "A form containing a dirty, but invisible, field is not dirty");
    });

    test("A form containing a dirty, invisible, but sanctioned element is dirty", function() {
        var dirtyInvisibleField = $("<input style='display:none' type='text' name='dirtyHiddenField' value='someValue'>");
        var form = getFormWithHtmlContent(dirtyInvisibleField);

        dirtyInvisibleField.addClass(JIRA.DirtyForm.ClassNames.SANCTIONED);
        dirtyInvisibleField.val("changed value");

        var result = form.isDirty();

        equal(result, true, "A form containing a dirty, invisible, but sanctioned field is dirty");
    });

    /**
     * Sanity-check -- the DOM doesn't honor defaultValue on input type=hidden values.
     * @see http://www.w3.org/TR/DOM-Level-2-HTML/html.html#ID-6043025
     */
    test("Hidden form elements never have default values, so are always clean", function() {
        var oldValue = "someValue";
        var newValue = "changed value";
        var hiddenInputField = $("<input type='hidden' name='hiddenInputField' value='"+oldValue+"'>");
        var sanctionedHiddenInputField = hiddenInputField.clone(true);
        var form = getFormWithHtmlContent(hiddenInputField, sanctionedHiddenInputField);

        equal(hiddenInputField[0].defaultValue, oldValue, "The default value is currently set to the old value");

        hiddenInputField.val(newValue);
        sanctionedHiddenInputField.addClass(JIRA.DirtyForm.ClassNames.SANCTIONED);
        sanctionedHiddenInputField.val(newValue);

        var result = form.isDirty();

        equal(result, false, "The form shouldn't be dirty");
        equal(hiddenInputField[0].value, newValue, "The value of the hidden field was set correctly");
        notEqual(hiddenInputField[0].defaultValue, oldValue, "The default value should not remain the same, since hidden fields have no defaultValue");
        equal(hiddenInputField[0].defaultValue, newValue, "The default value was updated as per the DOM specification");
        equal(sanctionedHiddenInputField[0].defaultValue, newValue, "Even sanctioning a hidden field won't help -- this is DOM behaviour, after all.");
    });

    test("A form containing a dirty button is not dirty", function() {
        var form = getFormWithHtmlContent("<input type='button' name='dirtyHiddenField' value='someValue'>"),
            dirtyHiddenField = form.find("input[name=dirtyHiddenField]")

        dirtyHiddenField.val("changed value");

        var result = form.isDirty();

        equal(result, false, "A form containing a dirty, button is not dirty");
    });

    test("A form containing a dirty select is dirty", function() {
        var form = getFormWithHtmlContent("<select name='dirtySelect'><option>Option 1</option><option selected='selected'>Option 2</option><option>Option 3</option></select>"),
            dirtySelect = form.find("select")

        dirtySelect.val('Option 3');

        var result = form.isDirty();

        equal(result, true, "A form containing a dirty select is dirty");
    });

    test("A form containing a dirty multi-select is dirty", function() {
        var form = getFormWithHtmlContent("<select multiple='multiple' name='dirtySelect'><option selected='selected'>Option 1</option><option selected='selected'>Option 2</option><option>Option 3</option></select>"),
            dirtySelect = form.find("select")

        dirtySelect.val('Option 3');

        var result = form.isDirty();

        equal(result, true, "A form containing a dirty multi-select is dirty");
    });

    test("A form containing a dirty radio button is dirty", function() {
        var form = getFormWithHtmlContent("<input type='radio' name='radiogroup' value='initialValue2' checked='checked'><input type='radio' name='radiogroup' value='initialValue2'>"),
            dirtyRadio = form.find("input").last();

        dirtyRadio.prop('checked', true);

        var result = form.isDirty();

        equal(result, true, "A form containing a dirty radio button is dirty");
    });

    test("A form containing a dirty checkbox is dirty", function() {
        var form = getFormWithHtmlContent("<input type='checkbox' name='checkgroup' value='initialValue'>"),
            dirtyCheckbox = form.find("input");

        dirtyCheckbox.prop('checked', true);

        var result = form.isDirty();

        equal(result, true, "A form containing a dirty checkbox is dirty");
    });

    test("A form with unchanged inputs is clean", function() {
        var form = getFormWithHtmlContent("<input type='text' name='input1' value='someValue'><input type='text' name='input2' value='someValue'>"),
            result = form.isDirty();

        equal(result, false, "A form with clean inputs is clean");
    });

    test("A form containing an unchanged textarea is clean", function() {
        var form = getFormWithHtmlContent("<textarea name='textarea1'>this is the defaultValue</textarea>"),
            result = form.isDirty();

        equal(result, false, "A form with a clean textarea is clean");
    });

    test("A form containing a changed textarea is dirty", function() {
        var form = getFormWithHtmlContent("<textarea name='textarea1'>this is the defaultValue</textarea>");
        form.find("textarea").val("dirty value");
        var result = form.isDirty();

        equal(result, true, "A form with a dirty textarea is dirty");
    });

    test("A form containing a changed text input is dirty", function() {
        var form = getFormWithHtmlContent("<input type='text' value='this is the defaultValue' name='input1'/>");
        form.find("input").val("dirty value");
        var result = form.isDirty();

        equal(result, true, "A form with a dirty text input is dirty");
    });

})();
