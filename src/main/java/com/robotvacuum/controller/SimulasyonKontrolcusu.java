package com.robotvacuum.controller;

import com.robotvacuum.model.*;
import com.robotvacuum.view.AnaGorunum;
import javafx.animation.AnimationTimer;

/**
 * MVC'deki "Controller" — UI'dan gelen olayları alıp modele iletir,
 * model değişince ekranı tazeler.
 *
 * Not (OOP): View ve Model birbirinden habersiz. Aralarındaki haberleşmeyi
 * tek elden bu sınıf yönetiyor. Böylece View'i ya da Model'i değiştirirken
 * diğeri etkilenmiyor.
 */
public class SimulasyonKontrolcusu {

    private final SimulasyonModeli model;
    private final AnaGorunum gorunum;

    private AnimationTimer animasyonZamanlayici;
    private long sonTikZamani = 0;

    // 1x hızda her tick 250 ms (saniyede 4 adım)
    private static final long TEMEL_TIK_NS = 250_000_000L;

    // Aktif düzenleme modu: "kir", "engel" veya null
    private String duzenlemeModu = null;

    public SimulasyonKontrolcusu(SimulasyonModeli model, AnaGorunum gorunum) {
        this.model = model;
        this.gorunum = gorunum;
    }

    /**
     * Uygulama açılırken çağrılır:
     * varsayılan mobilya/kir yerleşimi + animasyon zamanlayıcısı.
     * (initialize ismi JavaFX yaşam döngüsünden gelir, dokunmadık.)
     */
    public void initialize() {
        varsayilanMobilyalariYerlestir();
        varsayilanKirleriYerlestir();

        animasyonZamanlayici = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Hız çarpanı arttıkça tick aralığını kısaltıyoruz
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

    // ==================== BUTON OLAYLARI ====================

    public void baslatTiklandi() {
        duzenlemeModu = null;
        model.basla();
    }

    public void duraklatTiklandi() {
        model.duraklat();
    }

    /** Modeli sıfırla, sonra varsayılan düzeni yeniden kur. */
    public void sifirlaTiklandi() {
        duzenlemeModu = null;
        model.sifirla();
        varsayilanMobilyalariYerlestir();
        varsayilanKirleriYerlestir();
        gorunum.getOdaTuvali().yenidenCiz();
    }

    public void istasyonaDonTiklandi() {
        model.istasyonaDon();
    }

    /** Aynı butona tekrar basınca modu kapatıyoruz (toggle). */
    public void kirEkleModuTiklandi() {
        duzenlemeModu = "kir".equals(duzenlemeModu) ? null : "kir";
    }

    public void engelEkleModuTiklandi() {
        duzenlemeModu = "engel".equals(duzenlemeModu) ? null : "engel";
    }

    public void kirTuruSecildi(KirTuru tur) {
        model.setSecilenKirTuru(tur);
    }

    public void algoritmaSecildi(TemizlikAlgoritmasi algoritma) {
        model.setAlgoritma(algoritma);
    }

    public void hizDegisti(double hiz) {
        model.setHizCarpani(hiz);
    }

    /** Kullanıcı bataryayı kaydırıcı ile elle değiştirebilsin diye. */
    public void manuelBataryaAyarla(double deger) {
        model.getRobot().setBatarya(deger);
        model.bataryaOzelligi().set(deger);
    }

    // ==================== TUVALE TIKLAMA ====================

    /**
     * Kullanıcı tuvale tıkladığında çağrılır.
     * Moda göre kir ekler veya engel koyar/kaldırır.
     */
    public void tuvaleTiklandi(int sutun, int satir) {
        Oda oda = model.getOda();
        if (!oda.sinirlarIcindeMi(sutun, satir)) return;

        if ("kir".equals(duzenlemeModu)) {
            oda.kirEkle(sutun, satir, model.getSecilenKirTuru());
            gorunum.getOdaTuvali().yenidenCiz();
        } else if ("engel".equals(duzenlemeModu)) {
            Hucre hucre = oda.getHucre(sutun, satir);
            if (hucre.engelMi()) {
                oda.engelKaldir(sutun, satir);
            } else {
                // Robot şu an o hücredeyse engel koymayalım, üzerinde sıkışmasın
                if (sutun == model.getRobot().getX() && satir == model.getRobot().getY()) return;
                oda.engelKoy(sutun, satir);
            }
            gorunum.getOdaTuvali().yenidenCiz();
        }
    }

    // ==================== VARSAYILAN YERLEŞİM ====================

    /** Demo amacıyla odaya ev mobilyaları (kanepe, sehpa, raflar, masalar) koyar. */
    private void varsayilanMobilyalariYerlestir() {
        Oda oda = model.getOda();

        // Kanepe — üst-orta
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

        // Sol alt masa
        oda.engelKoy(2, 11);
        oda.engelKoy(2, 12);
        oda.engelKoy(3, 11);
    }

    /** Demo için başlangıç kirleri: toz, sıvı, leke (her birine örnek). */
    private void varsayilanKirleriYerlestir() {
        Oda oda = model.getOda();

        int[][] tozNoktalari = {
            {4, 3}, {11, 3}, {13, 4}, {3, 7}, {10, 10},
            {14, 10}, {4, 10}, {19, 3}, {12, 12}, {1, 5}
        };
        for (int[] nokta : tozNoktalari) oda.kirEkle(nokta[0], nokta[1], KirTuru.TOZ);

        int[][] siviNoktalari = {{7, 8}, {14, 7}, {3, 4}};
        for (int[] nokta : siviNoktalari) oda.kirEkle(nokta[0], nokta[1], KirTuru.SIVI);

        int[][] lekeNoktalari = {{11, 11}, {17, 4}};
        for (int[] nokta : lekeNoktalari) oda.kirEkle(nokta[0], nokta[1], KirTuru.LEKE);
    }
}
