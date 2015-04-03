
# Simple test script for displaying channel content
# $Id: read.py 26 2002-06-24 21:13:26Z niko_schmuck $

from java.io import File
from java.lang import System
from org.apache.log4j import BasicConfigurator
from de.nava.informa.impl.basic import ChannelBuilder
from de.nava.informa.parsers import RSS_0_91_Parser
from de.nava.informa.parsers import RSS_1_0_Parser

import sys

if len(sys.argv) < 3:
    print "Usage: %s filename format(0.91|1.0)" % sys.argv[0]
    sys.exit(0)

filename = sys.argv[1]
print "Try to parse %s" % filename

# Set up a simple configuration that logs on the console.
BasicConfigurator.configure()

format = sys.argv[2]
if format == "0.91":
    parser = RSS_0_91_Parser()
else:
    parser = RSS_1_0_Parser()

builder = ChannelBuilder()
parser.setBuilder(builder)

# time has come to actually start working
start_time = System.currentTimeMillis()
channel = parser.parse(File(filename).toURL())
end_time = System.currentTimeMillis()
print "Parsing took %d milliseconds." % (end_time-start_time)

# display a bit
print
print "Channel: %s (%s)" % (channel.getTitle(), channel.getDescription())
print "         %s" % channel.getSite()
print

for item in channel.getItems():
    print "  - %s" % item.getTitle()
    print "    [Id: %s]" % item.getId()
    print "    link to %s" % item.getLink()
    if item.getDescription():
        print '    "%s ..."' % item.getDescription()[:50]
    print
