package org.mos91.tcpping.pitcher;

import io.netty.channel.Channel;
import org.mos91.tcpping.commons.NettyClient;
import org.mos91.tcpping.messages.ping.PingMessage;
import org.mos91.tcpping.messages.ping.PingMessageDecoder;
import org.mos91.tcpping.messages.ping.PingMessageEncoder;
import org.mos91.tcpping.model.Statistics;
import org.mos91.tcpping.pitcher.handlers.PitcherHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.mos91.tcpping.Utilities.shuffleData;

/**
 * @author OMeleshin.
 * @version 23.07.2016
 */
public class Pitcher implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(Pitcher.class);

  private static final Logger STATISTICS_LOG = LoggerFactory.getLogger(Pitcher.class.getName() + ".stats");

  private static final long WARM_UP_PERIOD = 5000;

  private static final long STAT_INTERVAL = 1000;

  private PitcherHandler pitcherHandler;

  private String host;

  private int port;

  private int mps;

  private int size;

  private int rate;

  public Pitcher(String host, int port, int mps, int size, int rate) {
    this.host = host;
    this.port = port;
    this.mps = mps;
    this.size = size;
    this.rate = rate;

    pitcherHandler = new PitcherHandler();
  }

  @Override
  public void run() {
    // Configure the client.

    LOG.info("Start pitcher on {}:{}, with message size : {}, msg/s : {}", host, port, size, mps);

    try {
      doPitch();
    } catch (InterruptedException e) {
      LOG.error("Pitcher terminates abnormally", e);
    }
  }

  private void doPitch() throws InterruptedException {
    LOG.info("Start pitching ...");

    new NettyClient<Void>(host, port, Arrays.asList(new PingMessageEncoder(), new PingMessageDecoder(), pitcherHandler)).execute(new PitchCallback());
  }

  private class PitchCallback implements NettyClient.Callback<Void> {

    private void warmup(long pitchInterval, String data, Channel channel) throws InterruptedException {
      long beforePitch, interval;
      long warmupRemain = WARM_UP_PERIOD;
      LOG.info("Warming up catcher...");
      while (warmupRemain > 0) {
        beforePitch = System.currentTimeMillis();
        channel.writeAndFlush(new PingMessage(0, data)).sync();

        interval = pitchInterval - (System.currentTimeMillis() - beforePitch);
        if (interval <= 0) {
          continue;
        }

        warmupRemain -= interval;
        Thread.sleep(interval);
      }

      pitcherHandler.resetStatistics();
    }

    @Override
    public Void execute(Channel channel) throws Exception {
      long pitchInterval = STAT_INTERVAL / mps;

      String data = shuffleData(size);
      long timerRemain = STAT_INTERVAL;
      long printRemain = rate;
      long beforePitch, interval;
      long id = 0;

      warmup(pitchInterval, data, channel);

      while (true) {
        beforePitch = System.currentTimeMillis();
        // code in byte stream
        channel.writeAndFlush(new PingMessage(id++, data)).sync();

        interval = pitchInterval - (System.currentTimeMillis() - beforePitch);
        if (interval <= 0) {
          continue;
        }

        timerRemain -= interval;
        Thread.sleep(interval);

        if (timerRemain <= 0) {
          timerRemain = STAT_INTERVAL;
          printRemain--;

          Statistics stats = pitcherHandler.getStatistics();
          if (printRemain <= 0) {
            STATISTICS_LOG.info(String.format("Total Number : %d | Msgs/s : %d | Avg.RTT : %1.6f ms | Max.RTT : %d ms | Avg.SendTime : %1.6f ms "
                + "\n Avg.RcvTime : %1.6f ms | TotalLoss : %d | Loss/s : %d", stats.getTotalNumber(), stats.getMessagesPerPeriod(),
              stats.getAvgRtt(), stats.getMaxRtt(), stats.getAvgSendTime(), stats.getAvgRcvTime(), stats.getTotalLoss(), stats.getLossPerPeriod()));

            printRemain = rate;
          }

          continue;
        }
      }
    }
  }

}
