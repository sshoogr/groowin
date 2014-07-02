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

package com.aestasit.winrm.dsl

import com.xebialabs.overthere.OverthereConnection
import com.xebialabs.overthere.OverthereFile

import static org.apache.commons.io.FilenameUtils.separatorsToWindows

/**
 * This class represents a remote file providing methods to access file's content.
 *
 * @author Sergey Korenko
 *
 */
class RemoteFile {

  private final SessionDelegate delegate
  private final String destination

  RemoteFile(SessionDelegate delegate, String destination) {
    this.delegate = delegate
    this.destination = destination
  }

  String getText() {
    String content = null

    delegate.cifsConnection { OverthereConnection connection ->
      OverthereFile remoteFile = connection.getFile(separatorsToWindows(destination))
      content = remoteFile.inputStream.text
    }

    content
  }

  void setText(String text) {
    delegate.cifsConnection { OverthereConnection connection ->
      OverthereFile remoteFile = connection.getFile(separatorsToWindows(destination))
      remoteFile.outputStream << text
    }
  }

  void saveEmpty() {
    delegate.exec('echo.', '>', destination)
  }

  boolean canRead() {
    boolean readable = false

    delegate.cifsConnection { OverthereConnection connection ->
      OverthereFile remoteFile = connection.getFile(separatorsToWindows(destination))
      readable = remoteFile.canRead()
    }

    readable
  }

  boolean canWrite() {
    boolean writable = false

    delegate.cifsConnection { OverthereConnection connection ->
      OverthereFile remoteFile = connection.getFile(separatorsToWindows(destination))
      writable = remoteFile.canWrite()
    }

    writable
  }

  boolean isHidden() {
    boolean hidden = false

    delegate.cifsConnection { OverthereConnection connection ->
      OverthereFile remoteFile = connection.getFile(separatorsToWindows(destination))
      hidden = remoteFile.isHidden()
    }

    hidden
  }

  long lastModified() {
    long lastModified = 0l

    delegate.cifsConnection { OverthereConnection connection ->
      OverthereFile remoteFile = connection.getFile(separatorsToWindows(destination))
      lastModified = remoteFile.lastModified()
    }

    lastModified
  }

  long length() {
    long length = 0l

    delegate.cifsConnection { OverthereConnection connection ->
      OverthereFile remoteFile = connection.getFile(separatorsToWindows(destination))
      length = remoteFile.length()
    }

    length
  }
}
