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
import com.xebialabs.overthere.*

import static com.xebialabs.overthere.ConnectionOptions.*
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.*
import static com.xebialabs.overthere.cifs.CifsConnectionType.*

/**
 * Closure delegate that is used to collect WinRM options from all and give access to other DSL delegates.
 *
 * @author Andrey Adamovich
 */
@Mixin([CopyMethods, ExecMethods])
class SessionDelegate {

  private static final int DEFAULT_WINRM_PORT = 5985

  private String     host           = null
  private int        port           = DEFAULT_WINRM_PORT
  private String     username       = null
  private String     password       = null

  private final WinRMOptions options

  protected Logger logger           = null

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

  // todo
  RemoteFile remoteFile(String destination) {
    new RemoteFile(this, destination)
  }

  // todo currently for any operation
  // a new connection is created
  // It is reasonable to create single connection and use it for all operation inside <remoteSession>
  //
  public void cifsConnection(Closure cl) {
    if (options.verbose) {
      logger.debug("Initialization of CIFS connection to execute remote file processing or command execution")
    }

    if (host == null) {
      throw new WinRMException("Host is required.")
    }
    if (username == null) {
      throw new WinRMException("Username is required.")
    }
    if (password == null) {
      throw new WinRMException("Password is required.")
    }

    def otherthereOptions = new ConnectionOptions()

    otherthereOptions[ADDRESS]          = host
    otherthereOptions[PORT]             = port
    otherthereOptions[USERNAME]         = username
    otherthereOptions[PASSWORD]         = password
    otherthereOptions[OPERATING_SYSTEM] = WINDOWS
    otherthereOptions[CONNECTION_TYPE]  = WINRM_INTERNAL

    OverthereConnection connection = Overthere.getConnection(CIFS_PROTOCOL, otherthereOptions)

    try {
      cl(connection)
    } finally {
      connection.close()
    }
  }
}
