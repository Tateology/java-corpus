/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.castor.cpa.test.test2996.single.jdo_descriptors;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.lang.reflect.Method;
import org.castor.core.exception.IllegalClassDescriptorInitialization;
import org.castor.cpa.test.test2996.single.Book;
import org.castor.jdo.engine.SQLTypeInfos;
import org.exolab.castor.jdo.engine.nature.ClassDescriptorJDONature;
import org.exolab.castor.jdo.engine.nature.FieldDescriptorJDONature;
import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.mapping.FieldDescriptor;
import org.exolab.castor.mapping.FieldHandler;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.mapping.loader.FieldDescriptorImpl;
import org.exolab.castor.mapping.loader.FieldHandlerImpl;
import org.exolab.castor.mapping.loader.TypeInfo;
import org.exolab.castor.mapping.xml.ClassChoice;
import org.exolab.castor.mapping.xml.ClassMapping;
import org.exolab.castor.mapping.xml.FieldMapping;
import org.exolab.castor.mapping.xml.MapTo;
import org.exolab.castor.mapping.xml.Sql;
import org.exolab.castor.mapping.xml.types.ClassMappingAccessType;
import org.exolab.castor.mapping.xml.types.FieldMappingCollectionType;

/**
 * Class BookJDODescriptor.
 * 
 * @version $Revision$ $Date$
 */
public class BookJDODescriptor extends org.exolab.castor.mapping.loader.ClassDescriptorImpl {


      //----------------/
     //- Constructors -/
    //----------------/

    public BookJDODescriptor() {
        super();
        ClassMapping mapping = new ClassMapping();
        ClassChoice choice = new ClassChoice();
        MapTo mapTo = new MapTo();

        addNature(ClassDescriptorJDONature.class.getName());
        ClassDescriptorJDONature jdoNature = new ClassDescriptorJDONature(this);
        jdoNature.setTableName("test2996_single_book");
        setJavaClass(Book.class);
        jdoNature.setAccessMode(AccessMode.valueOf("shared"));
        jdoNature.addCacheParam("name", "org.castor.cpa.test.test2996.single.Book");

        mapping.setAccess(ClassMappingAccessType.fromValue("shared"));
        mapping.setAutoComplete(true);
        mapping.setName("org.castor.cpa.test.test2996.single.Book");
        mapping.setClassChoice(choice);
        mapTo.setTable("test2996_single_book");
        mapping.setMapTo(mapTo);
        setMapping(mapping);

        //isbn field
        String isbnFieldName = "isbn";
        FieldDescriptorImpl isbnFieldDescr;
        FieldMapping isbnFM = new FieldMapping();
        TypeInfo isbnType = new TypeInfo(java.lang.Long.class);
        // Set columns required (= not null)
        isbnType.setRequired(true);

        FieldHandler isbnHandler;
        try {
            Method isbnGetMethod = Book.class.getMethod("getIsbn", null);
            Method isbnSetMethod = Book.class.getMethod("setIsbn", new Class[]{
                long.class});

            isbnHandler = new FieldHandlerImpl(isbnFieldName, null, null,
                isbnGetMethod, isbnSetMethod, isbnType);

        } catch (SecurityException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (MappingException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (NoSuchMethodException e1) {
            throw new RuntimeException(e1.getMessage());
        }
        // Instantiate isbn field descriptor
        isbnFieldDescr = new FieldDescriptorImpl(isbnFieldName, isbnType,isbnHandler, false);
        isbnFieldDescr.addNature(FieldDescriptorJDONature.class.getName());
        FieldDescriptorJDONature isbnFieldJdoNature = new FieldDescriptorJDONature(isbnFieldDescr);
        isbnFieldJdoNature.setSQLName(new String[] { "isbn" });
        isbnFieldJdoNature.setSQLType(new int[] {SQLTypeInfos.javaType2sqlTypeNum(java.lang.Long.class) });
        isbnFieldJdoNature.setManyTable(null);
        isbnFieldJdoNature.setManyKey(new String[] {});
        isbnFieldJdoNature.setDirtyCheck(false);
        isbnFieldJdoNature.setReadOnly(false);

        isbnFieldDescr.setContainingClassDescriptor(this);
        isbnFieldDescr.setIdentity(true);
        isbnFM.setIdentity(true);
        isbnFM.setDirect(false);
        isbnFM.setName("isbn");
        isbnFM.setRequired(true);
        isbnFM.setSetMethod("setIsbn");
        isbnFM.setGetMethod("getIsbn");
        Sql isbnSql = new Sql();
        isbnSql.addName("isbn");
        isbnSql.setType("integer");
        isbnFM.setSql(isbnSql);
        isbnFM.setType("long");
        choice.addFieldMapping(isbnFM);

        //title field
        String titleFieldName = "title";
        FieldDescriptorImpl titleFieldDescr;
        FieldMapping titleFM = new FieldMapping();
        TypeInfo titleType = new TypeInfo(java.lang.String.class);
        // Set columns required (= not null)
        titleType.setRequired(true);

        FieldHandler titleHandler;
        try {
            Method titleGetMethod = Book.class.getMethod("getTitle", null);
            Method titleSetMethod = Book.class.getMethod("setTitle", new Class[]{
                java.lang.String.class});

            titleHandler = new FieldHandlerImpl(titleFieldName, null, null,
                titleGetMethod, titleSetMethod, titleType);

        } catch (SecurityException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (MappingException e1) {
            throw new RuntimeException(e1.getMessage());
        } catch (NoSuchMethodException e1) {
            throw new RuntimeException(e1.getMessage());
        }
        // Instantiate title field descriptor
        titleFieldDescr = new FieldDescriptorImpl(titleFieldName, titleType,titleHandler, false);
        titleFieldDescr.addNature(FieldDescriptorJDONature.class.getName());
        FieldDescriptorJDONature titleFieldJdoNature = new FieldDescriptorJDONature(titleFieldDescr);
        titleFieldJdoNature.setSQLName(new String[] { "title" });
        titleFieldJdoNature.setSQLType(new int[] {SQLTypeInfos.javaType2sqlTypeNum(java.lang.String.class) });
        titleFieldJdoNature.setManyTable(null);
        titleFieldJdoNature.setManyKey(new String[] {});
        titleFieldJdoNature.setDirtyCheck(false);
        titleFieldJdoNature.setReadOnly(false);

        titleFieldDescr.setContainingClassDescriptor(this);
        titleFieldDescr.setIdentity(false);
        titleFM.setIdentity(false);
        titleFM.setDirect(false);
        titleFM.setName("title");
        titleFM.setRequired(true);
        titleFM.setSetMethod("setTitle");
        titleFM.setGetMethod("getTitle");
        Sql titleSql = new Sql();
        titleSql.addName("title");
        titleSql.setType("varchar");
        titleFM.setSql(titleSql);
        titleFM.setType("java.lang.String");
        choice.addFieldMapping(titleFM);

        setFields(new FieldDescriptor[] {titleFieldDescr});
        setIdentities(new FieldDescriptor[] {isbnFieldDescr});
    }

}
