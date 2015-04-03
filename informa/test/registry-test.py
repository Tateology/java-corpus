
from java.lang import Thread
from java.net import URL
from java.util import Date
from de.nava.informa.impl.basic import ChannelBuilder
from de.nava.informa.utils import ChannelRegistry

builder = ChannelBuilder()
reg = ChannelRegistry(builder)

# url_xmlhack = "http://xmlhack.com/rss10.php"
# url_xmlhack = URL("http://localhost/rss/xmlhack-1.0.xml")
url_xmlhack = URL("http://localhost/rss/xmlhack-0.91.xml")
# update channel which should be updated every 30 seconds
reg.addChannel(url_xmlhack, 30, 1)
print "first time parsed in at ", Date()
for channel in reg.getChannels():
    print "channel:", channel
    for item in channel.getItems():
	print "  -", item

# sleep 100 seconds
Thread.currentThread().sleep(100 * 1000)

print "--- after updating"
for channel in reg.getChannels():
    print "channel:", channel
    for item in channel.getItems():
	print "  -", item

# offically deregister
# reg.removeChannel(channel)
