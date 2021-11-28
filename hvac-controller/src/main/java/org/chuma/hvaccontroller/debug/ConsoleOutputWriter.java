package org.chuma.hvaccontroller.debug;

import java.io.IOException;

public class ConsoleOutputWriter implements IOutputWriter {
    private String[] colors = {"pink", "green", "yellow", "lightblue"};


    public ConsoleOutputWriter() {
    }

    private static String getColorCode(String color, boolean foreground) {
        switch (color) {
            case "black":
                // Black
                return foreground ? "30" : "40";
            case "red":
                // Red
                return foreground ? "31" : "41";
            case "green":
                // Green

                return foreground ? "32" : "42";
            case "yellow":
                // Yellow
                return foreground ? "33" : "43";
            case "blue":
                // Blue
                return foreground ? "34" : "44";
            case "white":
                // White
                return foreground ? "37" : "47";
            case "orange":
                // BrightRed
                return foreground ? "1;31" : "101";
            case "lightgreen":
                // BrightGreen
                return foreground ? "1;32" : "102";
            case "lightblue":
                // BrightBlue
                // return foreground ? "1;34" : "104";
                // Cyan
                return foreground ? "36" : "46";
            case "pink":
                // BrightMagenta
                return foreground ? "1;35" : "105";

            default:
                throw new IllegalArgumentException(color);
/*
// BrightCyan
                return foreground ? "1;36" : "106";
// BrightWhite
            return foreground ? "1;37" : "107";
// BrightYellow
            return foreground ? "1;33" : "103";
// BrightBlack
            return foreground ? "1;30" : "100";
// Magenta
            return foreground ? "35" : "45";

            */
        }
    }

    @Override
    public void open() {
    }

    @Override
    public void close() {
    }

    @Override
    public void append(String s) throws IOException {
        System.out.print(s);
    }

    @Override
    public void appendColorized(String text, String color, String backgroundColor) throws IOException {
        append(colorize(text, color, backgroundColor));
    }

    public void appendColorized(String text, int colorIndex) throws IOException {
        appendColorized(text, "black", colors[colorIndex % colors.length]);
    }

    @Override
    public String colorize(String text, String color, String backgroundColor) {

        String fc = getColorCode(backgroundColor, true);
        String bc = getColorCode(color, false);


        char escapeChar = (char) 27;
//        char escapeChar = '@';
        return String.format("%s[%s;%sm%s%s[0m", escapeChar, fc, bc, text, escapeChar);
    }

    @Override
    public String colorize(String text, int colorIndex) {
        return colorize(text, "black", colors[colorIndex % colors.length]);
    }
}
