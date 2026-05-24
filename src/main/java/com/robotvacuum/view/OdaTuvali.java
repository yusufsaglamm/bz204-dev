package com.robotvacuum.view;

import com.robotvacuum.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.List;

/**
 * Odayı, robotu, kirleri ve engelleri çizen JavaFX Canvas.
 *
 * Controller her tick'te yenidenCiz() çağırıyor; biz de o anki model
 * durumuna bakıp ekranı baştan boyuyoruz (immediate-mode çizim).
 */
public class OdaTuvali extends Canvas {

    // Bir hücre kaç piksel
    private static final int HUCRE_BOYUTU = 40;

    // Zemin / engel / şarj / ızgara / temiz / iz renkleri
    private static final Color RENK_ZEMIN     = Color.rgb(245, 245, 240);
    private static final Color RENK_ZEMIN_ALT = Color.rgb(238, 238, 230); // dama deseni için ikinci ton
    private static final Color RENK_ENGEL     = Color.rgb(80, 80, 90);
    private static final Color RENK_SARJ      = Color.rgb(255, 200, 50);
    private static final Color RENK_IZGARA    = Color.rgb(200, 200, 195);
    private static final Color RENK_TEMIZ     = Color.rgb(200, 230, 255, 0.45);
    private static final Color RENK_YOL       = Color.rgb(100, 160, 220, 0.55);

    // Her kir türünün kendine ait rengi
    private static final Color RENK_TOZ       = Color.rgb(180, 150, 100);
    private static final Color RENK_SIVI      = Color.rgb(80, 140, 220);
    private static final Color RENK_LEKE      = Color.rgb(160, 60, 60);

    // Robot için ana ve vurgu rengi
    private static final Color RENK_ROBOT       = Color.rgb(40, 40, 50);
    private static final Color RENK_ROBOT_VURGU = Color.rgb(80, 180, 255);

    private final SimulasyonModeli model;
    private boolean izgaraGoster = true;
    private boolean yolGoster = true;

    public OdaTuvali(SimulasyonModeli model) {
        super(
            Oda.VARSAYILAN_SUTUN * HUCRE_BOYUTU,
            Oda.VARSAYILAN_SATIR * HUCRE_BOYUTU
        );
        this.model = model;
    }

    /**
     * Tuvali baştan çizer.
     * Sıra: arka plan → hücreler → iz → ızgara → koordinatlar → robot (en üstte).
     */
    public void yenidenCiz() {
        GraphicsContext gc = getGraphicsContext2D();
        Oda oda = model.getOda();
        Robot robot = model.getRobot();

        int genislik = (int) getWidth();
        int yukseklik = (int) getHeight();

        gc.setFill(Color.rgb(230, 228, 220));
        gc.fillRect(0, 0, genislik, yukseklik);

        for (int x = 0; x < oda.getSutunSayisi(); x++) {
            for (int y = 0; y < oda.getSatirSayisi(); y++) {
                hucreyiCiz(gc, oda, x, y);
            }
        }

        if (yolGoster) yoluCiz(gc, robot);
        if (izgaraGoster) izgarayiCiz(gc, oda);

        koordinatlariCiz(gc, oda);
        robotuCiz(gc, robot);
    }

    /** Bir hücreyi tipine göre (engel / şarj / zemin) çizer. */
    private void hucreyiCiz(GraphicsContext gc, Oda oda, int x, int y) {
        Hucre hucre = oda.getHucre(x, y);
        double px = x * HUCRE_BOYUTU;
        double py = y * HUCRE_BOYUTU;

        switch (hucre.getTip()) {
            case ENGEL -> {
                gc.setFill(RENK_ENGEL);
                gc.fillRect(px, py, HUCRE_BOYUTU, HUCRE_BOYUTU);
                // Mobilya hissi vermek için iki ince çizgi
                gc.setStroke(Color.rgb(60, 60, 70));
                gc.setLineWidth(0.5);
                gc.strokeLine(px + 4, py + 4, px + HUCRE_BOYUTU - 4, py + 4);
                gc.strokeLine(px + 4, py + HUCRE_BOYUTU - 4, px + HUCRE_BOYUTU - 4, py + HUCRE_BOYUTU - 4);
            }
            case SARJ_ISTASYONU -> {
                gc.setFill(RENK_SARJ);
                gc.fillRect(px, py, HUCRE_BOYUTU, HUCRE_BOYUTU);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", 18));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("⚡", px + HUCRE_BOYUTU / 2.0, py + HUCRE_BOYUTU / 2.0 + 6);
            }
            default -> {
                // Dama deseni: komşu hücre farklı renkte olsun diye (x+y) çift/tek bakıyoruz
                Color zeminRengi = (x + y) % 2 == 0 ? RENK_ZEMIN : RENK_ZEMIN_ALT;
                gc.setFill(zeminRengi);
                gc.fillRect(px, py, HUCRE_BOYUTU, HUCRE_BOYUTU);

                // Robotun süpürdüğü ama artık kir olmayan hücreler hafif mavi
                if (hucre.temizMi() && !hucre.kirVarMi()) {
                    gc.setFill(RENK_TEMIZ);
                    gc.fillRect(px, py, HUCRE_BOYUTU, HUCRE_BOYUTU);
                }

                if (hucre.kirVarMi()) {
                    kiriCiz(gc, hucre, px, py);
                }
            }
        }
    }

    /** Kirin türüne göre farklı şekil çizer (toz noktaları, sıvı damlası, leke). */
    private void kiriCiz(GraphicsContext gc, Hucre hucre, double px, double py) {
        KirTuru tur = hucre.getKirTuru();
        double cx = px + HUCRE_BOYUTU / 2.0;
        double cy = py + HUCRE_BOYUTU / 2.0;

        switch (tur) {
            case TOZ -> {
                // Birkaç küçük nokta = toz hissi
                gc.setFill(RENK_TOZ);
                double[] noktalarX = {cx - 8, cx, cx + 8, cx - 5, cx + 5};
                double[] noktalarY = {cy - 5, cy - 8, cy - 5, cy + 5, cy + 5};
                for (int i = 0; i < noktalarX.length; i++) {
                    gc.fillOval(noktalarX[i] - 2.5, noktalarY[i] - 2.5, 5, 5);
                }
            }
            case SIVI -> {
                // Yamuk oval şekiller = sıvı damlası
                gc.setFill(RENK_SIVI);
                gc.fillOval(cx - 10, cy - 6, 20, 12);
                gc.fillOval(cx - 5, cy - 10, 10, 20);
                gc.setFill(Color.rgb(150, 200, 255, 0.6));
                gc.fillOval(cx - 3, cy - 3, 6, 6);
            }
            case LEKE -> {
                // Tek büyük düzensiz oval = leke
                gc.setFill(RENK_LEKE);
                gc.fillOval(cx - 11, cy - 8, 22, 16);
                gc.setFill(Color.rgb(200, 80, 80, 0.6));
                gc.fillOval(cx - 5, cy - 4, 10, 8);
            }
        }

        // Çok adımlı kirlerde kalan adım sayısını göster
        int kalan = hucre.getKalanTemizlikAdimi();
        if (kalan > 1) {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial Bold", 9));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(kalan), cx, py + HUCRE_BOYUTU - 4);
        }
    }

    /** Robotun ziyaret ettiği hücreleri kesik çizgiyle birleştirir. */
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

    private void izgarayiCiz(GraphicsContext gc, Oda oda) {
        gc.setStroke(RENK_IZGARA);
        gc.setLineWidth(0.5);
        int toplamGenislik = oda.getSutunSayisi() * HUCRE_BOYUTU;
        int toplamYukseklik = oda.getSatirSayisi() * HUCRE_BOYUTU;
        for (int x = 0; x <= oda.getSutunSayisi(); x++) {
            gc.strokeLine(x * HUCRE_BOYUTU, 0, x * HUCRE_BOYUTU, toplamYukseklik);
        }
        for (int y = 0; y <= oda.getSatirSayisi(); y++) {
            gc.strokeLine(0, y * HUCRE_BOYUTU, toplamGenislik, y * HUCRE_BOYUTU);
        }
    }

    /** Üst ve sol kenarlara x/y indekslerini yazar (kullanıcıya yardımcı). */
    private void koordinatlariCiz(GraphicsContext gc, Oda oda) {
        gc.setFill(Color.rgb(150, 150, 150));
        gc.setFont(Font.font("Monospace", 9));
        gc.setTextAlign(TextAlignment.CENTER);
        for (int x = 0; x < oda.getSutunSayisi(); x++) {
            gc.fillText(String.valueOf(x), x * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0, 10);
        }
        for (int y = 0; y < oda.getSatirSayisi(); y++) {
            gc.fillText(String.valueOf(y), 10, y * HUCRE_BOYUTU + HUCRE_BOYUTU / 2.0 + 4);
        }
    }

    /** Robotu, yönünü ve batarya halkasını çizer. */
    private void robotuCiz(GraphicsContext gc, Robot robot) {
        double px = robot.getX() * HUCRE_BOYUTU;
        double py = robot.getY() * HUCRE_BOYUTU;
        double cx = px + HUCRE_BOYUTU / 2.0;
        double cy = py + HUCRE_BOYUTU / 2.0;
        double yaricap = HUCRE_BOYUTU * 0.38;

        // Hafif gölge
        gc.setFill(Color.rgb(0, 0, 0, 0.2));
        gc.fillOval(cx - yaricap + 2, cy - yaricap + 2, yaricap * 2, yaricap * 2);

        // Gövde
        gc.setFill(RENK_ROBOT);
        gc.fillOval(cx - yaricap, cy - yaricap, yaricap * 2, yaricap * 2);

        // Üstte küçük parlak nokta — 3B hissi
        gc.setFill(RENK_ROBOT_VURGU);
        gc.fillOval(cx - yaricap + 3, cy - yaricap + 3, yaricap * 0.8, yaricap * 0.8);

        // Robotun baktığı yöne kısa çizgi
        double yonUzunlugu = yaricap * 0.75;
        double yonX = cx + robot.getYon().getDx() * yonUzunlugu;
        double yonY = cy + robot.getYon().getDy() * yonUzunlugu;
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.strokeLine(cx, cy, yonX, yonY);

        // Batarya halkası: yüzdeye göre yeşil → sarı → kırmızı
        double bataryaYuzdesi = robot.getBatarya() / Robot.MAKS_BATARYA;
        Color bataryaRengi;
        if (bataryaYuzdesi > 0.5) {
            bataryaRengi = Color.rgb(50, 200, 100);
        } else if (bataryaYuzdesi > 0.2) {
            bataryaRengi = Color.rgb(255, 180, 50);
        } else {
            bataryaRengi = Color.rgb(220, 60, 60);
        }
        gc.setStroke(bataryaRengi);
        gc.setLineWidth(2);
        double yayAcisi = bataryaYuzdesi * 360;
        gc.strokeArc(cx - yaricap - 3, cy - yaricap - 3,
                     (yaricap + 3) * 2, (yaricap + 3) * 2,
                     90, -yayAcisi, javafx.scene.shape.ArcType.OPEN);

        // Şarj oluyorken üstte yıldırım
        if (robot.sarjOluyorMu()) {
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Arial", 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("⚡", cx, cy - yaricap - 5);
        }
    }

    public int getHucreBoyutu() { return HUCRE_BOYUTU; }
    public void setIzgaraGoster(boolean izgaraGoster) { this.izgaraGoster = izgaraGoster; }
    public void setYolGoster(boolean yolGoster) { this.yolGoster = yolGoster; }

    /** Fare X koordinatından (piksel) sütun indeksini çıkarır. */
    public int pikselDenSutuna(double pikselX) {
        return (int) (pikselX / HUCRE_BOYUTU);
    }

    /** Fare Y koordinatından (piksel) satır indeksini çıkarır. */
    public int pikselDenSatira(double pikselY) {
        return (int) (pikselY / HUCRE_BOYUTU);
    }
}
