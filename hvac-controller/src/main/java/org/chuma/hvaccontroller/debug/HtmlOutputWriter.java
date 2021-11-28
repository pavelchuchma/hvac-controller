package org.chuma.hvaccontroller.debug;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class HtmlOutputWriter implements IOutputWriter {
    OutputStreamWriter w;
    private String[] colors = {"pink", "lightgreen", "orange", "lightblue", "MediumSeaGreen"};

    public HtmlOutputWriter(OutputStreamWriter writer) throws IOException {
        w = writer;
    }

    @Override
    public void open() throws IOException {
        w.append("<code><pre>\n");
    }

    @Override
    public void close() throws IOException {
        w.append("</pre></code>\n");
        w.close();
    }

    @Override
    public void append(String s) throws IOException {
        w.append(s);
    }

    @Override
    public void appendColorized(String text, String color, String backgroundColor) throws IOException {
        append(colorize(text, color, backgroundColor));
    }

    @Override
    public void appendColorized(String text, int colorIndex) throws IOException {
        appendColorized(text, "black", colors[colorIndex % colors.length]);
    }

    @Override
    public String colorize(String text, String color, String backgroundColor) {
        return String.format("<span style=\"background-color: %s; color: %s\">%s</span>", backgroundColor, color, text);
    }

    @Override
    public String colorize(String text, int colorIndex) {
        return colorize(text, "black", colors[colorIndex % colors.length]);
    }
}
