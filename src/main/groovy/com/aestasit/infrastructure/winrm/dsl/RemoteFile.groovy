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

import com.aestasit.infrastructure.winrm.WinRMException
import groovy.transform.AutoClone
import groovy.transform.Canonical
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile

/**
 * This class represents a remote file providing methods to access file's content.
 *
 * @author Sergey Korenko
 */
@Canonical(includes = ['host', 'user', 'password', 'destinationFilepath'])
@AutoClone
class RemoteFile {
  String host
  String user
  String password
  String destinationFilepath

  SmbFile remoteFile
  String name

  void initialize() {
    destinationFilepath = destinationFilepath.replace('\\', '/')
    if (destinationFilepath.length() < 2) {
      throw new WinRMException("Local file path [$destinationFilepath] on a remote host is too short")
    }
    if (destinationFilepath[1] != ':') {
      throw new WinRMException("Local file path [$destinationFilepath] is not correct! The second symbol has to be a colon(:) symbol!")
    }
    if (destinationFilepath.length() > 2 && destinationFilepath[2] != '/') {
      throw new WinRMException("Local file path [$destinationFilepath] is not correct! It is expected a separator symbol(\\) after a colon(:) symbol!")
    }

    // https??
    NtlmPasswordAuthentication authentication = new NtlmPasswordAuthentication(null, user, password)
    name = convertToSmbFileFormat()
    remoteFile = new SmbFile(name, authentication)
  }

  /*
    * Converts local Windows path of a file on a remote host to samba path with administrative shares.
    * For example, [C:\temp\test.txt] file on remote host[TestMachine] will be converted to
    * [smb://TestMachine/C$/temp/test.txt]
    */
  private String convertToSmbFileFormat() {
    "smb://${host}/${destinationFilepath[0]}\$/${destinationFilepath.substring(3)}"
  }

  String getText() {
    remoteFile.inputStream.text
  }

  void setText(String text) {
    remoteFile.outputStream << text
  }

  void saveEmpty() {
    remoteFile.createNewFile()
  }

  boolean canRead() {
    remoteFile.canRead()
  }

  boolean canWrite() {
    remoteFile.canWrite()
  }

  boolean isHidden() {
    remoteFile.isHidden()
  }

  long lastModified() {
    remoteFile.lastModified
  }

  long length() {
    remoteFile.length()
  }

  boolean isDirectory(){
    remoteFile.isDirectory()
  }

  boolean exists(){
    remoteFile.exists()
  }

  void mkdir(){
    remoteFile.mkdir()
  }

  OutputStream getOutputStream(){
    remoteFile.outputStream
  }

  InputStream getInputStream(){
    remoteFile.inputStream
  }

  RemoteFile[] listFiles() {
    remoteFile.listFiles().collect({
      def readFile = this.clone()
      readFile.remoteFile = it
      readFile.name = it.name
      readFile
    }).toArray()
  }
}