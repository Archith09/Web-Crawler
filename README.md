Web crawler will be a Java application that can be run from the command line, as edu.upenn.cis455.crawler.XPathCrawler. It will take the following command-line arguments (in this specific order, and the first three are required):

The URL of the Web page at which to start. Note that there are several ways to open the URL. ** For plain HTTP URLs you will probably get the best performance by just opening a socket to the port (we’ve provided the URLInfo class to help parse the pieces out). ** For HTTPS URLs you may want to use java.net.URL’s openConnection() method and cast to javax.net.ssl.HttpsURLConnection. This in turn has input and output streams as usual.
The directory containing the BerkeleyDB database environment that holds your store. The directory should be created if it does not already exist. Your crawler should recursively follow links from the page it starts on. (Note: the store servlet takes the path from web.xml while the crawler expects it as a command-line argument. You may assume that these paths are the same.)
The maximum size, in megabytes, of a document to be retrieved from a Web server
An optional argument indicating the number of files (HTML and XML) to retrieve before stopping. This will be useful for testing! It is intended that the crawler be run periodically, either by hand or from an automated system like cron command. So there is therefore no need to build a connection from the Web interface to the crawler. The crawler traverses links in HTML documents. They are extracted using a HTML parser, such as the Mozilla parser (http://mozillaparser.sourceforge.net/), TagSoup (http://ccil.org/~cowan/XML/tagsoup/), JTidy (http://jtidy.sourceforge.net/) or simply by searching the HTML document for occurrences of the pattern href="URL" and its subtle variations. If a link points to another HTML document, it should be retrieved and scanned for links as well. If it points to an XML or RSS document, it should be retrieved as well. All retrieved HTML and XML documents are stored in the database (so that the crawler does not have to retrieve them again if they do not change before the next crawl), but only the XML documents that match one of the XPath expressions should be added to the corresponding channels. The crawler is careful not to search the same page multiple times during a given crawl, and it should exit when it has no more pages to crawl. When crawler is processing a new HTML or XML page, it prints a short status report to System.out. Example: "http://xyz.com/index.html: Downloading" (if the page is actually downloaded) or "http://abc.com/def.html: Not modified" (if the page is not downloaded because it has not changed).
Politeness

Crawler must be a considerate Web citizen.

It respects the robots.txt file, as described in A Standard for Robot Exclusion (http://www.robotstxt.org/robotstxt.html). It mainly supports the Crawl-Delay directive (see http://en.wikipedia.org/wiki/Robots.txt) and "User-agent: *"
