package org.chuma.hvaccontroller.debug;

import java.io.IOException;

public interface IOutputWriter {
    void open() throws IOException;

    void close() throws IOException;

    void append(String s) throws IOException;

    void appendColorized(String text, String color, String backgroundColor) throws IOException;

    void appendColorized(String text, int colorIndex) throws IOException;

    String colorize(String text, String color, String backgroundColor);

    String colorize(String text, int colorIndex);
}
