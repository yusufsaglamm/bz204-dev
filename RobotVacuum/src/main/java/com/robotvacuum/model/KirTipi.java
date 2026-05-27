package com.robotvacuum.model;

/**
 * Robotun temizleyebileceği kir tiplerini barındırır.
 * Her kir tipinin temizlik süresi (adım sayısı) ve bataryadan yiyeceği miktar farklıdır.
 */
public enum KirTipi {
    TOZ("Toz", 1, 1.0),
    SIVI("Sıvı", 3, 2.0),
    LEKE("Leke", 5, 3.0);

    private final String ekranAdi;
    
    // Bu kiri tamamen temizlemek için robotun üstünde kaç kez durması gerektiği
    private final int temizlikAdimi;
    
    // Temizlerken bataryanın ne kadar daha hızlı tükeneceğini belirleyen katsayı
    private final double bataryaMaliyetCarpani;

    KirTipi(String ekranAdi, int temizlikAdimi, double bataryaMaliyetCarpani) {
        this.ekranAdi = ekranAdi;
        this.temizlikAdimi = temizlikAdimi;
        this.bataryaMaliyetCarpani = bataryaMaliyetCarpani;
    }

    public String getEkranAdi() { return ekranAdi; }
    public int getTemizlikAdimi() { return temizlikAdimi; }
    public double getBataryaMaliyetCarpani() { return bataryaMaliyetCarpani; }
}
