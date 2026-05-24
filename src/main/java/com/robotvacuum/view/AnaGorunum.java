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
 * Simülasyonun ana görünüm sınıfı (MVC mimarisinde "View" katmanı).
 *
 * <p>Bu sınıf tüm kullanıcı arayüzünü oluşturur:</p>
 * <ul>
 *   <li>Üst başlık çubuğu (uygulama adı ve durum rozeti)</li>
 *   <li>Sol panel (araçlar, algoritma seçimi, robot durumu, kontroller)</li>
 *   <li>Orta canvas (oda görselleştirmesi)</li>
 *   <li>Alt durum çubuğu (alan istatistikleri ve süre)</li>
 * </ul>
 *
 * <p>Model özelliklerini JavaFX binding ile UI bileşenlerine bağlar ve
 * kullanıcı olaylarını kontrolcüye iletir.</p>
 *
 * <p><b>Not:</b> Bileşenlere uygulanan CSS sınıf adları (örn. "root-pane",
 * "title-bar") style.css dosyasıyla eşleştirildiği için <b>korunmuştur</b>.</p>
 */
public class AnaGorunum {

    /** Bağlı olduğumuz simülasyon modeli */
    private final SimulasyonModeli model;
    /** Kullanıcı olaylarını iletecek kontrolcü */
    private SimulasyonKontrolcusu kontrolcu;

    // Kök yerleşim
    private BorderPane kok;

    // Oda tuvali
    private OdaTuvali odaTuvali;

    // Sol panel kontrolleri
    private ToggleGroup kirTipiGrubu;
    private RadioButton rbToz, rbSivi, rbLeke;
    private Slider hizKaydirici;
    private Label hizEtiketi;
    private ToggleGroup algoritmaGrubu;
    private RadioButton rbRastgele, rbSpiral, rbDuvarTakip;
    private Label konumEtiketi;
    private Label yonEtiketi;
    private Label bataryaEtiketi;
    private ProgressBar bataryaCubugu;
    private Button btnKirEkle;
    private Button btnMobilyaEkle;
    private Button btnBaslat;
    private Button btnDuraklat;
    private Button btnSifirla;
    private Button btnIstasyonaDon;
    private Slider bataryaKaydirici;

    // Alt durum çubuğu
    private Label toplamAlanEtiketi;
    private Label temizlenenAlanEtiketi;
    private Label kalanAlanEtiketi;
    private Label gecenSureEtiketi;
    private Label toplananTozEtiketi;
    private Label durumEtiketi;

    /**
     * Yeni bir ana görünüm oluşturur ve UI bileşenlerini kurar.
     *
     * @param model Bağlanacak simülasyon modeli
     */
    public AnaGorunum(SimulasyonModeli model) {
        this.model = model;
        arayuzuOlustur();
        ozellikleriBagla();
    }

    /**
     * Kontrolcüyü ayarlar ve buton olaylarını ona bağlar.
     *
     * @param kontrolcu Kullanılacak simülasyon kontrolcüsü
     */
    public void setKontrolcu(SimulasyonKontrolcusu kontrolcu) {
        this.kontrolcu = kontrolcu;
        olaylariBagla();
    }

    // ==================== UI OLUŞTURMA ====================

    /**
     * Tüm arayüz hiyerarşisini oluşturur: başlık, sol panel, canvas, durum çubuğu.
     */
    private void arayuzuOlustur() {
        kok = new BorderPane();
        kok.getStyleClass().add("root-pane");

        // Başlık çubuğu
        HBox baslikCubugu = baslikCubuguOlustur();
        kok.setTop(baslikCubugu);

        // Sol panel
        VBox solPanel = solPaneliOlustur();
        kok.setLeft(solPanel);

        // Orta - kaydırılabilir canvas
        ScrollPane kaydirilabilirPanel = tuvalAlaniniOlustur();
        kok.setCenter(kaydirilabilirPanel);

        // Alt durum çubuğu
        HBox durumCubugu = durumCubuguOlustur();
        kok.setBottom(durumCubugu);
    }

    /**
     * Üst başlık çubuğunu oluşturur (uygulama adı ve durum rozeti).
     */
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
        HBox.setHgrow(new Region(), Priority.ALWAYS);
        Region bosluk = new Region();
        HBox.setHgrow(bosluk, Priority.ALWAYS);

        cubuk.getChildren().addAll(ikon, baslik, bosluk, durumEtiketi);
        return cubuk;
    }

    /**
     * Sol kontrol panelini oluşturur (araçlar, algoritma, robot durumu, vb.).
     */
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

    /**
     * Bölüm başlığı etiketi oluşturur.
     */
    private Label bolumBasligiOlustur(String metin) {
        Label lbl = new Label(metin);
        lbl.getStyleClass().add("section-header");
        return lbl;
    }

    /**
     * Kir ekleme bölümünü oluşturur (buton + radyo düğmeleri).
     */
    private VBox kirBolumuOlustur() {
        VBox kutu = new VBox(6);

        btnKirEkle = new Button("💩  Kir Ekle");
        btnKirEkle.getStyleClass().add("btn-action");
        btnKirEkle.setMaxWidth(Double.MAX_VALUE);

        Label lbl = new Label("Kir Türü:");
        lbl.getStyleClass().add("label-small");

        kirTipiGrubu = new ToggleGroup();
        rbToz  = new RadioButton("💨 Toz");
        rbSivi = new RadioButton("💧 Sıvı");
        rbLeke = new RadioButton("🌀 Leke");

        for (RadioButton rb : new RadioButton[]{rbToz, rbSivi, rbLeke}) {
            rb.setToggleGroup(kirTipiGrubu);
            rb.getStyleClass().add("rb-small");
        }
        rbToz.setSelected(true);

        kutu.getChildren().addAll(btnKirEkle, lbl, rbToz, rbSivi, rbLeke);
        return kutu;
    }

    /**
     * Mobilya (engel) ekleme bölümünü oluşturur.
     */
    private VBox engelBolumuOlustur() {
        VBox kutu = new VBox(4);
        btnMobilyaEkle = new Button("🪑  Mobilya Ekle");
        btnMobilyaEkle.getStyleClass().add("btn-action-secondary");
        btnMobilyaEkle.setMaxWidth(Double.MAX_VALUE);
        kutu.getChildren().add(btnMobilyaEkle);
        return kutu;
    }

    /**
     * Robot hızı bölümünü oluşturur (kaydırıcı + etiket).
     */
    private VBox hizBolumuOlustur() {
        VBox kutu = new VBox(4);
        Label lbl = new Label("🚀 Robot Hızı");
        lbl.getStyleClass().add("section-header-small");

        hizKaydirici = new Slider(0.5, 3.0, 1.0);
        hizKaydirici.setShowTickLabels(false);
        hizKaydirici.setShowTickMarks(true);
        hizKaydirici.setMajorTickUnit(0.5);
        hizKaydirici.getStyleClass().add("speed-slider");

        hizEtiketi = new Label("1.0x");
        hizEtiketi.getStyleClass().add("label-value");

        kutu.getChildren().addAll(lbl, hizKaydirici, hizEtiketi);
        return kutu;
    }

    /**
     * Temizlik algoritması seçim bölümünü oluşturur.
     */
    private VBox algoritmaBolumuOlustur() {
        VBox kutu = new VBox(5);
        Label lbl = new Label("⚙️ Temizlik Algoritması");
        lbl.getStyleClass().add("section-header-small");

        algoritmaGrubu = new ToggleGroup();
        rbRastgele    = new RadioButton("Rastgele");
        rbSpiral      = new RadioButton("Spiral");
        rbDuvarTakip  = new RadioButton("Duvar Takip");

        for (RadioButton rb : new RadioButton[]{rbRastgele, rbSpiral, rbDuvarTakip}) {
            rb.setToggleGroup(algoritmaGrubu);
            rb.getStyleClass().add("rb-small");
        }
        rbSpiral.setSelected(true);

        kutu.getChildren().addAll(lbl, rbRastgele, rbSpiral, rbDuvarTakip);
        return kutu;
    }

    /**
     * Robotun mevcut durumunu (konum, yön, batarya) gösteren bölümü oluşturur.
     */
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

        izgara.add(new Label("Konum:"), 0, 0);
        izgara.add(konumEtiketi, 1, 0);
        izgara.add(new Label("Yön:"), 0, 1);
        izgara.add(yonEtiketi, 1, 1);
        izgara.add(new Label("Batarya:"), 0, 2);
        izgara.add(bataryaEtiketi, 1, 2);

        bataryaCubugu = new ProgressBar(1.0);
        bataryaCubugu.setMaxWidth(Double.MAX_VALUE);
        bataryaCubugu.getStyleClass().add("battery-bar");

        kutu.getChildren().addAll(lbl, izgara, bataryaCubugu);
        return kutu;
    }

    /**
     * Başlat, duraklat, sıfırla ve istasyona dön kontrol butonlarını oluşturur.
     */
    private VBox kontrolBolumuOlustur() {
        VBox kutu = new VBox(5);
        Label lbl = new Label("🎮 Kontroller");
        lbl.getStyleClass().add("section-header-small");

        HBox satir1 = new HBox(5);
        btnBaslat = new Button("▶ Başlat");
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

    /**
     * Manuel batarya ayarlama bölümünü oluşturur (kaydırıcı).
     */
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

    /**
     * Orta bölümdeki kaydırılabilir canvas alanını oluşturur.
     */
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

    /**
     * Alt durum çubuğunu oluşturur (toplam alan, temizlenen, kalan, süre, toz).
     */
    private HBox durumCubuguOlustur() {
        HBox cubuk = new HBox();
        cubuk.getStyleClass().add("status-bar");
        cubuk.setAlignment(Pos.CENTER_LEFT);
        cubuk.setSpacing(0);
        cubuk.setPadding(new Insets(0, 0, 0, 0));

        toplamAlanEtiketi     = new Label();
        temizlenenAlanEtiketi = new Label();
        kalanAlanEtiketi      = new Label();
        gecenSureEtiketi      = new Label();
        toplananTozEtiketi    = new Label();

        cubuk.getChildren().addAll(
            istatistikHucresiOlustur("●", Color.rgb(80, 130, 200),   "Toplam Alan",    toplamAlanEtiketi,     "m²"),
            ayiriciOlustur(),
            istatistikHucresiOlustur("●", Color.rgb(60, 180, 120),   "Temizlenen",     temizlenenAlanEtiketi, "m²"),
            ayiriciOlustur(),
            istatistikHucresiOlustur("●", Color.rgb(200, 100, 60),   "Kalan Alan",     kalanAlanEtiketi,      "m²"),
            ayiriciOlustur(),
            istatistikHucresiOlustur("🕐", null,                      "Geçen Süre",     gecenSureEtiketi,      ""),
            ayiriciOlustur(),
            istatistikHucresiOlustur("🧹", null,                      "Toplanan Toz",   toplananTozEtiketi,    "%")
        );
        return cubuk;
    }

    /**
     * Durum çubuğundaki bir istatistik hücresini oluşturur.
     *
     * @param ikon       Hücrenin ikonu (emoji)
     * @param noktaRengi Hücrenin nokta rengi (null ise emoji ikon kullanılır)
     * @param ad         İstatistiğin adı (örn. "Toplam Alan")
     * @param degerEtiketi Değerin yazılacağı etiket
     * @param birim      Değerin birimi (örn. "m²")
     */
    private HBox istatistikHucresiOlustur(String ikon, Color noktaRengi, String ad, Label degerEtiketi, String birim) {
        HBox hucre = new HBox(6);
        hucre.setAlignment(Pos.CENTER);
        hucre.setPadding(new Insets(8, 16, 8, 16));
        HBox.setHgrow(hucre, Priority.ALWAYS);

        Node ikonNode;
        if (noktaRengi != null) {
            // Renkli nokta kullan
            Circle nokta = new Circle(5, noktaRengi);
            ikonNode = nokta;
        } else {
            // Emoji ikon kullan
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

    /**
     * Durum çubuğu hücreleri arasında kullanılan dikey ayırıcı çizgiyi oluşturur.
     */
    private Rectangle ayiriciOlustur() {
        Rectangle ayirici = new Rectangle(1, 36);
        ayirici.setFill(Color.rgb(255, 255, 255, 0.15));
        return ayirici;
    }

    // ==================== ÖZELLİK BAĞLAMA ====================

    /**
     * Model özelliklerini UI bileşenlerine bağlar.
     * Model değiştiğinde UI otomatik olarak güncellenir.
     */
    private void ozellikleriBagla() {
        // Batarya çubuğu ve etiketi
        bataryaCubugu.progressProperty().bind(
            model.bataryaOzelligi().divide(100.0)
        );
        bataryaEtiketi.textProperty().bind(
            Bindings.format("%.0f%%", model.bataryaOzelligi())
        );

        // Batarya seviyesine göre renk değişimi (yeşil -> sarı -> kırmızı)
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

        // Durum etiketi (üst sağdaki rozet)
        model.durumOzelligi().addListener((obs, e, y) -> durumEtiketi.setText(y));

        // Hız kaydırıcısı etiketi
        hizKaydirici.valueProperty().addListener((obs, e, y) -> {
            hizEtiketi.setText(String.format("%.1fx", y.doubleValue()));
        });

        // Manuel batarya kaydırıcısı - fare bırakıldığında uygulanır
        bataryaKaydirici.valueProperty().addListener((obs, e, y) -> {
            // sadece canlı önizleme
        });

        // İstatistikler
        model.toplamAlanOzelligi().addListener((obs, e, y) ->
            istatistikEtiketleriniGuncelle()
        );
        model.temizlenenAlanOzelligi().addListener((obs, e, y) ->
            istatistikEtiketleriniGuncelle()
        );
        model.kirliAlanOzelligi().addListener((obs, e, y) ->
            istatistikEtiketleriniGuncelle()
        );
        model.gecenSureOzelligi().addListener((obs, e, y) ->
            gecenSureEtiketi.setText(y)
        );
        model.toplananTozOzelligi().addListener((obs, e, y) -> {
            int toplam = model.getToplamBaslangicKirSayisi();
            double yuzde = toplam > 0 ? (y.doubleValue() / toplam) * 100 : 0;
            toplananTozEtiketi.setText(String.format("%.0f%%", yuzde));
        });

        // Başlangıç değerleri
        istatistikEtiketleriniGuncelle();
        durumEtiketi.setText("Hazır");
        gecenSureEtiketi.setText("00:00");
        toplananTozEtiketi.setText("0%");
    }

    /**
     * Alt durum çubuğundaki alan istatistik etiketlerini günceller.
     * Toplam, temizlenen ve kalan alanı yüzde değerleriyle gösterir.
     */
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

    // ==================== OLAY BAĞLAMA ====================

    /**
     * Tüm butonları, kaydırıcıları ve canvas tıklamalarını kontrolcü metotlarına bağlar.
     */
    private void olaylariBagla() {
        btnBaslat.setOnAction(e -> kontrolcu.baslatTiklandi());
        btnDuraklat.setOnAction(e -> kontrolcu.duraklatTiklandi());
        btnSifirla.setOnAction(e -> kontrolcu.sifirlaTiklandi());
        btnIstasyonaDon.setOnAction(e -> kontrolcu.istasyonaDonTiklandi());

        btnKirEkle.setOnAction(e -> kontrolcu.kirEkleModuTiklandi());
        btnMobilyaEkle.setOnAction(e -> kontrolcu.engelEkleModuTiklandi());

        // Kir türü radyo butonları
        kirTipiGrubu.selectedToggleProperty().addListener((obs, e, y) -> {
            if (y == rbToz)  kontrolcu.kirTipiSecildi(KirTipi.TOZ);
            if (y == rbSivi) kontrolcu.kirTipiSecildi(KirTipi.SIVI);
            if (y == rbLeke) kontrolcu.kirTipiSecildi(KirTipi.LEKE);
        });

        // Algoritma radyo butonları
        algoritmaGrubu.selectedToggleProperty().addListener((obs, e, y) -> {
            if (y == rbRastgele)   kontrolcu.algoritmaSecildi(TemizlikAlgoritmasi.RASTGELE);
            if (y == rbSpiral)     kontrolcu.algoritmaSecildi(TemizlikAlgoritmasi.SPIRAL);
            if (y == rbDuvarTakip) kontrolcu.algoritmaSecildi(TemizlikAlgoritmasi.DUVAR_TAKIP);
        });

        // Hız kaydırıcısı
        hizKaydirici.valueProperty().addListener((obs, e, y) ->
            kontrolcu.hizDegisti(y.doubleValue())
        );

        // Manuel batarya: kaydırıcı bırakıldığında değeri uygula
        bataryaKaydirici.setOnMouseReleased(e ->
            kontrolcu.manuelBataryaAyarla(bataryaKaydirici.getValue())
        );

        // Canvas tıklaması
        odaTuvali.setOnMouseClicked(e ->
            kontrolcu.tuvaleTiklandi(
                odaTuvali.pikselDenSutuna(e.getX()),
                odaTuvali.pikselDenSatira(e.getY())
            )
        );
    }

    // ==================== GETTER METODLARI ====================

    /** @return UI'nin kök düğümü (Scene'e eklenmek üzere) */
    public Parent getKok() { return kok; }

    /** @return Oda tuvali bileşeni (kontrolcü tarafından yeniden çizmek için) */
    public OdaTuvali getOdaTuvali() { return odaTuvali; }

    /** @return Başlık çubuğundaki durum etiketi */
    public Label getDurumEtiketi() { return durumEtiketi; }
}
