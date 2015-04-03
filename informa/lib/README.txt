================================================================
Thirdparty libraries used by Informa
$Id: README.txt 787 2006-01-03 00:14:41Z niko_schmuck $
================================================================

  o commons-beanutils.jar (1.7)
    - runtime (needed by hibernate)
    - Homepage: http://jakarta.apache.org/commons/beanutils.html
    - License: Apache Software License

  o commons-collections.jar (2.1.1)
    - runtime (needed by hibernate)
    - Homepage: http://jakarta.apache.org/commons/collections.html
    - License: Apache Software License

  o commons-lang.jar (2.1)
    - runtime (needed by hibernate)
    - Homepage: http://jakarta.apache.org/commons/lang.html
    - License: Apache Software License
    
  o commons-logging (1.0.4)
    - buildtime (required)
    - Homepage: http://jakarta.apache.org/commons/logging.html
    - License: Apache Software License

  o log4j.jar (1.2.9)
    - Logging framework
    - runtime (optional)
    - Homepage: http://jakarta.apache.org/log4j/
    - License: Apache Software License

  o jdom.jar (1.0)
    - Convient XML handling in Java
    - buildtime (required)
    - Homepage: http://www.jdom.org/
    - License: BSD/Apache style

  o junit.jar (3.8.1)
    - JUnit test framework
    - buildtime, test-runtime
    - Homepage: http://www.junit.org/
    - License: Common Public License

  o lucene.jar (1.4.3)
    - Full-text search engine
    - buildtime (required), runtime (optional)
    - Homepage: http://jakarta.apache.org/lucene/
    - License: Apache Software License

  o hibernate3.jar (3.1)
    - Object-Relational Mapping tool
    - runtime (optional)
    - Homepage: http://hibernate.sourceforge.net/
    - License: Lesser General Public License (LGPL)

	  o antlr.jar (2.7.6rc1)
	    - ANTLR parser generator
	    - required for running Hibernate3
	    - Homepage: http://www.antlr.org
	
	  o cglib-nodep.jar (2.1.3)
	    - CGLIB (http://cglib.sourceforge.net)
	    - required at runtime when proxying full target classes
	    - Homepage: http://cglib.sourceforge.net/
	    - License: Apache Software License
	    
	  o asm.jar and asm-attrs.jar (Unknown version)
	     - ASM bytecode library
	     - required at runtime
	
	  o ehcache.jar (1.1)
	    - runtime (needed by hibernate)
	    - Homepage: http://ehcache.sourceforge.net/
	    - License: Apache Software License    
	
	  o dom4j.jar (1.6.1)
	    - XML configuration and mapping parser
	    - runtime (needed by hibernate)
	    - Homepage: http://dom4j.sourceforge.net/
	    - License: BSD License
	

----------------------------------------------------------------------
OPTIONAL
----------------------------------------------------------------------

  o hsqldb.jar (1.8.0.2)
    - HypersonicSQL database
    - runtime (optionally needed by hibernate and when running tests)
    - Homepage: http://hsqldb.sourceforge.net/
    - License: BSD License

  o mysql.jar (3.0.17)
    - MySQL JDBC Driver (works with MySQL Server 4.0.x)
    - runtme (optional)
    - Homepage: http://www.mysql.com/downloads/api-jdbc.html
    - License: GPL

  o jta.jar (1.0.1)
    - Standard JTA API
    - runtime (optional, needed by Castor)
    - Homepage: http://java.sun.com/products/jta/
    - License: Sun
    
  o checkstyle-all.jar (2.2)
    - Java source code style analyser
    - runtime (optional)
    - Homepage: http://sourceforge.net/projects/checkstyle/
    - License: LGPL
    - Note: Contains also classes from antlr.jar and jakarta-regexp-1.2.jar

    
----------------------------------------------------------------------
NOT PART (Castor JDO support was experimental formerly)
----------------------------------------------------------------------
 
  o castor.jar (0.9.4.3)
    - Persistence framework (XML and JDO-like)
    - Warning: The Castor backend for informa is experimental
    - runtime (optional)
    - Homepage: http://castor.exolab.org/
    - License: BSD/Apache style

  o castor-doclet.jar (0.4.1)
    - Castor Doclet
    - buildtime (optional)
    - Homepage: http://castordoclet.sourceforge.net/
    - License: Lesser General Public License (LGPL)
 