# $Id: search.py 28 2002-06-26 08:03:29Z niko_schmuck $
# search specified value in lucene index of news items.

import sys

from org.apache.lucene.analysis.standard import StandardAnalyzer
from org.apache.lucene.search import IndexSearcher
from org.apache.lucene.queryParser import QueryParser

searcher = IndexSearcher("index")
analyzer = StandardAnalyzer()

if len(sys.argv) < 2:
    print "Usage: %s query" % sys.argv[0]
    sys.exit(0)

q_str = sys.argv[1]
print "\nSearching for <%s>." % q_str

query = QueryParser.parse(q_str, "titledesc", analyzer)
print "query:", query
hits = searcher.search(query)

print "%d total matching item(s).\n" % hits.length()

for i in xrange(hits.length()):
    doc = hits.doc(i)
    score = hits.score(i)
    print "- [Id: %s] Score %.4f" % (doc.get("id"), score)
    print "  Title: %s" % doc.get("title")
    print "  Description: %s" % doc.get("description")
    print

searcher.close()
print
