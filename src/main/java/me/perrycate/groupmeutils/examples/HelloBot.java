package me.perrycate.groupmeutils.examples;

import me.perrycate.groupmeutils.api.Bot;
import me.perrycate.groupmeutils.api.Responder;
import me.perrycate.groupmeutils.data.Message;

public class HelloBot implements Bot {

    @Override
    public void handleMessage(Message m, Responder r) {
        if (m.getText().toLowerCase().contains("hello, world!")) {
            r.sendMessage("Hello, " + m.getName() + "!");
        }
    }
}
