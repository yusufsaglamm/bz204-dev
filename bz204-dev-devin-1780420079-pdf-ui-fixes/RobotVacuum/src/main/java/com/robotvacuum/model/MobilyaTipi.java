package com.robotvacuum.model;

public enum MobilyaTipi {
    KANEPE(5, 3, "Kanepe"),
    TV_UNITESI(6, 2, "TV Ünitesi"),
    SEHPA(3, 2, "Sehpa"),
    YEMEK_MASASI(4, 4, "Yemek Masası"),
    DOLAP(5, 2, "Dolap"),
    KOMODIN(2, 2, "Komodin"),
    TEZGAH(6, 2, "Mutfak Tezgahı"),
    YATAK(5, 8, "Yatak");

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
