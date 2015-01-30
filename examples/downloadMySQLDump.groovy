/*
 * This script connects to remote server on which MySQL server is running.
 * It executes sql script on the remote MySQL server.
 * Before running sql script the current script creates dump of the remote db and copies it locally.
 * After copying the db dump it executes sql script via sql.exe. In case of error the current script copies MySQL script to the local host.
 */

@Grab('com.aestasit.infrastructure.groowin:groowin:0.1.10')

import static com.aestasit.infrastructure.winrm.DefaultWinRM.*

def user = 'user'
def pwd = 'password'
def host = 'server.windows2008r2'
def port = '5985'

def mySqlUser = 'root'
def mySqlPassword = 'secret'

def currentTime = new Date().format('yyyy-MM-dd HH-mm')
def folderToSaveDump = "applicationDBDump/application ${currentTime}/"
def dumpStorage = new File(folderToSaveDump)
if (!dumpStorage.exists()) {
  dumpStorage.mkdirs()
}

remoteManagement("${user}:${pwd}@${host}:${port}") {
  exec "c:\\mysql\\bin\\mysqldump --user=${mySqlUser} --password=${mySqlPassword} application_db > c:\\temp\\appScripts\\appl_dmp.sql"
  cp {
    from {remoteFile 'c:\\temp\\appScripts\\appl_dmp.sql'}
    into {localDir folderToSaveDump}
  }

  cp{
    from {localFile 'change_db_schema.sql'}
    into {remoteFile 'c:\\temp\\appScripts\\change_db_schema.sql'}
  }
  def result = exec "c:\\mysql\\bin\\mysql --user=${mySqlUser} --password=${mySqlPassword} application_db < c:\\temp\\appScripts\\change_db_schema.sql"
  if(result.exitStatus){
    cp{
      from {remoteDir 'c:\\temp\\mySqlLogs\\'}
      into {localDir folderToSaveDump}
    }
  }
}

