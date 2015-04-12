(function() {
  define(["./dom", "./events", "./console", "./ajax"], function(dom, events, console, ajax) {
    var AFL_SELECTOR, FRAGMENT_TYPE;
    AFL_SELECTOR = "[data-container-type='core/AjaxFormLoop']";
    FRAGMENT_TYPE = "core/ajaxformloop-fragment";
    dom.onDocument("click", "" + AFL_SELECTOR + " [data-afl-behavior=remove]", function() {
      var afl, url;
      afl = this.findParent(AFL_SELECTOR);
      if (!afl) {
        console.error("Enclosing element for AjaxFormLoop remove row link not found.");
        return false;
      }
      url = afl.attr("data-remove-row-url");
      ajax(url, {
        data: {
          "t:rowvalue": (this.closest("[data-afl-row-value]")).attr("data-afl-row-value")
        },
        success: (function(_this) {
          return function() {
            var fragment;
            fragment = _this.findParent("[data-container-type='" + FRAGMENT_TYPE + "']");
            return fragment.remove();
          };
        })(this)
      });
      return false;
    });
    dom.onDocument("click", "" + AFL_SELECTOR + " [data-afl-behavior=insert-before] [data-afl-trigger=add]", function() {
      var afl, insertionPoint, url;
      afl = this.findParent(AFL_SELECTOR);
      insertionPoint = this.findParent("[data-afl-behavior=insert-before]");
      url = afl.attr("data-inject-row-url");
      ajax(url, {
        success: (function(_this) {
          return function(response) {
            var content, newElement, _ref;
            content = ((_ref = response.json) != null ? _ref.content : void 0) || "";
            newElement = "<" + insertionPoint.element.tagName + " class=\"" + insertionPoint.element.className + "\"\n  data-container-type=\"" + FRAGMENT_TYPE + "\">\n  " + content + "\n  </" + insertionPoint.element.tagName + ">";
            insertionPoint.insertBefore(newElement);
            return insertionPoint.trigger(events.zone.didUpdate);
          };
        })(this)
      });
      return false;
    });
    return null;
  });

}).call(this);
