(function($){
    $(function() {
        //make sure search for does not get submitted with an empty string
        $('#submit-search').click(function(){
            if($('[name="searchQuery"]').val().length==0)
                return false;
        });

    });
})(jQuery)