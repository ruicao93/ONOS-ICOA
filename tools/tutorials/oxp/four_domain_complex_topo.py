#!/usr/bin/python

"""
    This example create 7 sub-networks to connect 7  domain controllers.
    Each domain network contains at least 5 switches.
    For an easy test, we add 2 hosts per switch.

    So, in our topology, we have at least 35 switches and 70 hosts.
    Hope it will work perfectly.

"""

from mininet.net import Mininet
from mininet.node import Controller, RemoteController, OVSSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.link import Link, Intf, TCLink
from mininet.topo import Topo
import logging
import os
def multiControllerNet():
    net = Mininet(controller=None,switch=OVSSwitch, link=TCLink, autoSetMacs=True)
    limit_bw = 30
    controller_ips=[ "192.168.1.5", "192.168.1.3","192.168.1.6", "192.168.1.7"]
    #controller_ips=[ "10.103.90.102", "10.103.90.102","10.103.90.102", "10.103.90.102"]
    controller_list = []
    switch_list = []
    host_list = []
    local_ip = "10.103.90.102"
    test_ip = "10.117.2.62"
    test2_ip = "10.117.2.43"
    for i in range(0, len(controller_ips)):
        name = "controller%s" % str(i+1)
        controller = net.addController(name, controller=RemoteController,ip=controller_ips[i],port=6653)
        controller_list.append(controller)

    controller_num = len(controller_ips)
    switch_num = controller_num * 5
    host_num = switch_num * 2

    for i in range(0, switch_num):
        switch_list.append(net.addSwitch("s%d" % (i+1)))
        info("Create switch: %s \n" % switch_list[i])
        host_list.append(net.addHost("h%d" % (2*i+1)))
        host_list.append(net.addHost("h%d" % (2*i+2)))
        net.addLink(switch_list[i], host_list[2*i])
        net.addLink(switch_list[i], host_list[2*i+1])

    for i in range(0, 4):
        for j in range(0, 5 - 1):
            for k in range(j + 1, 5):
                info("Add link s%d ---> s%d\n" % (i * 5 + j + 1, i * 5 + k + 1))
                net.addLink(switch_list[i * 5 + j], switch_list[i * 5 + k])

    # net.addLink(switch_list[5], switch_list[6])
    # net.addLink(switch_list[5], switch_list[7])
    # net.addLink(switch_list[5], switch_list[8])
    # net.addLink(switch_list[5], switch_list[9])
    # net.addLink(switch_list[6], switch_list[7])
    # net.addLink(switch_list[6], switch_list[8])
    # net.addLink(switch_list[6], switch_list[9])
    # net.addLink(switch_list[7], switch_list[8])
    # net.addLink(switch_list[7], switch_list[9])
    # net.addLink(switch_list[8], switch_list[9])

    # domain1 -> others
    net.addLink(switch_list[4], switch_list[6], bw=limit_bw )
    net.addLink(switch_list[4], switch_list[10], bw=limit_bw )
    # domain2 -> others
    net.addLink(switch_list[7], switch_list[18], bw=limit_bw )

    # domain3 -> others
    net.addLink(switch_list[10], switch_list[16], bw=limit_bw )
    net.addLink(switch_list[7], switch_list[10], bw=limit_bw )

    net.build()
    for controller in controller_list:
        controller.start()

    for i in range(0, switch_num):
        switch_list[i].start([controller_list[i / 5]])

    for i in range(0, switch_num):
        os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % switch_list[i])

    CLI(net)

    net.stop()

if __name__ == '__main__':
    setLogLevel('info')
    multiControllerNet()