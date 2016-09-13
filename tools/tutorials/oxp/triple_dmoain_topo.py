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
	domain1 = net.addController("domain1", controller=RemoteController,ip="10.103.90.102",port=6633)
	domain2 = net.addController("domain2", controller=RemoteController,port=6633)
	domain3 = net.addController("domain1", controller=RemoteController,ip="10.117.2.79",port=6633)
	s1 = net.addSwitch("s1")
	s2 = net.addSwitch("s2")
	s3 = net.addSwitch("s3")

	h1 = net.addHost("h1")
	h2 = net.addHost("h2")
	h3 = net.addHost("h3")
	h4 = net.addHost("h4")
	h5 = net.addHost("h6")
	h6 = net.addHost("h5")

	net.addLink(s1, s2)
	net.addLink(s1, s3)
	net.addLink(s2, s3)

	net.addLink(s1,h1)
	net.addLink(s1,h2)
	net.addLink(s2,h3)
	net.addLink(s2,h4)
	net.addLink(s3,h5)
	net.addLink(s3,h6)

	net.build()
	domain1.start()
	domain2.start()
	domain3.start()
	s1.start([domain1])
	s2.start([domain2])
	s3.start([domain3])
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s1)
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s2)
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s3)

	CLI(net)

	net.stop()

if __name__ == '__main__':
    setLogLevel('info')
    multiControllerNet()