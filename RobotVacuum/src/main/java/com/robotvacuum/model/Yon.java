package com.robotvacuum.model;

/**
 * Robotun odadaki hareket yönlerini temsil eden enum sınıfı.
 * Hem yönlerin koordinat değişimlerini (dx, dy) hem de ekranda görünecek isimlerini tutuyor.
 */
public enum Yon {
    KUZEY(0, -1, "Kuzey (↑)"),
    DOGU(1, 0, "Doğu (→)"),
    GUNEY(0, 1, "Güney (↓)"),
    BATI(-1, 0, "Batı (←)");

    // Koordinat düzlemindeki değişim miktarları (X ve Y eksenlerinde)
    private final int dx;
    private final int dy;
    private final String ekranAdi;

    Yon(int dx, int dy, String ekranAdi) {
        this.dx = dx;
        this.dy = dy;
        this.ekranAdi = ekranAdi;
    }

    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public String getEkranAdi() { return ekranAdi; }

    /**
     * Robotun o anki yönünün tam tersini verir.
     * Mesela duvara toslayınca veya geri dönmek isteyince işe yarıyor.
     */
    public Yon tersYon() {
        return switch (this) {
            case KUZEY -> GUNEY;
            case GUNEY -> KUZEY;
            case DOGU -> BATI;
            case BATI -> DOGU;
        };
    }

    /**
     * Robotun yönünü saat yönünde 90 derece döndürür (Sağa döner yani).
     */
    public Yon sagaDon() {
        return switch (this) {
            case KUZEY -> DOGU;
            case DOGU -> GUNEY;
            case GUNEY -> BATI;
            case BATI -> KUZEY;
        };
    }

    /**
     * Robotun yönünü saat yönünün tersine 90 derece döndürür (Sola döner yani).
     */
    public Yon solaDon() {
        return switch (this) {
            case KUZEY -> BATI;
            case BATI -> GUNEY;
            case GUNEY -> DOGU;
            case DOGU -> KUZEY;
        };
    }
}
