package com.bupt.ta.service.assistant;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 微信支付 APIv3：商户签名请求、拉取平台证书、验签与 AEAD 解密（不依赖第三方 SDK）。
 */
public final class WechatPayV3Client {

    private static final String HOST = "https://api.mch.weixin.qq.com";
    private static final String PATH_NATIVE = "/v3/pay/transactions/native";
    private static final String PATH_CERTS = "/v3/certificates";

    private final String mchid;
    private final String appid;
    private final String merchantSerial;
    private final PrivateKey merchantPrivateKey;
    private final byte[] apiV3Key;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    private final Object certLock = new Object();
    private Map<String, PublicKey> platformKeys = new HashMap<>();
    private long platformKeysLoadedAt;

    public WechatPayV3Client(String mchid, String appid, String merchantSerial,
                             PrivateKey merchantPrivateKey, byte[] apiV3Key) {
        this.mchid = mchid;
        this.appid = appid;
        this.merchantSerial = merchantSerial;
        this.merchantPrivateKey = merchantPrivateKey;
        this.apiV3Key = apiV3Key;
    }

    public static WechatPayV3Client fromConfig() throws Exception {
        String pkPath = AssistantConfig.wechatPayPrivateKeyPath();
        String pem = Files.readString(Paths.get(pkPath), StandardCharsets.UTF_8);
        PrivateKey pk = loadPrivateKeyFromPem(pem);
        byte[] keyBytes = AssistantConfig.wechatPayApiV3Key().getBytes(StandardCharsets.UTF_8);
        return new WechatPayV3Client(
                AssistantConfig.wechatPayMchId(),
                AssistantConfig.wechatPayAppId(),
                AssistantConfig.wechatPayMerchantSerial(),
                pk,
                keyBytes);
    }

    private static PrivateKey loadPrivateKeyFromPem(String pem) throws Exception {
        String normalized = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance("RSA").generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(der));
    }

    /**
     * Native 下单，返回 code_url（weixin://wxpay/...）。
     */
    public String nativePrepay(String outTradeNo, String description, int totalFen, String notifyUrl) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("appid", appid);
        body.addProperty("mchid", mchid);
        body.addProperty("description", description);
        body.addProperty("out_trade_no", outTradeNo);
        body.addProperty("notify_url", notifyUrl);
        JsonObject amount = new JsonObject();
        amount.addProperty("total", totalFen);
        amount.addProperty("currency", "CNY");
        body.add("amount", amount);
        String json = body.toString();
        String resp = post(PATH_NATIVE, json);
        JsonObject root = new JsonParser().parse(resp).getAsJsonObject();
        if (root.has("code_url") && root.get("code_url").isJsonPrimitive()) {
            return root.get("code_url").getAsString();
        }
        String err = root.has("message") ? root.get("message").getAsString() : resp;
        throw new IOException("wechat native prepay failed: " + err);
    }

    public boolean verifyNotify(String wechatpaySerial, String wechatpayTimestamp,
                                String wechatpayNonce, String body, String wechatpaySignature) throws Exception {
        ensurePlatformKeysFresh();
        PublicKey pub = platformKeys.get(wechatpaySerial);
        if (pub == null) {
            refreshPlatformCertificates();
            pub = platformKeys.get(wechatpaySerial);
        }
        if (pub == null) {
            return false;
        }
        String message = wechatpayTimestamp + "\n" + wechatpayNonce + "\n" + body + "\n";
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initVerify(pub);
        sign.update(message.getBytes(StandardCharsets.UTF_8));
        return sign.verify(Base64.getDecoder().decode(wechatpaySignature));
    }

    /**
     * 解密通知 resource 或证书 ciphertext（Base64）。
     */
    public String decryptAeadToUtf8(String associatedData, String nonce, String ciphertextBase64) throws Exception {
        byte[] adBytes = associatedData == null ? new byte[0] : associatedData.getBytes(StandardCharsets.UTF_8);
        byte[] nonceBytes = decodeNonce(nonce);
        byte[] cipherBytes = Base64.getDecoder().decode(ciphertextBase64);
        byte[] plain = decryptAesGcm(apiV3Key, adBytes, nonceBytes, cipherBytes);
        return new String(plain, StandardCharsets.UTF_8);
    }

    private static byte[] decodeNonce(String nonce) {
        if (nonce == null || nonce.isEmpty()) {
            return new byte[0];
        }
        try {
            byte[] d = Base64.getDecoder().decode(nonce);
            if (d.length > 0) {
                return d;
            }
        } catch (IllegalArgumentException ignored) {
        }
        return nonce.getBytes(StandardCharsets.UTF_8);
    }

    private void ensurePlatformKeysFresh() throws Exception {
        synchronized (certLock) {
            long now = System.currentTimeMillis();
            if (platformKeys.isEmpty() || now - platformKeysLoadedAt > 3_600_000L) {
                refreshPlatformCertificates();
            }
        }
    }

    private void refreshPlatformCertificates() throws Exception {
        String resp = get(PATH_CERTS);
        JsonObject root = new JsonParser().parse(resp).getAsJsonObject();
        JsonArray data = root.getAsJsonArray("data");
        Map<String, PublicKey> next = new HashMap<>();
        if (data != null) {
            for (JsonElement el : data) {
                JsonObject row = el.getAsJsonObject();
                String serial = row.get("serial_no").getAsString();
                JsonObject enc = row.getAsJsonObject("encrypt_certificate");
                String ad = enc.get("associated_data").getAsString();
                String nonce = enc.get("nonce").getAsString();
                String ct = enc.get("ciphertext").getAsString();
                String pem = decryptAeadToUtf8(ad, nonce, ct);
                X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509")
                        .generateCertificate(new java.io.ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8)));
                next.put(serial, cert.getPublicKey());
            }
        }
        synchronized (certLock) {
            this.platformKeys = next;
            this.platformKeysLoadedAt = System.currentTimeMillis();
        }
    }

    private String post(String path, String jsonBody) throws IOException {
        return exchange("POST", path, jsonBody);
    }

    private String get(String path) throws IOException {
        return exchange("GET", path, "");
    }

    private String exchange(String method, String path, String body) throws IOException {
        try {
            String ts = String.valueOf(System.currentTimeMillis() / 1000);
            String nonce = randomNonce();
            String signMessage = method + "\n" + path + "\n" + ts + "\n" + nonce + "\n" + body + "\n";
            String signature = sign(signMessage);

            String auth = "WECHATPAY2-SHA256-RSA2048 "
                    + "mchid=\"" + mchid + "\","
                    + "nonce_str=\"" + nonce + "\","
                    + "timestamp=\"" + ts + "\","
                    + "serial_no=\"" + merchantSerial + "\","
                    + "signature=\"" + signature + "\"";

            HttpRequest.Builder b = HttpRequest.newBuilder()
                    .uri(URI.create(HOST + path))
                    .timeout(Duration.ofSeconds(25))
                    .header("Authorization", auth)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "BUPT-TA-Recruitment/1.0");
            if ("POST".equals(method)) {
                b.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            } else {
                b.GET();
            }
            HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int code = resp.statusCode();
            if (code / 100 != 2) {
                throw new IOException("wechat http " + code + ": " + resp.body());
            }
            return resp.body();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private String sign(String message) throws Exception {
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initSign(merchantPrivateKey);
        s.update(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(s.sign());
    }

    private static String randomNonce() {
        byte[] buf = new byte[16];
        ThreadLocalRandom.current().nextBytes(buf);
        StringBuilder sb = new StringBuilder(32);
        for (byte b : buf) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] decryptAesGcm(byte[] aesKey, byte[] associatedData, byte[] nonce, byte[] ciphertext)
            throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, nonce);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        if (associatedData != null && associatedData.length > 0) {
            cipher.updateAAD(associatedData);
        }
        return cipher.doFinal(ciphertext);
    }
}
