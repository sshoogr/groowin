# groowin

## Overview

The `groowin` is a **Groovy**-based **DSL** library for working with remote (Windows) servers through **WinRM**. The **DSL** allows:

- connecting,
- executing remote commands,
- copying files and directories.

The library was jointly developed by **Aestas/IT** (http://aestasit.com) and **NetCompany A/S** (http://www.netcompany.com/) to support the quickly growing company's operations and hosting department.

### Using `groowin` in Groovy scripts

The easiest way to use `groowin` in a **Groovy** script is by importing the dependency using [Grape](http://groovy.codehaus.org/Grape).

    @Grab('com.aestasit.infrastructure.groowin:groowin:0.1.8')
    import static com.aestasit.winrm.DefaultWinRM.*

The entry point for using the **DSL** is the `remoteManagement` method, which accepts a closure with **Groovy** or **DSL** code:

    remoteManagement {
      
      host     = '127.0.0.1'
      user     = 'Administrator'
      password = 'secret'
      
      exec 'del', 'C:\\temp.txt'
      remoteFile('C:\\my.conf').text = "enabled=true"
      
    }

For more use cases, please refer to the following sections or to the `examples` folder in this repository.


### Working with files in `groowin`

For processing remote files `groowin` uses [jCIFS](http://jcifs.samba.org/) library.
jCIFS implements the CIFS/SMB networking protocol. To connect to a remote host using the CIFS protocol, ensure the host is reachable on port 445.
Ensure that the user that you will use to connect to the WinRM host has access to shares that correspond to the directory you want to access.

### Running `groowin` integration tests

Project's integration tests can be found in the `./src/integrationTest/` folder.
To run the integration tests:

1. Create base box using [groowin-test-box](https://github.com/aestasit/groowin-test-box) project. After finishing you will have `windows_2008_r2_virtualbox.box`
2. Add 'aestasit/2008r2' box using the command `vagrant box add --name aestasit/2008r2 windows_2008_r2_virtualbox.box`
3. Execute `vagrant up` using the `./Vagrantfile` file
4. execute `gradle integration` command to run integration tests