(function() {
  define(["t5/core/dom"], function(dom) {
    var exports;
    exports = {
      findCSSMatchCount: function(selector) {
        return dom.body.find(selector).length;
      },
      doesNotExist: function(elementId) {
        return (dom(elementId)) === null;
      }
    };
    window.testSupport = exports;
    return exports;
  });

}).call(this);
