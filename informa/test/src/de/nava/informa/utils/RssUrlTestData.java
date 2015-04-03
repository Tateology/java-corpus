//
//Informa -- RSS Library for Java
//Copyright (c) 2002-2003 by Niko Schmuck
//
//Niko Schmuck
//http://sourceforge.net/projects/informa
//mailto:niko_schmuck@users.sourceforge.net
//
//This library is free software.
//
//You may redistribute it and/or modify it under the terms of the GNU
//Lesser General Public License as published by the Free Software Foundation.
//
//Version 2.1 of the license should be included with this distribution in
//the file LICENSE. If the license is not included with this distribution,
//you may find a copy at the FSF web site at 'www.gnu.org' or 'www.fsf.org',
//or you may write to the Free Software Foundation, 675 Mass Ave, Cambridge,
//MA 02139 USA.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied waranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//Lesser General Public License for more details.
//
package de.nava.informa.utils;

/**
 * Handy Dandy Test Data generator. There are two ways of using this. By calling 'generate()' we
 * just generate a stream of different rss urls to use for testing. The stream wraps around
 * eventually. Calling reset() we start the stream up again. Or, you can just call 'get(int)' to get
 * the nth url.
 *  
 */
public class RssUrlTestData
{

    static int current = 0;
    static String[] xmlURLs = {
            "http://www.7nights.com/asterisk/index.xml",
            "http://barlow.typepad.com/barlowfriendz/index.rdf",
            "http://www.edithere.com/barry/xml/rss.xml",
            "http://cyber.law.harvard.edu/blogs/audio/lydonRss.xml",
            "http://comdig2.de/test/issue_rss2.php?id_issue=2003.46",
            "http://danbricklin.com/log_rss.xml",
            "http://weblog.siliconvalley.com/column/dangillmor/index.rdf",
            "http://blog.ziffdavis.com/coursey/Rss.aspx",
            "http://blog.fastcompany.com/index.xml",
            "http://isen.com/blog/index.rdf",
            "http://weblogs.cs.cornell.edu/AllThingsDistributed/",
            "http://seems2shel.typepad.com/itseemstome/index.rdf",
            "http://www.zeldman.com/feed/zeldman.xml",
            "http://joi.ito.com/index.xml",
            "http://www.meskill.net/weblogs/index.xml",
            "http://www.lessig.org/blog/index.rdf",
            "http://www.librarystuff.net/index.rdf",
            "http://www.lifewithalacrity.com/index.rdf",
            "http://blogs.law.harvard.edu/lydon/xml/rss.xml",
            "http://www.corante.com/many/index.rdf",
            "http://microdoc-news.info/rss",
            "http://novaspivack.typepad.com/nova_spivacks_weblog/index.rdf",
            "http://blogs.osafoundation.org/mitch/index.rdf",
            "http://www.tbray.org/ongoing/ongoing.rss",
            "http://paolo.evectors.it/rss.xml",
            "http://www.edventure.com/rss/R1feed.xml",
            "http://satn.org/satn_rss.xml",
            "http://partners.userland.com/people/docSearls.xml",
            "http://feedster.com/blog/rss.php?version=2.0",
            "http://www.neward.net/ted/weblog/rss.jsp",
            "http://www.theshiftedlibrarian.com/rss.xml",
            "http://socialsoftware.weblogsinc.com/entries.xml",
            "http://blog.zmag.org/ttt/index.rdf",
            "http://www.decafbad.com/blog/index.rdf",
            "http://www.activewin.com/awin/headlines.rss",
            "http://www.adambosworth.net/index.rdf",
            "http://weblog.anthonyeden.com/index.xml",
            "http://enthusiasm.cozy.org/index.rdf",
            "http://chris.pirillo.com/index.rdf",
            "http://cshipley.typepad.com/chris_shipley_group/index.rdf",
            "http://reviews.cnet.com/4924-3000_7-0.xml?orderBy=-7eRating&amp;7rType=70-80&amp;9lwPrc=0-\\",
            "http://www.ipadventures.com/blog/index.rdf",
            "http://grumet.net/weblog/rsstv.xml",
            "http://michaelthompson.org/news/gms.xml",
            "http://www.rassoc.com/gregr/weblog/rss.aspx",
            "http://weblog.infoworld.com/techwatch/index.rdf",
            "http://lockergnome.com/rss/windows.php", "http://rss.lockergnome.com/feed/",
            "http://www.lockergnome.com/lockergnome.xml", "http://blogs.it/0100198/rss.xml",
            "http://blogs.msdn.com/michael_howard/Rss.aspx",
            "http://rssnewsapps.ziffdavis.com/msw.xml", "http://blogs.msdn.com/MainFeed.aspx",
            "http://www.nedbatchelder.com/blog/rss.xml",
            "http://www.tbray.org/ongoing/ongoing.rss", "http://raindrop.msresearch.us/rss.aspx",
            "http://www.intertwingly.net/blog/index.rss", "http://www.kunal.org/scoble/index.rdf",
            "http://weblogs.asp.net/smguest/Rss.aspx", "http://www.telepocalypse.net/index.rdf",
            "http://www.officeletter.com/tolrss.xml",
            "http://www.theregister.co.uk/tonys/slashdot.rdf", "http://werbach.com/blog/rss.xml",
            "http://www.windley.com/rss.xml"};

    static public String get(int i)
    {
        return xmlURLs[i % xmlURLs.length];
    }

    static public String generate()
    {
        return get(current++);
    }

    static public void reset()
    {
        current = 0;
    }

}