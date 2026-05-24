package com.robotvacuum.model;

import com.robotvacuum.util.BFSYolBulucu;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Simülasyonun beyni — MVC'deki "Model" katmanı.
 *
 * Burada robotu nasıl hareket ettireceğimizi (3 algoritma), bataryayı,
 * kirleri ve zamanı yönetiyoruz. View bu sınıfı sadece "dışarıdan" görür;
 * binding üzerinden değerleri okur.
 *
 * Not (OOP - Tek Sorumluluk): UI ve hareket mantığı ayrılmış. UI'ı değiştirsek
 * bile bu sınıfa dokunmaya gerek kalmıyor. Hareket algoritmaları da
 * TemizlikAlgoritmasi enum'una göre seçildiği için yeni algoritma eklemek
 * sadece bu dosyada bir switch case eklemek demek.
 */
public class SimulasyonModeli {

    // --- Sahnedeki nesneler ---
    private final Oda oda;
    private final Robot robot;

    // --- JavaFX Property'leri ---
    // View bunlara binding yapar; biz set ettiğimizde UI otomatik güncellenir.
    private final DoubleProperty bataryaOzelligi       = new SimpleDoubleProperty(100.0);
    private final StringProperty konumOzelligi         = new SimpleStringProperty("(0, 0)");
    private final StringProperty yonOzelligi           = new SimpleStringProperty("Doğu (→)");
    private final IntegerProperty temizlenenAlanOzelligi = new SimpleIntegerProperty(0);
    private final IntegerProperty toplamAlanOzelligi   = new SimpleIntegerProperty(1);
    private final IntegerProperty kirliAlanOzelligi    = new SimpleIntegerProperty(0);
    private final StringProperty gecenSureOzelligi     = new SimpleStringProperty("00:00");
    private final StringProperty durumOzelligi         = new SimpleStringProperty("Hazır");
    private final DoubleProperty toplananTozOzelligi   = new SimpleDoubleProperty(0);

    // --- Simülasyon durumu ---
    private boolean calisiyor = false;
    private boolean duraklatildi = false;
    private long simulasyonBaslangicZamani = 0;
    private long duraklatmaSuresiMs = 0;
    private long duraklatmaBaslangicZamani = 0;

    // --- Kullanıcı ayarları ---
    private TemizlikAlgoritmasi algoritma = TemizlikAlgoritmasi.SPIRAL;
    private KirTuru secilenKirTuru = KirTuru.TOZ;
    private double hizCarpani = 1.0;

    // --- Spiral algoritmasının iç durumu ---
    private final Random rastgele = new Random();
    private int spiralAdim = 0;                    // bu bacakta kaç adım attık
    private int spiralBacaktakiAdimSayisi = 1;     // bu bacağın uzunluğu
    private int spiralBacakSayisi = 0;             // tamamlanan bacak sayısı
    private Yon spiralYonu = Yon.DOGU;

    // --- BFS ile şarja dönüş yolu ---
    private final Queue<int[]> planlananYol = new LinkedList<>();

    // --- Çok adımlı kir temizliği (sıvı/leke) ---
    private boolean temizlikDevamEdiyor = false;
    private int kalanTemizlikAdimi = 0;
    private double bekleyenBataryaMaliyeti = 0;

    // --- İstatistikler ---
    private double toplamToplananToz = 0;
    private int toplamHareket = 0;
    private int baslangicKirliHucreSayisi = 0;

    public SimulasyonModeli() {
        oda = new Oda();
        robot = new Robot(oda.getSarjIstasyonuX(), oda.getSarjIstasyonuY());
        ozellikleriGuncelle();
    }

    // ==================== SİMÜLASYON KONTROLÜ ====================

    /** İlk başlatma veya duraklatmadan devam etme. */
    public void basla() {
        if (!calisiyor) {
            calisiyor = true;
            duraklatildi = false;
            simulasyonBaslangicZamani = System.currentTimeMillis();
            baslangicKirliHucreSayisi = oda.getKirliHucreSayisi();
            durumOzelligi.set("Çalışıyor");
        } else if (duraklatildi) {
            // Duraklatma süresini toplam zamandan düşmek için ne kadar bekledik onu hesapla
            duraklatildi = false;
            duraklatmaSuresiMs += System.currentTimeMillis() - duraklatmaBaslangicZamani;
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

    /** Robot ve odayı başlangıç durumuna döndür. */
    public void sifirla() {
        calisiyor = false;
        duraklatildi = false;
        simulasyonBaslangicZamani = 0;
        duraklatmaSuresiMs = 0;
        temizlikDevamEdiyor = false;
        kalanTemizlikAdimi = 0;
        toplamToplananToz = 0;
        toplamHareket = 0;
        spiralAdim = 0;
        spiralBacaktakiAdimSayisi = 1;
        spiralBacakSayisi = 0;
        spiralYonu = Yon.DOGU;
        planlananYol.clear();

        oda.sifirla();
        robot.sifirla(oda.getSarjIstasyonuX(), oda.getSarjIstasyonuY());
        durumOzelligi.set("Sıfırlandı");
        ozellikleriGuncelle();
    }

    /** Robotu şarj istasyonuna geri gönder (BFS ile en kısa yoldan). */
    public void istasyonaDon() {
        if (!calisiyor || duraklatildi) return;
        istasyonaYolPlanla();
        robot.setIstasyonaDonuyor(true);
        durumOzelligi.set("İstasyona Dönüyor");
    }

    // ==================== TICK (Her kare çağrılır) ====================

    /**
     * Animasyon her çalıştığında bu metot bir kez çağrılır.
     * Sırası: zaman güncelle → batarya bitti mi? → şarj oluyor mu? →
     * çok adımlı temizlik var mı? → düşük bataryada otomatik dönüş başlat →
     * algoritmaya göre veya BFS yolu boyunca hareket et → mevcut hücreyi temizle.
     */
    public void tik() {
        if (!calisiyor || duraklatildi) return;

        gecenSureyiGuncelle();

        // Batarya bittiyse simülasyonu durdur
        if (robot.bataryaBosMu()) {
            robot.setSarjOluyor(false);
            durumOzelligi.set("Batarya Bitti!");
            calisiyor = false;
            return;
        }

        // Şarj istasyonundayken bataryayı doldur
        if (robot.sarjOluyorMu()) {
            robot.bataryaSarjEt(1.5);
            bataryaOzelligi.set(robot.getBatarya());
            if (robot.getBatarya() >= Robot.MAKS_BATARYA) {
                // Şarj tamam: tekrar çalışmaya başla
                robot.setSarjOluyor(false);
                robot.setIstasyonaDonuyor(false);
                planlananYol.clear();
                durumOzelligi.set("Şarj Tamamlandı - Çalışıyor");
            }
            return;
        }

        // Sıvı/leke gibi çok adımlı kirler için sayaç bitene kadar bekle
        if (temizlikDevamEdiyor) {
            kalanTemizlikAdimi--;
            robot.bataryaTuket(bekleyenBataryaMaliyeti);
            bataryaOzelligi.set(robot.getBatarya());
            if (kalanTemizlikAdimi <= 0) {
                temizlikDevamEdiyor = false;
                oda.getHucre(robot.getX(), robot.getY()).temizle();
                toplamToplananToz++;
                toplananTozOzelligi.set(toplamToplananToz);
                istatistikleriGuncelle();
            }
            return;
        }

        // Batarya düştü ve henüz dönmüyorsak otomatik dönüş başlat
        if (robot.bataryaDusukMu() && !robot.istasyonaDonuyorMu()) {
            istasyonaDon();
        }

        // Hareket: BFS yolu varsa onu takip et, yoksa algoritmaya göre git
        if (robot.istasyonaDonuyorMu()) {
            yolBoyuncaHarketEt();
        } else {
            algoritmayaGoreHarketEt();
        }

        // Bulunduğu hücrede kir varsa temizlemeye başla
        Hucre mevcutHucre = oda.getHucre(robot.getX(), robot.getY());
        if (mevcutHucre != null && mevcutHucre.kirVarMi()) {
            KirTuru kir = mevcutHucre.getKirTuru();
            int adimSayisi = kir.getTemizlikAdimSayisi();
            double adimBasinaMaliyet = Robot.HAREKET_BATARYA_MALIYETI * kir.getBataryaMaliyetCarpani();
            if (adimSayisi > 1) {
                // Sıvı veya leke: birden fazla geçişte temizlenir
                temizlikDevamEdiyor = true;
                kalanTemizlikAdimi = adimSayisi - 1;
                bekleyenBataryaMaliyeti = adimBasinaMaliyet;
                mevcutHucre.temizle(); // bu tick'lik adımı şimdi atıyoruz
            } else {
                // Toz: tek geçişte bitiyor
                mevcutHucre.temizle();
                robot.bataryaTuket(adimBasinaMaliyet);
                toplamToplananToz++;
                toplananTozOzelligi.set(toplamToplananToz);
            }
            istatistikleriGuncelle();
        } else if (mevcutHucre != null) {
            // Kir yoktu ama robot buradan geçti, "temizlendi" işaretle
            mevcutHucre.setTemiz(true);
        }

        ozellikleriGuncelle();
    }

    // ==================== HAREKET ====================

    /** BFS ile hesapladığımız yolun bir sonraki adımını uygula. */
    private void yolBoyuncaHarketEt() {
        if (planlananYol.isEmpty()) {
            // Yol bitti — istasyona vardık mı?
            if (robot.getX() == oda.getSarjIstasyonuX() && robot.getY() == oda.getSarjIstasyonuY()) {
                robot.setSarjOluyor(true);
                durumOzelligi.set("Şarj Oluyor...");
            } else {
                // Bir şekilde istasyona varamadıysak normal çalışmaya dön
                robot.setIstasyonaDonuyor(false);
                durumOzelligi.set("Çalışıyor");
            }
            return;
        }

        int[] sonraki = planlananYol.poll();
        robot.bataryaTuket(Robot.HAREKET_BATARYA_MALIYETI);
        robot.konumAyarla(sonraki[0], sonraki[1]);
        yonuHareketeGoreGuncelle(sonraki[0], sonraki[1]);
        toplamHareket++;
    }

    /** Robotun bulunduğu yerden şarj istasyonuna BFS ile yol planla. */
    private void istasyonaYolPlanla() {
        List<int[]> yol = BFSYolBulucu.yolBul(
                oda, robot.getX(), robot.getY(),
                oda.getSarjIstasyonuX(), oda.getSarjIstasyonuY()
        );
        planlananYol.clear();
        planlananYol.addAll(yol);
    }

    /** Robotun yönünü hareket ettiği yöne göre ayarla. */
    private void yonuHareketeGoreGuncelle(int yeniX, int yeniY) {
        int dx = yeniX - robot.getX();
        int dy = yeniY - robot.getY();
        for (Yon y : Yon.values()) {
            if (y.getDx() == dx && y.getDy() == dy) {
                robot.setYon(y);
                break;
            }
        }
    }

    /** Seçili algoritmaya göre tek adım hareket et. */
    private void algoritmayaGoreHarketEt() {
        switch (algoritma) {
            case RASTGELE:    rastgeleHarketEt(); break;
            case SPIRAL:      spiralHarketEt();   break;
            case DUVAR_TAKIP: duvarTakipEt();    break;
        }
    }

    /**
     * Rastgele algoritma: Robot rastgele bir yöne ilerler.
     * Sürekli yön değiştirmesin diye %70 ihtimalle mevcut yönde devam eder.
     */
    private void rastgeleHarketEt() {
        List<Yon> kullanilabilirYonler = kullanilabilirYonleriGetir();
        if (kullanilabilirYonler.isEmpty()) return;

        if (kullanilabilirYonler.contains(robot.getYon()) && rastgele.nextDouble() > 0.3) {
            // Mevcut yöne devam
            hareketiUygula(robot.getYon());
        } else {
            // Rastgele bir yön seç
            Yon secilen = kullanilabilirYonler.get(rastgele.nextInt(kullanilabilirYonler.size()));
            robot.setYon(secilen);
            hareketiUygula(secilen);
        }
    }

    /**
     * Spiral algoritma: önce 1 adım git, sağa dön, 1 adım, sağa dön, 2 adım,
     * sağa dön, 2 adım ... şeklinde dışa doğru sarmal çizer.
     * Engele takılırsa sağa, sonra sola, en son rastgele yön dener.
     */
    private void spiralHarketEt() {
        if (yoneHareketEdebilirMi(spiralYonu)) {
            hareketiUygula(spiralYonu);
            spiralAdim++;
            if (spiralAdim >= spiralBacaktakiAdimSayisi) {
                // Bu bacak bitti; saat yönünde dön ve sayaçları güncelle
                spiralAdim = 0;
                spiralBacakSayisi++;
                spiralYonu = spiralYonu.sagaDon();
                // Her 2 bacakta bir bacak uzunluğu artıyor (spiral genişlemesi)
                if (spiralBacakSayisi % 2 == 0) spiralBacaktakiAdimSayisi++;
            }
        } else {
            // Spirali engele takıldık: önce sağa, sonra sola dene
            Yon sag = spiralYonu.sagaDon();
            Yon sol = spiralYonu.solaDon();
            if (yoneHareketEdebilirMi(sag)) {
                spiralYonu = sag;
                spiralAdim = 0;
                hareketiUygula(spiralYonu);
            } else if (yoneHareketEdebilirMi(sol)) {
                spiralYonu = sol;
                spiralAdim = 0;
                hareketiUygula(spiralYonu);
            } else {
                // Hepsi kapalıysa rastgele bir yön bul
                List<Yon> kullanilabilirYonler = kullanilabilirYonleriGetir();
                if (!kullanilabilirYonler.isEmpty()) {
                    spiralYonu = kullanilabilirYonler.get(rastgele.nextInt(kullanilabilirYonler.size()));
                    hareketiUygula(spiralYonu);
                }
            }
        }
    }

    /**
     * Duvar Takip algoritma — sağ el kuralı:
     * 1) Sağa dönebiliyorsan sağa dön (duvarı sağında tut)
     * 2) Yoksa düz devam et
     * 3) Yoksa sola dön
     * 4) Hiçbiri olmazsa geri dön
     */
    private void duvarTakipEt() {
        Yon sagYon = robot.getYon().sagaDon();
        Yon mevcutYon = robot.getYon();
        Yon solYon = robot.getYon().solaDon();
        Yon geriYon = robot.getYon().tersi();

        if (yoneHareketEdebilirMi(sagYon)) {
            robot.setYon(sagYon);
            hareketiUygula(sagYon);
        } else if (yoneHareketEdebilirMi(mevcutYon)) {
            hareketiUygula(mevcutYon);
        } else if (yoneHareketEdebilirMi(solYon)) {
            robot.setYon(solYon);
            hareketiUygula(solYon);
        } else if (yoneHareketEdebilirMi(geriYon)) {
            robot.setYon(geriYon);
            hareketiUygula(geriYon);
        }
        // Hiçbir yön açık değilse robot olduğu yerde kalır
    }

    private boolean yoneHareketEdebilirMi(Yon y) {
        int yeniX = robot.getX() + y.getDx();
        int yeniY = robot.getY() + y.getDy();
        return oda.gecilebilirMi(yeniX, yeniY);
    }

    /**
     * Robotu belirtilen yöne 1 adım götür.
     * Hücre engel/sınır dışıysa hareket yok; sadece yön değiştirir.
     */
    private void hareketiUygula(Yon y) {
        int yeniX = robot.getX() + y.getDx();
        int yeniY = robot.getY() + y.getDy();
        if (oda.gecilebilirMi(yeniX, yeniY)) {
            robot.setYon(y);
            robot.bataryaTuket(Robot.HAREKET_BATARYA_MALIYETI);
            robot.konumAyarla(yeniX, yeniY);
            toplamHareket++;
        } else {
            // Çarpışma: yerinde kal, sağa dön ki bir sonraki adımda farklı yöne baksın
            robot.setYon(y.sagaDon());
        }
    }

    private List<Yon> kullanilabilirYonleriGetir() {
        List<Yon> yonler = new ArrayList<>();
        for (Yon y : Yon.values()) {
            if (yoneHareketEdebilirMi(y)) yonler.add(y);
        }
        return yonler;
    }

    // ==================== Property güncelleme ====================

    private void ozellikleriGuncelle() {
        bataryaOzelligi.set(robot.getBatarya());
        konumOzelligi.set("(" + robot.getX() + ", " + robot.getY() + ")");
        yonOzelligi.set(robot.getYon().getGorunenAd());
        toplamAlanOzelligi.set(oda.getToplamZeminHucresi());
        istatistikleriGuncelle();
    }

    private void istatistikleriGuncelle() {
        temizlenenAlanOzelligi.set(oda.getTemizlenenHucreSayisi());
        kirliAlanOzelligi.set(oda.getKirliHucreSayisi());
    }

    private void gecenSureyiGuncelle() {
        if (simulasyonBaslangicZamani == 0) return;
        long toplamMs = System.currentTimeMillis() - simulasyonBaslangicZamani - duraklatmaSuresiMs;
        long saniye = toplamMs / 1000;
        long dakika = saniye / 60;
        saniye = saniye % 60;
        gecenSureOzelligi.set(String.format("%02d:%02d", dakika, saniye));
    }

    // ==================== Getter / Setter ====================

    public Oda getOda() { return oda; }
    public Robot getRobot() { return robot; }
    public boolean calisiyorMu() { return calisiyor; }
    public boolean duraklatildiMi() { return duraklatildi; }

    public TemizlikAlgoritmasi getAlgoritma() { return algoritma; }
    public void setAlgoritma(TemizlikAlgoritmasi algoritma) {
        this.algoritma = algoritma;
        // Algoritma değişince spiral sayaçlarını sıfırla
        spiralAdim = 0;
        spiralBacaktakiAdimSayisi = 1;
        spiralBacakSayisi = 0;
        spiralYonu = Yon.DOGU;
    }

    public KirTuru getSecilenKirTuru() { return secilenKirTuru; }
    public void setSecilenKirTuru(KirTuru tur) { this.secilenKirTuru = tur; }

    public double getHizCarpani() { return hizCarpani; }
    public void setHizCarpani(double hizCarpani) { this.hizCarpani = hizCarpani; }

    // --- View'in binding yapacağı property'ler ---
    public DoubleProperty bataryaOzelligi()        { return bataryaOzelligi; }
    public StringProperty konumOzelligi()          { return konumOzelligi; }
    public StringProperty yonOzelligi()            { return yonOzelligi; }
    public IntegerProperty temizlenenAlanOzelligi(){ return temizlenenAlanOzelligi; }
    public IntegerProperty toplamAlanOzelligi()    { return toplamAlanOzelligi; }
    public IntegerProperty kirliAlanOzelligi()     { return kirliAlanOzelligi; }
    public StringProperty gecenSureOzelligi()      { return gecenSureOzelligi; }
    public StringProperty durumOzelligi()          { return durumOzelligi; }
    public DoubleProperty toplananTozOzelligi()    { return toplananTozOzelligi; }

    /** Yüzde hesabında 0'a bölünmesin diye en az 1 dön. */
    public int getToplamBaslangicKirSayisi() {
        int sayi = baslangicKirliHucreSayisi > 0
                ? baslangicKirliHucreSayisi
                : oda.getKirliHucreSayisi() + (int) toplamToplananToz;
        return Math.max(1, sayi);
    }
}
