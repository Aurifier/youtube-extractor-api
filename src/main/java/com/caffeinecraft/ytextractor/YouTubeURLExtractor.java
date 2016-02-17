package com.caffeinecraft.ytextractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class YouTubeURLExtractor {
    public URI getDASHAudioURI(String videoId) {
        URL manifestURL = this.getDASHManifestURL(videoId);
        Map<Integer, String> urlsByFormat = parseDASHManifest(manifestURL);
        try {
            return new URI(urlsByFormat.get(141));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public URL getDASHManifestURL(String videoId) {
        String urlString = "https://www.youtube.com/watch?v=" + videoId + "&gl=US&hl=en&has_verified=1&bpctr=9999999999";
        Scanner scanner;
        try {
            scanner = createScannerForURL(urlString);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Pattern p = Pattern.compile(";ytplayer\\.config\\s*=\\s*(\\{.*?\\});");

        Map<String, Object> ytplayerConfig = new HashMap<String, Object>();

        if(scanner.findWithinHorizon(p, 0) != null) {
            MatchResult match = scanner.match();
            ObjectMapper mapper = new ObjectMapper();
            try {
                ytplayerConfig = mapper.readValue(match.group(1), Map.class);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        Map<String, String> args = (Map<String, String>) ytplayerConfig.get("args");
        String dashUrl = args.get("dashmpd");
        if(dashUrl == null) {
            dashUrl = getDashManifestURLFromVideoInfo(videoId);
        }

        try {
            return new URL(dashUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getDashManifestURLFromVideoInfo(String videoId) {
        String urlString = "https://www.youtube.com/get_video_info?&video_id=" + videoId + "&el=info&ps=default&eurl=&gl=US&hl=en";
        InputStream info;
        try {
            info = new URL(urlString).openStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        List<NameValuePair> videoInfoList = URLEncodedUtils.parse(readEntireInputStream(info), Charset.forName("UTF-8"));
        Map<String, String> videoInfo = new HashMap<String, String>();
        for(NameValuePair item : videoInfoList) {
            videoInfo.put(item.getName(), item.getValue());
        }

        return videoInfo.get("dashmpd");
    }

    private String readEntireInputStream(InputStream is) {
        Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private Map<Integer, String> parseDASHManifest(URL manifestURL) {
        NodeList representations;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document manifest = builder.parse(manifestURL.openStream());
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "//Representation";
            representations = (NodeList) xPath.compile(expression).evaluate(manifest, XPathConstants.NODESET);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        } catch (SAXException e) {
            e.printStackTrace();
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Map<Integer, String> formats = new HashMap<Integer, String>();
        for(int i = 0; i < representations.getLength(); i++) {
            Node rep = representations.item(i);
            Integer id = Integer.parseInt(rep.getAttributes().getNamedItem("id").getNodeValue());
            NodeList kids = rep.getChildNodes();
            String url = "";
            for(int j = 0; j < kids.getLength(); j++) {
                Node node = kids.item(j);
                if("BaseURL".equals(node.getNodeName())) {
                    url = node.getTextContent();
                }
            }
            formats.put(id, url);
        }
        return formats;
    }

    private Scanner createScannerForURL(String url) throws IOException {
        return new Scanner(new URL(url).openStream());
    }
}
