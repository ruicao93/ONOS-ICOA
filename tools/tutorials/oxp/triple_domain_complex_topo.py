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
	domain3 = net.addController("domain2", controller=RemoteController,ip="10.117.2.35",port=6633)

	s1 = net.addSwitch("s1")
	s2 = net.addSwitch("s2")
	s3 = net.addSwitch("s3")

	s4 = net.addSwitch("s4")
	s5 = net.addSwitch("s5")
	s6 = net.addSwitch("s6")

	s7 = net.addSwitch("s7")
	s8 = net.addSwitch("s8")
	s9 = net.addSwitch("s9")

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

	h13 = net.addHost("h13")
	h14 = net.addHost("h14")
	h15 = net.addHost("h15")
	h16 = net.addHost("h16")
	h17 = net.addHost("h17")
	h18 = net.addHost("h18")

	net.addLink(s1, s2)
	net.addLink(s1, s3)
	net.addLink(s2, s3)

	net.addLink(s4, s5)
	net.addLink(s4, s6)
	net.addLink(s5, s6)

	net.addLink(s7, s8)
	net.addLink(s7, s9)
	net.addLink(s8, s9)

	net.addLink(s2, s5)
	net.addLink(s3, s6)
	net.addLink(s3, s8)
	net.addLink(s6, s9)

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

	net.addLink(s7, h13)
	net.addLink(s7, h14)
	net.addLink(s8, h15)
	net.addLink(s8, h16)
	net.addLink(s9, h17)
	net.addLink(s9, h18)

	net.build()
	domain1.start()
	domain2.start()
	domain3.start()

	s1.start([domain1])
	s2.start([domain1])
	s3.start([domain1])
	s4.start([domain2])
	s5.start([domain2])
	s6.start([domain2])
	s7.start([domain3])
	s8.start([domain3])
	s9.start([domain3])

	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s1)
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s2)
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s3)
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s4)
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s5)
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s6)
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s7)
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s8)
	os.system("sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % s9)
	CLI(net)

	net.stop()

if __name__ == '__main__':
    setLogLevel('info')
    multiControllerNet()