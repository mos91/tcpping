package org.mos91.tcpping.messages.ping;

/**
 * @author OMeleshin.
 * @version 23.07.2016
 */
public class PingMessage {

  private long id;

  private long sendTime;

  private long rcvTime;

  private long rcvTimezoneOffset;

  private String data;

  public PingMessage(long id, String data) {
    this.id = id;
    this.data = data;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public long getSendTime() {
    return sendTime;
  }

  public void setSendTime(long sendTime) {
    this.sendTime = sendTime;
  }

  public long getRcvTime() {
    return rcvTime;
  }

  public void setRcvTime(long rcvTime) {
    this.rcvTime = rcvTime;
  }

  public long getRcvTimezoneOffset() {
    return rcvTimezoneOffset;
  }

  public void setRcvTimezoneOffset(long rcvTimezoneOffset) {
    this.rcvTimezoneOffset = rcvTimezoneOffset;
  }


}
