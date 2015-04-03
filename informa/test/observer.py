"""
$Id: observer.py 46 2002-07-17 08:15:51Z niko_schmuck $
test the observer while new items pop up over a period
"""

from java.lang import Thread
from java.net import URL
from de.nava.informa.impl.basic import ChannelBuilder
from de.nava.informa.utils import ChannelRegistry
from de.nava.informa.utils import SimpleChannelObserver

# create channel registry
builder = ChannelBuilder()
reg = ChannelRegistry(builder)

# register channels (which should be updated every 15-35 minutes)
c = {}
c[1]=reg.addChannel(URL("http://www.newsforge.com/newsforge.rss"), 15*60, 1);
c[2]=reg.addChannel(URL("http://slashdot.org/slashdot.rdf"), 20*60, 1);
c[3]=reg.addChannel(URL("http://www.heise.de/newsticker/heise.rdf"), 25*60, 1);
c[4]=reg.addChannel(URL("http://xmlhack.com/rss10.php"), 30*60, 1)
c[5]=reg.addChannel(URL("http://freshmeat.net/backend/fm.rdf"), 35*60, 1);

# create a simple observer which watches out for new items
o = SimpleChannelObserver()
for idx in c.keys():
    c[idx].addObserver(o)

# sleep 4 hours (let the updating threads work)
Thread.currentThread().sleep(4 * 60 * 60 * 1000)

# the end.
print "--- finished observing"
for channel in reg.getChannels():
    print
    print "channel:", channel
    for item in channel.getItems():
      print "  -", item
