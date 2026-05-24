package com.robotvacuum;

import com.robotvacuum.controller.SimulasyonKontrolcusu;
import com.robotvacuum.model.SimulasyonModeli;
import com.robotvacuum.view.AnaGorunum;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * BZ 214 Görsel Programlama
 * Robot Süpürge Temizlik Simülasyonu
 *
 * <p>Uygulamanın ana giriş noktası. JavaFX {@link Application} sınıfından türetilir.</p>
 *
 * <p><b>MVC Mimarisi:</b></p>
 * <ul>
 *   <li>{@link SimulasyonModeli} - Model katmanı (durum ve mantık)</li>
 *   <li>{@link AnaGorunum} - View katmanı (UI bileşenleri)</li>
 *   <li>{@link SimulasyonKontrolcusu} - Controller katmanı (olay yönetimi)</li>
 * </ul>
 */
public class AnaUygulama extends Application {

    /**
     * JavaFX'in çağırdığı yaşam döngüsü metodu.
     * Uygulamanın ana penceresini ve sahnesini oluşturur.
     *
     * <p><b>Not:</b> "start" adı JavaFX framework'ünün zorunlu kıldığı bir
     * isim olduğu için <b>korunmuştur</b>.</p>
     *
     * @param primaryStage JavaFX tarafından sağlanan ana sahne (pencere)
     */
    @Override
    public void start(Stage primaryStage) {
        // MVC bileşenlerini oluştur
        SimulasyonModeli model = new SimulasyonModeli();
        AnaGorunum gorunum = new AnaGorunum(model);
        SimulasyonKontrolcusu kontrolcu = new SimulasyonKontrolcusu(model, gorunum);

        // Görünüme kontrolcüyü bağla (olayları yakalaması için)
        gorunum.setKontrolcu(kontrolcu);

        // Sahneyi oluştur ve CSS stilini uygula
        Scene sahne = new Scene(gorunum.getKok(), 1280, 780);
        sahne.getStylesheets().add(
            getClass().getResource("/com/robotvacuum/style.css").toExternalForm()
        );

        // Ana pencere ayarları
        primaryStage.setTitle("Robot Süpürge Simülasyonu");
        primaryStage.setScene(sahne);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.show();

        // Kontrolcüyü başlat (varsayılan mobilyalar, kirler ve animasyon zamanlayıcısı)
        kontrolcu.initialize();
    }

    /**
     * JVM tarafından çağrılan uygulama giriş noktası.
     *
     * <p><b>Not:</b> "main" adı Java dili tarafından zorunlu kılınan bir
     * isim olduğu için <b>korunmuştur</b>.</p>
     *
     * @param args Komut satırı argümanları (kullanılmıyor)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
