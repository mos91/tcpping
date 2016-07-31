package org.mos91.tcpping;

import java.util.Random;

import static org.mos91.tcpping.Constants.SYMBOLS;

/**
 * @author OMeleshin.
 * @version 31.07.2016
 */
public class Utilities {

  private Utilities() {}

  public static String shuffleData(int size) {
    StringBuilder sb = new StringBuilder();
    Random random = new Random();

    for (int i = 0;i < size;i++) {
      int idx = random.nextInt(SYMBOLS.length());
      sb.append(SYMBOLS.charAt(idx));
    }

    return sb.toString();
  }
}
