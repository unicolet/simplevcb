Simple VCB (vcb for the rest of us)
===================================

Getting backups done in vmware environments is easy.
Getting backups done right is difficult.

Simple VCB is a wrapper script around *vcbMounter* which attempts to
simplify its usage by providing an off-the-shelf free scripted solution
for those who can't afford/dont'want to use commercial backup solutions.

Installation
------------

Download and install Java 1.6 (or better) and Groovy from:

http://www.oracle.com/technetwork/java/javase/downloads/index.html

http://groovy.codehaus.org/

Copy this script and the sample cfg file in a directory of you choosing
on the VCB Server.

Edit the configuration file (it is commented and should be self-explanatory)
to suit your enviroment.

Run it as follows:

      PATH/TO/groovy.bat simplevcb.groovy

TODO
----

Simple VCB is not perfect, the following features are planned:

- make the vcbMounter flags configurable (at the moment they are hardcoded in the script)

- make the vcbMounter path configurable (at the moment it is hardcoded in the script)

- better logging

- email alerting (can currently be done with help from http://www.blat.net/)



