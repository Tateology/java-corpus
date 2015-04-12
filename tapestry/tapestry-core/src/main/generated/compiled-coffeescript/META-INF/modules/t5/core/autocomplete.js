(function() {
  define(["./dom", "./ajax", "jquery", "./utils", "./typeahead"], function(dom, ajax, $, _arg) {
    var exports, extendURL, init;
    extendURL = _arg.extendURL;
    init = function(spec) {
      var $field;
      $field = $(document.getElementById(spec.id));
      return $field.typeahead({
        minLength: spec.minChars,
        limit: spec.limit,
        remote: {
          url: spec.url,
          replace: function(uri, query) {
            return extendURL(uri, {
              "t:input": query
            });
          },
          filter: function(response) {
            return response.matches;
          }
        }
      });
    };
    return exports = init;
  });

}).call(this);
