package org.mos91.tcpping.model;

/**
 * @author OMeleshin.
 * @version 24.07.2016
 */
public class Statistics {

  private long totalNumber;

  private long totalLoss;

  private long messagesPerPeriod;

  private long lossPerPeriod;

  private double avgSendTime;

  private double avgRcvTime;

  private double avgRtt;

  private long maxRtt;

  public long getTotalNumber() {
    return totalNumber;
  }

  public void setTotalNumber(long totalNumber) {
    this.totalNumber = totalNumber;
  }

  public long getMessagesPerPeriod() {
    return messagesPerPeriod;
  }

  public void setMessagesPerPeriod(long messagesPerPeriod) {
    this.messagesPerPeriod = messagesPerPeriod;
  }

  public double getAvgSendTime() {
    return avgSendTime;
  }

  public void setAvgSendTime(double avgSendTime) {
    this.avgSendTime = avgSendTime;
  }

  public double getAvgRcvTime() {
    return avgRcvTime;
  }

  public void setAvgRcvTime(double avgRcvTime) {
    this.avgRcvTime = avgRcvTime;
  }

  public double getAvgRtt() {
    return avgRtt;
  }

  public void setAvgRtt(double avgRtt) {
    this.avgRtt = avgRtt;
  }

  public long getMaxRtt() {
    return maxRtt;
  }

  public void setMaxRtt(long maxRtt) {
    this.maxRtt = maxRtt;
  }

  public long getTotalLoss() {
    return totalLoss;
  }

  public void setTotalLoss(long totalLoss) {
    this.totalLoss = totalLoss;
  }

  public long getLossPerPeriod() {
    return lossPerPeriod;
  }

  public void setLossPerPeriod(long lossPerPeriod) {
    this.lossPerPeriod = lossPerPeriod;
  }
}
