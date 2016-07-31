package org.mos91.tcpping.pitcher.handlers;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.mos91.tcpping.messages.ping.PingMessage;
import org.mos91.tcpping.model.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author OMeleshin.
 * @version 23.07.2016
 */
public class PitcherHandler extends ChannelDuplexHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PitcherHandler.class);

  private final AtomicLong totalNumber = new AtomicLong();

  private final AtomicLong messagesPerPeriod = new AtomicLong();

  private final AtomicLong receiveNumberPerPeriod = new AtomicLong();

  private final AtomicLong totalLoss = new AtomicLong();

  private final AtomicLong sendDurationCumul = new AtomicLong();

  private final AtomicLong rcvDurationCumul = new AtomicLong();

  private final AtomicLong rttCumul = new AtomicLong();

  private final AtomicLong maxRtt = new AtomicLong();

  private final AtomicLong timeBarrier = new AtomicLong(0);

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    PingMessage response = (PingMessage) msg;

    if (response.getSendTime() >= timeBarrier.get()) {
      long timezoneDifference = TimeZone.getDefault().getRawOffset() - response.getRcvTimezoneOffset();
      long adjustedRcvTime = response.getRcvTime() + timezoneDifference;
      long sendDuration = adjustedRcvTime - response.getSendTime();
      long rcvDuration = System.currentTimeMillis() - adjustedRcvTime;
      long rtt = sendDuration + rcvDuration;

      sendDurationCumul.getAndAdd(sendDuration);
      rcvDurationCumul.getAndAdd(rcvDuration);
      rttCumul.getAndAdd(rtt);

      maxRtt.set(Math.max(maxRtt.longValue(), rtt));

      receiveNumberPerPeriod.incrementAndGet();
    }

    ctx.fireChannelRead(msg);
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    PingMessage pingMessage = (PingMessage) msg;

    long sendTime = new Date().getTime();
    pingMessage.setSendTime(sendTime);

    totalNumber.incrementAndGet();
    messagesPerPeriod.incrementAndGet();

    ctx.write(msg, promise);
  }

  public Statistics getStatistics() {
    Statistics statistics = new Statistics();

    timeBarrier.set(new Date().getTime());

    long lossPerSecond = messagesPerPeriod.get() - receiveNumberPerPeriod.get();

    statistics.setLossPerPeriod(lossPerSecond);
    statistics.setTotalLoss(totalLoss.addAndGet(lossPerSecond));

    double avgSendDuration = sendDurationCumul.doubleValue() / receiveNumberPerPeriod.doubleValue();
    double avgRcvDuration = rcvDurationCumul.doubleValue() / receiveNumberPerPeriod.doubleValue();
    double avgRtt = rttCumul.doubleValue() / receiveNumberPerPeriod.doubleValue();

    statistics.setAvgSendTime(avgSendDuration);
    statistics.setAvgRcvTime(avgRcvDuration);
    statistics.setAvgRtt(avgRtt);
    statistics.setMaxRtt(maxRtt.longValue());
    statistics.setTotalNumber(totalNumber.get());
    statistics.setMessagesPerPeriod(messagesPerPeriod.longValue());

    messagesPerPeriod.set(0);
    receiveNumberPerPeriod.set(0);
    sendDurationCumul.set(0);
    rcvDurationCumul.set(0);
    rttCumul.set(0);

    return statistics;
  }

  public void resetStatistics() {
    timeBarrier.set(new Date().getTime());

    totalNumber.set(0);
    totalLoss.set(0);
    messagesPerPeriod.set(0);
    receiveNumberPerPeriod.set(0);
    sendDurationCumul.set(0);
    rcvDurationCumul.set(0);
    rttCumul.set(0);
    maxRtt.set(0);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    LOG.error(MessageFormat.format("Exception in channel handler : {0}", getClass().getName()), cause);
    ctx.close();
  }
}
