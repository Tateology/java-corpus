(function() {
  define(["./dom", "underscore", "./events"], function(dom, _, events) {
    var PaletteController, isSelected;
    isSelected = function(option) {
      return option.selected;
    };
    PaletteController = (function() {
      function PaletteController(id) {
        this.selected = dom(id);
        this.container = this.selected.findParent(".palette");
        this.available = this.container.findFirst(".palette-available select");
        this.hidden = this.container.findFirst("input[type=hidden]");
        this.select = this.container.findFirst("[data-action=select]");
        this.deselect = this.container.findFirst("[data-action=deselect]");
        this.moveUp = this.container.findFirst("[data-action=move-up]");
        this.moveDown = this.container.findFirst("[data-action=move-down]");
        this.reorder = this.moveUp !== null;
        this.valueToOrderIndex = {};
        _.each(this.available.element.options, (function(_this) {
          return function(option, i) {
            return _this.valueToOrderIndex[option.value] = i;
          };
        })(this));
        this.initialTransfer();
        if (!this.selected.element.disabled) {
          this.updateButtons();
          this.bindEvents();
        }
      }

      PaletteController.prototype.initialTransfer = function() {
        var e, i, movers, option, pos, value, valueToPosition, values, _i, _j, _len, _ref, _results;
        values = JSON.parse(this.hidden.value());
        valueToPosition = {};
        _.each(values, function(v, i) {
          return valueToPosition[v] = i;
        });
        e = this.available.element;
        movers = [];
        for (i = _i = _ref = e.options.length - 1; _i >= 0; i = _i += -1) {
          option = e.options[i];
          value = option.value;
          pos = valueToPosition[value];
          if (pos !== void 0) {
            movers[pos] = option;
            e.remove(i);
          }
        }
        _results = [];
        for (_j = 0, _len = movers.length; _j < _len; _j++) {
          option = movers[_j];
          _results.push(this.selected.element.add(option));
        }
        return _results;
      };

      PaletteController.prototype.updateAfterChange = function() {
        this.updateHidden();
        return this.updateButtons();
      };

      PaletteController.prototype.updateHidden = function() {
        var values;
        values = _.pluck(this.selected.element.options, "value");
        return this.hidden.value(JSON.stringify(values));
      };

      PaletteController.prototype.bindEvents = function() {
        this.container.on("change", "select", (function(_this) {
          return function() {
            _this.updateButtons();
            return false;
          };
        })(this));
        this.select.on("click", (function(_this) {
          return function() {
            _this.doSelect();
            return false;
          };
        })(this));
        this.available.on("dblclick", (function(_this) {
          return function() {
            _this.doSelect();
            return false;
          };
        })(this));
        this.deselect.on("click", (function(_this) {
          return function() {
            _this.doDeselect();
            return false;
          };
        })(this));
        this.selected.on("dblclick", (function(_this) {
          return function() {
            _this.doDeselect();
            return false;
          };
        })(this));
        if (this.reorder) {
          this.moveUp.on("click", (function(_this) {
            return function() {
              _this.doMoveUp();
              return false;
            };
          })(this));
          return this.moveDown.on("click", (function(_this) {
            return function() {
              _this.doMoveDown();
              return false;
            };
          })(this));
        }
      };

      PaletteController.prototype.updateButtons = function() {
        var nothingSelected;
        this.select.element.disabled = this.available.element.selectedIndex < 0;
        nothingSelected = this.selected.element.selectedIndex < 0;
        this.deselect.element.disabled = nothingSelected;
        if (this.reorder) {
          this.moveUp.element.disabled = nothingSelected || this.allSelectionsAtTop();
          return this.moveDown.element.disabled = nothingSelected || this.allSelectionsAtBottom();
        }
      };

      PaletteController.prototype.doSelect = function() {
        return this.transferOptions(this.available, this.selected, this.reorder);
      };

      PaletteController.prototype.doDeselect = function() {
        return this.transferOptions(this.selected, this.available, false);
      };

      PaletteController.prototype.doMoveUp = function() {
        var firstMoverIndex, movers, o, options, pivot, splicePos, _i, _len;
        options = _.toArray(this.selected.element.options);
        movers = _.filter(options, isSelected);
        firstMoverIndex = _.first(movers).index;
        pivot = options[firstMoverIndex - 1];
        options = _.reject(options, isSelected);
        splicePos = pivot ? _.indexOf(options, pivot) : 0;
        movers.reverse();
        for (_i = 0, _len = movers.length; _i < _len; _i++) {
          o = movers[_i];
          options.splice(splicePos, 0, o);
        }
        return this.reorderSelected(options);
      };

      PaletteController.prototype.doMoveDown = function() {
        var lastMoverIndex, movers, o, options, pivot, splicePos, _i, _len;
        options = _.toArray(this.selected.element.options);
        movers = _.filter(options, isSelected);
        lastMoverIndex = _.last(movers).index;
        pivot = options[lastMoverIndex + 1];
        options = _.reject(options, isSelected);
        splicePos = pivot ? _.indexOf(options, pivot) + 1 : options.length;
        movers.reverse();
        for (_i = 0, _len = movers.length; _i < _len; _i++) {
          o = movers[_i];
          options.splice(splicePos, 0, o);
        }
        return this.reorderSelected(options);
      };

      PaletteController.prototype.reorderSelected = function(options) {
        return this.performUpdate(true, options, (function(_this) {
          return function() {
            var o, _i, _len, _results;
            _this.deleteOptions(_this.selected);
            _results = [];
            for (_i = 0, _len = options.length; _i < _len; _i++) {
              o = options[_i];
              _results.push(_this.selected.element.add(o, null));
            }
            return _results;
          };
        })(this));
      };

      PaletteController.prototype.performUpdate = function(reorder, selectedOptions, updateCallback) {
        var canceled, doUpdate, memo;
        canceled = false;
        doUpdate = (function(_this) {
          return function() {
            updateCallback();
            _this.selected.trigger(events.palette.didChange, {
              selectedOptions: selectedOptions,
              reorder: reorder
            });
            return _this.updateAfterChange();
          };
        })(this);
        memo = {
          selectedOptions: selectedOptions,
          reorder: reorder,
          cancel: function() {
            return canceled = true;
          },
          defer: function() {
            canceled = true;
            return doUpdate;
          }
        };
        this.selected.trigger(events.palette.willChange, memo);
        if (!canceled) {
          return doUpdate();
        }
      };

      PaletteController.prototype.deleteOptions = function(select) {
        var e, i, _i, _ref, _results;
        e = select.element;
        _results = [];
        for (i = _i = _ref = e.length - 1; _i >= 0; i = _i += -1) {
          _results.push(e.remove(i));
        }
        return _results;
      };

      PaletteController.prototype.transferOptions = function(from, to, atEnd) {
        var fromOptions, movers, o, selectedOptions, toOptions, _i, _len;
        if (from.element.selectedIndex === -1) {
          return;
        }
        movers = _.filter(from.element.options, isSelected);
        fromOptions = _.reject(from.element.options, isSelected);
        toOptions = _.toArray(to.element.options);
        for (_i = 0, _len = movers.length; _i < _len; _i++) {
          o = movers[_i];
          this.insertOption(toOptions, o, atEnd);
        }
        selectedOptions = to === this.selected ? toOptions : fromOptions;
        return this.performUpdate(false, selectedOptions, (function(_this) {
          return function() {
            var i, _j, _k, _l, _len1, _ref, _ref1, _results;
            for (i = _j = _ref = from.element.length - 1; _j >= 0; i = _j += -1) {
              if (from.element.options[i].selected) {
                from.element.remove(i);
              }
            }
            for (i = _k = _ref1 = to.element.length - 1; _k >= 0; i = _k += -1) {
              to.element.options[i].selected = false;
              to.element.remove(i);
            }
            _results = [];
            for (_l = 0, _len1 = toOptions.length; _l < _len1; _l++) {
              o = toOptions[_l];
              _results.push(to.element.add(o, null));
            }
            return _results;
          };
        })(this));
      };

      PaletteController.prototype.insertOption = function(options, option, atEnd) {
        var before, i, optionOrder;
        if (!atEnd) {
          optionOrder = this.valueToOrderIndex[option.value];
          before = _.find(options, (function(_this) {
            return function(o) {
              return _this.valueToOrderIndex[o.value] > optionOrder;
            };
          })(this));
        }
        if (before) {
          i = _.indexOf(options, before);
          return options.splice(i, 0, option);
        } else {
          return options.push(option);
        }
      };

      PaletteController.prototype.indexOfLastSelection = function(select) {
        var e, i, _i, _ref, _ref1;
        e = select.element;
        if (e.selectedIndex < 0) {
          return -1;
        }
        for (i = _i = _ref = e.options.length - 1, _ref1 = e.selectedIndex; _i >= _ref1; i = _i += -1) {
          if (e.options[i].selected) {
            return i;
          }
        }
        return -1;
      };

      PaletteController.prototype.allSelectionsAtTop = function() {
        var last, options;
        last = this.indexOfLastSelection(this.selected);
        options = _.toArray(this.selected.element.options);
        return _(options.slice(0, +last + 1 || 9e9)).all(function(o) {
          return o.selected;
        });
      };

      PaletteController.prototype.allSelectionsAtBottom = function() {
        var e, last, options;
        e = this.selected.element;
        last = e.selectedIndex;
        options = _.toArray(e.options);
        return _(options.slice(last)).all(function(o) {
          return o.selected;
        });
      };

      return PaletteController;

    })();
    return function(id) {
      return new PaletteController(id);
    };
  });

}).call(this);
