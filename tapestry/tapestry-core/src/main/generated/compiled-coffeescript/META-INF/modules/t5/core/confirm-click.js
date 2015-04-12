(function() {
  define(["jquery", "./events", "./dom", "bootstrap/modal"], function($, events, dom) {
    var runDialog;
    runDialog = function(options) {
      var $dialog, confirmed, content;
      confirmed = false;
      content = "<div class=\"modal fade\" role=\"dialog\">\n  <div class=\"modal-dialog\">\n    <div class=\"modal-content\">\n      <div class=\"modal-header\">\n        <a class=\"close\" data-dismiss=\"modal\">&times;</a>\n        <h3>" + (options.title || "Confirm") + "</h3>\n      </div>\n      <div class=\"modal-body\">" + options.message + "</div>\n      <div class=\"modal-footer\">\n        <button class=\"btn " + (options.okClass || "btn-warning") + "\" data-dismiss=\"modal\">" + (options.okLabel || "OK") + "</button>\n        <button class=\"btn btn-default\" data-dismiss=\"modal\">" + (options.cancelLabel || "Cancel") + "</button>\n      </div>\n    </div>\n  </div>\n</div>";
      $dialog = $(content);
      $dialog.on("click", ".modal-footer button:first", function() {
        confirmed = true;
      });
      $dialog.modal().on("hidden.bs.modal", function() {
        $dialog.remove();
        if (confirmed) {
          return options.ok();
        }
      });
      return $dialog.appendTo($("body"));
    };
    $("body").on("click", "[data-confirm-message]:not(.disabled)", function() {
      var $this;
      $this = $(this);
      if (($this.attr("data-confirm-state")) === "confirmed") {
        $this.attr("data-confirm-state", null);
        return;
      }
      runDialog({
        title: $this.attr("data-confirm-title"),
        message: $this.attr("data-confirm-message"),
        okLabel: $this.attr("data-confirm-label"),
        ok: function() {
          $this.attr("data-confirm-state", "confirmed");
          return $this.click();
        }
      });
      return false;
    });
    dom.onDocument("click", "a[data-confirm-message]:not(.disabled)", function() {
      if (this.attr("data-update-zone")) {
        return;
      }
      window.location.href = this.attr("href");
      return false;
    });
    return {
      runDialog: runDialog
    };
  });

}).call(this);
