(function() {
  require(["t5/core/validation"], function(v) {
    var parse;
    parse = v.parseNumber;
    module("t5/core/validation");
    test("basic numeric parsing", function() {
      strictEqual(parse("-1.23"), -1.23);
      strictEqual(parse("200", true), 200);
      strictEqual(parse("1,000"), 1000);
      return strictEqual(parse(".23"), 0.23);
    });
    test("minus not allowed in middle", function() {
      return throws((function() {
        return parse("1-1");
      }), /not numeric/);
    });
    test("input is trimmed", function() {
      return strictEqual(parse(" 123,456.78 "), 123456.78);
    });
    test("no grouping seperator after decimal point", function() {
      return throws((function() {
        return parse(".2,3");
      }), /not numeric/);
    });
    test("consecutive grouping seperator not allowed", function() {
      return throws((function() {
        return parse("2,,3");
      }), /not numeric/);
    });
    return test("decimal not allowed for integer", function() {
      return throws((function() {
        return parse("3.14", true);
      }), /not an integer/);
    });
  });

}).call(this);
