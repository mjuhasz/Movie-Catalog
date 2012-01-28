$(function() {
    var availableMovies = function(request, response) {
        $.ajax("/movies.json?q=" + encodeURIComponent(request.term), {
            dataType: "json",
            success: function(result) {
              var matches = $.map(result.content, function(match) {
                  return {
                      value: match.title,
                      label: match.title + " - " + match.title_hu,
                      movie: match
                  };
              });
              response(matches);
            },
            error: function() {
              response([])
            }
        });
    };
    $("#movieSearch input[type=text]").autocomplete({
        minLength: 3,
        source: availableMovies,
        change: function(event, ui) {
            $("#movieSearch").submit();
        }
    });
    $("#movieSearch").submit(function (event) {
        var value = $("#movieSearch input[type=text]").val().trim();
        if (!value) {
            event.preventDefault();
        }
    });
});