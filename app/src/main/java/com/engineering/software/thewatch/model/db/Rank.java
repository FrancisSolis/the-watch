package com.engineering.software.thewatch.model.db;

/**
 * Author: King
 * Date: 1/29/2017
 */

/**
 * Enum class for all Ranks.
 * Contains information regarding each rank: Rank name, Rank value, and Rank Color.
 */
public enum Rank {
    SENTINEL(6, "Sentinel", 12150, (long) Math.pow(2, 63) - 1),
    MAVEN(5, "Maven", 4050, 12150),
    CHRONICLER(4, "Chronicler", 1350, 4050),
    SPY(3, "Spy", 450, 1350),
    SCOUT(2, "Scout", 150, 450),
    SPOTTER(1, "Spotter", 50, 150),
    WATCHER(0, "Watcher", 0, 50),

    NAYSAYER(-1, "Naysayer", -((long) Math.pow(2, 63)), 0);

    public static final int COUNT = 8;
    public static final int MAX = 6;
    public static final int MIN = -1;

    public final int num;
    public final String text;
    public final long min;
    public final long max;

    /**
     *
     * @param num Integer representation of the Rank.
     * @param text String representation of the Rank.
     */
    Rank(int num, String text, long min, long max) {
        this.num = num;
        this.text = text;
        this.min = min;
        this.max = max;
    }

    /**
     * Gets the corresponding rank given a string.
     * @param rankName Name of the Rank.
     * @return An instance of a Rank enum.
     */
    public static Rank get(String rankName) {
        if (rankName.equals("Sentinel"))
            return Rank.SENTINEL;
        else if (rankName.equals("Maven"))
            return Rank.MAVEN;
        else if (rankName.equals("Chronicler"))
            return Rank.CHRONICLER;
        else if (rankName.equals("Spy"))
            return Rank.SPY;
        else if (rankName.equals("Scout"))
            return Rank.SCOUT;
        else if (rankName.equals("Spotter"))
            return Rank.SPOTTER;
        else if (rankName.equals("Watcher"))
            return Rank.WATCHER;
        else
            return Rank.NAYSAYER;
    }

    /**
     * Gets the corresponding rank given an integer.
     * @param rankNum Number of the Rank.
     * @return An instance of Rank enum.
     */
    public static Rank get(int rankNum) {
        switch (rankNum) {
            case 6:
                return Rank.SENTINEL;
            case 5:
                return Rank.MAVEN;
            case 4:
                return Rank.CHRONICLER;
            case 3:
                return Rank.SPY;
            case 2:
                return Rank.SCOUT;
            case 1:
                return Rank.SPOTTER;
            case 0:
                return Rank.WATCHER;
            case -1:
                return Rank.NAYSAYER;
            default: throw new IllegalArgumentException("Parameter \"rankNum\" is not a valid rank.");
        }
    }
}
