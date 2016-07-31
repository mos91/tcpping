package org.mos91.tcpping;

import com.google.common.base.Equivalence;
import com.google.common.primitives.Ints;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mos91.tcpping.messages.ping.PingMessage;
import org.mos91.tcpping.messages.ping.PingMessageDecoder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Queue;
import java.util.TimeZone;

import static com.google.common.primitives.Bytes.concat;
import static com.google.common.primitives.Longs.toByteArray;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mos91.tcpping.Utilities.shuffleData;

/**
 * @author OMeleshin.
 * @version 31.07.2016
 */
public class PingMessageDecoderTest {

  private PingMessage expected;

  @BeforeClass
  public void init() throws Exception {
    String data = shuffleData(50);
    expected = new PingMessage(1L, data);
    expected.setSendTime(1469946444161L);
    expected.setRcvTime(1469946472026L);
    expected.setRcvTimezoneOffset(TimeZone.getDefault().getRawOffset());
  }

  @DataProvider(name = "correctBuffer")
  public Object[][] correctBuffer() {
    ByteBuf buf = Unpooled.wrappedBuffer(concat(toByteArray(1L),
      toByteArray(1469946444161L), toByteArray(1469946472026L),
      toByteArray(TimeZone.getDefault().getRawOffset()), Ints.toByteArray(50), expected.getData().getBytes()));

    return new Object[][]{{
      buf
    }};
  }

  @DataProvider(name = "invalidBuffer")
  public Object[][] invalidBuffer() {
    ByteBuf buf = Unpooled.wrappedBuffer(concat(toByteArray(1L),
      toByteArray(1469946444161L), toByteArray(1469946472026L),
      Ints.toByteArray(50), expected.getData().getBytes()));

    return new Object[][]{{
      buf
    }};
  }

  @Test(dataProvider = "correctBuffer")
  public void shouldDecodeSuccess(ByteBuf buf) {
    EmbeddedChannel channel = decoderShouldExists();

    channel.writeInbound(buf);

    Queue<Object> messages = channel.inboundMessages();
    assertEquals(1, messages.size());
    PingMessage actual = (PingMessage) messages.poll();
    assertThat(actual, is(PingMessage.class));
    assertThat(actual, is(pingMessage(expected)));
  }

  @Test(dataProvider = "invalidBuffer")
  public void shouldFailureOnDecode(ByteBuf buf) {
    EmbeddedChannel channel = decoderShouldExists();

    channel.writeInbound(buf);

    Queue<Object> messages = channel.inboundMessages();
    assertEquals(0, messages.size());
  }

  private EmbeddedChannel decoderShouldExists() {
    PingMessageDecoder decoder = new PingMessageDecoder();
    EmbeddedChannel channel = new EmbeddedChannel(decoder);

    assertThat(channel.pipeline().get(PingMessageDecoder.class), is(notNullValue()));

    return channel;
  }

  private static Matcher<PingMessage> pingMessage(PingMessage expected) {
    Equivalence<PingMessage> equiv = new Equivalence<PingMessage>() {
      @Override
      protected boolean doEquivalent(PingMessage a, PingMessage b) {
        if (a == b)
          return true;
        if (a == null || a.getClass() != b.getClass())
          return false;


        if (a.getId() != b.getId())
          return false;
        if (a.getSendTime() != b.getSendTime())
          return false;
        if (a.getRcvTime() != b.getRcvTime())
          return false;
        if (a.getRcvTimezoneOffset() != b.getRcvTimezoneOffset())
          return false;

        String data = a.getData();
        String thatData = b.getData();
        return data != null ? data.equals(thatData) : thatData == null;
      }

      @Override
      protected int doHash(PingMessage pingMessage) {
        long id = pingMessage.getId();
        long sendTime = pingMessage.getSendTime();
        long rcvTime = pingMessage.getRcvTime();
        long rcvTimezoneOffset = pingMessage.getRcvTimezoneOffset();
        String data = pingMessage.getData();

        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (sendTime ^ (sendTime >>> 32));
        result = 31 * result + (int) (rcvTime ^ (rcvTime >>> 32));
        result = 31 * result + (int) (rcvTimezoneOffset ^ (rcvTimezoneOffset >>> 32));
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
      }
    };

    return new BaseMatcher<PingMessage>(){

      @Override
      public void describeTo(Description description) {
        description.appendText("Actual ping message must be equal to expected");
      }

      @Override
      public boolean matches(Object o) {
        PingMessage actual = (PingMessage) o;
        return equiv.equivalent(expected, actual);
      }
    };
  }
}
