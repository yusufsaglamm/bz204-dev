package com.robotvacuum.model;

public enum MobilyaTipi {
    TEKLI_KOLTUK(2, 2, "Tekli Koltuk"),
    YATAY_KANEPE(4, 2, "Yatay Kanepe"),
    DIKEY_KANEPE(2, 4, "Dikey Kanepe"),
    L_KANEPE(4, 4, "L Kanepe");

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
