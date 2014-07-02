
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  config.vm.box = "winauto/2008R2_puppet"

  config.vm.network "private_network", ip: "192.168.56.101"

  config.winrm.username = "vagrant"
  config.winrm.password = "vagrant"

  config.vm.provider :virtualbox do |v, override|
    v.gui = true
    v.customize ["modifyvm", :id, "--memory", 700]
    v.customize ["modifyvm", :id, "--cpus", 2]
  end

end

