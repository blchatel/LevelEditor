package ch.epfl.blchatel.leveleditor.io;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a simple file system, where each file is associated to a unique name.
 */
public interface FileSystem {
    
    /**
     * Open an existing file for read.
     * @param name (String): unique identifier, not null
     * @return (InputStream): content stream, not null
     * @throws IOException if file cannot be open for read
     */
    InputStream read(String name) throws IOException;

    /**
     * Open an existing Image file for read.
     * @param name (String): unique identifier, not null
     * @return (Image): content image, may be null if no image found
     */
    Image readImage(String name);

    /**
     * Open file for write, previous content overwritten if any.
     * @param name (String): unique identifier, not null
     * @return (InputStream): content stream, not null
     * @throws IOException if file cannot be open for write
     */
    OutputStream write(String name) throws IOException;
    
}
