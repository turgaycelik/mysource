/* A better cascading select list from http://www.bobbyvandersluis.com/articles/unobtrusivedynamicselect.php */
function dynamicSelect(id1, id2) {
	// Feature test to see if there is enough W3C DOM support
	if (document.getElementById && document.getElementsByTagName) {
		// Obtain references to both select boxes
		var sel1 = document.getElementById(id1);
		var sel2 = document.getElementById(id2);
		// Clone the dynamic select box
		var clone = sel2.cloneNode(true);
		// Obtain references to all cloned options
		var clonedOptions = clone.getElementsByTagName("option");

        // Fix IE bug JRA-8560 that fails to copy the selected value correctly
        var sel2Options = sel2.options;
        for (var i = 0; i < sel2Options.length; i++)
		{
            if (sel2Options[i].selected)
            {
                clonedOptions[i].selected = true;
                // JRA-9164 Opera workaround - selected item not being selected
                if (!clonedOptions[i].selected)
                {
                    clonedOptions[i].setAttribute('selected', 'true');
                }

            }
        }

		// Onload init: call a generic function to display the related options in the dynamic select box
		refreshDynamicSelectOptions(sel1, sel2, clonedOptions);
		// Onchange of the main select box: call a generic function to display the related options in the dynamic select box
		sel1.onchange = function() {
			refreshDynamicSelectOptions(sel1, sel2, clonedOptions);
		};
	}
}
function refreshDynamicSelectOptions(sel1, sel2, clonedOptions) {
	// Delete all options of the dynamic select box
	while (sel2.options.length) {
		sel2.remove(0);
	}
	// Create regular expression objects for "select" and the value of the selected option of the main select box as class names
	var pattern1 = /( |^)(select)( |$)/;
	var pattern2 = new RegExp("(:|^)(" + sel1.options[sel1.selectedIndex].value + ")(:|$)");
	// Iterate through all cloned options
	for (var i = 0; i < clonedOptions.length; i++) {
		// If the classname of a cloned option either equals "select" or equals the value of the selected option of the main select box
        if (clonedOptions[i].className.match(pattern1) || clonedOptions[i].className.match(pattern2)) {
			// Clone the option from the hidden option pool and append it to the dynamic select box
            addOption(clonedOptions[i], sel2);
        }
	}

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
