package ch.epfl.blchatel.leveleditor.io;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.NoSuchFileException;

/**
 * Empty implementation of file system.
 */
public enum DefaultFileSystem implements FileSystem {
    
    INSTANCE;

    @Override
    public InputStream read(String name) throws IOException {
        throw new NoSuchFileException(name);
    }

    @Override
    public Image readImage(String name) {
        return null;
    }

    @Override
    public OutputStream write(String name) throws IOException {
        throw new NoSuchFileException(name);
    }
}
