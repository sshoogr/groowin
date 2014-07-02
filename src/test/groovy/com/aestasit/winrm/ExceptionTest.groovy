package com.aestasit.winrm

import org.junit.Test

/**
 * Created by aad on 02-07-2014.
 */
class ExceptionTest {

  @Test
  void testException() {
    new WinRMException("Test Exception")
  }
}
