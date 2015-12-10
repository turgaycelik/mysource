var lookupTable = new Array();
function addEntry(projectId, configId)
{
    lookupTable[projectId] = configId;
}

function lookup(projectId)
{
    return lookupTable[projectId];
}

function getInputValues(elem)
{
    return getMultiSelectValuesAsArray(elem)
}

function dynamicMultiIssueTypeSelect(id1, id2) {
    // Feature test to see if there is enough W3C DOM support
    if (document.getElementById && document.getElementsByTagName) {
        // Obtain references to both select boxes
        var sel1 = document.getElementById(id1);
        var sel2 = document.getElementById(id2);
        // Clone the dynamic select box
        var clone = AJS.$(sel2).clone(true,true);
        // Obtain references to all cloned options
        var clonedOptions = clone.find("option");

        // Onload init: call a generic function to display the related options in the dynamic select box
        refreshDynamicMultiIssueTypeOptions(sel1, sel2, clonedOptions);
        // Onchange of the main select box: call a generic function to display the related options in the dynamic select box

        sel1.onchange = function() {
            refreshDynamicMultiIssueTypeOptions(sel1, sel2, clonedOptions);
            toggleRefresh();
            scrollToTop(sel2);
        };
    }
}

function scrollToTop(issueTypeSel) {
    // just reset the scrollTop
    issueTypeSel.scrollTop = 0;
}

var lastArray;

function selectOption(option)
{
    try {
        option.selected = true;
    } catch(e) {
        option.setAttribute('selected', 'true');
    }
}

function unselectOption(option)
{
    try {
        option.selected = false;
    } catch(e) {
        option.setAttribute('selected', 'false');
    }
}

function refreshDynamicMultiIssueTypeOptions(sel1, sel2, clonedOptions) {

    var selectedProjects = getInputValues(sel1);
    // var selectedConfigs = getSelected
    // If the selected projects is the same as the last one, the don't refresh


    // Get selected values for issue types list
    var lastSelected = getInputValues(sel2);

    // Delete all options of the dynamic select box
    while (sel2.options.length) {
        sel2.remove(0);
    }
    // Create regular expression objects for "select" and the value of the selected option of the main select box as class names
    var pattern1 = /( |^)(headerOption)( |$)/;


    // Iterate through all cloned options
    for (var i = 0; i < clonedOptions.length; i++) {
        // If the classname of a cloned option either equals "select" or equals the value of the selected option of the main select box
        if (clonedOptions[i].className.match(pattern1) || matchAnyRegex(clonedOptions[i].className, selectedProjects)) {
            // Clone the option from the hidden option pool and append it to the dynamic select box
            addOption(clonedOptions[i], sel2);
        }
    }

    // Restore previously selected
    for (var i = 0; i < sel2.options.length; i++) {
        var currentOpt = sel2.options[i];
        if (arrayContains(lastSelected, currentOpt.value)) {
            selectOption(currentOpt);
        } else {
            unselectOption(currentOpt);
        }
    }
}

function matchAnyRegex(s, array)
{
    if (array.length == 0 || (array.length == 1 && array[0] == "-1"))
        return true;

    for (var i = 0; i < array.length; i++) {
        var pattern = new RegExp("( |^)(" + lookup(array[i]) + ")( |$)");
        if (s.match(pattern)) {
            return true
        }
    }
    return false;
}

// JRA-9388 - can not use appendChild with the cloned option since this causes IE to pop up a dialog about unsecured
// content when running via SSL
function addOption(clonedOption, sel2)
{
    var opt = new Option(clonedOption.text, clonedOption.value);
    opt.id = clonedOption.id;
    opt.className = clonedOption.className;
    opt.title = clonedOption.title;
    opt.style.backgroundImage = clonedOption.style.backgroundImage;
    sel2.options[sel2.options.length] = opt;
    if (clonedOption.selected || clonedOption.getAttribute('selected'))
    {
        // JRA-9164 Opera workaround - selected item not being selected
        opt.selected = true;
    }
}
