package com.robotvacuum.model;

public class Mobilya {
    private MobilyaTipi tip;
    private int startX;
    private int startY;
    private boolean yatayMi;

    public Mobilya(MobilyaTipi tip, int startX, int startY, boolean yatayMi) {
        this.tip = tip;
        this.startX = startX;
        this.startY = startY;
        this.yatayMi = yatayMi;
    }

    public MobilyaTipi getTip() {
        return tip;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }
    
    public boolean isYatayMi() {
        return yatayMi;
    }

    public int getGenislik() {
        return yatayMi ? tip.getGenislik() : tip.getYukseklik();
    }

    public int getYukseklik() {
        return yatayMi ? tip.getYukseklik() : tip.getGenislik();
    }

    public java.util.List<int[]> getKaplananHucreler() {
        java.util.List<int[]> hucreler = new java.util.ArrayList<>();
        int w = getGenislik();
        int h = getYukseklik();

        if (tip == MobilyaTipi.L_KANEPE) {
            // L-shape: Covers the long edge (startY to startY+h-1 for x=startX) 
            // and the bottom edge (startX to startX+w-1 for y=startY+h-1)
            // Or something similar based on orientation. Let's make a standard L:
            // Left column and Bottom row.
            for (int y = 0; y < h; y++) {
                hucreler.add(new int[]{startX, startY + y});
            }
            for (int x = 1; x < w; x++) {
                hucreler.add(new int[]{startX + x, startY + h - 1});
            }
        } else {
            // Rectangle
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    hucreler.add(new int[]{startX + x, startY + y});
                }
            }
        }
        return hucreler;
    }
}
