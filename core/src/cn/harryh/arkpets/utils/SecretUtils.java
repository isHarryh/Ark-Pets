/** Copyright (c) 2022-2026, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;


public class SecretUtils {
    public interface SecretAlgorithm {
        String encrypt(String plaintext) throws GeneralSecurityException;

        String decrypt(String encoded) throws GeneralSecurityException;
    }


    public static class WeakEncryptionV0 implements SecretAlgorithm {
        private static final Charset charset = StandardCharsets.UTF_8;
        private static final int MAX_KEY_CANDIDATES = 64;

        public static class DeviceBindingKeyGenerator implements Iterator<String> {
            private final HashMap<NetworkInterface, Byte> niScoreMap;
            private final Iterator<NetworkInterface> niIter;
            private final String hostname;

            public DeviceBindingKeyGenerator() {
                this.niScoreMap = new HashMap<>();

                ArrayList<NetworkInterface> niList;
                try {
                    niList = Collections.list(NetworkInterface.getNetworkInterfaces());
                    niList.sort(Comparator.comparingInt(ni -> scoreNetworkInterface((NetworkInterface) ni)).reversed());
                } catch (Exception e) {
                    niList = new ArrayList<>();
                }
                niList.add(null);
                this.niIter = niList.iterator();

                String hostname;
                try {
                    hostname = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    hostname = "unknown_hostname";
                }
                this.hostname = hostname;
            }

            private byte scoreNetworkInterface(NetworkInterface ni) {
                if (ni == null)
                    return 0;
                if (niScoreMap.containsKey(ni))
                    return niScoreMap.get(ni);

                byte score;
                try {
                    score = (byte) (1 + (ni.isUp() ? 1 : 0) + (ni.isLoopback() ? 0 : 1) + (ni.isVirtual() ? 0 : 1));
                } catch (SocketException e) {
                    score = 0;
                }
                niScoreMap.put(ni, score);
                return score;
            }

            private static String formatMacAddress(NetworkInterface ni) {
                if (ni == null)
                    return null;

                try {
                    byte[] mac = ni.getHardwareAddress();
                    if (mac == null)
                        return null;
                    StringBuilder sb = new StringBuilder();
                    for (byte b : mac)
                        sb.append(String.format("%02X:", b));
                    return sb.substring(0, sb.length() - 1);
                } catch (SocketException e) {
                    return null;
                }
            }

            @Override
            public boolean hasNext() {
                return niIter.hasNext();
            }

            @Override
            public String next() {
                if (!hasNext())
                    throw new NoSuchElementException();

                String macAddress = formatMacAddress(niIter.next());
                if (macAddress == null)
                    macAddress = "unknown_mac";

                return System.getProperty("user.name") + ","
                        + System.getProperty("user.home") + ";"
                        + hostname + ","
                        + macAddress + ";"
                        + System.getProperty("os.name") + ","
                        + System.getProperty("os.arch") + ";";
            }
        }

        private static SecretKey generateKey(String passphrase, byte[] salt) {
            int iters = 65536;
            int keyLength = 256;
            SecretKey key;
            try {
                KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, iters, keyLength);
                key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec);
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            return new SecretKeySpec(key.getEncoded(), "AES");
        }

        @Override
        public String encrypt(String plaintext) throws GeneralSecurityException {
            try {
                byte[] salt = SecureRandom.getInstanceStrong().generateSeed(16);
                byte[] iv = SecureRandom.getInstanceStrong().generateSeed(12);

                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                Iterator<String> dbkGen = new DeviceBindingKeyGenerator();
                SecretKey key = generateKey(dbkGen.next(), salt); // Use the first dbk
                GCMParameterSpec spec = new GCMParameterSpec(128, iv);
                cipher.init(Cipher.ENCRYPT_MODE, key, spec);

                byte[] ciphertext = cipher.doFinal(plaintext.getBytes(charset));

                ByteBuffer buffer = ByteBuffer.allocate(salt.length + iv.length + ciphertext.length);
                buffer.put(salt);
                buffer.put(iv);
                buffer.put(ciphertext);

                return Base64.getEncoder().encodeToString(buffer.array());

            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (GeneralSecurityException e) {
                throw new GeneralSecurityException("Encryption failed");
            }
        }

        @Override
        public String decrypt(String encoded) throws GeneralSecurityException {
            byte[] data = Base64.getDecoder().decode(encoded);
            ByteBuffer buffer = ByteBuffer.wrap(data);

            byte[] salt = new byte[16];
            byte[] iv = new byte[12];
            byte[] ciphertext = new byte[data.length - 16 - 12];
            buffer.get(salt);
            buffer.get(iv);
            buffer.get(ciphertext);

            Set<String> tried = new HashSet<>();
            Iterator<String> dbkGen = new DeviceBindingKeyGenerator();
            while (dbkGen.hasNext()) {
                String dbk = dbkGen.next();
                if (tried.contains(dbk))
                    continue;
                String plaintext = decryptWith(dbk, salt, iv, ciphertext);
                if (plaintext != null)
                    return plaintext;
                tried.add(dbk);
                if (tried.size() >= MAX_KEY_CANDIDATES)
                    break;
            }

            throw new GeneralSecurityException("Decryption failed");
        }

        private String decryptWith(String dbk, byte[] salt, byte[] iv, byte[] ciphertext) {
            try {
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                SecretKey key = generateKey(dbk, salt);
                GCMParameterSpec spec = new GCMParameterSpec(128, iv);
                cipher.init(Cipher.DECRYPT_MODE, key, spec);
                return new String(cipher.doFinal(ciphertext), charset);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (GeneralSecurityException e) {
                return null;
            }
        }
    }
}
