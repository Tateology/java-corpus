/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.3-RC1</a>, using an
 * XML Schema.
 * $Id$
 */

package xml.srcgen.template.generated;

/**
 * Class IllegalJava5Generic.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class IllegalJava5Generic implements java.io.Serializable {

    /**
     * Field _firstInteger.
     */
    private long _firstInteger;

    /**
     * keeps track of state for field: _firstInteger
     */
    private boolean _has_firstInteger;

    /**
     * Field _firstString.
     */
    private java.lang.String _firstString;

    /**
     * Field _listOfIntegers.
     */
    private xml.srcgen.template.generated.ListOfIntegers _listOfIntegers;

    public IllegalJava5Generic() {
        super();
    }

    /**
     */
    public void deleteFirstInteger() {
        this._has_firstInteger= false;
    }

    /**
     * Returns the value of field 'firstInteger'.
     * 
     * @return the value of field 'FirstInteger'.
     */
    public long getFirstInteger() {
        return this._firstInteger;
    }

    /**
     * Returns the value of field 'firstString'.
     * 
     * @return the value of field 'FirstString'.
     */
    public java.lang.String getFirstString() {
        return this._firstString;
    }

    /**
     * Returns the value of field 'listOfIntegers'.
     * 
     * @return the value of field 'ListOfIntegers'.
     */
    public xml.srcgen.template.generated.ListOfIntegers getListOfIntegers() {
        return this._listOfIntegers;
    }

    /**
     * Method hasFirstInteger.
     * 
     * @return true if at least one FirstInteger has been added
     */
    public boolean hasFirstInteger() {
        return this._has_firstInteger;
    }

    /**
     * Method isValid.
     * 
     * @return true if this object is valid according to the schema
     */
    public boolean isValid() {
        try {
            validate();
        } catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    }

    /**
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void marshal(final java.io.Writer out) throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Marshaller.marshal(this, out);
    }

    /**
     * 
     * 
     * @param handler
     * @throws java.io.IOException if an IOException occurs during
     * marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     */
    public void marshal(final org.xml.sax.ContentHandler handler) throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Marshaller.marshal(this, handler);
    }

    /**
     * Sets the value of field 'firstInteger'.
     * 
     * @param firstInteger the value of field 'firstInteger'.
     */
    public void setFirstInteger(final long firstInteger) {
        this._firstInteger = firstInteger;
        this._has_firstInteger = true;
    }

    /**
     * Sets the value of field 'firstString'.
     * 
     * @param firstString the value of field 'firstString'.
     */
    public void setFirstString(final java.lang.String firstString) {
        this._firstString = firstString;
    }

    /**
     * Sets the value of field 'listOfIntegers'.
     * 
     * @param listOfIntegers the value of field 'listOfIntegers'.
     */
    public void setListOfIntegers(final xml.srcgen.template.generated.ListOfIntegers listOfIntegers) {
        this._listOfIntegers = listOfIntegers;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * xml.srcgen.template.generated.IllegalJava5Generic
     */
    public static xml.srcgen.template.generated.IllegalJava5Generic unmarshal(final java.io.Reader reader) throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (xml.srcgen.template.generated.IllegalJava5Generic) org.exolab.castor.xml.Unmarshaller.unmarshal(xml.srcgen.template.generated.IllegalJava5Generic.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate() throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
