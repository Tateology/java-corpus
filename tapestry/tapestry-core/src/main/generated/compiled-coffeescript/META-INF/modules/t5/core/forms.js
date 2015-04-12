(function() {
  define(["./events", "./dom", "underscore"], function(events, dom, _) {
    var SKIP_VALIDATION, clearSubmittingHidden, defaultValidateAndSubmit, exports, gatherParameters, setSubmittingHidden;
    SKIP_VALIDATION = "t5:skip-validation";
    clearSubmittingHidden = function(form) {
      var hidden;
      hidden = form.findFirst("[name='t:submit']");
      hidden && hidden.value(null);
      form.meta(SKIP_VALIDATION, null);
    };
    setSubmittingHidden = function(form, submitter) {
      var firstHidden, hidden, isCancel, mode, name;
      mode = submitter.attr("data-submit-mode");
      isCancel = mode === "cancel";
      if (mode && mode !== "normal") {
        form.meta(SKIP_VALIDATION, true);
      }
      hidden = form.findFirst("[name='t:submit']");
      if (!hidden) {
        firstHidden = form.findFirst("input[type=hidden]");
        hidden = dom.create("input", {
          type: "hidden",
          name: "t:submit"
        });
        firstHidden.insertBefore(hidden);
      }
      name = isCancel ? "cancel" : submitter.element.name;
      hidden.value("[\"" + submitter.element.id + "\",\"" + name + "\"]");
    };
    gatherParameters = function(form) {
      var fields, result;
      result = {};
      fields = form.find("input, select, textarea");
      _.each(fields, function(field) {
        var existing, name, type, value;
        if (field.attr("disabled")) {
          return;
        }
        type = field.element.type;
        if (type === "file" || type === "submit") {
          return;
        }
        if ((type === "checkbox" || type === "radio") && field.checked() === false) {
          return;
        }
        value = field.value();
        if (value === null) {
          return;
        }
        name = field.element.name;
        if (name === "") {
          return;
        }
        existing = result[name];
        if (_.isArray(existing)) {
          existing.push(value);
          return;
        }
        if (existing) {
          result[name] = [existing, value];
          return;
        }
        return result[name] = value;
      });
      return result;
    };
    defaultValidateAndSubmit = function() {
      var error, field, focusField, hasError, memo, where, _i, _len, _ref;
      where = function() {
        return "processing form submission";
      };
      try {
        if (((this.attr("data-validate")) === "submit") && (!this.meta(SKIP_VALIDATION))) {
          this.meta(SKIP_VALIDATION, null);
          hasError = false;
          focusField = null;
          _ref = this.find("[data-validation]");
          for (_i = 0, _len = _ref.length; _i < _len; _i++) {
            field = _ref[_i];
            memo = {};
            where = function() {
              return "triggering " + events.field.inputValidation + " event on " + (field.toString());
            };
            field.trigger(events.field.inputValidation, memo);
            if (memo.error) {
              hasError = true;
              if (!focusField) {
                focusField = field;
              }
            }
          }
          if (!hasError) {
            memo = {};
            where = function() {
              return "trigging cross-form validation event";
            };
            this.trigger(events.form.validate, memo);
            hasError = memo.error;
          }
          if (hasError) {
            clearSubmittingHidden(this);
            if (focusField) {
              focusField.focus();
            }
            return false;
          }
        }
        where = function() {
          return "triggering " + events.form.prepareForSubmit + " event (after validation)";
        };
        this.trigger(events.form.prepareForSubmit);
      } catch (_error) {
        error = _error;
        console.error("Form validiation/submit error `" + (error.toString()) + "', in form " + (this.toString()) + ", " + (where()));
        console.error(error);
        return false;
      }
    };
    dom.onDocument("submit", "form", defaultValidateAndSubmit);
    dom.onDocument("click", "input[type=submit], input[type=image]", function() {
      setSubmittingHidden(dom(this.element.form), this);
    });
    dom.onDocument("click", "a[data-submit-mode]", function() {
      var form;
      form = this.findParent("form");
      if (!form) {
        console.error("Submitting link element not contained inside a form element.");
        return false;
      }
      setSubmittingHidden(form, this.closest("a[data-submit-mode]"));
      form.trigger("submit");
      return false;
    });
    return exports = {
      gatherParameters: gatherParameters,
      setSubmittingElement: setSubmittingHidden,
      skipValidation: function(form) {
        return form.meta(SKIP_VALIDATION, true);
      }
    };
  });

}).call(this);
