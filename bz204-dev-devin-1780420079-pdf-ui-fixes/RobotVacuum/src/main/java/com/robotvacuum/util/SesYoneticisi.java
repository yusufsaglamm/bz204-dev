package com.robotvacuum.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Harici ses dosyalarına (mp3, wav vb.) bağımlı kalmadan, doğrudan
 * bilgisayarın hoparlöründen farklı frekanslarda (Hz) sentezlenmiş ses efektleri üreten sınıf.
 */
public class SesYoneticisi {

    private static boolean sesAktif = true;

    // Aynı anda üst üste yüzlerce ses çalıp ses kartını çökertmemek için kilit (bayrak) değişkenlerimiz.
    // 'volatile' kelimesi, bu değişkenlerin farklı Thread'ler arasında anında güncellenmesini sağlar.
    private static volatile boolean carpmaSesiCalisiyor = false;
    private static volatile boolean temizlikSesiCalisiyor = false;

    public static void setSesAktif(boolean aktif) {
        sesAktif = aktif;
    }

    /**
     * Robot duvara veya bir engele çarptığında çalacak "küt" ses efekti.
     * Frekansı yüksekten alçağa doğru hızlıca düşürerek çarpma hissi yaratıyoruz.
     */
    public static void playBumpSound() {
        // Eğer ses kapalıysa VEYA zaten o an bir çarpma sesi çalıyorsa yenisini başlatma (Çökme koruması)
        if (!sesAktif || carpmaSesiCalisiyor) return;

        carpmaSesiCalisiyor = true; // Ses başladı, kilidi kapat

        new Thread(() -> {
            try {
                AudioFormat af = new AudioFormat(8000f, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                byte[] buf = new byte[1];
                for (int i = 0; i < 50 * 8; i++) {
                    double hz = 400 - (i / (50.0 * 8.0)) * 250; // 400Hz'den 150Hz'e frekans düşüşü
                    double angle = i / (8000f / hz) * 2.0 * Math.PI;
                    double vol = 100.0 * (1.0 - (i / (50.0 * 8.0))); // Sesi yavaş yavaş kısıyoruz (fade-out)
                    buf[0] = (byte) (Math.sin(angle) * vol);
                    sdl.write(buf, 0, 1);
                }
                sdl.drain();
                sdl.stop();
                sdl.close();
            } catch (Exception e) {
                // Hata verirse sessizce geç, simülasyonu bozma
            } finally {
                // Ses başarıyla bitse de, hata verse de mutlaka kilidi geri aç ki sonraki sesler çalabilsin
                carpmaSesiCalisiyor = false;
            }
        }).start();
    }

    /**
     * Robot süpürge kirli bir hücreyi temizlerken çıkacak "bızzt" sesi.
     */
    public static void playCleanSound() {
        // Eğer ses kapalıysa VEYA zaten o an bir temizlik sesi çalıyorsa yenisini başlatma
        if (!sesAktif || temizlikSesiCalisiyor) return;

        temizlikSesiCalisiyor = true; // Ses başladı, kilidi kapat

        new Thread(() -> {
            try {
                double hz = 2000;
                int milisaniye = 50;
                byte[] buf = new byte[1];
                AudioFormat af = new AudioFormat(8000f, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                for (int i = 0; i < milisaniye * 8; i++) {
                    double angle = i / (8000f / hz) * 2.0 * Math.PI;
                    buf[0] = (byte) (Math.sin(angle) * 100.0);
                    sdl.write(buf, 0, 1);
                }
                sdl.drain();
                sdl.stop();
                sdl.close();
            } catch (Exception e) {
                // Sessizce yut
            } finally {
                // İşlem bitince kilidi aç
                temizlikSesiCalisiyor = false;
            }
        }).start();
    }

    /**
     * Robot şarj istasyonuna başarıyla oturduğunda çalacak "dındırınn" efekti.
     * Şarj sesi çok nadir (sadece istasyona dönünce 1 kere) çaldığı için kilit koymaya gerek yoktur.
     */
    public static void playChargeSound() {
        if (!sesAktif) return;
        new Thread(() -> {
            try {
                AudioFormat af = new AudioFormat(8000f, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                byte[] buf = new byte[1];
                for (int i = 0; i < 200 * 8; i++) {
                    double hz = 400 + (i / (200.0 * 8.0)) * 400; // 400Hz'den 800Hz'e frekans artışı
                    double angle = i / (8000f / hz) * 2.0 * Math.PI;
                    buf[0] = (byte) (Math.sin(angle) * 100.0);
                    sdl.write(buf, 0, 1);
                }
                sdl.drain();
                sdl.stop();
                sdl.close();
            } catch (Exception e) {
                // Hata durumunda simülasyonu etkilemesin
            }
        }).start();
    }
}