//
// Informa -- RSS Library for Java
// Copyright (c) 2002 by Niko Schmuck
//
// Niko Schmuck
// http://sourceforge.net/projects/informa
// mailto:niko_schmuck@users.sourceforge.net
//
// This library is free software.
//
// You may redistribute it and/or modify it under the terms of the GNU
// Lesser General Public License as published by the Free Software Foundation.
//
// Version 2.1 of the license should be included with this distribution in
// the file LICENSE. If the license is not included with this distribution,
// you may find a copy at the FSF web site at 'www.gnu.org' or 'www.fsf.org',
// or you may write to the Free Software Foundation, 675 Mass Ave, Cambridge, 
// MA 02139 USA.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied waranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//


// $Id: RSS_0_91_Exporter.java 853 2008-11-07 09:13:30Z niko_schmuck $

package de.nava.informa.exporters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.nava.informa.core.ChannelExporterIF;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.utils.ParserUtils;

/**
 * A channel exporter that can write channel objects out into the
 * interchange syntax defined by RSS 0.91.</p>
 */
public class RSS_0_91_Exporter implements ChannelExporterIF {

  public static final String PUBLIC_ID =
    "-//Netscape Communications//DTD RSS 0.91//EN";
  public static final String SYSTEM_ID =
    "http://my.netscape.com/publish/formats/rss-0.91.dtd";
  public static final String RSS_VERSION = "0.91";
  
  private Writer writer;
  private String encoding;
  
  /**
   * Creates a channel exporter bound to the file given in the
   * argument. The channel will be written out in the UTF-8 encoding.
   *
   * @param filename - The name of the file to which the channel object
   *                   is to be written.
   */
  public RSS_0_91_Exporter(String filename) throws IOException {
    this(new File(filename), "utf-8");
  }

  /**
   * Creates a channel exporter bound to the file given in the
   * argument. The channel will be written out in the UTF-8 encoding.
   *
   * @param file - The file object to which the channel object is
   *               to be written.
   */
  public RSS_0_91_Exporter(File file) throws IOException {
    this(file, "utf-8");
  }

  /**
   * Creates a channel exporter bound to the file given in the
   * arguments.
   *
   * @param file - The file object to which the channel object is
   *               to be written.
   * @param encoding - The character encoding to write the channel
   *                   object in.
   */
  public RSS_0_91_Exporter(File file, String encoding) throws IOException {
    this.writer = new OutputStreamWriter(new FileOutputStream(file), encoding);
    this.encoding = encoding;
  }
  
  /**
   * Creates a channel exporter bound to the Writer given in
   * the arguments.
   *
   * @param writer - The Writer to which the channel object is to be
   *                 written.
   * @param encoding - The character encoding the Writer writes in.
   */
  public RSS_0_91_Exporter(Writer writer, String encoding) {
    this.writer = writer;
    this.encoding = encoding;
  }

  // ------------------------------------------------------------
  // implementation of ChannelExporterIF interface
  // ------------------------------------------------------------
  
  public void write(ChannelIF channel) throws IOException {
    if (writer == null) {
      throw new RuntimeException("No writer has been initialized.");
    }

    // create XML outputter with indent: 2 spaces, print new lines.
    Format format = Format.getPrettyFormat();
    format.setEncoding(encoding);
    XMLOutputter outputter = new XMLOutputter(format);
    
    // ----
    Element rootElem = new Element("rss");
    rootElem.setAttribute("version", RSS_VERSION);
    Element channelElem = new Element("channel");
            
    channelElem.addContent(new Element("title").setText(channel.getTitle()));
    
    channelElem.addContent(new Element("description")
                           .setText(channel.getDescription()));
    if (channel.getSite() != null) {
      channelElem.addContent(new Element("link")
                             .setText(channel.getSite().toString()));
    }
    if (channel.getLanguage() != null) {
      channelElem.addContent(new Element("language")
                             .setText(channel.getLanguage()));
    }

    Collection items = channel.getItems();
    Iterator it = items.iterator();
    while (it.hasNext()) {
      channelElem.addContent(getItemElement((ItemIF) it.next()));
    }

    // export channel image            
    if (channel.getImage() != null) {
      Element imgElem = new Element("image");
      imgElem.addContent(new Element("title")
                         .setText(channel.getImage().getTitle()));
      imgElem.addContent(new Element("url")
                         .setText(channel.getImage().getLocation().toString()));
      imgElem.addContent(new Element("link")
                         .setText(channel.getImage().getLink().toString()));
      imgElem.addContent(new Element("height")
                         .setText("" + channel.getImage().getHeight()));
      imgElem.addContent(new Element("width")
                         .setText("" + channel.getImage().getWidth()));
      imgElem.addContent(new Element("description")
                         .setText(channel.getImage().getDescription()));
      channelElem.addContent(imgElem);
    }
     
    // TODO: add exporting textinput field
    //     if (channel.getTextInput() != null) {
    //       channelElem.addContent(channel.getTextInput().getElement());
    //     }

    if (channel.getCopyright() != null) {
      channelElem.addContent(new Element("copyright")
                             .setText(channel.getCopyright()));
    }

    // we have all together for the channel definition
    rootElem.addContent(channelElem);
    // ---
    DocType docType = new DocType("rss", PUBLIC_ID, SYSTEM_ID);
    Document doc = new Document(rootElem, docType);
    outputter.output(doc, writer);
  }


  protected Element getItemElement(ItemIF item) {
    Element itemElem = new Element("item");
    itemElem.addContent(new Element("title").setText(item.getTitle()));
    if (item.getLink() != null) {
      itemElem.addContent(new Element("link")
                          .setText(item.getLink().toString()));
    }
    if (item.getDescription() != null) {
      itemElem.addContent(new Element("description")
                          .setText(item.getDescription()));
    }
    if (item.getDate() != null) {
      itemElem.addContent(new Element("pubDate")
                          .setText(ParserUtils.formatDate(item.getDate())));
    }
    return itemElem;
  }
  
}
