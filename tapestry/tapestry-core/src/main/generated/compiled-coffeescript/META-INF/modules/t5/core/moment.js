(function() {
  define(["moment"], function(moment) {
    var locale;
    locale = (document.documentElement.getAttribute("data-locale")) || "en";
    moment.locale(locale);
    return moment;
  });

}).call(this);
