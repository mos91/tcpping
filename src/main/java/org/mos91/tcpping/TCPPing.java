package org.mos91.tcpping;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.mos91.tcpping.catcher.Catcher;
import org.mos91.tcpping.pitcher.Pitcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main bootstrap class
 *
 * @author OMeleshin.
 * @version 22.07.2016
 */
public class TCPPing {

  private static final Logger LOG = LoggerFactory.getLogger(TCPPing.class);

  private static final String HOST = "127.0.0.1";

  private static final int MIN_SIZE = 50;

  private static final int MAX_SIZE = 3000;

  @Option(name = "-p", forbids = {"-c", "-bind"}, aliases = "--pitcher", usage = "Pitcher mode")
  private boolean isPitcher;

  @Option(name = "-c", forbids = {"-p", "-mps", "-size", "-h"}, aliases = "--catcher", usage = "Catcher mode")
  private boolean isCatcher;

  @Option(name = "-port", metaVar = "PORT", usage = "-port <port>\n[Pitcher] TCP socket port used for connecting\n[Catcher] TCP socket port used for listening")
  private int port = 8089;

  @Option(name = "-bind", metaVar = "BIND_ADDRESS", depends = "-c", usage = "-bind\n<ip_address>\n[Catcher] TCP socket bind address that will be used to run listen")
  private String bind = HOST;

  @Option(name = "-mps", depends = "-p", usage = "[Pitcher] the speed of message sending expressed as „messages per second“\nDefault: 1")
  private int mps = 1;

  @Option(name = "-size", depends = "-p", usage = "[Pitcher] message length\nMinimum: 50\nMaximum: 3000\nDefault: 300")
  private int size = 300;

  @Option(name = "-h", depends = "-p", aliases = "--hostname", usage = "[Pitcher] the name of the computer which runs Catcher")
  private String hostname = HOST;

  @Option(name = "-r", depends = "-[", aliases = "--rate", usage = "[Pitcher] Determines how frequently the pitcher will feed out the network metrics, "
    + "e.g -r 5 means every 5 seconds")
  private int rate = 5;

  private void run() {

    if (!isPitcher && !isCatcher) {
      LOG.info("Usage : tcpping [MODE] ...\n [MODE] '-p' (Pitcher mode) or '-c' (Catcher mode)");

      System.exit(-1);
    }

    if (isPitcher && isCatcher) {
      LOG.info("Usage : tcpping [MODE] ...\n [MODE] '-p' (Pitcher mode) or '-c' (Catcher mode)");

      System.exit(-1);
    }

    if (size < MIN_SIZE || size > MAX_SIZE) {
      LOG.info("Usage : size of message must be between {} and {} bounds", MIN_SIZE, MAX_SIZE);

      System.exit(-1);
    }

    Runnable service;
    if (isPitcher) {
      service = new Pitcher(hostname, port, mps, size, rate);
    } else {
      service = new Catcher(bind, port);
    }

    service.run();
  }

  /**
   * Starts TCPPing Utility
   * */
  public static void main(String[] args) {
    TCPPing tcpping = new TCPPing();
    CmdLineParser parser = new CmdLineParser(tcpping);
    try {
      parser.parseArgument(args);

      tcpping.run();
    } catch (CmdLineException e) {
      parser.printUsage(System.err);
    }
  }
}
