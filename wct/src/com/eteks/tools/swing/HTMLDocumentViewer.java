/*
 * @(#)HTMLDocumentViewer.java   05/02/99
 *
 * Copyright (c) 1998-2001 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Visit eTeks web site for up-to-date versions of this file and other
 * Java tools and tutorials : http://www.eteks.com/
 */
package com.eteks.tools.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 * Frame class able to display a HTML file.
 *
 * @version 1.0
 * @author  Emmanuel Puybaret
 * @since   Jeks 1.0
 */
public class HTMLDocumentViewer extends JFrame
                                implements HyperlinkListener,
                                           ActionListener
{
  // Composant Swing permettant de visualiser un document
  JEditorPane viewer       = new JEditorPane ();
  // Champ de saisie de l'URL a visualiser
  JTextField  urlTextField = new JTextField ();

  public HTMLDocumentViewer ()
  {
    this (false);
  }

  public HTMLDocumentViewer (boolean textField)
  {
    // Construction de l'Interface Graphique
    if (textField)
    {
      // Panel en haut avec un label et le champ de saisie
      JPanel inputPanel = new JPanel (new BorderLayout ());
      JLabel label = new JLabel ("URL : ");
      inputPanel.add (label, BorderLayout.WEST);
      inputPanel.add (urlTextField, BorderLayout.CENTER);
      getContentPane ().add (inputPanel, BorderLayout.NORTH);
    }

    // Zone scrollee au centre avec le document
    JScrollPane scrollPane = new JScrollPane (viewer);
    getContentPane ().add (scrollPane, BorderLayout.CENTER);

    // Mode non editable pour recevoir les clics sur les
    // liens hypertexte
    viewer.setEditable (false);
    // Ajout du listener de clic sur lien
    viewer.addHyperlinkListener (this);
    // Ajout du listener de modification de la saisie
    urlTextField.addActionListener (this);
  }

  // Methode appelee apres un clic sur un lien hyper texte
  public void hyperlinkUpdate (HyperlinkEvent event)
  {
    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
    {
      // Modification du champ de saisie
      if (urlTextField != null)
        urlTextField.setText (event.getURL ().toString ());

      if (event instanceof HTMLFrameHyperlinkEvent)
      {
        // Evenement special en cas d'utilisation de Frame HTML
        HTMLDocument doc = (HTMLDocument)viewer.getDocument ();
        doc.processHTMLFrameHyperlinkEvent (
                       (HTMLFrameHyperlinkEvent)event);
      }
      else
        // Chargement de la page
        setPage (urlTextField.getText ());
    }
  }

  public void actionPerformed (ActionEvent event)
  {
    setPage (urlTextField.getText ());
  }

  public void setPage (String urlString)
  {
    try
    {
      // Modification du document visualise
      setPage (new URL (urlString));
    }
    catch (MalformedURLException ex)
    {
      System.err.println ("Acces impossible a " + urlString);
    }
  }

  public void setPage (URL url)
  {
    try
    {
      // Modification du document visualise
      viewer.setPage (url);
    }
    catch (Exception ex)
    {
      System.err.println ("Acces impossible a " + url);
    }
  }
}
