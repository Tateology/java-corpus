"""Shows how to access the channels via JDO in the database
and marshal them into a XML instance with the given mapping.
$Id: marshal.py 114 2002-10-20 15:39:03Z niko_schmuck $
"""

from java.io import FileWriter

from org.exolab.castor.jdo import JDO
from org.exolab.castor.mapping import Mapping
from org.exolab.castor.xml import Marshaller

# -- Define the JDO object
jdo = JDO()
jdo.setDatabaseName("jdoinforma")
jdo.setConfiguration("../../src/de/nava/informa/impl/jdo/database.xml")
db = jdo.getDatabase()

# -- Create a File to marshal to
writer = FileWriter("test-channels.xml")

# -- Load the mapping file
mapping = Mapping()
mapping.loadMapping("../../src/de/nava/informa/impl/jdo/mapping.xml")

# -- prepare XML marshalling
marshaller = Marshaller(writer)
marshaller.setMapping(mapping)

# -- write out all channels in the database
db.begin()
oql = db.getOQLQuery("SELECT c FROM de.nava.informa.impl.jdo.Channel c")
results = oql.execute()
while results.hasMore():
    marshaller.marshal(results.next())

db.commit()

db.close()
