package me.perrycate.groupmeutils.data;

/**
 * Some sort of file or media that is part of a message. Payload and methods
 * depend on attachment type.
 */
public interface Attachment {
    public AttachmentType getType();
}
