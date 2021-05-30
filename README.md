# COMP3100: DSClient Stage 2

*by Eli Mana*


DSClient is a client-Side simulator for the Distributed System Simulator 'ds-sim'. This client implements more advanced job scheduling algorithms with improved performance following three baseline algorithms (FF, BF and WF). The scheduling algorithm prioritises the maximisation of average resource utilisation and the minimisation of average turnaround time, while maintaining a reasonable total server rental cost.

To compile the program, run the *makefile.sh* script and class files will be generated in the *compiled* folder.

To run the program, use the command `java DSClient`, optionally with the arguments:

- `-g` | `--getsall` to force the program to use 'GETS All' to retrieve the server information instead of parsing the *ds-system.xml* file.
- `-e` | `--est` to use estimated server waiting time instead of the more precise next available server time to speed up the program.
- `-t` | `--termidle` to terminate servers when they are idle.
- `-b` | `--boot` to consider booting servers as available servers with no waiting jobs.
- `-f` | `--fitcore` to calculate server fitness in Best Fit by only the number of available cores.