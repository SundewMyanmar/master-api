/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

/**
 * @author Htoonlin
 */
@Component
public class BarCodeManager {
    private final String FILE_TYPE = "png";

    public BarCodeManager() {
    }

    private void imageWriter(File outputFile, BitMatrix byteMatrix) throws IOException {
        int matrixWidth = byteMatrix.getWidth();
        int matrixHeight = byteMatrix.getHeight();
        //transparant need to change to argb
        //BufferedImage image = new BufferedImage(matrixWidth, matrixHeight, BufferedImage.TYPE_INT_ARGB);
        BufferedImage image = new BufferedImage(matrixWidth, matrixHeight, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();
        Graphics2D graphics = (Graphics2D) image.getGraphics();

        //transparent
        //Color myColour = new Color(255, 255, 255, 0);
        //graphics.setColor(myColour);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixHeight);
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixHeight; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        ImageIO.write(image, FILE_TYPE, outputFile);
    }

    public void createBarcode(File barcodeFile, BarcodeFormat format, String content, int width, int height)
            throws WriterException, IOException {
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        MultiFormatWriter codeWriter = new MultiFormatWriter();
        BitMatrix byteMatrix = codeWriter.encode(content, format, width, height, hintMap);
        imageWriter(barcodeFile, byteMatrix);
    }

    public void createQR(File qrFile, String content, int size) throws WriterException, IOException {
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter codeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = codeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hintMap);
        imageWriter(qrFile, byteMatrix);
    }
}
