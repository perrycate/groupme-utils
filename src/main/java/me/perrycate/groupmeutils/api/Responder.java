package me.perrycate.groupmeutils.api;

import java.io.File;

public interface Responder {
    public void sendMessage(String text);
    public void sendImage(String text, File image);
}
