package com.robotvacuum.controller;

import com.robotvacuum.model.*;
import com.robotvacuum.view.AnaGorunum;
import javafx.animation.AnimationTimer;

/**
 * Simülasyon kontrolcü sınıfı (MVC mimarisinde "Controller" katmanı).
 *
 * <p>Bu sınıf:
 * <ul>
 *   <li>Kullanıcı girdilerini (buton, tıklama, kaydırma) yakalar ve modele iletir</li>
 *   <li>JavaFX {@link AnimationTimer} ile animasyon döngüsünü yönetir</li>
 *   <li>Model ve View arasında köprü görevi görür</li>
 *   <li>Varsayılan mobilya yerleşimi ve başlangıç kirlerini yerleştirir</li>
 * </ul>
 */
public class SimulasyonKontrolcusu {

    /** Simülasyon modeli (durum ve mantık) */
    private final SimulasyonModeli model;
    /** Ana görünüm (UI bileşenleri) */
    private final AnaGorunum gorunum;

    /** Her animasyon karesinde modeli güncelleyen zamanlayıcı */
    private AnimationTimer animasyonZamanlayici;
    /** Son tick zamanı (nanosaniye cinsinden) */
    private long sonTikZamani = 0;

    /** Temel tick aralığı (nanosaniye). 1 saniye / 4 = 250ms (1x hızda her adım) */
    private static final long TEMEL_TIK_NS = 250_000_000L;

    /** Mevcut düzenleme modu: "kir", "engel" veya null */
    private String duzenlemeModu = null;

    /**
     * Yeni bir kontrolcü oluşturur.
     *
     * @param model   Simülasyon modeli
     * @param gorunum Ana görünüm
     */
    public SimulasyonKontrolcusu(SimulasyonModeli model, AnaGorunum gorunum) {
        this.model = model;
        this.gorunum = gorunum;
    }

    /**
     * Kontrolcüyü başlatır: varsayılan mobilyaları ve kirleri yerleştirir,
     * animasyon zamanlayıcısını kurar ve başlatır.
     *
     * <p>Not: Bu metodun adı (initialize) JavaFX yaşam döngüsü sözleşmesi gereği
     * korunmuştur.</p>
     */
    public void initialize() {
        // Varsayılan mobilya yerleşimini ekle
        varsayilanMobilyalariYerlestir();

        // Demonstrasyon için bir miktar başlangıç kiri ekle
        varsayilanKirleriYerlestir();

        // Animasyon zamanlayıcısını oluştur ve başlat
        animasyonZamanlayici = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Hız çarpanına göre tick aralığını hesapla
                long tikAraligiNs = (long) (TEMEL_TIK_NS / model.getHizCarpani());
                if (now - sonTikZamani >= tikAraligiNs) {
                    sonTikZamani = now;
                    model.tik();
                    gorunum.getOdaTuvali().yenidenCiz();
                }
            }
        };
        animasyonZamanlayici.start();
        gorunum.getOdaTuvali().yenidenCiz();
    }

    // ==================== BUTON EYLEMLERİ ====================

    /** "Başlat" butonu olay yöneticisi */
    public void baslatTiklandi() {
        duzenlemeModu = null;
        model.basla();
    }

    /** "Duraklat" butonu olay yöneticisi */
    public void duraklatTiklandi() {
        model.duraklat();
    }

    /** "Sıfırla" butonu olay yöneticisi - modeli sıfırlar ve varsayılan düzeni geri yükler */
    public void sifirlaTiklandi() {
        duzenlemeModu = null;
        model.sifirla();
        varsayilanMobilyalariYerlestir();
        varsayilanKirleriYerlestir();
        gorunum.getOdaTuvali().yenidenCiz();
    }

    /** "İstasyona Dön" butonu olay yöneticisi */
    public void istasyonaDonTiklandi() {
        model.istasyonaDon();
    }

    /** "Kir Ekle" modu butonu - tıklandığında kir ekleme modunu aç/kapat */
    public void kirEkleModuTiklandi() {
        duzenlemeModu = duzenlemeModu != null && duzenlemeModu.equals("kir") ? null : "kir";
        imleciGuncelle();
    }

    /** "Mobilya Ekle" modu butonu - tıklandığında engel ekleme modunu aç/kapat */
    public void engelEkleModuTiklandi() {
        duzenlemeModu = duzenlemeModu != null && duzenlemeModu.equals("engel") ? null : "engel";
        imleciGuncelle();
    }

    /**
     * Radyo butonundan kir türü seçildiğinde çağrılır.
     *
     * @param tip Seçilen kir türü
     */
    public void kirTipiSecildi(KirTipi tip) {
        model.setSecilenKirTipi(tip);
    }

    /**
     * Radyo butonundan algoritma seçildiğinde çağrılır.
     *
     * @param algoritma Seçilen temizlik algoritması
     */
    public void algoritmaSecildi(TemizlikAlgoritmasi algoritma) {
        model.setAlgoritma(algoritma);
    }

    /**
     * Hız kaydırıcısı değiştiğinde çağrılır.
     *
     * @param hiz Yeni hız çarpanı
     */
    public void hizDegisti(double hiz) {
        model.setHizCarpani(hiz);
    }

    /**
     * Kullanıcı bataryayı kaydırıcı ile manuel ayarladığında çağrılır.
     *
     * @param deger Yeni batarya seviyesi (0-100)
     */
    public void manuelBataryaAyarla(double deger) {
        model.getRobot().setBatarya(deger);
        model.bataryaOzelligi().set(deger);
    }

    // ==================== TUVAL TIKLAMA ====================

    /**
     * Tuval (canvas) üzerine tıklandığında çağrılır.
     * Düzenleme moduna göre kir ekler veya engel yerleştirir/kaldırır.
     *
     * @param sutun Tıklanan hücrenin sütun indeksi
     * @param satir Tıklanan hücrenin satır indeksi
     */
    public void tuvaleTiklandi(int sutun, int satir) {
        Oda oda = model.getOda();
        if (!oda.sinirlarIcindeMi(sutun, satir)) return;

        if ("kir".equals(duzenlemeModu)) {
            // Kir ekleme modu
            oda.kirEkle(sutun, satir, model.getSecilenKirTipi());
            gorunum.getOdaTuvali().yenidenCiz();
        } else if ("engel".equals(duzenlemeModu)) {
            // Engel ekleme/kaldırma modu
            Hucre hucre = oda.getHucre(sutun, satir);
            if (hucre.engelMi()) {
                oda.engelKaldir(sutun, satir);
            } else {
                // Robotun mevcut konumuna engel konulamaz
                if (sutun == model.getRobot().getX() && satir == model.getRobot().getY()) return;
                oda.engelKoy(sutun, satir);
            }
            gorunum.getOdaTuvali().yenidenCiz();
        }
    }

    // ==================== YARDIMCI METODLAR ====================

    /**
     * Düzenleme moduna göre fare imlecini günceller (görsel geri bildirim için).
     * Şu an için boş bırakılmıştır; ileride genişletilebilir.
     */
    private void imleciGuncelle() {
        // Mod için görsel geri bildirim ileride buraya eklenebilir
    }

    /**
     * Varsayılan ev mobilyalarını odaya yerleştirir.
     * Kanepe, sehpa, raflar ve küçük masalar oluşturulur.
     */
    private void varsayilanMobilyalariYerlestir() {
        Oda oda = model.getOda();
        // Kanepe bölgesi (üst-orta)
        for (int x = 5; x <= 9; x++) oda.engelKoy(x, 1);
        for (int x = 5; x <= 9; x++) oda.engelKoy(x, 2);

        // Sehpa
        for (int x = 6; x <= 8; x++) {
            oda.engelKoy(x, 5);
            oda.engelKoy(x, 6);
        }

        // Sağ taraf rafı
        for (int y = 0; y <= 3; y++) oda.engelKoy(17, y);
        oda.engelKoy(18, 0);
        oda.engelKoy(18, 1);

        // Sağ üstte küçük masa
        oda.engelKoy(15, 5);
        oda.engelKoy(15, 6);
        oda.engelKoy(16, 5);
        oda.engelKoy(16, 6);

        // Sol alt köşede masa
        oda.engelKoy(2, 11);
        oda.engelKoy(2, 12);
        oda.engelKoy(3, 11);
    }

    /**
     * Başlangıç kirlerini odaya yerleştirir (demo amaçlı).
     * Tozlar, sıvı sızıntıları ve birkaç leke eklenir.
     */
    private void varsayilanKirleriYerlestir() {
        Oda oda = model.getOda();
        // Birkaç noktaya toz serpiştir
        int[][] tozNoktalari = {
            {4,3}, {11,3}, {13,4}, {3,7}, {10,10},
            {14,10}, {4,10}, {19,3}, {12,12}, {1,5}
        };
        for (int[] nokta : tozNoktalari) oda.kirEkle(nokta[0], nokta[1], KirTipi.TOZ);

        // Sıvı sızıntıları
        int[][] siviNoktalari = {{7,8}, {14,7}, {3,4}};
        for (int[] nokta : siviNoktalari) oda.kirEkle(nokta[0], nokta[1], KirTipi.SIVI);

        // Birkaç leke
        int[][] lekeNoktalari = {{11,11}, {17,4}};
        for (int[] nokta : lekeNoktalari) oda.kirEkle(nokta[0], nokta[1], KirTipi.LEKE);
    }
}
