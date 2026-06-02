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
        // Simülasyon çalışmıyorken/duraklatılmışken model işlemi sessizce reddeder.
        // Kullanıcıya neden çalışmadığını net bir durum mesajıyla bildiriyoruz.
        if (!model.calisiyorMu() || model.duraklatildiMi()) {
            model.durumOzelligi().set("Önce simülasyonu başlatın");
            return;
        }
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

    /**
     * Arayüzün (View) aktif düzenleme modunu öğrenip buton/rozet geri bildirimi
     * yapabilmesi için modu döndürür ("kir", "engel" veya null).
     */
    public String getDuzenlemeModu() {
        return duzenlemeModu;
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

        Hucre hucre = oda.getHucre(sutun, satir);

        if ("kir".equals(duzenlemeModu)) {
            // Engel veya istasyon üzerine kir eklenemez; kullanıcıya kısa bilgi verelim
            if (hucre != null && (hucre.engelMi() || hucre.sarjIstasyonuMu())) {
                model.durumOzelligi().set("Buraya kir eklenemez");
                return;
            }
            oda.kirEkle(sutun, satir, model.getSecilenKirTipi());
            gorunum.getOdaKanvasi().yenidenCiz();
        } else if ("engel".equals(duzenlemeModu)) {
            if (hucre.engelMi()) {
                oda.engeliKaldir(sutun, satir);
            } else {
                // Robotun bulunduğu hücreye mobilya koymaya izin vermiyoruz
                if (model.getRobot().getX() == sutun && model.getRobot().getY() == satir) {
                    model.durumOzelligi().set("Robotun üzerine mobilya konamaz");
                    return;
                }
                com.robotvacuum.model.MobilyaTipi tip = gorunum.getSecilenMobilyaTipi();
                Yon yon = gorunum.getSecilenMobilyaYonu();
                if (tip != null && yon != null) {
                    boolean eklendi = oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(tip, sutun, satir, yon));
                    if (!eklendi) {
                        model.durumOzelligi().set("Mobilya buraya sığmıyor");
                    }
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
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KANEPE, 4, 2, Yon.KUZEY));
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KANEPE, 10, 5, Yon.DOGU));
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KANEPE, 14, 0, Yon.GUNEY));

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

        // Mutfak tezgahı (sol üst köşe - kuzeye bakan)
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KANEPE, 0, 0, Yon.KUZEY));
        // Yemek masası / koltuk (ortada - batıya bakan)
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KANEPE, 8, 5, Yon.BATI));
        // Tezgah karşısı kanepe (sağ alt - güneye bakan)
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KANEPE, 14, 8, Yon.GUNEY));

        // Mutfak kirleri (daha çok sıvı dökülür mutfakta)
        oda.kirEkle(2, 4, KirTipi.SIVI);
        oda.kirEkle(5, 7, KirTipi.SIVI);
        oda.kirEkle(13, 10, KirTipi.LEKE);
        oda.kirEkle(10, 3, KirTipi.TOZ);
    }

    private void yatakOdasiOlustur() {
        Oda oda = model.getOda();
        int sx = 19, sy = 13; // İstasyon sağ alt köşede
        oda.setSarjIstasyonu(sx, sy);
        model.getRobot().sifirla(sx, sy);

        // Yatak (odanın üst ortasında - güneye bakan)
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KANEPE, 8, 0, Yon.GUNEY));
        // Dolap/gardırop yerine kanepe (sol tarafta - doğuya bakan)
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KANEPE, 0, 4, Yon.DOGU));
        // Şezlong / koltuk (sağ alt - batıya bakan)
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KANEPE, 15, 8, Yon.BATI));

        // Yatak odasındaki kirler
        oda.kirEkle(5, 3, KirTipi.TOZ);
        oda.kirEkle(12, 6, KirTipi.TOZ);
        oda.kirEkle(16, 11, KirTipi.LEKE);
    }
}
