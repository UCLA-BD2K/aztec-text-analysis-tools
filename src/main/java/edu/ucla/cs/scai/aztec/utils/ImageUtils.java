/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.aztec.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class ImageUtils {

    Pattern pattern = Pattern.compile("data:image/(.*);base64,");

    public double histogramSimilarity(double[] h1, double[] h2) {
        double sqrErr = 0;
        for (int i = 0; i < h1.length; i++) {
            sqrErr += Math.pow(h1[i] - h2[i], 2);
        }
        return 1 - Math.sqrt(sqrErr);
    }

    public byte[][] imgToBytes(BufferedImage img) {
        int c = img.getWidth();
        int r = img.getHeight();
        byte[][] res = new byte[r][c];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                int pixel = img.getRGB(j, i);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = (pixel) & 0xff;
                res[i][j] = (byte) (0.5 + 0.114 * blue + 0.587 * green + 0.299 * red);
            }
        }
        return res;
    }

    public byte[][] loadImageFromURL(String s) throws IOException {
        BufferedImage img = ImageIO.read(new URL(s));
        return imgToBytes(img);
    }

    public byte[][] loadImageFromBase64(String s) throws Exception {
        Matcher m = pattern.matcher(s);
        if (m.find()) {
            String type = m.group(1);
            String sbstr = type + ";base64,";
            s = s.substring(s.indexOf(sbstr) + sbstr.length());
            InputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(s));
            BufferedImage img = ImageIO.read(stream);
            return imgToBytes(img);
        }
        return null;
    }

    public byte[][] loadImage(String s) throws Exception {
        if (s.startsWith("http://") || s.startsWith("https://")) {
            return loadImageFromURL(s);
        }
        if (s.startsWith("data:image")) {
            return loadImageFromBase64(s);
        }
        return null;
    }

    public double[] histogramFromImage(String s) throws Exception {
        byte[][] img = loadImage(s);
        return histogramFromImage(img);
    }

    public double[] histogramFromImage(byte[][] img) {
        byte[][] tiled = img; //tile(img);
        equalize(tiled);
        double[] res = new double[16];
        if (tiled.length > 0) {
            double c = 1.0 / (tiled.length * tiled[0].length);
            for (int i = 0; i < tiled.length; i++) {
                for (int j = 0; j < tiled[0].length; j++) {
                    res[(img[i][j] & 0xFF) >> 4] += c;
                }
            }
        }
        return res;
    }

    public byte[][] tile(byte[][] img) {
        //remove all the pixel of the border with the same value
        byte val = img[0][0];
        int i = 0;
        boolean ok = true;
        while (ok && i < img.length) {
            for (int j = 0; j < img[0].length; j++) {
                if (img[i][j] != val) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                i++;
            }
        }
        if (i == img.length) { //it was an empty image
            return new byte[0][0];
        }
        int infI = i; //previous rows are all the same color
        i = img.length - 1;
        ok = true;
        while (ok && i > infI) {
            for (int j = 0; j < img[0].length; j++) {
                if (img[i][j] != val) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                i--;
            }
        }
        int supI = i; //following rows are all the same color
        int j = 0;
        ok = true;
        while (ok && j < img[0].length) {
            for (i = infI; i <= supI; i++) {
                if (img[i][j] != val) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                j++;
            }
        }
        int infJ = j; //previous columns are all the same color
        j = img[0].length - 1;
        while (ok && j > infJ) {
            for (i = infI; i <= supI; i++) {
                if (img[i][j] != val) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                j--;
            }
        }
        int supJ = j;
        //now copy [infI..supI][infJ..supJ]
        byte[][] res = new byte[supI - infJ + 1][supJ - infJ + 1];
        for (i = 0; i < res.length; i++) {
            for (j = 0; j < res[0].length; j++) {
                res[i][j] = img[infI + i][infJ + j];
            }
        }
        return res;
    }

    public void equalize(byte[][] img) {
        int r = img.length;
        int c = img[0].length;
        int[] hist = new int[256];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                hist[img[i][j] & 0xFF]++;
            }
        }
        int min = hist[0];
        for (int i = 1; i < 256; i++) {
            hist[i & 0xFF] += hist[(i - 1) & 0xFF];
            if (min == 0) {
                min = hist[i & 0xFF];
            }
        }
        int max = r * c;
        int delta = max - min;
        if (delta == 0) { //the equalization is not possibile: all the colors are the same
            byte color = (img[0][0] > (byte) 0) ? (byte) 0 : (byte) 255; //img[0]<0 -> img[0] & 0xFF > 127
            for (int i = 0; i < r; i++) {
                for (int j = 0; j < c; j++) {
                    img[i][j] = color;
                }
            }
        } else {
            double normalization = 255.0 / delta;
            for (int i = 0; i < r; i++) {
                for (int j = 0; j < c; j++) {
                    img[i][j] = (byte) (0.5 + ((hist[img[i][j] & 0xFF]) - min) * normalization);
                }
            }
        }
    }

}
