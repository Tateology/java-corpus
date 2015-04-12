(function() {
  define(["t5/core/dom", "t5/core/events", "underscore", "t5/core/console"], function(dom, events, _, console) {
    return dom.body.on(events.palette.willChange, function(event, memo) {
      var values;
      console.info("palette-demo, palette willChange");
      values = _.map(memo.selectedOptions, function(o) {
        return o.value;
      });
      (dom("event-selection")).update(JSON.stringify(values));
      return (dom("event-reorder")).update(memo.reorder.toString());
    });
  });

}).call(this);
