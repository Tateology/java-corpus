from jarray import array

from java.lang import Object
from java.net import URL
from java.util import Date

from de.nava.informa.impl.basic import Channel
from de.nava.informa.impl.basic import Item


def addItem(channel, title, descr, link):
    item = Item(title, descr, URL(link))
    item.setFound(Date())
    channel.addItem(item)
    


# create demo channel
channel = Channel("The Great Demo Channel")
addItem(channel, "Bugo", "All about it!", "http://nava.de/huhu2002")
addItem(channel, "Python", "Instant Python updated", "http://nava.de/I9")

# convert to array
items = channel.getItems().toArray()
print "items", items

new_item = Item("NN", "Fresh", URL("http://new.com"))

new_items = []
new_items.append(new_item)
for item in items:
    new_items.append(item)

items = array(new_items, Object)
print "now items is", items

