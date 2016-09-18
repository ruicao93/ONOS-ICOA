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
	domain2 = net.addController("domain2", controller=RemoteController,ip="10.117.2.43",port=6633)

	s1 = net.addSwitch("s1")
	s2 = net.addSwitch("s2")
	s3 = net.addSwitch("s3")

	s4 = net.addSwitch("s4")
	s5 = net.addSwitch("s5")
	s6 = net.addSwitch("s6")

	h1 = net.addHost("h1")
	h2 = net.addHost("h2")
	h3 = net.addHost("h3")
	h4 = net.addHost("h3")
	h5 = net.addHost("h5")
	h6 = net.addHost("h6")

	h7 = net.addHost("h7")
	h8 = net.addHost("h8")
	h9 = net.addHost("h9")
	h10 = net.addHost("h10")
	h11 = net.addHost("h11")
	h12 = net.addHost("h12")


	net.addLink(s1, s2)
	net.addLink(s1, s3)
	net.addLink(s2, s3)

	net.addLink(s4, s5)
	net.addLink(s4, s6)
	net.addLink(s5, s6)

	net.addLink(s2, s5)
	net.addLink(s3, s6)

	net.addLink(s1, h1)
	net.addLink(s1, h2)
	net.addLink(s2, h3)
	net.addLink(s2, h4)
	net.addLink(s3, h5)
	net.addLink(s3, h6)

	net.addLink(s4, h7)
	net.addLink(s4, h8)
	net.addLink(s5, h9)
	net.addLink(s5, h10)
	net.addLink(s6, h11)
	net.addLink(s6, h12)

	net.build()
	domain1.start()
	domain2.start()
	s1.start([domain1])
	s2.start([domain1])
	s3.start([domain1])
	s4.start([domain2])
	s5.start([domain2])
	s6.start([domain2])
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s1)
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s2)
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s3)
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s4)
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s5)
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s6)
	CLI(net)

	net.stop()

if __name__ == '__main__':
    setLogLevel('info')
    multiControllerNet()