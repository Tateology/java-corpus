(function() {
  define(["underscore", "./dom", "./events", "./utils", "./messages", "./fields"], function(_, dom, events, utils, messages) {
    var REGEXP_META, decimal, grouping, minus, parseNumber, translate;
    REGEXP_META = "t5:regular-expression";
    minus = messages("decimal-symbols.minus");
    grouping = messages("decimal-symbols.group");
    decimal = messages("decimal-symbols.decimal");
    parseNumber = function(input, isInteger) {
      var accept, acceptDigitOnly, any, canonical, ch, decimalPortion, leadingMinus, mustBeDigit, state, _i, _len, _ref;
      canonical = "";
      accept = function(ch) {
        return canonical += ch;
      };
      acceptDigitOnly = function(ch) {
        if (ch < "0" || ch > "9") {
          throw new Error(messages("core-input-not-numeric"));
        }
        accept(ch);
      };
      mustBeDigit = function(ch) {
        acceptDigitOnly(ch);
        return any;
      };
      decimalPortion = function(ch) {
        acceptDigitOnly(ch);
        return decimalPortion;
      };
      any = function(ch) {
        switch (ch) {
          case grouping:
            return mustBeDigit;
          case decimal:
            if (isInteger) {
              throw new Error(messages("core-input-not-integer"));
            }
            accept(".");
            return decimalPortion;
          default:
            return mustBeDigit(ch);
        }
      };
      leadingMinus = function(ch) {
        if (ch === minus) {
          accept("-");
          return mustBeDigit;
        } else {
          return any(ch);
        }
      };
      state = leadingMinus;
      _ref = utils.trim(input);
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        ch = _ref[_i];
        state = state(ch);
      }
      return Number(canonical);
    };
    translate = function(field, memo, isInteger) {
      var e, result;
      try {
        result = parseNumber(memo.value, isInteger);
        if (_.isNaN(result)) {
          throw messages("core-input-not-numeric");
        }
        return memo.translated = result;
      } catch (_error) {
        e = _error;
        memo.error = (field.attr("data-translation-message")) || e.message || "ERROR";
        return false;
      }
    };
    dom.onDocument(events.field.optional, "[data-optionality=required]", function(event, memo) {
      if (utils.isBlank(memo.value)) {
        return memo.error = (this.attr("data-required-message")) || "REQUIRED";
      }
    });
    dom.onDocument(events.field.translate, "[data-translation=numeric]", function(event, memo) {
      return translate(this, memo, false);
    });
    dom.onDocument(events.field.translate, "[data-translation=integer]", function(event, memo) {
      return translate(this, memo, true);
    });
    dom.onDocument(events.field.validate, "[data-validate-min-length]", function(event, memo) {
      var min;
      min = parseInt(this.attr("data-validate-min-length"));
      if (memo.translated.length < min) {
        memo.error = (this.attr("data-min-length-message")) || "TOO SHORT";
        return false;
      }
    });
    dom.onDocument(events.field.validate, "[data-validate-max-length]", function(event, memo) {
      var max;
      max = parseInt(this.attr("data-validate-max-length"));
      if (memo.translated.length > max) {
        memo.error = (this.attr("data-max-length-message")) || "TOO LONG";
        return false;
      }
    });
    dom.onDocument(events.field.validate, "[data-validate-max]", function(event, memo) {
      var max;
      max = parseInt(this.attr("data-validate-max"));
      if (memo.translated > max) {
        memo.error = (this.attr("data-max-message")) || "TOO LARGE";
        return false;
      }
    });
    dom.onDocument(events.field.validate, "[data-validate-min]", function(event, memo) {
      var min;
      min = parseInt(this.attr("data-validate-min"));
      if (memo.translated < min) {
        memo.error = (this.attr("data-min-message")) || "TOO SMALL";
        return false;
      }
    });
    dom.onDocument(events.field.validate, "[data-validate-regexp]", function(event, memo) {
      var re;
      re = this.meta(REGEXP_META);
      if (!re) {
        re = new RegExp(this.attr("data-validate-regexp"));
        this.meta(REGEXP_META, re);
      }
      if (!re.test(memo.translated)) {
        memo.error = (this.attr("data-regexp-message")) || "INVALID";
        return false;
      }
    });
    return {
      parseNumber: parseNumber
    };
  });

}).call(this);
