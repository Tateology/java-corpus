package de.nava.informa.impl.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import de.nava.informa.core.ChannelUpdatePeriod;

public class ChannelUpdatePeriodUserType implements UserType {

  private static final int[] SQL_TYPES = { Types.VARCHAR };

  /**
   * @see org.hibernate.usertype.UserType#sqlTypes()
   */
  public int[] sqlTypes() {
    return SQL_TYPES;
  }

  /**
   * @see org.hibernate.usertype.UserType#returnedClass()
   */
  public Class returnedClass() {
    return ChannelUpdatePeriod.class;
  }

  /**
   * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
   */
  public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
      throws HibernateException, SQLException {
    String name = resultSet.getString(names[0]);
    ChannelUpdatePeriod result = null;
    if (!resultSet.wasNull()) {
      result = ChannelUpdatePeriod.valueFromText(name);
    }
    return result;
  }

  /**
   * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
   */
  public void nullSafeSet(PreparedStatement preparedStatement, Object value,
      int index) throws HibernateException, SQLException {
    if (null == value) {
      preparedStatement.setNull(index, Types.VARCHAR);
    } else {
      preparedStatement.setString(index, ((ChannelUpdatePeriod) value).toString());
    }
  }

  /**
   * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
   */
  public Object deepCopy(Object value) throws HibernateException {
    return value;
  }

  /**
   * @see org.hibernate.usertype.UserType#isMutable()
   */
  public boolean isMutable() {
    return false;
  }

  /**
   * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
   */
  public Object assemble(Serializable cached, Object owner)
      throws HibernateException {
    return cached;
  }

  /**
   * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
   */
  public Serializable disassemble(Object value) throws HibernateException {
    return (Serializable) value;
  }

  /**
   * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
   */
  public Object replace(Object original, Object target, Object owner)
      throws HibernateException {
    return original;
  }

  /**
   * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
   */
  public int hashCode(Object x) throws HibernateException {
    return x.hashCode();
  }

  /**
   * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
   */
  public boolean equals(Object x, Object y) throws HibernateException {
    if (x == y)
      return true;
    if (null == x || null == y)
      return false;
    return x.equals(y);
  }
}
