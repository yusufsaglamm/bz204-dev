package com.robotvacuum.model;

/**
 * Robot süpürge için kullanılabilir temizlik algoritmalarını tanımlayan enum sınıfı.
 *
 * <ul>
 *   <li><b>RASTGELE:</b> Robot rastgele yönlerde hareket eder</li>
 *   <li><b>SPIRAL:</b> Robot spiral (sarmal) şeklinde dışa doğru genişleyerek ilerler</li>
 *   <li><b>DUVAR_TAKIP:</b> Robot sağ el kuralını kullanarak duvar boyunca ilerler</li>
 * </ul>
 */
public enum TemizlikAlgoritmasi {

    /** Rastgele hareket algoritması: Robot rastgele yönlere doğru hareket eder */
    RASTGELE("Rastgele"),

    /** Spiral algoritması: Robot sarmal şeklinde dışa doğru genişleyerek ilerler */
    SPIRAL("Spiral"),

    /** Duvar takip algoritması: Sağ el kuralı ile duvar boyunca hareket eder */
    DUVAR_TAKIP("Duvar Takip");

    /** Kullanıcı arayüzünde görüntülenecek Türkçe ad */
    private final String gorunenAd;

    /**
     * Temizlik algoritması enum sabiti oluşturucusu.
     *
     * @param gorunenAd Kullanıcıya gösterilecek Türkçe algoritma adı
     */
    TemizlikAlgoritmasi(String gorunenAd) {
        this.gorunenAd = gorunenAd;
    }

    /** @return Kullanıcı arayüzünde görüntülenecek Türkçe ad */
    public String getGorunenAd() { return gorunenAd; }
}
