(function() {
  define(["./dom", "underscore"], function(dom, _) {
    var clear, container, create, iframe, iframeDocument, write;
    container = null;
    iframe = null;
    iframeDocument = null;
    write = function(content) {
      iframeDocument.open();
      iframeDocument.write(content);
      return iframeDocument.close();
    };
    clear = function() {
      write("");
      container.hide();
      return false;
    };
    create = function() {
      if (container) {
        return;
      }
      container = dom.create({
        "class": "exception-container"
      }, "<iframe> </iframe>\n<div>\n  <button class=\"pull-right btn btn-primary\">\n    <i class=\"icon-remove icon-white\"></i>\n    Close\n  </button>\n</div>");
      dom.body.append(container.hide());
      iframe = (container.findFirst("iframe")).element;
      iframeDocument = iframe.contentWindow || iframe.contentDocument;
      if (iframeDocument.document) {
        iframeDocument = iframeDocument.document;
      }
      return container.on("click", "button", clear);
    };
    return function(exceptionContent) {
      create();
      write(exceptionContent);
      return container.show();
    };
  });

}).call(this);
