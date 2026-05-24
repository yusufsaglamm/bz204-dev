package com.robotvacuum.model;

/**
 * Hücredeki kirin türünü tutan enum.
 * Her türün temizlik zorluğu ve batarya tüketimi farklı.
 *
 * Not (OOP): Sabit veri (adım sayısı, maliyet çarpanı) zaten her tür için
 * bellidir. Bu yüzden enum içine alanlar ekleyip tek yerde topladık.
 */
public enum KirTuru {

    // Toz: en kolay kir, tek geçişte siliniyor.
    TOZ ("Toz",  1, 1.0),

    // Sıvı: ıslak olduğu için 2 geçişe ve daha çok bataryaya ihtiyaç duyar.
    SIVI("Sıvı", 2, 1.5),

    // Leke: en zorlu kir, 3 geçiş + en yüksek batarya maliyeti.
    LEKE("Leke", 3, 2.0);

    private final String gorunenAd;
    private final int temizlikAdimSayisi;
    private final double bataryaMaliyetCarpani;

    KirTuru(String gorunenAd, int adim, double maliyet) {
        this.gorunenAd = gorunenAd;
        this.temizlikAdimSayisi = adim;
        this.bataryaMaliyetCarpani = maliyet;
    }

    public String getGorunenAd() { return gorunenAd; }

    /** Bu kiri tamamen temizlemek için kaç tick gerekir. */
    public int getTemizlikAdimSayisi() { return temizlikAdimSayisi; }

    /** Bir hareketin normal batarya maliyetinin kaç katı harcanır. */
    public double getBataryaMaliyetCarpani() { return bataryaMaliyetCarpani; }
}
