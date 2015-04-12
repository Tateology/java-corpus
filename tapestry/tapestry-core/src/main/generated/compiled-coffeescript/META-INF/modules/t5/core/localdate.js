(function() {
  define(["./dom", "./moment"], function(dom, moment) {
    var ATTR;
    ATTR = "data-localdate-format";
    return dom.scanner("[" + ATTR + "]", function(el) {
      var format, isoString, m;
      format = el.attr(ATTR);
      isoString = el.text();
      m = moment(isoString);
      el.update(m.format(format));
      el.attr(ATTR, null);
    });
  });

}).call(this);
