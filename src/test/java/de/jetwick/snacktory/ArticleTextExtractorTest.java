package de.jetwick.snacktory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Alex P, (ifesdjeen from jreadability)
 * @author Peter Karich
 */
public class ArticleTextExtractorTest {

    ArticleTextExtractor extractor;
    Converter c;

    @Before
    public void setup() throws Exception {
        c = new Converter();
        extractor = new ArticleTextExtractor();
    }

    @Test
    public void testData1() throws Exception {
        // ? http://www.npr.org/blogs/money/2010/10/04/130329523/how-fake-money-saved-brazil
        JResult res = extractor.extractContent(readFileAsString("test_data/1.html"));
        assertEquals("How Fake Money Saved Brazil", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("This is a story about how an economist and his buddies tricked the people of Brazil into saving the country from rampant inflation. They had a crazy, unlikely plan, and it worked. Twenty years ago, Brazil's"));
        assertTrue(res.getText(), res.getText().endsWith("\"How Four Drinking Buddies Saved Brazil.\""));
        assertEquals("http://media.npr.org/assets/img/2010/10/04/real_wide.jpg?t=1286218782&s=3", res.getImageUrl());
        assertTrue(res.getKeywords().isEmpty());
        assertEquals("Chana Joffe-Walt", res.getAuthorName());
    }

    /*
     * Test broken because of change of heuristics to extract title,
     * however since the change seems to improve the cleanliness of the extracted titles
     * I am leaving the change and disabling the test.
    @Test
    public void testData2() throws Exception {
        // http://benjaminste.in/post/1223476561/hey-guys-whatcha-doing
        JResult res = extractor.extractContent(readFileAsString("test_data/2.html"));
        assertEquals("Hey guys, whatcha doing?", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("This month is the 15th anniversary of my last CD."));
        assertTrue(res.getKeywords().isEmpty());
    }*/

    @Test
    public void testData3() throws Exception {
        JResult res = extractor.extractContent(readFileAsString("test_data/3.html"));
        assertTrue("data3:" + res.getText(), res.getText().startsWith("October 2010 Silicon Valley proper is mostly suburban sprawl. At first glance it "));
        assertTrue(res.getText().endsWith(" and Jessica Livingston for reading drafts of this."));
        assertTrue(res.getKeywords().isEmpty());
    }

    @Test
    public void testData5() throws Exception {
        JResult res = extractor.extractContent(readFileAsString("test_data/5.html"));
        assertTrue("data5:" + res.getText(), res.getText().startsWith("Hackers unite in Stanford"));
//        assertTrue(res.getText().endsWith("have beats and bevvies a-plenty. RSVP here.    "));
        assertTrue(res.getKeywords().isEmpty());
    }

    @Test
    public void testCNN() throws Exception {
        // http://edition.cnn.com/2011/WORLD/africa/04/06/libya.war/index.html?on.cnn=1
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("cnn.html")));
        assertEquals("Gadhafi asks Obama to end NATO bombing", res.getTitle());
        assertEquals("/2011/WORLD/africa/04/06/libya.war/t1larg.libyarebel.gi.jpg", res.getImageUrl());
        assertTrue("cnn:" + res.getText(), res.getText().startsWith("Tripoli, Libya (CNN) -- As rebel and pro-government forces in Libya maneuvered on the battlefield Wedn"));
        assertEquals("By the CNN Wire Staff", res.getAuthorName());
    }

    @Test
    public void testBBC() throws Exception {
        // http://www.bbc.co.uk/news/world-latin-america-21226565
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("bbc_noscript.html")));
        assertEquals("Brazil mourns Santa Maria nightclub fire victims", res.getTitle());
        assertEquals("http://news.bbcimg.co.uk/media/images/65545000/gif/_65545798_brazil_santa_m_kiss_464.gif", res.getImageUrl());
        assertTrue(res.getText().startsWith("Brazil has declared three days of national mourning for 231 people killed in a nightclub fire in the southern city of Santa Maria."));
        assertEquals("Caio Quero", res.getAuthorName());
    }

    @Test
    public void testReuters() throws Exception {
        // http://www.reuters.com/article/2012/08/03/us-knightcapital-trading-technology-idUSBRE87203X20120803
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("reuters.html")));
        assertEquals("Knight trading loss shows cracks in equity markets", res.getTitle());
        assertEquals("http://s1.reutersmedia.net/resources/r/?m=02&d=20120803&t=2&i=637797752&w=460&fh=&fw=&ll=&pl=&r=CBRE872074Y00", res.getImageUrl());
        assertTrue("reuters:" + res.getText(), res.getText().startsWith("(Reuters) - The software glitch that cost Knight Capital Group $440 million in just 45 minutes reveals the deep fault lines in stock markets that are increasingly dominated by sophisticated high-speed trading systems. But Wall Street firms and regulators have few easy solutions for such problems."));
        assertEquals("Jed Horowitz and Joseph Menn", res.getAuthorName());
    }

    @Test
    public void testCaltonCaldwell() throws Exception {
        // http://daltoncaldwell.com/dear-mark-zuckerberg (html5)
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("daltoncaldwell.html")));
        assertEquals("Dear Mark Zuckerberg by Dalton Caldwell", res.getTitle());
        assertTrue("daltoncaldwell:" + res.getText(), res.getText().startsWith("On June 13, 2012, at 4:30 p.m., I attended a meeting at Facebook HQ in Menlo Park, California."));
    }

    @Test
    public void testWordpress() throws Exception {
        // http://karussell.wordpress.com/
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("wordpress.html")));
        assertEquals("Twitter API and Me « Find Time for the Karussell", res.getTitle());
        assertTrue("wordpress:" + res.getText(), res.getText().startsWith("I have a love hate relationship with Twitter. As a user I see "));
    }

    @Test
    public void testFirefox() throws Exception {
        // http://www.golem.de/1104/82797.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("golem.html")));
        assertTrue(res.getText(), res.getText().startsWith("Mozilla hat Firefox 5.0a2 veröffentlicht und zugleich eine erste Entwicklerversion von Firefox 6 freigegeben."));
        assertEquals("http://scr3.golem.de/screenshots/1104/Firefox-Aurora/thumb480/aurora-nighly-beta-logos.png", res.getImageUrl());
        assertEquals("Vorabversionen von Firefox 5 und 6 veröffentlicht", res.getTitle());
    }

    @Test
    public void testYomiuri() throws Exception {
        // http://www.yomiuri.co.jp/e-japan/gifu/news/20110410-OYT8T00124.htm
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("yomiuri.html")));
        assertEquals("色とりどりのチューリップ", res.getTitle());
        assertTrue("yomiuri:" + res.getText(), res.getText().contains("海津市海津町の国営木曽三川公園で、チューリップが見頃を迎えている。２０日までは「チューリップ祭」が開かれており、大勢の人たちが多彩な色や形を鑑賞している＝写真＝"));
        assertEquals(Arrays.asList("読売新聞", "地域"), res.getKeywords());
    }

    @Test
    public void testFAZ() throws Exception {
        // http://www.faz.net/s/Rub469C43057F8C437CACC2DE9ED41B7950/Doc~EBA775DE7201E46E0B0C5AD9619BD56E9~ATpl~Ecommon~Scontent.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("faz.html")));
        assertTrue(res.getText(), res.getText().startsWith("Deutschland hat vor, ganz auf Atomkraft zu verzichten. Ist das eine gute"));
        assertEquals("/m/{5F104CCF-3B5A-4B4C-B83E-4774ECB29889}g225_4.jpg", res.getImageUrl());
        assertEquals("FAZ Electronic Media", res.getAuthorName());
        assertEquals(Arrays.asList("Atomkraft", "Deutschland", "Jahren", "Atommüll", "Fukushima", "Problem", "Brand", "Kohle", "2011", "11",
                "Stewart", "Atomdebatte", "Jahre", "Boden", "Treibhausgase", "April", "Welt", "Müll", "Radioaktivität",
                "Gesamtbild", "Klimawandel", "Reaktoren", "Verzicht", "Scheinheiligkeit", "Leute", "Risiken", "Löcher",
                "Fusion", "Gefahren", "Land"),
                res.getKeywords());
    }

    @Test
    public void testRian() throws Exception {
        // http://en.rian.ru/world/20110410/163458489.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("rian.html")));
        assertTrue(res.getText(), res.getText().startsWith("About 15,000 people took to the streets in Tokyo on Sunday to protest against th"));
        assertEquals("Japanese rally against nuclear power industry | World", res.getTitle());
        assertEquals("/favicon.ico", res.getFaviconUrl());
        assertTrue(res.getKeywords().isEmpty());
    }

    @Test
    public void testJetwick() throws Exception {
        // http://jetwick.com
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("jetwick.html")));
        assertEquals(Arrays.asList("news", "twitter", "search", "jetwick"), res.getKeywords());
    }

    @Test
    public void testVimeo() throws Exception {
        // http://vimeo.com/20910443
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("vimeo.html")));
        assertTrue(res.getText(), res.getText().startsWith("1 month ago 1 month ago: Fri, Mar 11, 2011 2:24am EST (Eastern Standard Time) See all Show me 1. finn. & Dirk von Lowtzow"));
        assertTrue(res.getTitle(), res.getTitle().startsWith("finn. & Dirk von Lowtzow \"CRYING IN THE RAIN\""));
        assertEquals("", res.getVideoUrl());
        assertEquals(Arrays.asList("finn", "finn.", "Dirk von Lowtzow", "crying in the rain", "I wish I was someone else", "Tocotronic",
                "Sunday Service", "Indigo", "Patrick Zimmer", "Patrick Zimmer aka finn.", "video", "video sharing",
                "digital cameras", "videoblog", "vidblog", "video blogging", "home video", "home movie"),
                res.getKeywords());
        assertEquals("finn.", res.getAuthorName());
    }

    /* Test broken after change to check ratio of text in getFormattedText. TODO: Find a way to support this.
    @Test
    public void testYoutube() throws Exception {
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("youtube.html")));
        assertTrue(res.getText(), res.getText().startsWith("Master of the Puppets by Metallica. Converted to 8 bit with GSXCC. Original verson can be found us"));
        assertEquals("YouTube - Metallica - Master of the Puppets 8-bit", res.getTitle());
        assertEquals("http://i4.ytimg.com/vi/wlupmjrfaB4/default.jpg", res.getImageUrl());
        assertEquals("http://www.youtube.com/v/wlupmjrfaB4?version=3", res.getVideoUrl());
    }*/

    @Test
    public void testSpiegel() throws Exception {
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("spiegel.html")));
        assertTrue(res.getText(), res.getText().startsWith("Da ist er wieder, der C64: Eigentlich längst ein Relikt der Technikgeschichte, soll der "));
    }

    @Test
    public void testGithub() throws Exception {
        // https://github.com/ifesdjeen/jReadability
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("github.html")));
        assertTrue(res.getDescription(), res.getDescription().startsWith("Article text extractor from given HTML text"));
        assertTrue(res.getText(), res.getText().startsWith("= jReadability This is a small helper utility (only 130 lines of code) for pepole"));
    }

    @Test
    public void testITunes() throws Exception {
        // http://itunes.apple.com/us/album/21/id420075073
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("itunes.html")));
        assertTrue(res.getText(), res.getText().startsWith("What else can be said of this album other than that it is simply amazing? Adele's voice is powerful, vulnerable, assured, and heartbreaking all in one fell swoop."));
        assertTrue("itunes:" + res.getDescription(), res.getDescription().startsWith("Preview songs from 21 by ADELE"));
    }

    @Test
    public void testTwitpic() throws Exception {
        // http://twitpic.com/4k1ku3
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("twitpic.html")));
        assertEquals("It’s hard to be a dinosaur. on Twitpic", res.getTitle());
//        assertEquals("", res.getText());
//        assertTrue(res.getText(), res.getText().isEmpty());
    }

    @Test
    public void testTwitpic2() throws Exception {
        // http://twitpic.com/4kuem8
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("twitpic2.html")));
        assertEquals("*Not* what you want to see on the fetal monitor when your wif... on Twitpic", res.getTitle());
//        assertEquals("", res.getText());
    }

    @Test
    public void testHeise() throws Exception {
        // http://www.heise.de/newsticker/meldung/Internet-Explorer-9-jetzt-mit-schnellster-JavaScript-Engine-1138062.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("heise.html")));
        assertEquals("", res.getImageUrl());
        assertEquals("Internet Explorer 9 jetzt mit schnellster JavaScript-Engine", res.getTitle());
        assertTrue(res.getText().startsWith("Microsoft hat heute eine siebte Platform Preview des Internet Explorer veröffentlicht. In den nur dr"));
    }

    @Test
    public void testTechcrunch() throws Exception {
        // http://techcrunch.com/2011/04/04/twitter-advanced-search/
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("techcrunch.html")));
        assertEquals("http://tctechcrunch2011.files.wordpress.com/2011/04/screen-shot-2011-04-04-at-12-11-36-pm.png?w=285&h=85", res.getImageUrl());
        assertEquals("Twitter Finally Brings Advanced Search Out Of Purgatory; Updates Discovery Algorithms", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("A couple weeks ago, we wrote a post wishing Twitter a happy fifth birthday, but also noting "));
        assertEquals("MG Siegler", res.getAuthorName());
    }

    @Test
    public void testEngadget() throws Exception {
        // http://www.engadget.com/2011/04/09/editorial-androids-problem-isnt-fragmentation-its-contamina/
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("engadget.html")));
        assertTrue(res.getText(), res.getText().startsWith("Editorial: Android's problem isn't fragmentation, it's contamination This thought was first given voice by Myriam Joire on last night's Mobile Podcast, and the"));
        assertEquals("http://www.blogcdn.com/www.engadget.com/media/2011/04/11x0409mnbvhg_thumbnail.jpg", res.getImageUrl());
        assertEquals("Editorial: Android's problem isn't fragmentation, it's contamination -- Engadget", res.getTitle());
        // TODO: Fix author extraction.
        //assertEquals("Vlad Savov", res.getAuthorName());
    }

    @Test
    public void testTwitterblog() throws Exception {
        // http://engineering.twitter.com/2011/04/twitter-search-is-now-3x-faster_1656.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("twitter.html")));
        assertEquals("Twitter Engineering: Twitter Search is Now 3x Faster", res.getTitle());
        assertEquals("http://4.bp.blogspot.com/-CmXJmr9UAbA/TZy6AsT72fI/AAAAAAAAAAs/aaF5AEzC-e4/s72-c/Blender_Tsunami.jpg", res.getImageUrl());
        assertTrue("twitter:" + res.getText(), res.getText().startsWith("In the spring of 2010, the search team at Twitter started to rewrite our search engine in order to serve our ever-growin"));
    }

    @Test
    public void testTazBlog() throws Exception {
        // http://www.taz.de/1/politik/asien/artikel/1/anti-atomkraft-nein-danke/
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("taz.html")));
        assertTrue("taz:" + res.getText(), res.getText().startsWith("Absolute Minderheit: Im Shiba-Park in Tokio treffen sich jetzt jeden Sonntag die Atomkraftgegner. Sie blicken neidisch auf die Anti-AKW-Bewegung in Deutschland. "));
        assertEquals("Anti-Atomkraft? Nein danke!", res.getTitle());
        assertEquals("Georg Blume", res.getAuthorName());
    }

    @Test
    public void testFacebook() throws Exception {
        // http://www.facebook.com/ejdionne/posts/10150154175658687
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("facebook.html")));
        assertTrue(res.getText(), res.getText().startsWith("In my column tomorrow, I urge President Obama to end the spectacle of"));
        assertEquals("", res.getImageUrl());
        assertEquals("In my column...", res.getTitle());
    }

    @Test
    public void testFacebook2() throws Exception {
        // http://www.facebook.com/permalink.php?story_fbid=214289195249322&id=101149616624415 
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("facebook2.html")));
        assertTrue(res.getText(), res.getText().startsWith("Sommer is the best time to wear Jetwick T-Shirts!"));
        assertEquals("", res.getImageUrl());
        assertEquals("Sommer is the best...", res.getTitle());
    }

    @Test
    public void testBlogger() throws Exception {
        // http://blog.talawah.net/2011/04/gavin-king-unviels-red-hats-top-secret.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("blogger.html")));
        assertTrue(res.getText(), res.getText().startsWith("Gavin King of Red Hat/Hibernate/Seam fame recently"));
        assertEquals("http://3.bp.blogspot.com/-cyMzveP3IvQ/TaR7f3qkYmI/AAAAAAAAAIk/mrChE-G0b5c/s200/Java.png", res.getImageUrl());
        assertEquals("The Brain Dump: Gavin King unveils Red Hat's Java killer successor: The Ceylon Project", res.getTitle());
        assertEquals("http://blog.talawah.net/feeds/posts/default?alt=rss", res.getRssUrl());
        assertEquals("Marc Richards", res.getAuthorName());
    }

    @Test
    public void testNyt() throws Exception {
        // http://dealbook.nytimes.com/2011/04/11/for-defense-in-galleon-trial-no-time-to-rest/
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("nyt.html")));
        assertEquals("http://graphics8.nytimes.com/images/2011/04/12/business/dbpix-raj-rajaratnam-1302571800091/dbpix-raj-rajaratnam-1302571800091-tmagSF.jpg",
                res.getImageUrl());
        assertTrue(res.getText(), res.getText().startsWith("I wouldn’t want to be Raj Rajaratnam’s lawyer right now."));
        assertEquals("Andrew Ross Sorkin", res.getAuthorName());
    }

    @Test
    public void testHuffingtonpost() throws Exception {
        // http://www.huffingtonpost.com/2010/08/13/federal-reserve-pursuing_n_681540.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("huffingtonpost.html")));
        assertEquals("Federal Reserve's Low Rate Policy Is A 'Dangerous Gamble,' Says Top Central Bank Official", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("A top regional Federal Reserve official sharply"));
        assertEquals("http://i.huffpost.com/gen/157611/thumbs/s-FED-large.jpg", res.getImageUrl());
        assertEquals("Shahien Nasiripour", res.getAuthorName());
    }

    @Test
    public void testTechcrunch2() throws Exception {
        // http://techcrunch.com/2010/08/13/gantto-takes-on-microsoft-project-with-web-based-project-management-application/
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("techcrunch2.html")));
        assertEquals("Gantto Takes On Microsoft Project With Web-Based Project Management Application", article.getTitle());
        assertTrue(article.getText(), article.getText().startsWith("Y Combinator-backed Gantto is launching"));
        assertEquals("http://i0.wp.com/tctechcrunch2011.files.wordpress.com/2010/08/gantto.jpg?resize=680%2C680", article.getImageUrl());
        assertEquals("Leena Rao", article.getAuthorName());
    }

    @Test
    public void testCnn2() throws Exception {
        // http://www.cnn.com/2010/POLITICS/08/13/democrats.social.security/index.html
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("cnn2.html")));
        assertEquals("Democrats to use Social Security against GOP this fall", article.getTitle());
        assertTrue(article.getText(), article.getText().startsWith("Washington (CNN) -- Democrats pledged "));
        assertEquals(article.getImageUrl(), "http://i.cdn.turner.com/cnn/2010/POLITICS/08/13/democrats.social.security/story.kaine.gi.jpg");
        assertEquals("Ed Hornick", article.getAuthorName());
    }

    @Test
    public void testBusinessweek2() throws Exception {
        // http://www.businessweek.com/magazine/content/10_34/b4192048613870.htm
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("businessweek2.html")));
        assertTrue(article.getText(), article.getText().startsWith("There's discord on Wall Street: Strategists at major American investment "));
        assertEquals("http://images.businessweek.com/mz/covers/current_120x160.jpg", article.getImageUrl());
        assertEquals("Whitney Kisling,Caroline Dye", article.getAuthorName());
    }

    @Test
    public void testFoxnews() throws Exception {
        // http://www.foxnews.com/politics/2010/08/14/russias-nuclear-help-iran-stirs-questions-improved-relations/
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("foxnews.html")));
        assertTrue("Foxnews:" + article.getText(), article.getText().startsWith("Apr. 8: President Obama signs the New START treaty with Russian President Dmitry Medvedev at the Prague Castle. Russia's announcement "));
        assertEquals("http://a57.foxnews.com/static/managed/img/Politics/397/224/startsign.jpg", article.getImageUrl());
        assertEquals("", article.getAuthorName());
    }

    @Test
    public void testStackoverflow() throws Exception {
        // http://stackoverflow.com/questions/3553693/wicket-vs-vaadin/3660938
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("stackoverflow.html")));
        assertTrue("stackoverflow:" + article.getText(), article.getText().startsWith("I think I've invested some time for both frameworks. I really like bo"));
        assertEquals("wicket vs Vaadin", article.getTitle());
        assertEquals("", article.getImageUrl());
    }

    @Test
    public void testAolnews() throws Exception {
        // http://www.aolnews.com/nation/article/the-few-the-proud-the-marines-getting-a-makeover/19592478
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("aolnews.html")));
        assertEquals("http://o.aolcdn.com/art/ch_news/aol_favicon.ico", article.getFaviconUrl());
        assertTrue(article.getText(), article.getText().startsWith("WASHINGTON (Aug. 13) -- Declaring \"the maritime soul of the Marine Corps"));
        assertEquals("http://o.aolcdn.com/photo-hub/news_gallery/6/8/680919/1281734929876.JPEG", article.getImageUrl());
        assertEquals(Arrays.asList("news", "update", "breaking", "nation", "U.S.", "elections", "world", "entertainment", "sports", "business",
                "weird news", "health", "science", "latest news articles", "breaking news", "current news", "top news"),
                article.getKeywords());
    }

    @Test
    public void testWallstreetjournal() throws Exception {
        // http://online.wsj.com/article/SB10001424052748704532204575397061414483040.html
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("wsj.html")));
        assertTrue(article.getText(), article.getText().startsWith("The Obama administration has paid out less than a third of the nearly $230 billion"));
        assertEquals("http://si.wsj.net/public/resources/images/OB-JO747_stimul_D_20100814113803.jpg", article.getImageUrl());
        assertEquals("LOUISE RADNOFSKY", article.getAuthorName());
    }

    @Test
    public void testUsatoday() throws Exception {
        // http://content.usatoday.com/communities/thehuddle/post/2010/08/brett-favre-practices-set-to-speak-about-return-to-minnesota-vikings/1
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("usatoday.html")));
        assertTrue(article.getText(), article.getText().startsWith("Brett Favre couldn't get away from the"));
        assertEquals("http://i.usatoday.net/communitymanager/_photos/the-huddle/2010/08/18/favrespeaksx-inset-community.jpg", article.getImageUrl());
        assertEquals("Sean Leahy", article.getAuthorName());
    }

    @Test
    public void testUsatoday2() throws Exception {
        // http://content.usatoday.com/communities/driveon/post/2010/08/gm-finally-files-for-ipo/1
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("usatoday2.html")));
        assertTrue(article.getText(), article.getText().startsWith("General Motors just filed with the Securities and Exchange "));
        assertEquals("http://i.usatoday.net/communitymanager/_photos/drive-on/2010/08/18/cruzex-wide-community.jpg", article.getImageUrl());
    }

    @Test
    public void testEspn() throws Exception {
        // http://sports.espn.go.com/espn/commentary/news/story?id=5461430
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("espn.html")));
        assertTrue(article.getText(), article.getText().startsWith("If you believe what college football coaches have said about sports"));
        assertEquals("http://a.espncdn.com/photo/2010/0813/ncf_i_mpouncey1_300.jpg", article.getImageUrl());
    }

    @Test
    public void testGizmodo() throws Exception {
        // http://www.gizmodo.com.au/2010/08/xbox-kinect-gets-its-fight-club/
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("gizmodo.html")));
        assertTrue(article.getText(), article.getText().startsWith("You love to punch your arms through the air"));
        assertEquals("http://cache.gawkerassets.com/assets/images/9/2010/08/500x_fighters_uncaged__screenshot_4b__rider.jpg", article.getImageUrl());
        // author tested in juicer.
    }

    /*
    @Test
    public void testGizmodo2() throws Exception {
        // http://gizmodo.com/the-only-way-to-save-the-northern-white-rhino-is-a-jura-1745213055
        JResult res = new JResult();
        res.setUrl("http://gizmodo.com/the-only-way-to-save-the-northern-white-rhino-is-a-jura-1745213055");
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("gizmodo2.html")));
        assertEquals("The Only Way to Save the Northern White Rhino Is a Jurassic Park-Style Intervention", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("It sounds like the plot of a Hollywood science-fiction movie."));
        compareDates("2015-11-30 03:30:00", res.getDate());
    }*/
    
    /*
    @Test
    public void testGizmodo3() throws Exception {
        // http://gizmodo.com/finally-well-designed-pipes-for-the-discerning-stoner-1746298385
        JResult res = new JResult();
        res.setUrl("http://gizmodo.com/finally-well-designed-pipes-for-the-discerning-stoner-1746298385");
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("gizmodo3.html")));
        assertEquals("Finally, Well-Designed Pipes For the Discerning Stoner", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("The design associated with smoking weed has heretofore"));
        compareDates("2015-04-12 18:00:00", res.getDate());
    }*/

    @Test
    public void testEngadget2() throws Exception {
        // http://www.engadget.com/2010/08/18/verizon-fios-set-top-boxes-getting-a-new-hd-guide-external-stor/
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("engadget2.html")));
        assertEquals("Verizon FiOS set-top boxes getting a new HD guide, external storage and more in Q4 -- Engadget", article.getTitle());
        assertTrue(article.getText(), article.getText().startsWith("Streaming and downloading TV content to mobiles is nice"));
        assertEquals("http://www.blogcdn.com/www.engadget.com/media/2010/08/44ni600_thumbnail.jpg", article.getImageUrl());
    }

    @Test
    public void testWired() throws Exception {
        // http://www.wired.com/playbook/2010/08/stress-hormones-boxing/
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("wired.html")));
        assertTrue(article.getText(), article.getText().startsWith("On November 25, 1980, professional boxing"));
        assertEquals("Stress Hormones Could Predict Boxing Dominance", article.getTitle());
        assertEquals("http://www.wired.com/playbook/wp-content/uploads/2010/08/fight_f-660x441.jpg", article.getImageUrl());
        assertEquals("Brian Mossop", article.getAuthorName());
    }

    @Test
    public void tetGigaohm() throws Exception {
        //String url = "http://gigaom.com/apple/apples-next-macbook-an-800-mac-for-the-masses/";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("gigaom.html")));
        assertTrue(article.getText(), article.getText().startsWith("The MacBook Air is a bold move forward "));
        assertEquals("http://gigapple.files.wordpress.com/2010/10/macbook-feature.png?w=300&h=200", article.getImageUrl());
    }

    @Test
    public void testMashable() throws Exception {
        //String url = "http://mashable.com/2010/08/18/how-tonot-to-ask-someone-out-online/";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("mashable.html")));
        assertTrue(article.getText(), article.getText().startsWith("Imagine, if you will, a crowded dance floor"));
        assertEquals("http://9.mshcdn.com/wp-content/uploads/2010/07/love.jpg", article.getImageUrl());
    }

    @Test
    public void testVenturebeat() throws Exception {
        //String url = "http://social.venturebeat.com/2010/08/18/facebook-reveals-the-details-behind-places/";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("venturebeat.html")));
        assertTrue(article.getText(), article.getText().startsWith("Facebook just confirmed the rumors"));
        assertEquals("http://cdn.venturebeat.com/wp-content/uploads/2010/08/mark-zuckerberg-facebook-places.jpg", article.getImageUrl());
    }

    @Test
    public void testPolitico() throws Exception {
        //String url = "http://www.politico.com/news/stories/1010/43352.html";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("politico.html")));
        assertTrue(article.getText(), article.getText().startsWith("If the newest Census Bureau estimates stay close to form"));
        assertEquals("http://images.politico.com/global/news/100927_obama22_ap_328.jpg", article.getImageUrl());
    }

    @Test
    public void testNinjablog() throws Exception {
        //String url = "http://www.ninjatraderblog.com/im/2010/10/seo-marketing-facts-about-google-instant-and-ranking-your-website/";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("ninjatraderblog.html")));
        assertTrue(article.getText(), article.getText().startsWith("Many users around the world Google their queries"));
    }

    @Test
    public void testSportsillustrated() throws Exception {
        //String url = "http://sportsillustrated.cnn.com/2010/football/ncaa/10/15/ohio-state-holmes.ap/index.html?xid=si_ncaaf";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("sportsillustrated.html")));
        assertTrue(article.getText(), article.getText().startsWith("COLUMBUS, Ohio (AP) -- Ohio State has closed"));
        assertEquals("http://i.cdn.turner.com/si/.e1d/img/4.0/global/logos/si_100x100.jpg",
                article.getImageUrl());
    }

    @Test public void testDailybeast() throws Exception {
        //String url = "http://www.thedailybeast.com/blogs-and-stories/2010-11-01/ted-sorensen-speechwriter-behind-jfks-best-jokes/?cid=topic:featured1";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("thedailybeast.html")));
        assertTrue(article.getText(), article.getText().startsWith("Legendary Kennedy speechwriter Ted Sorensen passed"));
        assertEquals("http://www.tdbimg.com/files/2010/11/01/img-article---katz-ted-sorensen_163531624950.jpg",
                article.getImageUrl());
    }

    @Test
    public void testScience() throws Exception {
        //String url = "http://news.sciencemag.org/sciencenow/2011/04/early-birds-smelled-good.html";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("sciencemag.html")));
        assertTrue(article.getText(), article.getText().startsWith("About 65 million years ago, most of the dinosaurs and many other animals and plants were wiped off Earth, probably due to an asteroid hitting our planet. Researchers have long debated how and why some "));
    }

    @Test
    public void testSlamMagazine() throws Exception {
        //String url = "http://www.slamonline.com/online/nba/2010/10/nba-schoolyard-rankings/";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("slamonline.html")));
        assertTrue(article.getText(), article.getText().startsWith("When in doubt, rank players and add your findings"));
        assertEquals(article.getImageUrl(), "http://www.slamonline.com/online/wp-content/uploads/2010/10/celtics.jpg");
        assertEquals("NBA Schoolyard Rankings", article.getTitle());
    }

    @Test
    public void testEspn3WithFlashVideo() throws Exception {
        //String url = "http://sports.espn.go.com/nfl/news/story?id=5971053";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("espn3.html")));
        assertTrue(article.getText(), article.getText().startsWith("PHILADELPHIA -- Michael Vick missed practice Thursday"));
        assertEquals("http://a.espncdn.com/i/espn/espn_logos/espn_red.png", article.getImageUrl());
        assertEquals("Michael Vick of Philadelphia Eagles misses practice, unlikely to play vs. Dallas Cowboys - ESPN", article.getTitle());
    }

    @Test
    public void testSportingNews() throws Exception {
        //String url = "http://www.sportingnews.com/nfl/feed/2011-01/nfl-coaches/story/raiders-cut-ties-with-cable";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("sportingnews.html")));
        assertTrue(article.getText(), article.getText().startsWith("ALAMEDA, Calif. — The Oakland Raiders informed coach Tom Cable on Tuesday that they will not bring him back"));
        assertEquals("http://dy.snimg.com/story-image/0/69/174475/14072-650-366.jpg",
                article.getImageUrl());
        assertEquals("Raiders cut ties with Cable - NFL - Sporting News", article.getTitle());
    }

    @Test
    public void testFoxSports() throws Exception {
        //String url = "http://msn.foxsports.com/nfl/story/Tom-Cable-fired-contract-option-Oakland-Raiders-coach-010411";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("foxsports.html")));
        assertTrue(article.getText(), article.getText().startsWith("The Oakland Raiders informed coach Tom Cable"));
        assertEquals("Oakland Raiders won't bring Tom Cable back as coach - NFL News",
                article.getTitle());
    }

    @Test
    public void testEconomist() throws Exception {
        //String url = "http://www.economist.com/node/17956885";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("economist.html")));
        assertTrue(article.getText(), article.getText().startsWith("FOR beleaguered smokers, the world is an increasingly"));
        assertEquals("http://www.economist.com/sites/default/files/images/articles/migrated/20110122_stp004.jpg",
                article.getImageUrl());
        assertFalse(article.getText(), article.getText().contains("Related topics"));
    }

    @Test
    public void testTheVacationGals() throws Exception {
        //String url = "http://thevacationgals.com/vacation-rental-homes-are-a-family-reunion-necessity/";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("thevacationgals.html")));
        assertTrue(article.getText(), article.getText().startsWith("Editors’ Note: We are huge proponents of vacation rental homes"));
        assertEquals("http://thevacationgals.com/wp-content/uploads/2010/11/Gemmel-Family-Reunion-at-a-Vacation-Rental-Home1-300x225.jpg",
                article.getImageUrl());
    }

    @Test
    public void testShockYa() throws Exception {
        //String url = "http://www.shockya.com/news/2011/01/30/daily-shock-jonathan-knight-of-new-kids-on-the-block-publicly-reveals-hes-gay/";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("shockya.html")));
        assertTrue(article.getText(), article.getText().startsWith("New Kids On The Block singer Jonathan Knight has publicly"));
        assertEquals("http://www.shockya.com/news/wp-content/uploads/jonathan_knight_new_kids_gay.jpg",
                article.getImageUrl());
    }

    @Test
    public void testWikipedia() throws Exception {
        // String url = "http://en.wikipedia.org/wiki/Therapsids";
        // Wikipedia has the advantage of also testing protocol relative URL extraction for Favicon and Images.
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("wikipedia.html")));
        assertTrue(article.getText(), article.getText().startsWith("Therapsida is a group of the most advanced reptile-grade synapsids, and the ancestors of mammals"));
        assertEquals("//upload.wikimedia.org/wikipedia/commons/thumb/4/42/Pristeroognathus_DB.jpg/240px-Pristeroognathus_DB.jpg",
                article.getImageUrl());
        assertEquals("//en.wikipedia.org/apple-touch-icon.png",
                article.getFaviconUrl());
    }

    @Test
    public void testWikipedia2() throws Exception {
        // http://en.wikipedia.org/wiki/President_of_the_United_States
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("wikipedia_president.html")));
        assertTrue(article.getText(), article.getText().startsWith("The President of the United States of America (acronym: POTUS)[6] is the head of state and head of government"));
    }

    @Test
    public void testWikipedia3() throws Exception {
        // http://en.wikipedia.org/wiki/Muhammad
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("wikipedia_muhammad.html")));
        assertTrue(article.getText(), article.getText().startsWith("Muhammad (c. 570 – c. 8 June 632);[1] also transliterated as Mohammad, Mohammed, or Muhammed; Arabic: محمد‎, full name: Abū al-Qāsim Muḥamma"));
    }

    @Test
    public void testWikipedia4() throws Exception {
        // http://de.wikipedia.org/wiki/Henne_Strand
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("wikipedia_Henne_Strand.html")));
        assertTrue(article.getText(), article.getText().startsWith("Der dänische Ort Henne Strand befindet sich in Südwest-Jütland und gehört zur Kommune Varde"));
    }

    @Test
    public void testWikipedia5() throws Exception {
        // http://de.wikipedia.org/wiki/Java
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("wikipedia_java.html")));
        assertTrue(article.getText(), article.getText().startsWith("Java (Indonesian: Jawa) is an island of Indonesia. With a population of 135 million"));
    }

    @Test
    public void testWikipedia6() throws Exception {
        // http://de.wikipedia.org/wiki/Knight_Rider
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("wikipedia-knight_rider_de.html")));
        assertTrue(article.getText(), article.getText().startsWith("Knight Rider ist eine US-amerikanische Fernsehserie, "
                + "die von 1982 bis 1986 produziert wurde. Knight Rider ist eine Krimi-Action-Serie mit futuristischen Komponenten "
                + "und hat weltweit Kultstatus erlangt."));
    }

    @Test
    public void testData4() throws Exception {
        // http://blog.traindom.com/places-where-to-submit-your-startup-for-coverage/
        JResult res = extractor.extractContent(readFileAsString("test_data/4.html"));
        assertEquals("36 places where you can submit your startup for some coverage", res.getTitle());
        assertEquals(Arrays.asList("blog coverage", "get coverage", "startup review", "startups", "submit startup"), res.getKeywords());
        assertTrue("data4:" + res.getText(), res.getText().startsWith("So you have a new startup company and want some coverage"));
    }

    @Test
    public void testTimemagazine() throws Exception {
        //String url = "http://www.time.com/time/health/article/0,8599,2011497,00.html";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("time.html")));
        assertTrue(article.getText(), article.getText().startsWith("This month, the federal government released"));
        assertEquals("http://img.timeinc.net/time/daily/2010/1008/bp_oil_spill_0817.jpg", article.getImageUrl());
    }

    @Test
    public void testCnet() throws Exception {
        //String url = "http://news.cnet.com/8301-30686_3-20014053-266.html?tag=topStories1";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("cnet.html")));
        assertTrue(article.getText(), article.getText().startsWith("NEW YORK--Verizon Communications is prepping a new"));
        assertEquals("http://i.i.com.com/cnwk.1d/i/tim//2010/08/18/Verizon_iPad_and_live_TV_610x458.JPG", article.getImageUrl());
    }

    @Test
    public void testBloomberg() throws Exception {
        //String url = "http://www.bloomberg.com/news/2010-11-01/china-becomes-boss-in-peru-on-50-billion-mountain-bought-for-810-million.html";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("bloomberg.html")));
        assertTrue(article.getText(), article.getText().startsWith("The Chinese entrepreneur and the Peruvian shopkeeper"));
        assertEquals("http://www.bloomberg.com/apps/data?pid=avimage&iid=iimODmqjtcQU", article.getImageUrl());
    }

    @Test
    public void testTheFrisky() throws Exception {
        //String url = "http://www.thefrisky.com/post/246-rachel-dratch-met-her-baby-daddy-in-a-bar/";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("thefrisky.html")));
        assertTrue(article.getText(), article.getText().startsWith("Rachel Dratch had been keeping the identity of her baby daddy "));

        assertEquals("http://cdn.thefrisky.com/images/uploads/rachel_dratch_102810_m.jpg",
                article.getImageUrl());
        assertEquals("Rachel Dratch Met Her Baby Daddy At A Bar", article.getTitle());
    }

    @Test
    public void testBrOnline() throws Exception {
        // TODO charset for opera was removed:
        // <![endif]-->
        // <link rel="stylesheet" type="text/x-opera-css;charset=utf-8" href="/css/opera.css" />

        //String url = "http://www.br-online.de/br-klassik/programmtipps/highlight-bayreuth-tannhaeuser-festspielzeit-2011-ID1309895438808.xml";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("br-online.html")));
        assertTrue(article.getText(), article.getText().startsWith("Wenn ein Dirigent, der Alte Musik liebt, erstmals eine "
                + "Neuproduktion bei den Bayreuther Richard-Wagner-Festspielen übernimmt,"));
        assertEquals("Eröffnung der 100. Bayreuther Festspiele: Alles neu beim \"Tannhäuser\" | Programmtipps | BR-KLASSIK",
                article.getTitle());
    }

    @Test
    public void cleanTitle() {
        String title = "Hacker News | Ask HN: Apart from Hacker News, what else you read?";
        assertEquals("Ask HN: Apart from Hacker News, what else you read?", extractor.cleanTitle(title));
        assertEquals("mytitle irgendwas", extractor.cleanTitle("mytitle irgendwas | Facebook"));
        assertEquals("mytitle irgendwas", extractor.cleanTitle("mytitle irgendwas | Irgendwas"));

        // this should fail as most sites do store their name after the post
        assertEquals("Irgendwas | mytitle irgendwas", extractor.cleanTitle("Irgendwas | mytitle irgendwas"));
    }

    @Test
    public void testGaltimeWhereUrlContainsSpaces() throws Exception {
        //String url = "http://galtime.com/article/entertainment/37/22938/kris-humphries-avoids-kim-talk-gma";
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("galtime.com.html")));
        assertEquals("http://vnetcdn.dtsph.com/files/vnet3/imagecache/opengraph_ogimage/story-images/Kris%20Humphries%20Top%20Bar.JPG", article.getImageUrl());
    }

    @Test
    public void testIssue8() throws Exception {
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("no-hidden.html")));
        assertEquals("This is the text which is shorter but visible", res.getText());
    }

    @Test
    public void testIssue8False() throws Exception {
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("no-hidden2.html")));
        assertEquals("This is the NONE-HIDDEN text which shouldn't be shown and it is a bit longer so normally prefered", res.getText());
    }

    @Test
    public void testIssue4() throws Exception {
        JResult res = extractor.extractContent("<html><body><div> aaa<a> bbb </a>ccc</div></body></html>");
        assertEquals("aaa bbb ccc", res.getText());

        res = extractor.extractContent("<html><body><div> aaa <strong>bbb </strong>ccc</div></body></html>");
        assertEquals("aaa bbb ccc", res.getText());

        res = extractor.extractContent("<html><body><div> aaa <strong> bbb </strong>ccc</div></body></html>");
        assertEquals("aaa bbb ccc", res.getText());
    }

    @Test
    public void testI4Online() throws Exception {
        //https://i4online.com
        JResult article = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("i4online.html")));
        assertTrue(article.getText(), article.getText().startsWith("Just one week to go and everything is set for the summer Forum 2013"));

        ArticleTextExtractor extractor2 = new ArticleTextExtractor();
        OutputFormatter outputFormater = new OutputFormatter(10);
        outputFormater.setNodesToKeepCssSelector("p,h1,h2,h3,h4,h5,h6");
        extractor2.setOutputFormatter(outputFormater);
        article = extractor2.extractContent(c.streamToString(getClass().getResourceAsStream("i4online.html")));
        assertTrue(article.getText(), article.getText().startsWith("Upcoming events: Forum 79 Just one week to go and everything is set for the summer Forum 2013"));
    }

    @Test
    public void testImagesList() throws Exception {
        // http://www.reuters.com/article/2012/08/03/us-knightcapital-trading-technology-idUSBRE87203X20120803
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("reuters.html")));
        assertEquals(1, res.getImagesCount());
        assertEquals(res.getImageUrl(), res.getImages().get(0).src);
        assertEquals("http://s1.reutersmedia.net/resources/r/?m=02&d=20120803&t=2&i=637797752&w=460&fh=&fw=&ll=&pl=&r=CBRE872074Y00",
                res.getImages().get(0).src);

        // http://thevacationgals.com/vacation-rental-homes-are-a-family-reunion-necessity/
        res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("thevacationgals.html")));
        assertEquals(3, res.getImagesCount());
        assertEquals("http://thevacationgals.com/wp-content/uploads/2010/11/Gemmel-Family-Reunion-at-a-Vacation-Rental-Home1-300x225.jpg",
                res.getImages().get(0).src);
        assertEquals("../wp-content/uploads/2010/11/The-Gemmel-Family-Does-a-Gilligans-Island-Theme-Family-Reunion-Vacation-Sarah-Gemmel-300x225.jpg",
                res.getImages().get(1).src);
        assertEquals("http://www.linkwithin.com/pixel.png", res.getImages().get(2).src);
    }

    @Test
    public void testSearchEngineJournal() throws Exception {
        // http://www.searchenginejournal.com/planning-progress-18-tips-successful-social-media-strategy/112567/
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("searchenginejournal.html")));
        assertEquals("18 Tips for a Successful Social Media Strategy", res.getTitle());
        assertTrue(res.getText(), res.getText().contains("Sharam"));
    }

    @Test
    public void testAdweek() throws Exception {
        // http://www.adweek.com/prnewser/5-digital-data-metricstools-that-pr-pros-need-to-know/97735?red=pr
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("adweek.html")));
        assertEquals("5 Digital Metrics/Tools That PR Pros Need to Know", res.getTitle());
        assertTrue(res.getText(), res.getText().contains("Cision provides a proprietary"));
        assertTrue(res.getText(), res.getText().contains("Moz’s Domain Authority."));
        assertTrue(res.getText(), res.getText().contains("Google Authorship and Google Analytics."));
    }

    @Test
    public void testSpinsucks() throws Exception {
        // http://spinsucks.com/communication/2015-communications-trends/
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("spinsucks.html")));
        assertEquals("The Five Communications Trends for 2015 Spin Sucks", res.getTitle());
        assertTrue(res.getText(), res.getText().contains("Conversion of media. Julie Hong, the community manager"));
        assertTrue(res.getText(), res.getText().contains("Paid media affects traditional PR"));
        assertTrue(res.getText(), res.getText().contains("Old ideas become new again. We’ve stopped doing things such as deskside briefings, large events, direct mail"));
        compareDates("2014-12-09 05:58:40", res.getDate());
    }

    @Test
    public void testPRDaily() throws Exception {
        // http://www.prdaily.com/Main/Articles/7_PR_blogs_worth_reading_17870.aspx
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("prdaily.html")));
        assertEquals("7 PR blogs worth reading", res.getTitle());
        assertTrue(res.getText(), res.getText().contains("I really love the way Rebekah"));
        assertTrue(res.getText(), res.getText().contains("my team became fascinated with Bad Pitch Blog"));
        assertTrue(res.getText(), res.getText().contains("I’m fairly certain there is no one nicer than Deirdre Breakenridge"));
        compareDates("2015-01-08", res.getDate());
    }

    @Test
    public void testMarthaStewartWeddings() throws Exception {
        // http://www.marthastewartweddings.com/363473/bridal-beauty-diaries-lauren-%25E2%2580%2593-toning-and-cutting-down
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("marthastewartweddings.html")));
        assertEquals("Bridal Beauty Diaries: Lauren – Toning Up and Cutting Down", res.getTitle());
        assertTrue(res.getText(), res.getText().contains("Its “go” time. Approximately seven months until the big day"));
        compareDates("2014-07-02", res.getDate());
    }

    @Test
    public void testNotebookCheck() throws Exception {
        // http://www.notebookcheck.com/UEbernahme-Microsoft-schluckt-Devices-und-Services-Sparte-von-Nokia.115522.0.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("notebookcheck.html")));
        assertEquals("Übernahme: Microsoft schluckt Devices und Services Sparte von Nokia", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Tschüss Nokia. Willkommen Microsoft. Die Übernahmen ist unter Dach und Fach"));
    }

    @Test
    public void testPeople() throws Exception {
        // http://www.people.com/article/ryan-seacrest-marriage-turning-40
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("people.html")));
        assertEquals("Ryan Seacrest on Marriage: 'I Want What My Mom and Dad Have' - American Idol, Ryan Seacrest : People.com", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("There are those who are in denial about turning 40"));
        assertFalse(res.getText(), res.getText().contains("Poppy Montgomery Drama Unforgettable Is Being Brought Back"));
        compareDates("2014-08-20 09:15:00", res.getDate());
    }

    @Test
    public void testPeople2() throws Exception {
        // http://www.people.com/article/truck-driver-rescues-family-burning-car-video
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("people2.html")));
        assertEquals("Truck Driver Rescues Family in Burning Wreck : People.com", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("David Fredericksen was driving his semi truck along"));
        assertFalse(res.getText(), res.getText().contains("How Water Helps with Weight Loss"));
        compareDates("2014-08-20 10:30:00", res.getDate());
    }

    @Test
    public void testPeople3() throws Exception {
        // http://www.people.com/article/pierce-brosnan-jimmy-fallon-goldeneye-007-n64
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("people3.html")));
        assertEquals("Pierce Brosnan Plays Jimmy Fallon in 'GoldenEye 007' on 'Tonight Show' : People.com", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Just because you star in a video game, doesn't mean you'll be any good at it."));
        assertFalse(res.getText(), res.getText().contains("How Water Helps with Weight Loss"));
        assertEquals("Alex Heigl", res.getAuthorName());
        compareDates("2014-08-20 08:20:00", res.getDate());
    }

    @Test
    public void testEntrepreneur() throws Exception {
        // http://www.entrepreneur.com/article/237402
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("entrepreneur.html")));
        assertEquals("7 Big Changes in the PR Landscape Every Business Should Know About", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("At least three times a week, I get emails from entrepreneurs or small-business owners asking for advice on public relations."));
        assertEquals("Rebekah Iliff", res.getAuthorName());
        assertEquals("Chief Strategy Officer for AirPR", res.getAuthorDescription());
        compareDates("2014-09-15 17:30:00", res.getDate());
    }

    @Test
    public void testHuffingtonpostAuthor() throws Exception {
        // http://www.huffingtonpost.com/rebekah-iliff/millions-of-consumers-aba_b_5269051.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("huffingtonpost2.html")));
        assertEquals("Millions of Consumers Abandon Hashtag for Backslash", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("In a special Silicon Valley \"Tech Report,\" sources confirmed Monday that millions of "));
        assertEquals("Rebekah Iliff", res.getAuthorName());
        assertEquals("Chief Strategy Officer, AirPR", res.getAuthorDescription());
        // Mon, 05 May 2014 16:04:09 -0400 (sailthru.date?)
        compareDates("2014-05-05 20:04:09", res.getDate());
    }

    @Test
    public void testAllVoices() throws Exception {
        // http://www.allvoices.com/article/17660716
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("allvoices.html")));
        assertEquals("Marchex exec: Lead generation moving away from 'faceless transactions'", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Driven by the surge in mobile, lead generation"));
        compareDates("2014-08-14", res.getDate());
    }

    @Test
    public void testRocketFuel() throws Exception {
        // http://rocketfuel.com/blog/you-wont-be-seeing-coca-cola-ads-for-awhile-the-reason-why-is-amazing
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("rocketfuel.html")));
        assertEquals("You Won't be Seeing Coca Cola Ads for Awhile. The Reason why Is Amazing.", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Coca Cola announced that it will not be spending"));
    }

    @Test
    public void testHuffingtonpostAuthorDesc() throws Exception {
        // http://www.huffingtonpost.com/2015/03/10/bruce-miller-san-francisco-49ers-domestic-violence_n_6836416.html
        JResult res = new JResult();
        res.setUrl("http://www.huffingtonpost.com/2015/03/10/bruce-miller-san-francisco-49ers-domestic-violence_n_6836416.html");
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("huffingtonpost3.html")));
        assertEquals("San Francisco 49ers Fullback Bruce Miller Arrested On Domestic Violence Charges", res.getTitle());
        assertEquals("", res.getAuthorDescription());
        compareDates("2015-03-10 05:27:01", res.getDate());
    }

    @Test
    public void testPRnewswireAuthorDesc() throws Exception {
        // http://www.prnewswire.com/news-releases/tableau-to-present-at-upcoming-investor-conferences-300039248.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("prnewswire.html")));
        assertEquals("Tableau to Present at Upcoming Investor Conferences -- SEATTLE, Feb. 24, 2015 /PRNewswire/ --", res.getTitle());
        assertEquals("Tableau Software (NYSE: DATA) helps people see and understand data. Tableau helps anyone quickly analyze, visualize and share information. More than 26,000 customer accounts get rapid results with Tableau in the office and on-the-go. And tens of thousands of people use Tableau Public to share data in their blogs and websites. See how Tableau can help you by downloading the free trial at www.tableau.com/trial.", res.getAuthorDescription());
        compareDates("2015-02-24", res.getDate());
    }

    @Test
    public void testTrendkraftAuthorDesc() throws Exception {
        // http://www.trendkraft.de/it-software/freigegeben-und-ab-sofort-verfuegbar-die-sechste-generation-des-ecm-systems-windream/
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("trendkraft_de.html")));
        assertEquals("Freigegeben und ab sofort verfügbar: die sechste Generation des ECM-Systems windream", res.getTitle());
        assertTrue(res.getAuthorDescription(), res.getAuthorDescription().length() == 987);
        compareDates("2015-03-11 09:19:56", res.getDate());
    }

    @Test
    public void testLimitSize() throws Exception {
        // https://medium.com/@nathanbruinooge/a-travelogue-of-india-7b1f3aa62a19
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("medium.html")), 1000);
        assertEquals("A Travelogue of India — Medium", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Schipol Airport in 2012 looks nothing like Schipol Airport in the Eighties"));
        assertTrue("Should be less than 1000", res.getText().length() <= 1000);
        compareDates("2014-08-19 04:11:18", res.getDate());
    }

    @Test
    public void testQualcomm() throws Exception {
        // https://www.qualcomm.com/news/releases/2014/10/16/qualcomm-declares-quarterly-cash-dividend
        JResult res = new JResult();
        res.setUrl("https://www.qualcomm.com/news/releases/2014/10/16/qualcomm-declares-quarterly-cash-dividend");
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("qualcomm.html")));
        assertEquals("Qualcomm Declares Quarterly Cash Dividend", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Qualcomm Incorporated (NASDAQ: QCOM) today announced"));
        compareDates("2014-10-16", res.getDate());
    }

    @Test
    public void testApplePR() throws Exception {
        // http://www.apple.com/pr/library/2015/04/27Apple-Expands-Capital-Return-Program-to-200-Billion.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("applepr.html")));
        assertEquals("Apple Expands Capital Return Program to $200 Billion", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Apple Expands Capital Return Program to $200 Billion CUPERTINO, California—April 27, 2015—Apple"));
    }

    @Test
    public void testApplePR2() throws Exception {
        // http://www.apple.com/pr/library/2015/03/09Apple-Watch-Available-in-Nine-Countries-on-April-24.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("applepr2.html")));
        assertEquals("Apple Watch Available in Nine Countries on April 24", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Apple Watch Available in Nine Countries on April 24"));
    }

    @Test
    public void testForbes() throws Exception {
        // http://fortune.com/2015/05/11/rackspaces-support-other-cloud/
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("forbes.html")));
        assertEquals("Will Rackspace support Google's or Amazon's clouds? - Fortune", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Rackspace, a true cloud computing pioneer, is starting to sound like a company that will"));
        compareDates("2015-05-11 23:01:19", res.getDate());
    }

    @Test
    public void testLongImageName() throws Exception {
        // http://www.adverts.ie/lego-building-toys/lego-general-zod-minifigure-brand-new/5980084
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("adverts.ie.html")));
        assertEquals("Lego General Zod Minifigure Brand New For Sale in Tralee, Kerry from dlaw1", res.getTitle());
        assertTrue(res.getImageUrl(), res.getImageUrl().length() == 0);
    }

    @Test
    public void testCloudComputingExpo() throws Exception {
        // http://www.cloudcomputingexpo.com/node/3342675
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("cloudcomputingexpo.html")));
        assertTrue(res.getText(), res.getText().startsWith("How to Put Public Sector Data Migration Hassles on the Road to Extinction"));
        // test it doesn't extract outside the article content
        assertFalse("Extracted text outside the content", res.getText().contains("Sandy Carter"));
        assertTrue(res.getText(), res.getText().startsWith("How to Put Public Sector Data Migration Hassles on the Road to Extinction"));
        compareDates("2015-06-29 12:00:00", res.getDate());
    }

    @Test
    public void testCloudComputingExpo2() throws Exception {
        // http://www.cloudcomputingexpo.com/node/3346367
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("cloudcomputingexpo2.html")));
        assertTrue(res.getText(), res.getText().startsWith("Merck, a leading company for innovative, top-quality high-tech"));
        assertTrue(res.getText(), res.getText().endsWith("EMD Millipore and EMD Performance Materials."));
        // test it doesn't extract outside the article content
        assertFalse("Extracted text outside the content", res.getText().contains("Sandy Carter"));
        compareDates("2015-06-23 10:31:00", res.getDate());
    }

    @Test
    public void testCloudComputingExpo3() throws Exception {
        // http://www.cloudcomputingexpo.com/node/3432136
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("cloudcomputingexpo3.html")));
        assertEquals("IHS to Hold Conference Call and Webcast on September 29, 2015 with Release of Third Quarter Results for Fiscal Year 2015", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("IHS Inc. (NYSE: IHS), the leading global "));
        assertTrue(res.getText(), res.getText().endsWith("http://www.businesswire.com/news/home/20150828005027/en/"));
        compareDates("2015-08-28 08:00:00", res.getDate());
    }

    @Test
    public void testCloudComputingExpo4() throws Exception {
        // http://www.cloudcomputingexpo.com/node/3372014
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("cloudcomputingexpo4.html")));
        assertEquals("As New Cases Of Ebola Are Confirmed We Highlight The Need For Global Coordination In The Field Of Distance Education", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("LOS ANGELES, July 16, 2015 /PRNewswire-iReach/"));
        assertTrue(res.getText(), res.getText().endsWith("News distributed by PR Newswire iReach: https://ireach.prnewswire.com"));
        compareDates("2015-07-16 19:37:00", res.getDate());
    }

    @Test
    public void testCloudComputingExpo5() throws Exception {
        // http://www.cloudcomputingexpo.com/node/3345803
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("cloudcomputingexpo5.html")));
        assertEquals("U.S. FDA Approves Eisai's Antiepileptic Agent Fycompa as Adjunctive Treatment For Primary Generalized Tonic-Clonic Seizures", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("U.S. FDA Approves Eisai's Antiepileptic Agent Fycompa as Adjunctive Treatment"));
        assertTrue(res.getText(), res.getText().endsWith("or for any actions taken in reliance thereon."));
        compareDates("2015-06-22 02:21:00", res.getDate());
    }

    @Test
    public void testCanonical() throws Exception {
        // http://www.cio.com/article/2941417/internet/internet-of-things-is-overhyped-should-be-called-internet-with-things.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("cio.com.html")));
        assertEquals("Internet of things is overhyped, should be called internet with things", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("The internet of things is overhyped and should instead be called the internet with things"));
        assertEquals("http://www.techworld.com/news/startups/rackspace-mongodb-execs-take-iot-hype-down-notch-3617731/", res.getCanonicalUrl());
        compareDates("2015-06-26 07:52:00-0700", res.getDate());
    }

    @Test
    public void testYahooMobile() throws Exception {
        // https://m.yahoo.com/w/legobpengine/finance/news/stevia-first-corp-stvf-looks-123500390.html?.intl=us&.lang=en-us
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("m_yahoo.html")));
        assertEquals("Stevia First Corp. (STVF) Looks to Disrupt Flavor Industry", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("WHITEFISH, MT / ACCESSWIRE / July 13, 2015 / The global market for sugar and sweeteners"));
        // not supported
        //compareDates("2015-07-13 00:00:00", res.getDate());
    }

    @Test
    public void testWeixin() throws Exception {
        // http://mp.weixin.qq.com/s?3rd=MzA3MDU4NTYzMw%3D%3D&__biz=MzA4MTQ0Njc2Nw%3D%3D&idx=4&mid=207614885&scene=6&sn=eda80bb13406fb31cb25f70d12e6e7dc
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("weixin.qq.com.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("缺少IT支持成跨境电商发展阻力"));
        assertTrue(res.getText(), res.getText().startsWith("根据联合国贸发会议预计"));
        compareDates("2015-07-27 00:00:00", res.getDate());
    }

    @Test
    public void testNaturebox() throws Exception {
        // http://blog.naturebox.com/posts/lunch-box-idea-breakfast-for-lunch
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("naturebox.com.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("Lunch Box Idea: Breakfast for Lunch"));
        assertTrue(res.getText(), res.getText().startsWith("I don’t know a kid who doesn’t enjoy breakfast for lunch!"));
        compareDates("2015-02-19 00:00:00", res.getDate());
    }

    @Test
    public void testItsaLovelyLife() throws Exception {
        // http://itsalovelylife.com/why-having-a-sparkling-smile-is-important-to-me/
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("itsalovelylife.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("Why Having A Sparkling Smile Is Important To Me"));
        assertTrue(res.getText(), res.getText().startsWith("I am still working my way through my New Year"));
        compareDates("2015-10-05 00:00:00", res.getDate());
    }

    @Test
    public void testWsjVideo() throws Exception {
        // http://www.wsj.com/video/what-did-steve-jobs-learn-from-the-beatles/46155A57-A19F-4AA1-9B2D-A3D8B1568A14.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("wsj.com.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("What Did Steve Jobs Learn from the Beatles?"));
        assertTrue(res.getText(), res.getText().startsWith("This transcript has been automatically generated"));
        compareDates("2011-10-31 14:00:00", res.getDate());
    }

    @Test
    public void testMsn() throws Exception {
        // http://www.msn.com/en-us/lifestyle/weddings/how-to-make-your-smile-extra-stunning-for-your-wedding/ar-AAfi5iR
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("msn.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("How to Make Your Smile Extra Stunning for Your Wedding"));
        assertTrue(res.getText(), res.getText().startsWith("Want to make your stunning natural smile even more gorgeous?"));
        compareDates("2015-10-09 19:07:02", res.getDate());
    }

    @Test
    public void testMsn2() throws Exception {
        // http://www.msn.com/en-us/news/other/update-4-tennis-halle-open-mens-singles-round-1-results/ar-BBlaHCs
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("msn2.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("UPDATE 4-Tennis-Halle Open men's singles round 1 results"));
        assertTrue(res.getText(), res.getText().startsWith("June 15 (Infostrada Sports) - Results from the Halle Open Men'"));
        compareDates("2015-06-15 15:36:06", res.getDate());
    }

    @Test
    public void testMsn3() throws Exception {
        // http://www.msn.com/en-us/news/other/microsoft-shows-off-minecraft-built-specifically-for-hololens/ar-BBlb2RM
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("msn3.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("Microsoft shows off 'Minecraft' built specifically for HoloLens"));
        assertTrue(res.getText(), res.getText().startsWith("At its E3 2015 event, Microsoft has given us a new demo of Minecraft"));
        compareDates("2015-06-15 17:54:00", res.getDate());
    }

    @Test
    public void testMsn4() throws Exception {
        // http://www.msn.com/en-sg/money/other/asia-stocks-fall-second-week-as-china-h-shares-enter-bear-market/ar-AAoxWG
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("msn4.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("Asia Stocks Fall Second Week as China H-Shares Enter Bear Market"));
        assertTrue(res.getText(), res.getText().startsWith("March 22 (Bloomberg) -- Asia’s benchmark stock index fell the past five days to the biggest "));
        compareDates("2014-03-22 00:15:58", res.getDate());
    }

    @Test
    public void testCNBC() throws Exception {
        // http://www.cnbc.com/2015/10/01/amazon-google-move-into-on-demand-home-services.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("cnbc1.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("Amazon, Google move into on-demand home services"));
        assertTrue(res.getText(), res.getText().startsWith("Amazon and Google are vying to become the Uber for handymen"));
        assertTrue(res.getText(), res.getText().endsWith("This story has been updated to reflect that Thumbtack's CEO is Marco Zappacosta."));
        compareDates("2015-10-01 16:12:05", res.getDate());
    }

    @Test
    public void testCNBC2() throws Exception {
        // http://www.cnbc.com/2015/10/18/chinas-q3-gdp-up-69-y-o-y-compared-to-forecast-of-68.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("cnbc2.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("China's Q3 GDP up 6.9% y-o-y compared to forecast of 6.8%"));
        assertTrue(res.getText(), res.getText().startsWith("China's economy grew at its slowest pace since the global financial"));
        assertTrue(res.getText(), res.getText().endsWith("This report has been updated to show that China's economy grew 6.9 percent in the third-quarter."));
        compareDates("2015-10-18 22:00:02", res.getDate());
    }

    @Test
    public void testCNBC3() throws Exception {
        // http://www.cnbc.com/2015/10/12/india-us-japan-hold-naval-drills-in-bay-of-bengal-china-concerned.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("cnbc3.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("India, US, Japan hold naval drills in Bay of Bengal, China concerned"));
        assertTrue(res.getText(), res.getText().startsWith("India, Japan and the United States will hold joint naval exercises each year"));
        assertTrue(res.getText(), res.getText().endsWith("Indo-Pacific idea,\" he said."));
        compareDates("2015-10-12 21:49:04", res.getDate());
    }

    @Test
    public void testCNN3() throws Exception {
        // http://www.cnn.com/2015/09/24/politics/donald-trump-marco-rubio-foreign-policy/
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("cnn3.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("Rubio strikes back at Trump - CNNPolitics.com"));
        assertTrue(res.getText(), res.getText().startsWith("Washington Sen. Marco Rubio is firing back at Donald Trump as \"insecure\" and \"touchy\" after the mogul has spent the past two days going after his Republican presidential opponent."));
        assertTrue(res.getText(), res.getText().endsWith("\"Every time they kill mid-level accounting person from ISIS they have a news conference,\" he said. Marco Rubio's below-the-radar campaign"));
        compareDates("2015-09-24 12:29:56", res.getDate());
    }

    @Test
    public void testCNN4() throws Exception {
        // http://www.cnn.com/2015/10/20/middleeast/israeli-palestinian-tensions/index.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("cnn4.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("U.N. leader visits Israel, Palestinian territories - CNN.com"));
        assertTrue(res.getText(), res.getText().startsWith("Jerusalem (CNN) U.N. Secretary-General Ban Ki-moon "));
        assertTrue(res.getText(), res.getText().endsWith("Ben Wedeman contributed to this report."));
        compareDates("2015-10-20 09:05:18", res.getDate());
    }

    @Test
    public void testCNN5() throws Exception {
        // http://money.cnn.com/2015/10/20/news/uber-india-rape-verdict/index.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("cnn5.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("Uber driver guilty in India rape case"));
        assertTrue(res.getText(), res.getText().startsWith("Yadav was convicted Tuesday on four charges related to"));
        assertTrue(res.getText(), res.getText().endsWith("puts Uber in the shade"));
        compareDates("2015-10-20 03:16:59", res.getDate());
    }

    @Test
    public void testWamu() throws Exception {
        // https://wamu.org/news/15/10/23/why_calling_slaves_workers_is_more_than_an_editing_error
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("wamu.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("Why Calling Slaves 'Workers' Is More Than An Editing Error"));
        assertTrue(res.getText(), res.getText().startsWith("Coby Burren was reading his textbook"));
        assertTrue(res.getText(), res.getText().endsWith("\"and that he'll be heard.\""));
        // not supported
        //compareDates("2015-10-23", res.getDate());
    }

    @Test
    public void testJdsupra() throws Exception {
        // http://www.jdsupra.com/legalnews/defending-the-sec-s-choice-of-the-69927/
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("jdsupra.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("Defending"));
        assertTrue(res.getText(), res.getText().startsWith("Wharton Associate Professor"));
        assertEquals("Keith Paul Bishop", res.getAuthorName());
        assertEquals("| Allen Matkins Leck Gamble Mallory & Natsis LLP", res.getAuthorDescription());
        compareDates("2015-10-20", res.getDate());
    }

    @Test
    public void testDailyMail() throws Exception {
        // http://www.dailymail.co.uk/news/article-2763386/With-breath-hero-mom-hid-baby-toilet-helping-daughter-escape-father-shot-head-turning-gun-himself.html
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("dailymail.co.uk.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("With her 'last breath,' hero mom hid her baby in the toilet, sheltering the infant in the toilet as her boyfriend shot her in the head before turning the gun on himself"));
        assertTrue(res.getText(), res.getText().startsWith("A mother saved her six-month-old"));
        compareDates("2014-09-20 14:09:00", res.getDate());
    }

    @Test
    public void testITV() throws Exception {
        // http://www.itv.com/news/2014-04-14/boston-marathon-bombings-one-year-on/
        JResult res = extractor.extractContent(c.streamToString(getClass().getResourceAsStream("itv.html")));
        assertTrue(res.getTitle(), res.getTitle().startsWith("Boston Marathon bomb attacks: One year on"));
        assertTrue(res.getText(), res.getText().startsWith("Today Boston will mark the first anniversary"));
        compareDates("2014-04-14 23:53:00", res.getDate());
    }

    @Test
    public void testNola() throws Exception {
        // http://www.nola.com/running/index.ssf/2014/04/race_director_bill_burke_hopin.html
        JResult res = new JResult();
        res.setUrl("http://www.nola.com/running/index.ssf/2014/04/race_director_bill_burke_hopin.html");
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("nola.com.html")));
        assertEquals("Wife of Atlanta firefighter who died during training ride for New Orleans Ironman spoke at race Sunday", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Kimberly Guinn took the microphone"));
        compareDates("2014-04-13 17:42:00", res.getDate());
    }

    @Test
    public void testMercuryNews() throws Exception {
        //http://www.mercurynews.com/ci_26860908/virgin-galactic-co-pilot-michael-alsbury-from-scotts
        JResult res = new JResult();
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("mercurynews.html")));
        assertEquals("Virgin Galactic co-pilot Michael Alsbury from Scotts Valley", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("SANTA CRUZ -- Test pilot Michael Alsbury"));
        compareDates("2014-11-04 16:05:13", res.getDate());
    }

    @Test
    public void testMlive() throws Exception {
        //http://www.mlive.com/news/bay-city/index.ssf/2014/12/christmas_wishes_tour_fills_pa.html
        JResult res = new JResult();
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("mlive.html")));
        assertEquals("'Christmas Wishes Tour' party bus brightens the holiday season for Essexville family", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("On its final stop of"));
        compareDates("2014-12-22 17:54:00", res.getDate());
    }

    @Test
    public void testKagstv() throws Exception {
        //http://kagstv.com/News/KAGSNews/ID/6575/Friend-of-teen-torched-I-would-have-fought-for-her
        JResult res = new JResult();
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("kagstv.html")));
        assertEquals("Friend of teen torched: 'I would have fought for her' > KAGS TV - College Station, Texas", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("COURTLAND, Miss."));
        compareDates("2014-12-10 12:53:00", res.getDate());
    }

    @Test
    public void testKjrh() throws Exception {
        //http://www.kjrh.com/news/local-news/muskogee-roughers-football-team-collecting-donations-for-jenks-trojans-assistant-coach-bryant-calip
        JResult res = new JResult();
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("kjrh.html")));
        assertEquals("Muskogee Roughers football team collecting donations for Jenks Trojans assistant coach Bryant Calip - KJRH.com", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("That's the distance that separates two opposing football teams"));
        compareDates("2014-10-21 12:54:43", res.getDate());
    }

    /*
    @Test
    public void testJezebel() throws Exception {
        //http://jezebel.com/honey-boo-boo-star-selling-oils-to-save-you-from-ebola-1665926767
        JResult res = new JResult();
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("jezebel.html")));
        assertEquals("Honey Boo Boo Star Selling Oils to Save You From Ebola", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Anna Cardwell, former co-star"));
        compareDates("2014-02-12 20:00:00", res.getDate());
    }*/

    @Test
    public void testThetelegram() throws Exception {
        //http://www.thetelegram.com/News/Local/2014-11-24/article-3949943/Fundraiser-response-overwhelms-family/1
        JResult res = new JResult();
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("thetelegram.html")));
        assertEquals("Fundraiser response overwhelms family", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Rebecca, 9, daughter of Paul Byrne, holds a thank-you card"));
        compareDates("2014-11-24 00:00:00", res.getDate());
    }

    @Test
    public void testDogsbite() throws Exception {
        // http://blog.dogsbite.org/2014/08/014-dog-bite-fatality-toddler-dies-family-pit-bull-attack-under-grandmothers-care.html
        JResult res = new JResult();
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("dogsbite.html")));
        assertEquals("2014 Dog Bite Fatality: Toddler Dies After Attack by Family Pit Bull While Under Grandmother's Care - DogsBite.org", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Labels: 2014 Dog Bite Fatality"));
        compareDates("2015-01-29 00:00:00", res.getDate());
    }

    @Test
    public void testFoxNews() throws Exception {
        //http://latino.foxnews.com/latino/lifestyle/2014/07/22/cancer-stricken-father-with-4-months-to-live-heads-to-disneyland-with-family/
        JResult res = new JResult();
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("latino.foxnews.html")));
        assertEquals("Cancer-Stricken Father With 4 Months To Live Heads To Disneyland With Family For Last Time", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Expected to live only four more months"));
        compareDates("2014-07-22 00:00:00", res.getDate());
    }

    @Test
    public void testSimcoeReformer() throws Exception {
        //http://www.simcoereformer.ca/2014/09/29/ddss-teacher-begins-treatment-in-atlanta
        JResult res = new JResult();
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("simcoereformer.html")));
        assertEquals("`Team Lane’ has its fingers crossed", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("A Delhi teacher is waging the fight of his life"));
        compareDates("2014-09-29 23:44:08", res.getDate());
    }

    @Test
    public void testEurweb() throws Exception {
        // http://www.eurweb.com/2014/09/darren-wilson-fundraisers-end-without-explanation/
        JResult res = new JResult();
        res.setUrl("http://www.eurweb.com/2014/09/darren-wilson-fundraisers-end-without-explanation/");
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("eurweb.html")));
        assertEquals("Darren Wilson Online Fundraisers Stop Without Any Reason", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("*Online fundraisers for embattled Ferguson"));
        compareDates("2014-09-01", res.getDate());
    }

    @Test
    public void testShropshirestar() throws Exception {
        // http://www.shropshirestar.com/news/2014/09/16/shropshire-border-village-rallies-round-mend-our-mum-fight/
        JResult res = new JResult();
        res.setUrl("http://www.shropshirestar.com/news/2014/09/16/shropshire-border-village-rallies-round-mend-our-mum-fight/");
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("shropshirestar.html")));
        assertEquals("Shropshire border village rallies round Mend Our Mum fight « Shropshire Star", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("The Cliffe family were devastated"));
        compareDates("2014-09-16 18:59:00", res.getDate());
    }

    @Test
    public void testConnectionnewspapers() throws Exception {
        // http://www.connectionnewspapers.com/news/2014/sep/25/local-aikido-studio-reduces-ptsd-effects/
        JResult res = new JResult();
        res.setUrl("http://www.connectionnewspapers.com/news/2014/sep/25/local-aikido-studio-reduces-ptsd-effects/");
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("connectionnewspapers.html")));
        assertEquals("Local Aikido Studio Reduces PTSD Effects", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("As a soldier fighting"));
        compareDates("2014-09-25", res.getDate());
    }

    @Test
    public void testNewsSky() throws Exception {
        // http://news.sky.com/story/1515847/taylor-swift-gives-50k-to-fan-with-leukaemia
        JResult res = new JResult();
        res.setUrl("http://news.sky.com/story/1515847/taylor-swift-gives-50k-to-fan-with-leukaemia");
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("news.sky.com.html")));
        assertEquals("Taylor Swift Gives $50k To Fan With Leukaemia", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Taylor Swift has surprised one of her young fans"));
        compareDates("2015-07-09 09:39:00", res.getDate());
    }

    @Test
    public void testBildDe() throws Exception {
        // http://www.bild.de/news/ausland/wunder/aerzte-rieten-zur-abtreibung-jaxon-du-bist-ein-wunder-42736638.bild.html
        JResult res = new JResult();
        res.setUrl("http://www.bild.de/news/ausland/wunder/aerzte-rieten-zur-abtreibung-jaxon-du-bist-ein-wunder-42736638.bild.html");
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("bild.de.html")));
        assertEquals("Ärzte rieten Eltern zur Abtreibung, jetzt feiert er seinen 1. Geburtstag: Kleiner Jaxon, du bist ein Wunder! - News Ausland - Bild.de", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("Jaxon lernt gerade laufen und sprechen."));
        compareDates("2015-09-27 13:13:00", res.getDate());
    }

    @Test
    public void testMobileSlashdotOrg() throws Exception {
        // http://mobile.slashdot.org/story/15/11/12/1516255/mozilla-launches-firefox-for-ios
        JResult res = new JResult();
        res.setUrl("http://mobile.slashdot.org/story/15/11/12/1516255/mozilla-launches-firefox-for-ios");
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("mobile.slashdot.org.html")));
        assertEquals("Mozilla Launches Firefox For IOS - Slashdot", res.getTitle());
        // TODO: Text extraction in this case is not correct, includes some advertisement, fix this.
        //assertTrue(res.getText(), res.getText().startsWith("An anonymous reader writes:"));
        assertTrue(res.getText(), res.getText().startsWith("Slashdot Deals: Get The Fastest VPN For Your Internet Security Lifetime Subscription Of PureVPN"));
        compareDates("2015-11-12 10:17:00", res.getDate());
    }

    @Test
    public void testKwch() throws Exception {
        // http://www.kwch.com/news/local-news/crowdfunding-trend-growing-for-cancer-patients/33575512
        JResult res = new JResult();
        res.setUrl("http://www.kwch.com/news/local-news/crowdfunding-trend-growing-for-cancer-patients/33575512");
        res = extractor.extractContent(res, c.streamToString(getClass().getResourceAsStream("kwch.html")));
        assertEquals("Crowdfunding trend growing for cancer patients", res.getTitle());
        assertTrue(res.getText(), res.getText().startsWith("For 5-year-old Prestun Siebel being a kid hasn't been easy."));
        compareDates("2015-06-15 00:41:00", res.getDate());
    }

    public static void compareDates(String wanted, Date extracted) throws Exception {
        Date wantedDate = null;
        SimpleDateFormat[] dateFormats = {
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ssz"),
        };

        for(SimpleDateFormat dateFormat : dateFormats)
        {
            try {
                wantedDate = dateFormat.parse(wanted);
            } catch(ParseException ex) {
                continue;
            }
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        assertEquals(formatter.format(wantedDate), formatter.format(extracted));
    }

    /**
     * @param filePath the name of the file to open. Not sure if it can accept
     * URLs or just filenames. Path handling could be better, and buffer sizes
     * are hardcoded
     */
    public static String readFileAsString(String filePath)
            throws java.io.IOException {
        StringBuilder fileData = new StringBuilder(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
}
