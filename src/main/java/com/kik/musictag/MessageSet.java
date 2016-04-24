package com.kik.musictag;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class MessageSet
{
    Set<Message> messages;
    public MessageSet()
    {
       messages = new HashSet<Message>();
    }
}
