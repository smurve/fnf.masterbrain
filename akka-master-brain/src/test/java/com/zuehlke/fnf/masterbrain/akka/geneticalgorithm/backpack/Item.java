package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.backpack;

/**
 * Created by mhan on 09.07.2015.
 */
public enum Item {
    SAMSUNG_GALAXY_S3_WITH_HD_AWESOME_DISPLAY(16, 7),
    IPAD(15, 6),
    OLD_BANANA(1, 4),
    BESEN(7, 7),
    SUDELNUPPE(5, 5),
    RANZIGER_SCHIRM(5, 6),
    A(15, 10),
    DELL(20, 9),
    IPOD(10, 8),
    CUP_OF_COFEE(5, 7),
    ONE_SHOE(2, 2),
    TWO_SHOES(6, 4);


    private final int value;
    private final int size;

    Item(final int value, final int size) {
        this.value = value;
        this.size = size;
    }

    public int getValue() {
        return value;
    }

    public int getSize() {
        return size;
    }
}
