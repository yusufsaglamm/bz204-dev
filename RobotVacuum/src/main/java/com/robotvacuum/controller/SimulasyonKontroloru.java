package com.robotvacuum.controller;

import com.robotvacuum.model.*;
import com.robotvacuum.view.AnaGorunum;
import javafx.animation.AnimationTimer;

/**
 * Arayüz ile Model arasındaki iletişimi sağlayan Kontrolcü (Controller) sınıfı.
 * Kullanıcının arayüzdeki düğmelere tıklamasını dinler, model üzerindeki verileri
 * günceller ve arka planda çalışan simülasyon zamanlayıcısını (AnimationTimer) yönetir.
 */
public class SimulasyonKontroloru {

    private final SimulasyonModeli model;
    private final AnaGorunum gorunum;

    private long sonTickZamaniNanos = 0;
    
    // Robotun temel adım hızı (Nanosaniye cinsinden 250 milisaniye)
    private static final long TEMEL_TICK_NS = 250_000_000L;
    
    // Kir ekleme veya engel ekleme modu durumunu tutar
    private String duzenlemeModu = null;

    public SimulasyonKontroloru(SimulasyonModeli model, AnaGorunum gorunum) {
        this.model = model;
        this.gorunum = gorunum;
    }

    /**
     * Simülasyonu ilk durumuna getirir ve arka plan zamanlayıcısını başlatır.
     */
    public void initialize() {
        salonOlustur(); // İlk açılışta varsayılan olarak salon yüklensin

        AnimationTimer zamanlayici = new AnimationTimer() {
            @Override
            public void handle(long simdi) {
                // Seçilen hız çarpanına göre adım sıklığını belirliyoruz
                long tickAraligiNs = (long) (TEMEL_TICK_NS / model.getHizCarpani());
                if (simdi - sonTickZamaniNanos >= tickAraligiNs) {
                    sonTickZamaniNanos = simdi;
                    model.tick(); // Modeli güncelle
                }
                gorunum.getOdaKanvasi().yenidenCiz(); // Ekrana yeniden çizdir
            }
        };
        zamanlayici.start();
        gorunum.getOdaKanvasi().yenidenCiz();
    }

    public void baslat() {
        duzenlemeModu = null; // Çalışırken kir/engel eklenmesin
        model.baslat();
    }

    public void duraklat() {
        model.duraklat();
    }

    public void sifirla() {
        duzenlemeModu = null;
        String seciliOda = gorunum.getOdaSecimKutusu().getValue();
        odaPlaniDegistir(seciliOda);
    }

    public void istasyonaDon() {
        model.istasyonaDon();
    }

    public void ulasilamayanAlanBul() {
        model.ulasilamayanAlanlariTespitEt();
    }

    /**
     * Kir ekleme moduna geçişi ayarlar. Tıklanmışsa kapatır, tıklanmamışsa açar.
     */
    public void kirEklemeModu() {
        duzenlemeModu = (duzenlemeModu != null && duzenlemeModu.equals("kir")) ? null : "kir";
    }

    /**
     * Engel ekleme moduna geçişi ayarlar.
     */
    public void engelEklemeModu() {
        duzenlemeModu = (duzenlemeModu != null && duzenlemeModu.equals("engel")) ? null : "engel";
    }

    public void kirTipiSec(KirTipi tip) {
        model.setSecilenKirTipi(tip);
    }

    public void algoritmaSec(TemizlikAlgoritmasi algoritma) {
        model.setAlgoritma(algoritma);
    }

    public void hizDegistir(double hiz) {
        model.setHizCarpani(hiz);
    }

    /**
     * Arayüzdeki kaydırıcı ile robotun bataryasını manuel değiştirmek için.
     * Batarya azaldığında otomatik şarja dönme mantığını da tetikler.
     */
    public void elleBataryaAyarla(double deger) {
        model.getRobot().setBatarya(deger);
        model.bataryaOzelligi().set(deger);
        if (deger <= 20.0) {
            model.istasyonaDon();
        }
    }

    /**
     * Oda planı ComboBox'ından seçim değişince yeni odayı ve engellerini yerleştirir.
     */
    /**
     * Oda planı ComboBox'ından seçim değişince yeni odayı ve engellerini yerleştirir.
     * Metin tabanlı (String) kontrol yerine enum nesneleri üzerinden güvenli eşleşme yapar.
     */
    public void odaPlaniDegistir(String planAdi) {
        model.sifirla();

        // Ekranda seçilen String isme karşılık gelen OdaTipi nesnesini buluyoruz
        OdaTipi secilenTip = null;
        for (OdaTipi tip : OdaTipi.values()) {
            if (tip.getEkranAdi().equals(planAdi)) {
                secilenTip = tip;
                break;
            }
        }

        // Bulunan enum nesnesine göre ilgili odayı inşa eden metodu çağırıyoruz
        if (secilenTip != null) {
            switch (secilenTip) {
                case SALON -> salonOlustur();
                case MUTFAK -> mutfakOlustur();
                case YATAK_ODASI -> yatakOdasiOlustur();
            }
        }

        gorunum.getOdaKanvasi().yenidenCiz();
    }

    /**
     * Kullanıcı odadaki (Kanvastaki) bir hücreye tıkladığında, o anki düzenleme moduna göre işlem yapar.
     */
    public void tuvaleTiklandi(int sutun, int satir) {
        Oda oda = model.getOda();
        if (!oda.sinirlarIcindeMi(sutun, satir)) return;

        if ("kir".equals(duzenlemeModu)) {
            oda.kirEkle(sutun, satir, model.getSecilenKirTipi());
            gorunum.getOdaKanvasi().yenidenCiz();
        } else if ("engel".equals(duzenlemeModu)) {
            Hucre hucre = oda.getHucre(sutun, satir);
            if (hucre.engelMi()) {
                oda.engeliKaldir(sutun, satir);
            } else {
                com.robotvacuum.model.MobilyaTipi tip = gorunum.getSecilenMobilyaTipi();
                if (tip != null) {
                    boolean yatay = (tip == com.robotvacuum.model.MobilyaTipi.YATAY_KANEPE);
                    oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(tip, sutun, satir, yatay));
                } else {
                    // Fallback for safe mode
                    oda.engelKoy(sutun, satir);
                }
            }
            gorunum.getOdaKanvasi().yenidenCiz();
        }
    }

    // =========================================================================
    // VARSAYILAN ODA YERLEŞİMLERİ (ENGEL VE KİRLERİN DOSDOĞRU YERLEŞİMİ)
    // =========================================================================

    private void salonOlustur() {
        Oda oda = model.getOda();
        int sx = 0, sy = 12; // Şarj istasyonu sol alt köşede
        oda.setSarjIstasyonu(sx, sy);
        model.getRobot().sifirla(sx, sy);

        // Salon mobilyalarını (büyük bloklar halinde) ekleyelim
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.L_KANEPE, 4, 2, false));
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.TEKLI_KOLTUK, 6, 5, false));
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.YATAY_KANEPE, 14, 0, true));

        // İlk kirleri serpiştirelim
        oda.kirEkle(5, 4, KirTipi.TOZ);
        oda.kirEkle(8, 6, KirTipi.SIVI);
        oda.kirEkle(15, 2, KirTipi.LEKE);
    }

    private void mutfakOlustur() {
        Oda oda = model.getOda();
        int sx = 19, sy = 0; // İstasyon sağ üst köşede
        oda.setSarjIstasyonu(sx, sy);
        model.getRobot().sifirla(sx, sy);

        // Mutfak tezgah ve masalarını yerleştir
        for (int y = 0; y <= 8; y++) oda.engelKoy(0, y);
        for (int x = 0; x <= 6; x++) oda.engelKoy(x, 0);
        for (int x = 4; x <= 7; x++) {
            oda.engelKoy(x, 4);
            oda.engelKoy(x, 5);
        }
        for (int x = 12; x <= 15; x++) {
            oda.engelKoy(x, 7);
            oda.engelKoy(x, 8);
        }

        // Mutfak kirleri (daha çok sıvı dökülür mutfakta)
        oda.kirEkle(2, 2, KirTipi.SIVI);
        oda.kirEkle(5, 6, KirTipi.SIVI);
        oda.kirEkle(13, 9, KirTipi.LEKE);
        oda.kirEkle(10, 5, KirTipi.TOZ);
    }

    private void yatakOdasiOlustur() {
        Oda oda = model.getOda();
        int sx = 19, sy = 13; // İstasyon sağ alt köşede
        oda.setSarjIstasyonu(sx, sy);
        model.getRobot().sifirla(sx, sy);

        // Yatak Odası dolap ve yatak yerleşimi
        for (int x = 8; x <= 11; x++) {
            for (int y = 0; y <= 4; y++) {
                oda.engelKoy(x, y);
            }
        }
        oda.engelKoy(7, 0);
        oda.engelKoy(12, 0);
        for (int y = 2; y <= 9; y++) oda.engelKoy(0, y);
        for (int x = 15; x <= 18; x++) oda.engelKoy(x, 5);

        // Yatak odasındaki kirler
        oda.kirEkle(9, 6, KirTipi.TOZ);
        oda.kirEkle(3, 5, KirTipi.TOZ);
        oda.kirEkle(16, 6, KirTipi.LEKE);
    }
}
