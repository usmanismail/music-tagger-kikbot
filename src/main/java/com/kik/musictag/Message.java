package com.kik.musictag;

import java.util.List;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = Inclusion.NON_DEFAULT)
public class Message
{
    boolean isTyping;
    String from;
    String text;
    String title;
    String url;
    String to;
    long timestamp;
    String mention;
    Set<String> participants;
    boolean readReceiptRequested;
    // is-typing, text, video
    String type;
    String id;
    String chatId;
    String body;
    String videoUrl;
    List<Keyboard> keyboards;

}
