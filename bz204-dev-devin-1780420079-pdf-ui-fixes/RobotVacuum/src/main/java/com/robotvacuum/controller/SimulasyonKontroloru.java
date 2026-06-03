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
        duzenlemeModu = (duzenlemeModu != null && duzenlemeModu.equals("engel_ekle")) ? null : "engel_ekle";
    }

    /**
     * Engel silme moduna geçişi ayarlar.
     */
    public void engelSilmeModu() {
        duzenlemeModu = (duzenlemeModu != null && duzenlemeModu.equals("engel_sil")) ? null : "engel_sil";
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
        } else if ("engel_ekle".equals(duzenlemeModu)) {
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
            gorunum.getOdaKanvasi().yenidenCiz();
        } else if ("engel_sil".equals(duzenlemeModu)) {
            if (hucre.engelMi()) {
                oda.engeliKaldir(sutun, satir);
            }
            gorunum.getOdaKanvasi().yenidenCiz();
        }
    }

    /**
     * Farenin kanvas üzerindeki anlık pozisyonunu günceller ve silüet çizimi için arayüzü tetikler.
     */
    public void yeniFarePozisyonu(int sutun, int satir) {
        gorunum.getOdaKanvasi().setSiluetVerisi(duzenlemeModu, gorunum.getSecilenMobilyaTipi(), gorunum.getSecilenMobilyaYonu());
        gorunum.getOdaKanvasi().setHoverPozisyonu(sutun, satir);
        gorunum.getOdaKanvasi().yenidenCiz();
    }

    // =========================================================================
    // VARSAYILAN ODA YERLEŞİMLERİ (ENGEL VE KİRLERİN DOSDOĞRU YERLEŞİMİ)
    // =========================================================================

    private void salonOlustur() {
        Oda oda = model.getOda();
        model.setAktifOdaTipi(OdaTipi.SALON);
        int sx = 0, sy = 12; // Şarj istasyonu sol alt köşede
        oda.setSarjIstasyonu(sx, sy);
        model.getRobot().sifirla(sx, sy);

        // ── Salon: U-şeklinde oturma grubu ──
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KANEPE, 7, 0, Yon.KUZEY));
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KANEPE, 0, 4, Yon.BATI));
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KANEPE, 17, 4, Yon.DOGU));

        // Dekoratif mobilyalar
        // TV ünitesi (alt duvara yakın, ortada, güney duvara yaslı olduğu için KUZEY'e bakar)
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.TV_UNITESI, 7, 13, Yon.KUZEY));
        // Sehpa (oturma grubunun ortasında, KUZEY'e dönük)
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.SEHPA, 8, 5, Yon.KUZEY));

        // Kirleri oturma grubunun ortasına ve etrafına serpiştirelim
        oda.kirEkle(4, 8, KirTipi.TOZ);
        oda.kirEkle(10, 4, KirTipi.SIVI);
        oda.kirEkle(14, 8, KirTipi.LEKE);
    }

    private void mutfakOlustur() {
        Oda oda = model.getOda();
        model.setAktifOdaTipi(OdaTipi.MUTFAK);
        int sx = 19, sy = 0; // İstasyon sağ üst köşede
        oda.setSarjIstasyonu(sx, sy);
        model.getRobot().sifirla(sx, sy);

        // ── Mutfak: Tezgah ve yemek köşesi ──
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KANEPE, 1, 0, Yon.KUZEY));
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KANEPE, 0, 8, Yon.BATI));
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KANEPE, 14, 12, Yon.GUNEY));

        // Dekoratif mobilyalar
        // Tezgah (üst duvar boyunca sağ tarafta, GÜNEY'e bakar)
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.TEZGAH, 7, 0, Yon.GUNEY));
        // Yemek masası
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.YEMEK_MASASI, 9, 6, Yon.KUZEY));

        // Mutfak kirleri
        oda.kirEkle(6, 3, KirTipi.SIVI);
        oda.kirEkle(14, 8, KirTipi.SIVI);
        oda.kirEkle(15, 10, KirTipi.LEKE);
        oda.kirEkle(5, 5, KirTipi.TOZ);
    }

    private void yatakOdasiOlustur() {
        Oda oda = model.getOda();
        model.setAktifOdaTipi(OdaTipi.YATAK_ODASI);
        int sx = 19, sy = 13; // İstasyon sağ alt köşede
        oda.setSarjIstasyonu(sx, sy);
        model.getRobot().sifirla(sx, sy);

        // ── Yatak Odası: Yatak ve dolap ──
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.YATAK, 7, 0, Yon.KUZEY));

        // Dekoratif mobilyalar
        // Komodin (yatağın yanlarında)
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KOMODIN, 5, 0, Yon.KUZEY));
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.KOMODIN, 12, 0, Yon.KUZEY));
        // Dolap (sağ duvarda, odaya yani BATI'ya baksın)
        oda.mobilyaEkle(new com.robotvacuum.model.Mobilya(com.robotvacuum.model.MobilyaTipi.DOLAP, 18, 0, Yon.BATI));

        // Yatak odasındaki kirler
        oda.kirEkle(5, 12, KirTipi.TOZ);
        oda.kirEkle(14, 10, KirTipi.TOZ);
        oda.kirEkle(2, 10, KirTipi.LEKE);
    }
}
