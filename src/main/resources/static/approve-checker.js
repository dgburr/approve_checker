AJS.toInit(function() {
    (function($){
        function register_callback(num) {
            $(document.body).on('change', '#enable' + num, function(event) {
                // show/hide container based on state of checkbox
                var container = $("#rule" + num + "-container");
                if(event.currentTarget.checked == true) container.show();
                else container.hide();
                // update height of dialog
                var dialog = $("#repository-hook-dialog").data("dialog_obj");
                if(!(typeof dialog === 'undefined')) dialog.updateHeight();
            });
        }

        for(i = 1; i < 6; i++) register_callback(i);
    })(AJS.$);
});
