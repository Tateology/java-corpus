(function() {
  require(["t5/core/utils"], function(utils) {
    module("t5/core/utils");
    test("startsWith, positive case", function() {
      ok(utils.startsWith('foobar', 'foo'));
      return ok(utils.startsWith('foobarfoo', 'foo'));
    });
    return test("startsWith, negative case", function() {
      return equal(utils.startsWith('barfoo', 'foo'), false);
    });
  });

}).call(this);
