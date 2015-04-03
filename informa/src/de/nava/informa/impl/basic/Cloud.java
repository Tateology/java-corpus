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

// $Id: Cloud.java 779 2005-09-27 22:17:06Z niko_schmuck $

package de.nava.informa.impl.basic;

import de.nava.informa.core.CloudIF;

/**
 * In-Memory implementation of the CloudIF interface.
 *
 * @author Michael Harhen
 */
public class Cloud implements CloudIF {

	private static final long serialVersionUID = -48710418882153466L;

	private long id;
  private String domain;
  private int port;
  private String path;
  private String registerProcedure;
  private String protocol;

  public Cloud() {
    this("[No Cloud]", -1, null, null, null);
  }

  public Cloud(String domain, int port, String path, String registerProcedure, String protocol) {
    this.id = IdGenerator.getInstance().getId();
    this.domain = domain;
    this.port = port;
    this.path = path;
    this.registerProcedure = registerProcedure;
    this.protocol = protocol;
  }

  // --------------------------------------------------------------
  // implementation of CloudIF interface
  // --------------------------------------------------------------

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getRegisterProcedure() {
    return registerProcedure;
  }

  public void setRegisterProcedure(String registerProcedure) {
    this.registerProcedure = registerProcedure;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

}
