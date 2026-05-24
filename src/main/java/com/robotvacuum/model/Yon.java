package com.robotvacuum.model;

/**
 * Robotun bakabileceği 4 ana yön.
 * Her yön x/y eksenindeki adım büyüklüğünü ve UI'da görünen adı taşır.
 *
 * Not (OOP): Enum içine davranış (tersi, sağaDön, solaDön) gömdük.
 * Böylece "yön değişimi" mantığı tek bir yerden yönetiliyor, başka
 * yerlerde switch-case tekrarlamak zorunda kalmıyoruz.
 */
public enum Yon {

    KUZEY(0, -1, "Kuzey (↑)"),
    DOGU (1,  0, "Doğu (→)"),
    GUNEY(0,  1, "Güney (↓)"),
    BATI (-1, 0, "Batı (←)");

    // x ve y'deki adım miktarı (örn. doğuda dx=+1)
    private final int dx;
    private final int dy;
    private final String gorunenAd;

    Yon(int dx, int dy, String gorunenAd) {
        this.dx = dx;
        this.dy = dy;
        this.gorunenAd = gorunenAd;
    }

    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public String getGorunenAd() { return gorunenAd; }

    /** Yönün tam tersini döndürür (kuzey↔güney, doğu↔batı). */
    public Yon tersi() {
        switch (this) {
            case KUZEY: return GUNEY;
            case GUNEY: return KUZEY;
            case DOGU:  return BATI;
            case BATI:  return DOGU;
            default:    return this;
        }
    }

    /** Saat yönünde 90° döner. (Kuzey → Doğu → Güney → Batı → Kuzey) */
    public Yon sagaDon() {
        switch (this) {
            case KUZEY: return DOGU;
            case DOGU:  return GUNEY;
            case GUNEY: return BATI;
            case BATI:  return KUZEY;
            default:    return this;
        }
    }

    /** Saat yönünün tersine 90° döner. */
    public Yon solaDon() {
        switch (this) {
            case KUZEY: return BATI;
            case BATI:  return GUNEY;
            case GUNEY: return DOGU;
            case DOGU:  return KUZEY;
            default:    return this;
        }
    }
}
