(function() {
  var locale;

  locale = (document.documentElement.getAttribute("data-locale")) || "en";

  define(["./messages/" + locale, "underscore", "./console"], function(messages, _, console) {
    var get;
    get = function(key) {
      var value;
      value = messages[key];
      if (value) {
        return value;
      } else {
        console.error("No value for message catalog key '" + key + "' exists.");
        return "[[Missing Key: '" + key + "']]";
      }
    };
    get.keys = function() {
      return _.keys(messages);
    };
    return get;
  });

}).call(this);
