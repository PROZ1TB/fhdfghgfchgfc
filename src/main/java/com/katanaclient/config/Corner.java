package com.katanaclient.config;

public enum Corner {
    TOP_LEFT("Top Left"), TOP_RIGHT("Top Right"), BOTTOM_LEFT("Bottom Left"), BOTTOM_RIGHT("Bottom Right");

    public final String label;

    Corner(String label) { this.label = label; }

    public Corner next() { return values()[(ordinal() + 1) % values().length]; }

    /** Top-left x of a box of the given size anchored in this corner. */
    public int x(int screenW, int boxW, int margin) {
        return (this == TOP_LEFT || this == BOTTOM_LEFT) ? margin : screenW - boxW - margin;
    }

    public int y(int screenH, int boxH, int margin) {
        return (this == TOP_LEFT || this == TOP_RIGHT) ? margin : screenH - boxH - margin;
    }
}
