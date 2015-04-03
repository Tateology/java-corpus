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

// $Id: Cloud.java 788 2006-01-03 00:30:39Z niko_schmuck $

package de.nava.informa.impl.hibernate;

import de.nava.informa.core.CloudIF;

/**
 * Hibernate implementation of the CloudIF interface.
 *
 * @author Michael Harhen
 */
public class Cloud implements CloudIF {

  private static final long serialVersionUID = -5075549979291565373L;
	
  private int id = -1;
  private String domain;
  private int port;
  private String path;
  private String registerProcedure;
  private String protocol;

  public Cloud() {
    this("[No Cloud]", -1, null, null, null);
  }

  public Cloud(String domain, int port, String path, String registerProcedure, String protocol) {
    this.domain = domain;
    this.port = port;
    this.path = path;
    this.registerProcedure = registerProcedure;
    this.protocol = protocol;
  }

  // --------------------------------------------------------------
  // implementation of CloudIF interface
  // --------------------------------------------------------------

  public int getIntId() {
    return id;
  }

  public void setIntId(int id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = (int) id;
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

  /**
   * equality by domain, port, and path
   */
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CloudIF)) return false;

    final CloudIF cloud = (CloudIF) o;

    if (port != cloud.getPort()) return false;
    if (domain != null ? !domain.equals(cloud.getDomain()) : cloud.getDomain() != null) return false;
    if (path != null ? !path.equals(cloud.getPath()) : cloud.getPath() != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (domain != null ? domain.hashCode() : 0);
    result = 29 * result + port;
    result = 29 * result + (path != null ? path.hashCode() : 0);
    return result;
  }

}
