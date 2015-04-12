(function() {
  define(["underscore", "./events", "./dom", "./utils", "./forms"], function(_, events, dom, utils) {
    var collectOptionValues, createHelpBlock, ensureFieldId, exports, findHelpBlocks, showValidationError;
    ensureFieldId = function(field) {
      var fieldId;
      fieldId = field.attr("id");
      if (!fieldId) {
        fieldId = _.uniqueId("field");
        field.attr("id", fieldId);
      }
      return fieldId;
    };
    findHelpBlocks = function(field) {
      var block, blocks, fieldId, group;
      fieldId = field.attr("id");
      if (fieldId) {
        blocks = dom.body.find("[data-error-block-for='" + fieldId + "']");
        if (blocks.length > 0) {
          return blocks;
        }
      } else {
        fieldId = ensureFieldId(field);
      }
      group = field.findParent(".form-group");
      if (!group) {
        return null;
      }
      block = group.findFirst("[data-presentation=error]");
      if (block) {
        block.attr("data-error-block-for", fieldId);
        return [block];
      }
      return null;
    };
    createHelpBlock = function(field) {
      var block, container, fieldId;
      fieldId = ensureFieldId(field);
      container = field.parent();
      block = dom.create("p", {
        "class": "help-block",
        "data-error-block-for": fieldId
      });
      if (container.hasClass("input-group")) {
        container.insertAfter(block);
      } else {
        field.insertAfter(block);
      }
      return block;
    };
    showValidationError = function(id, message) {
      return dom.wrap(id).trigger(events.field.showValidationError, {
        message: message
      });
    };
    collectOptionValues = function(wrapper) {
      return _.pluck(wrapper.element.options, "value");
    };
    dom.onDocument(events.field.inputValidation, function(event, formMemo) {
      var failure, fieldValue, memo, postEventTrigger;
      if (this.element.disabled || (!this.deepVisible())) {
        return;
      }
      failure = false;
      fieldValue = (this.attr("data-value-mode")) === "options" ? collectOptionValues(this) : this.value();
      memo = {
        value: fieldValue
      };
      postEventTrigger = (function(_this) {
        return function() {
          if (memo.error) {
            failure = true;
            if (_.isString(memo.error)) {
              return _this.trigger(events.field.showValidationError, {
                message: memo.error
              });
            }
          }
        };
      })(this);
      this.trigger(events.field.optional, memo);
      postEventTrigger();
      if (!(failure || (utils.isBlank(memo.value)))) {
        this.trigger(events.field.translate, memo);
        postEventTrigger();
        if (!failure) {
          if (_.isUndefined(memo.translated)) {
            memo.translated = memo.value;
          }
          this.trigger(events.field.validate, memo);
          postEventTrigger();
        }
      }
      if (failure) {
        formMemo.error = true;
      } else {
        this.trigger(events.field.clearValidationError);
      }
    });
    dom.onDocument(events.field.clearValidationError, function() {
      var block, blocks, group, _i, _len, _ref;
      blocks = exports.findHelpBlocks(this);
      _ref = blocks || [];
      for (_i = 0, _len = _ref.length; _i < _len; _i++) {
        block = _ref[_i];
        block.hide().update("");
        block.parent().removeClass("has-error");
      }
      group = this.findParent(".form-group");
      group && group.removeClass("has-error");
    });
    dom.onDocument(events.field.showValidationError, function(event, memo) {
      var block, blocks, container, group, _i, _len;
      blocks = exports.findHelpBlocks(this);
      if (!blocks) {
        blocks = [exports.createHelpBlock(this)];
      }
      for (_i = 0, _len = blocks.length; _i < _len; _i++) {
        block = blocks[_i];
        block.removeClass("invisible").show().update(memo.message);
        block.parent().addClass("has-error");
      }
      group = this.findParent(".form-group");
      container = group || this.parent().closest(":not(.input-group)");
      container.addClass("has-error");
    });
    return exports = {
      findHelpBlocks: findHelpBlocks,
      createHelpBlock: createHelpBlock,
      showValidationError: showValidationError
    };
  });

}).call(this);
