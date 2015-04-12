(function() {
  define(["./dom", "./console", "./messages", "./ajax", "underscore", "./bootstrap"], function(dom, console, messages, ajax, _, _arg) {
    var alert, dismissAll, dismissOne, exports, findInnerContainer, getURL, glyph, removeAlert, setupUI, severityToClass;
    glyph = _arg.glyph;
    severityToClass = {
      info: "alert-info",
      success: "alert-success",
      warn: "alert-warning",
      error: "alert-danger"
    };
    getURL = function(container) {
      return container.attr("data-dismiss-url");
    };
    removeAlert = function(container, alert) {
      alert.remove();
      if (container.find(".alert").length === 0) {
        return container.update(null);
      }
    };
    dismissAll = function(container) {
      var alerts;
      alerts = container.find("[data-alert-id]");
      if (alerts.length === 0) {
        container.update(null);
        return;
      }
      return ajax(getURL(container), {
        success: function() {
          return container.update(null);
        }
      });
    };
    dismissOne = function(container, button) {
      var alert, id;
      alert = button.parent();
      id = alert.attr("data-alert-id");
      if (!id) {
        removeAlert(container, alert);
        return;
      }
      return ajax(getURL(container), {
        data: {
          id: id
        },
        success: function() {
          return removeAlert(container, alert);
        }
      });
    };
    setupUI = function(outer) {
      outer.update("<div data-container-type=\"inner\"></div>");
      if ((outer.attr("data-show-dismiss-all")) === "true") {
        outer.append("<div class=\"pull-right\">\n   <button class=\"btn btn-xs btn-default\" data-action=\"dismiss-all\">\n     " + (glyph("remove")) + "\n     " + (messages("core-dismiss-label")) + "\n   </button>\n </div>");
      }
      outer.on("click", "[data-action=dismiss-all]", function() {
        dismissAll(outer);
        return false;
      });
      return outer.on("click", "button.close", function() {
        dismissOne(outer, this);
        return false;
      });
    };
    findInnerContainer = function() {
      var outer;
      outer = dom.body.findFirst("[data-container-type=alerts]");
      if (!outer) {
        console.error("Unable to locate alert container element to present an alert.");
        return null;
      }
      if (!outer.element.firstChild) {
        setupUI(outer);
      }
      return outer != null ? outer.findFirst("[data-container-type=inner]") : void 0;
    };
    alert = function(data) {
      var className, container, content, element, outerContainer;
      container = findInnerContainer();
      if (!container) {
        return;
      }
      className = severityToClass[data.severity] || "alert-info";
      content = data.markup ? data.message : _.escape(data.message);
      element = dom.create("div", {
        "data-alert-id": data.id,
        "class": "alert alert-dismissable " + className
      }, data.markup ? "<button type=\"button\" class=\"close\">&times;</button>\n" + content : "<button type=\"button\" class=\"close\">&times;</button>\n<span>" + content + "</span>");
      container.append(element);
      if (data['transient']) {
        outerContainer = container.findParent('[data-container-type=alerts]');
        return _.delay(removeAlert, exports.TRANSIENT_DURATION, outerContainer, element);
      }
    };
    alert.TRANSIENT_DURATION = 5000;
    return exports = alert;
  });

}).call(this);
