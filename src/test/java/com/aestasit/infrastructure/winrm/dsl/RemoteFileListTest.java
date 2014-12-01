package com.aestasit.infrastructure.winrm.dsl;

import org.junit.Test;

import static org.mockito.Mockito.*;

/*
 * This java class is introduced to avoid
 * NullPointerException in groovy with Mockito
 * (https://code.google.com/p/mockito/issues/detail?id=303)
 *
 */
public class RemoteFileListTest {
  RemoteFile mockRemoteFolder() {
    RemoteFile mockedFile = mock(RemoteFile.class);
    when(mockedFile.exists()).thenReturn(true);
    when(mockedFile.isDirectory()).thenReturn(true);
    RemoteFile file1 = mock(RemoteFile.class);
    RemoteFile file2 = mock(RemoteFile.class);
    when(mockedFile.listFiles()).thenReturn(new RemoteFile[]{file1, file2});

    return mockedFile;
  }

  @Test
  public void testListFiles() {
    RemoteFile file = mockRemoteFolder();
    file.initialize();
    assert file.exists();
    assert file.isDirectory();
    RemoteFile[] files = file.listFiles();
    assert files.length == 2;
  }
}
