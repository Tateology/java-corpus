(function() {
  define(["./dom", "./events", "./messages", "./ajax", "underscore", "./datepicker", "./fields"], function(dom, events, messages, ajax, _, DatePicker) {
    var Controller, activePopup, datePickerFirstDay, days, daysLabels, isPartOfPopup, monthsLabels, noneLabel, serverFirstDay, todayLabel;
    serverFirstDay = parseInt(messages("date-symbols.first-day"));
    datePickerFirstDay = serverFirstDay === 0 ? 6 : serverFirstDay - 1;
    days = (messages("date-symbols.days")).split(",");
    days.push(days.shift());
    monthsLabels = (messages("date-symbols.months")).split(",");
    daysLabels = _.map(days, function(name) {
      return name.substr(0, 1).toLowerCase();
    });
    todayLabel = messages("core-datefield-today");
    noneLabel = messages("core-datefield-none");
    activePopup = null;
    isPartOfPopup = function(element) {
      return (element.findParent(".labelPopup") != null) || (element.findParent(".datefield-popup") != null);
    };
    dom.body.on("click", function() {
      if (activePopup && !isPartOfPopup(this)) {
        activePopup.hide();
        activePopup = null;
      }
    });
    Controller = (function() {
      function Controller(container) {
        this.container = container;
        this.field = this.container.findFirst("input");
        this.trigger = this.container.findFirst("button");
        this.trigger.on("click", (function(_this) {
          return function() {
            _this.doTogglePopup();
            return false;
          };
        })(this));
      }

      Controller.prototype.showPopup = function() {
        if (activePopup && activePopup !== this.popup) {
          activePopup.hide();
        }
        this.popup.show();
        return activePopup = this.popup;
      };

      Controller.prototype.hidePopup = function() {
        this.popup.hide();
        return activePopup = null;
      };

      Controller.prototype.doTogglePopup = function() {
        var value;
        if (this.field.element.disabled) {
          return;
        }
        if (!this.popup) {
          this.createPopup();
          if (activePopup != null) {
            activePopup.hide();
          }
        } else if (this.popup.visible()) {
          this.hidePopup();
          return;
        }
        value = this.field.value();
        if (value === "") {
          this.datePicker.setDate(null);
          this.showPopup();
          return;
        }
        this.field.addClass("ajax-wait");
        return ajax(this.container.attr("data-parse-url"), {
          data: {
            input: value
          },
          onerror: (function(_this) {
            return function(message) {
              _this.field.removeClass("ajax-wait");
              _this.fieldError(message);
              _this.showPopup();
            };
          })(this),
          success: (function(_this) {
            return function(response) {
              var date, reply;
              _this.field.removeClass("ajax-wait");
              reply = response.json;
              if (reply.result) {
                _this.clearFieldError();
                date = new Date();
                date.setTime(reply.result);
                _this.datePicker.setDate(date);
              }
              if (reply.error) {
                _this.fieldError(_.escape(reply.error));
                _this.datePicker.setDate(null);
              }
              _this.showPopup();
            };
          })(this)
        });
      };

      Controller.prototype.fieldError = function(message) {
        return this.field.focus().trigger(events.field.showValidationError, {
          message: message
        });
      };

      Controller.prototype.clearFieldError = function() {
        return this.field.trigger(events.field.clearValidationError);
      };

      Controller.prototype.createPopup = function() {
        this.datePicker = new DatePicker();
        this.datePicker.setFirstWeekDay(datePickerFirstDay);
        this.datePicker.setLocalizations(monthsLabels, daysLabels, todayLabel, noneLabel);
        this.popup = dom.create("div", {
          "class": "datefield-popup well"
        }).append(this.datePicker.create());
        this.container.insertAfter(this.popup);
        return this.datePicker.onselect = _.bind(this.onSelect, this);
      };

      Controller.prototype.onSelect = function() {
        var date;
        date = this.datePicker.getDate();
        if (date === null) {
          this.hidePopup();
          this.clearFieldError();
          this.field.value("");
          return;
        }
        this.field.addClass("ajax-wait");
        return ajax(this.container.attr("data-format-url"), {
          data: {
            input: date.getTime()
          },
          failure: (function(_this) {
            return function(response, message) {
              _this.field.removeClass("ajax-wait");
              return _this.fieldError(message);
            };
          })(this),
          success: (function(_this) {
            return function(response) {
              _this.field.removeClass("ajax-wait");
              _this.clearFieldError();
              _this.field.value(response.json.result);
              return _this.hidePopup();
            };
          })(this)
        });
      };

      return Controller;

    })();
    dom.scanner("[data-component-type='core/DateField']", function(container) {
      container.attr("data-component-type", null);
      return new Controller(container);
    });
    return null;
  });

}).call(this);
