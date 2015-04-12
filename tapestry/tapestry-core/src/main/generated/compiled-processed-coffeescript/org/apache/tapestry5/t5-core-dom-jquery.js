(function() {
  define(["underscore", "./utils", "./events", "jquery"], function(_, utils, events, $) {
    var ElementWrapper, EventWrapper, RequestWrapper, ResponseWrapper, activeAjaxCount, adjustAjaxCount, ajaxRequest, convertContent, createElement, exports, onevent, scanner, scanners, wrapElement;
    convertContent = function(content) {
      if (_.isString(content)) {
        return content;
      }
      if (_.isElement(content)) {
        return content;
      }
      if (content instanceof ElementWrapper) {
        return content.$;
      }
      throw new Error("Provided value <" + content + "> is not valid as DOM element content.");
    };
    EventWrapper = (function() {
      function EventWrapper(event, memo) {
        var name, _i, _len, _ref;
        this.nativeEvent = event;
        this.memo = memo;
        _ref = ["type", "char", "key"];
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
          name = _ref[_i];
          this[name] = event[name];
        }
      }

      EventWrapper.prototype.stop = function() {
        this.nativeEvent.preventDefault();
        return this.nativeEvent.stopImmediatePropagation();
      };

      return EventWrapper;

    })();
    onevent = function(jqueryObject, eventNames, match, handler) {
      var wrapped;
      if (handler == null) {
        throw new Error("No event handler was provided.");
      }
      wrapped = function(jqueryEvent, memo) {
        var elementWrapper, eventWrapper, result;
        elementWrapper = new ElementWrapper($(jqueryEvent.target));
        eventWrapper = new EventWrapper(jqueryEvent, memo);
        result = handler.call(elementWrapper, eventWrapper, memo);
        if (result === false) {
          eventWrapper.stop();
        }
      };
      jqueryObject.on(eventNames, match, wrapped);
      return function() {
        return jqueryObject.off(eventNames, match, wrapped);
      };
    };
    ElementWrapper = (function() {
      function ElementWrapper(query) {
        this.$ = query;
        this.element = query[0];
      }

      ElementWrapper.prototype.toString = function() {
        var markup;
        markup = this.element.outerHTML;
        return "ElementWrapper[" + (markup.substring(0, (markup.indexOf(">")) + 1)) + "]";
      };

      ElementWrapper.prototype.hide = function() {
        this.$.hide();
        return this;
      };

      ElementWrapper.prototype.show = function() {
        this.$.show();
        return this;
      };

      ElementWrapper.prototype.css = function(name, value) {
        if (arguments.length === 1) {
          return this.$.css(name);
        }
        this.$.css(name, value);
        return this;
      };

      ElementWrapper.prototype.offset = function() {
        return this.$.offset();
      };

      ElementWrapper.prototype.remove = function() {
        this.$.detach();
        return this;
      };

      ElementWrapper.prototype.attr = function(name, value) {
        var attributeName, current;
        if (_.isObject(name)) {
          for (attributeName in name) {
            value = name[attributeName];
            this.attr(attributeName, value);
          }
          return this;
        }
        current = this.$.attr(name);
        if (arguments.length > 1) {
          if (value === null) {
            this.$.removeAttr(name);
          } else {
            this.$.attr(name, value);
          }
        }
        if (_.isUndefined(current)) {
          current = null;
        }
        return current;
      };

      ElementWrapper.prototype.focus = function() {
        this.$.focus();
        return this;
      };

      ElementWrapper.prototype.hasClass = function(name) {
        return this.$.hasClass(name);
      };

      ElementWrapper.prototype.removeClass = function(name) {
        this.$.removeClass(name);
        return this;
      };

      ElementWrapper.prototype.addClass = function(name) {
        this.$.addClass(name);
        return this;
      };

      ElementWrapper.prototype.update = function(content) {
        this.$.empty();
        if (content) {
          this.$.append(convertContent(content));
        }
        return this;
      };

      ElementWrapper.prototype.append = function(content) {
        this.$.append(convertContent(content));
        return this;
      };

      ElementWrapper.prototype.prepend = function(content) {
        this.$.prepend(convertContent(content));
        return this;
      };

      ElementWrapper.prototype.insertBefore = function(content) {
        this.$.before(convertContent(content));
        return this;
      };

      ElementWrapper.prototype.insertAfter = function(content) {
        this.$.after(convertContent(content));
        return this;
      };

      ElementWrapper.prototype.findFirst = function(selector) {
        var match;
        match = this.$.find(selector);
        if (match.length) {
          return new ElementWrapper(match.first());
        } else {
          return null;
        }
      };

      ElementWrapper.prototype.find = function(selector) {
        var i, matches, _i, _ref, _results;
        matches = this.$.find(selector);
        _results = [];
        for (i = _i = 0, _ref = matches.length; 0 <= _ref ? _i < _ref : _i > _ref; i = 0 <= _ref ? ++_i : --_i) {
          _results.push(new ElementWrapper(matches.eq(i)));
        }
        return _results;
      };

      ElementWrapper.prototype.findParent = function(selector) {
        var parents;
        parents = this.$.parents(selector);
        if (!parents.length) {
          return null;
        }
        return new ElementWrapper(parents.eq(0));
      };

      ElementWrapper.prototype.closest = function(selector) {
        var match;
        match = this.$.closest(selector);
        switch (false) {
          case match.length !== 0:
            return null;
          case match[0] !== this.element:
            return this;
          default:
            return new ElementWrapper(match);
        }
      };

      ElementWrapper.prototype.parent = function() {
        var parent;
        parent = this.$.parent();
        if (!parent.length) {
          return null;
        }
        return new ElementWrapper(parent);
      };

      ElementWrapper.prototype.children = function() {
        var children, i, _i, _ref, _results;
        children = this.$.children();
        _results = [];
        for (i = _i = 0, _ref = children.length; 0 <= _ref ? _i < _ref : _i > _ref; i = 0 <= _ref ? ++_i : --_i) {
          _results.push(new ElementWrapper(children.eq(i)));
        }
        return _results;
      };

      ElementWrapper.prototype.visible = function() {
        return this.$.css("display") !== "none";
      };

      ElementWrapper.prototype.deepVisible = function() {
        var cursor;
        cursor = this;
        while (cursor) {
          if (!cursor.visible()) {
            return false;
          }
          cursor = cursor.parent();
          if (cursor && cursor.element === document.body) {
            return true;
          }
        }
        return false;
      };

      ElementWrapper.prototype.trigger = function(eventName, memo) {
        var jqEvent;
        if (eventName == null) {
          throw new Error("Attempt to trigger event with null event name");
        }
        if (!((_.isNull(memo)) || (_.isObject(memo)) || (_.isUndefined(memo)))) {
          throw new Error("Event memo may be null or an object, but not a simple type.");
        }
        jqEvent = $.Event(eventName);
        this.$.trigger(jqEvent, memo);
        return !jqEvent.isImmediatePropagationStopped();
      };

      ElementWrapper.prototype.value = function(newValue) {
        var current;
        current = this.$.val();
        if (arguments.length > 0) {
          this.$.val(newValue);
        }
        return current;
      };

      ElementWrapper.prototype.checked = function() {
        return this.element.checked;
      };

      ElementWrapper.prototype.meta = function(name, value) {
        var current;
        current = this.$.data(name);
        if (arguments.length > 1) {
          this.$.data(name, value);
        }
        return current;
      };

      ElementWrapper.prototype.on = function(events, match, handler) {
        exports.on(this.element, events, match, handler);
        return this;
      };

      ElementWrapper.prototype.text = function() {
        return this.$.text();
      };

      return ElementWrapper;

    })();
    RequestWrapper = (function() {
      function RequestWrapper(jqxhr) {
        this.jqxhr = jqxhr;
      }

      RequestWrapper.prototype.abort = function() {
        return this.jqxhr.abort();
      };

      return RequestWrapper;

    })();
    ResponseWrapper = (function() {
      function ResponseWrapper(jqxhr, data) {
        this.jqxhr = jqxhr;
        this.status = jqxhr.status;
        this.statusText = jqxhr.statusText;
        this.json = data;
        this.text = jqxhr.responseText;
      }

      ResponseWrapper.prototype.header = function(name) {
        return this.jqxhr.getResponseHeader(name);
      };

      return ResponseWrapper;

    })();
    activeAjaxCount = 0;
    adjustAjaxCount = function(delta) {
      activeAjaxCount += delta;
      return exports.body.attr("data-ajax-active", activeAjaxCount > 0);
    };
    ajaxRequest = function(url, options) {
      var jqxhr, _ref;
      if (options == null) {
        options = {};
      }
      jqxhr = $.ajax({
        url: url,
        type: ((_ref = options.method) != null ? _ref.toUpperCase() : void 0) || "POST",
        contentType: options.contentType,
        traditional: true,
        data: options.data,
        error: function(jqXHR, textStatus, errorThrown) {
          var message, text;
          adjustAjaxCount(-1);
          if (textStatus === "abort") {
            return;
          }
          message = "Request to " + url + " failed with status " + textStatus;
          text = jqXHR.statusText;
          if (!_.isEmpty(text)) {
            message += " -- " + text;
          }
          message += ".";
          if (options.failure) {
            options.failure(new ResponseWrapper(jqXHR), message);
          } else {
            throw new Error(message);
          }
        },
        success: function(data, textStatus, jqXHR) {
          adjustAjaxCount(-1);
          options.success && options.success(new ResponseWrapper(jqXHR, data));
        }
      });
      adjustAjaxCount(+1);
      return new RequestWrapper(jqxhr);
    };
    scanners = null;
    scanner = function(selector, callback) {
      var scan;
      scan = function(root) {
        var el, _i, _len, _ref;
        _ref = root.find(selector);
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
          el = _ref[_i];
          callback(el);
        }
      };
      scan(exports.body);
      if (scanners === null) {
        scanners = [];
        exports.body.on(events.zone.didUpdate, function() {
          var f, _i, _len;
          for (_i = 0, _len = scanners.length; _i < _len; _i++) {
            f = scanners[_i];
            f(this);
          }
        });
      }
      scanners.push(scan);
    };
    exports = wrapElement = function(element) {
      if (_.isString(element)) {
        element = document.getElementById(element);
        if (!element) {
          return null;
        }
      } else {
        if (!element) {
          throw new Error("Attempt to wrap a null DOM element");
        }
      }
      return new ElementWrapper($(element));
    };
    createElement = function(elementName, attributes, body) {
      var element;
      if (_.isObject(elementName)) {
        body = attributes;
        attributes = elementName;
        elementName = null;
      }
      if (_.isString(attributes)) {
        body = attributes;
        attributes = null;
      }
      element = wrapElement(document.createElement(elementName || "div"));
      if (attributes) {
        element.attr(attributes);
      }
      if (body) {
        element.update(body);
      }
      return element;
    };
    _.extend(exports, {
      wrap: wrapElement,
      create: createElement,
      ajaxRequest: ajaxRequest,
      on: function(selector, events, match, handler) {
        var elements;
        if (handler == null) {
          handler = match;
          match = null;
        }
        elements = $(selector);
        return onevent(elements, events, match, handler);
      },
      onDocument: function(events, match, handler) {
        return exports.on(document, events, match, handler);
      },
      body: wrapElement(document.body),
      scanner: scanner
    });
    return exports;
  });

}).call(this);
