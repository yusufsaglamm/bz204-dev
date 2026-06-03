package com.robotvacuum.view;

import com.robotvacuum.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Robot süpürgenin ve odanın 2 boyutlu haritasını ekrana çizen sınıf.
 * JavaFX Canvas bileşeninden türemiştir. Hücre boyutlarını dinamik olarak pencere boyutuna göre ayarlar,
 * böylece görseller tam olarak ekrana sığar.
 */
public class OdaKanvasi extends Canvas {

    private double hucreBoyutu = 40;
    
    // Çizim renk paletimiz
    private static final Color RENK_SARJ_ISTASYONU = Color.rgb(255, 200, 50);
    private static final Color RENK_IZGARA = Color.rgb(200, 200, 195, 0.4);
    private static final Color RENK_TEMIZLENMIS = Color.rgb(200, 230, 255, 0.45);
    private static final Color RENK_YOL_IZI = Color.rgb(100, 160, 220, 0.55);
    private static final Color RENK_TOZ = Color.rgb(180, 150, 100);
    private static final Color RENK_SIVI = Color.rgb(80, 140, 220);
    private static final Color RENK_LEKE = Color.rgb(160, 60, 60);
    private static final Color RENK_ROBOT = Color.rgb(40, 40, 50);
    private static final Color RENK_ROBOT_ISIK = Color.rgb(80, 180, 255);

    private final SimulasyonModeli model;

    // Kanvasın içinde odanın ortalanması için kullanılan kaydırma (offset) değerleri
    private double kaymaX = 0;
    private double kaymaY = 0;

    // Üstten görünüm mobilya görselleri
    private final Image kanepGorseli;
    private final Image tvUnitesiGorseli;
    private final Image sehpaGorseli;
    private final Image yemekMasasiGorseli;
    private final Image dolapGorseli;
    private final Image komodinGorseli;
    private final Image tezgahGorseli;
    private final Image yatakGorseli;

    public OdaKanvasi(SimulasyonModeli model) {
        // İlk açılışta varsayılan boyutlarla oluşturuyoruz
        super(Oda.VARSAYILAN_SUTUN * 40, Oda.VARSAYILAN_SATIR * 40);
        this.model = model;

        // Mobilya görsellerini resources klasöründen yüklüyoruz
        kanepGorseli = gorselYukle("/com/robotvacuum/images/kanepe.png");
        tvUnitesiGorseli = gorselYukle("/com/robotvacuum/images/tv_unitesi.png");
        sehpaGorseli = gorselYukle("/com/robotvacuum/images/sehpa.png");
        yemekMasasiGorseli = gorselYukle("/com/robotvacuum/images/yemek_masasi.png");
        dolapGorseli = gorselYukle("/com/robotvacuum/images/dolap.png");
        komodinGorseli = gorselYukle("/com/robotvacuum/images/komodin.png");
        tezgahGorseli = gorselYukle("/com/robotvacuum/images/tezgah.png");
        yatakGorseli = gorselYukle("/com/robotvacuum/images/yatak.png");

        // Kir ve Efekt görselleri
        tozGorseli = gorselYukle("/com/robotvacuum/images/toz.png");
        siviGorseli = gorselYukle("/com/robotvacuum/images/sivi.png");
        lekeGorseli = gorselYukle("/com/robotvacuum/images/leke.png");
        temizlemeEfektiGorseli = gorselYukle("/com/robotvacuum/images/temizleme_efekti.png");
    }

    private Image tozGorseli, siviGorseli, lekeGorseli, temizlemeEfektiGorseli;

    // Fare silüeti (hover) için saklanan koordinatlar
    private int hoverSutun = -1;
    private int hoverSatir = -1;
    private String aktifMod = null;
    private MobilyaTipi seciliTip = null;
    private Yon seciliYon = null;

    /**
     * Farenin kanvas üzerindeki anlık pozisyonunu ayarlar
     */
    public void setHoverPozisyonu(int sutun, int satir) {
        this.hoverSutun = sutun;
        this.hoverSatir = satir;
    }

    public void setSiluetVerisi(String mod, MobilyaTipi tip, Yon yon) {
        this.aktifMod = mod;
        this.seciliTip = tip;
        this.seciliYon = yon;
    }

    /**
     * Resources klasöründen görsel dosyasını yükler.
     */
    private Image gorselYukle(String yol) {
        var kaynak = getClass().getResourceAsStream(yol);
        if (kaynak != null) {
            return new Image(kaynak);
        }
        return null;
    }

    /**
     * İlgili mobilya tipine ait görseli döndürür (Arayüzde önizleme için).
     */
    public Image getMobilyaGorseli(MobilyaTipi tip) {
        if (tip == null) return null;
        return switch (tip) {
            case KANEPE -> kanepGorseli;
            case TV_UNITESI -> tvUnitesiGorseli;
            case SEHPA -> sehpaGorseli;
            case YEMEK_MASASI -> yemekMasasiGorseli;
            case DOLAP -> dolapGorseli;
            case KOMODIN -> komodinGorseli;
            case TEZGAH -> tezgahGorseli;
            case YATAK -> yatakGorseli;
        };
    }

    /**
     * Tüm ekranı temizler ve odanın son halini sıfırdan çizer.
     */
    public void yenidenCiz() {
        double genislik = getWidth();
        double yukseklik = getHeight();
        if (genislik <= 0 || yukseklik <= 0) return;

        GraphicsContext gc = getGraphicsContext2D();
        Oda oda = model.getOda();
        Robot robot = model.getRobot();

        // Kanvas kenarlarından biraz boşluk bırakalım ki çok dip dibe durmasın
        double bosluk = 20.0;
        double kullanilabilirGenislik = genislik - (bosluk * 2);
        double kullanilabilirYukseklik = yukseklik - (bosluk * 2);
        
        // Hücre boyutunu pencereye sığacak şekilde dinamik hesapla
        this.hucreBoyutu = Math.max(10, Math.min(kullanilabilirGenislik / oda.getSutunSayisi(), kullanilabilirYukseklik / oda.getSatirSayisi()));
        
        // Odayı tam ortalamak için gereken X ve Y kayma değerleri
        this.kaymaX = (genislik - (oda.getSutunSayisi() * hucreBoyutu)) / 2.0;
        this.kaymaY = (yukseklik - (oda.getSatirSayisi() * hucreBoyutu)) / 2.0;

        // Arka planı koyu lacivert yapalım, modern dursun
        gc.setFill(Color.rgb(15, 18, 35));
        gc.fillRect(0, 0, genislik, yukseklik);

        gc.save();
        gc.translate(kaymaX, kaymaY);

        double odaGenislik = oda.getSutunSayisi() * hucreBoyutu;
        double odaYukseklik = oda.getSatirSayisi() * hucreBoyutu;

        // Oda tipine göre zemin rengini belirle
        OdaTipi odaTipi = model.getAktifOdaTipi();
        Color zeminAcik, zeminKoyu, engelRenk;
        String odaEmoji, odaAdi;
        switch (odaTipi) {
            case MUTFAK -> {
                zeminAcik = Color.rgb(235, 238, 242); // Açık gri karo
                zeminKoyu = Color.rgb(220, 225, 232);
                engelRenk = Color.rgb(90, 95, 105);   // Metal/gri tezgah
                odaEmoji = "🍳"; odaAdi = "Mutfak";
            }
            case YATAK_ODASI -> {
                zeminAcik = Color.rgb(225, 232, 242); // Yumuşak mavi halı
                zeminKoyu = Color.rgb(215, 222, 235);
                engelRenk = Color.rgb(120, 90, 65);   // Koyu ahşap dolap
                odaEmoji = "🛏️"; odaAdi = "Yatak Odası";
            }
            default -> { // SALON
                zeminAcik = Color.rgb(235, 220, 200); // Sıcak bej parke
                zeminKoyu = Color.rgb(225, 210, 190);
                engelRenk = Color.rgb(101, 67, 33);   // Ahşap mobilya
                odaEmoji = "🏠"; odaAdi = "Salon";
            }
        }

        // Oda alanının ana rengini çizelim
        gc.setFill(zeminAcik);
        gc.fillRect(0, 0, odaGenislik, odaYukseklik);

        // Her bir hücreyi tipine göre çiz
        for (int x = 0; x < oda.getSutunSayisi(); x++) {
            for (int y = 0; y < oda.getSatirSayisi(); y++) {
                hucreyiCiz(gc, oda, x, y, zeminAcik, zeminKoyu, engelRenk, odaTipi);
            }
        }
        
        // Sabit büyük boyutlu mobilya nesnelerini çiz
        mobilyalariCiz(gc, oda);
        
        // Yol geçmişi, koordinat çizgileri ve en son robotu çiziyoruz
        yoluCiz(gc, robot);
        izgarayiCiz(gc, oda);
        
        // Kalın duvar bordürü (oda sınırları)
        duvarlariCiz(gc, oda);
        
        koordinatlariYaz(gc, oda);
        robotuCiz(gc, robot);
        
        // Farenin bulunduğu hücrede eğer mobilya ekleme modundaysak silüet çizimi yap
        siluetCiz(gc, oda);
        
        // Oda adını sol üst köşeye yazdır
        odaAdiniYaz(gc, odaEmoji, odaAdi);
        
        gc.restore();
    }

    /**
     * Odanın kenarlarına kalın duvar çizgileri çizer.
     */
    private void duvarlariCiz(GraphicsContext gc, Oda oda) {
        double odaGenislik = oda.getSutunSayisi() * hucreBoyutu;
        double odaYukseklik = oda.getSatirSayisi() * hucreBoyutu;
        double kalinlik = 4.0;
        
        gc.setStroke(Color.rgb(60, 50, 40));
        gc.setLineWidth(kalinlik);
        gc.strokeRect(kalinlik / 2, kalinlik / 2, odaGenislik - kalinlik, odaYukseklik - kalinlik);
        
        // İç gölge efekti (duvarların derinlik hissi vermesi için)
        gc.setStroke(Color.rgb(40, 35, 28, 0.3));
        gc.setLineWidth(1.5);
        gc.strokeRect(kalinlik + 1, kalinlik + 1, odaGenislik - kalinlik * 2 - 2, odaYukseklik - kalinlik * 2 - 2);
    }

    /**
     * Oda adını ve emojisini kanvasın sol üst köşesine yazar.
     */
    private void odaAdiniYaz(GraphicsContext gc, String emoji, String ad) {
        gc.setFill(Color.rgb(0, 0, 0, 0.45));
        gc.fillRoundRect(8, 8, 110, 28, 8, 8);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 13));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(emoji + " " + ad, 16, 27);
    }

    /**
     * Tek bir hücreyi çizer (boş zemin, engel veya şarj istasyonu)
     */
    private void hucreyiCiz(GraphicsContext gc, Oda oda, int x, int y,
                             Color zeminAcik, Color zeminKoyu, Color engelRenk, OdaTipi odaTipi) {
        Hucre hucre = oda.getHucre(x, y);
        double px = x * hucreBoyutu;
        double py = y * hucreBoyutu;

        switch (hucre.getTip()) {
            case ENGEL -> {
                boolean mobilyaMi = false;
                for (com.robotvacuum.model.Mobilya m : oda.getMobilyalar()) {
                    for (int[] p : m.getKaplananHucreler()) {
                        if (p[0] == x && p[1] == y) {
                            mobilyaMi = true; break;
                        }
                    }
                    if (mobilyaMi) break;
                }
                
                if (!mobilyaMi) {
                    // Dekoratif engeller odaya özel renkte
                    gc.setFill(engelRenk);
                    gc.fillRoundRect(px + 1, py + 1, hucreBoyutu - 2, hucreBoyutu - 2, 4, 4);
                    // Hafif parlaklık efekti
                    gc.setFill(Color.rgb(255, 255, 255, 0.12));
                    gc.fillRect(px + 2, py + 2, hucreBoyutu - 4, (hucreBoyutu - 4) * 0.35);
                }
            }
            case SARJ_ISTASYONU -> {
                gc.setFill(RENK_SARJ_ISTASYONU);
                gc.fillRect(px, py, hucreBoyutu, hucreBoyutu);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", 18));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("⚡", px + hucreBoyutu / 2.0, py + hucreBoyutu / 2.0 + 6);
            }
            default -> {
                // Oda tipine göre zemin deseni
                if (odaTipi == OdaTipi.MUTFAK) {
                    // Karo fayans deseni
                    gc.setFill((x + y) % 2 == 0 ? zeminAcik : zeminKoyu);
                    gc.fillRect(px, py, hucreBoyutu, hucreBoyutu);
                    // Fayans derz çizgileri
                    gc.setStroke(Color.rgb(200, 205, 215, 0.6));
                    gc.setLineWidth(0.5);
                    gc.strokeRect(px + 0.5, py + 0.5, hucreBoyutu - 1, hucreBoyutu - 1);
                } else if (odaTipi == OdaTipi.YATAK_ODASI) {
                    // Halı deseni (daha yumuşak geçişler)
                    gc.setFill((x + y) % 2 == 0 ? zeminAcik : zeminKoyu);
                    gc.fillRect(px, py, hucreBoyutu, hucreBoyutu);
                } else {
                    // Parke deseni (salon)
                    boolean parkeYonu = (y % 2 == 0);
                    if (parkeYonu) {
                        gc.setFill(x % 2 == 0 ? zeminAcik : zeminKoyu);
                    } else {
                        gc.setFill(x % 2 == 0 ? zeminKoyu : zeminAcik);
                    }
                    gc.fillRect(px, py, hucreBoyutu, hucreBoyutu);
                    // Parke çizgi
                    gc.setStroke(Color.rgb(190, 175, 155, 0.3));
                    gc.setLineWidth(0.3);
                    gc.strokeLine(px, py + hucreBoyutu, px + hucreBoyutu, py + hucreBoyutu);
                }

                if (hucre.temizlendiMi() && !hucre.kirliMi()) {
                    gc.setFill(RENK_TEMIZLENMIS);
                    gc.fillRect(px, py, hucreBoyutu, hucreBoyutu);
                }

                if (hucre.kirliMi()) kiriCiz(gc, hucre, px, py);
            }
        }
    }

    private void mobilyalariCiz(GraphicsContext gc, Oda oda) {
        for (com.robotvacuum.model.Mobilya m : oda.getMobilyalar()) {
            double px = m.getStartX() * hucreBoyutu;
            double py = m.getStartY() * hucreBoyutu;
            double g = (m.getGenislik() * hucreBoyutu) - 2;
            double h = (m.getYukseklik() * hucreBoyutu) - 2;
            px += 1;
            py += 1;

            Image cizilecekGorsel = switch (m.getTip()) {
                case KANEPE -> kanepGorseli;
                case TV_UNITESI -> tvUnitesiGorseli;
                case SEHPA -> sehpaGorseli;
                case YEMEK_MASASI -> yemekMasasiGorseli;
                case DOLAP -> dolapGorseli;
                case KOMODIN -> komodinGorseli;
                case TEZGAH -> tezgahGorseli;
                case YATAK -> yatakGorseli;
            };

            if (cizilecekGorsel != null) {
                gc.save();
                gc.translate(px + g/2, py + h/2);
                
                // Rotations based on orientation.
                switch (m.getYon()) {
                    case KUZEY:
                        gc.drawImage(cizilecekGorsel, -g/2, -h/2, g, h);
                        break;
                    case GUNEY:
                        gc.rotate(180);
                        gc.drawImage(cizilecekGorsel, -g/2, -h/2, g, h);
                        break;
                    case DOGU:
                        gc.rotate(90);
                        gc.drawImage(cizilecekGorsel, -h/2, -g/2, h, g);
                        break;
                    case BATI:
                        gc.rotate(-90);
                        gc.drawImage(cizilecekGorsel, -h/2, -g/2, h, g);
                        break;
                }
                gc.restore();
            }
        }
    }

    private void siluetCiz(GraphicsContext gc, Oda oda) {
        if ("engel_ekle".equals(aktifMod) && seciliTip != null && seciliYon != null && hoverSutun >= 0 && hoverSatir >= 0) {
            com.robotvacuum.model.Mobilya sanalMobilya = new com.robotvacuum.model.Mobilya(seciliTip, hoverSutun, hoverSatir, seciliYon);
            boolean sigiyorMu = oda.mobilyaEklenebilirMi(sanalMobilya);

            // Mavi (Uygun) veya Kırmızı (Sığmıyor) yarı şeffaf renk belirle
            Color siluetRengi = sigiyorMu ? Color.rgb(0, 150, 255, 0.4) : Color.rgb(255, 0, 0, 0.4);

            double px = hoverSutun * hucreBoyutu + 1;
            double py = hoverSatir * hucreBoyutu + 1;
            double g = (sanalMobilya.getGenislik() * hucreBoyutu) - 2;
            double h = (sanalMobilya.getYukseklik() * hucreBoyutu) - 2;

            gc.save();
            gc.translate(px + g/2, py + h/2);

            // Resim yerine doğrudan renkli bir silüet blok çizeceğiz
            // Rotations
            switch (seciliYon) {
                case KUZEY:
                    break;
                case GUNEY:
                    gc.rotate(180);
                    break;
                case DOGU:
                    gc.rotate(90);
                    // swap dimensions for drawing since we rotated
                    double tmp = g; g = h; h = tmp;
                    break;
                case BATI:
                    gc.rotate(-90);
                    double tmp2 = g; g = h; h = tmp2;
                    break;
            }

            gc.setFill(siluetRengi);
            gc.fillRoundRect(-g/2, -h/2, g, h, 8, 8);
            
            // Çizgili kenarlık
            gc.setStroke(siluetRengi.deriveColor(0, 1.0, 1.0, 0.8));
            gc.setLineWidth(2);
            gc.setLineDashes(5);
            gc.strokeRoundRect(-g/2, -h/2, g, h, 8, 8);

            gc.restore();
        }
    }

    /**
     * Kirlere özel PNG görsellerini çizer (sayılar kaldırıldı)
     */
    private void kiriCiz(GraphicsContext gc, Hucre hucre, double px, double py) {
        KirTipi tip = hucre.getKirTipi();
        double cx = px + hucreBoyutu / 2.0;
        double cy = py + hucreBoyutu / 2.0;

        Image kirGorseli = switch (tip) {
            case TOZ -> tozGorseli;
            case SIVI -> siviGorseli;
            case LEKE -> lekeGorseli;
        };

        if (kirGorseli != null) {
            double boyut = hucreBoyutu * 0.8; // Hücreden biraz küçük olsun
            gc.drawImage(kirGorseli, cx - boyut/2, cy - boyut/2, boyut, boyut);
        }
    }

    /**
     * Robotun odada geçtiği yerleri kesikli mavi bir çizgiyle birbirine bağlar.
     */
    private void yoluCiz(GraphicsContext gc, Robot robot) {
        List<int[]> gecmis = robot.getYolGecmisi();
        if (gecmis.size() < 2) return;
        gc.setStroke(RENK_YOL_IZI);
        gc.setLineWidth(2.5);
        gc.setLineDashes(4, 4); // Kesikli çizgi efekti
        double yariBoyut = hucreBoyutu / 2.0;
        for (int i = 1; i < gecmis.size(); i++) {
            int[] onceki = gecmis.get(i - 1);
            int[] simdiki = gecmis.get(i);
            gc.strokeLine(
                    onceki[0] * hucreBoyutu + yariBoyut, 
                    onceki[1] * hucreBoyutu + yariBoyut, 
                    simdiki[0] * hucreBoyutu + yariBoyut, 
                    simdiki[1] * hucreBoyutu + yariBoyut
            );
        }
        gc.setLineDashes(null); // Çizgi biçimini sıfırla ki diğer yerler etkilenmesin
    }

    /**
     * Hücrelerin sınırlarını çizen ince ızgara çizgileri.
     */
    private void izgarayiCiz(GraphicsContext gc, Oda oda) {
        gc.setStroke(RENK_IZGARA);
        gc.setLineWidth(0.5);
        double toplamGenislik = oda.getSutunSayisi() * hucreBoyutu;
        double toplamYukseklik = oda.getSatirSayisi() * hucreBoyutu;
        for (int x = 0; x <= oda.getSutunSayisi(); x++) {
            gc.strokeLine(x * hucreBoyutu, 0, x * hucreBoyutu, toplamYukseklik);
        }
        for (int y = 0; y <= oda.getSatirSayisi(); y++) {
            gc.strokeLine(0, y * hucreBoyutu, toplamGenislik, y * hucreBoyutu);
        }
    }

    /**
     * Koordinatların daha rahat anlaşılması için sol ve üst kenara sayıları yazarız.
     */
    private void koordinatlariYaz(GraphicsContext gc, Oda oda) {
        gc.setFill(Color.rgb(150, 150, 150));
        gc.setFont(Font.font("Monospace", 9));
        gc.setTextAlign(TextAlignment.CENTER);
        for (int x = 0; x < oda.getSutunSayisi(); x++) {
            gc.fillText(String.valueOf(x), x * hucreBoyutu + hucreBoyutu / 2.0, 10);
        }
        for (int y = 0; y < oda.getSatirSayisi(); y++) {
            gc.fillText(String.valueOf(y), 10, y * hucreBoyutu + hucreBoyutu / 2.0 + 4);
        }
    }

    /**
     * Robotun dairesel gövdesini, baktığı yön çizgisini, şarj durumunu
     * ve etrafındaki batarya halkasını çizer.
     */
    private void robotuCiz(GraphicsContext gc, Robot robot) {
        double cx = robot.getX() * hucreBoyutu + hucreBoyutu / 2.0;
        double cy = robot.getY() * hucreBoyutu + hucreBoyutu / 2.0;
        double r = hucreBoyutu * 0.38;

        // Robot o an temizlik yapıyorsa oluşturduğumuz efekti çizelim
        if (model.isTemizlikDevamEdiyor() && temizlemeEfektiGorseli != null) {
            long zaman = System.currentTimeMillis();
            double aci = (zaman % 1000) / 1000.0 * 360.0;
            gc.save();
            gc.translate(cx, cy);
            gc.rotate(aci);
            
            double efektBoyutu = hucreBoyutu * 1.6;
            gc.drawImage(temizlemeEfektiGorseli, -efektBoyutu/2, -efektBoyutu/2, efektBoyutu, efektBoyutu);
            gc.restore();
        }

        // Robotun altına hafif bir gölge atalım
        gc.setFill(Color.rgb(0, 0, 0, 0.2));
        gc.fillOval(cx - r + 2, cy - r + 2, r * 2, r * 2);

        // Robot gövdesi
        gc.setFill(RENK_ROBOT);
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);
        
        // Robot üzerindeki şık küçük ışık detayı
        gc.setFill(RENK_ROBOT_ISIK);
        gc.fillOval(cx - r + 3, cy - r + 3, r * 0.8, r * 0.8);

        // Robotun baktığı yönü gösteren beyaz burun çizgisi
        double yonUzunlugu = r * 0.75;
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeLine(cx, cy, cx + robot.getYon().getDx() * yonUzunlugu, cy + robot.getYon().getDy() * yonUzunlugu);

        // Robotun dışındaki batarya halkası (yüzdeye göre uzunluğu değişir)
        double pilYuzdesi = robot.getBatarya() / Robot.MAKS_BATARYA;
        // Pil durumuna göre renk değişimi (Yeşil, Turuncu, Kırmızı)
        gc.setStroke(pilYuzdesi > 0.5 ? Color.rgb(50, 200, 100) : pilYuzdesi > 0.2 ? Color.rgb(255, 180, 50) : Color.rgb(220, 60, 60));
        gc.strokeArc(cx - r - 3, cy - r - 3, (r + 3) * 2, (r + 3) * 2, 90, -(pilYuzdesi * 360), javafx.scene.shape.ArcType.OPEN);

        // Şarj oluyorsa kafasında şimşek işareti çıksın
        if (robot.sarjOluyorMu()) {
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Arial", 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("⚡", cx, cy - r - 5);
        }
    }

    // Tıklanan pikselin hangi ızgara hücresine (satır/sütun) denk geldiğini çözen metotlar
    public int pikseldenSutuna(double px) { return (int) ((px - kaymaX) / hucreBoyutu); }
    public int pikseldenSatira(double py) { return (int) ((py - kaymaY) / hucreBoyutu); }
}
