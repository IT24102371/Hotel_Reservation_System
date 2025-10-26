package com.hotel.eventreservation.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Component
public class QRCodeGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(QRCodeGenerator.class);
    
    @Value("${app.qr.size:300}")
    private int qrCodeSize;
    
    @Value("${app.qr.base-url:http://localhost:8080}")
    private String baseUrl;
    
    /**
     * Generate QR code as Base64 encoded string
     * @param referenceCode The booking reference code
     * @return Base64 encoded QR code image
     */
    public String generateQRCodeBase64(String referenceCode) {
        try {
            String qrData = baseUrl + "/verify-booking?ref=" + referenceCode;
            return generateQRCodeBase64(qrData, qrCodeSize, qrCodeSize);
        } catch (Exception e) {
            logger.error("Error generating QR code for reference: {}", referenceCode, e);
            return null;
        }
    }
    
    /**
     * Generate QR code as Base64 encoded string with custom dimensions
     * @param data The data to encode in QR code
     * @param width QR code width
     * @param height QR code height
     * @return Base64 encoded QR code image
     */
    public String generateQRCodeBase64(String data, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            byte[] imageBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
            
        } catch (WriterException | IOException e) {
            logger.error("Error generating QR code for data: {}", data, e);
            return null;
        }
    }
    
    /**
     * Generate QR code as byte array
     * @param data The data to encode in QR code
     * @param width QR code width
     * @param height QR code height
     * @return QR code as byte array
     */
    public byte[] generateQRCodeBytes(String data, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return outputStream.toByteArray();
            
        } catch (WriterException | IOException e) {
            logger.error("Error generating QR code for data: {}", data, e);
            return null;
        }
    }
}
