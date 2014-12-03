/*
 * Copyright (C) 2011-2014 Aestas/IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    assert file.exists();
    assert file.isDirectory();
    RemoteFile[] files = file.listFiles();
    assert files.length == 2;
  }
}
