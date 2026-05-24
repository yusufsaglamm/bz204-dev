package com.robotvacuum.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Robot süpürgeyi temsil eden ana model sınıfı.
 * Robotun konumunu, yönünü, bataryasını ve gezdiği yolun geçmişini yönetir.
 * Şarj olma ve istasyona dönme gibi durum bilgilerini de takip eder.
 */
public class Robot {

    /** Robotun maksimum batarya kapasitesi (yüzde olarak) */
    public static final double MAKS_BATARYA = 100.0;

    /** Düşük batarya eşik değeri - bu değerin altında robot istasyona döner */
    public static final double DUSUK_BATARYA_ESIGI = 20.0;

    /** Bir hareket adımının temel batarya maliyeti */
    public static final double HAREKET_BATARYA_MALIYETI = 0.5;

    /** Robotun ızgaradaki mevcut X koordinatı (sütun) */
    private int x;

    /** Robotun ızgaradaki mevcut Y koordinatı (satır) */
    private int y;

    /** Robotun bakış yönü */
    private Yon yon;

    /** Robotun mevcut batarya seviyesi (0.0 - 100.0) */
    private double batarya;

    /** Robot şu anda şarj oluyor mu? */
    private boolean sarjOluyor;

    /** Robot şarj istasyonuna geri dönüş yolunda mı? */
    private boolean istasyonaDonuyor;

    /** Aktif temizlik adımı sayacı */
    private int temizlikAdimSayisi;

    /** Robotun gezdiği yolun geçmişi (çizim için kullanılır) */
    private final List<int[]> yolGecmisi;

    /**
     * Belirtilen başlangıç koordinatlarında yeni bir robot oluşturur.
     * Robot başlangıçta tam dolu bataryaya sahiptir ve doğu yönüne bakar.
     *
     * @param baslangicX Robotun başlangıç X koordinatı (sütun)
     * @param baslangicY Robotun başlangıç Y koordinatı (satır)
     */
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

    /** @return Robotun mevcut X koordinatı */
    public int getX() { return x; }

    /** @return Robotun mevcut Y koordinatı */
    public int getY() { return y; }

    /**
     * Robotun konumunu günceller ve yol geçmişine yeni konumu ekler.
     * Bellek kullanımını sınırlamak için yol geçmişi 500 noktayla sınırlandırılır.
     *
     * @param x Yeni X koordinatı
     * @param y Yeni Y koordinatı
     */
    public void konumAyarla(int x, int y) {
        this.x = x;
        this.y = y;
        yolGecmisi.add(new int[]{x, y});
        // Bellek sorunlarını önlemek için yol geçmişini sınırlı tut
        if (yolGecmisi.size() > 500) {
            yolGecmisi.remove(0);
        }
    }

    /** @return Robotun bakış yönü */
    public Yon getYon() { return yon; }

    /**
     * Robotun bakış yönünü değiştirir.
     *
     * @param yon Yeni yön
     */
    public void setYon(Yon yon) { this.yon = yon; }

    /** @return Robotun mevcut batarya seviyesi (0.0 - 100.0) */
    public double getBatarya() { return batarya; }

    /**
     * Robotun bataryasını belirli bir değere ayarlar.
     * Değer [0, MAKS_BATARYA] aralığında sınırlandırılır.
     *
     * @param batarya Yeni batarya seviyesi
     */
    public void setBatarya(double batarya) {
        this.batarya = Math.max(0, Math.min(MAKS_BATARYA, batarya));
    }

    /**
     * Bataryadan belirtilen miktarı düşer.
     * Batarya 0'ın altına düşemez.
     *
     * @param miktar Tüketilecek batarya miktarı
     */
    public void bataryaTuket(double miktar) {
        this.batarya = Math.max(0, this.batarya - miktar);
    }

    /**
     * Bataryayı belirtilen miktarda doldurur.
     * Batarya maksimum kapasitenin üstüne çıkamaz.
     *
     * @param miktar Eklenecek batarya miktarı
     */
    public void bataryaSarjEt(double miktar) {
        this.batarya = Math.min(MAKS_BATARYA, this.batarya + miktar);
    }

    /** @return Batarya seviyesi düşük eşik değerinin altında mı? */
    public boolean bataryaDusukMu() {
        return batarya <= DUSUK_BATARYA_ESIGI;
    }

    /** @return Batarya tamamen bitti mi? */
    public boolean bataryaBosMu() {
        return batarya <= 0;
    }

    /** @return Robot şu anda şarj oluyor mu? */
    public boolean sarjOluyorMu() { return sarjOluyor; }

    /**
     * Robotun şarj olma durumunu değiştirir.
     *
     * @param sarjOluyor true ise robot şarj olmaya başlar
     */
    public void setSarjOluyor(boolean sarjOluyor) { this.sarjOluyor = sarjOluyor; }

    /** @return Robot istasyona dönüş yolunda mı? */
    public boolean istasyonaDonuyorMu() { return istasyonaDonuyor; }

    /**
     * Robotun istasyona dönüş durumunu ayarlar.
     *
     * @param istasyonaDonuyor true ise robot istasyona dönmeye başlar
     */
    public void setIstasyonaDonuyor(boolean istasyonaDonuyor) {
        this.istasyonaDonuyor = istasyonaDonuyor;
    }

    /** @return Aktif temizlik adımı sayısı */
    public int getTemizlikAdimSayisi() { return temizlikAdimSayisi; }

    /** Temizlik adım sayacını bir artırır */
    public void temizlikAdimiArtir() { temizlikAdimSayisi++; }

    /** Temizlik adım sayacını sıfırlar */
    public void temizlikAdimiSifirla() { temizlikAdimSayisi = 0; }

    /** @return Robotun gezdiği yolun geçmişi (her eleman [x, y] dizisi) */
    public List<int[]> getYolGecmisi() { return yolGecmisi; }

    /**
     * Yol geçmişini temizler ve sadece mevcut konumu içerecek şekilde sıfırlar.
     */
    public void yolGecmisiniTemizle() {
        yolGecmisi.clear();
        yolGecmisi.add(new int[]{x, y});
    }

    /** @return Batarya seviyesi yüzde olarak (0.0 - 100.0) */
    public double getBataryaYuzdesi() {
        return (batarya / MAKS_BATARYA) * 100.0;
    }

    /**
     * Robotu başlangıç durumuna döndürür.
     * Konum, yön, batarya ve tüm durum bilgileri sıfırlanır.
     *
     * @param baslangicX Sıfırlama sonrası X koordinatı
     * @param baslangicY Sıfırlama sonrası Y koordinatı
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
