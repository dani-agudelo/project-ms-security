package com.ucaldas.mssecurity.Services;

import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class MfaService {
  /**
   * Generates a random code consisting of 8 digits.
   *
   * @return the generated code as a string
   */
  public String generateCode() {
    return Stream.generate(() -> (int) (Math.random() * 10))
        .limit(6) // 6 digits
        .map(String::valueOf)
        .reduce("", String::concat);
  }
}
