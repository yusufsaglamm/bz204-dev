package com.robotvacuum.model;

/**
 * Odadaki her bir küçük kareyi (hücreyi) temsil eder.
 * Bir hücre boş (zemin) olabilir, üzerinde engel (mobilya) bulunabilir 
 * veya şarj istasyonu olarak ayarlanmış olabilir. Tabii bir de üstünde kir barındırabilir.
 */
public class Hucre {

    public enum HucreTipi {
        ZEMIN,
        ENGEL,
        SARJ_ISTASYONU
    }

    private final int x;
    private final int y;
    private HucreTipi tip;
    private KirTipi kirTipi;
    
    // Kirin tamamen temizlenmesi için robotun üstünde kaç adım durması gerektiğini tutar
    private int kalanTemizlikAdimi;
    private boolean temizlendi;

    public Hucre(int x, int y) {
        this.x = x;
        this.y = y;
        this.tip = HucreTipi.ZEMIN;
        this.kirTipi = null;
        this.kalanTemizlikAdimi = 0;
        this.temizlendi = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public HucreTipi getTip() { return tip; }
    public void setTip(HucreTipi tip) { this.tip = tip; }

    // Kolaylık olsun diye engeli ve istasyonu sorgulayan ufak metotlar
    public boolean engelMi() { return tip == HucreTipi.ENGEL; }
    public boolean sarjIstasyonuMu() { return tip == HucreTipi.SARJ_ISTASYONU; }

    // Üzerinde aktif temizlenmesi gereken bir kir var mı?
    public boolean kirliMi() { return kirTipi != null && kalanTemizlikAdimi > 0; }

    public KirTipi getKirTipi() { return kirTipi; }

    public void setKir(KirTipi kirTipi) {
        // Engellerin veya şarj istasyonunun üzerine kir eklenmesine izin vermiyoruz tabii ki
        if (tip == HucreTipi.ENGEL || tip == HucreTipi.SARJ_ISTASYONU) return;
        this.kirTipi = kirTipi;
        this.kalanTemizlikAdimi = kirTipi.getTemizlikAdimi();
        this.temizlendi = false;
    }

    public void kiriTemizle() {
        this.kirTipi = null;
        this.kalanTemizlikAdimi = 0;
    }

    /**
     * Robot bu hücreye her bastığında bir temizlik adımı uygular.
     * Kir tamamen yok olduysa true döner, yoksa devam etmesi için false döner.
     */
    public boolean temizle() {
        if (!kirliMi()) {
            temizlendi = true;
            return true;
        }
        kalanTemizlikAdimi--;
        if (kalanTemizlikAdimi <= 0) {
            kirTipi = null;
            kalanTemizlikAdimi = 0;
            temizlendi = true;
            return true;
        }
        return false;
    }

    public int getKalanTemizlikAdimi() { return kalanTemizlikAdimi; }

    public boolean temizlendiMi() { return temizlendi; }
    public void setTemizlendi(boolean temizlendi) { this.temizlendi = temizlendi; }

    @Override
    public String toString() {
        return "Hucre(" + x + "," + y + ") tip=" + tip + " kir=" + kirTipi;
    }
}
