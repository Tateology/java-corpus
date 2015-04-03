
from org.jdom.input import SAXBuilder
from org.jdom import Namespace

import sys

if len(sys.argv) < 2:
    print "Usage: %s filename" % sys.argv[0]
    sys.exit(0)
    
filename = sys.argv[1]

builder = SAXBuilder(0)
print "Read in document from", filename
doc = builder.build(filename)

root = doc.getRootElement()
# print "root", root
print "Root element name: %s, namespace prefix: %s." \
      % (root.getName(), root.getNamespacePrefix())

# --- retrieve default namespace from root element
ns_root = root.getNamespace()
ns_root_others = root.getAdditionalNamespaces()
# print "additional namespaces", ns_root_others

dns = None
for ns in ns_root_others:
    if ns.getPrefix() == "":
        print "default namespace", ns.getURI()
        dns = ns
    else:
        print "namespace", ns.getPrefix(), "->", ns.getURI()

# --- retrieve 1 channel element
if dns == None:
    # --- old way:
    # dns = Namespace.getNamespace("", "http://purl.org/rss/1.0/")
    # does not work (retrieve anything) when leaving out the URI
    # print "No default namespace found, using self constructed one."
    # --- using no namespace
    channel = root.getChild("channel")
else:
    channel = root.getChild("channel", dns)
    
print "channel", channel

# --- retrieve n item elements
if dns == None:
    items = channel.getChildren("item")
else:
    items = root.getChildren("item", dns)
    
print "Found %i item(s)." % items.size()
for item in items:
    if dns == None:
        title = item.getChild("title").getTextTrim()
    else:
        title = item.getChild("title", dns).getTextTrim()    
    print "  *", title
