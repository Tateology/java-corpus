(function() {
  define(["underscore"], function(_) {
    var exports, extendURL, trim;
    trim = function(input) {
      if (String.prototype.trim) {
        return input.trim();
      } else {
        return input.replace(/^\s+/, '').replace(/\s+$/, '');
      }
    };
    extendURL = function(url, params) {
      var name, sep, value;
      sep = url.indexOf("?") >= 0 ? "&" : "?";
      for (name in params) {
        value = params[name];
        url = url + sep + name + "=" + value;
        sep = "&";
      }
      return url;
    };
    return exports = {
      trim: trim,
      extendURL: extendURL,
      startsWith: function(string, pattern) {
        return (string.indexOf(pattern)) === 0;
      },
      isBlank: function(input) {
        if (input === null) {
          return true;
        }
        if (_.isArray(input)) {
          return input.length === 0;
        }
        return (exports.trim(input)).length === 0;
      },
      split: function(str) {
        return _(str.split(" ")).reject(function(s) {
          return s === "";
        });
      }
    };
  });

}).call(this);
