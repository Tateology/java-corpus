(function() {
  define(["./dom", "./moment"], function(dom, moment) {
    var ATTR, toMoment, updateDynamics, updateElement;
    ATTR = "data-timeinterval";
    toMoment = function(s) {
      if (s) {
        return moment(s);
      } else {
        return moment();
      }
    };
    updateElement = function(el) {
      var end, plain, start;
      start = toMoment(el.attr("data-timeinterval-start"));
      end = toMoment(el.attr("data-timeinterval-end"));
      plain = el.attr("data-timeinterval-plain");
      el.update(end.from(start, plain));
    };
    updateDynamics = function() {
      var el, _i, _len, _ref;
      _ref = dom.body.find("[" + ATTR + "=dynamic]");
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        el = _ref[_i];
        updateElement(el);
      }
    };
    setInterval(updateDynamics, 1000);
    dom.scanner("[" + ATTR + "=true]", function(el) {
      updateElement(el);
      if ((el.attr("data-timeinterval-start")) && (el.attr("data-timeinterval-end"))) {
        el.attr(ATTR, null);
      } else {
        el.attr(ATTR, "dynamic");
      }
    });
  });

}).call(this);
