package com.ucaldas.mssecurity.Services;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service
public class EncryptionService {
  public String convertSHA256(String password) {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
    byte[] hash = md.digest(password.getBytes());
    StringBuffer sb = new StringBuffer();
    for (byte b : hash) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  /**
   * Generates a random password of the specified length.
   *
   * @return the generated password
   */
  public String generatePassword() {
    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    return Stream.generate(() -> (int) (Math.random() * alphabet.length()))
        .limit(6) // 6 digits
        .map(alphabet::charAt)
        .map(String::valueOf)
        .reduce("", String::concat);
  }
}
