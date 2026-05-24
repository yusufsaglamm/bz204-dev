package com.robotvacuum.util;

import com.robotvacuum.model.Oda;

import java.util.*;

/**
 * BFS (Breadth-First Search / Genişlik Öncelikli Arama) algoritmasını kullanan
 * yardımcı yol bulma sınıfı.
 *
 * <p>Bu algoritma, ızgara üzerindeki iki nokta arasındaki <b>en kısa yolu</b>
 * (engellere takılmadan) bulmak için kullanılır. Robot süpürgenin şarj istasyonuna
 * en hızlı şekilde geri dönmesini sağlar.</p>
 *
 * <p><b>BFS Çalışma Prensibi:</b><br>
 * 1) Başlangıç noktasını kuyruğa ekle ve ziyaret edildi olarak işaretle.<br>
 * 2) Kuyruktan bir hücre çıkar; bu hücre hedefse arama biter.<br>
 * 3) Aksi halde komşu hücreleri (kuzey, doğu, güney, batı) kontrol et.<br>
 * 4) Henüz ziyaret edilmemiş ve geçilebilir komşu hücreleri kuyruğa ekle.<br>
 * 5) Her komşunun parent (üst) bilgisini sakla ki yol geri inşa edilebilsin.<br>
 * 6) Hedefe ulaşıldığında parent bilgileriyle yolu geri inşa et.</p>
 */
public class BFSYolBulucu {

    /**
     * (baslangicX, baslangicY) noktasından (hedefX, hedefY) noktasına en kısa yolu bulur.
     * Engelleri (mobilyaları) aşamaz, sadece geçilebilir hücreler üzerinden ilerler.
     *
     * @param oda         Üzerinde arama yapılacak oda nesnesi
     * @param baslangicX  Başlangıç X koordinatı (sütun)
     * @param baslangicY  Başlangıç Y koordinatı (satır)
     * @param hedefX      Hedef X koordinatı (sütun)
     * @param hedefY      Hedef Y koordinatı (satır)
     * @return Başlangıç noktası hariç, hedefe kadar (hedef dahil) sıralı
     *         [x, y] adımlarının listesi. Yol yoksa boş liste döner.
     */
    public static List<int[]> yolBul(Oda oda, int baslangicX, int baslangicY, int hedefX, int hedefY) {
        // Başlangıç ve hedef aynıysa boş liste döndür (hareket gerekmiyor)
        if (baslangicX == hedefX && baslangicY == hedefY) return Collections.emptyList();

        int sutunSayisi = oda.getSutunSayisi();
        int satirSayisi = oda.getSatirSayisi();

        // Ziyaret edilen hücreleri ve her hücrenin parent (üst) hücresini takip eden diziler
        boolean[][] ziyaretEdildi = new boolean[sutunSayisi][satirSayisi];
        int[][] parentX = new int[sutunSayisi][satirSayisi];
        int[][] parentY = new int[sutunSayisi][satirSayisi];

        // Parent dizilerini -1 ile başlat (henüz ziyaret edilmedi anlamında)
        for (int[] satir : parentX) Arrays.fill(satir, -1);
        for (int[] satir : parentY) Arrays.fill(satir, -1);

        // BFS için FIFO kuyruğu
        Queue<int[]> kuyruk = new LinkedList<>();
        kuyruk.add(new int[]{baslangicX, baslangicY});
        ziyaretEdildi[baslangicX][baslangicY] = true;

        // 4 ana yön için dx, dy yer değiştirme vektörleri (kuzey, doğu, güney, batı)
        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        boolean bulundu = false;

        // BFS ana döngüsü: kuyruk boşalana veya hedef bulunana kadar devam et
        while (!kuyruk.isEmpty()) {
            int[] mevcut = kuyruk.poll();
            int mevcutX = mevcut[0], mevcutY = mevcut[1];

            // Hedefe ulaştıysak aramayı sonlandır
            if (mevcutX == hedefX && mevcutY == hedefY) {
                bulundu = true;
                break;
            }

            // 4 yöndeki komşu hücreleri kontrol et
            for (int d = 0; d < 4; d++) {
                int yeniX = mevcutX + dx[d];
                int yeniY = mevcutY + dy[d];

                // Sınırlar dışındaki hücreleri atla
                if (!oda.sinirlarIcindeMi(yeniX, yeniY)) continue;
                // Zaten ziyaret edilmiş hücreleri atla
                if (ziyaretEdildi[yeniX][yeniY]) continue;
                // Engel ise atla (ancak hedef noktasına ulaşabiliriz - şarj istasyonu gibi)
                if (!oda.gecilebilirMi(yeniX, yeniY) && !(yeniX == hedefX && yeniY == hedefY)) continue;

                // Hücreyi ziyaret edildi olarak işaretle ve parent bilgisini kaydet
                ziyaretEdildi[yeniX][yeniY] = true;
                parentX[yeniX][yeniY] = mevcutX;
                parentY[yeniX][yeniY] = mevcutY;
                kuyruk.add(new int[]{yeniX, yeniY});
            }
        }

        // Hedef bulunamadıysa boş yol döndür
        if (!bulundu) return Collections.emptyList();

        // Hedeften başlayarak parent zincirini takip et ve yolu geri inşa et
        List<int[]> yol = new ArrayList<>();
        int gx = hedefX, gy = hedefY;
        while (!(gx == baslangicX && gy == baslangicY)) {
            // Adımı listenin başına ekle (sırasıyla geriye doğru inşa ediyoruz)
            yol.add(0, new int[]{gx, gy});
            int px = parentX[gx][gy];
            int py = parentY[gx][gy];
            gx = px;
            gy = py;
        }
        return yol;
    }
}
