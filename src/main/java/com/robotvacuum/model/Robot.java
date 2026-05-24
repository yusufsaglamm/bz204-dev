package com.robotvacuum.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Robot süpürgenin durumunu tutan model sınıfı:
 * konum, yön, batarya, şarj durumu ve gezdiği yol.
 *
 * Not (OOP - Kapsülleme): Tüm alanlar private. Batarya gibi kritik değerler
 * için "ham" setter yerine bataryaTuket / bataryaSarjEt gibi metotlar
 * kullandık. Bu sayede batarya negatife düşmesin veya 100'ü geçmesin diye
 * sınırı tek bir yerde koruyoruz.
 */
public class Robot {

    public static final double MAKS_BATARYA = 100.0;
    public static final double DUSUK_BATARYA_ESIGI = 20.0;   // Bu seviyenin altında otomatik istasyona dön
    public static final double HAREKET_BATARYA_MALIYETI = 0.5;

    private int x;
    private int y;
    private Yon yon;
    private double batarya;
    private boolean sarjOluyor;
    private boolean istasyonaDonuyor;
    private int temizlikAdimSayisi;

    // Robotun gezdiği yol (UI'da iz olarak çizilir)
    private final List<int[]> yolGecmisi;

    public Robot(int baslangicX, int baslangicY) {
        this.x = baslangicX;
        this.y = baslangicY;
        this.yon = Yon.DOGU;
        this.batarya = MAKS_BATARYA;
        this.sarjOluyor = false;
        this.istasyonaDonuyor = false;
        this.temizlikAdimSayisi = 0;
        this.yolGecmisi = new ArrayList<>();
        yolGecmisi.add(new int[]{baslangicX, baslangicY});
    }

    public int getX() { return x; }
    public int getY() { return y; }

    /**
     * Robotu yeni konuma taşır ve geçmişe ekler.
     * Liste çok büyümesin diye 500 adımdan eskileri atıyoruz.
     */
    public void konumAyarla(int x, int y) {
        this.x = x;
        this.y = y;
        yolGecmisi.add(new int[]{x, y});
        if (yolGecmisi.size() > 500) {
            yolGecmisi.remove(0);
        }
    }

    public Yon getYon() { return yon; }
    public void setYon(Yon yon) { this.yon = yon; }

    public double getBatarya() { return batarya; }

    /** Bataryayı 0-100 aralığında tutarak ayarlar. */
    public void setBatarya(double batarya) {
        if (batarya < 0) batarya = 0;
        if (batarya > MAKS_BATARYA) batarya = MAKS_BATARYA;
        this.batarya = batarya;
    }

    /** Bataryadan harcanan kadar düş. 0'ın altına inmesine izin verme. */
    public void bataryaTuket(double miktar) {
        this.batarya -= miktar;
        if (this.batarya < 0) this.batarya = 0;
    }

    /** Bataryayı doldur ama 100'ü geçme. */
    public void bataryaSarjEt(double miktar) {
        this.batarya += miktar;
        if (this.batarya > MAKS_BATARYA) this.batarya = MAKS_BATARYA;
    }

    public boolean bataryaDusukMu() { return batarya <= DUSUK_BATARYA_ESIGI; }
    public boolean bataryaBosMu() { return batarya <= 0; }

    public boolean sarjOluyorMu() { return sarjOluyor; }
    public void setSarjOluyor(boolean sarjOluyor) { this.sarjOluyor = sarjOluyor; }

    public boolean istasyonaDonuyorMu() { return istasyonaDonuyor; }
    public void setIstasyonaDonuyor(boolean istasyonaDonuyor) { this.istasyonaDonuyor = istasyonaDonuyor; }

    public int getTemizlikAdimSayisi() { return temizlikAdimSayisi; }
    public void temizlikAdimiArtir() { temizlikAdimSayisi++; }
    public void temizlikAdimiSifirla() { temizlikAdimSayisi = 0; }

    public List<int[]> getYolGecmisi() { return yolGecmisi; }

    public void yolGecmisiniTemizle() {
        yolGecmisi.clear();
        yolGecmisi.add(new int[]{x, y});
    }

    /** Robotu başlangıç durumuna geri al. */
    public void sifirla(int baslangicX, int baslangicY) {
        this.x = baslangicX;
        this.y = baslangicY;
        this.yon = Yon.DOGU;
        this.batarya = MAKS_BATARYA;
        this.sarjOluyor = false;
        this.istasyonaDonuyor = false;
        this.temizlikAdimSayisi = 0;
        yolGecmisiniTemizle();
    }
}
