/** Copyright (c) 2022-2025, Harry Huang
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
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Enumeration;


public class SecretUtils {
    public interface SecretAlgorithm {
        String encrypt(String plaintext) throws GeneralSecurityException;
        String decrypt(String encoded) throws GeneralSecurityException;
    }


    public static class WeakEncryptionV0 implements SecretAlgorithm {
        private final String deviceBinding;
        private static final Charset charset = StandardCharsets.UTF_8;

        public WeakEncryptionV0() {
            deviceBinding = getDeviceBindingKey();
        }

        private static String getPrimaryMacAddress() {
            try {
                Enumeration<NetworkInterface> eni = NetworkInterface.getNetworkInterfaces();
                while (eni.hasMoreElements()) {
                    NetworkInterface ni = eni.nextElement();
                    if (ni.isUp() && !ni.isLoopback() && !ni.isVirtual()) {
                        byte[] mac = ni.getHardwareAddress();
                        if (mac != null) {
                            StringBuilder sb = new StringBuilder();
                            for (byte b : mac)
                                sb.append(String.format("%02X:", b));
                            return sb.substring(0, sb.length() - 1);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            return "unknown_mac";
        }

        private static String getHostname() {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ignored) {
            }
            return  "unknown_hostname";
        }

        private static String getDeviceBindingKey() {
            return System.getProperty("user.name") + ","
                    + System.getProperty("user.home") + ";"
                    + getHostname() + ","
                    + getPrimaryMacAddress() + ";"
                    + System.getProperty("os.name") + ","
                    + System.getProperty("os.arch") + ";";
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
                SecretKey key = generateKey(deviceBinding, salt);
                GCMParameterSpec spec = new GCMParameterSpec(128, iv);
                cipher.init(Cipher.ENCRYPT_MODE, key, spec);

                byte[] ciphertext = cipher.doFinal(plaintext.getBytes(charset));

                ByteBuffer buffer = ByteBuffer.allocate(salt.length + iv.length + ciphertext.length);
                buffer.put(salt);
                buffer.put(iv);
                buffer.put(ciphertext);

                return Base64.getEncoder().encodeToString(buffer.array());

            } catch (NoSuchAlgorithmException e){
                throw new RuntimeException(e);
            } catch (GeneralSecurityException e) {
                throw new GeneralSecurityException("Encryption failed");
            }
        }

        @Override
        public String decrypt(String encoded) throws GeneralSecurityException {
            try {
                byte[] data = Base64.getDecoder().decode(encoded);
                ByteBuffer buffer = ByteBuffer.wrap(data);

                byte[] salt = new byte[16];
                byte[] iv = new byte[12];
                byte[] ciphertext = new byte[data.length - 16 - 12];
                buffer.get(salt);
                buffer.get(iv);
                buffer.get(ciphertext);

                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                SecretKey key = generateKey(deviceBinding, salt);
                GCMParameterSpec spec = new GCMParameterSpec(128, iv);
                cipher.init(Cipher.DECRYPT_MODE, key, spec);

                byte[] plaintext = cipher.doFinal(ciphertext);
                return new String(plaintext, charset);

            } catch (NoSuchAlgorithmException e){
                throw new RuntimeException(e);
            } catch (GeneralSecurityException e) {
                throw new GeneralSecurityException("Decryption failed");
            }
        }
    }
}
