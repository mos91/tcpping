package org.mos91.tcpping.messages.ping;

/**
 * @author OMeleshin.
 * @version 23.07.2016
 */
public enum DecoderState {
  READ_ID, READ_SEND_TIME, READ_RCV_TIME, READ_TZ_OFFSET, READ_DATA_LENGTH, READ_DATA
}
