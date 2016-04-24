package com.kik.musictag;

import java.util.function.Function;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.codehaus.jackson.map.ObjectMapper;

import com.acrcloud.utils.ACRCloudRecognizer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import model.Details;
import model.ExternalData;
import model.TagResponse;

@Path("/message")
@Singleton
@Slf4j
public class MessageResource
{
    private static final String BOTS_URL = "https://api.kik.com/v1/message";
    private static final String ACR_ACCESS_KEY = "ACR_ACCESS_KEY";
    private static final String ACR_ACCESS_SECRET = "ACR_ACCESS_SECRET";
    private static final String KIK_BOT_NAME = "KIK_BOT_NAME";
    private static final String KIK_API_KEY = "KIK_API_KEY";
    private HttpClient client;
    private HttpClientContext context;
    private ObjectMapper mapper;
    private HttpHost targetHost;
    private ACRCloudRecognizer recognizer;
    // Need to limit this or put it in a database with expiry
    private Map<String, TagResponse> lastResponse = new HashMap<>();

    @Inject
    public MessageResource(ObjectMapper mapper)
    {
        client = HttpClients.createDefault();
        targetHost = new HttpHost("api.kik.com", 443, "https");
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(KIK_BOT_NAME, KIK_API_KEY));

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        this.mapper = mapper;

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("host", "ap-southeast-1.api.acrcloud.com");
        config.put("access_key", ACR_ACCESS_KEY);
        config.put("access_secret", ACR_ACCESS_SECRET);
        config.put("debug", false);
        config.put("timeout", 30); // seconds

        recognizer = new ACRCloudRecognizer(config);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postMessage(@Context HttpServletRequest request, MessageSet messageSet)
    {

        MessageSet responseSet = new MessageSet();
        log.info("Post Message Called {}", messageSet.toString());
        for (Message message : messageSet.getMessages()) {
            try {
                if (message.getType().equals("video")) {

                    log.debug("Trying to recognize {}", message.getVideoUrl());
                    Message response = new Message();
                    response.setChatId(message.getChatId());
                    response.setTo(message.getFrom());
                    response.setType("text");
                    TagResponse songData = getSong(message.getVideoUrl());
                    if (songData.getStatus().getMsg().equals("Success")) {
                        Details songMetaData = songData.getMetadata().getMusic().get(0);
                        response.setBody(String.format("%s by %s", songMetaData.getTitle(), songMetaData.getArtists()
                                .stream().map(i -> i.get("name")).collect(Collectors.joining(", "))));

                        response.setKeyboards(Arrays.asList(new Keyboard().withType("suggested")
                                .withResponses(songMetaData.getExternalData().keySet().stream().map(i -> new SuggestedResponse("text", i)).collect(Collectors.toList()))));
                        responseSet.getMessages().add(response);
                        lastResponse.put(message.getChatId(), songData);
                    } else {
                        response.setBody("Sorry song not found :(");
                    }
                    responseSet.getMessages().add(response);
                } else if (message.getType().equals("text") && message.getBody().toLowerCase().equals("test")) {
                    Message response = new Message();
                    response.setChatId(message.getChatId());
                    response.setTo(message.getFrom());
                    response.setType("text");
                    TagResponse songData = getSample("/sample.m4a");
                    if (songData.getStatus().getMsg().equals("Success")) {
                        Details songMetaData = songData.getMetadata().getMusic().get(0);
                        response.setBody(String.format("%s by %s", songMetaData.getTitle(), songMetaData.getArtists()
                                .stream().map(i -> i.get("name")).collect(Collectors.joining(", "))));

                        response.setKeyboards(Arrays.asList(new Keyboard().withType("suggested")
                                .withResponses(songMetaData.getExternalData().keySet().stream().map(i -> new SuggestedResponse("text", i)).collect(Collectors.toList()))));
                        responseSet.getMessages().add(response);
                        lastResponse.put(message.getChatId(), songData);
                    } else {
                        response.setBody("Sorry song not found :(");
                    }

                } else if (message.getType().equals("text") && message.getBody().toLowerCase().equals("test1")) {
                    Message response = new Message();
                    response.setChatId(message.getChatId());
                    response.setTo(message.getFrom());
                    response.setType("text");
                    TagResponse songData = getSample("/sample1.m4a");
                    if (songData.getStatus().getMsg().equals("Success")) {
                        Details songMetaData = songData.getMetadata().getMusic().get(0);
                        response.setBody(String.format("%s by %s", songMetaData.getTitle(), songMetaData.getArtists()
                                .stream().map(i -> i.get("name")).collect(Collectors.joining(", "))));

                        response.setKeyboards(Arrays.asList(new Keyboard().withType("suggested")
                                .withResponses(songMetaData.getExternalData().keySet().stream().map(i -> new SuggestedResponse("text", i)).collect(Collectors.toList()))));
                        responseSet.getMessages().add(response);
                        lastResponse.put(message.getChatId(), songData);
                    } else {
                        response.setBody("Sorry song not found :(");
                    }

                }else if (message.getType().equals("text")) {
                    TagResponse songData = lastResponse.get(message.getChatId());
                    if (songData != null) {
                        log.debug("Found chat ID");
                        Details songMetaData = songData.getMetadata().getMusic().get(0);
                        ExternalData externalData = songMetaData.getExternalData().get(message.getBody());
                        if (externalData != null) {
                            log.debug("Found External Data");
                            Message response = getExternalDetailMessage(message.getBody(), externalData, songMetaData);
                            response.setChatId(message.getChatId());
                            response.setTo(message.getFrom());
                            response.setKeyboards(Arrays.asList(new Keyboard().withType("suggested")
                                    .withResponses(songMetaData.getExternalData().keySet().stream().map(i -> new SuggestedResponse("text", i)).collect(Collectors.toList()))));
                            responseSet.getMessages().add(response);
                        } else {
                            Message response = new Message();
                            response.setChatId(message.getChatId());
                            response.setTo(message.getFrom());
                            response.setType("text");
                            response.setBody("External Provider not found" + message.getBody());
                            responseSet.getMessages().add(response);
                        }
                    } else {
                        Message response = new Message();
                        response.setChatId(message.getChatId());
                        response.setTo(message.getFrom());
                        response.setType("text");
                        response.setBody("Chat ID Not Found");
                        responseSet.getMessages().add(response);
                    }
                }
            } catch (Exception ex) {
                Message response = new Message();
                response.setChatId(message.getChatId());
                response.setTo(message.getFrom());
                response.setType("text");
                response.setBody("Unable to decode song :(");
                responseSet.getMessages().add(response);
            }
        }
        if (responseSet.getMessages().size() > 0) {
            sendMessage(responseSet);
        }
        return Response.ok().build();

    }

    private Message getExternalDetailMessage(String externalSource, ExternalData externalData, Details songMetaData)
    {
        Message response = new Message();
        if (externalSource.equals("youtube")) {
            response.setType("link");
            response.setTitle(songMetaData.getTitle());
            response.setText(String.format("%s by %s", songMetaData.getTitle(), songMetaData.getArtists()
                    .stream().map(i -> i.get("name")).collect(Collectors.joining(", "))));
            response.setUrl(String.format("https://youtu.be/%s", externalData.getVid()));
        } else if (externalSource.equals("itunes")) {
            response.setType("link");
            response.setTitle(songMetaData.getTitle());
            response.setText(String.format("%s by %s", songMetaData.getTitle(), songMetaData.getArtists()
                    .stream().map(i -> i.get("name")).collect(Collectors.joining(", "))));
            response.setUrl(String.format("http://itunes.apple.com/us/album/id%s?i=%s", externalData.getAlbum().get("id"), externalData.getTrack().get("id")));
        } else if (externalSource.equals("spotify")) {
            response.setType("link");
            response.setTitle(songMetaData.getTitle());
            response.setText(String.format("%s by %s", songMetaData.getTitle(), songMetaData.getArtists()
                    .stream().map(i -> i.get("name")).collect(Collectors.joining(", "))));
            response.setUrl(String.format("http://open.spotify.com/track/%s", externalData.getTrack().get("id")));
        } else if (externalSource.equals("deezer")) {
            response.setType("link");
            response.setTitle(songMetaData.getTitle());
            response.setText(String.format("%s by %s", songMetaData.getTitle(), songMetaData.getArtists()
                    .stream().map(i -> i.get("name")).collect(Collectors.joining(", "))));
            response.setUrl(String.format("http://www.deezer.com/track/%s", externalData.getTrack().get("id")));
        }

        return response;
    }

    private TagResponse getSong(String videoUrl) throws ClientProtocolException, IOException
    {
        byte[] fileBuffer = getVideoBytes(videoUrl);
        String info = recognizer.recognizeByFileBuffer(fileBuffer, fileBuffer.length, 5);

        TagResponse response = mapper.readValue(info, TagResponse.class);
        log.debug("Song response", response.toString());
        return response;
    }

    private TagResponse getSample(String filename) throws IOException
    {
        InputStream resource = this.getClass().getResourceAsStream(filename);
        byte[] bytes = IOUtils.toByteArray(resource);
        String info = recognizer.recognizeByFileBuffer(bytes, bytes.length, 5);

        TagResponse response = mapper.readValue(info, TagResponse.class);
        log.debug("Song response", response.toString());
        return response;
    }

    private byte[] getVideoBytes(String videoUrl) throws ClientProtocolException, IOException
    {
        HttpGet httpGet = new HttpGet(videoUrl);
        try {
            HttpResponse response = client.execute(httpGet);
            log.debug("Get Video response {}", response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == 200) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                response.getEntity().writeTo(baos);
                return baos.toByteArray();
            } else {
                throw new RuntimeException("Unable to get video: " + response.getStatusLine().getReasonPhrase());
            }
        } finally {
            httpGet.releaseConnection();
        }
    }

    private void sendMessage(MessageSet messageSet)
    {
        try {
            HttpPost httpPost = new HttpPost(BOTS_URL);
            try {
                httpPost = new HttpPost(BOTS_URL);
                httpPost.addHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                log.debug("Writting Messages: {}", mapper.writeValueAsString(messageSet));
                httpPost.setEntity(new ByteArrayEntity(mapper.writeValueAsBytes(messageSet)));

                HttpResponse response = client.execute(targetHost, httpPost, context);
                log.debug("Send message response {}", response.getStatusLine().getStatusCode());
            } finally {
                httpPost.releaseConnection();
            }
        } catch (Exception ex) {
            log.debug("Unable to send messages", ex);
        }

    }

}
