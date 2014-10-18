AJS.toInit(function() {
    AJS.bind("show.dialog", function(event, data) {
        // store dialog object for use in the config dialog
        AJS.$("#repository-hook-dialog").data("dialog_obj", data.dialog);
    });
});
