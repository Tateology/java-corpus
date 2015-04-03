
# Test Castor XML marshalling with channel read in from file
# $Id: persist-channel.py 114 2002-10-20 15:39:03Z niko_schmuck $

from java.io import File
from java.io import FileWriter
from java.lang import System
from de.nava.informa.impl.basic import ChannelBuilder
from de.nava.informa.parsers import RSS_0_91_Parser
from de.nava.informa.parsers import RSS_1_0_Parser

from org.exolab.castor.mapping import Mapping
from org.exolab.castor.xml import Marshaller

import sys

if len(sys.argv) < 3:
    print "Usage: %s filename format(0.91|1.0)" % sys.argv[0]
    sys.exit(0)

filename = sys.argv[1]
print "Try to parse %s" % filename

# Create a Parser of specified format
format = sys.argv[2]
builder = ChannelBuilder()
if format == "0.91":
    parser = RSS_0_91_Parser(builder)
else:
    parser = RSS_1_0_Parser(builder)

# Read in Channel from File
channel = parser.parse(File(filename).toURL())    
    
# Create a File to marshal to
writer = FileWriter("test-persist-channel.xml")

# Load the mapping information from the file
mapping = Mapping()
mapping.loadMapping("../../src/de/nava/informa/impl/jdo/mapping.xml");

# Marshal the channel object
marshaller = Marshaller(writer)
marshaller.setMapping(mapping)
marshaller.marshal(channel)

# ---- Unmarshalling 

# Create a Reader to the file to unmarshal from
# reader = FileReader("test-persist-channel.xml")

# Unmarshal the channel object
# channel = Unmarshaller.unmarshal(Channel.class, reader);
