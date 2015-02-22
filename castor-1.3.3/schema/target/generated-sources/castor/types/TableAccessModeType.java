/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package types;

/**
 * Enumeration TableAccessModeType.
 * 
 * @version $Revision$ $Date$
 */
public enum TableAccessModeType {


      //------------------/
     //- Enum Constants -/
    //------------------/

    /**
     * Constant READ_ONLY
     */
    READ_ONLY("read-only"),
    /**
     * Constant SHARED
     */
    SHARED("shared"),
    /**
     * Constant EXCLUSIVE
     */
    EXCLUSIVE("exclusive"),
    /**
     * Constant DB_LOCKED
     */
    DB_LOCKED("db-locked");

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
    private static final java.util.Map<java.lang.String, TableAccessModeType> enumConstants = new java.util.HashMap<java.lang.String, TableAccessModeType>();


    static {
        for (TableAccessModeType c: TableAccessModeType.values()) {
            TableAccessModeType.enumConstants.put(c.value, c);
        }

    };


      //----------------/
     //- Constructors -/
    //----------------/

    private TableAccessModeType(final java.lang.String value) {
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
    public static types.TableAccessModeType fromValue(
            final java.lang.String value) {
        TableAccessModeType c = TableAccessModeType.enumConstants.get(value);
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
