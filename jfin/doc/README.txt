jFin, open source derivatives processing.

DateMath Beta Version B0.2.0

-- LEGAL --
Copyright (C) 2005-2007 Morgan Brown Consultancy Ltd.

This file is part of jFin.

jFin is free software; you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation; either version 2 of the License, or (at your option) any later
version.

Please make sure you read and understand COPYING.txt included with this distribution

-- FINANCIALCALENDAR.COM --
Sample files are provided from financialcalendar.com, along with an implementation
of a HolidayCalendarFactory to use them.

The files are provided only as samples and do not represent the actual holiday
calendars.

To license the full actual set of holiday calendars (covering some 540 holiday
centers) please visit http://financialcalendar.com/.

http://financialcalendar.com/ is not affiliated in any way with jfin.org or
Morgan Brown Consultancy Limited and does not endorse the implementation
provided as part of jFin in any way. The HolidayCalendarFactory to use these
files is provided under the GNU General Public License, as above.

-- WHAT YOU NEED --
jFin just requires a Java 1.5 JVM.

The examples folder contains two examples at the moment
which you can run on a *NIX system by executing:
cd examples
./SwapScheduleGenerator.sh
./DaycountFractionCalculator.sh 2006/02/18 2006/05/18 WE ACTACT

These are pretty simple scripts, and I'll provide a .bat file at some point
so it's easier for windows users.

The source files are included in the examples directory, so this would
probably make a good starting point for exploring the library.

-- What's changed --
The changelog is contained in the RELEASES.txt file included in this distribution.

-- WHY Java 1.5 --
I like using strongly typed enums and collections. If enough people or stuck on
1.4 then I'll refactor it to use the old style static int enums and weakly
typed collections.

-- Why it is a Beta release --
The next stage is to create a comprehensive and veracious test pack for both
date adjustment and day count calculation. This test pack should include trades
calculations produced by proprietary derivatives processing applications.

Currently it is tested and conforms to:
http://www.isda.org/c_and_a/pdf/mktc1198.pdf
Notes on non conformity can be found in org.jfin.date.test.ISDAMarketConventionsDocTest

-- Getting involved --
Things that need to be done:
* Feedback on the balance of ease of use versus flexibility of the API
* Contribute to the test pack
* Test it against your own existing systems functionality and results
* Better log-comments, autodocs and documentation
