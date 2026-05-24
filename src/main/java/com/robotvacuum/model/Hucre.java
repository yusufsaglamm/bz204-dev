package com.robotvacuum.model;

/**
 * Oda ızgarasındaki tek bir hücreyi temsil eden sınıf.
 * Bir hücre zemin, engel (mobilya) veya şarj istasyonu olabilir.
 * Ayrıca üzerinde farklı türlerde kir bulunabilir ve temizlenme durumunu takip eder.
 */
public class Hucre {

    /**
     * Hücre tiplerini tanımlayan iç enum sınıfı.
     * Bir hücre üç tipten biri olabilir: zemin, engel veya şarj istasyonu.
     */
    public enum HucreTipi {
        /** Normal zemin hücresi - robot üzerinden geçebilir */
        ZEMIN,
        /** Engel (mobilya) - robot üzerinden geçemez */
        ENGEL,
        /** Şarj istasyonu - robotun bataryasını şarj ettiği nokta */
        SARJ_ISTASYONU
    }

    /** Hücrenin ızgaradaki X koordinatı (sütun) */
    private final int x;

    /** Hücrenin ızgaradaki Y koordinatı (satır) */
    private final int y;

    /** Hücrenin mevcut tipi (zemin, engel veya şarj istasyonu) */
    private HucreTipi tip;

    /** Hücre üzerindeki kir türü (null ise kir yok) */
    private KirTipi kirTipi;

    /** Kiri tamamen temizlemek için kalan adım sayısı */
    private int kalanTemizlikAdimi;

    /** Hücrenin en az bir kere temizlenip temizlenmediğini gösteren bayrak */
    private boolean temiz;

    /**
     * Belirtilen koordinatlarda yeni bir zemin hücresi oluşturur.
     * Başlangıçta kir yoktur ve hücre temizlenmemiş durumdadır.
     *
     * @param x Hücrenin X koordinatı (sütun indeksi)
     * @param y Hücrenin Y koordinatı (satır indeksi)
     */
    public Hucre(int x, int y) {
        this.x = x;
        this.y = y;
        this.tip = HucreTipi.ZEMIN;
        this.kirTipi = null;
        this.kalanTemizlikAdimi = 0;
        this.temiz = false;
    }

    /** @return Hücrenin X koordinatı (sütun) */
    public int getX() { return x; }

    /** @return Hücrenin Y koordinatı (satır) */
    public int getY() { return y; }

    /** @return Hücrenin mevcut tipi */
    public HucreTipi getTip() { return tip; }

    /**
     * Hücrenin tipini değiştirir.
     *
     * @param tip Yeni hücre tipi
     */
    public void setTip(HucreTipi tip) { this.tip = tip; }

    /** @return Bu hücre bir engel (mobilya) mi? */
    public boolean engelMi() { return tip == HucreTipi.ENGEL; }

    /** @return Bu hücre şarj istasyonu mu? */
    public boolean sarjIstasyonuMu() { return tip == HucreTipi.SARJ_ISTASYONU; }

    /** @return Bu hücrede temizlenmemiş kir var mı? */
    public boolean kirVarMi() { return kirTipi != null && kalanTemizlikAdimi > 0; }

    /** @return Hücredeki kir türü (null ise kir yok) */
    public KirTipi getKirTipi() { return kirTipi; }

    /**
     * Hücreye belirtilen türde kir yerleştirir.
     * Engel veya şarj istasyonu hücrelerine kir eklenemez.
     *
     * @param kirTipi Eklenecek kir türü
     */
    public void setKir(KirTipi kirTipi) {
        // Engel ve şarj istasyonu üzerine kir konamaz
        if (tip == HucreTipi.ENGEL || tip == HucreTipi.SARJ_ISTASYONU) return;
        this.kirTipi = kirTipi;
        this.kalanTemizlikAdimi = kirTipi.getTemizlikAdimSayisi();
        this.temiz = false;
    }

    /**
     * Hücredeki kiri tamamen siler (anında temizlik).
     * Kalan temizlik adımı 0'a düşer ve kir türü null olur.
     */
    public void kiriTemizle() {
        this.kirTipi = null;
        this.kalanTemizlikAdimi = 0;
    }

    /**
     * Bir temizlik adımı uygular. Kir türüne göre birden fazla adım gerekebilir.
     * Kalan adım sayısı 0'a ulaştığında kir tamamen temizlenmiş olur.
     *
     * @return Kir tamamen temizlendiyse true, hâlâ devam ediyorsa false
     */
    public boolean temizle() {
        // Kir yoksa hücreyi temizlenmiş olarak işaretle
        if (!kirVarMi()) {
            temiz = true;
            return true;
        }
        // Kalan temizlik adımını bir azalt
        kalanTemizlikAdimi--;
        if (kalanTemizlikAdimi <= 0) {
            // Kir tamamen temizlendi
            kirTipi = null;
            kalanTemizlikAdimi = 0;
            temiz = true;
            return true;
        }
        // Temizlik devam ediyor
        return false;
    }

    /** @return Kiri tamamen temizlemek için kalan adım sayısı */
    public int getKalanTemizlikAdimi() { return kalanTemizlikAdimi; }

    /** @return Hücre daha önce en az bir kere temizlendi mi? */
    public boolean temizMi() { return temiz; }

    /**
     * Hücrenin temizlenme durumunu ayarlar.
     *
     * @param temiz true ise hücre temizlenmiş olarak işaretlenir
     */
    public void setTemiz(boolean temiz) { this.temiz = temiz; }

    /**
     * Hücrenin bilgilerini metin olarak döndürür (hata ayıklama amaçlı).
     *
     * @return "Hucre(x,y) tip=... kir=..." biçiminde metin
     */
    @Override
    public String toString() {
        return "Hucre(" + x + "," + y + ") tip=" + tip + " kir=" + kirTipi;
    }
}
