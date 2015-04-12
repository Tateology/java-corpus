(function() {
  define(["./events", "./dom", "./console"], function(events, dom, console) {
    var initialize;
    initialize = function(zoneId, period, url) {
      var cleanUp, executing, handler, intervalId, zone;
      zone = dom(zoneId);
      if (!zone) {
        console.err("Zone " + zoneId + " not found for periodic refresh.");
        return;
      }
      if (zone.meta("periodic-refresh")) {
        return;
      }
      zone.meta("periodic-refresh", true);
      executing = false;
      zone.on(events.zone.didUpdate, function() {
        return executing = false;
      });
      cleanUp = function() {
        window.clearInterval(intervalId);
        return zone = null;
      };
      handler = function() {
        if (executing) {
          return;
        }
        if (!(zone.closest('body'))) {
          cleanUp();
          return;
        }
        executing = true;
        return zone.trigger(events.zone.refresh, {
          url: url
        });
      };
      intervalId = window.setInterval(handler, period * 1000);
      return (dom(window)).on("beforeunload", cleanUp);
    };
    return initialize;
  });

}).call(this);
