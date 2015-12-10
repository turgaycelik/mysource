var lookupTable = new Array();
function addEntry(projectId, configId)
{
    lookupTable[projectId] = configId;
}

function lookup(projectId)
{
    return lookupTable[projectId];
}

var configToDefaultOption = new Array();
function addDefaultEntry(configId, optionId)
{
    configToDefaultOption[configId] = optionId;
}

function setDefaultValue(projectListId, issueTypeListId)
{
    try
    {
        var sel1 = document.getElementById(projectListId);
        var projectId = getInputValue(sel1);
        var configId = lookup(projectId);
        var defaultId = lookupDefault(configId);
        document.getElementById(issueTypeListId + defaultId).selected = true;
    }
    catch(e){}
}

function lookupDefault(configId)
{
    return configToDefaultOption[configId];
}

function getInputValue(elem)
{
    if (elem.options)
    {
        return elem.options[elem.selectedIndex].value;
    }
    else
    {
        return elem.value;
    }
}

function dynamicIssueTypeSelect(id1, id2) {

    function create() {
        // Clone the dynamic select box
        var clone = AJS.$(sel2).clone(true,true);
		// Obtain references to all cloned options
        var clonedOptions = clone.find("option");

        // Remove all optgroups (bloody Opera!)
        var optgroups = sel2.getElementsByTagName("optgroup");
        while (optgroups.length > 0)
        {
            sel2.removeChild(optgroups[0]);
	    }

        // Onload init: call a generic function to display the related options in the dynamic select box
		refreshDynamicIssueTypeOptions(sel1, sel2, clonedOptions);
		// Onchange of the main select box: call a generic function to display the related options in the dynamic select box
		sel1.onchange = function() {
			refreshDynamicIssueTypeOptions(sel1, sel2, clonedOptions);
            setDefaultValue(id1, id2);
        };
    }

	// Feature test to see if there is enough W3C DOM support
	if (document.getElementById && document.getElementsByTagName) {
		// Obtain references to both select boxes
		var sel1 = document.getElementById(id1);
		var sel2 = document.getElementById(id2);

        // if these are null then the script got executed before elements where appended to DOM. This can happen in dialogs
        // When building content up outside of the document jQuery will eval scripts. Wait until the dialog content is appended to
        // document and try again.
        if (sel1 === null || sel2 === null) {
            AJS.$(document).bind("dialogContentReady", function(e, dialog) {
                sel1 = document.getElementById(id1);
		        sel2 = document.getElementById(id2);

                if (sel1 && sel2) {
                    create();
                } else {
                    console.warn("Dynamic issue type select failed to initialize, <select> elements could not be found.")
                }
            });
        } else {
            create();
        }
	}
}
function refreshDynamicIssueTypeOptions(sel1, sel2, clonedOptions) {
	// Delete all options of the dynamic select box
	while (sel2.options.length) {
		sel2.remove(0);
	}
	// Create regular expression objects for "select" and the value of the selected option of the main select box as class names
	var pattern1 = /( |^)(select)( |$)/;
	var pattern2 = new RegExp("( |^)(" + lookup(getInputValue(sel1)) + ")( |$)");
	// Iterate through all cloned options
	for (var i = 0; i < clonedOptions.length; i++) {
		// If the classname of a cloned option either equals "select" or equals the value of the selected option of the main select box
		if (clonedOptions[i].className.match(pattern1) || clonedOptions[i].className.match(pattern2)) {
			// Clone the option from the hidden option pool and append it to the dynamic select box
            addOption(clonedOptions[i], sel2);
        }
	}
}

function matchAnyRegex(s, array)
{
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
