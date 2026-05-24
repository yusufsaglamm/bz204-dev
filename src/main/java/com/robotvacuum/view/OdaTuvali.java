package com.robotvacuum.view;

import com.robotvacuum.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.List;

/**
 * Oda ızgarasını, robotu, kirleri, engelleri ve yol geçmişini çizmeye yarayan
 * JavaFX Canvas bileşeni.
 *
 * <p>Bu sınıf simülasyon modelinden veriyi okur ve görsel olarak temsil eder.
 * Her tick'te kontrolcü tarafından {@link #yenidenCiz()} çağrılarak yenilenir.</p>
 */
public class OdaTuvali extends Canvas {

    /** Her hücrenin piksel cinsinden kenar uzunluğu */
    private static final int HUCRE_BOYUTU = 40;

    /** Zemin için ana renk */
    private static final Color RENK_ZEMIN = Color.rgb(245, 245, 240);
    /** Zemin için alternatif renk (dama deseni) */
    private static final Color RENK_ZEMIN_ALT = Color.rgb(238, 238, 230);
    /** Engel (mobilya) rengi */
    private static final Color RENK_ENGEL = Color.rgb(80, 80, 90);
    /** Şarj istasyonu rengi */
    private static final Color RENK_SARJ = Color.rgb(255, 200, 50);
    /** Izgara çizgileri rengi */
    private static final Color RENK_IZGARA = Color.rgb(200, 200, 195);
    /** Temizlenmiş alan üzerindeki yarı saydam mavi katman */
    private static final Color RENK_TEMIZ = Color.rgb(200, 230, 255, 0.45);
    /** Robotun yol çizgisi rengi */
    private static final Color RENK_YOL = Color.rgb(100, 160, 220, 0.55);
    /** Toz rengi */
    private static final Color RENK_TOZ = Color.rgb(180, 150, 100);
    /** Sıvı kir rengi */
    private static final Color RENK_SIVI = Color.rgb(80, 140, 220);
    /** Leke rengi */
    private static final Color RENK_LEKE = Color.rgb(160, 60, 60);
    /** Robot gövde rengi */
    private static final Color RENK_ROBOT = Color.rgb(40, 40, 50);
    /** Robotun üst kısmındaki vurgu rengi */
    private static final Color RENK_ROBOT_VURGU = Color.rgb(80, 180, 255);

    /** Bu canvas'ın bağlı olduğu simülasyon modeli */
    private final SimulasyonModeli model;
    /** Izgara çizgileri görünür mü? */
    private boolean izgaraGoster = true;
    /** Robotun yolu görünür mü? */
    private boolean yolGoster = true;

    /**
     * Yeni bir oda tuvali oluşturur. Boyutu oda hücre sayısına göre belirlenir.
     *
     * @param model Görselleştirilecek simülasyon modeli
     */
    public OdaTuvali(SimulasyonModeli model) {
        super(
            Oda.VARSAYILAN_SUTUN * HUCRE_BOYUTU,
            Oda.VARSAYILAN_SATIR * HUCRE_BOYUTU
        );
        this.model = model;
    }

    /**
     * Tuvali tamamen yeniden çizer.
     * Önce arka plan, sonra hücreler, yol, ızgara, koordinatlar ve son olarak
     * robot çizilir.
     */
    public void yenidenCiz() {
        GraphicsContext gc = getGraphicsContext2D();
        Oda oda = model.getOda();
        Robot robot = model.getRobot();

        int genislik = (int) getWidth();
        int yukseklik = (int) getHeight();

        // Arka plan
        gc.setFill(Color.rgb(230, 228, 220));
        gc.fillRect(0, 0, genislik, yukseklik);

        // Hücreleri çiz
        for (int x = 0; x < oda.getSutunSayisi(); x++) {
            for (int y = 0; y < oda.getSatirSayisi(); y++) {
                hucreyiCiz(gc, oda, x, y);
            }
        }

        // Robotun yol geçmişini çiz
        if (yolGoster) {
            yoluCiz(gc, robot);
        }

        // Izgara çizgilerini çiz
        if (izgaraGoster) {
            izgarayiCiz(gc, oda);
        }

        // Satır/sütun koordinat etiketlerini çiz
        koordinatlariCiz(gc, oda);

        // Robotu çiz (en üstte görünmesi için en son)
        robotuCiz(gc, robot);
    }

    /**
     * Belirtilen koordinattaki bir hücreyi tipine göre çizer.
     */
    private void hucreyiCiz(GraphicsContext gc, Oda oda, int x, int y) {
        Hucre hucre = oda.getHucre(x, y);
        double px = x * HUCRE_BOYUTU;
        double py = y * HUCRE_BOYUTU;

        switch (hucre.getTip()) {
            case ENGEL -> {
                // Mobilya görünümü
                gc.setFill(RENK_ENGEL);
                gc.fillRect(px, py, HUCRE_BOYUTU, HUCRE_BOYUTU);
                // Mobilya doku çizgileri
                gc.setStroke(Color.rgb(60, 60, 70));
                gc.setLineWidth(0.5);
                gc.strokeLine(px + 4, py + 4, px + HUCRE_BOYUTU - 4, py + 4);
                gc.strokeLine(px + 4, py + HUCRE_BOYUTU - 4, px + HUCRE_BOYUTU - 4, py + HUCRE_BOYUTU - 4);
            }
            case SARJ_ISTASYONU -> {
                // Şarj istasyonu görünümü
                gc.setFill(RENK_SARJ);
                gc.fillRect(px, py, HUCRE_BOYUTU, HUCRE_BOYUTU);
                // Yıldırım sembolü
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", 18));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("⚡", px + HUCRE_BOYUTU / 2.0, py + HUCRE_BOYUTU / 2.0 + 6);
            }
            default -> {
                // Dama desenli zemin
                Color zeminRengi = (x + y) % 2 == 0 ? RENK_ZEMIN : RENK_ZEMIN_ALT;
                gc.setFill(zeminRengi);
                gc.fillRect(px, py, HUCRE_BOYUTU, HUCRE_BOYUTU);

                // Temizlenmiş alan göstergesi (saydam mavi katman)
                if (hucre.temizMi() && !hucre.kirVarMi()) {
                    gc.setFill(RENK_TEMIZ);
                    gc.fillRect(px, py, HUCRE_BOYUTU, HUCRE_BOYUTU);
                }

                // Kir varsa kir görselini çiz
                if (hucre.kirVarMi()) {
                    kiriCiz(gc, hucre, px, py);
                }
            }
        }
    }

    /**
     * Bir hücredeki kiri türüne göre çizer (toz noktaları, sıvı, leke).
     */
    private void kiriCiz(GraphicsContext gc, Hucre hucre, double px, double py) {
        KirTipi tip = hucre.getKirTipi();
        double cx = px + HUCRE_BOYUTU / 2.0;
        double cy = py + HUCRE_BOYUTU / 2.0;

        switch (tip) {
            case TOZ -> {
                gc.setFill(RENK_TOZ);
                // Tozu temsil etmek için birkaç küçük nokta çiz
                double[] noktalarX = {cx - 8, cx, cx + 8, cx - 5, cx + 5};
                double[] noktalarY = {cy - 5, cy - 8, cy - 5, cy + 5, cy + 5};
                for (int i = 0; i < noktalarX.length; i++) {
                    gc.fillOval(noktalarX[i] - 2.5, noktalarY[i] - 2.5, 5, 5);
                }
            }
            case SIVI -> {
                gc.setFill(RENK_SIVI);
                // Düzensiz sıçrama şekli
                gc.fillOval(cx - 10, cy - 6, 20, 12);
                gc.fillOval(cx - 5, cy - 10, 10, 20);
                gc.setFill(Color.rgb(150, 200, 255, 0.6));
                gc.fillOval(cx - 3, cy - 3, 6, 6);
            }
            case LEKE -> {
                gc.setFill(RENK_LEKE);
                // Düzensiz leke
                gc.fillOval(cx - 11, cy - 8, 22, 16);
                gc.setFill(Color.rgb(200, 80, 80, 0.6));
                gc.fillOval(cx - 5, cy - 4, 10, 8);
            }
        }

        // Kalan temizlik adımını küçük bir gösterge olarak yaz
        int kalan = hucre.getKalanTemizlikAdimi();
        if (kalan > 1) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial Bold", 9));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(kalan), cx, py + HUCRE_BOYUTU - 4);
        }
    }

    /**
     * Robotun gezdiği yolu kesik çizgilerle çizer.
     */
    private void yoluCiz(GraphicsContext gc, Robot robot) {
        List<int[]> gecmis = robot.getYolGecmisi();
        if (gecmis.size() < 2) return;

        gc.setStroke(RENK_YOL);
        gc.setLineWidth(2.5);
        gc.setLineDashes(4, 4);

        double ofset = HUCRE_BOYUTU / 2.0;
        for (int i = 1; i < gecmis.size(); i++) {
            int[] onceki = gecmis.get(i - 1);
            int[] mevcut = gecmis.get(i);
            gc.strokeLine(
                onceki[0] * HUCRE_BOYUTU + ofset,
                onceki[1] * HUCRE_BOYUTU + ofset,
                mevcut[0] * HUCRE_BOYUTU + ofset,
                mevcut[1] * HUCRE_BOYUTU + ofset
            );
        }
        gc.setLineDashes(null);
    }

    /**
     * Izgara çizgilerini çizer.
     */
    private void izgarayiCiz(GraphicsContext gc, Oda oda) {
        gc.setStroke(RENK_IZGARA);
        gc.setLineWidth(0.5);
        int toplamGenislik = oda.getSutunSayisi() * HUCRE_BOYUTU;
        int toplamYukseklik = oda.getSatirSayisi() * HUCRE_BOYUTU;

        // Dikey çizgiler
        for (int x = 0; x <= oda.getSutunSayisi(); x++) {
            gc.strokeLine(x * HUCRE_BOYUTU, 0, x * HUCRE_BOYUTU, toplamYukseklik);
        }
        // Yatay çizgiler
        for (int y = 0; y <= oda.getSatirSayisi(); y++) {
            gc.strokeLine(0, y * HUCRE_BOYUTU, toplamGenislik, y * HUCRE_BOYUTU);
        }
    }

    /**
     * Izgaranın üst ve sol kenarına satır/sütun numaralarını yazar.
     */
    private void koordinatlariCiz(GraphicsContext gc, Oda oda) {
        gc.setFill(Color.rgb(150, 150, 150));
        gc.setFont(Font.font("Monospace", 9));
        gc.setTextAlign(TextAlignment.CENTER);

        // Üst kenarda sütun numaraları
        for (int x = 0; x < oda.getSutunSayisi(); x++) {
            gc.fillText(String.valueOf(x), x * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0, 10);
        }
        // Sol kenarda satır numaraları
        for (int y = 0; y < oda.getSatirSayisi(); y++) {
            gc.fillText(String.valueOf(y), 10, y * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0 + 4);
        }
    }

    /**
     * Robotu mevcut konumunda yönü ve batarya halkasıyla birlikte çizer.
     */
    private void robotuCiz(GraphicsContext gc, Robot robot) {
        double px = robot.getX() * HUCRE_BOYUTU;
        double py = robot.getY() * HUCRE_BOYUTU;
        double cx = px + HUCRE_BOYUTU / 2.0;
        double cy = py + HUCRE_BOYUTU / 2.0;
        double yaricap = HUCRE_BOYUTU * 0.38;

        // Gölge
        gc.setFill(Color.rgb(0, 0, 0, 0.2));
        gc.fillOval(cx - yaricap + 2, cy - yaricap + 2, yaricap * 2, yaricap * 2);

        // Gövde
        gc.setFill(RENK_ROBOT);
        gc.fillOval(cx - yaricap, cy - yaricap, yaricap * 2, yaricap * 2);

        // Üst vurgu (parlama)
        gc.setFill(RENK_ROBOT_VURGU);
        gc.fillOval(cx - yaricap + 3, cy - yaricap + 3, yaricap * 0.8, yaricap * 0.8);

        // Yön göstergesi (gözün bakış yönü)
        double yonUzunlugu = yaricap * 0.75;
        double yonX = cx + robot.getYon().getDx() * yonUzunlugu;
        double yonY = cy + robot.getYon().getDy() * yonUzunlugu;
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeLine(cx, cy, yonX, yonY);

        // Batarya halkası (renkli yay)
        double bataryaYuzdesi = robot.getBatarya() / Robot.MAKS_BATARYA;
        Color bataryaRengi = bataryaYuzdesi > 0.5 ? Color.rgb(50, 200, 100)
                           : bataryaYuzdesi > 0.2 ? Color.rgb(255, 180, 50)
                           : Color.rgb(220, 60, 60);
        gc.setStroke(bataryaRengi);
        gc.setLineWidth(2);
        double yayAcisi = bataryaYuzdesi * 360;
        gc.strokeArc(cx - yaricap - 3, cy - yaricap - 3,
                     (yaricap + 3) * 2, (yaricap + 3) * 2,
                     90, -yayAcisi, javafx.scene.shape.ArcType.OPEN);

        // Şarj olma göstergesi
        if (robot.sarjOluyorMu()) {
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Arial", 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("⚡", cx, cy - yaricap - 5);
        }
    }

    /** @return Hücre boyutu (piksel cinsinden) */
    public int getHucreBoyutu() { return HUCRE_BOYUTU; }

    /** Izgara çizgilerinin görünürlüğünü ayarlar */
    public void setIzgaraGoster(boolean izgaraGoster) { this.izgaraGoster = izgaraGoster; }

    /** Robot yol çizgisinin görünürlüğünü ayarlar */
    public void setYolGoster(boolean yolGoster) { this.yolGoster = yolGoster; }

    /**
     * X piksel koordinatını ızgara sütun indeksine dönüştürür.
     *
     * @param pikselX Piksel cinsinden X koordinatı
     * @return Sütun indeksi
     */
    public int pikselDenSutuna(double pikselX) {
        return (int) (pikselX / HUCRE_BOYUTU);
    }

    /**
     * Y piksel koordinatını ızgara satır indeksine dönüştürür.
     *
     * @param pikselY Piksel cinsinden Y koordinatı
     * @return Satır indeksi
     */
    public int pikselDenSatira(double pikselY) {
        return (int) (pikselY / HUCRE_BOYUTU);
    }
}
