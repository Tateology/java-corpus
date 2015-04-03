
from java.net import URL
from java.util import Date

from de.nava.informa.impl.basic import Channel
from de.nava.informa.impl.basic import Item

from de.nava.informa.exporters import RSS_0_91_Exporter


# create demo channel
channel = Channel("The Great Demo Channel")
item = Item("Bugo", "All about it!", URL("http://nava.de/huhu2002"))
item.setFound(Date())
channel.addItem(item)

# try to write out to file
exp_file = "test-rss091.xml"
print "Start to export channel to", exp_file
exporter = RSS_0_91_Exporter(exp_file)
exporter.write(channel)
print "Exporting finished."
