
from java.net import URL
from java.util import Date

from de.nava.informa.impl.jdo import ChannelGroup
from de.nava.informa.impl.jdo import Channel
from de.nava.informa.impl.jdo import Item

from org.exolab.castor.jdo import JDO

# -- Define the JDO object
jdo = JDO()
jdo.setDatabaseName("jdoinforma")
jdo.setConfiguration("../../src/de/nava/informa/impl/jdo/database.xml")
# jdo.setClassLoader( self.getClass().getClassLoader() )

# -- Obtain a new database
db = jdo.getDatabase()

# -- Begin a transaction
db.begin()

# -- Create a new Object and Make it persistent
print "creating channel"

group = ChannelGroup("Default")

channel = Channel()
channel.setTitle("ChannelOne")
channel.setLocation("http://nava.de")
channel.setDescription("Test description for a channel.")
group.add(channel)

itemA = Item()
itemA.setTitle("Bugo")
itemA.setDescription("All about it!")
itemA.setLink(URL("http://nava.de/huhu2002"))
itemA.setFound(Date())
channel.addItem(itemA)

db.create( group )
print "persisted", group

# because there is no 'depends' declared
db.create( channel )
print "persisted", channel

channel_id = channel.getId()

# -- Commit the transaction
db.commit()

# -- Look up
db.begin()
print "Try to lookup channel with id", channel_id
oql = db.getOQLQuery("SELECT c FROM de.nava.informa.impl.jdo.Channel c" \
                     "         WHERE id = $1")
oql.bind( channel_id )
results = oql.execute()
if results.hasMore():
    r = results.next()
    print "Retrieved with OQL:", r
else:
    print "Nothing found :-("
db.commit()

# -- Close the database
db.close()
