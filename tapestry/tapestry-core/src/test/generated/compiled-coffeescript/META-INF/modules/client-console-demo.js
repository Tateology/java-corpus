(function() {
  define(["t5/core/dom", "t5/core/console"], function(dom, console) {
    var name, _fn, _i, _len, _ref;
    _ref = ["debug", "info", "warn", "error"];
    _fn = function(name) {
      return (dom(name)).on("change", function() {
        return console[name](this.value());
      });
    };
    for (_i = 0, _len = _ref.length; _i < _len; _i++) {
      name = _ref[_i];
      _fn(name);
    }
  });

}).call(this);
