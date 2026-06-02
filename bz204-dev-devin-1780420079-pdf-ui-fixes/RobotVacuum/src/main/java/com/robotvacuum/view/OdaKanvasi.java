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
    private static final Color RENK_ZEMIN = Color.rgb(245, 245, 240);
    private static final Color RENK_ZEMIN_KOYU = Color.rgb(238, 238, 230);
    private static final Color RENK_SARJ_ISTASYONU = Color.rgb(255, 200, 50);
    private static final Color RENK_IZGARA = Color.rgb(200, 200, 195);
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

    public OdaKanvasi(SimulasyonModeli model) {
        // İlk açılışta varsayılan boyutlarla oluşturuyoruz
        super(Oda.VARSAYILAN_SUTUN * 40, Oda.VARSAYILAN_SATIR * 40);
        this.model = model;

        // Mobilya görsellerini resources klasöründen yüklüyoruz
        kanepGorseli = gorselYukle("/com/robotvacuum/images/kanepe.png");
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

        // Oda alanının ana rengini çizelim
        gc.setFill(Color.rgb(230, 228, 220));
        gc.fillRect(0, 0, oda.getSutunSayisi() * hucreBoyutu, oda.getSatirSayisi() * hucreBoyutu);

        // Her bir hücreyi tipine göre çiz
        for (int x = 0; x < oda.getSutunSayisi(); x++) {
            for (int y = 0; y < oda.getSatirSayisi(); y++) {
                hucreyiCiz(gc, oda, x, y);
            }
        }
        
        // Sabit büyük boyutlu mobilya nesnelerini çiz
        mobilyalariCiz(gc, oda);
        
        // Yol geçmişi, koordinat çizgileri ve en son robotu çiziyoruz (üst üste binme sırası)
        yoluCiz(gc, robot);
        izgarayiCiz(gc, oda);
        koordinatlariYaz(gc, oda);
        robotuCiz(gc, robot);
        
        gc.restore();
    }

    /**
     * Tek bir hücreyi çizer (boş zemin, engel veya şarj istasyonu)
     */
    private void hucreyiCiz(GraphicsContext gc, Oda oda, int x, int y) {
        Hucre hucre = oda.getHucre(x, y);
        double px = x * hucreBoyutu;
        double py = y * hucreBoyutu;

        switch (hucre.getTip()) {
            case ENGEL -> {
                // Sadece eski tip veya arayüzde mobilya olmayan engeller için fallback (Örn: mutfak tezgahı)
                // Yeni nesil büyük mobilyalar mobilyalariCiz() metodunda çizilecek.
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
                    gc.setFill(Color.rgb(101, 67, 33));
                    gc.fillRect(px + 1, py + 1, hucreBoyutu - 2, hucreBoyutu - 2);
                }
            }
            case SARJ_ISTASYONU -> {
                // Şarj istasyonu sarı renkte ve ortasında şimşek simgesi var
                gc.setFill(RENK_SARJ_ISTASYONU);
                gc.fillRect(px, py, hucreBoyutu, hucreBoyutu);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", 18));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("⚡", px + hucreBoyutu / 2.0, py + hucreBoyutu / 2.0 + 6);
            }
            default -> {
                // Dama tahtası deseni oluşturmak için tek-çift kontrolüyle zemin rengini seçiyoruz
                gc.setFill((x + y) % 2 == 0 ? RENK_ZEMIN : RENK_ZEMIN_KOYU);
                gc.fillRect(px, py, hucreBoyutu, hucreBoyutu);

                // Hücre temizlenmişse mavi yarı saydam bir katman atıyoruz
                if (hucre.temizlendiMi() && !hucre.kirliMi()) {
                    gc.setFill(RENK_TEMIZLENMIS);
                    gc.fillRect(px, py, hucreBoyutu, hucreBoyutu);
                }

                // Üzerinde kir varsa kir görselini çizelim
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

            if (kanepGorseli != null) {
                gc.save();
                gc.translate(px + g/2, py + h/2);
                
                // Rotations based on orientation.
                // Assuming kanepe.png image faces North by default.
                switch (m.getYon()) {
                    case KUZEY:
                        // No rotation, but we need to ensure the image uses proper w/h
                        gc.drawImage(kanepGorseli, -g/2, -h/2, g, h);
                        break;
                    case GUNEY:
                        gc.rotate(180);
                        gc.drawImage(kanepGorseli, -g/2, -h/2, g, h);
                        break;
                    case DOGU:
                        gc.rotate(90);
                        // Swap dimensions for image drawing when rotated 90/270 because width/height 
                        // of the canvas transform takes over. Wait, if we rotate 90, the width becomes height
                        gc.drawImage(kanepGorseli, -h/2, -g/2, h, g);
                        break;
                    case BATI:
                        gc.rotate(-90);
                        gc.drawImage(kanepGorseli, -h/2, -g/2, h, g);
                        break;
                }
                gc.restore();
            }
        }
    }

    /**
     * Kirlere özel çizimler ekler (toz noktacıkları, sıvı damlası veya leke)
     */
    private void kiriCiz(GraphicsContext gc, Hucre hucre, double px, double py) {
        KirTipi tip = hucre.getKirTipi();
        double cx = px + hucreBoyutu / 2.0;
        double cy = py + hucreBoyutu / 2.0;

        switch (tip) {
            case TOZ -> {
                gc.setFill(RENK_TOZ);
                double[] dotsX = {cx - 8, cx, cx + 8, cx - 5, cx + 5};
                double[] dotsY = {cy - 5, cy - 8, cy - 5, cy + 5, cy + 5};
                for (int i = 0; i < dotsX.length; i++) {
                    gc.fillOval(dotsX[i] - 2.5, dotsY[i] - 2.5, 5, 5);
                }
            }
            case SIVI -> {
                gc.setFill(RENK_SIVI);
                gc.fillOval(cx - 10, cy - 6, 20, 12);
                gc.fillOval(cx - 5, cy - 10, 10, 20);
            }
            case LEKE -> {
                gc.setFill(RENK_LEKE);
                gc.fillOval(cx - 11, cy - 8, 22, 16);
            }
        }

        // Sıvı ve lekelerin üzerinde kalan temizlik adımını sayı olarak yazıyoruz (Toz tek adım olduğu için gerek yok)
        int kalanAdim = hucre.getKalanTemizlikAdimi();
        if (kalanAdim > 0 && tip != KirTipi.TOZ) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial Bold", 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(kalanAdim), cx, cy + 4);
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

        // Robot o an temizlik yapıyorsa etrafında dönen tatlı bir fırça animasyonu
        if (model.temizlikYapiliyorMu()) {
            long zaman = System.currentTimeMillis();
            double aci = (zaman % 1000) / 1000.0 * 360.0;
            gc.save();
            gc.translate(cx, cy);
            gc.rotate(aci);
            gc.setStroke(Color.rgb(150, 200, 255, 0.7));
            gc.setLineWidth(3);
            // Dış fırça halkaları
            for (int i = 0; i < 4; i++) {
                gc.strokeArc(-r * 1.3, -r * 1.3, r * 2.6, r * 2.6, i * 90 + 10, 70, javafx.scene.shape.ArcType.OPEN);
            }
            // Fırça uçlarındaki toz noktacıkları
            gc.setFill(Color.rgb(200, 200, 200, 0.9));
            for (int i = 0; i < 3; i++) {
                double radyan = Math.toRadians((aci + i * 120) % 360);
                double px = Math.cos(radyan) * (r * 1.4);
                double py = Math.sin(radyan) * (r * 1.4);
                gc.fillOval(px - 2, py - 2, 4, 4);
            }
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
