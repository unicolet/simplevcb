Simple VCB (vcb for the rest of us)
===================================

Getting backups done in vmware environments is easy.
Getting backups done right is difficult.

Simple VCB is a wrapper script around *vcbMounter* which attempts to
simplify its usage by providing a free, off-the-shelf scripted solution
for those who can't afford/dont'want to use commercial backup solutions.

Features
--------

Simple VCB can:

- execute backups by commanding to more that one esx/vsphere/vi server (as long as
  the vcb host can mount their storage)

- define per server or per vm backup policies

- the backup policy allows you to specify interval (how often we should backup the vms, min
  frequency is every day) and protection (how many copies we should keep around
  for each vm)

For instance you can tell Simple VCB to backup only vms vm1,vm2 and vm3
on server esxi01 (not vm4 because maybe it is not in production yet).
Then you could specify that for vm1 and vm2 you want daily backups and
that at most 14 copies should be kept around. 
vm3 instead can be backed up up only once a week and two copies are enough.

Installation
------------

Download and install Java 1.6 (or better) and Groovy from:

http://www.oracle.com/technetwork/java/javase/downloads/index.html

http://groovy.codehaus.org/

Copy this script and the sample cfg file in a directory of you choosing
on the VCB Server.

Edit the configuration file (it is commented and should be self-explanatory)
to suit your enviroment.

Edit the vcbMounter flags (in simplevcb.groovy) you don't like the defaults.

Run it as follows:

      PATH/TO/groovy.bat simplevcb.groovy

You can also write a bat file to simplify execution.

TODO
----

Simple VCB is not perfect, the following features are planned:

- make the vcbMounter flags configurable (at the moment they are hardcoded in the script)

- make the vcbMounter path configurable (at the moment it is hardcoded in the script)

- better logging [DONE, via log4j]

- email alerting (can currently be done with help from http://www.blat.net/)

DISCLAIMER
----------

Use at your own risk. Be careful in prod environments, test on expendable vms first.
Just because it works well for me it doesn't mean it will work for you.
You have been warned.

If it does work you might as well drop me a line.

