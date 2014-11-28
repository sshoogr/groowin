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

package com.aestasit.infrastructure.winrm.dsl

import com.aestasit.infrastructure.winrm.log.Logger

/**
 * This class represents a remote file providing methods to access file's content.
 *
 * @author Sergey Korenko
 *
 */
class RemoteFile {

  private final SessionDelegate delegate
  private final String destination

  protected Logger logger


  RemoteFile(SessionDelegate delegate, String destination) {
    this.delegate = delegate
    this.destination = destination
    this.logger = delegate.logger
  }

  String getText() {
    String content = null
    logger.debug 'Get file content using jcifs'
    content
  }

  void setText(String text) {
    logger.debug "Set new file content[=$text] via jcifs"
  }

  void saveEmpty() {
    delegate.exec('echo.', '>', destination)
  }

  boolean canRead() {
    boolean readable = false
    logger.debug 'Check if file is readable via jcifs'
    readable
  }

  boolean canWrite() {
    boolean writable = false
    logger.debug 'Check if file is writable via jcifs'
    writable
  }

  boolean isHidden() {
    boolean hidden = false
    logger.debug 'Check if file is hidden via jcifs'
    hidden
  }

  long lastModified() {
    long lastModified = 0l
    logger.debug 'get last modification date via jcifs'
    lastModified
  }

  long length() {
    long length = 0l
    logger.debug 'Get file size via jcifs'
    length
  }
}
