

from java.net import URL
from java.lang import System
from java.util import Date

from org.jdom.output import XMLOutputter
from org.jdom.contrib.beans import BeanMapper

from de.nava.informa.impl.basic import Item



# create test bean
item = Item("Bugo", "All about it!", URL("http://nava.de/huhu2002"))
item.setFound(Date())

# bean -> jdom
mapper = BeanMapper()
mapper.setBeanPackage("de.nava.informa.impl.basic")

# problems with mapping an URL object
mapper.ignoreProperty("link")

doc = mapper.toDocument(item)
o = XMLOutputter("  ", 1)
o.output(doc, System.out)

# may throw BeanMapperException
