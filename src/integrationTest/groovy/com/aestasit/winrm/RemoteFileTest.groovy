package com.aestasit.winrm

import org.junit.Test

class RemoteFileTest extends BaseIntegrationTest {
  @Test
  void testRemoteFileGetContent() throws Exception {
    engine.remoteSession {
      assert remoteFile('c:\\Windows\\System32\\drivers\\etc\\hosts').text.contains('localhost')
    }
  }

  @Test
  void testRemoteFileSetContent() throws Exception {
    engine.remoteSession {
      String filename = 'c:\\temp\\empty.groovy'

      remoteFile(filename).saveEmpty()
      remoteFile(filename).text = 'Hello World'
      assert remoteFile(filename).canRead()
      assert !remoteFile(filename).isHidden()
      println new Date(remoteFile(filename).lastModified())
      assert remoteFile(filename).length() < 1000l

      exec('del', filename)
    }
  }
}
