package com.robotvacuum.model;

public class Mobilya {
    private MobilyaTipi tip;
    private int startX;
    private int startY;
    private Yon yon;

    public Mobilya(MobilyaTipi tip, int startX, int startY, Yon yon) {
        this.tip = tip;
        this.startX = startX;
        this.startY = startY;
        this.yon = yon;
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
    
    public Yon getYon() {
        return yon;
    }

    public int getGenislik() {
        return (yon == Yon.DOGU || yon == Yon.BATI) ? tip.getYukseklik() : tip.getGenislik();
    }

    public int getYukseklik() {
        return (yon == Yon.DOGU || yon == Yon.BATI) ? tip.getGenislik() : tip.getYukseklik();
    }

    public java.util.List<int[]> getKaplananHucreler() {
        java.util.List<int[]> hucreler = new java.util.ArrayList<>();
        int w = getGenislik();
        int h = getYukseklik();

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                hucreler.add(new int[]{startX + x, startY + y});
            }
        }
        return hucreler;
    }
}
