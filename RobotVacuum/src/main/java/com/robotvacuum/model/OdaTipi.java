package com.robotvacuum.model;

/**
 * Simülasyonda kullanılabilecek oda planı tiplerini ve arayüz isimlerini barındırır.
 * Yeni bir oda eklenmek istendiğinde sadece buraya yeni bir satır eklenmesi yeterlidir.
 */
public enum OdaTipi {
    SALON("Salon"),
    MUTFAK("Mutfak"),
    YATAK_ODASI("Yatak Odası");

    private final String ekranAdi;

    OdaTipi(String ekranAdi) {
        this.ekranAdi = ekranAdi;
    }

    public String getEkranAdi() {
        return ekranAdi;
    }
}