package com.robotvacuum;

import com.robotvacuum.controller.SimulasyonKontroloru;
import com.robotvacuum.model.SimulasyonModeli;
import com.robotvacuum.view.AnaGorunum;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Robot Süpürge Temizlik Simülasyonu uygulamasının ana giriş (başlangıç) noktası.
 * JavaFX Application sınıfından türemiştir. Model, View ve Controller'ı
 * burada birbirine bağlayıp pencereyi ayağa kaldırıyoruz.
 */
public class AnaUygulama extends Application {

    @Override
    public void start(Stage anaSahne) {
        // MVC Bileşenlerini oluşturup birbirine bağlıyoruz
        SimulasyonModeli model = new SimulasyonModeli();
        AnaGorunum gorunum = new AnaGorunum(model);
        SimulasyonKontroloru kontrolor = new SimulasyonKontroloru(model, gorunum);

        gorunum.setKontrolor(kontrolor);

        // Ekran boyutunu ve stil dosyamızı ayarlıyoruz
        Scene sahne = new Scene(gorunum.getRoot(), 950, 550);
        sahne.getStylesheets().add(
            getClass().getResource("/com/robotvacuum/style.css").toExternalForm()
        );

        // Pencere özelliklerini verip ekranda gösteriyoruz
        anaSahne.setTitle("Robot Süpürge Simülasyonu");
        anaSahne.setScene(sahne);
        anaSahne.setMinWidth(800);
        anaSahne.setMinHeight(500);
        anaSahne.show();

        // Arka plandaki zamanlayıcıyı çalıştırıyoruz
        kontrolor.initialize();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
