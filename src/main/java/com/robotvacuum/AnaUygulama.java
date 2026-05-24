package com.robotvacuum;

import com.robotvacuum.controller.SimulasyonKontrolcusu;
import com.robotvacuum.model.SimulasyonModeli;
import com.robotvacuum.view.AnaGorunum;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * BZ 214 Görsel Programlama — Robot Süpürge Simülasyonu.
 * Uygulamanın giriş noktası. JavaFX Application'dan türemiştir.
 *
 * MVC: SimulasyonModeli = Model, AnaGorunum = View, SimulasyonKontrolcusu = Controller.
 * Üçü bu sınıfta bağlanıyor.
 */
public class AnaUygulama extends Application {

    /**
     * JavaFX uygulama açıldığında bu metodu otomatik çağırır.
     * (start ismi framework zorunluluğu, dokunmadık.)
     */
    @Override
    public void start(Stage primaryStage) {
        // Model -> View -> Controller sırasıyla kuruluyoruz, sonra birbirlerine tanıştırıyoruz.
        SimulasyonModeli model = new SimulasyonModeli();
        AnaGorunum gorunum = new AnaGorunum(model);
        SimulasyonKontrolcusu kontrolcu = new SimulasyonKontrolcusu(model, gorunum);
        gorunum.setKontrolcu(kontrolcu);

        Scene sahne = new Scene(gorunum.getKok(), 1280, 780);
        sahne.getStylesheets().add(
            getClass().getResource("/com/robotvacuum/style.css").toExternalForm()
        );

        primaryStage.setTitle("Robot Süpürge Simülasyonu");
        primaryStage.setScene(sahne);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.show();

        // Varsayılan mobilyalar, kirler ve animasyon timer'ı burada başlıyor.
        kontrolcu.initialize();
    }

    /** Java'nın aradığı standart giriş noktası — JavaFX'i başlatır. */
    public static void main(String[] args) {
        launch(args);
    }
}
