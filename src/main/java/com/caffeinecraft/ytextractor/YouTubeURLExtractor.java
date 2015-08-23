package com.caffeinecraft.ytextractor;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class YouTubeURLExtractor {
    public URI getDASHAudioURI(String videoId) {
        String urlString = "https://www.youtube.com/watch?v=" + videoId + "&gl=US&hl=en&has_verified=1&bpctr=9999999999";
        Scanner scanner;
        try {
            scanner = this.createScannerForURL(urlString);
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

        Map<Integer, String> urlsByFormat = parseDASHManifest(args.get("dashmpd"));
        try {
            return new URI(urlsByFormat.get(141));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }

        /*
        TODO: Remove these scraps once they exist in version control
        Pattern playerURLPattern = Pattern.compile("swfConfig.*?\"(https?:\\\\/\\\\/.*?watch.*?-.*?\\.swf)\"");
        try {
            scanner = this.createScannerForURL(urlString);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        String playerUrl = "";
        if(scanner.findWithinHorizon(playerURLPattern, 0) != null) {
            MatchResult match = scanner.match();
            playerUrl = match.group(1).replaceAll("\\\\(.)", "$1");
        }
        */

        /*
        String encodedURLs = new StringBuilder()
                .append(args.get("url_encoded_fmt_stream_map"))
                .append(',')
                .append(args.get("adaptive_fmts"))
                .toString();

        Map<Integer, String> urlsByFormat = new HashMap<Integer, String>();
        for(String urlDataString : encodedURLs.split(",")) {
            List<NameValuePair> urlData = URLEncodedUtils.parse(urlDataString, Charset.forName("UTF-8"));
            Integer format = 0;
            String url = "";
            for(NameValuePair pair : urlData) {
                if(pair.getName().equalsIgnoreCase("itag")) {
                    format = Integer.parseInt(pair.getValue());
                } else if(pair.getName().equalsIgnoreCase("url")) {
                    try {
                        url = URLDecoder.decode(pair.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        //TODO:
                        e.printStackTrace();
                        continue;
                    }
                }
                //System.out.println(pair.getName() + ":" + pair.getValue());
            }
            //System.out.println();
            urlsByFormat.put(format, url);
        }
        */

        /*
        for(Integer key : urlsByFormat.keySet()) {
            System.out.println(key + ":" + urlsByFormat.get(key));
        }
        */
    }

    private Map<Integer, String> parseDASHManifest(String manifestURL) {
        NodeList representations;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document manifest = builder.parse(new URL(manifestURL).openStream());
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
