package com.aestasit.winrm

import org.junit.Test

class WinRMWebServiceTest {

  @Test
  void testWebService() {

    println new URL('http://192.168.56.101:5985/wsman').text

  }

}
