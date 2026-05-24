package com.robotvacuum.util;

import com.robotvacuum.model.Oda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * BFS (Breadth-First Search) ile en kısa yolu bulan yardımcı sınıf.
 *
 * BFS'i şöyle düşün: başlangıç hücresinden dalga gibi etrafa yayılırız.
 * Hangi hücreye hangi sıradan ulaştığımızı bir kuyruğa yazıyoruz. Kuyrukta
 * önce eklenen önce çıkar; dolayısıyla hedefe ilk ulaştığımız yol mutlaka
 * en kısasıdır.
 *
 * Robot şarja dönerken kullanıyoruz: "bulunduğum hücreden istasyona kaç
 * kareyle giderim?" sorusunun cevabını veriyor.
 */
public class BFSYolBulucu {

    /**
     * (baslangicX, baslangicY)'den (hedefX, hedefY)'ye en kısa yolu döndürür.
     * Engelleri aşmaz; sadece geçilebilir hücreler üstünden gider.
     *
     * @return Adım adım [x, y] noktalarının listesi. Başlangıç dahil değil,
     *         hedef dahil. Yol bulunamazsa boş liste döner.
     */
    public static List<int[]> yolBul(Oda oda, int baslangicX, int baslangicY,
                                     int hedefX, int hedefY) {
        // 1) Zaten hedefteyiz: gidecek yol yok
        if (baslangicX == hedefX && baslangicY == hedefY) {
            return Collections.emptyList();
        }

        int sutun = oda.getSutunSayisi();
        int satir = oda.getSatirSayisi();

        // 2) "Bu hücreye uğradım mı?" ve "buraya hangi hücreden geldim?" bilgilerini tut
        boolean[][] ziyaretEdildi = new boolean[sutun][satir];
        int[][] geldigiX = new int[sutun][satir];
        int[][] geldigiY = new int[sutun][satir];
        for (int[] satirDizisi : geldigiX) Arrays.fill(satirDizisi, -1);
        for (int[] satirDizisi : geldigiY) Arrays.fill(satirDizisi, -1);

        // 3) BFS kuyruğu: hücreler keşfedildikçe buraya eklenir
        Queue<int[]> kuyruk = new LinkedList<>();
        kuyruk.add(new int[]{baslangicX, baslangicY});
        ziyaretEdildi[baslangicX][baslangicY] = true;

        // Komşu hücreler için 4 yön: kuzey, doğu, güney, batı
        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        boolean hedefBulundu = false;

        // 4) Kuyruk boşalana kadar veya hedefi bulana kadar arama
        while (!kuyruk.isEmpty()) {
            int[] mevcut = kuyruk.poll();
            int mx = mevcut[0];
            int my = mevcut[1];

            // Hedefe geldik mi?
            if (mx == hedefX && my == hedefY) {
                hedefBulundu = true;
                break;
            }

            // 5) 4 komşu hücreye bak
            for (int d = 0; d < 4; d++) {
                int yeniX = mx + dx[d];
                int yeniY = my + dy[d];

                if (!oda.sinirlarIcindeMi(yeniX, yeniY)) continue;   // ızgara dışı
                if (ziyaretEdildi[yeniX][yeniY]) continue;           // zaten gördük
                // Engelse atla. (Tek istisna: hedef hücre zaten istasyon olabilir
                // ve istasyona ulaşabilmemiz lazım.)
                if (!oda.gecilebilirMi(yeniX, yeniY)
                        && !(yeniX == hedefX && yeniY == hedefY)) continue;

                // Komşuyu ziyaret edildi olarak işaretle, nereden geldiğini kaydet
                ziyaretEdildi[yeniX][yeniY] = true;
                geldigiX[yeniX][yeniY] = mx;
                geldigiY[yeniX][yeniY] = my;
                kuyruk.add(new int[]{yeniX, yeniY});
            }
        }

        // 6) Hedefe ulaşamadıysak boş yol döndür
        if (!hedefBulundu) return Collections.emptyList();

        // 7) Hedeften geriye doğru "nereden geldim" zincirini takip et,
        //    böylece yolu adım adım inşa ederiz
        List<int[]> yol = new ArrayList<>();
        int gx = hedefX;
        int gy = hedefY;
        while (!(gx == baslangicX && gy == baslangicY)) {
            yol.add(0, new int[]{gx, gy});  // listenin başına ekle (sıra düzelsin)
            int px = geldigiX[gx][gy];
            int py = geldigiY[gx][gy];
            gx = px;
            gy = py;
        }
        return yol;
    }
}
