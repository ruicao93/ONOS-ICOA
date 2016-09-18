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


def multiControllerNet(con_num=7, sw_num=35, host_num=70):
    "Create a network from semi-scratch with multiple controllers."
    controller_ips=["10.103.90.102", "10.117.2.43", "10.117.2.35", "10.117.2.37"]
    controller_list = []
    switch_list = []
    host_list = []

    inter_bw = 50

    logger = logging.getLogger('ryu.openexchange.test.multi_network')

    net = Mininet(controller=None,
                  switch=OVSSwitch, link=TCLink, autoSetMacs=True)

    i=1
    for ip_str in controller_ips:
        name = 'controller%s' % str(i)
        i += 1
        c = net.addController(name, controller=RemoteController,ip=ip_str,
                              port=6633)
        controller_list.append(c)
        logger.debug("*** Creating %s" % name)

    logger.debug("*** Creating switches")
    switch_list = [net.addSwitch('s%d' % n) for n in xrange(1, int(sw_num)+1)]

    logger.debug("*** Creating hosts")
    host_list = [net.addHost('h%d' % n) for n in xrange(host_num)]

    logger.debug("*** Creating links of host2switch.")
    for i in xrange(0, sw_num):
        #net.addLink(switch_list[i], host_list[i])
        net.addLink(switch_list[i], host_list[i*2])
        net.addLink(switch_list[i], host_list[i*2+1])

    logger.debug("*** Creating interior links of switch2switch.")
    for i in xrange(0, sw_num, sw_num/con_num):
        for j in xrange(sw_num/con_num):
            for k in xrange(sw_num/con_num):
                if j != k and j > k:
                    net.addLink(switch_list[i+j], switch_list[i+k])

    logger.debug("*** Creating intra links of switch2switch.")

    # 0-4  5-9 10-14 15-19 20-24 25-29 30-34
    # domain1 -> others
    net.addLink(switch_list[4], switch_list[6], bw=inter_bw)
    net.addLink(switch_list[4], switch_list[10], bw=inter_bw)
    #net.addLink(switch_list[1], switch_list[20], bw=inter_bw)

    # domain2 -> others
    #net.addLink(switch_list[6], switch_list[10], bw=inter_bw)
    #net.addLink(switch_list[8], switch_list[12], bw=inter_bw)
    net.addLink(switch_list[7], switch_list[18], bw=inter_bw)

    # domain3 -> others
    net.addLink(switch_list[10], switch_list[16], bw=inter_bw)
    net.addLink(switch_list[7], switch_list[10], bw=inter_bw)
    #net.addLink(switch_list[12], switch_list[27], bw=inter_bw)

    # domain4 -> others
    #net.addLink(switch_list[16], switch_list[21], bw=inter_bw)
    #net.addLink(switch_list[18], switch_list[27], bw=inter_bw)
    #net.addLink(switch_list[19], switch_list[34], bw=inter_bw)

    # domain5 -> others
    #net.addLink(switch_list[21], switch_list[27], bw=inter_bw)
    #net.addLink(switch_list[23], switch_list[31], bw=inter_bw)

    # domain6 -> others
    #net.addLink(switch_list[25], switch_list[31], bw=inter_bw)
    #net.addLink(switch_list[27], switch_list[32], bw=inter_bw)

    #domain7 has not need to add links.

    net.build()
    for c in controller_list:
        c.start()

    _No = 0
    for i in xrange(0, sw_num, sw_num/con_num):
        for j in xrange(sw_num/con_num):
            switch_list[i+j].start([controller_list[_No]])
        _No += 1

    logger.info("*** Setting OpenFlow version")
    for sw in switch_list:
        cmd = "sudo ovs-vsctl set bridge %s protocols=OpenFlow13" % sw
        os.system(cmd)

    logger.info("*** Running CLI")
    CLI(net)

    logger.info("*** Stopping network")
    net.stop()

if __name__ == '__main__':
    setLogLevel('info')
    multiControllerNet(con_num=4, sw_num=20, host_num=40)
