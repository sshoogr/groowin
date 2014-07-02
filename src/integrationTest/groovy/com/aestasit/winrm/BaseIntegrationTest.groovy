package com.aestasit.winrm

import com.aestasit.winrm.dsl.WinRMDslEngine
import com.aestasit.winrm.log.SysOutLogger
import org.junit.BeforeClass

class BaseIntegrationTest {

  static WinRMOptions options
  static WinRMDslEngine engine

  @BeforeClass
  def static void defineOptions() {
    options = new WinRMOptions()
    options.with {
      logger = new SysOutLogger()

      defaultHost = '192.168.56.101'
      defaultUser = 'vagrant'
      defaultPassword = 'vagrant'
      defaultPort = 5985

      verbose = true
    }
    engine = new WinRMDslEngine(options)
  }

}