# $Id: update-index.py 27 2002-06-25 08:18:23Z niko_schmuck $
# update an existing index (adding a new document).

from java.net import URL
from java.util import Date

# from de.nava.informa.impl.basic import ChannelBuilder
# from de.nava.informa.utils import ChannelRegistry
from de.nava.informa.impl.basic import Item
from de.nava.informa.search import ItemDocument

from org.apache.lucene.analysis.standard import StandardAnalyzer
from org.apache.lucene.index import IndexWriter


# update (3rd arg) index writer in directory (first arg)
start_time = Date()
writer = IndexWriter("index", StandardAnalyzer(), 0)

# create new (dummy) item
item = Item("Informa released", "blubb",
            URL("http://nava.de/news/2002/06/25"))
item.setFound(Date())
	    
# add new item to index
writer.addDocument(ItemDocument.makeDocument(item))

writer.optimize()
writer.close()
end_time = Date()

print "updating the index took %d milliseconds in total." \
      % (end_time.getTime() - start_time.getTime())
