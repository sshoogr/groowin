/*
 * This script connects to remote server on which MySQL server is running.
 * It creates dump of MySQL db and copies it locally.
 */

@Grab('com.aestasit.infrastructure.groowin:groowin:0.1.11-SNAPSHOT')

import static com.aestasit.infrastructure.winrm.DefaultWinRM.*

def user = 'vagrant'
def pwd = 'vagrant'
def host = '192.168.25.25'
def port = '5986'

def mySqlUser = 'root'
def mySqlPassword = 'secret'

def currentTime = new Date().format('yyyy-MM-dd HH-mm')
def folderToSaveDump = "applicationDBDump/sakila ${currentTime}/"
def dumpStorage = new File(folderToSaveDump)
if (!dumpStorage.exists()) {
  dumpStorage.mkdirs()
}

remoteManagement("${user}:${pwd}@${host}:${port}") {
  exec "c:\\mysql\\bin\\mysqldump --user=${mySqlUser} --password=${mySqlPassword} sakila > C:\\sakila_dmp.sql"
  cp {
    from {remoteFile 'C:\\sakila_dmp.sql'}
    into {localDir folderToSaveDump}
  }
}

