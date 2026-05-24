package com.robotvacuum.model;

import com.robotvacuum.util.BFSYolBulucu;
import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Simülasyonun merkezi model sınıfı (MVC mimarisinde "Model" katmanı).
 *
 * <p>Bu sınıf:
 * <ul>
 *   <li>Oda ve robot nesnelerini barındırır</li>
 *   <li>Tüm simülasyon durumunu (çalışıyor, duraklatıldı, vb.) tutar</li>
 *   <li>Temizlik algoritmalarının (Rastgele, Spiral, Duvar Takip) mantığını uygular</li>
 *   <li>JavaFX Property nesneleri ile View katmanının kolayca veri bağlaması (binding)
 *       yapmasını sağlar</li>
 *   <li>Her simülasyon adımında ({@link #tik()}) çağrılır ve modelin durumunu ilerletir</li>
 * </ul>
 */
public class SimulasyonModeli {

    // --- Oda ve Robot ---
    private final Oda oda;
    private final Robot robot;

    // --- View'a bağlama için gözlemlenebilir özellikler (JavaFX Properties) ---
    /** Bataryanın gözlemlenebilir özelliği (0.0 - 100.0) */
    private final DoubleProperty bataryaOzelligi = new SimpleDoubleProperty(100.0);
    /** Robotun konumunu "(x, y)" formatında tutan özellik */
    private final StringProperty konumOzelligi = new SimpleStringProperty("(0, 0)");
    /** Robotun bakış yönünü gösteren özellik */
    private final StringProperty yonOzelligi = new SimpleStringProperty("Doğu (→)");
    /** Temizlenmiş alan hücre sayısı */
    private final IntegerProperty temizlenenAlanOzelligi = new SimpleIntegerProperty(0);
    /** Odadaki toplam zemin (geçilebilir) hücre sayısı */
    private final IntegerProperty toplamAlanOzelligi = new SimpleIntegerProperty(1);
    /** Hâlâ kirli olan hücre sayısı */
    private final IntegerProperty kirliAlanOzelligi = new SimpleIntegerProperty(0);
    /** Simülasyon başlangıcından bu yana geçen süre ("dd:ss") */
    private final StringProperty gecenSureOzelligi = new SimpleStringProperty("00:00");
    /** Simülasyonun mevcut durumu (Hazır, Çalışıyor, Duraklatıldı, vb.) */
    private final StringProperty durumOzelligi = new SimpleStringProperty("Hazır");
    /** Toplanan toz miktarı (temizlenen kir sayısı) */
    private final DoubleProperty toplananTozOzelligi = new SimpleDoubleProperty(0);

    // --- Simülasyon durumu ---
    /** Simülasyon çalışıyor mu? */
    private boolean calisiyor = false;
    /** Simülasyon duraklatıldı mı? */
    private boolean duraklatildi = false;
    /** Simülasyonun başlangıç zamanı (milisaniye) */
    private long simulasyonBaslangicZamani = 0;
    /** Duraklatma süresince geçen toplam zaman (milisaniye) */
    private long duraklatmaSuresiMs = 0;
    /** Duraklatmanın başladığı zaman */
    private long duraklatmaBaslangicZamani = 0;

    // --- Ayarlar ---
    /** Şu anda seçili olan temizlik algoritması */
    private TemizlikAlgoritmasi algoritma = TemizlikAlgoritmasi.SPIRAL;
    /** Kullanıcının seçtiği kir türü (kir ekleme modunda kullanılır) */
    private KirTipi secilenKirTipi = KirTipi.TOZ;
    /** Simülasyon hızı çarpanı (0.5 - 3.0) */
    private double hizCarpani = 1.0;

    // --- Algoritma durumu (spiral hareket için) ---
    /** Rastgele yönler için kullanılan rastgele sayı üreteci */
    private final Random rastgele = new Random();
    /** Spiral algoritmada kullanılan yarıçap değeri */
    private int spiralYaricap = 0;
    /** Spiralin mevcut bacağındaki adım sayacı */
    private int spiralAdim = 0;
    /** Spiralin bir bacağında atılacak adım sayısı */
    private int spiralBacaktakiAdimSayisi = 1;
    /** Spirallerde tamamlanan bacak sayısı */
    private int spiralBacakSayisi = 0;
    /** Spiral hareket için mevcut yön */
    private Yon spiralYonu = Yon.DOGU;

    // --- Yol takibi (BFS ile istasyona dönüş) ---
    /** BFS ile hesaplanmış istasyona dönüş yolu (sırayla takip edilir) */
    private Queue<int[]> planlananYol = new LinkedList<>();

    // --- Devam eden temizlik işlemi (çok adımlı kirler için) ---
    /** Mevcut hücrede çok adımlı bir temizlik işlemi devam ediyor mu? */
    private boolean temizlikDevamEdiyor = false;
    /** Mevcut temizlik işleminde kalan adım sayısı */
    private int kalanTemizlikAdimi = 0;
    /** Her temizlik adımında düşülecek batarya maliyeti */
    private double bekleyenBataryaMaliyeti = 0;

    // --- İstatistikler ---
    /** Simülasyon süresince toplanan toplam toz miktarı */
    private double toplamToplananToz = 0;
    /** Robotun yaptığı toplam hareket sayısı */
    private int toplamHareket = 0;
    /** Simülasyon başlamadan önceki başlangıç kir sayısı */
    private int baslangicKirliHucreSayisi = 0;

    /**
     * Yeni bir simülasyon modeli oluşturur.
     * Varsayılan oda boyutu ve robot konumu (şarj istasyonu) ile başlar.
     */
    public SimulasyonModeli() {
        oda = new Oda();
        robot = new Robot(oda.getSarjIstasyonuX(), oda.getSarjIstasyonuY());
        ozellikleriGuncelle();
    }

    // ==================== SİMÜLASYON KONTROLÜ ====================

    /**
     * Simülasyonu başlatır veya duraklatılmış durumdaysa devam ettirir.
     * Çalışıyor durumuna geçildiğinde başlangıç istatistikleri kaydedilir.
     */
    public void basla() {
        if (!calisiyor) {
            // İlk kez başlatılıyor
            calisiyor = true;
            duraklatildi = false;
            simulasyonBaslangicZamani = System.currentTimeMillis();
            baslangicKirliHucreSayisi = oda.getKirliHucreSayisi();
            durumOzelligi.set("Çalışıyor");
        } else if (duraklatildi) {
            // Duraklatılmış simülasyona devam ediliyor
            duraklatildi = false;
            duraklatmaSuresiMs += System.currentTimeMillis() - duraklatmaBaslangicZamani;
            durumOzelligi.set("Çalışıyor");
        }
    }

    /**
     * Simülasyonu duraklatır. Çalışmıyorsa veya zaten duraklatılmışsa hiçbir şey yapmaz.
     */
    public void duraklat() {
        if (calisiyor && !duraklatildi) {
            duraklatildi = true;
            duraklatmaBaslangicZamani = System.currentTimeMillis();
            durumOzelligi.set("Duraklatıldı");
        }
    }

    /**
     * Simülasyonu tamamen sıfırlar. Robot ve oda başlangıç konumlarına döner,
     * tüm istatistikler ve algoritma durumu temizlenir.
     */
    public void sifirla() {
        calisiyor = false;
        duraklatildi = false;
        simulasyonBaslangicZamani = 0;
        duraklatmaSuresiMs = 0;
        temizlikDevamEdiyor = false;
        kalanTemizlikAdimi = 0;
        toplamToplananToz = 0;
        toplamHareket = 0;
        spiralYaricap = 0;
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

    /**
     * Robotu şarj istasyonuna gönderir. BFS algoritması ile en kısa yol hesaplanır
     * ve robot bu yolu takip ederek istasyona ulaşır.
     */
    public void istasyonaDon() {
        if (!calisiyor || duraklatildi) return;
        istasyonaYolPlanla();
        robot.setIstasyonaDonuyor(true);
        durumOzelligi.set("İstasyona Dönüyor");
    }

    // ==================== SİMÜLASYON ADIMI (TICK) ====================

    /**
     * Her animasyon karesinde çağrılır ve simülasyonu bir adım ilerletir.
     *
     * <p>İşlem sırası:</p>
     * <ol>
     *   <li>Geçen süreyi güncelle</li>
     *   <li>Batarya boşsa simülasyonu durdur</li>
     *   <li>Şarj oluyorsa bataryayı doldur</li>
     *   <li>Çok adımlı temizlik devam ediyorsa bir adım daha temizle</li>
     *   <li>Batarya düşükse istasyona dönüş başlat</li>
     *   <li>Algoritma veya planlı yola göre robotu hareket ettir</li>
     *   <li>Mevcut hücrede kir varsa temizle</li>
     * </ol>
     */
    public void tik() {
        if (!calisiyor || duraklatildi) return;

        // Geçen süreyi güncelle
        gecenSureyiGuncelle();

        // Batarya tamamen bitmişse simülasyonu durdur
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
                // Şarj tamamlandı, normal çalışmaya geri dön
                robot.setSarjOluyor(false);
                robot.setIstasyonaDonuyor(false);
                planlananYol.clear();
                durumOzelligi.set("Şarj Tamamlandı - Çalışıyor");
            }
            return;
        }

        // Çok adımlı temizlik devam ediyorsa bir adım daha uygula
        if (temizlikDevamEdiyor) {
            kalanTemizlikAdimi--;
            robot.bataryaTuket(bekleyenBataryaMaliyeti);
            bataryaOzelligi.set(robot.getBatarya());
            if (kalanTemizlikAdimi <= 0) {
                // Temizlik bitti, hücreyi temiz olarak işaretle
                temizlikDevamEdiyor = false;
                oda.getHucre(robot.getX(), robot.getY()).temizle();
                toplamToplananToz++;
                toplananTozOzelligi.set(toplamToplananToz);
                istatistikleriGuncelle();
            }
            return;
        }

        // Batarya düşükse otomatik olarak istasyona dön
        if (robot.bataryaDusukMu() && !robot.istasyonaDonuyorMu()) {
            istasyonaDon();
        }

        // Robotu hareket ettir: istasyona dönüş ise planlı yol, yoksa algoritma
        if (robot.istasyonaDonuyorMu()) {
            yolBoyuncaHarketEt();
        } else {
            algoritmayaGoreHarketEt();
        }

        // Mevcut hücreyi temizle
        Hucre mevcutHucre = oda.getHucre(robot.getX(), robot.getY());
        if (mevcutHucre != null && mevcutHucre.kirVarMi()) {
            KirTipi kir = mevcutHucre.getKirTipi();
            int adimSayisi = kir.getTemizlikAdimSayisi();
            double adimBasinaMaliyet = Robot.HAREKET_BATARYA_MALIYETI * kir.getBataryaMaliyetCarpani();
            if (adimSayisi > 1) {
                // Çok adımlı kir: ilk adımı şimdi uygula, kalan adımlar sonraki tick'lerde
                temizlikDevamEdiyor = true;
                kalanTemizlikAdimi = adimSayisi - 1;
                bekleyenBataryaMaliyeti = adimBasinaMaliyet;
                mevcutHucre.temizle(); // ilk adım
            } else {
                // Tek adımlı kir: hemen temizlenir
                mevcutHucre.temizle();
                robot.bataryaTuket(adimBasinaMaliyet);
                toplamToplananToz++;
                toplananTozOzelligi.set(toplamToplananToz);
            }
            istatistikleriGuncelle();
        } else {
            // Kir yoksa hücreyi temizlenmiş olarak işaretle (gezilen alan kapsamı)
            mevcutHucre.setTemiz(true);
        }

        ozellikleriGuncelle();
    }

    // ==================== HAREKET ====================

    /**
     * Robotu BFS ile hesaplanmış planlı yol boyunca bir adım ilerletir.
     * Yol bittiğinde, eğer istasyondaysa şarja başlar.
     */
    private void yolBoyuncaHarketEt() {
        if (planlananYol.isEmpty()) {
            // Yol bitti - istasyona ulaştık mı?
            if (robot.getX() == oda.getSarjIstasyonuX() && robot.getY() == oda.getSarjIstasyonuY()) {
                robot.setSarjOluyor(true);
                durumOzelligi.set("Şarj Oluyor...");
            } else {
                // İstasyona ulaşamadıysak normal çalışmaya devam
                robot.setIstasyonaDonuyor(false);
                durumOzelligi.set("Çalışıyor");
            }
            return;
        }

        // Yolun bir sonraki adımına git
        int[] sonraki = planlananYol.poll();
        robot.bataryaTuket(Robot.HAREKET_BATARYA_MALIYETI);
        robot.konumAyarla(sonraki[0], sonraki[1]);
        yonuHareketeGoreGuncelle(sonraki[0], sonraki[1]);
        toplamHareket++;
    }

    /**
     * BFS algoritmasını kullanarak robottan şarj istasyonuna en kısa yolu hesaplar
     * ve planlananYol kuyruğunu doldurur.
     */
    private void istasyonaYolPlanla() {
        List<int[]> yol = BFSYolBulucu.yolBul(
            oda, robot.getX(), robot.getY(),
            oda.getSarjIstasyonuX(), oda.getSarjIstasyonuY()
        );
        planlananYol.clear();
        planlananYol.addAll(yol);
    }

    /**
     * Robot bir konuma taşındığında, bakış yönünü hareketin doğrultusuna göre günceller.
     *
     * @param yeniX Yeni X koordinatı
     * @param yeniY Yeni Y koordinatı
     */
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

    /**
     * Seçili olan temizlik algoritmasına göre robotu bir adım hareket ettirir.
     */
    private void algoritmayaGoreHarketEt() {
        switch (algoritma) {
            case RASTGELE -> rastgeleHarketEt();
            case SPIRAL -> spiralHarketEt();
            case DUVAR_TAKIP -> duvarTakipEt();
        }
    }

    /**
     * RASTGELE algoritması: Robot rastgele bir yöne hareket eder.
     * Zigzag hareketi azaltmak için %70 olasılıkla mevcut yönde devam eder.
     */
    private void rastgeleHarketEt() {
        List<Yon> kullanilabilirYonler = kullanilabilirYonleriGetir();
        if (kullanilabilirYonler.isEmpty()) return;

        // Zigzag'i azaltmak için %70 olasılıkla mevcut yönde devam et
        if (kullanilabilirYonler.contains(robot.getYon()) && rastgele.nextDouble() > 0.3) {
            hareketiUygula(robot.getYon());
        } else {
            // Aksi halde rastgele yeni bir yön seç
            Yon secilen = kullanilabilirYonler.get(rastgele.nextInt(kullanilabilirYonler.size()));
            robot.setYon(secilen);
            hareketiUygula(secilen);
        }
    }

    /**
     * SPIRAL algoritması: Robot sarmal şeklinde dışa doğru genişleyerek hareket eder.
     * Her iki bacakta bir, bacak uzunluğu bir artar (1, 1, 2, 2, 3, 3, ...).
     * Bir engele çarpınca sağa, sonra sola dönmeyi dener; tamamen tıkanırsa rastgele yöne gider.
     */
    private void spiralHarketEt() {
        // Dışa doğru spiral: her bacakta artan uzunlukla bir yönde hareket et
        if (yoneHareketEdebilirMi(spiralYonu)) {
            hareketiUygula(spiralYonu);
            spiralAdim++;
            if (spiralAdim >= spiralBacaktakiAdimSayisi) {
                // Bacak tamamlandı, saat yönünde dön
                spiralAdim = 0;
                spiralBacakSayisi++;
                spiralYonu = spiralYonu.sagaDon();
                // Her iki bacakta bir, bacak uzunluğunu artır (spiral genişlemesi)
                if (spiralBacakSayisi % 2 == 0) spiralBacaktakiAdimSayisi++;
            }
        } else {
            // Engele çarpıldı: önce sağa, sonra sola, son çare olarak rastgele bir yön dene
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
                // Tüm yönler tıkalı, rastgele bir yön dene
                List<Yon> kullanilabilirYonler = kullanilabilirYonleriGetir();
                if (!kullanilabilirYonler.isEmpty()) {
                    spiralYonu = kullanilabilirYonler.get(rastgele.nextInt(kullanilabilirYonler.size()));
                    hareketiUygula(spiralYonu);
                }
            }
        }
    }

    /**
     * DUVAR_TAKIP algoritması: Sağ el kuralı ile duvar boyunca ilerler.
     * Robot sağ elini sürekli duvarda tutuyormuş gibi hareket eder.
     *
     * <p>Öncelik sırası:
     * <ol>
     *   <li>Sağa dönebiliyorsa sağa dön (duvarı sağda tut)</li>
     *   <li>Aksi halde düz devam et</li>
     *   <li>Sola dön</li>
     *   <li>Son çare olarak geri dön</li>
     * </ol>
     */
    private void duvarTakipEt() {
        // Sağ el kuralı: önceliği sağ tarafa ver
        Yon sagYon = robot.getYon().sagaDon();
        Yon mevcutYon = robot.getYon();
        Yon solYon = robot.getYon().solaDon();
        Yon geriYon = robot.getYon().tersi();

        if (yoneHareketEdebilirMi(sagYon)) {
            // Sağa dönebiliyorsa duvardan uzaklaşmamak için sağa dön
            robot.setYon(sagYon);
            hareketiUygula(sagYon);
        } else if (yoneHareketEdebilirMi(mevcutYon)) {
            // Düz devam et
            hareketiUygula(mevcutYon);
        } else if (yoneHareketEdebilirMi(solYon)) {
            // Sola dön
            robot.setYon(solYon);
            hareketiUygula(solYon);
        } else if (yoneHareketEdebilirMi(geriYon)) {
            // Son çare: geri dön
            robot.setYon(geriYon);
            hareketiUygula(geriYon);
        }
        // Tamamen tıkanmış - hareket yok
    }

    /**
     * Robotun belirtilen yöne hareket edip edemeyeceğini kontrol eder.
     *
     * @param y Kontrol edilecek yön
     * @return O yöndeki hücre geçilebilirse true
     */
    private boolean yoneHareketEdebilirMi(Yon y) {
        int yeniX = robot.getX() + y.getDx();
        int yeniY = robot.getY() + y.getDy();
        return oda.gecilebilirMi(yeniX, yeniY);
    }

    /**
     * Robotu belirtilen yöne bir adım hareket ettirir.
     * Hareket sırasında batarya tüketilir ve toplam hareket sayısı artırılır.
     * Eğer hareket mümkün değilse (engel/sınır), robot sağa döner ama yer değiştirmez.
     *
     * @param y Hareket yönü
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
            // Çarpışma: hareket yok ama yön değişebilir
            robot.setYon(y.sagaDon());
        }
    }

    /**
     * Robotun mevcut konumundan hareket edebileceği tüm yönleri listeler.
     *
     * @return Geçilebilir yönlerin listesi
     */
    private List<Yon> kullanilabilirYonleriGetir() {
        List<Yon> yonler = new ArrayList<>();
        for (Yon y : Yon.values()) {
            if (yoneHareketEdebilirMi(y)) yonler.add(y);
        }
        return yonler;
    }

    // ==================== ÖZELLİKLER VE İSTATİSTİKLER ====================

    /**
     * Tüm gözlemlenebilir özellikleri günceller (View'da otomatik güncelleme tetiklenir).
     */
    private void ozellikleriGuncelle() {
        bataryaOzelligi.set(robot.getBatarya());
        konumOzelligi.set("(" + robot.getX() + ", " + robot.getY() + ")");
        yonOzelligi.set(robot.getYon().getGorunenAd());
        toplamAlanOzelligi.set(oda.getToplamZeminHucresi());
        istatistikleriGuncelle();
    }

    /**
     * Temizlenen ve kirli alan istatistiklerini günceller.
     */
    private void istatistikleriGuncelle() {
        temizlenenAlanOzelligi.set(oda.getTemizlenenHucreSayisi());
        kirliAlanOzelligi.set(oda.getKirliHucreSayisi());
    }

    /**
     * Geçen süreyi hesaplar ve "dd:ss" formatında günceller.
     * Duraklatma süreleri toplam süreden düşülür.
     */
    private void gecenSureyiGuncelle() {
        if (simulasyonBaslangicZamani == 0) return;
        long toplamMs = System.currentTimeMillis() - simulasyonBaslangicZamani - duraklatmaSuresiMs;
        long saniye = toplamMs / 1000;
        long dakika = saniye / 60;
        saniye = saniye % 60;
        gecenSureOzelligi.set(String.format("%02d:%02d", dakika, saniye));
    }

    // ==================== GETTER / SETTER METODLARI ====================

    /** @return Simülasyondaki oda nesnesi */
    public Oda getOda() { return oda; }

    /** @return Simülasyondaki robot nesnesi */
    public Robot getRobot() { return robot; }

    /** @return Simülasyon çalışıyor mu? */
    public boolean calisiyorMu() { return calisiyor; }

    /** @return Simülasyon duraklatıldı mı? */
    public boolean duraklatildiMi() { return duraklatildi; }

    /** @return Mevcut temizlik algoritması */
    public TemizlikAlgoritmasi getAlgoritma() { return algoritma; }

    /**
     * Aktif temizlik algoritmasını değiştirir.
     * Spiral algoritmasının iç durumu da sıfırlanır.
     *
     * @param algoritma Yeni temizlik algoritması
     */
    public void setAlgoritma(TemizlikAlgoritmasi algoritma) {
        this.algoritma = algoritma;
        // Spiral durumunu sıfırla (yeni algoritma seçildiğinde temiz başlangıç)
        spiralYaricap = 0; spiralAdim = 0;
        spiralBacaktakiAdimSayisi = 1; spiralBacakSayisi = 0;
        spiralYonu = Yon.DOGU;
    }

    /** @return Şu anda seçili olan kir türü */
    public KirTipi getSecilenKirTipi() { return secilenKirTipi; }

    /**
     * Kir ekleme modu için seçili kir türünü değiştirir.
     *
     * @param tip Yeni kir türü
     */
    public void setSecilenKirTipi(KirTipi tip) { this.secilenKirTipi = tip; }

    /** @return Simülasyon hız çarpanı (0.5 - 3.0) */
    public double getHizCarpani() { return hizCarpani; }

    /**
     * Simülasyon hızını değiştirir. 1.0 = normal hız.
     *
     * @param hizCarpani Yeni hız çarpanı
     */
    public void setHizCarpani(double hizCarpani) { this.hizCarpani = hizCarpani; }

    // Gözlemlenebilir özelliklere erişim sağlayan metodlar
    /** @return Bataryanın gözlemlenebilir özelliği */
    public DoubleProperty bataryaOzelligi() { return bataryaOzelligi; }
    /** @return Robotun konumunun gözlemlenebilir özelliği */
    public StringProperty konumOzelligi() { return konumOzelligi; }
    /** @return Robotun yönünün gözlemlenebilir özelliği */
    public StringProperty yonOzelligi() { return yonOzelligi; }
    /** @return Temizlenen alanın gözlemlenebilir özelliği */
    public IntegerProperty temizlenenAlanOzelligi() { return temizlenenAlanOzelligi; }
    /** @return Toplam alanın gözlemlenebilir özelliği */
    public IntegerProperty toplamAlanOzelligi() { return toplamAlanOzelligi; }
    /** @return Kirli alanın gözlemlenebilir özelliği */
    public IntegerProperty kirliAlanOzelligi() { return kirliAlanOzelligi; }
    /** @return Geçen sürenin gözlemlenebilir özelliği */
    public StringProperty gecenSureOzelligi() { return gecenSureOzelligi; }
    /** @return Simülasyon durumunun gözlemlenebilir özelliği */
    public StringProperty durumOzelligi() { return durumOzelligi; }
    /** @return Toplanan tozun gözlemlenebilir özelliği */
    public DoubleProperty toplananTozOzelligi() { return toplananTozOzelligi; }

    /**
     * Toplam başlangıç kir sayısını döndürür.
     * Yüzde hesabı için sıfıra bölünmeyi engellemek üzere en az 1 döner.
     *
     * @return Başlangıçtaki toplam kir sayısı (en az 1)
     */
    public int getToplamBaslangicKirSayisi() {
        return Math.max(1, baslangicKirliHucreSayisi > 0 ? baslangicKirliHucreSayisi : oda.getKirliHucreSayisi() + (int) toplamToplananToz);
    }
}
