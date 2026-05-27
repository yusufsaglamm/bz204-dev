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

    public static void setSesAktif(boolean aktif) {
        sesAktif = aktif;
    }

    /**
     * Robot duvara veya bir engele çarptığında çalacak "küt" ses efekti.
     * Frekansı yüksekten alçağa doğru hızlıca düşürerek çarpma hissi yaratıyoruz.
     */
    public static void playBumpSound() {
        if (!sesAktif) return;
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
            }
        }).start();
    }

    /**
     * Robot süpürge kirli bir hücreyi temizlerken çıkacak "bızzt" sesi.
     */
    public static void playCleanSound() {
        if (!sesAktif) return;
        sesCal(2000, 50); // 2000 Hz frekansta 50 milisaniye çal
    }

    /**
     * Robot şarj istasyonuna başarıyla oturduğunda çalacak "dındırınn" efekti.
     * Frekansı alçaktan yükseğe doğru süpürerek (sweep) pozitif bir bildirim sesi veririz.
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

    /**
     * Belirli frekanstaki ve milisaniyedeki sinüs ses dalgasını çalar.
     */
    private static void sesCal(double hz, int milisaniye) {
        new Thread(() -> {
            try {
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
            }
        }).start();
    }
}
