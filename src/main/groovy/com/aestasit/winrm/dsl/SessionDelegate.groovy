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

import com.aestasit.winrm.WinRMException
import com.aestasit.winrm.WinRMOptions
import com.aestasit.winrm.log.Logger
import com.aestasit.winrm.log.Slf4jLogger
import com.xebialabs.overthere.ConnectionOptions
import com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection

import java.util.regex.Pattern

import static com.xebialabs.overthere.ConnectionOptions.*
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_TIMEMOUT
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_INTERNAL
import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * Closure delegate that is used to collect all WinRM options and give access to other DSL delegates.
 *
 * @author Andrey Adamovich
 */
@Mixin([CopyMethods, ExecMethods])
class SessionDelegate {

  private static final int DEFAULT_WINRM_PORT = 5985
  private static final Pattern WINRM_URL = ~/^(([^:@]+)(:([^@]+))?@)?([^:]+)(:(\d+))?$/

  private String host = null
  private int port = DEFAULT_WINRM_PORT
  private String username = null
  private String password = null

  private final WinRMOptions options

  protected Logger logger = null

  SessionDelegate(WinRMOptions options) {
    this.options = options
    this.host = options.defaultHost
    this.username = options.defaultUser
    this.port = options.defaultPort
    this.password = options.defaultPassword
    if (options.logger != null) {
      logger = options.logger
    } else {
      logger = new Slf4jLogger()
    }
  }

  RemoteFile remoteFile(String destination) {
    new RemoteFile(this, destination)
  }

  // TODO: currently for any operation a new connection is created
  // It is reasonable to create single connection and use it for all operation inside <remoteManagement>
  // Implement it in the same way as for sshoogr (connection control happens in the engine)


  def cifsConnection(@DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {

    if (host == null) {
      throw new WinRMException("Host is required.")
    }
    if (username == null) {
      throw new WinRMException("Username is required.")
    }
    if (password == null) {
      throw new WinRMException("Password is required.")
    }

    def connectionOptions = new ConnectionOptions()

    connectionOptions[ADDRESS] = host
    connectionOptions[PORT] = port
    connectionOptions[USERNAME] = username
    connectionOptions[PASSWORD] = password
    connectionOptions[OPERATING_SYSTEM] = WINDOWS
    connectionOptions[CONNECTION_TYPE] = WINRM_INTERNAL

    // TODO: add support for HTTPS

    connectionOptions[WINRM_TIMEMOUT] = "PT${options.maxWait / 1000}.${options.maxWait % 1000}S".toString()

    CifsWinRmConnection connection = new CifsWinRmConnection('cifs', connectionOptions)

    cl.delegate = this
    cl.resolveStrategy = DELEGATE_FIRST

    try {
      cl(connection)
    } finally {
      connection.close()
    }

  }

  String getHost() {
    host
  }

  int getPort() {
    port
  }

  String getUser() {
    username
  }

  String getPassword() {
    password
  }

  void setHost(String host) {
    this.host = host
  }

  void setPort(int port) {
    this.port = port
  }

  void setUser(String username) {
    this.username = username
  }

  void setPassword(String password) {
    this.password = password
  }

  void setUrl(String url) {
    def matcher = WINRM_URL.matcher(url)
    if (matcher.matches()) {
      setHost(matcher.group(5))
      if (matcher.group(7)) {
        setPort(matcher.group(7).toInteger())
      } else {
        setPort(DEFAULT_WINRM_PORT)
      }
      setUser(matcher.group(2))
      setPassword(matcher.group(4))
    } else {
      throw new MalformedURLException("Unknown URL format: " + url)
    }
  }

}
