(function() {
  define(["./dom", "./ajax", "./zone"], function(dom, ajax) {
    var EXPANDED, LOADED, LOADING, NODE_ID, SELECTED, SELECTOR, TREE, clickHandler, loadChildren, send, toggle, toggleSelection;
    TREE = "[data-component-type='core/Tree']";
    NODE_ID = "data-node-id";
    SELECTOR = "" + TREE + " [" + NODE_ID + "]";
    LOADING = "tree-children-loading";
    LOADED = "tree-children-loaded";
    EXPANDED = "tree-expanded";
    SELECTED = "selected-leaf-node";
    send = function(node, action, success) {
      var container, url;
      container = node.findParent(TREE);
      url = container.attr("data-tree-action-url");
      return ajax(url, {
        data: {
          "t:action": action,
          "t:nodeid": node.attr(NODE_ID)
        },
        success: success
      });
    };
    loadChildren = function(node) {
      if (node.meta(LOADING)) {
        return;
      }
      node.meta(LOADING, true);
      node.addClass("empty-node");
      node.update("<span class='tree-ajax-wait'/>");
      return send(node, "expand", function(response) {
        var label;
        node.update("").addClass(EXPANDED).removeClass("empty-node");
        label = node.findParent("li").findFirst(".tree-label");
        label.insertAfter(response.json.content);
        node.meta(LOADING, false);
        return node.meta(LOADED, true);
      });
    };
    toggle = function(node) {
      var sublist;
      sublist = node.findParent("li").findFirst("ul");
      if (node.hasClass(EXPANDED)) {
        node.removeClass(EXPANDED);
        sublist.hide();
        send(node, "markCollapsed");
        return;
      }
      node.addClass(EXPANDED);
      sublist.show();
      return send(node, "markExpanded");
    };
    clickHandler = function() {
      if ((this.parent().hasClass("leaf-node")) || (this.hasClass("empty-node"))) {
        return false;
      }
      if ((this.meta(LOADED)) || (this.hasClass(EXPANDED))) {
        toggle(this);
      } else {
        loadChildren(this);
      }
      return false;
    };
    toggleSelection = function() {
      var node, selected;
      selected = this.hasClass(SELECTED);
      node = this.findParent("li").findFirst("[" + NODE_ID + "]");
      if (selected) {
        this.removeClass(SELECTED);
        send(node, "deselect");
      } else {
        this.addClass(SELECTED);
        send(node, "select");
      }
      return false;
    };
    dom.onDocument("click", SELECTOR, clickHandler);
    dom.onDocument("click", "" + TREE + "[data-tree-node-selection-enabled] LI.leaf-node > .tree-label", toggleSelection);
    return null;
  });

}).call(this);
