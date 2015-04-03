OVERVIEW
----------------------

Informa is an open source Java API for building applications which want to
access, modify and persist news channels information to allow content
syndication.

PLEASE NOTE: That although the Informa API is in a pretty stable state the
whole distribution is not complete and still considered in beta stadium.
Please report any problems to the informa-developers mailing list. Thanks
for your patience.

Additional information and documentation can be obtained from the Informa
web site: http://informa.sourceforge.net/


GETTING STARTED
----------------------

Requirements: it is assumed that you have a running Java 2 SDK (v. >= 1.3) 
environment and a SAX Parser (like Xerces-J) available (not needed if you
already use J2SDK 1.4 and higher).

Two jar files for Informa are provided, one with just the compiled class files
for production use, and the other including both source java files and compiled 
class files for development and debugging purposes. Append the necessary JARs in 
the lib directory (e.g, informa.jar along with required dependencies) to your 
CLASSPATH. Together with the JavaDoc you should be able to get up to speed in 
relative short time, one main goal in the design of the Informa API was to be 
as much intuitive as possible.

This distribution will also includes an example application at some point in
the future. In the meantime please see the test directory in the src
package or in CVS to obtain samples and a minigui test application.


To verify that parsing is working properly you might want to execute for a
local feed:

  ant parse -Dargs='-f /path/to/my-weblog.xml'

If the feed is available from an URI please specify:

  ant parse -Dargs='-u http://url.to/my-weblog.xml'

Happy news channels parsing.


NOTES
----------------------

Please note that this release is a beta release.  Although Informa should be
fairly stable at this point, there is the possibility for changes before the
final release.

If you like you could have a look into the FAQ which is at:
http://informa.sourceforge.net/faq.html


CONTACT
----------------------

Please send all questions to niko_schmuck@users.sourceforge.net or join the
Informa developers list by visiting the web site
http://lists.sourceforge.net/mailman/listinfo/informa-developer .


Have fun,
Niko Schmuck
