package com.engineering.software.thewatch.model.db;

/**
 * Author: King
 * Date: 1/29/2017
 */

/**
 * Enum class for all avatars.
 * Each enum contains Uri for the image within the Firebase Storage.
 */
public enum Avatar {
    A0(0, "https://firebasestorage.googleapis.com/v0/b/the-watch-539d3.appspot.com/o/" +
            "avatars%2Favatar_0.png?alt=media&token=e6103f3c-66ae-4323-8e4f-eb148a9ec98c"),
    A1(1, "https://firebasestorage.googleapis.com/v0/b/the-watch-539d3.appspot.com/o/" +
            "avatars%2Favatar_1.png?alt=media&token=a40d6c22-ee79-4a68-8c28-711cb5e04f7e"),
    A2(2, "https://firebasestorage.googleapis.com/v0/b/the-watch-539d3.appspot.com/o/" +
            "avatars%2Favatar_2.png?alt=media&token=46335544-4a88-4e34-81b6-65982b930273"),
    A3(3, "https://firebasestorage.googleapis.com/v0/b/the-watch-539d3.appspot.com/o/" +
            "avatars%2Favatar_3.png?alt=media&token=431869d3-f7f6-43c0-b08f-ae6d14f662b1"),
    A4(4, "https://firebasestorage.googleapis.com/v0/b/the-watch-539d3.appspot.com/o/" +
            "avatars%2Favatar_4.png?alt=media&token=f2060328-b413-4efc-969b-003e7ee89850"),
    A5(5, "https://firebasestorage.googleapis.com/v0/b/the-watch-539d3.appspot.com/o/" +
            "avatars%2Favatar_5.png?alt=media&token=fce6509a-1537-48e6-9a35-68d5d0e5e02c"),
    A6(6, "https://firebasestorage.googleapis.com/v0/b/the-watch-539d3.appspot.com/o/" +
            "avatars%2Favatar_6.png?alt=media&token=ed582a91-b9e6-43b9-bbda-cc3c5b8ca649"),
    A7(7, "https://firebasestorage.googleapis.com/v0/b/the-watch-539d3.appspot.com/o/" +
            "avatars%2Favatar_7.png?alt=media&token=9598fe15-9a65-41a2-941f-ff83248afadf"),
    A8(8, "https://firebasestorage.googleapis.com/v0/b/the-watch-539d3.appspot.com/o/" +
            "avatars%2Favatar_8.png?alt=media&token=6f7823a8-3bf7-4b25-93c3-a9be66f092c1");

    public int num;
    public String url;

    Avatar(int num, String url) {
        this.num = num;
        this.url = url;
    }

    public static Avatar get(int num) {
        switch (num) {
            case 0:
                return Avatar.A0;
            case 1:
                return Avatar.A1;
            case 2:
                return Avatar.A2;
            case 3:
                return Avatar.A3;
            case 4:
                return Avatar.A4;
            case 5:
                return Avatar.A5;
            case 6:
                return Avatar.A6;
            case 7:
                return Avatar.A7;
            case 8:
                return Avatar.A8;
            default:
                return null;
        }
    }
}
