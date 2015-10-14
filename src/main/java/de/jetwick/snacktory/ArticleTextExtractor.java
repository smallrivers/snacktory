package de.jetwick.snacktory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Date;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector.SelectorParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.time.*;
import org.apache.commons.lang3.*;

/**
 * This class is thread safe.
 * Class for content extraction from string form of webpage
 * 'extractContent' is main call from external programs/classes
 *
 * @author Alex P (ifesdjeen from jreadability)
 * @author Peter Karich
 */
public class ArticleTextExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ArticleTextExtractor.class);
    // Interessting nodes
    private static final Pattern NODES = Pattern.compile("p|div|td|h1|h2|article|section");
    // Unlikely candidates
    private String unlikelyStr;
    private Pattern UNLIKELY;
    // Likely positive candidates
    private String positiveStr;
    private Pattern POSITIVE;
    // Most likely positive candidates
    private String highlyPositiveStr;
    private Pattern HIGHLY_POSITIVE;
    // Most likely negative candidates
    private String negativeStr;
    private Pattern NEGATIVE;
    private static final Pattern NEGATIVE_STYLE =
            Pattern.compile("hidden|display: ?none|font-size: ?small");
    private static final Pattern IGNORE_AUTHOR_PARTS =
        Pattern.compile("by|name|author|posted|twitter|handle|news", Pattern.CASE_INSENSITIVE);
    private static final Set<String> IGNORED_TITLE_PARTS = new LinkedHashSet<String>() {
        {
            add("hacker news");
            add("facebook");
            add("home");
            add("articles");
        }
    };
    private static final OutputFormatter DEFAULT_FORMATTER = new OutputFormatter();
    private OutputFormatter formatter = DEFAULT_FORMATTER;

    private static final int MAX_AUTHOR_NAME_LENGHT = 255;
    private static final int MIN_AUTHOR_NAME_LENGTH = 4;
    
    private static final List<Pattern> CLEAN_AUTHOR_PATTERNS = Arrays.asList(
        Pattern.compile("By\\S*(.*)[\\.,].*")
    );

    private static final List<Pattern> CLEAN_DATE_PATTERNS = Arrays.asList(
        Pattern.compile("Posted:(.*)")
    );

    private static final int MAX_AUTHOR_DESC_LENGHT = 1000;
    private static final int MAX_IMAGE_LENGHT = 255;

    // For debugging
    private static final boolean DEBUG_WEIGHTS = false;
    private static final boolean DEBUG_BASE_WEIGHTS = false;
    private static final boolean DEBUG_CHILDREN_WEIGHTS = false;
    private static final int MAX_LOG_LENGTH = 200;
    private static final int MIN_WEIGHT_TO_SHOW_IN_LOG = 10;

    public ArticleTextExtractor() {
        setUnlikely("com(bx|ment|munity)|dis(qus|cuss)|e(xtra|[-]?mail)|foot|"
                + "header|menu|re(mark|ply)|rss|sh(are|outbox)|sponsor"
                + "a(d|ll|gegate|rchive|ttachment)|(pag(er|ination))|popup|print|"
                + "login|si(debar|gn|ngle)");
        setPositive("(^(body|content|h?entry|main|page|post|text|blog|story|haupt))"
                + "|arti(cle|kel)|instapaper_body|storybody");
        setHighlyPositive("storybody|main-content");
        setNegative("nav($|igation)|user|com(ment|bx)|(^com-)|contact|"
                + "foot|masthead|(me(dia|ta))|outbrain|promo|related|scroll|(sho(utbox|pping))|"
                + "sidebar|sponsor|tags|tool|widget|player|disclaimer|toc|infobox|vcard|title");
    }

    public ArticleTextExtractor setUnlikely(String unlikelyStr) {
        this.unlikelyStr = unlikelyStr;
        UNLIKELY = Pattern.compile(unlikelyStr);
        return this;
    }

    public ArticleTextExtractor addUnlikely(String unlikelyMatches) {
        return setUnlikely(unlikelyStr + "|" + unlikelyMatches);
    }

    public ArticleTextExtractor setPositive(String positiveStr) {
        this.positiveStr = positiveStr;
        POSITIVE = Pattern.compile(positiveStr);
        return this;
    }

    public ArticleTextExtractor setHighlyPositive(String highlyPositiveStr) {
        this.highlyPositiveStr = highlyPositiveStr;
        HIGHLY_POSITIVE = Pattern.compile(highlyPositiveStr);
        return this;
    }

    public ArticleTextExtractor addPositive(String pos) {
        return setPositive(positiveStr + "|" + pos);
    }

    public ArticleTextExtractor setNegative(String negativeStr) {
        this.negativeStr = negativeStr;
        NEGATIVE = Pattern.compile(negativeStr);
        return this;
    }

    public ArticleTextExtractor addNegative(String neg) {
        setNegative(negativeStr + "|" + neg);
        return this;
    }

    public void setOutputFormatter(OutputFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * @param html extracts article text from given html string. wasn't tested
     * with improper HTML, although jSoup should be able to handle minor stuff.
     * @returns extracted article, all HTML tags stripped
     */
    public JResult extractContent(String html, int maxContentSize) throws Exception {
        return extractContent(new JResult(), html, maxContentSize);
    }

    public JResult extractContent(String html) throws Exception {
        return extractContent(new JResult(), html, 0);
    }

    public JResult extractContent(JResult res, String html, int maxContentSize) throws Exception {
        return extractContent(res, html, formatter, true, maxContentSize);
    }

    public JResult extractContent(JResult res, String html) throws Exception {
        return extractContent(res, html, formatter, true, 0);
    }

    public JResult extractContent(JResult res, String html, OutputFormatter formatter, 
                                  Boolean extractimages, int maxContentSize) throws Exception {
        if (html.isEmpty())
            throw new IllegalArgumentException("html string is empty!?");

        // http://jsoup.org/cookbook/extracting-data/selector-syntax
        return extractContent(res, Jsoup.parse(html), formatter, extractimages, maxContentSize);
    }

    // Returns the best node match based on the weights (see getWeight for strategy)
	private Element getBestMatchElement(Collection<Element> nodes){
		int maxWeight = -200;        // why -200 now instead of 0?
		Element bestMatchElement = null;
		
        boolean ignoreMaxWeightLimit = false;
        for (Element entry : nodes) {

            LogEntries entries = null;
            if (DEBUG_WEIGHTS)
                entries = new LogEntries();
            int currentWeight = getWeight(entry, false, entries);
            if (DEBUG_WEIGHTS){
                if(currentWeight>MIN_WEIGHT_TO_SHOW_IN_LOG){
                    System.out.println("");
                    System.out.println("-------------------------------------------");
                    System.out.println("         TAG: " + entry.tagName());
                    entries.print();
                    System.out.println("======================================");
                    System.out.println("                  TOTAL WEIGHT:" 
                                        + String.format("%3d", currentWeight));
                    String outerHtml = entry.outerHtml();
                    if (outerHtml.length() > MAX_LOG_LENGTH)
                        outerHtml = outerHtml.substring(0, MAX_LOG_LENGTH);
                    System.out.println(outerHtml);
                }
            }
            if (currentWeight > maxWeight) {
                maxWeight = currentWeight;
                bestMatchElement = entry;

                /*
                // NOTE: This optimization fails with large pages that
                contains chunks of text that can be mistaken by articles, since we 
                want the best accuracy possible, I am disabling it for now. AP.

                // The original code had a limit of 200, the intention was that
                // if a node had a weight greater than it, then it most likely
                // it was the main content.
                // However this assumption fails when the amount of text in the 
                // children (or grandchildren) is too large. If we detect this
                // case then the limit is ignored and we try all the nodes to select
                // the one with the absolute maximum weight.
                if (maxWeight > 500){
                    ignoreMaxWeightLimit = true;
                    continue;
                } 
                
                // formerly 200, increased to 250 to account for the fact
                // we are not adding the weights of the grand children to the
                // tally.
                
                if (maxWeight > 250 && !ignoreMaxWeightLimit) 
                    break;
                */
            }
        }

        return bestMatchElement;
    }

    public JResult extractContent(JResult res, Document doc, OutputFormatter formatter, 
                                  Boolean extractimages, int maxContentSize) throws Exception {
        Document origDoc = doc.clone();
        JResult result = extractContent(res, doc, formatter, extractimages, maxContentSize, true);
        //System.out.println("result.getText().length()="+result.getText().length());
        if (result.getText().length() == 0) {
            result = extractContent(res, origDoc, formatter, extractimages, maxContentSize, false);
        }
        return result;
    }


    // main workhorse
    public JResult extractContent(JResult res, Document doc, OutputFormatter formatter, 
                                  Boolean extractimages, int maxContentSize, boolean cleanScripts) throws Exception {
        if (doc == null)
            throw new NullPointerException("missing document");

        // get the easy stuff
        res.setTitle(extractTitle(doc));
        res.setDescription(extractDescription(doc));
        res.setCanonicalUrl(extractCanonicalUrl(doc));
        res.setType(extractType(doc));
        res.setSitename(extractSitename(doc));
        res.setLanguage(extractLanguage(doc));

        // get author information
        res.setAuthorName(extractAuthorName(doc));
        res.setAuthorDescription(extractAuthorDescription(doc, res.getAuthorName()));

        // add extra selection gravity to any element containing author name
        // wasn't useful in the case I implemented it for, but might be later
        /*
        Elements authelems = doc.select(":containsOwn(" + res.getAuthorName() + ")");
        for (Element elem : authelems) {
            elem.attr("extragravityscore", Integer.toString(100));
            System.out.println("modified element " + elem.toString());
        }
        */

        // get date from document, if not present, extract from URL if possible
        Date docdate = extractDate(doc);
        if (docdate == null) {
            String dateStr = SHelper.estimateDate(res.getUrl());

            docdate = parseDate(dateStr);
            res.setDate(docdate);
        } else {
            res.setDate(docdate);
        }

        // now remove the clutter 
        if (cleanScripts) {
            prepareDocument(doc);
        }

        // init elements and get the one with highest weight (see getWeight for strategy)
        Collection<Element> nodes = getNodes(doc);
        Element bestMatchElement = getBestMatchElement(nodes);

        // do extraction from the best element
        if (bestMatchElement != null) {

            if (DEBUG_WEIGHTS){
                System.out.println("----------- BEST ELEMENT --------------");
                System.out.println("         TAG: " + bestMatchElement.tagName());
                String bestMatchrHtml = bestMatchElement.outerHtml();
                if (bestMatchrHtml.length() > MAX_LOG_LENGTH)
                    bestMatchrHtml = bestMatchrHtml.substring(0, MAX_LOG_LENGTH);
                System.out.println(bestMatchrHtml);
                System.out.println("======================================");
                String outerHtml = bestMatchElement.outerHtml();
                if (outerHtml.length() > MAX_LOG_LENGTH)
                    outerHtml = outerHtml.substring(0, MAX_LOG_LENGTH);
                System.out.println(outerHtml);
            }

            if (extractimages) {
                List<ImageResult> images = new ArrayList<ImageResult>();
                Element imgEl = determineImageSource(bestMatchElement, images);
                if (imgEl != null) {
                    res.setImageUrl(SHelper.replaceSpaces(imgEl.attr("src")));
                    // TODO remove parent container of image if it is contained in bestMatchElement
                    // to avoid image subtitles flooding in

                    res.setImages(images);
                }
            }

            // clean before grabbing text
            String text = formatter.getFormattedText(bestMatchElement);
            text = removeTitleFromText(text, res.getTitle());
            // this fails for short facebook post and probably tweets: text.length() > res.getDescription().length()
            if (text.length() > res.getTitle().length()) {
                if (maxContentSize > 0){
                    if (text.length() > maxContentSize){
                        text = utf8truncate(text, maxContentSize);
                    }
                }
                res.setText(text);
                //                print("best element:", bestMatchElement);
            }

            // extract links from the same best element
            String fullhtml = bestMatchElement.toString();
            Elements children = bestMatchElement.select("a[href]"); // a with href = link
            String linkstr = "";
            Integer linkpos = 0;
            Integer lastlinkpos = 0;
            for (Element child : children) {
                linkstr = child.toString();
                linkpos = fullhtml.indexOf(linkstr, lastlinkpos);
                res.addLink(child.attr("abs:href"), child.text(), linkpos);
                lastlinkpos = linkpos;
            }
        }

        if (extractimages) {
            if (res.getImageUrl().isEmpty()) {
                res.setImageUrl(extractImageUrl(doc));
            }
        }

        res.setRssUrl(extractRssUrl(doc));
        res.setVideoUrl(extractVideoUrl(doc));
        res.setFaviconUrl(extractFaviconUrl(doc));
        res.setKeywords(extractKeywords(doc));

        // Sanity checks in author
        if (res.getAuthorName().length() > MAX_AUTHOR_NAME_LENGHT){
            res.setAuthorName(utf8truncate(res.getAuthorName(), MAX_AUTHOR_NAME_LENGHT));
        }

        // Sanity checks in author description.
        String authorDescSnippet = getSnippet(res.getAuthorDescription());
        if (getSnippet(res.getText()).equals(authorDescSnippet) || 
             getSnippet(res.getDescription()).equals(authorDescSnippet)) {
            res.setAuthorDescription("");
        } else {
            if (res.getAuthorDescription().length() > MAX_AUTHOR_DESC_LENGHT){
                res.setAuthorDescription(utf8truncate(res.getAuthorDescription(), MAX_AUTHOR_DESC_LENGHT));
            }
        }

        // Sanity checks in image name
        if (res.getImageUrl().length() > MAX_IMAGE_LENGHT){
            // doesn't make sense to truncate a URL
            res.setImageUrl("");
        }

        return res;
    }

    private static String getSnippet(String data){
        if (data.length() < 50)
            return data;
        else
            return data.substring(0, 50);
    }

    protected String extractTitle(Document doc) {

        String title = doc.title();
        if (title.isEmpty()) {
            title = SHelper.innerTrim(doc.select("head title").text());
            if (title.isEmpty()) {
                title = SHelper.innerTrim(doc.select("head meta[name=title]").attr("content"));
                if (title.isEmpty()) {
                    title = SHelper.innerTrim(doc.select("head meta[property=og:title]").attr("content"));
                    if (title.isEmpty()) {
                        title = SHelper.innerTrim(doc.select("head meta[name=twitter:title]").attr("content"));
                        if (title.isEmpty()) {
                            title = SHelper.innerTrim(doc.select("h1:first-of-type").text());
                        }
                    }
                }
            }
        } else {
            // Apply heuristic to try to determine whether the title is a substring of the
            // document title.
            boolean usingPossibleTitle = false;
            if (title.contains(" | ") || title.contains(" : ") || title.contains(" - ")){
                String possibleTitle = SHelper.innerTrim(doc.select("h1:first-of-type").text());
                if(!possibleTitle.isEmpty()){
                    String doc_title = doc.title();
                    if (doc_title.toLowerCase().contains(possibleTitle.toLowerCase())){
                        title = possibleTitle;
                        usingPossibleTitle = true;
                    }
                }
            }

            if(!usingPossibleTitle){
                title = cleanTitle(title);
            }

        }
        return title;
    }

    protected String extractCanonicalUrl(Document doc) {
        String url = SHelper.replaceSpaces(doc.select("head link[rel=canonical]").attr("href"));
        if (url.isEmpty()) {
            url = SHelper.replaceSpaces(doc.select("head meta[property=og:url]").attr("content"));
            if (url.isEmpty()) {
                url = SHelper.replaceSpaces(doc.select("head meta[name=twitter:url]").attr("content"));
            }
        }
        return url;
    }

    protected String extractDescription(Document doc) {
        String description = SHelper.innerTrim(doc.select("head meta[name=description]").attr("content"));
        if (description.isEmpty()) {
            description = SHelper.innerTrim(doc.select("head meta[property=og:description]").attr("content"));
            if (description.isEmpty()) {
                description = SHelper.innerTrim(doc.select("head meta[name=twitter:description]").attr("content"));
            }
        }
        return description;
    }

    // Returns the publication Date or null
	protected Date extractDate(Document doc) {
		String dateStr = "";

        // try some locations that nytimes uses
        Element elem = doc.select("meta[name=ptime]").first();
		if (elem != null) {
            dateStr = SHelper.innerTrim(elem.attr("content"));
            //            elem.attr("extragravityscore", Integer.toString(100));
            //            System.out.println("date modified element " + elem.toString());
        }

		if (dateStr == "") {
            dateStr = SHelper.innerTrim(doc.select("meta[name=utime]").attr("content"));
        }
		if (dateStr == "") {
            dateStr = SHelper.innerTrim(doc.select("meta[name=pdate]").attr("content"));
        }
		if (dateStr == "") {
            dateStr = SHelper.innerTrim(doc.select("meta[property=article:published]").attr("content"));
        }
		if (dateStr != "") {
            return parseDate(dateStr);
        }

        // taking this stuff directly from Juicer (and converted to Java)
        // opengraph (?)
        Elements elems = doc.select("meta[property=article:published_time]");
        if (elems.size() > 0) {
            Element el = elems.get(0);
            if (el.hasAttr("content")) {
                dateStr = el.attr("content");
                try {
                    if (dateStr.endsWith("Z")) {
                        dateStr = dateStr.substring(0, dateStr.length() - 1) + "GMT-00:00";
                    } else {
                        dateStr = "%sGMT%s".format(dateStr.substring(0, dateStr.length() - 6), 
                                                   dateStr.substring(dateStr.length() - 6, 
                                                                     dateStr.length()));
                    }
                } catch(StringIndexOutOfBoundsException ex) {
                    // do nothing
                } 
                return parseDate(dateStr);
            }
        } 

        // rnews 
        elems = doc.select("meta[property=dateCreated], span[property=dateCreated]");
        if (elems.size() > 0) {
            Element el = elems.get(0);
            if (el.hasAttr("content")) {
                dateStr = el.attr("content");
                
                return parseDate(dateStr);
            } else {
                return parseDate(el.text());
            }
        }

        // schema.org creativework
        elems = doc.select("meta[itemprop=datePublished], span[itemprop=datePublished]");
        if (elems.size() > 0) {
            Element el = elems.get(0);
            if (el.hasAttr("content")) {
                dateStr = el.attr("content");
                return parseDate(dateStr);
            } else if (el.hasAttr("value")) {
                dateStr = el.attr("value");
                return parseDate(dateStr);
            } else {
                return parseDate(el.text());
            }
        } 

        // parsely page (?)
        /*  skip conversion for now, seems highly specific and uses new lib
        elems = doc.select("meta[name=parsely-page]");
        if (elems.size() > 0) {
            implicit val formats = net.liftweb.json.DefaultFormats

                Element el = elems.get(0);
                if(el.hasAttr("content")) {
                    val json = parse(el.attr("content"))

                        return DateUtils.parseDateStrictly((json \ "pub_date").extract[String], Array("yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ssZZ", "yyyy-MM-dd'T'HH:mm:ssz"))
                        }
            } 
        */

        // BBC
        elems = doc.select("meta[name=OriginalPublicationDate]");
        if (elems.size() > 0) {
            Element el = elems.get(0);
            if (el.hasAttr("content")) {
                dateStr = el.attr("content");
                return parseDate(dateStr);
            }
        }

        // wired
        elems = doc.select("meta[name=DisplayDate]");
        if (elems.size() > 0) {
            Element el = elems.get(0);
            if (el.hasAttr("content")) {
                dateStr = el.attr("content");
                return parseDate(dateStr);
            }
        }

        // wildcard
        elems = doc.select("meta[name*=date]");
        if (elems.size() > 0) {
            Element el = elems.get(0);
            if (el.hasAttr("content")) {
                dateStr = el.attr("content");
                Date parsedDate = parseDate(dateStr);
                if (parsedDate != null){
                    return parsedDate;
                }
            }
        }

        // blogger
        elems = doc.select(".date-header");
        if (elems.size() > 0) {
            Element el = elems.get(0);
            dateStr = el.text();
            return parseDate(dateStr);
        }

        // naturebox.com
        elems = doc.select("time[class=published]");
        if (elems.size() > 0) {
            Element el = elems.get(0);
            dateStr = el.text();
            return parseDate(dateStr);
        }

        // itsalovelylife.com
        elems = doc.select("*[itemprop=datePublished]");
        if (elems.size() > 0) {
            Element el = elems.get(0);
            dateStr = el.text();
            if (dateStr != null)
                return parseDate(dateStr);
        }

        // trendkraft.de
        elems = doc.select("*[itemprop=dateCreated]");
        if (elems.size() > 0) {
            Element el = elems.get(0);
            if (el.hasAttr("datetime")) {
                dateStr = el.attr("datetime");
                if (dateStr != null)
                    return parseDate(dateStr);
            }
            dateStr = el.text();
            if (dateStr != null)
                return parseDate(dateStr);
        }

        elems = doc.select("*[id=post-date]");
        if (elems.size() > 0) {
            Element el = elems.get(0);
            dateStr = el.text();
            if (dateStr != null)
                return parseDate(dateStr);
        }

        elems = doc.select("*[class=storydatetime]");
        if (elems.size() > 0) {
            Element el = elems.get(0);
            dateStr = el.text();
            if (dateStr != null)
                return parseDate(dateStr);
        }

        elems = doc.select("*[class*=date]");
        if (elems.size() > 0) {
            Element el = elems.get(0);
            dateStr = el.text();
            if (dateStr != null)
                return parseDate(dateStr);
        }

        return null;
    }

    private Date parseDate(String dateStr) {
        String[] parsePatterns = {
            "dd MMM yyyy HH:mm",
            "dd MMM yyyy HH:mm:ss",
            "dd MMM yyyy",
            "dd MMMM yyyy HH:mm",
            "dd MMMM yyyy HH:mm:ss",
            "dd MMMM yyyy",
            "dd-MM-yyyy HH:mm",
            "dd-MM-yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy HH:mm:ss",
            "EEE, dd MMM yyyy HH:mm:ss z",
            "EEE, dd MMM yyyy HH:mm:ss",
            "EEE, MMM dd, yyyy",
            "MM-dd-yyyy hh:mm a",
            "MM-dd-yyyy HH:mm",
            "MM-dd-yyyy hh:mm:ss a",
            "MM-dd-yyyy HH:mm:ss",
            "MM-dd-yyyy",
            "MM/dd/yyyy hh:mm a",
            "MM/dd/yyyy HH:mm",
            "MM/dd/yyyy hh:mm:ss a",
            "MM/dd/yyyy HH:mm:ss",
            "MM/dd/yyyy HH:mma",
            "MM/dd/yyyy",
            "MMM dd, yyyy hh:mm a",
            "MMM dd, yyyy HH:mm",
            "MMM dd, yyyy hh:mm:ss a",
            "MMM dd, yyyy HH:mm:ss",
            "MMM dd, yyyy",
            "MMM. dd, yyyy hh:mm a",
            "MMM. dd, yyyy HH:mm",
            "MMM. dd, yyyy hh:mm:ss a",
            "MMM. dd, yyyy HH:mm:ss",
            "MMM. dd, yyyy",
            "yyyy-MM-dd hh:mm a",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd hh:mm:ss a", 
            "yyyy-MM-dd HH:mm:ss", 
            "yyyy-MM-dd", 
            "yyyy-MM-dd'T'HH:mm", 
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSSz",
            "yyyy-MM-dd'T'HH:mm:ssz",
            "yyyy-MM-dd'T'HH:mmz",
            "yyyy/MM/dd hh:mm ",
            "yyyy/MM/dd HH:mm",
            "yyyy/MM/dd hh:mm:ss a ", 
            "yyyy/MM/dd HH:mm:ss", 
            "yyyy/MM/dd",
            "yyyyMMdd HHmm",
            "yyyyMMdd HHmmss",
            "yyyyMMdd",
            "yyyyMMddHHmm",
            "yyyyMMddHHmmss",
        };

        try {
            dateStr = cleanDate(dateStr);
            //System.out.println("dateStr="+dateStr+"|");
            return DateUtils.parseDateStrictly(dateStr, parsePatterns);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String toUnicode(char ch) {
        return String.format("\\u%04x", (int) ch);
    }

    private String cleanDate(String dateStr) {

        // Workaround for Zulu timezone not support in DateUtil formats
        // see: http://stackoverflow.com/questions/2580925/simpledateformat-parsing-date-with-z-literal
        dateStr = dateStr.replaceAll("Z$", "+0000");

        // Workaround for issue with DateUtil format not supporting semicolon
        // on the tz format, see: http://stackoverflow.com/questions/6841067/date-format-error-with-2011-07-27t0641110000
        if (!dateStr.contains("GMT")){
            dateStr = dateStr.replaceAll("(.*[+-]\\d\\d):(\\d\\d)", "$1$2");
        }

        for (Pattern pattern : CLEAN_DATE_PATTERNS) {
            Matcher matcher = pattern.matcher(dateStr);
            if(matcher.matches()){
                dateStr = SHelper.innerTrim(matcher.group(1));
                break;
            }
        }

        // See: http://stackoverflow.com/questions/1060570/why-is-non-breaking-space-not-a-whitespace-character-in-java
        dateStr = dateStr.replaceAll("^\u00A0*(.*)\u00A0*","$1");
        dateStr = StringUtils.strip(dateStr);
        return dateStr;
    }

    // Returns the author name or null
	protected String extractAuthorName(Document doc) {
		String authorName = "";
		
        // first try the Google Author tag
		Element result = doc.select("body [rel*=author]").first();
		if (result != null)
			authorName = SHelper.innerTrim(result.ownText());

        // if that doesn't work, try some other methods
		if (authorName.isEmpty()) {

            // meta tag approaches, get content
            result = doc.select("head meta[name=author]").first();
            if (result != null) {
                authorName = SHelper.innerTrim(result.attr("content"));
            }

            if (authorName.isEmpty()) {  // for "opengraph"
                authorName = SHelper.innerTrim(doc.select("head meta[property=article:author]").attr("content"));
            }
            if (authorName.isEmpty()) { // OpenGraph twitter:creator tag
            	authorName = SHelper.innerTrim(doc.select("head meta[property=twitter:creator]").attr("content"));
            }
            if (authorName.isEmpty()) {  // for "schema.org creativework"
                authorName = SHelper.innerTrim(doc.select("meta[itemprop=author], span[itemprop=author]").attr("content"));
            }

            // other hacks
			if (authorName.isEmpty()) {
				try{
                    // build up a set of elements which have likely author-related terms
                    // .X searches for class X
					Elements matches = doc.select("a[rel=author],.byline-name,.byLineTag,.byline,.author,.by,.writer,.address");

					if(matches == null || matches.size() == 0){
						matches = doc.select("body [class*=author]");
					}
					
					if(matches == null || matches.size() == 0){
						matches = doc.select("body [title*=author]");
					}

                    // a hack for huffington post
					if(matches == null || matches.size() == 0){
						matches = doc.select(".staff_info dl a[href]");
					}

                    // a hack for http://sports.espn.go.com/
                    if(matches == null || matches.size() == 0){
                        matches = doc.select("cite[class*=source]");
                    }

                    // select the best element from them
					if(matches != null){
						Element bestMatch = getBestMatchElement(matches);

						if(!(bestMatch == null))
						{
							authorName = bestMatch.text();
							
							if(authorName.length() < MIN_AUTHOR_NAME_LENGTH){
								authorName = bestMatch.text();
							}
							
							authorName = SHelper.innerTrim(IGNORE_AUTHOR_PARTS.matcher(authorName).replaceAll(""));
							
							if(authorName.indexOf(",") != -1){
								authorName = authorName.split(",")[0];
							}
						}
					}
				}
				catch(Exception e){
					System.out.println(e.toString());
				}
			}
		}

        for (Pattern pattern : CLEAN_AUTHOR_PATTERNS) {
            Matcher matcher = pattern.matcher(authorName);
            if(matcher.matches()){
                authorName = SHelper.innerTrim(matcher.group(1));
                break;
            }
        }

        return authorName;
    }

    // Returns the author description or null
    protected String extractAuthorDescription(Document doc, String authorName){

        String authorDesc = "";

        if(authorName.equals(""))
            return "";

        // Special case for entrepreneur.com
        Elements matches = doc.select(".byline > .bio");
        if (matches!= null && matches.size() > 0){
            Element bestMatch = matches.first(); // assume it is the first.
            authorDesc = bestMatch.text();
            return authorDesc;
        }
        
        // Special case for huffingtonpost.com
        matches = doc.select(".byline span[class*=teaser]");
        if (matches!= null && matches.size() > 0){
            Element bestMatch = matches.first(); // assume it is the first.
            authorDesc = bestMatch.text();
            return authorDesc;
        }

        try {
            Elements nodes = doc.select(":containsOwn(" + authorName + ")");
            Element bestMatch = getBestMatchElement(nodes);
            if (bestMatch != null)
                authorDesc = bestMatch.text();
        } catch(SelectorParseException se){
            // Avoid error when selector is invalid
        }

        return authorDesc;
    }

    protected Collection<String> extractKeywords(Document doc) {
        String content = SHelper.innerTrim(doc.select("head meta[name=keywords]").attr("content"));

        if (content != null) {
            if (content.startsWith("[") && content.endsWith("]"))
                content = content.substring(1, content.length() - 1);

            String[] split = content.split("\\s*,\\s*");
            if (split.length > 1 || (split.length > 0 && !"".equals(split[0])))
                return Arrays.asList(split);
        }
        return Collections.emptyList();
    }

    /**
     * Tries to extract an image url from metadata if determineImageSource
     * failed
     *
     * @return image url or empty str
     */
    protected String extractImageUrl(Document doc) {
        // use open graph tag to get image
        String imageUrl = SHelper.replaceSpaces(doc.select("head meta[property=og:image]").attr("content"));
        if (imageUrl.isEmpty()) {
            imageUrl = SHelper.replaceSpaces(doc.select("head meta[name=twitter:image]").attr("content"));
            if (imageUrl.isEmpty()) {
                // prefer link over thumbnail-meta if empty
                imageUrl = SHelper.replaceSpaces(doc.select("link[rel=image_src]").attr("href"));
                if (imageUrl.isEmpty()) {
                    imageUrl = SHelper.replaceSpaces(doc.select("head meta[name=thumbnail]").attr("content"));
                }
            }
        }
        return imageUrl;
    }

    protected String extractRssUrl(Document doc) {
        return SHelper.replaceSpaces(doc.select("link[rel=alternate]").select("link[type=application/rss+xml]").attr("href"));
    }

    protected String extractVideoUrl(Document doc) {
        return SHelper.replaceSpaces(doc.select("head meta[property=og:video]").attr("content"));
    }

    protected String extractFaviconUrl(Document doc) {
        String faviconUrl = SHelper.replaceSpaces(doc.select("head link[rel=icon]").attr("href"));
        if (faviconUrl.isEmpty()) {
            faviconUrl = SHelper.replaceSpaces(doc.select("head link[rel^=shortcut],link[rel$=icon]").attr("href"));
        }
        return faviconUrl;
    }

    protected String extractType(Document doc) {
        String type = cleanTitle(doc.title());
        type = SHelper.innerTrim(doc.select("head meta[property=og:type]").attr("content"));
        return type;
    }

    protected String extractSitename(Document doc) {
        String sitename = SHelper.innerTrim(doc.select("head meta[property=og:site_name]").attr("content"));
        if (sitename.isEmpty()) {
        	sitename = SHelper.innerTrim(doc.select("head meta[name=twitter:site]").attr("content"));
        }
		if (sitename.isEmpty()) {
			sitename = SHelper.innerTrim(doc.select("head meta[property=og:site_name]").attr("content"));
		}
        return sitename;
    }

	protected String extractLanguage(Document doc) {
		String language = SHelper.innerTrim(doc.select("head meta[property=language]").attr("content"));
	    if (language.isEmpty()) {
	    	language = SHelper.innerTrim(doc.select("html").attr("lang"));
	    	if (language.isEmpty()) {
				language = SHelper.innerTrim(doc.select("head meta[property=og:locale]").attr("content"));
	    	}
	    }
	    if (!language.isEmpty()) {
			if (language.length()>2) {
				language = language.substring(0,2);
			}
		}
	    return language;
	}

    /**
     * Weights current element. By matching it with positive candidates and
     * weighting child nodes. Since it's impossible to predict which exactly
     * names, ids or class names will be used in HTML, major role is played by
     * child nodes
     *
     * @param e Element to weight, along with child nodes
     */
    protected int getWeight(Element e, boolean checkextra, LogEntries logEntries) {
        int weight = calcWeight(e);
        if(logEntries!=null) logEntries.add("       ======>     BASE WEIGHT:" + String.format("%3d", weight));
        int ownTextWeight = (int) Math.round(e.ownText().length() / 100.0 * 10);
        weight+=ownTextWeight;
        if(logEntries!=null) logEntries.add("       ======> OWN TEXT WEIGHT:" + String.format("%3d", ownTextWeight));
        int childrenWeight = (int) Math.round(weightChildNodes(e, logEntries) * 0.9);
        weight+=childrenWeight;
        if(logEntries!=null) logEntries.add("       ======> CHILDREN WEIGHT:" + String.format("%3d", childrenWeight));

        // add additional weight using possible 'extragravityscore' attribute
        if (checkextra) {
            Element xelem = e.select("[extragravityscore]").first();
            if (xelem != null) {
                //                System.out.println("HERE found one: " + xelem.toString());
                weight += Integer.parseInt(xelem.attr("extragravityscore"));
                //                System.out.println("WITH WEIGHT: " + xelem.attr("extragravityscore"));
            }
        }

        return weight;
    }

    /**
     * Weights a child nodes of given Element. During tests some difficulties
     * were met. For instance, not every single document has nested paragraph
     * tags inside of the major article tag. Sometimes people are adding one
     * more nesting level. So, we're adding 4 points for every 100 symbols
     * contained in tag nested inside of the current weighted element, but only
     * 3 points for every element that's nested 2 levels deep. This way we give
     * more chances to extract the element that has less nested levels,
     * increasing probability of the correct extraction.
     *
     * @param rootEl Element, who's child nodes will be weighted
     */
    protected int weightChildNodes(Element rootEl, LogEntries logEntries) {
        int weight = 0;
        Element caption = null;
        List<Element> pEls = new ArrayList<Element>(5);

        for (Element child : rootEl.children()) {
            String ownText = child.ownText();
            int ownTextLength = ownText.length();
            if (ownTextLength < 20)
                continue;

            if (DEBUG_CHILDREN_WEIGHTS){
                if(logEntries!=null) {
                    logEntries.add("\t      CHILD TAG: " + child.tagName());
                }
            }

            if (ownTextLength > 200){
                int childOwnTextWeight = Math.max(50, ownTextLength / 10);
                if(logEntries!=null)
                    logEntries.add("      CHILD TEXT WEIGHT:" 
                                   + String.format("%3d", childOwnTextWeight));
                weight += childOwnTextWeight;
            }

            if (child.tagName().equals("h1") || child.tagName().equals("h2")) {
                int h2h1Weight = 30;
                weight += h2h1Weight;
                if(logEntries!=null)
                    logEntries.add("\t   H1/H2 WEIGHT:" 
                                   + String.format("%3d", h2h1Weight));
            } else if (child.tagName().equals("div") || child.tagName().equals("p")) {
                int calcChildWeight = calcWeightForChild(child, ownText);
                weight+=calcChildWeight;
                if(logEntries!=null)
                    logEntries.add("\t   CHILD WEIGHT:" 
                                   + String.format("%3d", calcChildWeight));
                if (child.tagName().equals("p") && ownTextLength > 50)
                    pEls.add(child);

                if (child.className().toLowerCase().equals("caption"))
                    caption = child;
            }
        }

        //
        // Visit grandchildren, This section visits the grandchildren 
        // of the node and calculate their weights. Note that grandchildren
        // weights are only worth 1/3 of children's
        //
        int grandChildrenWeight = 0;
        int grandChildrenCount = 0;
        for (Element child2 : rootEl.children()) {

            if (DEBUG_CHILDREN_WEIGHTS){
                if(logEntries!=null) {
                    logEntries.add("\t    CHILD TAG: " + child2.tagName());
                    //logEntries.add(child2.outerHtml());
                }
            }

            // If the node looks negative don't include it in the weights
            // instead penalize the grandparent. This is done to try to 
            // avoid giving weigths to navigation nodes, etc.
            if (NEGATIVE.matcher(child2.id()).find() || 
                NEGATIVE.matcher(child2.className()).find()){
                if(logEntries!=null){
                    logEntries.add("\t  CHILD DISCARDED");
                }
                grandChildrenWeight-=30;
                continue;
            }

            for (Element grandchild : child2.children()) {
                int grandchildWeight = 0;
                String ownText = grandchild.ownText();
                int ownTextLength = ownText.length();
                if (ownTextLength < 20)
                    continue;

                if(logEntries!=null) {
                    logEntries.add("\t    GRANDCHILD TAG: " + grandchild.tagName());
                    //logEntries.add(grandchild.outerHtml());
                }
                grandChildrenCount+=1;

                if (ownTextLength > 200){
                    int childOwnTextWeight = Math.max(50, ownTextLength / 10);
                    if(logEntries!=null)
                        logEntries.add("    GRANDCHILD TEXT WEIGHT:" 
                                       + String.format("%3d", childOwnTextWeight));
                    grandchildWeight += childOwnTextWeight;
                }

                if (grandchild.tagName().equals("h1") || grandchild.tagName().equals("h2")) {
                    int h2h1Weight = 30;
                    grandchildWeight += h2h1Weight;
                    if(logEntries!=null)
                        logEntries.add("   GRANDCHILD H1/H2 WEIGHT:" 
                                       + String.format("%3d", h2h1Weight));
                } else if (grandchild.tagName().equals("div") || grandchild.tagName().equals("p")) {
                    int calcChildWeight = calcWeightForChild(grandchild, ownText);
                    grandchildWeight+=calcChildWeight;
                    if(logEntries!=null)
                        logEntries.add("   GRANDCHILD CHILD WEIGHT:" 
                                       + String.format("%3d", calcChildWeight));
                }

                if(logEntries!=null)
                    logEntries.add("\t GRANDCHILD WEIGHT:" 
                                   + String.format("%3d", grandchildWeight));
                grandChildrenWeight += grandchildWeight;
            }
        }

        if (grandChildrenCount <= 0)
            grandChildrenCount = 1;
        grandChildrenWeight = grandChildrenWeight / 3;
        if(logEntries!=null){
            logEntries.add("\t  GRANDCHILDREN WEIGHT:" 
                           + String.format("%3d", grandChildrenWeight));
            logEntries.add("\t   GRANDCHILDREN COUNT:" 
                           + String.format("%3d", grandChildrenCount));
        }
        weight+=grandChildrenWeight;

        // use caption and image
        if (caption != null){
            int captionWeight = 30;
            weight+=captionWeight;
            if(logEntries!=null)
                logEntries.add("\t CAPTION WEIGHT:" 
                               + String.format("%3d", captionWeight));
        }

        if (pEls.size() >= 2) {
            for (Element subEl : rootEl.children()) {
                if ("h1;h2;h3;h4;h5;h6".contains(subEl.tagName())) {
                    int h1h2h3Weight = 20;
                    weight += h1h2h3Weight;
                    if(logEntries!=null)
                        logEntries.add("  h1;h2;h3;h4;h5;h6 WEIGHT:" 
                                       + String.format("%3d", h1h2h3Weight));
                    // headerEls.add(subEl);
                } else if ("table;li;td;th".contains(subEl.tagName())) {
                    addScore(subEl, -30);
                }

                if ("p".contains(subEl.tagName()))
                    addScore(subEl, 30);
            }
        }
        return weight;
    }

    public void addScore(Element el, int score) {
        int old = getScore(el);
        setScore(el, score + old);
    }

    public int getScore(Element el) {
        int old = 0;
        try {
            old = Integer.parseInt(el.attr("gravityScore"));
        } catch (Exception ex) {
        }
        return old;
    }

    public void setScore(Element el, int score) {
        el.attr("gravityScore", Integer.toString(score));
    }

    private int calcWeightForChild(Element child, String ownText) {
        int c = SHelper.count(ownText, "&quot;");
        c += SHelper.count(ownText, "&lt;");
        c += SHelper.count(ownText, "&gt;");
        c += SHelper.count(ownText, "px");
        int val;
        if (c > 5)
            val = -30;
        else
            val = (int) Math.round(ownText.length() / 35.0);

        addScore(child, val);
        return val;
    }

    private int calcWeight(Element e) {
        int weight = 0;

        if (HIGHLY_POSITIVE.matcher(e.className()).find()){
            weight += 200;
            if (DEBUG_BASE_WEIGHTS) { System.out.println("+200"); }
        }

        if (HIGHLY_POSITIVE.matcher(e.id()).find()) {
            weight += 90;
            if (DEBUG_BASE_WEIGHTS) { System.out.println("+90"); }
        }

        if (POSITIVE.matcher(e.className()).find()){
            weight += 35;
            if (DEBUG_BASE_WEIGHTS) { System.out.println("+35"); }
        }

        if (POSITIVE.matcher(e.id()).find()){
            weight += 45;
            if (DEBUG_BASE_WEIGHTS) { System.out.println("+45"); }
        }

        if (UNLIKELY.matcher(e.className()).find()){
            weight -= 20;
            if (DEBUG_BASE_WEIGHTS) { System.out.println("-20"); }
        }

        if (UNLIKELY.matcher(e.id()).find()){
            weight -= 20;
            if (DEBUG_BASE_WEIGHTS) { System.out.println("-20"); }
        }

        if (NEGATIVE.matcher(e.className()).find()){
            weight -= 50;
            if (DEBUG_BASE_WEIGHTS) { System.out.println("-50"); }
        }

        if (NEGATIVE.matcher(e.id()).find()){
            weight -= 50;
            if (DEBUG_BASE_WEIGHTS) { System.out.println("-50"); }
        }

        String style = e.attr("style");
        if (style != null && !style.isEmpty() && NEGATIVE_STYLE.matcher(style).find()){
            weight -= 50;
            if (DEBUG_BASE_WEIGHTS) { System.out.println("-50"); }
        }

        String itemprop = e.attr("itemprop");
        if (itemprop != null && !itemprop.isEmpty() && POSITIVE.matcher(itemprop).find()){
            weight += 100;
            if (DEBUG_BASE_WEIGHTS) { System.out.println("+100"); }
        }

        return weight;
    }

    public Element determineImageSource(Element el, List<ImageResult> images) {
        int maxWeight = 0;
        Element maxNode = null;
        Elements els = el.select("img");
        if (els.isEmpty())
            els = el.parent().select("img");

        double score = 1;
        for (Element e : els) {
            String sourceUrl = e.attr("src");
            if (sourceUrl.isEmpty() || isAdImage(sourceUrl))
                continue;

            int weight = 0;
            int height = 0;
            try {
                height = Integer.parseInt(e.attr("height"));
                if (height >= 50)
                    weight += 20;
                else
                    weight -= 20;
            } catch (Exception ex) {
            }

            int width = 0;
            try {
                width = Integer.parseInt(e.attr("width"));
                if (width >= 50)
                    weight += 20;
                else
                    weight -= 20;
            } catch (Exception ex) {
            }
            String alt = e.attr("alt");
            if (alt.length() > 35)
                weight += 20;

            String title = e.attr("title");
            if (title.length() > 35)
                weight += 20;

            String rel = null;
            boolean noFollow = false;
            if (e.parent() != null) {
                rel = e.parent().attr("rel");
                if (rel != null && rel.contains("nofollow")) {
                    noFollow = rel.contains("nofollow");
                    weight -= 40;
                }
            }

            weight = (int) (weight * score);
            if (weight > maxWeight) {
                maxWeight = weight;
                maxNode = e;
                score = score / 2;
            }

            ImageResult image = new ImageResult(sourceUrl, weight, title, height, width, alt, noFollow);
            images.add(image);
        }

        Collections.sort(images, new ImageComparator());
        return maxNode;
    }

    /**
     * Prepares document. Currently only stipping unlikely candidates, since
     * from time to time they're getting more score than good ones especially in
     * cases when major text is short.
     *
     * @param doc document to prepare. Passed as reference, and changed inside
     * of function
     */
    protected void prepareDocument(Document doc) {
//        stripUnlikelyCandidates(doc);
        removeScriptsAndStyles(doc);
    }

    /**
     * Removes unlikely candidates from HTML. Currently takes id and class name
     * and matches them against list of patterns
     *
     * @param doc document to strip unlikely candidates from
     */
    protected void stripUnlikelyCandidates(Document doc) {
        for (Element child : doc.select("body").select("*")) {
            String className = child.className().toLowerCase();
            String id = child.id().toLowerCase();

            if (NEGATIVE.matcher(className).find()
                    || NEGATIVE.matcher(id).find()) {
//                print("REMOVE:", child);
                child.remove();
            }
        }
    }

    private Document removeScriptsAndStyles(Document doc) {
        Elements scripts = doc.getElementsByTag("script");
        for (Element item : scripts) {
            item.remove();
        }
        Elements noscripts = doc.getElementsByTag("noscript");
        for (Element item : noscripts) {
            item.remove();
        }

        Elements styles = doc.getElementsByTag("style");
        for (Element style : styles) {
            style.remove();
        }

        return doc;
    }

    private void print(Element child) {
        print("", child, "");
    }

    private void print(String add, Element child) {
        print(add, child, "");
    }

    private void print(String add1, Element child, String add2) {
        logger.info(add1 + " " + child.nodeName() + " id=" + child.id()
                + " class=" + child.className() + " text=" + child.text() + " " + add2);
    }

    private boolean isAdImage(String imageUrl) {
        return SHelper.count(imageUrl, "ad") >= 2;
    }

    /**
     * Match only exact matching as longestSubstring can be too fuzzy
     */
    public String removeTitleFromText(String text, String title) {
        // don't do this as its terrible to read
//        int index1 = text.toLowerCase().indexOf(title.toLowerCase());
//        if (index1 >= 0)
//            text = text.substring(index1 + title.length());
//        return text.trim();
        return text;
    }

    /**
     * based on a delimeter in the title take the longest piece or do some
     * custom logic based on the site
     *
     * @param title
     * @param delimeter
     * @return
     */
    private String doTitleSplits(String title, String delimeter) {
        String largeText = "";
        int largetTextLen = 0;
        String[] titlePieces = title.split(delimeter);

        // take the largest split
        for (String p : titlePieces) {
            if (p.length() > largetTextLen) {
                largeText = p;
                largetTextLen = p.length();
            }
        }

        largeText = largeText.replace("&raquo;", " ");
        largeText = largeText.replace("»", " ");
        return largeText.trim();
    }

    /**
     * @return a set of all important nodes
     */
    public Collection<Element> getNodes(Document doc) {
        Map<Element, Object> nodes = new LinkedHashMap<Element, Object>(64);
        int score = 100;
        for (Element el : doc.select("body").select("*")) {
            if (NODES.matcher(el.tagName()).matches()) {
                nodes.put(el, null);
                setScore(el, score);
                score = score / 2;
            }
        }
        return nodes.keySet();
    }

    public String cleanTitle(String title) {
        StringBuilder res = new StringBuilder();
//        int index = title.lastIndexOf("|");
//        if (index > 0 && title.length() / 2 < index)
//            title = title.substring(0, index + 1);

        int counter = 0;
        String[] strs = title.split("\\|");
        for (String part : strs) {
            if (IGNORED_TITLE_PARTS.contains(part.toLowerCase().trim()))
                continue;

            if (counter == strs.length - 1 && res.length() > part.length())
                continue;

            if (counter > 0)
                res.append("|");

            res.append(part);
            counter++;
        }

        return SHelper.innerTrim(res.toString());
    }

    /**
     * Truncate a Java string so that its UTF-8 representation will not 
     * exceed the specified number of bytes.
     *
     * For discussion of why you might want to do this, see
     * http://lpar.ath0.com/2011/06/07/unicode-alchemy-with-db2/
     */
    public static String utf8truncate(String input, int length) {
      StringBuffer result = new StringBuffer(length);
      int resultlen = 0;
      for (int i = 0; i < input.length(); i++) {
        char c = input.charAt(i);
        int charlen = 0;
        if (c <= 0x7f) {
          charlen = 1;
        } else if (c <= 0x7ff) {
          charlen = 2;
        } else if (c <= 0xd7ff) {
          charlen = 3;
        } else if (c <= 0xdbff) {
          charlen = 4;
        } else if (c <= 0xdfff) {
          charlen = 0;
        } else if (c <= 0xffff) {
          charlen = 3;
        }
        if (resultlen + charlen > length) {
          break;
        }
        result.append(c);
        resultlen += charlen;
      }
      return result.toString();
    }


    /**
     * Comparator for Image by weight
     *
     * @author Chris Alexander, chris@chris-alexander.co.uk
     *
     */
    public class ImageComparator implements Comparator<ImageResult> {

        @Override
        public int compare(ImageResult o1, ImageResult o2) {
            // Returns the highest weight first
            return o2.weight.compareTo(o1.weight);
        }
    }


    /**
    *   Helper class to keep track of log entries.
    */
    private class LogEntries {

        List<String> entries;

        public LogEntries(){
            entries = new ArrayList();
        }

        public void add(String entry){
            this.entries.add(entry);
        }

        public void print(){
            for (String entry : this.entries) {
                System.out.println(entry);
            }
        }
    }
}
