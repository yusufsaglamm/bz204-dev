package com.robotvacuum.view;

import com.robotvacuum.controller.SimulasyonKontrolcusu;
import com.robotvacuum.model.*;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * MVC'deki "View" — kullanıcı arayüzünü kuran sınıf.
 * Üst başlık, sol kontrol paneli, orta canvas ve alttaki durum çubuğu burada.
 *
 * Not: CSS sınıf adlarına (root-pane, title-bar vb.) dokunmadık çünkü
 * style.css dosyasıyla bire bir eşleşmeleri lazım.
 */
public class AnaGorunum {

    private final SimulasyonModeli model;
    private SimulasyonKontrolcusu kontrolcu;

    private BorderPane kok;
    private OdaTuvali odaTuvali;

    // Sol panel: kir türü seçimi
    private ToggleGroup kirTuruGrubu;
    private RadioButton rbToz, rbSivi, rbLeke;

    // Hız kaydırıcısı
    private Slider hizKaydirici;
    private Label hizEtiketi;

    // Algoritma seçimi
    private ToggleGroup algoritmaGrubu;
    private RadioButton rbRastgele, rbSpiral, rbDuvarTakip;

    // Robot durumu
    private Label konumEtiketi;
    private Label yonEtiketi;
    private Label bataryaEtiketi;
    private ProgressBar bataryaCubugu;

    // Butonlar
    private Button btnKirEkle;
    private Button btnMobilyaEkle;
    private Button btnBaslat;
    private Button btnDuraklat;
    private Button btnSifirla;
    private Button btnIstasyonaDon;
    private Slider bataryaKaydirici;

    // Alt durum çubuğu etiketleri
    private Label toplamAlanEtiketi;
    private Label temizlenenAlanEtiketi;
    private Label kalanAlanEtiketi;
    private Label gecenSureEtiketi;
    private Label toplananTozEtiketi;
    private Label durumEtiketi;

    public AnaGorunum(SimulasyonModeli model) {
        this.model = model;
        arayuzuOlustur();
        ozellikleriBagla();
    }

    /** Kontrolcü atanınca buton olaylarını da bağlıyoruz. */
    public void setKontrolcu(SimulasyonKontrolcusu kontrolcu) {
        this.kontrolcu = kontrolcu;
        olaylariBagla();
    }

    // ==================== UI ====================

    /** Tüm UI'yi kurar: üst başlık + sol panel + orta canvas + alt durum çubuğu. */
    private void arayuzuOlustur() {
        kok = new BorderPane();
        kok.getStyleClass().add("root-pane");
        kok.setTop(baslikCubuguOlustur());
        kok.setLeft(solPaneliOlustur());
        kok.setCenter(tuvalAlaniniOlustur());
        kok.setBottom(durumCubuguOlustur());
    }

    private HBox baslikCubuguOlustur() {
        HBox cubuk = new HBox(12);
        cubuk.getStyleClass().add("title-bar");
        cubuk.setAlignment(Pos.CENTER_LEFT);
        cubuk.setPadding(new Insets(10, 16, 10, 16));

        Label ikon = new Label("🤖");
        ikon.setStyle("-fx-font-size: 22px;");
        Label baslik = new Label("Robot Süpürge Simülasyonu");
        baslik.getStyleClass().add("title-label");

        durumEtiketi = new Label("Hazır");
        durumEtiketi.getStyleClass().add("status-badge");

        Region bosluk = new Region();
        HBox.setHgrow(bosluk, Priority.ALWAYS);

        cubuk.getChildren().addAll(ikon, baslik, bosluk, durumEtiketi);
        return cubuk;
    }

    private VBox solPaneliOlustur() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("left-panel");
        panel.setPadding(new Insets(12, 12, 12, 12));
        panel.setPrefWidth(210);
        panel.getChildren().addAll(
            bolumBasligiOlustur("🔧 Araçlar"),
            kirBolumuOlustur(),
            engelBolumuOlustur(),
            hizBolumuOlustur(),
            algoritmaBolumuOlustur(),
            robotDurumuBolumuOlustur(),
            kontrolBolumuOlustur(),
            bataryaBolumuOlustur()
        );
        return panel;
    }

    private Label bolumBasligiOlustur(String metin) {
        Label lbl = new Label(metin);
        lbl.getStyleClass().add("section-header");
        return lbl;
    }

    /** Kir Ekle butonu ve hangi kir türü konulacağını seçen radyo butonlar. */
    private VBox kirBolumuOlustur() {
        VBox kutu = new VBox(6);
        btnKirEkle = new Button("💩  Kir Ekle");
        btnKirEkle.getStyleClass().add("btn-action");
        btnKirEkle.setMaxWidth(Double.MAX_VALUE);

        Label lbl = new Label("Kir Türü:");
        lbl.getStyleClass().add("label-small");

        kirTuruGrubu = new ToggleGroup();
        rbToz  = new RadioButton("💨 Toz");
        rbSivi = new RadioButton("💧 Sıvı");
        rbLeke = new RadioButton("🌀 Leke");
        for (RadioButton rb : new RadioButton[]{rbToz, rbSivi, rbLeke}) {
            rb.setToggleGroup(kirTuruGrubu);
            rb.getStyleClass().add("rb-small");
        }
        rbToz.setSelected(true);

        kutu.getChildren().addAll(btnKirEkle, lbl, rbToz, rbSivi, rbLeke);
        return kutu;
    }

    private VBox engelBolumuOlustur() {
        VBox kutu = new VBox(4);
        btnMobilyaEkle = new Button("🪑  Mobilya Ekle");
        btnMobilyaEkle.getStyleClass().add("btn-action-secondary");
        btnMobilyaEkle.setMaxWidth(Double.MAX_VALUE);
        kutu.getChildren().add(btnMobilyaEkle);
        return kutu;
    }

    private VBox hizBolumuOlustur() {
        VBox kutu = new VBox(4);
        Label lbl = new Label("🚀 Robot Hızı");
        lbl.getStyleClass().add("section-header-small");

        hizKaydirici = new Slider(0.5, 3.0, 1.0);
        hizKaydirici.setShowTickMarks(true);
        hizKaydirici.setMajorTickUnit(0.5);
        hizKaydirici.getStyleClass().add("speed-slider");

        hizEtiketi = new Label("1.0x");
        hizEtiketi.getStyleClass().add("label-value");

        kutu.getChildren().addAll(lbl, hizKaydirici, hizEtiketi);
        return kutu;
    }

    private VBox algoritmaBolumuOlustur() {
        VBox kutu = new VBox(5);
        Label lbl = new Label("⚙️ Temizlik Algoritması");
        lbl.getStyleClass().add("section-header-small");

        algoritmaGrubu = new ToggleGroup();
        rbRastgele   = new RadioButton("Rastgele");
        rbSpiral     = new RadioButton("Spiral");
        rbDuvarTakip = new RadioButton("Duvar Takip");
        for (RadioButton rb : new RadioButton[]{rbRastgele, rbSpiral, rbDuvarTakip}) {
            rb.setToggleGroup(algoritmaGrubu);
            rb.getStyleClass().add("rb-small");
        }
        rbSpiral.setSelected(true);

        kutu.getChildren().addAll(lbl, rbRastgele, rbSpiral, rbDuvarTakip);
        return kutu;
    }

    /** Konum / Yön / Batarya bilgilerinin gösterildiği panel. */
    private VBox robotDurumuBolumuOlustur() {
        VBox kutu = new VBox(5);
        Label lbl = new Label("🤖 Robot Durumu");
        lbl.getStyleClass().add("section-header-small");

        GridPane izgara = new GridPane();
        izgara.setHgap(6);
        izgara.setVgap(3);

        konumEtiketi   = new Label("(0, 0)");
        yonEtiketi     = new Label("Doğu (→)");
        bataryaEtiketi = new Label("100%");
        konumEtiketi.getStyleClass().add("label-value");
        yonEtiketi.getStyleClass().add("label-value");
        bataryaEtiketi.getStyleClass().add("label-value");

        izgara.add(new Label("Konum:"),   0, 0);
        izgara.add(konumEtiketi,          1, 0);
        izgara.add(new Label("Yön:"),     0, 1);
        izgara.add(yonEtiketi,            1, 1);
        izgara.add(new Label("Batarya:"), 0, 2);
        izgara.add(bataryaEtiketi,        1, 2);

        bataryaCubugu = new ProgressBar(1.0);
        bataryaCubugu.setMaxWidth(Double.MAX_VALUE);
        bataryaCubugu.getStyleClass().add("battery-bar");

        kutu.getChildren().addAll(lbl, izgara, bataryaCubugu);
        return kutu;
    }

    /** Başlat / Duraklat / Sıfırla / İstasyona Dön butonları. */
    private VBox kontrolBolumuOlustur() {
        VBox kutu = new VBox(5);
        Label lbl = new Label("🎮 Kontroller");
        lbl.getStyleClass().add("section-header-small");

        HBox satir1 = new HBox(5);
        btnBaslat   = new Button("▶ Başlat");
        btnDuraklat = new Button("⏸ Duraklat");
        btnBaslat.getStyleClass().add("btn-start");
        btnDuraklat.getStyleClass().add("btn-pause");
        btnBaslat.setMaxWidth(Double.MAX_VALUE);
        btnDuraklat.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnBaslat, Priority.ALWAYS);
        HBox.setHgrow(btnDuraklat, Priority.ALWAYS);
        satir1.getChildren().addAll(btnBaslat, btnDuraklat);

        btnSifirla = new Button("⬛ Sıfırla");
        btnSifirla.getStyleClass().add("btn-reset");
        btnSifirla.setMaxWidth(Double.MAX_VALUE);

        btnIstasyonaDon = new Button("🏠 İstasyona Dön");
        btnIstasyonaDon.getStyleClass().add("btn-station");
        btnIstasyonaDon.setMaxWidth(Double.MAX_VALUE);

        kutu.getChildren().addAll(lbl, satir1, btnSifirla, btnIstasyonaDon);
        return kutu;
    }

    /** Kullanıcı bataryayı elle değiştirebilsin diye kaydırıcı. */
    private VBox bataryaBolumuOlustur() {
        VBox kutu = new VBox(4);
        Label lbl = new Label("🔋 Batarya Ayarla");
        lbl.getStyleClass().add("section-header-small");

        bataryaKaydirici = new Slider(0, 100, 100);
        bataryaKaydirici.setShowTickLabels(true);
        bataryaKaydirici.setMajorTickUnit(50);
        bataryaKaydirici.getStyleClass().add("speed-slider");

        Label ipucu = new Label("Kaydır ve bırak");
        ipucu.getStyleClass().add("label-hint");

        kutu.getChildren().addAll(lbl, bataryaKaydirici, ipucu);
        return kutu;
    }

    /** Canvas kaydırılabilir bir panel içine alınıyor — küçük ekranlarda da görünsün. */
    private ScrollPane tuvalAlaniniOlustur() {
        odaTuvali = new OdaTuvali(model);
        odaTuvali.yenidenCiz();

        ScrollPane sp = new ScrollPane(odaTuvali);
        sp.getStyleClass().add("canvas-scroll");
        sp.setFitToHeight(false);
        sp.setFitToWidth(false);
        sp.setPadding(new Insets(8));
        return sp;
    }

    /** Alt durum çubuğu: toplam alan, temizlenen, kalan, süre ve toz oranı. */
    private HBox durumCubuguOlustur() {
        HBox cubuk = new HBox();
        cubuk.getStyleClass().add("status-bar");
        cubuk.setAlignment(Pos.CENTER_LEFT);

        toplamAlanEtiketi     = new Label();
        temizlenenAlanEtiketi = new Label();
        kalanAlanEtiketi      = new Label();
        gecenSureEtiketi      = new Label();
        toplananTozEtiketi    = new Label();

        cubuk.getChildren().addAll(
            istatistikHucresiOlustur("●", Color.rgb(80, 130, 200), "Toplam Alan",  toplamAlanEtiketi,     "m²"),
            ayiriciOlustur(),
            istatistikHucresiOlustur("●", Color.rgb(60, 180, 120), "Temizlenen",   temizlenenAlanEtiketi, "m²"),
            ayiriciOlustur(),
            istatistikHucresiOlustur("●", Color.rgb(200, 100, 60), "Kalan Alan",   kalanAlanEtiketi,      "m²"),
            ayiriciOlustur(),
            istatistikHucresiOlustur("🕐", null,                    "Geçen Süre",   gecenSureEtiketi,      ""),
            ayiriciOlustur(),
            istatistikHucresiOlustur("🧹", null,                    "Toplanan Toz", toplananTozEtiketi,    "%")
        );
        return cubuk;
    }

    /** Durum çubuğunda tek bir istatistik (renkli nokta + ad + değer) hücresi. */
    private HBox istatistikHucresiOlustur(String ikon, Color noktaRengi, String ad, Label degerEtiketi, String birim) {
        HBox hucre = new HBox(6);
        hucre.setAlignment(Pos.CENTER);
        hucre.setPadding(new Insets(8, 16, 8, 16));
        HBox.setHgrow(hucre, Priority.ALWAYS);

        Node ikonNode;
        if (noktaRengi != null) {
            ikonNode = new Circle(5, noktaRengi);
        } else {
            Label ico = new Label(ikon);
            ico.setStyle("-fx-font-size: 14px;");
            ikonNode = ico;
        }

        VBox metinler = new VBox(1);
        Label adEtiketi = new Label(ad);
        adEtiketi.getStyleClass().add("stat-name");
        degerEtiketi.getStyleClass().add("stat-value");

        metinler.getChildren().addAll(adEtiketi, degerEtiketi);
        hucre.getChildren().addAll(ikonNode, metinler);
        return hucre;
    }

    /** İki istatistik arasındaki ince dikey ayırıcı. */
    private Rectangle ayiriciOlustur() {
        Rectangle ayirici = new Rectangle(1, 36);
        ayirici.setFill(Color.rgb(255, 255, 255, 0.15));
        return ayirici;
    }

    // ==================== BINDING ====================

    /**
     * Model property'lerini UI'a bağlıyoruz.
     * Burada binding kullanmanın güzelliği: model değişince UI kendiliğinden
     * güncelleniyor; bizim ayrıca "ekran tazele" diye uğraşmamıza gerek yok.
     */
    private void ozellikleriBagla() {
        // Batarya çubuğu (0-1 aralığı) ve yüzde etiketi
        bataryaCubugu.progressProperty().bind(model.bataryaOzelligi().divide(100.0));
        bataryaEtiketi.textProperty().bind(Bindings.format("%.0f%%", model.bataryaOzelligi()));

        // Batarya yüzdesine göre renk: yeşil → sarı → kırmızı
        model.bataryaOzelligi().addListener((obs, eski, yeni) -> {
            double yuzde = yeni.doubleValue();
            if (yuzde > 50) {
                bataryaCubugu.setStyle("-fx-accent: #3cb96a;");
            } else if (yuzde > 20) {
                bataryaCubugu.setStyle("-fx-accent: #e8a020;");
            } else {
                bataryaCubugu.setStyle("-fx-accent: #d94040;");
            }
        });

        model.konumOzelligi().addListener((obs, e, y) -> konumEtiketi.setText(y));
        model.yonOzelligi().addListener((obs, e, y) -> yonEtiketi.setText(y));
        model.durumOzelligi().addListener((obs, e, y) -> durumEtiketi.setText(y));

        // Kaydırıcı değişince yanındaki etiket de değişsin (1.0x, 2.0x gibi)
        hizKaydirici.valueProperty().addListener((obs, e, y) ->
            hizEtiketi.setText(String.format("%.1fx", y.doubleValue()))
        );

        // Alan istatistikleri herhangi biri değişince hepsini tazele
        model.toplamAlanOzelligi().addListener((obs, e, y) -> istatistikEtiketleriniGuncelle());
        model.temizlenenAlanOzelligi().addListener((obs, e, y) -> istatistikEtiketleriniGuncelle());
        model.kirliAlanOzelligi().addListener((obs, e, y) -> istatistikEtiketleriniGuncelle());

        model.gecenSureOzelligi().addListener((obs, e, y) -> gecenSureEtiketi.setText(y));

        // Toplanan kir / başlangıçtaki toplam kir = yüzde
        model.toplananTozOzelligi().addListener((obs, e, y) -> {
            int toplam = model.getToplamBaslangicKirSayisi();
            double yuzde = toplam > 0 ? (y.doubleValue() / toplam) * 100 : 0;
            toplananTozEtiketi.setText(String.format("%.0f%%", yuzde));
        });

        istatistikEtiketleriniGuncelle();
        durumEtiketi.setText("Hazır");
        gecenSureEtiketi.setText("00:00");
        toplananTozEtiketi.setText("0%");
    }

    /** Toplam / temizlenen / kalan alan etiketlerini birlikte günceller. */
    private void istatistikEtiketleriniGuncelle() {
        int toplam     = model.getOda().getToplamZeminHucresi();
        int temizlenen = model.getOda().getTemizlenenHucreSayisi();
        int kalan      = toplam - temizlenen;

        toplamAlanEtiketi.setText(toplam + " m²");

        double temizlenenYuzde = toplam > 0 ? (temizlenen * 100.0 / toplam) : 0;
        temizlenenAlanEtiketi.setText(String.format("%d m² (%.0f%%)", temizlenen, temizlenenYuzde));

        double kalanYuzde = toplam > 0 ? (kalan * 100.0 / toplam) : 0;
        kalanAlanEtiketi.setText(String.format("%d m² (%.0f%%)", kalan, kalanYuzde));
    }

    // ==================== OLAYLAR ====================

    /** Buton/slider/canvas olaylarını Controller'a iletir. */
    private void olaylariBagla() {
        btnBaslat.setOnAction(e -> kontrolcu.baslatTiklandi());
        btnDuraklat.setOnAction(e -> kontrolcu.duraklatTiklandi());
        btnSifirla.setOnAction(e -> kontrolcu.sifirlaTiklandi());
        btnIstasyonaDon.setOnAction(e -> kontrolcu.istasyonaDonTiklandi());
        btnKirEkle.setOnAction(e -> kontrolcu.kirEkleModuTiklandi());
        btnMobilyaEkle.setOnAction(e -> kontrolcu.engelEkleModuTiklandi());

        kirTuruGrubu.selectedToggleProperty().addListener((obs, e, y) -> {
            if (y == rbToz)  kontrolcu.kirTuruSecildi(KirTuru.TOZ);
            if (y == rbSivi) kontrolcu.kirTuruSecildi(KirTuru.SIVI);
            if (y == rbLeke) kontrolcu.kirTuruSecildi(KirTuru.LEKE);
        });

        algoritmaGrubu.selectedToggleProperty().addListener((obs, e, y) -> {
            if (y == rbRastgele)   kontrolcu.algoritmaSecildi(TemizlikAlgoritmasi.RASTGELE);
            if (y == rbSpiral)     kontrolcu.algoritmaSecildi(TemizlikAlgoritmasi.SPIRAL);
            if (y == rbDuvarTakip) kontrolcu.algoritmaSecildi(TemizlikAlgoritmasi.DUVAR_TAKIP);
        });

        hizKaydirici.valueProperty().addListener((obs, e, y) ->
            kontrolcu.hizDegisti(y.doubleValue())
        );

        // Bataryayı sürekli güncellemek yerine kullanıcı bıraktığında uygula
        bataryaKaydirici.setOnMouseReleased(e ->
            kontrolcu.manuelBataryaAyarla(bataryaKaydirici.getValue())
        );

        odaTuvali.setOnMouseClicked(e ->
            kontrolcu.tuvaleTiklandi(
                odaTuvali.pikselDenSutuna(e.getX()),
                odaTuvali.pikselDenSatira(e.getY())
            )
        );
    }

    // ==================== GETTER ====================

    public Parent getKok() { return kok; }
    public OdaTuvali getOdaTuvali() { return odaTuvali; }
    public Label getDurumEtiketi() { return durumEtiketi; }
}
