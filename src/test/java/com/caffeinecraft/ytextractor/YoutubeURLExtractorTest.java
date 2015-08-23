package com.caffeinecraft.ytextractor;

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class YoutubeURLExtractorTest {
    @Test
    public void testGetDASHAudioURI() {
        String expectedQueryStringParts = "id=c1a22bdd6b8703cb&itag=141"
                + "&source=youtube&requiressl=yes&mm=31&ms=au&mv=m"
                + "&ratebypass=yes&mime=audio/mp4&gir=yes&clen=6670723"
                + "&lmt=1394281651289479&dur=209.211"
                + "&key=dg_yt0&sver=3"
                + "&ipbits=0";
                //+ "&sparams=ip,ipbits,expire,id,itag,source,requiressl,pl,mn,mm,ms,mv,nh,ratebypass,mime,gir,clen,lmt,dur";

        YouTubeURLExtractor extractor = new YouTubeURLExtractor();
        URI uri = extractor.getDASHAudioURI("waIr3WuHA8s");

        for(String part : expectedQueryStringParts.split("&")) {
            assertThat(uri.toString(), containsString(part));
        }
    }

    @Test
    public void testGetAnotherDASHAudioURI() {
        String expectedURL = "id=c9e60d6a39f725d9&itag=141"
                + "&source=youtube&requiressl=yes&mm=31&ms=au&mv=m"
                + "&ratebypass=yes&mime=audio/mp4&gir=yes&clen=5224980&lmt=1387725644689690&dur=163.863"
                + "&key=dg_yt0"
                + "&sver=3"
                + "&ipbits=0";
                //+ "&sparams=ip,ipbits,expire,id,itag,source,requiressl,mm,mn,ms,mv,nh,pl,ratebypass,mime,gir,clen,lmt,dur";

        YouTubeURLExtractor extractor = new YouTubeURLExtractor();
        URI uri = extractor.getDASHAudioURI("yeYNajn3Jdk");

        for(String part : expectedURL.split("&")) {
            assertThat(uri.toString(), containsString(part));
        }
    }
}
