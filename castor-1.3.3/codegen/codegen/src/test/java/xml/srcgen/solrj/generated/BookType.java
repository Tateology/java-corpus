/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.3-RC1</a>, using an
 * XML Schema.
 * $Id$
 */

package xml.srcgen.solrj.generated;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.apache.solr.client.solrj.beans.Field;

/**
 * 
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class BookType implements java.io.Serializable {

    /**
     * Field _coverType.
     */
    @Field()
    private java.lang.String _coverType;

    /**
     * Field _isbn.
     */
    @Field("isbn")
    private java.lang.String _isbn;

    /**
     * Field _title.
     */
    @Field("title")
    private java.lang.String _title;

    public BookType() {
        super();
    }

    /**
     * Returns the value of field 'coverType'.
     * 
     * @return the value of field 'CoverType'.
     */
    public java.lang.String getCoverType() {
        return this._coverType;
    }

    /**
     * Returns the value of field 'isbn'.
     * 
     * @return the value of field 'Isbn'.
     */
    public java.lang.String getIsbn() {
        return this._isbn;
    }

    /**
     * Returns the value of field 'title'.
     * 
     * @return the value of field 'Title'.
     */
    public java.lang.String getTitle() {
        return this._title;
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
     * Sets the value of field 'coverType'.
     * 
     * @param coverType the value of field 'coverType'.
     */
    public void setCoverType(final java.lang.String coverType) {
        this._coverType = coverType;
    }

    /**
     * Sets the value of field 'isbn'.
     * 
     * @param isbn the value of field 'isbn'.
     */
    public void setIsbn(final java.lang.String isbn) {
        this._isbn = isbn;
    }

    /**
     * Sets the value of field 'title'.
     * 
     * @param title the value of field 'title'.
     */
    public void setTitle(final java.lang.String title) {
        this._title = title;
    }

    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled xml.srcgen.solrj.generated.BookType
     */
    public static xml.srcgen.solrj.generated.BookType unmarshal(final java.io.Reader reader) throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (xml.srcgen.solrj.generated.BookType) org.exolab.castor.xml.Unmarshaller.unmarshal(xml.srcgen.solrj.generated.BookType.class, reader);
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
