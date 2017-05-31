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

import de.jetwick.snacktory.utils.SSLConnectionSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Class to fetch articles. This class is thread safe.
 *
 * @author Peter Karich
 */
public class HtmlFetcher {

    static {
        SHelper.enableCookieMgmt();
        SHelper.enableUserAgentOverwrite();
        SHelper.enableAnySSL();
    }
    private static final Logger logger = LoggerFactory.getLogger(HtmlFetcher.class);

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("urls.txt"));
        String line = null;
        Set<String> existing = new LinkedHashSet<String>();
        while ((line = reader.readLine()) != null) {
            int index1 = line.indexOf("\"");
            int index2 = line.indexOf("\"", index1 + 1);
            String url = line.substring(index1 + 1, index2);
            String domainStr = SHelper.extractDomain(url, true);
            String counterStr = "";
            // TODO more similarities
            if (existing.contains(domainStr))
                counterStr = "2";
            else
                existing.add(domainStr);

            String html = new HtmlFetcher().fetchAsString(url, 20000);
            String outFile = domainStr + counterStr + ".html";
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
            writer.write(html);
            writer.close();
        }
        reader.close();
    }
    private static final boolean DISABLE_SSL_VERIFICATION = true;
    private String referrer = "http://jetsli.de/crawler";
    private String userAgent = "Mozilla/5.0 (compatible; Jetslide; +" + referrer + ")";
    private String cacheControl = "max-age=0";
    private String language = "en-us";
    private String accept = "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
    private String charset = "UTF-8";
    private SCache cache;
    private AtomicInteger cacheCounter = new AtomicInteger(0);
    private int maxTextLength = -1;
    private ArticleTextExtractor extractor = new ArticleTextExtractor();
    private Set<String> furtherResolveNecessary = new LinkedHashSet<String>() {
        {
            add("bit.ly");
            add("cli.gs");
            add("deck.ly");
            add("fb.me");
            add("feedproxy.google.com");
            add("flic.kr");
            add("fur.ly");
            add("goo.gl");
            add("is.gd");
            add("ink.co");
            add("j.mp");
            add("lnkd.in");
            add("on.fb.me");
            add("ow.ly");
            add("plurl.us");
            add("sns.mx");
            add("snurl.com");
            add("su.pr");
            add("t.co");
            add("tcrn.ch");
            add("tl.gd");
            add("tiny.cc");
            add("tinyurl.com");
            add("tmi.me");
            add("tr.im");
            add("twurl.nl");
        }
    };

    public HtmlFetcher() {
    }

    public void setExtractor(ArticleTextExtractor extractor) {
        this.extractor = extractor;
    }

    public ArticleTextExtractor getExtractor() {
        return extractor;
    }

    public HtmlFetcher setCache(SCache cache) {
        this.cache = cache;
        return this;
    }

    public SCache getCache() {
        return cache;
    }

    public int getCacheCounter() {
        return cacheCounter.get();
    }

    public HtmlFetcher clearCacheCounter() {
        cacheCounter.set(0);
        return this;
    }

    public HtmlFetcher setMaxTextLength(int maxTextLength) {
        this.maxTextLength = maxTextLength;
        return this;
    }

    public int getMaxTextLength() {
        return maxTextLength;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getReferrer() {
        return referrer;
    }

    public HtmlFetcher setReferrer(String referrer) {
        this.referrer = referrer;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAccept() {
        return accept;
    }

    public String getCacheControl() {
        return cacheControl;
    }

    public String getCharset() {
        return charset;
    }

    public JResult fetchAndExtractCanonical(String url, int timeout, boolean resolve) throws Exception {
        return fetchAndExtract(url, timeout, resolve, 0, false, true);
    }

    public JResult fetchAndExtract(String url, int timeout, boolean resolve) throws Exception {
        return fetchAndExtract(url, timeout, resolve, 0, false, false);
    }

    // main workhorse to call externally
    public JResult fetchAndExtract(String url, int timeout, boolean resolve, 
                                   int maxContentSize, boolean forceReload,
                                   boolean onlyExtractCanonical) throws Exception {
        String originalUrl = url;
        url = SHelper.removeHashbang(url);
        String gUrl = SHelper.getUrlFromUglyGoogleRedirect(url);
        if (gUrl != null)
            url = gUrl;
        else {
            gUrl = SHelper.getUrlFromUglyFacebookRedirect(url);
            if (gUrl != null)
                url = gUrl;
        }

        if (resolve) {
            // check if we can avoid resolving the URL (which hits the website!)
            JResult res = getFromCache(url, originalUrl);
            if (res != null)
                return res;

            String resUrl = getResolvedUrl(url, timeout, 0);
            /*
            // There are some cases when the resolved URL is empty (some sites
            don't like the HEAD request, in that case instead of returning an
            empty result try to access the site normally with GET.)
            if (resUrl.isEmpty()) {
                if (logger.isDebugEnabled())
                    logger.warn("resolved url is empty. Url is: " + url);

                JResult result = new JResult();
                if (cache != null)
                    cache.put(url, result);
                return result.setUrl(url);
            }*/

            // if resolved url is different then use it!
            if (resUrl != null && !resUrl.isEmpty() && resUrl != url) {
                // this is necessary e.g. for some homebaken url resolvers which return
                // the resolved url relative to url!
                url = SHelper.useDomainOfFirstArg4Second(url, resUrl);
            }
        }

        // check if we have the (resolved) URL in cache
        JResult res = getFromCache(url, originalUrl);
        if (res != null)
            return res;

        JResult result = new JResult();
        // or should we use? <link rel="canonical" href="http://www.N24.de/news/newsitem_6797232.html"/>
        result.setUrl(url);
        result.setOriginalUrl(originalUrl);

        // Immediately put the url into the cache as extracting content takes time.
        if (cache != null) {
            cache.put(originalUrl, result);
            cache.put(url, result);
        }

        // extract content to the extent appropriate for content type
        String lowerUrl = url.toLowerCase();
        if (SHelper.isDoc(lowerUrl) || SHelper.isApp(lowerUrl) || SHelper.isPackage(lowerUrl)) {
            // skip
        } else if (SHelper.isVideo(lowerUrl) || SHelper.isAudio(lowerUrl)) {
            result.setVideoUrl(url);
        } else if (SHelper.isImage(lowerUrl)) {
            result.setImageUrl(url);
        } else {
            try {
                String urlToDownload = url;
                if(forceReload){
                    urlToDownload = getURLtoBreakCache(url);
                } 

                if (!onlyExtractCanonical){
                    extractor.extractContent(result, fetchAsString(urlToDownload, timeout), maxContentSize);
                } else {
                    extractor.extractCanonical(result, fetchAsString(urlToDownload, timeout), false);
                }
            } catch (FileNotFoundException fe){
                throw new SnacktoryNotFoundException();
            } catch (IOException io){
                // do nothing
                logger.error("Exception for URL: " + url + ":" + io);
            }

            if (!onlyExtractCanonical){
                if (result.getFaviconUrl().isEmpty())
                    result.setFaviconUrl(SHelper.getDefaultFavicon(url));

                // some links are relative to root and do not include the domain of the url :(
                if(!result.getFaviconUrl().isEmpty())
                    result.setFaviconUrl(fixUrl(url, result.getFaviconUrl()));

                if(!result.getImageUrl().isEmpty())
                    result.setImageUrl(fixUrl(url, result.getImageUrl()));

                if(!result.getVideoUrl().isEmpty())
                    result.setVideoUrl(fixUrl(url, result.getVideoUrl()));

                if(!result.getRssUrl().isEmpty())
                    result.setRssUrl(fixUrl(url, result.getRssUrl()));
            }
        }

        if (!onlyExtractCanonical){
            result.setText(lessText(result.getText()));
        }
        synchronized (result) {
            result.notifyAll();
        }
        return result;
    }

    // Ugly hack to break free from any cached versions, a few URLs required this.
    public String getURLtoBreakCache(String url) {
        try {
            URL aURL = new URL(url);
            if (aURL.getQuery() != "") {
                return url + "?1";
            } else {
                return url + "&1";
            }
        } catch(MalformedURLException e){
            return url;
        }
    }

    public String lessText(String text) {
        if (text == null)
            return "";

        if (maxTextLength >= 0 && text.length() > maxTextLength)
            return text.substring(0, maxTextLength);

        return text;
    }

    private static String fixUrl(String url, String urlOrPath) {
        return SHelper.useDomainOfFirstArg4Second(url, urlOrPath);
    }

    public String fetchAsString(String urlAsString, int timeout)
            throws MalformedURLException, IOException {
        return fetchAsString(urlAsString, timeout, true);
    }

    // main routine to get raw webpage content
    public String fetchAsString(String urlAsString, int timeout, boolean includeSomeGooseOptions)
            throws MalformedURLException, IOException {
        HttpURLConnection hConn = createUrlConnection(urlAsString, timeout, includeSomeGooseOptions);
        hConn.setInstanceFollowRedirects(true);
        String encoding = hConn.getContentEncoding();
        InputStream is;
        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
            is = new GZIPInputStream(hConn.getInputStream());
        } else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
            is = new InflaterInputStream(hConn.getInputStream(), new Inflater(true));
        } else {
            is = hConn.getInputStream();
        }

        String enc = Converter.extractEncoding(hConn.getContentType());
        String res = createConverter(urlAsString).streamToString(is, enc);
        if (logger.isDebugEnabled())
            logger.debug(res.length() + " FetchAsString:" + urlAsString);
        return res;
    }

    public Converter createConverter(String url) {
        return new Converter(url);
    }

    /**
     * On some devices we have to hack:
     * http://developers.sun.com/mobility/reference/techart/design_guidelines/http_redirection.html
     *
     * @param timeout Sets a specified timeout value, in milliseconds
     * @return the resolved url if any. Or null if it couldn't resolve the url
     * (within the specified time) or the same url if response code is OK
     */
    public String getResolvedUrl(String urlAsString, int timeout, 
                                 int num_redirects) {
        String newUrl = null;
        int responseCode = -1;
        try {
            HttpURLConnection hConn = createUrlConnection(urlAsString, timeout, true);
            // force no follow
            hConn.setInstanceFollowRedirects(false);
            // the program doesn't care what the content actually is !!
            // http://java.sun.com/developer/JDCTechTips/2003/tt0422.html
            hConn.setRequestMethod("HEAD");
            hConn.connect();
            responseCode = hConn.getResponseCode();
            hConn.getInputStream().close();
            if (responseCode == HttpURLConnection.HTTP_OK)
                return urlAsString;

            newUrl = hConn.getHeaderField("Location");
            // Note that the max recursion level is 5.
            if (responseCode / 100 == 3 && newUrl != null && num_redirects<5) {
                newUrl = newUrl.replaceAll(" ", "+");
                // some services use (none-standard) utf8 in their location header
                if (urlAsString.startsWith("http://bit.ly") 
                    || urlAsString.startsWith("http://is.gd"))
                    newUrl = encodeUriFromHeader(newUrl);

                // AP: This code is not longer need, instead we always follow
                // multiple redirects.
                //
                // fix problems if shortened twice. as it is often the case after twitters' t.co bullshit
                //if (furtherResolveNecessary.contains(SHelper.extractDomain(newUrl, true)))
                //    newUrl = getResolvedUrl(newUrl, timeout);

                // Add support for URLs with multiple levels of redirection,
                // call getResolvedUrl until there is no more redirects or a
                // max number of redirects is reached.
                newUrl = SHelper.useDomainOfFirstArg4Second(urlAsString, newUrl);
                newUrl = getResolvedUrl(newUrl, timeout, num_redirects+1);
                return newUrl;
            } else
                return urlAsString;

        } catch (Exception ex) {
            logger.warn("getResolvedUrl:" + urlAsString + " Error:" 
                        + ex.getMessage());
            return "";
        } finally {
            if (logger.isDebugEnabled())
                logger.debug(responseCode + " url:" + urlAsString 
                             + " resolved:" + newUrl);
        }
    }

    /**
     * Takes a URI that was decoded as ISO-8859-1 and applies percent-encoding
     * to non-ASCII characters. Workaround for broken origin servers that send
     * UTF-8 in the Location: header.
     */
    static String encodeUriFromHeader(String badLocation) {
        StringBuilder sb = new StringBuilder();

        for (char ch : badLocation.toCharArray()) {
            if (ch < (char) 128) {
                sb.append(ch);
            } else {
                // this is ONLY valid if the uri was decoded using ISO-8859-1
                sb.append(String.format("%%%02X", (int) ch));
            }
        }

        return sb.toString();
    }

    protected HttpURLConnection createUrlConnection(String urlAsStr, int timeout,
            boolean includeSomeGooseOptions) throws MalformedURLException, IOException {

        URL url = new URL(urlAsStr);
        //using proxy may increase latency
        HttpURLConnection hConn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        hConn.setRequestProperty("User-Agent", userAgent);
        hConn.setRequestProperty("Accept", accept);

        if (includeSomeGooseOptions) {
            hConn.setRequestProperty("Accept-Language", language);
            hConn.setRequestProperty("content-charset", charset);
            hConn.addRequestProperty("Referer", referrer);
            // avoid the cache for testing purposes only?
            hConn.setRequestProperty("Cache-Control", cacheControl);
        }

        // suggest respond to be gzipped or deflated (which is just another compression)
        // http://stackoverflow.com/q/3932117
        hConn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        hConn.setConnectTimeout(timeout);
        hConn.setReadTimeout(timeout);

        if(DISABLE_SSL_VERIFICATION){
            if (urlAsStr.toLowerCase().startsWith("https://")){
                try {
                    List sniHostNames = new ArrayList() {{
                        add(new SNIHostName(url.getHost()));
                    }};
                    SSLParameters sslParameters = new SSLParameters();
                    sslParameters.setServerNames(sniHostNames);

                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, new TrustManager[]{new NullX509TrustManager()}, null);

                    HttpsURLConnection hConnSecure = (HttpsURLConnection) hConn;
                    hConnSecure.setDefaultSSLSocketFactory(new SSLConnectionSocketFactory(sslContext.getSocketFactory(), sslParameters));
                    hConnSecure.setDefaultHostnameVerifier(new NullHostnameVerifier());
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return hConn;
    }

    private JResult getFromCache(String url, String originalUrl) throws Exception {
        if (cache != null) {
            JResult res = cache.get(url);
            if (res != null) {
                // e.g. the cache returned a shortened url as original url now we want to store the
                // current original url! Also it can be that the cache response to url but the JResult
                // does not contain it so overwrite it:
                res.setUrl(url);
                res.setOriginalUrl(originalUrl);
                cacheCounter.addAndGet(1);
                return res;
            }
        }
        return null;
    }

    private static class NullX509TrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //System.out.println();
        }
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //System.out.println();
        }
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
 
    private static class NullHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
