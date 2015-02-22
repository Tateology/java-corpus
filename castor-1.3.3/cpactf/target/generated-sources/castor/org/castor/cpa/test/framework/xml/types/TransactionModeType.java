/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.castor.cpa.test.framework.xml.types;

/**
 * Enumeration TransactionModeType.
 * 
 * @version $Revision$ $Date$
 */
public enum TransactionModeType {


      //------------------/
     //- Enum Constants -/
    //------------------/

    /**
     * Constant LOCAL
     */
    LOCAL("local"),
    /**
     * Constant GLOBAL
     */
    GLOBAL("global");

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field value.
     */
    private final java.lang.String value;

    /**
     * Field enumConstants.
     */
    private static final java.util.Map<java.lang.String, TransactionModeType> enumConstants = new java.util.HashMap<java.lang.String, TransactionModeType>();


    static {
        for (TransactionModeType c: TransactionModeType.values()) {
            TransactionModeType.enumConstants.put(c.value, c);
        }

    };


      //----------------/
     //- Constructors -/
    //----------------/

    private TransactionModeType(final java.lang.String value) {
        this.value = value;
    }


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method fromValue.
     * 
     * @param value
     * @return the constant for this value
     */
    public static org.castor.cpa.test.framework.xml.types.TransactionModeType fromValue(
            final java.lang.String value) {
        TransactionModeType c = TransactionModeType.enumConstants.get(value);
        if (c != null) {
            return c;
        }
        throw new IllegalArgumentException(value);
    }

    /**
     * 
     * 
     * @param value
     */
    public void setValue(
            final java.lang.String value) {
    }

    /**
     * Method toString.
     * 
     * @return the value of this constant
     */
    public java.lang.String toString(
    ) {
        return this.value;
    }

    /**
     * Method value.
     * 
     * @return the value of this constant
     */
    public java.lang.String value(
    ) {
        return this.value;
    }

}
