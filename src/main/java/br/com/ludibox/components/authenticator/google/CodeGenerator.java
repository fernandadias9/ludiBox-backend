package br.com.ludibox.components.authenticator.google;


import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.SecretGenerator;
import com.bastiaanjansen.otp.TOTPGenerator;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

@Component
public class CodeGenerator {

    public BufferedImage generate(String issuer, String email, byte[] secretBytes) throws WriterException, URISyntaxException {
        Base32 base32 = new Base32();
        String secretBase32 = base32.encodeToString(secretBytes).replace("=", "");

        TOTPGenerator totp = new TOTPGenerator.Builder(secretBytes)
                .withHOTPGenerator(builder -> {
                    builder.withPasswordLength(6);
                    builder.withAlgorithm(HMACAlgorithm.SHA1);
                })
                .withPeriod(Duration.ofSeconds(30))
                .build();

        URI uri = totp.getURI(issuer, email);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(uri.toString(), BarcodeFormat.QR_CODE, 300, 300);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

}
