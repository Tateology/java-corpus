"""
$Id: minigui.py 137 2003-01-15 00:17:47Z niko_schmuck $
Experiments how news items could be shown in a Swing table.
"""

# TODO:
#  - integrate update mechanism
#    o if new item in channel is found mark tree node in bold
#  - allow item description to flow to more than one line
#  - display channel image on right side in channel panel
#    (local cache mechanism for images)
#  - fix problem when selecting root tree node
#    ca. line 307, in valueChanged AttributeError: getChannel
#  - on double-click on item open link in external browser
#  - sort order should be reverse of the item
#
# (may wait)
#  - set icon for application
#  - re-work alignment of components in channel info panel
#  - display number of read / un-read items per channel
#    (in node title of channel tree)

import sys
from jarray import array

from javax.swing import JFrame
from javax.swing import JLabel
from javax.swing import JPanel
from javax.swing import JScrollPane
from javax.swing import JSplitPane
from javax.swing import JTable
from javax.swing import JTextField
from javax.swing import JTextArea
from javax.swing import JTree
from javax.swing.border import TitledBorder
from javax.swing.event import TreeSelectionListener
from javax.swing.table import AbstractTableModel
from javax.swing.table import TableCellRenderer
from javax.swing.tree import DefaultTreeModel
from javax.swing.tree import DefaultMutableTreeNode

from java.awt import BorderLayout
from java.awt import GridBagLayout
from java.awt import GridBagConstraints
from java.awt import Dimension
from java.awt import Insets
from java.awt.event import WindowAdapter

from java.lang import String
from java.lang import Object
from java.lang import System
from java.lang import Thread
from java.io import FileOutputStream
from java.io import ObjectOutputStream
from java.net import URL
from java.util import ArrayList
from java.util import Arrays
from java.util import Date
from java.text import SimpleDateFormat

from de.nava.informa.core import ChannelObserverIF
from de.nava.informa.impl.basic import ChannelBuilder
from de.nava.informa.utils import ChannelRegistry
from de.nava.informa.utils import ItemComparator
from de.nava.informa.utils import SimpleChannelObserver


# ------------------------------------------------------------
class ItemModel(AbstractTableModel):

    dateFmt = SimpleDateFormat("dd.MM.yyyy, HH:mm:ss")
    
    def __init__(self, channel):
        # self.headers = ("Title", "Description", "Found",  "Link")
        self.headers = ("Title", "Description", "Found")
        if channel != None:
            self.setActiveChannel(channel)
        else:
            self.channel = None
            self.items = []

    def setActiveChannel(self, channel):
        self.channel = channel
        # convert from List to Array
        items = self.channel.getItems().toArray()
        # sort news items
        Arrays.sort(items, ItemComparator(1))
        self.items = items
        self.fireTableDataChanged()

    def getActiveChannel(self):
        return self.channel

    def addItem(self, new_item):
        new_items = []
        new_items.append(new_item)
        for item in self.items:
            new_items.append(item)
        self.items = array(new_items, Object)
        # TODO: figure out if this can be optimized
        #       instead of refreshing the whole table
        self.fireTableDataChanged()

    def getRowCount(self):
        return len(self.items)
        
    def getColumnCount(self):
        return len(self.headers)
    
    def getColumnName(self, col):
        return self.headers[col]

    def getColumnClass(self, col):
        if col == 1:
            return MultiLine
        else:
            return String

    def getValueAt(self, row, col):
        if col == 0:
            return self.items[row].getTitle()
        elif col == 1:
            return MultiLine(self.items[row].getDescription())
        elif col == 2:
            return self.dateFmt.format(self.items[row].getFound())
        elif col == 3:
            return self.items[row].getLink()
        else:
            print "Invalid column %d specified." % col


# ------------------------------------------------------------
class MultiLine(Object):

    def __init__(self, string):
        self.string = string

class MultiLineRenderer(JTextArea, TableCellRenderer):

    def __init__(self):
        JTextArea.__init__(self, 2, 20)
        self.setLineWrap(1)
        self.setWrapStyleWord(1)
        self.setOpaque(1)
        self.setEditable(0)


    def getTableCellRendererComponent(self, table, value, isSelected,
                                      hasFocus, row, column):
        if isinstance(value, MultiLine):
            self.setText(value.string)
            newRowHeight = int(self.getPreferredSize().getHeight())
            # print "newRowHeight: ", newRowHeight, " off:", table.getRowHeight(row)
            if newRowHeight != table.getRowHeight(row):
                table.setRowHeight(row, newRowHeight)
                
            return self
        else:
            return JLabel("Wrong type")


# ------------------------------------------------------------  
class ItemTable(JTable):

    def __init__(self, channel):
        print "ItemTable __init__"
        self.item_model = ItemModel(channel)
        JTable.__init__(self, self.item_model)
        self.setDefaultRenderer(MultiLine, MultiLineRenderer())

    def getItemModel(self):
        return self.item_model


# ------------------------------------------------------------
class ChannelTreeNode(DefaultMutableTreeNode):

    def __init__(self, channel):
        self.channel = channel
        DefaultMutableTreeNode.__init__(self, channel.getTitle())

    def getChannel(self):
        return self.channel


# ------------------------------------------------------------
class ChannelTree(JTree):

    def __init__(self, channels, item_model):
        print "ChannelTree __init__"
        self.item_model = item_model
        root = DefaultMutableTreeNode("Root")
        tree_model = DefaultTreeModel(root)
        
        idx = 0
        print "channels", channels
        for channel in channels:
            node = ChannelTreeNode(channel)
            tree_model.insertNodeInto(node, root, idx)
            idx = idx + 1

        JTree.__init__(self, tree_model)


# ------------------------------------------------------------
class ChannelPanel(JPanel):

    gbc = GridBagConstraints()

    def __init__(self):
        JPanel.__init__(self)

        self.setLayout(GridBagLayout())
        self.setBorder(TitledBorder("Channel"))

        # some helper constants
        REL = GridBagConstraints.RELATIVE
        REM = GridBagConstraints.REMAINDER
        HORIZ = GridBagConstraints.HORIZONTAL
        NW = GridBagConstraints.NORTHWEST
        CENTER = GridBagConstraints.CENTER

        # --- title
        label = JLabel("Title:")
        self.constrain(label, REL, REL, REL, 1,
                       HORIZ, CENTER, 1.0, 1.0,
                       2, 2, 2, 2)
        self.field_title = JTextField()
        self.field_title.setEditable(0)
        self.constrain(self.field_title, REL, REL, REM, 1,
                       HORIZ, CENTER, 1.0, 1.0,
                       2, 2, 2, 2)

        # --- description
        label = JLabel("Description:")
        self.constrain(label, REL, REL, REL, 1,
                       HORIZ, NW, 1.0, 1.0,
                       2, 2, 2, 2)
        self.field_descr = JTextArea(3, 40)
        self.field_descr.setEditable(0)
        # wrap long lines
        self.field_descr.setLineWrap(1)
        # allow only full words to be wrapped
        self.field_descr.setWrapStyleWord(1)
        # ensure that the border look is the same
        self.field_descr.setBorder(self.field_title.getBorder())
        self.constrain(self.field_descr, REL, REL, REM, 1,
                       HORIZ, NW, 1.0, 1.0,
                       2, 2, 2, 2)

        # --- location
        label = JLabel("Location:")
        self.constrain(label, REL, REL, REL, 1,
                       HORIZ, NW, 1.0, 1.0,
                       2, 2, 2, 2)
        self.field_location = JTextField()
        self.constrain(self.field_location, REL, REL, REM, REL,
                       HORIZ, NW, 1.0, 1.0,
                       2, 2, 2, 2)

        # --- last update
        label = JLabel("Last Update:")
        self.constrain(label, REL, REL, REL, REM,
                       HORIZ, NW, 1.0, 1.0,
                       2, 2, 2, 2)
        self.field_lastupdate = JTextField()
        self.field_lastupdate.setEditable(0)
        self.constrain(self.field_lastupdate, REL, REL, REM, REM,
                       HORIZ, NW, 1.0, 1.0,
                       2, 2, 2, 2)

    def setChannel(self, channel):
        self.channel = channel
        self.field_title.setText(channel.getTitle())
        self.field_descr.setText(channel.getDescription())
        self.field_location.setText(channel.getLocation().toString())
        self.field_lastupdate.setText(channel.getSubscription().getLastUpdated().toString())

    def refresh(self):
        self.setChannel(self.channel)

    def constrain(self, component,
                  grid_x, grid_y, grid_width, grid_height,
                  fill, anchor, weight_x, weight_y,
                  top, left, bottom, right):
        container = self
        c = self.gbc
        c.gridx = grid_x
        c.gridy = grid_y
        c.gridwidth = grid_width
        c.gridheight = grid_height
        c.fill = fill
        c.anchor = anchor
        c.weightx = weight_x
        c.weighty = weight_y
        if (top + bottom + left + right > 0):
            c.insets = Insets(top, left, bottom, right)

        container.getLayout().setConstraints(component, c)
        container.add(component)


# ------------------------------------------------------------
class MainMediator(TreeSelectionListener, ChannelObserverIF):

    def __init__(self, gui):
        self.gui = gui

    def valueChanged(self, tree_sel_ev):
        tree_path = tree_sel_ev.getNewLeadSelectionPath()
        # retrieve node (cast to ChannelTreeNode needed)
        channel = tree_path.getLastPathComponent().getChannel()
        print "selected:", channel.getTitle()
        self.gui.getItemTable().getItemModel().setActiveChannel(channel)
        self.gui.getChannelPanel().setChannel(channel)

    def itemAdded(self, new_item):
        print "new item", new_item
        channel = new_item.getChannel()
        # only act if channel is currently displayed
        if channel == self.gui.getItemTable().getItemModel().getActiveChannel():
            self.gui.getItemTable().getItemModel().addItem(new_item)
            self.gui.getChannelPanel().refresh()


# ------------------------------------------------------------
class BasicWindowMonitor(WindowAdapter):

    def windowClosing(self, event):
        w = event.getWindow()
        w.setVisible(0)
        w.dispose()
        System.exit(0)


# ------------------------------------------------------------
class MainFrame(JFrame):

    def __init__(self, channels):
        JFrame.__init__(self, "Informa Example News Client")
        self.setSize(400, 400)
        self.addWindowListener(BasicWindowMonitor())

        self.channels = channels
        self.channel_dict = None

        self.channelTree = None
        self.channelPanel = None
        self.itemTable = None

        self.mediator = MainMediator(self)
        
        # items (in table)
        itemScrollPane = JScrollPane(self.getItemTable())

        # channels (as tree)
        channelScrollPane = JScrollPane(self.getChannelTree())

        # put together channel info with item table
        ch_and_items = JPanel()
        ch_and_items.setLayout(BorderLayout())
        ch_and_items.add(self.getChannelPanel(), BorderLayout.NORTH)
        ch_and_items.add(itemScrollPane, BorderLayout.CENTER)

        # final step
        sp = JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                        channelScrollPane, ch_and_items)
        # print "dividerLocation", sp.getDividerLocation()
        # sp.setDividerLocation(0.5)
        self.getContentPane().add(sp, BorderLayout.CENTER)

    def getChannelTree(self):
        if self.channelTree == None:
            self.channelTree = ChannelTree(self.channels,
                                           self.getItemTable().getItemModel())
            self.channelTree.setMinimumSize(Dimension(150, 150))
            # self.channelTree.setPreferredSize(Dimension(150, 200))
            self.channelTree.addTreeSelectionListener(self.mediator)
        return self.channelTree

    def getItemTable(self):
        if self.itemTable == None:
            activeChannel = None
            self.itemTable = ItemTable(activeChannel)
            self.itemTable.setMinimumSize(Dimension(150, 150))
            # self.itemTable.setPreferredSize(Dimension(250, 200))
        return self.itemTable

    def getChannelPanel(self):
        if self.channelPanel == None:
            self.channelPanel = ChannelPanel()
            self.channelPanel.setMinimumSize(Dimension(150, 150))
            # self.channelPanel.setPreferredSize(Dimension(150, 200))
        return self.channelPanel


# ------------------------------------------------------------  
class AutosaveThread(Thread):

    def __init__(self, registry):
        self.registry = registry
        self.filename = "channels.bin"
        # delay between saving in milliseconds
        self.delay = 15 * 60 * 1000

    def run(self):
        while 1:
            print "Writing out channels", Date()
            ostream = FileOutputStream(self.filename)
            p = ObjectOutputStream(ostream)
            p.writeObject(self.registry.getChannelGroup("Default"))
            p.flush()
            ostream.close()
            self.sleep(self.delay)


# ------------------------------------------------------------  
if __name__ == '__main__':

    # retrieve wether channels are off- or online
    if len(sys.argv) > 1:
        online = (sys.argv[1] == "online")
    else:
        online = 1

    # online urls
    if online:
        chl_urls = [["http://www.newsforge.com/newsforge.rss", 15*60], 
                    ["http://slashdot.org/slashdot.rdf", 20*60], 
                    ["http://www.heise.de/newsticker/heise.rdf", 25*60], 
                    ["http://xmlhack.com/rss10.php", 30*60], 
                    ["http://freshmeat.net/backend/fm.rdf", 35*60]]
    else:
        data_dir = "file://" + System.getProperty("user.dir") + "/data/"
        chl_urls = [[data_dir + "linuxjournal.rss", 1*60], 
                    [data_dir + "salon_use.rdf", 2*60], 
                    [data_dir + "heise.rdf", 3*60], 
                    [data_dir + "slashdot.rdf", 4*60], 
                    [data_dir + "xmlhack-1.0.xml", 5*60]]

    # create channel registry
    builder = ChannelBuilder()
    reg = ChannelRegistry(builder)
        
    # add channels to registry and populate news items
    observer = SimpleChannelObserver()
    for chl_url in chl_urls:
        c = reg.addChannel(URL(chl_url[0]), chl_url[1], 1)
        c.addObserver(observer)

    # finally show the whole thing
    main_frame = MainFrame(reg.getChannels())
    main_frame.pack()
    main_frame.setVisible(1)
    
    # autosave task
    t = AutosaveThread(reg)
    t.start()
