package com.engineering.software.thewatch.model.db;

/**
 * Author: King
 * Date: 1/29/2017
 */

public enum Mood {
    HAPPY(0, "Happy"),
    EXCITED(1, "Excited"),
    SAD(2, "Sad"),
    ANGRY(3, "Angry"),
    WORRIED(4, "Worried"),
    ANNOYED(5, "Annoyed"),
    FRUSTRATED(6, "Frustrated"),
    DISAPPOINTED(7, "Disappointed"),
    SCARED(8, "Scared"),
    INDIFFERENT(9, "Indifferent");

    public final int num;
    public final String text;

    public static final int COUNT = 10;

    Mood(int num, String text) {
        this.num = num;
        this.text = text;
    }

    static public Mood getValue(int i){
        switch (i){
            case 0:
                return Mood.HAPPY;
            case 1:
                return Mood.EXCITED;
            case 2:
                return Mood.SAD;
            case 3:
                return Mood.ANGRY;
            case 4:
                return Mood.WORRIED;
            case 5:
                return Mood.ANNOYED;
            case 6:
                return Mood.FRUSTRATED;
            case 7:
                return Mood.DISAPPOINTED;
            case 8:
                return Mood.SCARED;
            case 9:
                return Mood.INDIFFERENT;
            default:
                return null;
        }
    }
}
