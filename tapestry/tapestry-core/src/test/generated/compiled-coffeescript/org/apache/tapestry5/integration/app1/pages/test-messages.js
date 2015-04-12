(function() {
  require(["t5/core/messages", "underscore"], function(messages, _) {
    var missing;
    module("t5/core/messages");
    missing = function(key) {
      return (_.indexOf(messages.keys(), key)) === -1;
    };
    test("access known key", function() {
      return equal(messages("client-accessible"), "Client Accessible");
    });
    test("unknown messages key", function() {
      return equal(messages("gnip-gnop"), "[[Missing Key: 'gnip-gnop']]");
    });
    test("messages values with '%' are not client accessible", function() {
      return ok(missing("not-visible"));
    });
    return test("messages prefixed with 'private-' are not client accessible", function() {
      return ok(missing("private-is-not-visible"));
    });
  });

}).call(this);
