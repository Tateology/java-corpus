(function() {
  require(["t5/core/dom", "t5/core/events"], function(dom, events) {
    return dom.onDocument(events.zone.didUpdate, function() {
      return (dom("zone-update-message")).update("Zone updated.");
    });
  });

}).call(this);
