package com.robotvacuum.model;

/**
 * Robotun hareket edebileceği ana yönleri temsil eden enum sınıfı.
 * Her yön, ızgara üzerindeki X ve Y eksenlerindeki yer değiştirme miktarını
 * (dx, dy) ve kullanıcıya gösterilecek Türkçe adını içerir.
 */
public enum Yon {

    /** Kuzey yönü: Y ekseninde -1 birim (yukarı) */
    KUZEY(0, -1, "Kuzey (↑)"),

    /** Doğu yönü: X ekseninde +1 birim (sağ) */
    DOGU(1, 0, "Doğu (→)"),

    /** Güney yönü: Y ekseninde +1 birim (aşağı) */
    GUNEY(0, 1, "Güney (↓)"),

    /** Batı yönü: X ekseninde -1 birim (sol) */
    BATI(-1, 0, "Batı (←)");

    /** X eksenindeki yer değiştirme miktarı */
    private final int dx;

    /** Y eksenindeki yer değiştirme miktarı */
    private final int dy;

    /** Kullanıcı arayüzünde görüntülenecek Türkçe ad */
    private final String gorunenAd;

    /**
     * Yön enum sabiti oluşturucusu.
     *
     * @param dx        X eksenindeki yer değiştirme (-1, 0 veya +1)
     * @param dy        Y eksenindeki yer değiştirme (-1, 0 veya +1)
     * @param gorunenAd Kullanıcıya gösterilecek Türkçe yön adı
     */
    Yon(int dx, int dy, String gorunenAd) {
        this.dx = dx;
        this.dy = dy;
        this.gorunenAd = gorunenAd;
    }

    /** @return X eksenindeki yer değiştirme miktarı */
    public int getDx() { return dx; }

    /** @return Y eksenindeki yer değiştirme miktarı */
    public int getDy() { return dy; }

    /** @return Kullanıcı arayüzünde görüntülenecek Türkçe ad */
    public String getGorunenAd() { return gorunenAd; }

    /**
     * Bu yönün tam tersini (180°) döndürür.
     * Örneğin KUZEY'in tersi GUNEY'dir.
     *
     * @return Zıt yön
     */
    public Yon tersi() {
        return switch (this) {
            case KUZEY -> GUNEY;
            case GUNEY -> KUZEY;
            case DOGU -> BATI;
            case BATI -> DOGU;
        };
    }

    /**
     * Saat yönünde 90° döndürülmüş yönü döndürür.
     * Örneğin KUZEY → DOGU → GUNEY → BATI → KUZEY.
     *
     * @return Saat yönünde 90° dönmüş yön
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
     * Saat yönünün tersine 90° döndürülmüş yönü döndürür.
     * Örneğin KUZEY → BATI → GUNEY → DOGU → KUZEY.
     *
     * @return Saat yönünün tersine 90° dönmüş yön
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
