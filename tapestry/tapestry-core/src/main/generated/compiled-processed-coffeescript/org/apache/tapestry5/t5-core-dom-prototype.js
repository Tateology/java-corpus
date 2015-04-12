(function() {
  define(["underscore", "./utils", "./events", "jquery"], function(_, utils, events) {
    var $, ElementWrapper, EventWrapper, RequestWrapper, ResponseWrapper, activeAjaxCount, adjustAjaxCount, ajaxRequest, convertContent, createElement, exports, fireNativeEvent, onevent, parseSelectorToElements, scanner, scanners, wrapElement;
    $ = window.$;
    fireNativeEvent = function(element, eventName) {
      var event;
      if (document.createEventObject) {
        event = document.createEventObject();
        return element.fireEvent("on" + eventName, event);
      }
      event = document.createEvent("HTMLEvents");
      event.initEvent(eventName, true, true);
      element.dispatchEvent(event);
      return !event.defaultPrevented;
    };
    parseSelectorToElements = function(selector) {
      if (_.isString(selector)) {
        return $$(selector);
      }
      if (_.isArray(selector)) {
        return selector;
      }
      return [selector];
    };
    convertContent = function(content) {
      if (_.isString(content)) {
        return content;
      }
      if (_.isElement(content)) {
        return content;
      }
      if (content instanceof ElementWrapper) {
        return content.element;
      }
      throw new Error("Provided value <" + content + "> is not valid as DOM element content.");
    };
    EventWrapper = (function() {
      function EventWrapper(event) {
        var name, _i, _len, _ref;
        this.nativeEvent = event;
        this.memo = event.memo;
        _ref = ["type", "char", "key"];
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
          name = _ref[_i];
          this[name] = event[name];
        }
      }

      EventWrapper.prototype.stop = function() {
        return this.nativeEvent.stop();
      };

      return EventWrapper;

    })();
    onevent = function(elements, eventNames, match, handler) {
      var element, eventHandlers, eventName, wrapped, _i, _j, _len, _len1;
      if (handler == null) {
        throw new Error("No event handler was provided.");
      }
      wrapped = function(prototypeEvent) {
        var elementWrapper, eventWrapper, result;
        elementWrapper = new ElementWrapper(prototypeEvent.findElement());
        eventWrapper = new EventWrapper(prototypeEvent);
        result = prototypeEvent.stopped ? false : handler.call(elementWrapper, eventWrapper, eventWrapper.memo);
        if (result === false) {
          prototypeEvent.stop();
        }
      };
      eventHandlers = [];
      for (_i = 0, _len = elements.length; _i < _len; _i++) {
        element = elements[_i];
        for (_j = 0, _len1 = eventNames.length; _j < _len1; _j++) {
          eventName = eventNames[_j];
          eventHandlers.push(Event.on(element, eventName, match, wrapped));
        }
      }
      return function() {
        var eventHandler, _k, _len2, _results;
        _results = [];
        for (_k = 0, _len2 = eventHandlers.length; _k < _len2; _k++) {
          eventHandler = eventHandlers[_k];
          _results.push(eventHandler.stop());
        }
        return _results;
      };
    };
    ElementWrapper = (function() {
      function ElementWrapper(element) {
        this.element = element;
      }

      ElementWrapper.prototype.toString = function() {
        var markup;
        markup = this.element.outerHTML;
        return "ElementWrapper[" + (markup.substring(0, (markup.indexOf(">")) + 1)) + "]";
      };

      ElementWrapper.prototype.hide = function() {
        this.element.hide();
        return this;
      };

      ElementWrapper.prototype.show = function() {
        this.element.show();
        return this;
      };

      ElementWrapper.prototype.css = function(name, value) {
        if (arguments.length === 1) {
          return this.element.getStyle(name);
        }
        this.element.setStyle({
          name: value
        });
        return this;
      };

      ElementWrapper.prototype.offset = function() {
        return this.element.viewportOffset();
      };

      ElementWrapper.prototype.remove = function() {
        this.element.remove();
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
        current = this.element.readAttribute(name);
        if (arguments.length > 1) {
          this.element.writeAttribute(name, value === void 0 ? null : value);
        }
        return current;
      };

      ElementWrapper.prototype.focus = function() {
        this.element.focus();
        return this;
      };

      ElementWrapper.prototype.hasClass = function(name) {
        return this.element.hasClassName(name);
      };

      ElementWrapper.prototype.removeClass = function(name) {
        this.element.removeClassName(name);
        return this;
      };

      ElementWrapper.prototype.addClass = function(name) {
        this.element.addClassName(name);
        return this;
      };

      ElementWrapper.prototype.update = function(content) {
        this.element.update(content && convertContent(content));
        return this;
      };

      ElementWrapper.prototype.append = function(content) {
        this.element.insert({
          bottom: convertContent(content)
        });
        return this;
      };

      ElementWrapper.prototype.prepend = function(content) {
        this.element.insert({
          top: convertContent(content)
        });
        return this;
      };

      ElementWrapper.prototype.insertBefore = function(content) {
        this.element.insert({
          before: convertContent(content)
        });
        return this;
      };

      ElementWrapper.prototype.insertAfter = function(content) {
        this.element.insert({
          after: convertContent(content)
        });
        return this;
      };

      ElementWrapper.prototype.findFirst = function(selector) {
        var match;
        match = this.element.down(selector);
        if (match) {
          return new ElementWrapper(match);
        } else {
          return null;
        }
      };

      ElementWrapper.prototype.find = function(selector) {
        var e, matches, _i, _len, _results;
        matches = this.element.select(selector);
        _results = [];
        for (_i = 0, _len = matches.length; _i < _len; _i++) {
          e = matches[_i];
          _results.push(new ElementWrapper(e));
        }
        return _results;
      };

      ElementWrapper.prototype.findParent = function(selector) {
        var parent;
        parent = this.element.up(selector);
        if (!parent) {
          return null;
        }
        return new ElementWrapper(parent);
      };

      ElementWrapper.prototype.closest = function(selector) {
        if (this.element.match(selector)) {
          return this;
        }
        return this.findParent(selector);
      };

      ElementWrapper.prototype.parent = function() {
        var parent;
        parent = this.element.parentNode;
        if (!parent) {
          return null;
        }
        return new ElementWrapper(parent);
      };

      ElementWrapper.prototype.children = function() {
        var e, _i, _len, _ref, _results;
        _ref = this.element.childElements();
        _results = [];
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
          e = _ref[_i];
          _results.push(new ElementWrapper(e));
        }
        return _results;
      };

      ElementWrapper.prototype.visible = function() {
        return this.element.visible();
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
        var event;
        if (eventName == null) {
          throw new Error("Attempt to trigger event with null event name");
        }
        if (!((_.isNull(memo)) || (_.isObject(memo)) || (_.isUndefined(memo)))) {
          throw new Error("Event memo may be null or an object, but not a simple type.");
        }
        if ((eventName.indexOf(':')) > 0) {
          event = this.element.fire(eventName, memo);
          return !event.defaultPrevented;
        }
        if (memo) {
          throw new Error("Memo must be null when triggering a native event");
        }
        return fireNativeEvent(this.element, eventName);
      };

      ElementWrapper.prototype.value = function(newValue) {
        var current;
        current = this.element.getValue();
        if (arguments.length > 0) {
          this.element.setValue(newValue);
        }
        return current;
      };

      ElementWrapper.prototype.checked = function() {
        return this.element.checked;
      };

      ElementWrapper.prototype.meta = function(name, value) {
        var current;
        current = this.element.retrieve(name);
        if (arguments.length > 1) {
          this.element.store(name, value);
        }
        return current;
      };

      ElementWrapper.prototype.on = function(events, match, handler) {
        exports.on(this.element, events, match, handler);
        return this;
      };

      ElementWrapper.prototype.text = function() {
        return this.element.textContent || this.element.innerText;
      };

      return ElementWrapper;

    })();
    RequestWrapper = (function() {
      function RequestWrapper(req) {
        this.req = req;
      }

      RequestWrapper.prototype.abort = function() {
        throw "Cannot abort Ajax request when using Prototype.";
      };

      return RequestWrapper;

    })();
    ResponseWrapper = (function() {
      function ResponseWrapper(res) {
        this.res = res;
        this.status = res.status;
        this.statusText = res.statusText;
        this.json = res.responseJSON;
        this.text = res.responseText;
      }

      ResponseWrapper.prototype.header = function(name) {
        return this.res.getHeader(name);
      };

      return ResponseWrapper;

    })();
    activeAjaxCount = 0;
    adjustAjaxCount = function(delta) {
      activeAjaxCount += delta;
      return exports.body.attr("data-ajax-active", activeAjaxCount > 0);
    };
    ajaxRequest = function(url, options) {
      var finalOptions;
      if (options == null) {
        options = {};
      }
      finalOptions = {
        method: options.method || "post",
        contentType: options.contentType || "application/x-www-form-urlencoded",
        parameters: options.data,
        onException: function(ajaxRequest, exception) {
          adjustAjaxCount(-1);
          if (options.exception) {
            options.exception(exception);
          } else {
            throw exception;
          }
        },
        onFailure: function(response) {
          var message, text;
          adjustAjaxCount(-1);
          message = "Request to " + url + " failed with status " + (response.getStatus());
          text = response.getStatusText();
          if (!_.isEmpty(text)) {
            message += " -- " + text;
          }
          message += ".";
          if (options.failure) {
            options.failure(new ResponseWrapper(response), message);
          } else {
            throw new Error(message);
          }
        },
        onSuccess: function(response) {
          adjustAjaxCount(-1);
          if ((!response.getStatus()) || (!response.request.success())) {
            finalOptions.onFailure(new ResponseWrapper(response));
            return;
          }
          options.success && options.success(new ResponseWrapper(response));
        }
      };
      adjustAjaxCount(+1);
      return new RequestWrapper(new Ajax.Request(url, finalOptions));
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
        element = $(element);
        if (!element) {
          return null;
        }
      } else {
        if (!element) {
          throw new Error("Attempt to wrap a null DOM element");
        }
      }
      return new ElementWrapper(element);
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
        elements = parseSelectorToElements(selector);
        events = utils.split(events);
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
