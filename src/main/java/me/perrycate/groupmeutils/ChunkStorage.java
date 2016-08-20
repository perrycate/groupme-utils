package me.perrycate.groupmeutils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * Stores (ideally) relatively small arrays of bytes (referred to as Chunks) as
 * a Deque. Chunks are stored in files, not kept in memory. As a result, expect
 * reading and writing to be relatively slow, but memory usage will be low,
 * since we're not actually storing the Chunks in memory.
 * 
 * In other words, use this class when you have more data (in small pieces)
 * than you want to keep in memory at a time and are willing to pay a slight
 * performance penalty for it.
 */
public class ChunkStorage implements Iterable<byte[]>, Closeable {
    // NOTE: this is essentially a Deque, but I decided not to explicitly
    // implement Deque<byte[]> because there are lots of methods I choose not to
    // implement. (Anything that involves checking every single chunk, I left
    // out, since the performance penalty of reading all those individual files
    // would've defeated the purpose of this class anyway.

    private static String FILE_EXT = ".cnk";

    // Location where chunks will be stored
    private Path baseDir;

    // Each Path is to a single chunk 
    private ArrayDeque<Path> chunks;

    // Number of chunks ever added. We base chunk path names off this so that
    // no two chunks ever have the same name.
    private int chunksAdded = 0;

    public ChunkStorage() {
        try {
            baseDir = Files.createTempDirectory("groupme-dump-");
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

        chunks = new ArrayDeque<Path>();
    }

    /**
     * Adds each array of bytes in the collection as a new chunk in order, where
     * the first item in the collection is the head of the deque.
     */
    public boolean addAll(Collection<? extends byte[]> c) {
        for (byte[] data : c) {
            this.addLast(data);
        }
        return c.size() != 0;
    }

    /**
     * Deletes all stored chunks.
     */
    public void clear() {
        for (Path p : chunks) {
            delete(p);
        }

        chunks.clear();
    }

    public boolean isEmpty() {
        return chunks.isEmpty();
    }

    /**
     * Returns a 2D array of all the chunks stored in order from first to last
     * element.
     */
    public byte[][] toArray() {
        byte[][] chunksArray = new byte[chunks.size()][];

        for (int i = 0; chunks.size() != 0; i++) {
            chunksArray[i] = read(chunks.removeFirst());
        }

        return chunksArray;
    }

    /**
     * Adds data chunk to the head of the deque. 
     */
    public void addFirst(byte[] data) {
        Path chunk = getNewChunk();
        write(chunk, data);
        chunks.addFirst(chunk);
    }

    /**
     * Adds data chunk to the tail of the deque.
     */
    public void addLast(byte[] data) {
        Path chunk = getNewChunk();
        write(chunk, data);
        chunks.addLast(chunk);
    }

    /**
     * Returns the data chunk at the head of the deque, but does not remove it.
     */
    public byte[] getFirst() {
        return read(chunks.getFirst());
    }

    /**
     * Returns the data chunk at the tail of the deque, but does not remove it.
     */
    public byte[] getLast() {
        return read(chunks.getLast());
    }

    /**
     * Attempts to add data chunk to head of the deque. On a fail, return false,
     * true otherwise. (Will not throw an exception.)
     */
    public boolean offerFirst(byte[] data) {
        Path chunk = getNewChunk();
        write(chunk, data);
        return chunks.offerFirst(chunk);
    }

    /**
     * Attempts to add data chunk to the tail of deque. On a fail, return false,
     * true otherwise. (Will not throw an exception.)
     */
    public boolean offerLast(byte[] data) {
        Path chunk = getNewChunk();
        write(chunk, data);
        return chunks.offerLast(chunk);
    }

    /**
     * Removes and returns the chunk at the head of the deque. Returns null if
     * the deque is empty.
     */
    public byte[] pollFirst() {
        if (chunks.isEmpty())
            return null;

        return readThenDelete(chunks.pollFirst());
    }

    /**
     * Removes and returns the chunk at the tail of the deque. Returns null if
     * the deque is empty.
     */
    public byte[] pollLast() {
        if (chunks.isEmpty())
            return null;

        return readThenDelete(chunks.pollLast());
    }

    /**
     * Removes and returns the chunk at the head of the deque. Throws a
     * NoSuchElementException if the deque is empty.
     */
    public byte[] removeFirst() {
        return readThenDelete(chunks.removeFirst());
    }

    /**
     * Removes and returns the chunk at the tail of the deque. Throws a
     * NoSuchElementException if the deque is empty.
     */
    public byte[] removeLast() {
        return readThenDelete(chunks.removeFirst());
    }

    public int size() {
        return chunks.size();
    }

    /**
     * Returns an iterator ordered such that the head of the deque is the first
     * item in the iterator.
     */
    public Iterator<byte[]> iterator() {
        return new ChunkStorageIterator(chunks.iterator());
    }

    /**
     * Returns an iterator ordered such that the tail of the deque is the first
     * item in the iterator.
     */
    public Iterator<byte[]> descendingIterator() {
        return new ChunkStorageIterator(chunks.descendingIterator());
    }

    /**
     * Frees all related resources associated with this object.
     */
    public void close() throws IOException {
        clear();
        Files.delete(baseDir);
    }

    private class ChunkStorageIterator implements Iterator<byte[]> {

        private Iterator<Path> paths;

        public ChunkStorageIterator(Iterator<Path> iterator) {
            paths = iterator;
        }

        public boolean hasNext() {
            return paths.hasNext();
        }

        public byte[] next() {
            return read(paths.next());
        }

    }

    private Path getNewChunk() {
        String name = baseDir.toString() + File.separator + chunksAdded
                + FILE_EXT;
        chunksAdded++;
        return Paths.get(name);
    }

    private byte[] read(Path chunk) {
        try {
            return Files.readAllBytes(chunk);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void write(Path chunk, byte[] data) {
        try {
            Files.write(chunk, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void delete(Path chunk) {
        try {
            Files.delete(chunk);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] readThenDelete(Path chunk) {
        byte[] data = read(chunk);
        delete(chunk);
        return data;
    }
}
