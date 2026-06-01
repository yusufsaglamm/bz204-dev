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
            // Sol dikey 2x4 parça (x: startX..startX+1, y: startY..startY+3)
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 4; y++) {
                    hucreler.add(new int[]{startX + x, startY + y});
                }
            }
            // Alt sağ 2x2 parça (x: startX+2..startX+3, y: startY+2..startY+3)
            for (int x = 2; x < 4; x++) {
                for (int y = 2; y < 4; y++) {
                    hucreler.add(new int[]{startX + x, startY + y});
                }
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
