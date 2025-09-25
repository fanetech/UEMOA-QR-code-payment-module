package com.example.qrapi.mpos.service;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Service for handling certificate operations and mTLS
 */
@Slf4j
@Service
public class CertificateService {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Generate a self-signed X.509 certificate
     */
    public X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String cn) 
            throws OperatorCreationException, CertificateException, IOException {
        
        long now = System.currentTimeMillis();
        Date startDate = new Date(now);
        Date endDate = new Date(now + 365 * 24 * 60 * 60 * 1000L); // 1 year validity
        
        X500Name dnName = new X500Name("CN=" + cn + ", O=MPOS, C=CI");
        BigInteger certSerialNumber = new BigInteger(Long.toString(now));
        
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                dnName,
                certSerialNumber,
                startDate,
                endDate,
                dnName,
                keyPair.getPublic()
        );
        
        // Add extensions
        BasicConstraints basicConstraints = new BasicConstraints(false);
        certBuilder.addExtension(Extension.basicConstraints, true, basicConstraints);
        
        // Sign the certificate
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider("BC")
                .build(keyPair.getPrivate());
        
        X509CertificateHolder certHolder = certBuilder.build(contentSigner);
        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);
    }

    /**
     * Convert X509Certificate to PEM format
     */
    public String certificateToPEM(X509Certificate certificate) throws IOException {
        StringWriter stringWriter = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
            pemWriter.writeObject(certificate);
        }
        return stringWriter.toString();
    }

    /**
     * Convert PEM string to X509Certificate
     */
    public X509Certificate pemToCertificate(String pemString) throws Exception {
        try (PEMParser pemParser = new PEMParser(new StringReader(pemString))) {
            Object object = pemParser.readObject();
            if (object instanceof X509CertificateHolder) {
                return new JcaX509CertificateConverter()
                        .setProvider("BC")
                        .getCertificate((X509CertificateHolder) object);
            }
            throw new IllegalArgumentException("Invalid certificate PEM format");
        }
    }

    /**
     * Convert PEM string to PrivateKey (supporting both PKCS#1 and PKCS#8 formats)
     */
    public PrivateKey pemToPrivateKey(String pemString) throws Exception {
        try (PEMParser pemParser = new PEMParser(new StringReader(pemString))) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            
            if (object instanceof PEMKeyPair) {
                // PKCS#1 format
                PEMKeyPair pemKeyPair = (PEMKeyPair) object;
                return converter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());
            } else if (object instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo) {
                // PKCS#8 format
                return converter.getPrivateKey((org.bouncycastle.asn1.pkcs.PrivateKeyInfo) object);
            }
            
            throw new IllegalArgumentException("Invalid private key PEM format");
        }
    }

    /**
     * Create SSL context for mTLS
     */
    public SSLContext createMTLSContext(String clientCertPEM, String clientKeyPEM, String caCertPEM) 
            throws Exception {
        
        // Load client certificate and key
        X509Certificate clientCert = pemToCertificate(clientCertPEM);
        PrivateKey clientKey = pemToPrivateKey(clientKeyPEM);
        
        // Create keystore with client certificate
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setKeyEntry("client", clientKey, "".toCharArray(), new Certificate[]{clientCert});
        
        // Create key manager
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "".toCharArray());
        
        // Create truststore with CA certificate
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        if (caCertPEM != null && !caCertPEM.isEmpty()) {
            X509Certificate caCert = pemToCertificate(caCertPEM);
            trustStore.setCertificateEntry("ca", caCert);
        }
        
        // Create trust manager
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        
        // Create SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        
        return sslContext;
    }

    /**
     * Generate RSA key pair
     */
    public KeyPair generateRSAKeyPair(int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(keySize);
        return keyGen.generateKeyPair();
    }

    /**
     * Convert PrivateKey to PEM format
     */
    public String privateKeyToPEM(PrivateKey privateKey) throws IOException {
        StringWriter stringWriter = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
            pemWriter.writeObject(privateKey);
        }
        return stringWriter.toString();
    }

    /**
     * Convert PublicKey to PEM format
     */
    public String publicKeyToPEM(PublicKey publicKey) throws IOException {
        StringWriter stringWriter = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
            pemWriter.writeObject(publicKey);
        }
        return stringWriter.toString();
    }
}
