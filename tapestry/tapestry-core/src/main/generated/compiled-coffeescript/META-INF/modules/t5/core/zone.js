(function() {
  define(["./dom", "./events", "./ajax", "./console", "./forms", "underscore"], function(dom, events, ajax, console, forms, _) {
    var deferredZoneUpdate, findZone;
    findZone = function(element) {
      var zone, zoneId;
      zoneId = element.attr("data-update-zone");
      if (zoneId === "^") {
        zone = element.findParent("[data-container-type=zone]");
        if (zone === null) {
          throw new Error("Unable to locate containing zone for " + element + ".");
        }
        return zone;
      }
      zone = dom(zoneId);
      if (zone === null) {
        throw new Error("Unable to locate zone '" + zoneId + "'.");
      }
      return zone;
    };
    dom.onDocument("click", "a[data-update-zone]", function() {
      var element, zone;
      element = this.closest("[data-update-zone]");
      if (!element) {
        throw new Error("Could not locate containing element with data-update-zone attribute.");
      }
      zone = findZone(element);
      if (zone) {
        zone.trigger(events.zone.refresh, {
          url: element.attr("href")
        });
      }
      return false;
    });
    dom.onDocument("submit", "form[data-update-zone]", function() {
      var formParameters, zone;
      zone = findZone(this);
      if (zone) {
        formParameters = forms.gatherParameters(this);
        zone.trigger(events.zone.refresh, {
          url: this.attr("action"),
          parameters: formParameters
        });
      }
      return false;
    });
    dom.onDocument("submit", "form[data-async-trigger]", function() {
      var formParameters;
      formParameters = forms.gatherParameters(this);
      this.addClass("ajax-update");
      ajax(this.attr("action"), {
        data: formParameters,
        complete: (function(_this) {
          return function() {
            return _this.removeClass("ajax-update");
          };
        })(this)
      });
      return false;
    });
    dom.onDocument(events.zone.update, function(event) {
      var content;
      this.trigger(events.zone.willUpdate);
      content = event.memo.content;
      if (content !== void 0) {
        this.update(content);
      }
      return this.trigger(events.zone.didUpdate);
    });
    dom.onDocument(events.zone.refresh, function(event) {
      var attr, parameters, simpleIdParams, zone;
      zone = this.closest("[data-container-type=zone]");
      attr = zone.attr("data-zone-parameters");
      parameters = attr && JSON.parse(attr);
      simpleIdParams = zone.attr("data-simple-ids") ? {
        "t:suppress-namespaced-ids": true
      } : void 0;
      return ajax(event.memo.url, {
        data: _.extend({
          "t:zoneid": zone.element.id
        }, simpleIdParams, parameters, event.memo.parameters),
        success: function(response) {
          var _ref;
          return zone.trigger(events.zone.update, {
            content: (_ref = response.json) != null ? _ref.content : void 0
          });
        }
      });
    });
    dom.onDocument("click", "a[data-async-trigger]", function() {
      var link;
      link = this.closest('a[data-async-trigger]');
      link.addClass("ajax-update");
      ajax(link.attr("href"), {
        complete: function() {
          return link.removeClass("ajax-update");
        }
      });
      return false;
    });
    deferredZoneUpdate = function(id, url) {
      return _.defer(function() {
        var zone;
        zone = dom(id);
        if (zone === null) {
          console.error("Could not locate element '" + id + "' to update.");
          return;
        }
        return zone.trigger(events.zone.refresh, {
          url: url
        });
      });
    };
    return {
      deferredZoneUpdate: deferredZoneUpdate,
      findZone: findZone
    };
  });

}).call(this);
