# The contents below were provided by the Packer Vagrant post-processor
Vagrant.configure("2") do |config|
    config.winrm.username = "vagrant"
    config.winrm.password = "vagrant"

    config.vm.box = "aestasit/2008r2"
  
    config.vm.guest = :windows

    config.vm.network "private_network", ip: "192.168.25.25"
    config.vm.network :forwarded_port, guest: 5985, host: 5985, id: "winrm", auto_correct: true

    config.vm.provider :virtualbox do |v, override|
        v.gui = true
        v.customize ["modifyvm", :id, "--memory", 700]
        v.customize ["modifyvm", :id, "--cpus", 2]
    end
end

