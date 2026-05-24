package com.robotvacuum.model;

/**
 * Robot süpürgenin temizleyebileceği kir türlerini temsil eden enum sınıfı.
 * Her kir türünün farklı bir temizlik süresi (adım sayısı) ve batarya maliyeti vardır.
 *
 * <ul>
 *   <li><b>TOZ:</b> 1 adımda temizlenir, düşük batarya tüketimi</li>
 *   <li><b>SIVI:</b> 3 adımda temizlenir, orta batarya tüketimi</li>
 *   <li><b>LEKE:</b> 5 adımda temizlenir, yüksek batarya tüketimi</li>
 * </ul>
 */
public enum KirTipi {

    /** Toz: En hafif kir türü, 1 adımda temizlenir */
    TOZ("Toz", 1, 1.0),

    /** Sıvı: Orta seviye kir türü, 3 adımda temizlenir */
    SIVI("Sıvı", 3, 2.0),

    /** Leke: En zor kir türü, 5 adımda temizlenir */
    LEKE("Leke", 5, 3.0);

    /** Kullanıcı arayüzünde görüntülenecek Türkçe ad */
    private final String gorunenAd;

    /** Bu kir türünü tamamen temizlemek için gereken adım sayısı */
    private final int temizlikAdimSayisi;

    /** Temizlik sırasında uygulanan batarya maliyet çarpanı */
    private final double bataryaMaliyetCarpani;

    /**
     * Kir türü enum sabiti oluşturucusu.
     *
     * @param gorunenAd            Kullanıcıya gösterilecek Türkçe ad
     * @param temizlikAdimSayisi   Temizlik için gereken adım sayısı
     * @param bataryaMaliyetCarpani Batarya tüketim çarpanı (1.0 = normal)
     */
    KirTipi(String gorunenAd, int temizlikAdimSayisi, double bataryaMaliyetCarpani) {
        this.gorunenAd = gorunenAd;
        this.temizlikAdimSayisi = temizlikAdimSayisi;
        this.bataryaMaliyetCarpani = bataryaMaliyetCarpani;
    }

    /** @return Kullanıcı arayüzünde görüntülenecek Türkçe ad */
    public String getGorunenAd() { return gorunenAd; }

    /** @return Bu kir türünü temizlemek için gereken toplam adım sayısı */
    public int getTemizlikAdimSayisi() { return temizlikAdimSayisi; }

    /** @return Batarya maliyet çarpanı (1.0 = standart tüketim) */
    public double getBataryaMaliyetCarpani() { return bataryaMaliyetCarpani; }
}
