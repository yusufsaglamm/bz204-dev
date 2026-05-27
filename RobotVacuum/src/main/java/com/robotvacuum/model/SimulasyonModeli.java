package com.robotvacuum.model;

import com.robotvacuum.util.BFSYolBulucu;
import com.robotvacuum.util.SesYoneticisi;
import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Simülasyonumuzun tüm çalışma mantığını ve beynini oluşturan Model sınıfı.
 */
public class SimulasyonModeli {

    private final Oda oda;
    private final Robot robot;

    private final DoubleProperty bataryaOzelligi = new SimpleDoubleProperty(100.0);
    private final StringProperty konumOzelligi = new SimpleStringProperty("(0, 0)");
    private final StringProperty yonOzelligi = new SimpleStringProperty("Doğu (→)");
    private final IntegerProperty temizlenenAlanOzelligi = new SimpleIntegerProperty(0);
    private final IntegerProperty toplamAlanOzelligi = new SimpleIntegerProperty(1);
    private final IntegerProperty kirliAlanOzelligi = new SimpleIntegerProperty(0);
    private final StringProperty gecenSureOzelligi = new SimpleStringProperty("00:00");
    private final StringProperty durumOzelligi = new SimpleStringProperty("Hazır");
    private final DoubleProperty toplananTozOzelligi = new SimpleDoubleProperty(0);
    private final IntegerProperty ulasilamayanAlanOzelligi = new SimpleIntegerProperty(0);

    private boolean calisiyor = false;
    private boolean duraklatildi = false;
    private long simulasyonBaslangicZamani = 0;
    private long duraklatmaGecenSureMs = 0;
    private long duraklatmaBaslangicZamani = 0;
    private long sonTemizlemeZamani = 0;

    private TemizlikAlgoritmasi algoritma = TemizlikAlgoritmasi.SPIRAL;
    private KirTipi secilenKirTipi = KirTipi.TOZ;
    private double hizCarpani = 1.0;

    private final Random rastgele = new Random();

    // Algoritma Durumları
    private int spiralAdimi = 0;
    private int spiralDogruAdimSayisi = 1;
    private int spiralKenarSayisi = 0;
    private Yon spiralYonu = Yon.DOGU;
    private boolean duvaraYaslaniyorMu = false;

    private final Queue<int[]> planlananYol = new LinkedList<>();

    private boolean temizlikDevamEdiyor = false;
    private double bekleyenBataryaMaliyeti = 0;

    private double toplamToplananToz = 0;
    private int toplamHareket = 0;

    public SimulasyonModeli() {
        oda = new Oda();
        robot = new Robot(oda.getSarjIstasyonuX(), oda.getSarjIstasyonuY());
        ozellikleriGuncelle();
    }

    public void baslat() {
        if (!calisiyor) {
            calisiyor = true;
            duraklatildi = false;
            simulasyonBaslangicZamani = System.currentTimeMillis();
            durumOzelligi.set("Çalışıyor");
        } else if (duraklatildi) {
            duraklatildi = false;
            duraklatmaGecenSureMs += System.currentTimeMillis() - duraklatmaBaslangicZamani;
            durumOzelligi.set("Çalışıyor");
        }
    }

    public void duraklat() {
        if (calisiyor && !duraklatildi) {
            duraklatildi = true;
            duraklatmaBaslangicZamani = System.currentTimeMillis();
            durumOzelligi.set("Duraklatıldı");
        }
    }

    public void sifirla() {
        calisiyor = false;
        duraklatildi = false;
        simulasyonBaslangicZamani = 0;
        duraklatmaGecenSureMs = 0;
        temizlikDevamEdiyor = false;
        toplamToplananToz = 0;
        toplamHareket = 0;

        spiralAdimi = 0;
        spiralDogruAdimSayisi = 1;
        spiralKenarSayisi = 0;
        spiralYonu = Yon.DOGU;
        duvaraYaslaniyorMu = false;

        planlananYol.clear();

        oda.temizligiSifirla();
        robot.sifirla(oda.getSarjIstasyonuX(), oda.getSarjIstasyonuY());
        ulasilamayanAlanOzelligi.set(0);
        durumOzelligi.set("Sıfırlandı");
        ozellikleriGuncelle();
    }

    public void istasyonaDon() {
        if (!calisiyor || duraklatildi) return;
        istasyonaYolPlanla();
        robot.setIstasyonaDonuyor(true);
        durumOzelligi.set("İstasyona Dönüyor");
    }

    public void ulasilamayanAlanlariTespitEt() {
        int sutunlar = oda.getSutunSayisi();
        int satirlar = oda.getSatirSayisi();
        boolean[][] ziyaretEdildi = new boolean[sutunlar][satirlar];
        Queue<int[]> kuyruk = new LinkedList<>();

        int baslangicX = oda.getSarjIstasyonuX();
        int baslangicY = oda.getSarjIstasyonuY();
        kuyruk.add(new int[]{baslangicX, baslangicY});
        ziyaretEdildi[baslangicX][baslangicY] = true;

        while (!kuyruk.isEmpty()) {
            int[] mevcut = kuyruk.poll();
            for (Yon yon : Yon.values()) {
                int nx = mevcut[0] + yon.getDx();
                int ny = mevcut[1] + yon.getDy();
                if (oda.gecilebilirMi(nx, ny) && !ziyaretEdildi[nx][ny]) {
                    ziyaretEdildi[nx][ny] = true;
                    kuyruk.add(new int[]{nx, ny});
                }
            }
        }

        int ulasilamayanSayac = 0;
        for (int x = 0; x < sutunlar; x++) {
            for (int y = 0; y < satirlar; y++) {
                if (oda.gecilebilirMi(x, y) && !ziyaretEdildi[x][y]) {
                    ulasilamayanSayac++;
                }
            }
        }
        ulasilamayanAlanOzelligi.set(ulasilamayanSayac);
        durumOzelligi.set("Ulaşılamayan: " + ulasilamayanSayac + " m²");
    }

    public void tick() {
        if (!calisiyor || duraklatildi) return;

        gecenSureyiGuncelle();

        if (robot.bataryaBitmisMi()) {
            robot.setSarjOluyor(false);
            durumOzelligi.set("Batarya Bitti!");
            calisiyor = false;
            return;
        }

        if (robot.sarjOluyorMu()) {
            sarjIsleminiYurut();
            return;
        }

        if (temizlikDevamEdiyor) {
            Hucre hucre = oda.getHucre(robot.getX(), robot.getY());
            if (hucre != null) {
                aktifTemizligiYurut(hucre);
            }
            return;
        }

        if (robot.bataryaDusukMu() && !robot.istasyonaDonuyorMu()) {
            istasyonaDon();
        }

        if (robot.istasyonaDonuyorMu()) {
            yolBoyuncaIlerle();
        } else {
            algoritmayaGoreIlerle();
        }

        Hucre mevcutHucre = oda.getHucre(robot.getX(), robot.getY());
        yeniHucreTemizlikKontrolu(mevcutHucre);

        ozellikleriGuncelle();
    }

    private void sarjIsleminiYurut() {
        robot.bataryayiSarjEt(1.5);
        bataryaOzelligi.set(robot.getBatarya());
        if (robot.getBatarya() >= Robot.MAKS_BATARYA) {
            robot.setSarjOluyor(false);
            robot.setIstasyonaDonuyor(false);
            planlananYol.clear();
            durumOzelligi.set("Şarj Tamamlandı - Çalışıyor");
        }
    }

    private void aktifTemizligiYurut(Hucre hucre) {
        hucre.temizle();
        SesYoneticisi.playCleanSound();
        sonTemizlemeZamani = System.currentTimeMillis();
        robot.bataryayiTuket(bekleyenBataryaMaliyeti);
        bataryaOzelligi.set(robot.getBatarya());

        if (!hucre.kirliMi()) {
            temizlikDevamEdiyor = false;
            toplamToplananToz++;
            toplananTozOzelligi.set(toplamToplananToz);
            istatistikleriGuncelle();
        }

        if (robot.bataryaDusukMu() && !robot.istasyonaDonuyorMu()) {
            temizlikDevamEdiyor = false;
            istasyonaDon();
        }
    }

    private void yeniHucreTemizlikKontrolu(Hucre mevcutHucre) {
        if (mevcutHucre == null) return;

        if (!robot.istasyonaDonuyorMu() && mevcutHucre.kirliMi()) {
            KirTipi kir = mevcutHucre.getKirTipi();
            double adimMaliyeti = Robot.HAREKET_BATARYA_MALIYETI * kir.getBataryaMaliyetCarpani();

            mevcutHucre.temizle();
            SesYoneticisi.playCleanSound();
            sonTemizlemeZamani = System.currentTimeMillis();
            robot.bataryayiTuket(adimMaliyeti);

            if (mevcutHucre.kirliMi()) {
                temizlikDevamEdiyor = true;
                bekleyenBataryaMaliyeti = adimMaliyeti;
            } else {
                toplamToplananToz++;
                toplananTozOzelligi.set(toplamToplananToz);
            }
            istatistikleriGuncelle();
        } else {
            mevcutHucre.setTemizlendi(true);
        }
    }

    private void yolBoyuncaIlerle() {
        if (planlananYol.isEmpty()) {
            if (robot.getX() == oda.getSarjIstasyonuX() && robot.getY() == oda.getSarjIstasyonuY()) {
                if (!robot.sarjOluyorMu()) SesYoneticisi.playChargeSound();
                robot.setSarjOluyor(true);
                durumOzelligi.set("Şarj Oluyor...");
            } else {
                robot.setIstasyonaDonuyor(false);
                durumOzelligi.set("Çalışıyor");
            }
            return;
        }

        int[] sonraki = planlananYol.poll();
        robot.bataryayiTuket(Robot.HAREKET_BATARYA_MALIYETI);
        robot.setKonum(sonraki[0], sonraki[1]);
        hareketeGoreYonuGuncelle(sonraki[0], sonraki[1]);
        toplamHareket++;
    }

    private void istasyonaYolPlanla() {
        List<int[]> yol = BFSYolBulucu.yolBul(
                oda, robot.getX(), robot.getY(),
                oda.getSarjIstasyonuX(), oda.getSarjIstasyonuY()
        );
        planlananYol.clear();
        planlananYol.addAll(yol);
    }

    private void hareketeGoreYonuGuncelle(int yeniX, int yeniY) {
        int dx = yeniX - robot.getX();
        int dy = yeniY - robot.getY();
        for (Yon y : Yon.values()) {
            if (y.getDx() == dx && y.getDy() == dy) {
                robot.setYon(y);
                break;
            }
        }
    }

    private void algoritmayaGoreIlerle() {
        switch (algoritma) {
            case RASTGELE -> rastgeleIlerle();
            case SPIRAL -> spiralIlerle();
            case DUVAR_TAKIP -> duvarTakipIlerle();
        }
    }

    // ================= DÜZELTİLMİŞ RASTGELE ALGORİTMASI (GİDİLMEMİŞ YOL İHTİMALİ YÜKSEK) =================
    private void rastgeleIlerle() {
        List<Yon> kullanilabilir = kullanilabilirYonleriGetir();
        if (kullanilabilir.isEmpty()) return;

        Yon tersi = robot.getYon().tersYon();

        // Git-gel yapmaması için arkasını dönmesini listeden çıkarıyoruz (sıkışmadıysa)
        if (kullanilabilir.size() > 1) {
            kullanilabilir.remove(tersi);
        }

        // Daha önce gidilmemiş (temizlenmemiş) yolları tespit edelim
        List<Yon> gidilmemisYonler = new ArrayList<>();
        for (Yon y : kullanilabilir) {
            int nx = robot.getX() + y.getDx();
            int ny = robot.getY() + y.getDy();
            Hucre h = oda.getHucre(nx, ny);
            if (h != null && !h.temizlendiMi()) {
                gidilmemisYonler.add(y);
            }
        }

        Yon secilen;

        // Eğer gidilmemiş (yeni) bir yol varsa, %85 ihtimalle o yönü seçecek şekilde ağırlık veriyoruz.
        // Bu sayede hem çok daha akıllı ve hızlı harita geziyor hem de rastgelelikten ödün vermiyor.
        if (!gidilmemisYonler.isEmpty() && rastgele.nextInt(100) < 85) {
            secilen = gidilmemisYonler.get(rastgele.nextInt(gidilmemisYonler.size()));
        } else {
            // Eğer her yer gezilmişse veya %15 şansa denk geldiyse normal rastgele davranır
            secilen = kullanilabilir.get(rastgele.nextInt(kullanilabilir.size()));
        }

        robot.setYon(secilen);
        hareketiGerceklestir(secilen);
    }

    // ================= DÜZELTİLMİŞ SPİRAL ALGORİTMASI =================
    private void spiralIlerle() {
        if (ilerleyebilirMi(spiralYonu) && spiralAdimi < spiralDogruAdimSayisi) {
            hareketiGerceklestir(spiralYonu);
            spiralAdimi++;
        } else {
            if (!ilerleyebilirMi(spiralYonu)) {
                SesYoneticisi.playBumpSound();
                spiralAdimi = 0;
                spiralDogruAdimSayisi = 1;
                spiralKenarSayisi = 0;

                List<Yon> kullanilabilir = kullanilabilirYonleriGetir();
                if (!kullanilabilir.isEmpty()) {
                    spiralYonu = kullanilabilir.get(rastgele.nextInt(kullanilabilir.size()));
                    robot.setYon(spiralYonu);
                }
            } else {
                spiralAdimi = 0;
                spiralKenarSayisi++;
                spiralYonu = spiralYonu.sagaDon();

                if (spiralKenarSayisi % 2 == 0) {
                    spiralDogruAdimSayisi++;
                }

                if (ilerleyebilirMi(spiralYonu)) {
                    hareketiGerceklestir(spiralYonu);
                    spiralAdimi++;
                }
            }
        }
    }

    // ================= DÜZELTİLMİŞ DUVAR TAKİP ALGORİTMASI =================
    private void duvarTakipIlerle() {
        Yon sag = robot.getYon().sagaDon();
        Yon on = robot.getYon();
        Yon sol = robot.getYon().solaDon();
        Yon arkam = robot.getYon().tersYon();

        if (!duvaraYaslaniyorMu) {
            if (ilerleyebilirMi(on)) {
                hareketiGerceklestir(on);
                if (!ilerleyebilirMi(sag)) duvaraYaslaniyorMu = true;
            } else {
                robot.setYon(sol);
                duvaraYaslaniyorMu = true;
            }
            return;
        }

        if (ilerleyebilirMi(sag)) {
            robot.setYon(sag);
            hareketiGerceklestir(sag);
        } else if (ilerleyebilirMi(on)) {
            hareketiGerceklestir(on);
        } else if (ilerleyebilirMi(sol)) {
            robot.setYon(sol);
            hareketiGerceklestir(sol);
        } else if (ilerleyebilirMi(arkam)) {
            robot.setYon(arkam);
            hareketiGerceklestir(arkam);
        }
    }

    private boolean ilerleyebilirMi(Yon y) {
        int nx = robot.getX() + y.getDx();
        int ny = robot.getY() + y.getDy();
        return oda.gecilebilirMi(nx, ny);
    }

    private void hareketiGerceklestir(Yon y) {
        int nx = robot.getX() + y.getDx();
        int ny = robot.getY() + y.getDy();
        if (oda.gecilebilirMi(nx, ny)) {
            robot.setYon(y);
            robot.bataryayiTuket(Robot.HAREKET_BATARYA_MALIYETI);
            robot.setKonum(nx, ny);
            toplamHareket++;
        } else {
            SesYoneticisi.playBumpSound();
            robot.setYon(y.sagaDon());
        }
    }

    private List<Yon> kullanilabilirYonleriGetir() {
        List<Yon> yonler = new ArrayList<>();
        for (Yon y : Yon.values()) {
            if (ilerleyebilirMi(y)) yonler.add(y);
        }
        return yonler;
    }

    private void ozellikleriGuncelle() {
        bataryaOzelligi.set(robot.getBatarya());
        konumOzelligi.set("(" + robot.getX() + ", " + robot.getY() + ")");
        yonOzelligi.set(robot.getYon().getEkranAdi());
        toplamAlanOzelligi.set(oda.getToplamYuruyebilirAlan());
        istatistikleriGuncelle();
    }

    private void istatistikleriGuncelle() {
        temizlenenAlanOzelligi.set(oda.getTemizlenenAlan());
        kirliAlanOzelligi.set(oda.getKirliAlanSayisi());
    }

    private void gecenSureyiGuncelle() {
        if (simulasyonBaslangicZamani == 0) return;
        long toplamMs = System.currentTimeMillis() - simulasyonBaslangicZamani - duraklatmaGecenSureMs;
        long saniye = toplamMs / 1000;
        long dakika = saniye / 60;
        saniye = saniye % 60;
        gecenSureOzelligi.set(String.format("%02d:%02d", dakika, saniye));
    }

    public Oda getOda() { return oda; }
    public Robot getRobot() { return robot; }
    public boolean calisiyorMu() { return calisiyor; }
    public boolean duraklatildiMi() { return duraklatildi; }
    public boolean temizlikYapiliyorMu() { return temizlikDevamEdiyor || (System.currentTimeMillis() - sonTemizlemeZamani < 300); }

    public TemizlikAlgoritmasi getAlgoritma() { return algoritma; }

    public void setAlgoritma(TemizlikAlgoritmasi algoritma) {
        this.algoritma = algoritma;
        spiralAdimi = 0;
        spiralDogruAdimSayisi = 1;
        spiralKenarSayisi = 0;
        spiralYonu = Yon.DOGU;
        duvaraYaslaniyorMu = false;
    }

    public KirTipi getSecilenKirTipi() { return secilenKirTipi; }
    public void setSecilenKirTipi(KirTipi tip) { this.secilenKirTipi = tip; }
    public double getHizCarpani() { return hizCarpani; }
    public void setHizCarpani(double hizCarpani) { this.hizCarpani = hizCarpani; }

    public DoubleProperty bataryaOzelligi() { return bataryaOzelligi; }
    public StringProperty konumOzelligi() { return konumOzelligi; }
    public StringProperty yonOzelligi() { return yonOzelligi; }
    public IntegerProperty temizlenenAlanOzelligi() { return temizlenenAlanOzelligi; }
    public IntegerProperty toplamAlanOzelligi() { return toplamAlanOzelligi; }
    public IntegerProperty kirliAlanOzelligi() { return kirliAlanOzelligi; }
    public StringProperty gecenSureOzelligi() { return gecenSureOzelligi; }
    public StringProperty durumOzelligi() { return durumOzelligi; }
    public DoubleProperty toplananTozOzelligi() { return toplananTozOzelligi; }
    public IntegerProperty ulasilamayanAlanOzelligi() { return ulasilamayanAlanOzelligi; }

    public int getToplamBaslangicKiri() {
        // Toplam kir = Odada anlık olarak kalan kirler + Şu ana kadar robotun temizledikleri
        return Math.max(1, oda.getKirliAlanSayisi() + (int) toplamToplananToz);
    }
}