(function() {
  define(["./dom"], function(dom) {
    dom.onDocument("click", "[data-behavior=stack-trace-filter-toggle]", function() {
      var checked, traceList, _i, _len, _ref;
      checked = this.element.checked;
      _ref = dom.body.find(".stack-trace");
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        traceList = _ref[_i];
        traceList[checked ? "addClass" : "removeClass"]("filtered");
      }
    });
    return null;
  });

}).call(this);
