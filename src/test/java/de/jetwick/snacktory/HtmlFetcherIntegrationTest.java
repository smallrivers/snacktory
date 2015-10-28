/*
 *  Copyright 2011 Peter Karich 
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package de.jetwick.snacktory;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.lang.time.*;

/**
 *
 * @author Peter Karich
 */
public class HtmlFetcherIntegrationTest {

    public HtmlFetcherIntegrationTest() {
    }

    @Test
    public void testNoException() throws Exception {
        JResult res = new HtmlFetcher().fetchAndExtract("http://www.tumblr.com/xeb22gs619", 10000, true);
//        System.out.println("tumblr:" + res.getUrl());

//        res = new HtmlFetcher().fetchAndExtract("http://www.faz.net/-01s7fc", 10000, true);
//        System.out.println("faz:" + res.getUrl());

        res = new HtmlFetcher().fetchAndExtract("http://www.google.com/url?sa=x&q=http://www.taz.de/1/politik/asien/artikel/1/anti-atomkraft-nein-danke/&ct=ga&cad=caeqargbiaaoataaoabaltmh7qrialaawabibwrllurf&cd=d5glzns5m_4&usg=afqjcnetx___sph8sjwhjwi-_mmdnhilra&utm_source=twitterfeed&utm_medium=twitter", 10000, true);
        assertTrue(res.getUrl(), res.getUrl().startsWith("http://www.taz.de/!"));
//        System.out.println("google redirect:" + res.getUrl());

        // Two levels of redirection:
        // http://on.fb.me/IKFRtL --> http://www.facebook.com --> https://www.facebook.com
        res = new HtmlFetcher().fetchAndExtract("http://on.fb.me/IKFRtL", 10000, true);
        assertEquals("https://www.facebook.com/", res.getUrl());

        // Redirection.
        res = new HtmlFetcher().fetchAndExtract("http://www.azcentral.com/videos/news/12-news/arizona-midday/2014/11/26/19550567/", 10000, true);
        assertEquals("http://www.azcentral.com/videos/news/12-news/arizona-midday/2014/11/26/19550567/", res.getUrl());
    }

    @Test
    public void testWithTitle() throws Exception {
        JResult res = new HtmlFetcher().fetchAndExtract("http://www.midgetmanofsteel.com/2011/03/its-only-matter-of-time-before-fox-news.html", 10000, true);
        assertEquals("It's Only a Matter of Time Before Fox News Takes Out a Restraining Order", res.getTitle());
        assertEquals("2011/03/02", DateFormatUtils.format(res.getDate(), "yyyy/MM/dd"));
    }

    // do not support this uglyness
//    @Test
//    public void doubleRedirect() throws Exception {
//        JResult res = new HtmlFetcher().fetchAndExtract("http://bit.ly/eZPI1c", 10000, true);
//        assertEquals("12 Minuten Battlefield 3 Gameplay - ohne Facebook-Bedingungen | Spaß und Spiele", res.getTitle());
//    }
    // not available anymore
//    @Test
//    public void testTwitpicGzipDoesNOTwork() throws Exception {
//        JResult res = new HtmlFetcher().fetchAndExtract("http://twitpic.com/4kuem8", 12000, true);
//        assertTrue(res.getText(), res.getText().contains("*Not* what you want to see"));
//    }

    // TODO: Change test so they depend on external pages anymore
    @Test
    public void testEncoding() throws Exception {
        JResult res = new HtmlFetcher().fetchAndExtract("http://www.yomiuri.co.jp/science/", 10000, true);
        assertEquals("科学・ＩＴニュース：読売新聞(YOMIURI ONLINE)", res.getTitle());
    }

    @Test
    public void testHashbang() throws Exception {
        JResult res = new HtmlFetcher().fetchAndExtract("http://www.facebook.com/democracynow", 10000, true);
        assertTrue(res.getTitle(), res.getTitle().startsWith("Democracy Now!"));

        // not available anymore
        //       res = new HtmlFetcher().fetchAndExtract("http://twitter.com/#!/th61/status/57141697720745984", 10000, true);
        //       assertTrue(res.getTitle(), res.getTitle().startsWith("Twitter / TH61: “@AntiAtomPiraten:"));
    }

    public void testImage() throws Exception {
        JResult res = new HtmlFetcher().fetchAndExtract("http://grfx.cstv.com/schools/okla/graphics/auto/20110505_schedule.jpg", 10000, true);
        assertEquals("http://grfx.cstv.com/schools/okla/graphics/auto/20110505_schedule.jpg", res.getImageUrl());
        assertTrue(res.getTitle().isEmpty());
        assertTrue(res.getText().isEmpty());
    }

    @Test
    public void testFurther() throws Exception {
        JResult res = new HtmlFetcher().fetchAndExtract("https://linksunten.indymedia.org/de/node/41619?utm_source=twitterfeed&utm_medium=twitter", 10000, true);
        assertTrue(res.getText(), res.getText().startsWith("Es gibt kein ruhiges Hinterland! Schon wieder den "));
    }

    @Test
    public void testDoubleResolve() throws Exception {
        JResult res = new HtmlFetcher().fetchAndExtract("http://t.co/eZRKcEYI", 10000, true);
        assertTrue(res.getTitle(), res.getTitle().startsWith("teleject/Responsive-Web-Design-Artboards"));
    }

    @Test
    public void testXml() throws Exception {
        String str = new HtmlFetcher().fetchAsString("https://karussell.wordpress.com/feed/", 10000);
        assertTrue(str, str.startsWith("<?xml version="));
    }

    @Test
    public void testYahooMobile() throws Exception {
        JResult res  = new HtmlFetcher().fetchAndExtract("https://m.yahoo.com/w/legobpengine/finance/news/stevia-first-corp-stvf-looks-123500390.html?.intl=us&.lang=en-us", 10000, true);
        assertTrue(res.getTitle(), res.getTitle().startsWith("Stevia First Corp. (STVF) Looks to Disrupt Flavor Industry"));
    }

    @Test
    public void testWamu() throws Exception {
        JResult res  = new HtmlFetcher().fetchAndExtract("https://wamu.org/news/15/10/23/why_calling_slaves_workers_is_more_than_an_editing_error", 10000, true);
        assertTrue(res.getTitle(), res.getTitle().startsWith("Why Calling Slaves 'Workers' Is More Than An Editing Error"));
        assertTrue(res.getText(), res.getText().startsWith("Coby Burren was reading his textbook"));
        assertTrue(res.getText(), res.getText().endsWith("\"and that he'll be heard.\""));
    }

    /* FIXME: This test fails with a java.io.IOException: Invalid Http response 
    @Test
    public void testWeixin() throws Exception {
        JResult res  = new HtmlFetcher().fetchAndExtract("http://mp.weixin.qq.com/s?3rd=MzA3MDU4NTYzMw%3D%3D&__biz=MzA4MTQ0Njc2Nw%3D%3D&idx=4&mid=207614885&scene=6&sn=eda80bb13406fb31cb25f70d12e6e7dc", 10000, true);
        assertTrue(res.getTitle(), res.getTitle().startsWith("缺少IT支持成跨境电商发展阻力"));
    }*/
}
