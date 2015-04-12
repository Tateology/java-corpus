(function() {
  require(["t5/core/dom"], function(dom) {
    module("t5/core/dom");
    test("get wrapped element by id", function() {
      var e;
      e = dom("dom-eventelement-native");
      return ok(e !== null, "element found and wrapped");
    });
    test("get wrapped element by unknown id is null", function() {
      var e;
      e = dom("dom-does-not-exist-element");
      return ok(e === null, "element not found and null");
    });
    test("trigger native events", function() {
      var button, clicks, container;
      clicks = 0;
      container = dom("dom-eventelement-native");
      button = container.findFirst("a");
      container.on("click", "a", function() {
        clicks++;
        return false;
      });
      button.trigger("click");
      return equal(clicks, 1, "native event was triggered");
    });
    test("selector used with events filters", function() {
      var clicks, container, primary, secondary;
      clicks = 0;
      container = dom("dom-eventelement-selector");
      primary = container.findFirst("a.btn-primary");
      secondary = container.findFirst("a[data-use=secondary]");
      container.on("x:click", "a.btn-primary", function() {
        clicks++;
        return false;
      });
      primary.trigger("x:click");
      equal(clicks, 1, "click on selected element invokes handler");
      secondary.trigger("x:click");
      return equal(clicks, 1, "click on non-selected element does not invoke handler");
    });
    test("this is matched element in handler", function() {
      var container, primary;
      container = dom("dom-eventelement-matched");
      primary = container.findFirst("a.btn-primary");
      container.on("x:click", "a.btn-primary", function() {
        strictEqual(this.element, primary.element, "this should be the wrapper for element that was matched");
        return false;
      });
      return primary.trigger("x:click");
    });
    return test("visibility, hide(), and show()", function() {
      var e;
      e = (dom("dom-visibility")).findFirst("span");
      equal(e.visible(), true, "element is initially visible");
      e.hide();
      equal(e.visible(), false, "element is not visible once hidden");
      e.show();
      return equal(e.visible(), true, "element is visible against once shown");
    });
  });

}).call(this);
