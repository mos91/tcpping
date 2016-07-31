package org.mos91.tcpping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * @author OMeleshin.
 * @version 31.07.2016
 */
public class TestListener extends TestListenerAdapter {
  private static final Logger LOG = LoggerFactory.getLogger(TestListener.class);

  @Override
  public void onTestSuccess(ITestResult tr) {
    LOG.info(tr.getName()+ " -- Test method succeeded");
  }

  @Override
  public void onTestFailure(ITestResult tr) {
    LOG.info(tr.getName()+ " -- Test method failed");
  }

  @Override
  public void onTestSkipped(ITestResult tr) {
    LOG.info(tr.getName()+ " -- Test method skipped");
  }
}
