package com.engineering.software.thewatch;

/**
 * Author: King
 * Date: 1/29/2017
 */

public enum Tag {
    //@TODO Add more TAGS
    ACCIDENT(0, "Accident"),
    EVENT(1, "Event");

    public final int num;
    public final String text;

    Tag(int num, String text) {
        this.num = num;
        this.text = text;
    }

    public Tag get(int tagNum) {
        switch (num) {
            case 0:
                return Tag.ACCIDENT;
            case 1:
                return Tag.EVENT;
            default:
                throw new IllegalArgumentException("Parameter tagNum does not correspond to a tag.");
        }
    }
}
