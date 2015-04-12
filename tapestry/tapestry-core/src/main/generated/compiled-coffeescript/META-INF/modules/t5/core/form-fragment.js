(function() {
  define(["underscore", "./dom", "./events", "./forms"], function(_, dom, events) {
    var SELECTOR, linkTrigger;
    SELECTOR = "[data-component-type='core/FormFragment']";
    dom.onDocument(events.form.prepareForSubmit, "form", function(event) {
      var fragments;
      fragments = this.find(SELECTOR);
      return _.each(fragments, function(frag) {
        var fragmentId, hidden;
        fragmentId = frag.attr("id");
        hidden = frag.findFirst("input[type=hidden][data-for-fragment='" + fragmentId + "']");
        return hidden && hidden.attr("disabled", !frag.deepVisible());
      });
    });
    dom.onDocument(events.formfragment.changeVisibility, SELECTOR, function(event) {
      var makeVisible;
      makeVisible = event.memo.visible;
      this[makeVisible ? "show" : "hide"]();
      this.trigger(events.element[makeVisible ? "didShow" : "didHide"]);
      return false;
    });
    linkTrigger = function(spec) {
      var invert, trigger, update;
      trigger = dom(spec.triggerId);
      invert = spec.invert || false;
      update = function() {
        var checked, makeVisible;
        checked = trigger.element.checked;
        makeVisible = checked !== invert;
        (dom(spec.fragmentId)).trigger(events.formfragment.changeVisibility, {
          visible: makeVisible
        });
      };
      if (trigger.element.type === "radio") {
        return dom.on(trigger.element.form, "click", update);
      } else {
        return trigger.on("click", update);
      }
    };
    return {
      linkTrigger: linkTrigger
    };
  });

}).call(this);
