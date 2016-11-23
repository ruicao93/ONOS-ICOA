ONOS : Open Network Operating System
====================================

# ONOS-ICOA项目

### 项目描述
ICOA全称：Inter Controller ONOS Application
以东西向协议OXP作为支持在ONOS上开发APP，支持异构控制器之间通过OXP进行跨SDN域的协同工作。修改了部分ONOS源码，在后期优化中，逐渐消除对ONOS源码的修改，尽量作为一个独立的APP.

### What is ONOS?
ONOS is a new SDN network operating system designed for high availability,
performance, scale-out.

### Top-Level Features

* High availability through clustering and distributed state management.
* Scalability through clustering and sharding of network device control.
* Performance that is good for a first release, and which has an architecture
  that will continue to support improvements.
* Northbound abstractions for a global network view, network graph, and
  application intents.
* Pluggable southbound for support of OpenFlow and new or legacy protocols.
* Graphical user interface to view multi-layer topologies and inspect elements
  of the topology.
* REST API for access to Northbound abstractions as well as CLI commands.
* CLI for debugging.
* Support for both proactive and reactive flow setup.
* SDN-IP application to support interworking with traditional IP networks
  controlled by distributed routing protocols such as BGP.
* IP-Optical use case demonstration.

Checkout out our [website](http://www.onosproject.org) and our
[tools](http://www.onosproject.org/software/#tools)
