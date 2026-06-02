package com.robotvacuum.view;

import com.robotvacuum.controller.SimulasyonKontroloru;
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
 * Simülasyonun kullanıcı arayüzünü (UI) oluşturan View sınıfı.
 * JavaFX bileşenlerini (Butonlar, Slider'lar, ComboBox vb.) bir araya getirir.
 * Model'deki verilere bağlanarak (binding) arayüzün dinamik olarak güncellenmesini sağlar.
 */
public class AnaGorunum {

    private final SimulasyonModeli model;
    private SimulasyonKontroloru kontrolor;

    private BorderPane anaPanel;
    private OdaKanvasi odaKanvasi;

    private ComboBox<String> odaSecimKutusu;
    private Button btnUlasilamayanKontrol;

    private ToggleGroup kirGrubu;
    private RadioButton rbToz, rbSivi, rbLeke;
    private Slider hizKaydirici;
    private Label lblHizDegeri;
    private ToggleGroup algoritmaGrubu;
    private RadioButton rbRastgele, rbSpiral, rbDuvarTakip;
    private Label lblKonum, lblYon, lblBatarya;
    private ProgressBar pilGostergesi;
    private Button btnKirEkle, btnEngelEkle, btnBaslat, btnDuraklat, btnSifirla, btnIstasyonaDon;
    private ComboBox<com.robotvacuum.model.MobilyaTipi> mobilyaSecimKutusu;
    private Slider pilAyarKaydirici;

    // Alt durum çubuğundaki (Status Bar) etiketler
    private Label lblToplamAlan, lblTemizlenenAlan, lblKalanAlan, lblKirliAlan, lblGecenSure, lblToplananToz, lblDurumMesaji;

    // Olayların yalnızca bir kez bağlanmasını garantiler
    private boolean olaylarBaglandiMi = false;

    public AnaGorunum(SimulasyonModeli model) {
        this.model = model;
        arayuzuInsaEt();
        ozellikleriBagla();
    }

    public void setKontrolor(SimulasyonKontroloru kontrolor) {
        this.kontrolor = kontrolor;
        olaylariBagla();
    }

    /**
     * Arayüzün tüm yerleşim düzenini (Layout) kurar.
     */
    private void arayuzuInsaEt() {
        anaPanel = new BorderPane();
        anaPanel.getStyleClass().add("root-pane");
        
        // Üst kısım: Başlık barı
        anaPanel.setTop(baslikBariniKur());
        
        // Sol kısım: Kontrol paneli (kaydırılabilir yapıyoruz ki küçük ekranlarda sığsın)
        ScrollPane solKaydirmaPaneli = new ScrollPane(solPaneliKur());
        solKaydirmaPaneli.setFitToWidth(true);
        solKaydirmaPaneli.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        solKaydirmaPaneli.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        solKaydirmaPaneli.setStyle("-fx-background-color: transparent; -fx-background: #16213e; -fx-padding: 0; -fx-border-width: 0;");
        solKaydirmaPaneli.setMinHeight(0);
        anaPanel.setLeft(solKaydirmaPaneli);
        
        // Orta kısım: Kanvas (Oda) ve en alttaki durum çubuğu
        VBox ortaAlan = new VBox();
        StackPane kanvasAlani = kanvasAlaniniKur();
        VBox.setVgrow(kanvasAlani, Priority.ALWAYS);
        HBox durumBar = durumBariniKur();
        ortaAlan.getChildren().addAll(kanvasAlani, durumBar);
        
        anaPanel.setCenter(ortaAlan);
    }

    /**
     * Üst başlık alanını oluşturur.
     */
    private HBox baslikBariniKur() {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("title-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 20, 8, 20));

        Label robotSimgesi = new Label("🤖");
        robotSimgesi.setStyle("-fx-font-size: 22px;");
        Label baslikLabel = new Label("Robot Süpürge Simülasyonu");
        baslikLabel.getStyleClass().add("title-label");

        lblDurumMesaji = new Label("Hazır");
        lblDurumMesaji.getStyleClass().add("status-badge");
        
        Region bosluk = new Region();
        HBox.setHgrow(bosluk, Priority.ALWAYS);

        bar.getChildren().addAll(robotSimgesi, baslikLabel, bosluk, lblDurumMesaji);
        return bar;
    }

    /**
     * Sol taraftaki tüm ayarları ve durumları içeren dikey paneli kurar.
     */
    private VBox solPaneliKur() {
        VBox panel = new VBox(6);
        panel.getStyleClass().add("left-panel");
        panel.setPadding(new Insets(10, 10, 10, 10));
        panel.setPrefWidth(220);

        panel.getChildren().addAll(
                odaSecimBolumunuKur(),
                altBaslikOlustur("🔧 Araçlar"),
                kirSecimBolumunuKur(),
                engelSecimBolumunuKur(),
                hizAyarBolumunuKur(),
                algoritmaSecimBolumunuKur(),
                robotDurumBolumunuKur(),
                kontrolDugmeleriBolumunuKur(),
                pilAyarBolumunuKur()
        );
        return panel;
    }

    private VBox odaSecimBolumunuKur() {
        VBox kutu = new VBox(5);
        Label baslik = new Label("🏠 Odalar");
        baslik.getStyleClass().add("section-header-small");

        odaSecimKutusu = new ComboBox<>();

        // ESKİ HALİ: odaSecimKutusu.getItems().addAll("Salon", "Mutfak", "Yatak Odası");
        // YENİ HALİ: Arayüz metinlerini doğrudan oluşturduğumuz OdaTipi enum'ından çekiyoruz
        for (OdaTipi tip : OdaTipi.values()) {
            odaSecimKutusu.getItems().add(tip.getEkranAdi());
        }

        odaSecimKutusu.getSelectionModel().selectFirst();
        odaSecimKutusu.setMaxWidth(Double.MAX_VALUE);

        kutu.getChildren().addAll(baslik, odaSecimKutusu);
        return kutu;
    }

    private Label altBaslikOlustur(String metin) {
        Label baslik = new Label(metin);
        baslik.getStyleClass().add("section-header");
        return baslik;
    }

    private VBox kirSecimBolumunuKur() {
        VBox kutu = new VBox(6);
        btnKirEkle = new Button("Kir Ekle");
        btnKirEkle.getStyleClass().add("btn-action");
        btnKirEkle.setMaxWidth(Double.MAX_VALUE);

        kirGrubu = new ToggleGroup();

        // YENİ HALİ: İsimleri doğrudan senin KirTipi enum'ından çekiyoruz
        rbToz = new RadioButton("💨 " + KirTipi.TOZ.getEkranAdi());
        rbSivi = new RadioButton("💧 " + KirTipi.SIVI.getEkranAdi());
        rbLeke = new RadioButton("🌀 " + KirTipi.LEKE.getEkranAdi());

        for (RadioButton rb : new RadioButton[]{rbToz, rbSivi, rbLeke}) {
            rb.setToggleGroup(kirGrubu);
            rb.getStyleClass().add("rb-small");
        }
        rbToz.setSelected(true);

        kutu.getChildren().addAll(btnKirEkle, rbToz, rbSivi, rbLeke);
        return kutu;
    }

    private VBox engelSecimBolumunuKur() {
        VBox kutu = new VBox(4);
        
        Label baslik = new Label("🪑 Mobilya Seçimi");
        baslik.getStyleClass().add("section-header-small");

        mobilyaSecimKutusu = new ComboBox<>();
        mobilyaSecimKutusu.getItems().addAll(com.robotvacuum.model.MobilyaTipi.values());
        mobilyaSecimKutusu.setValue(com.robotvacuum.model.MobilyaTipi.TEKLI_KOLTUK);
        
        // Enum'ın ekranAdı metodunu kullanarak ComboBox'ta güzel görünmesini sağlıyoruz
        mobilyaSecimKutusu.setCellFactory(lv -> new javafx.scene.control.ListCell<com.robotvacuum.model.MobilyaTipi>() {
            @Override
            protected void updateItem(com.robotvacuum.model.MobilyaTipi item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getEkranAdi() + " (" + item.getGenislik() + "x" + item.getYukseklik() + ")");
            }
        });
        mobilyaSecimKutusu.setButtonCell(new javafx.scene.control.ListCell<com.robotvacuum.model.MobilyaTipi>() {
            @Override
            protected void updateItem(com.robotvacuum.model.MobilyaTipi item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getEkranAdi() + " (" + item.getGenislik() + "x" + item.getYukseklik() + ")");
            }
        });

        btnEngelEkle = new Button("➕ Mobilya Ekle");
        btnEngelEkle.getStyleClass().add("btn-action-secondary");
        btnEngelEkle.setMaxWidth(Double.MAX_VALUE);
        
        kutu.getChildren().addAll(baslik, mobilyaSecimKutusu, btnEngelEkle);
        return kutu;
    }

    private VBox hizAyarBolumunuKur() {
        VBox kutu = new VBox(4);
        Label baslik = new Label("🚀 Robot Hızı");
        baslik.getStyleClass().add("section-header-small");
        
        hizKaydirici = new Slider(0.5, 3.0, 1.0);
        hizKaydirici.setShowTickMarks(true);
        hizKaydirici.setMajorTickUnit(0.5);
        
        lblHizDegeri = new Label("1.0x");
        lblHizDegeri.getStyleClass().add("label-value");
        
        kutu.getChildren().addAll(baslik, hizKaydirici, lblHizDegeri);
        return kutu;
    }

    private VBox algoritmaSecimBolumunuKur() {
        VBox kutu = new VBox(5);
        Label baslik = new Label("⚙️ Temizlik Algoritması");
        baslik.getStyleClass().add("section-header-small");

        algoritmaGrubu = new ToggleGroup();

        // YENİ HALİ: İsimleri doğrudan TemizlikAlgoritmasi enum'ından çekiyoruz
        rbRastgele = new RadioButton(TemizlikAlgoritmasi.RASTGELE.getEkranAdi());
        rbSpiral = new RadioButton(TemizlikAlgoritmasi.SPIRAL.getEkranAdi());
        rbDuvarTakip = new RadioButton(TemizlikAlgoritmasi.DUVAR_TAKIP.getEkranAdi());

        for (RadioButton rb : new RadioButton[]{rbRastgele, rbSpiral, rbDuvarTakip}) {
            rb.setToggleGroup(algoritmaGrubu);
            rb.getStyleClass().add("rb-small");
        }
        rbSpiral.setSelected(true);

        kutu.getChildren().addAll(baslik, rbRastgele, rbSpiral, rbDuvarTakip);
        return kutu;
    }

    private VBox robotDurumBolumunuKur() {
        VBox kutu = new VBox(5);
        Label baslik = new Label("🤖 Robot Durumu");
        baslik.getStyleClass().add("section-header-small");
        
        GridPane tablo = new GridPane();
        tablo.setHgap(6);
        tablo.setVgap(3);
        
        lblKonum = new Label("(0, 0)");
        lblYon = new Label("Doğu (→)");
        lblBatarya = new Label("100%");
        
        lblKonum.getStyleClass().add("label-value");
        lblYon.getStyleClass().add("label-value");
        lblBatarya.getStyleClass().add("label-value");
        
        tablo.add(new Label("Konum:"), 0, 0);   tablo.add(lblKonum, 1, 0);
        tablo.add(new Label("Yön:"), 0, 1);     tablo.add(lblYon, 1, 1);
        tablo.add(new Label("Batarya:"), 0, 2);  tablo.add(lblBatarya, 1, 2);
        
        pilGostergesi = new ProgressBar(1.0);
        pilGostergesi.setMaxWidth(Double.MAX_VALUE);
        pilGostergesi.getStyleClass().add("battery-bar");
        
        kutu.getChildren().addAll(baslik, tablo, pilGostergesi);
        return kutu;
    }

    private VBox kontrolDugmeleriBolumunuKur() {
        VBox kutu = new VBox(5);
        Label baslik = new Label("🎮 Kontroller");
        baslik.getStyleClass().add("section-header-small");
        
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
        
        btnUlasilamayanKontrol = new Button("🔍 Ulaşılamayan Alan Bul");
        btnUlasilamayanKontrol.getStyleClass().add("btn-action-secondary");
        btnUlasilamayanKontrol.setMaxWidth(Double.MAX_VALUE);
        
        kutu.getChildren().addAll(baslik, satir1, btnSifirla, btnIstasyonaDon, btnUlasilamayanKontrol);
        return kutu;
    }

    private VBox pilAyarBolumunuKur() {
        VBox kutu = new VBox(4);
        Label baslik = new Label("🔋 Batarya Ayarla");
        baslik.getStyleClass().add("section-header-small");
        
        pilAyarKaydirici = new Slider(0, 100, 100);
        pilAyarKaydirici.setShowTickLabels(true);
        pilAyarKaydirici.setMajorTickUnit(50);
        
        Label ipucu = new Label("Kaydır ve bırak");
        ipucu.getStyleClass().add("label-hint");
        
        kutu.getChildren().addAll(baslik, pilAyarKaydirici, ipucu);
        return kutu;
    }

    private StackPane kanvasAlaniniKur() {
        odaKanvasi = new OdaKanvasi(model);
        StackPane panel = new StackPane(odaKanvasi);
        panel.getStyleClass().add("canvas-scroll");
        
        // Kanvasın boyutunu panelinkine bağlıyoruz ki pencere boyutu değiştikçe tetiklensin
        odaKanvasi.widthProperty().bind(panel.widthProperty());
        odaKanvasi.heightProperty().bind(panel.heightProperty());
        
        panel.widthProperty().addListener((obs, eskiV, yeniV) -> odaKanvasi.yenidenCiz());
        panel.heightProperty().addListener((obs, eskiV, yeniV) -> odaKanvasi.yenidenCiz());
        
        return panel;
    }

    /**
     * En alttaki durum çubuğunun yapısını oluşturur (Özel tasarlanmış hücreler).
     */
    private HBox durumBariniKur() {
        HBox disKonteyner = new HBox();
        disKonteyner.setStyle("-fx-background-color: #0b1120; -fx-padding: 6 10 6 10;");
        disKonteyner.setAlignment(Pos.CENTER);

        HBox icKutu = new HBox();
        icKutu.setStyle("-fx-background-color: #1a2238; -fx-background-radius: 8; -fx-border-color: #2b3655; -fx-border-radius: 8; -fx-padding: 5 15 5 15;");
        icKutu.setAlignment(Pos.CENTER);
        icKutu.setSpacing(15);

        lblToplamAlan = new Label("0 m²");
        lblTemizlenenAlan = new Label("0 m² (0%)");
        lblKalanAlan = new Label("0 m² (0%)");
        lblKirliAlan = new Label("0 m²");
        lblGecenSure = new Label("00:00");
        lblToplananToz = new Label("0%");

        icKutu.getChildren().addAll(
                istatistikHucresiKur(new Circle(5, Color.web("#1f80ff")), "Toplam Alan", lblToplamAlan),
                dikeyAyiriciKur(),
                istatistikHucresiKur(new Circle(5, Color.web("#22c55e")), "Temizlenen Alan", lblTemizlenenAlan),
                dikeyAyiriciKur(),
                istatistikHucresiKur(new Circle(5, Color.web("#d1c0a8")), "Gezilmemiş Alan", lblKalanAlan),
                dikeyAyiriciKur(),
                istatistikHucresiKur(new Circle(5, Color.web("#ef4444")), "Kirli Alan", lblKirliAlan),
                dikeyAyiriciKur(),
                istatistikHucresiKur(simgeOlustur("🕒", "#a855f7"), "Geçen Süre", lblGecenSure),
                dikeyAyiriciKur(),
                istatistikHucresiKur(simgeOlustur("🧹", "#f59e0b"), "Temizlenen Kir", lblToplananToz)
        );

        HBox.setHgrow(icKutu, Priority.ALWAYS);
        disKonteyner.getChildren().add(icKutu);
        return disKonteyner;
    }

    private Node simgeOlustur(String emoji, String renkKodu) {
        Label lbl = new Label(emoji);
        lbl.setStyle("-fx-font-size: 18px; -fx-text-fill: " + renkKodu + ";");
        return lbl;
    }

    private HBox istatistikHucresiKur(Node simge, String alanAdi, Label degerLabel) {
        HBox hucre = new HBox(12);
        hucre.setAlignment(Pos.CENTER_LEFT);

        VBox metinler = new VBox(2);
        Label baslik = new Label(alanAdi);
        baslik.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        degerLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold;");

        metinler.getChildren().addAll(baslik, degerLabel);
        hucre.getChildren().addAll(simge, metinler);

        HBox.setHgrow(hucre, Priority.ALWAYS);
        return hucre;
    }

    private Rectangle dikeyAyiriciKur() {
        Rectangle cizgi = new Rectangle(1, 35);
        cizgi.setFill(Color.web("#2b3655"));
        return cizgi;
    }

    /**
     * Model'deki verileri JavaFX özellikleri yardımıyla arayüze bağlar.
     */
    private void ozellikleriBagla() {
        pilGostergesi.progressProperty().bind(model.bataryaOzelligi().divide(100.0));
        lblBatarya.textProperty().bind(Bindings.format("%.0f%%", model.bataryaOzelligi()));
        
        // Batarya seviyesine göre pil çubuğunun rengini dinamik değiştiriyoruz
        model.bataryaOzelligi().addListener((obs, eskiD, yeniD) -> {
            double yuzde = yeniD.doubleValue();
            pilGostergesi.setStyle(yuzde > 50 ? "-fx-accent: #3cb96a;" : yuzde > 20 ? "-fx-accent: #e8a020;" : "-fx-accent: #d94040;");
        });
        
        model.konumOzelligi().addListener((obs, o, n) -> lblKonum.setText(n));
        model.yonOzelligi().addListener((obs, o, n) -> lblYon.setText(n));
        model.durumOzelligi().addListener((obs, o, n) -> lblDurumMesaji.setText(n));
        
        hizKaydirici.valueProperty().addListener((obs, o, n) -> lblHizDegeri.setText(String.format("%.1fx", n.doubleValue())));

        model.toplamAlanOzelligi().addListener((obs, o, n) -> istatistikEtiketleriniGuncelle());
        model.temizlenenAlanOzelligi().addListener((obs, o, n) -> istatistikEtiketleriniGuncelle());
        model.kirliAlanOzelligi().addListener((obs, o, n) -> istatistikEtiketleriniGuncelle());
        model.gecenSureOzelligi().addListener((obs, o, n) -> lblGecenSure.setText(n));
        
        model.toplananTozOzelligi().addListener((obs, o, n) -> {
            int toplamKir = model.getToplamBaslangicKiri();
            double yuzde = toplamKir > 0 ? (n.doubleValue() / toplamKir) * 100 : 0;
            lblToplananToz.setText(String.format("%.0f%%", yuzde));
        });
        
        istatistikEtiketleriniGuncelle();
        lblDurumMesaji.setText("Hazır");
        lblGecenSure.setText("00:00");
        lblToplananToz.setText("0%");
    }

    private void istatistikEtiketleriniGuncelle() {
        int toplam = model.getOda().getToplamYuruyebilirAlan();
        int temizlenen = model.getOda().getTemizlenenAlan();
        int kalan = toplam - temizlenen;
        
        lblToplamAlan.setText(toplam + " m²");
        lblTemizlenenAlan.setText(String.format("%d m² (%.0f%%)", temizlenen, toplam > 0 ? (temizlenen * 100.0 / toplam) : 0));
        lblKalanAlan.setText(String.format("%d m² (%.0f%%)", kalan, toplam > 0 ? (kalan * 100.0 / toplam) : 0));
        lblKirliAlan.setText(model.getOda().getKirliAlanSayisi() + " m²");
    }

    /**
     * Düğmelere tıklandığında kontrolcüdeki (Controller) olayları tetikler.
     */
    private void olaylariBagla() {
        // Aynı dinleyicilerin (listener) birden fazla kez bağlanmasını engelliyoruz
        if (olaylarBaglandiMi) return;
        olaylarBaglandiMi = true;

        btnBaslat.setOnAction(e -> { kontrolor.baslat(); duzenlemeModuGeriBildirimiGuncelle(); });
        btnDuraklat.setOnAction(e -> kontrolor.duraklat());
        btnSifirla.setOnAction(e -> { kontrolor.sifirla(); duzenlemeModuGeriBildirimiGuncelle(); });
        btnIstasyonaDon.setOnAction(e -> kontrolor.istasyonaDon());
        btnUlasilamayanKontrol.setOnAction(e -> kontrolor.ulasilamayanAlanBul());

        odaSecimKutusu.setOnAction(e -> {
            if (odaSecimKutusu.getValue() != null) {
                kontrolor.odaPlaniDegistir(odaSecimKutusu.getValue());
            }
        });

        btnKirEkle.setOnAction(e -> { kontrolor.kirEklemeModu(); duzenlemeModuGeriBildirimiGuncelle(); });
        btnEngelEkle.setOnAction(e -> { kontrolor.engelEklemeModu(); duzenlemeModuGeriBildirimiGuncelle(); });

        kirGrubu.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n == rbToz) kontrolor.kirTipiSec(KirTipi.TOZ);
            if (n == rbSivi) kontrolor.kirTipiSec(KirTipi.SIVI);
            if (n == rbLeke) kontrolor.kirTipiSec(KirTipi.LEKE);
        });

        // Hız, algoritma, batarya ve kanvas tıklama olaylarını da bağlıyoruz
        algoritmaBagla();
    }

    /**
     * Kir/Mobilya ekleme modlarının arayüzde net görünmesini sağlar:
     * aktif buton vurgulanır ve üst durum rozeti güncel modu yazar.
     */
    private void duzenlemeModuGeriBildirimiGuncelle() {
        String mod = kontrolor.getDuzenlemeModu();
        btnKirEkle.getStyleClass().remove("btn-active");
        btnEngelEkle.getStyleClass().remove("btn-active");

        if ("kir".equals(mod)) {
            btnKirEkle.getStyleClass().add("btn-active");
            lblDurumMesaji.setText("Kir ekleme modu");
        } else if ("engel".equals(mod)) {
            btnEngelEkle.getStyleClass().add("btn-active");
            lblDurumMesaji.setText("Mobilya ekleme modu");
        }
    }

    public KirTipi getSecilenKirTipi() {
        if (rbSivi.isSelected()) return KirTipi.SIVI;
        if (rbLeke.isSelected()) return KirTipi.LEKE;
        return KirTipi.TOZ;
    }

    public com.robotvacuum.model.MobilyaTipi getSecilenMobilyaTipi() {
        return mobilyaSecimKutusu.getValue();
    }

    private void algoritmaBagla() {
        algoritmaGrubu.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n == rbRastgele) kontrolor.algoritmaSec(TemizlikAlgoritmasi.RASTGELE);
            if (n == rbSpiral) kontrolor.algoritmaSec(TemizlikAlgoritmasi.SPIRAL);
            if (n == rbDuvarTakip) kontrolor.algoritmaSec(TemizlikAlgoritmasi.DUVAR_TAKIP);
        });

        hizKaydirici.valueProperty().addListener((obs, o, n) -> kontrolor.hizDegistir(n.doubleValue()));
        pilAyarKaydirici.setOnMouseReleased(e -> kontrolor.elleBataryaAyarla(pilAyarKaydirici.getValue()));
        
        // Kanvasa tıklandığında (engelleri veya kirleri yerleştirmek için) koordinat çözümlemesi yapıyoruz
        odaKanvasi.setOnMouseClicked(e -> {
            int sutun = odaKanvasi.pikseldenSutuna(e.getX());
            int satir = odaKanvasi.pikseldenSatira(e.getY());
            kontrolor.tuvaleTiklandi(sutun, satir);
        });
    }

    public Parent getRoot() { return anaPanel; }
    public OdaKanvasi getOdaKanvasi() { return odaKanvasi; }
    public ComboBox<String> getOdaSecimKutusu() { return odaSecimKutusu; }
}
