package com.robotvacuum.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Simülasyonun göz bebeği, robot süpürgemiz.
 * Kendi konumunu (x, y), baktığı yönü, bataryasını ve geçtiği yolların geçmişini yönetir.
 */
public class Robot {

    // Robotun batarya sınırları ve maliyetleri
    public static final double MAKS_BATARYA = 100.0;
    public static final double DUSUK_BATARYA_ESIGI = 20.0;
    public static final double HAREKET_BATARYA_MALIYETI = 0.5;

    private int x;
    private int y;
    private Yon yon;
    private double batarya;
    private boolean sarjOluyor;
    private boolean istasyonaDonuyor;
    private int temizlikAdimSayisi;

    // Robotun geçtiği karelerin geçmişi (ekranda mavi kesikli çizgiyle çizilen yol izi için)
    private final List<int[]> yolGecmisi;

    public Robot(int baslangicX, int baslangicY) {
        this.x = baslangicX;
        this.y = baslangicY;
        this.yon = Yon.DOGU; // Varsayılan olarak doğuya doğru baksın
        this.batarya = MAKS_BATARYA;
        this.sarjOluyor = false;
        this.istasyonaDonuyor = false;
        this.temizlikAdimSayisi = 0;
        this.yolGecmisi = new LinkedList<>();
        yolGecmisi.add(new int[]{baslangicX, baslangicY});
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void setKonum(int x, int y) {
        this.x = x;
        this.y = y;
        yolGecmisi.add(new int[]{x, y});
        // Belleği şişirmemek için yol geçmişini maksimum 500 adımda sınırlandırıyoruz
        if (yolGecmisi.size() > 500) {
            yolGecmisi.remove(0);
        }
    }

    public Yon getYon() { return yon; }
    public void setYon(Yon yon) { this.yon = yon; }

    public double getBatarya() { return batarya; }
    public void setBatarya(double batarya) {
        this.batarya = Math.max(0, Math.min(MAKS_BATARYA, batarya));
    }

    /**
     * Robot hareket ettikçe veya temizlik yaptıkça bataryasını buradan azaltıyoruz.
     */
    public void bataryayiTuket(double miktar) {
        this.batarya = Math.max(0, this.batarya - miktar);
    }

    /**
     * Şarj istasyonuna oturunca bataryasını buradan dolduruyoruz.
     */
    public void bataryayiSarjEt(double miktar) {
        this.batarya = Math.min(MAKS_BATARYA, this.batarya + miktar);
    }

    /**
     * Batarya düşük mü kontrolü. Eşik değerin (20.0) altına inerse istasyona kaçacak.
     */
    public boolean bataryaDusukMu() {
        return batarya <= DUSUK_BATARYA_ESIGI;
    }

    /**
     * Robotun şarjı tamamen bitti mi? Bitince ortada kalıyor tabii.
     */
    public boolean bataryaBitmisMi() {
        return batarya <= 0;
    }

    public boolean sarjOluyorMu() { return sarjOluyor; }
    public void setSarjOluyor(boolean sarjOluyor) { this.sarjOluyor = sarjOluyor; }

    public boolean istasyonaDonuyorMu() { return istasyonaDonuyor; }
    public void setIstasyonaDonuyor(boolean istasyonaDonuyor) {
        this.istasyonaDonuyor = istasyonaDonuyor;
    }

    public int getTemizlikAdimSayisi() { return temizlikAdimSayisi; }
    public void temizlikAdiminiArttir() { temizlikAdimSayisi++; }
    public void temizlikAdiminiSifirla() { temizlikAdimSayisi = 0; }

    public List<int[]> getYolGecmisi() { return yolGecmisi; }

    public void yolGecmisiniTemizle() {
        yolGecmisi.clear();
        yolGecmisi.add(new int[]{x, y});
    }

    /**
     * Arayüzdeki pil çubuğu yüzdesel çalıştığı için yüzdeyi veren ufak bir metot
     */
    public double getBataryaYuzdesi() {
        return (batarya / MAKS_BATARYA) * 100.0;
    }

    /**
     * Simülasyon sıfırlandığında robotu da ilk doğduğu haline getiriyoruz.
     */
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
