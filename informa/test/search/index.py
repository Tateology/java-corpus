# $Id: index.py 26 2002-06-24 21:13:26Z niko_schmuck $
# create an index for the lucene engine to perform search queries with.

from java.io import File
from java.util import Date

from de.nava.informa.impl.basic import ChannelBuilder
from de.nava.informa.utils import ChannelRegistry
from de.nava.informa.search import ItemDocument

from org.apache.lucene.analysis.standard import StandardAnalyzer
from org.apache.lucene.index import IndexWriter


# create channel registry
builder = ChannelBuilder()
reg = ChannelRegistry(builder)

# register some channels
chfiles = ('data/heise.rdf',
           'data/slashdot.rdf',
           'data/xmlhack-0.91.xml',
           'data/xmlhack-1.0.xml')
for filename in chfiles:
  reg.addChannel(File(filename).toURL(), 60, 0)

# create (3rd arg) index writer in directory (first arg)
start_time = Date()
writer = IndexWriter("index", StandardAnalyzer(), 1)

# loop over all channels and their items
for channel in reg.getChannels():
    print
    print "channel:", channel
    for item in channel.getItems():
      print "  add %s to index" % item
      writer.addDocument(ItemDocument.makeDocument(item))

writer.optimize()
writer.close()
end_time = Date()

print "building the index took %d milliseconds in total." \
      % (end_time.getTime() - start_time.getTime())
