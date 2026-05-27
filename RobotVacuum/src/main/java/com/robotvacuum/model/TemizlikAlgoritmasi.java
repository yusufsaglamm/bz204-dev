package com.robotvacuum.model;

/**
 * Robot süpürgenin odayı temizlerken kullanabileceği hareket rotası algoritmaları.
 */
public enum TemizlikAlgoritmasi {
    RASTGELE("Rastgele"),
    SPIRAL("Spiral"),
    DUVAR_TAKIP("Duvar Takip");

    private final String ekranAdi;

    TemizlikAlgoritmasi(String ekranAdi) {
        this.ekranAdi = ekranAdi;
    }

    public String getEkranAdi() { return ekranAdi; }
}
