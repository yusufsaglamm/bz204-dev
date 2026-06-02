package com.robotvacuum.model;

public enum MobilyaTipi {
    KANEPE(3, 2, "Kanepe");

    private final int genislikHucresi;
    private final int yukseklikHucresi;
    private final String ekranAdi;

    MobilyaTipi(int genislik, int yukseklik, String ekranAdi) {
        this.genislikHucresi = genislik;
        this.yukseklikHucresi = yukseklik;
        this.ekranAdi = ekranAdi;
    }

    public int getGenislik() { return genislikHucresi; }
    public int getYukseklik() { return yukseklikHucresi; }
    public String getEkranAdi() { return ekranAdi; }
}
