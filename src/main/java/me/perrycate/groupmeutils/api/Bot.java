package me.perrycate.groupmeutils.api;

import me.perrycate.groupmeutils.data.Message;

public interface Bot {
    public void handleMessage(Message m, Responder r);
}
