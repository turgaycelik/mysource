AJS.toInit(function($) {
    var warningMessage = AJS.$('#mode_warning');
    var initialMode = AJS.$('#mode_select option:selected').val();
    AJS.$('#mode_select').change(function() {
        if(initialMode == AJS.$('#mode_select option:selected').val()) {
            warningMessage.hide();
        }
        else {
            warningMessage.show();
        }
    });    
});
