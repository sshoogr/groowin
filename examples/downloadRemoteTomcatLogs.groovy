/*
 * This script connects to remote server on which Tomcat server is running.
 * The scripts installs 7-zip archiver into 'C:\\Program Files\\7-zip' folder, archives Tomcat logs
 * and downloads created archive locally.
 */

@Grab('com.aestasit.infrastructure.groowin:groowin:0.1.11-SNAPSHOT')

import static com.aestasit.infrastructure.winrm.DefaultWinRM.*

def user = 'user'
def pwd = 'secret'
def host = 'server.windows2008r2'
def port = '5985'

remoteManagement("${user}:${pwd}@${host}:${port}") {
  cp {
    from { localFile '7z920.exe' }
    into { remoteDir 'C:\\temp' }
  }
  exec 'c:\\temp\\7z920 /S /D=c:\\Program Files\\7-zip'
  exec 'c: && cd c:\\"Program Files"\\7-zip && 7z.exe a log_back_up.zip c:\\apache-tomcat\\logs\\*'

  cp {
    from {remoteFile 'c:\\Program Files\\7-zip\\log_back_up.zip'}
    into {localDir './temp/'}
  }
}