(function() {
  var __slice = [].slice;

  define(["./console"], function(console) {
    return function() {
      var args, fn, initName;
      initName = arguments[0], args = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
      fn = T5.initializers[initName];
      if (!fn) {
        return console.error("Initialization function '" + initName + "' not found in T5.initializers namespace.");
      } else {
        return fn.apply(null, args);
      }
    };
  });

}).call(this);
