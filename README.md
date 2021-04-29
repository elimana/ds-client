# COMP3100: DSClient Stage 2

*by Eli Mana*


DSClient is a client-Side simulator for the Distributed System Simulator 'ds-sim'. This client implements more advanced job scheduling algorithms with improved performance following three baseline algorithms (FF, BF and WF). The scheduling algorithm prioritises the maximisation of average resource utilisation, then the minimisation of average turnaround time, and finally the minimisation of total server rental cost.

To compile the program, run the *makefile.sh* script and class files will be generated in the *compiled* folder.

To run the program, use the command `java DSClient.class`, optionally with the argument `-g` to force the program to use 'GETS All' to retrieve the server information instead of parsing the *ds-system.xml* file.