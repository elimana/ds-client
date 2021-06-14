# DSClient: Client-Side Job Scheduler for a Distributed System Simulator 

*by Eli Mana*

## Overview

DSClient is a client-Side simulator for the Distributed System Simulator 'ds-sim' (https://github.com/distsys-MQ/ds-sim). This client implements more advanced job scheduling algorithms with improved performance following three baseline algorithms: First Fit, Best Fit, and Worst Fit (FF, BF and WF). The scheduling algorithm prioritises the maximisation of average resource utilisation and the minimisation of average turnaround time, while maintaining a reasonable total server rental cost.

## Installation 

To compile the program, run the *makefile.sh* script and class files will be generated in the *compiled* folder.

To run the program, use the command `java DSClient`, optionally with the arguments:

- `-g`, `--getsall` to force the program to use 'GETS All' to retrieve the server information instead of parsing the *ds-system.xml* file.
- `-e`, `--est` to use estimated server waiting time instead of the more precise next available server time to speed up the program.
- `-t`, `--termidle` to terminate servers when they are idle.
- `-b`, `--boot` to consider booting servers as available servers with no waiting jobs.
- `-f`, `--fitcore` to calculate server fitness in Best Fit by only the number of available cores.

## Algorithm Description

### Minimum Turnaround Time Algorithm (MTTA)

The *Minimum Turnaround Time Algorithm (MTTA)* is primarily based on the Best Fit algorithm, where jobs are first scheduled to the server with sufficient available resources for the job and with the lowest aggregated fitness value. The aggregated fitness statistic is calculated as follows:

<img src="http://www.sciweavers.org/tex2img.php?eq=fitness%3D%5Cfrac%7Bcore_s%7D%7Bcore_j%7D%2B%5Cfrac%7Bmemory_s%7D%7Bmemory_j%7D%2B%5Cfrac%7Bdisk_s%7D%7Bdisk_j%7D&bc=White&fc=Black&im=jpg&fs=12&ff=arev&edit=0" align="center" border="0" alt="fitness=\frac{core_s}{core_j}+\frac{memory_s}{memory_j}+\frac{disk_s}{disk_j}"/>

s	- an available server.

j - the job to be scheduled.

When there is no capable server with sufficient available resources for the job, the next available server, i.e., the first/quickest server calculated to have sufficient available resources for the job, is selected. To calculate this, the list of running jobs for each capable server is iterated through in ascending order of estimated job end time. This is calculated by summing the job start time and estimated run time. As each running job is iterated through, the job resources are added back to the available server resources and any waiting jobs capable of running are added to the list of running jobs, with their start time set to the estimated end time of the previously removed job. When there are no more waiting jobs, and there are sufficient available resources for the submitted job, the last removed job’s estimated end time is considered as the next available server time for the job. The capable server with the lowest available server time is then selected for the job scheduling.

### Minimum Turnaround Time Algorithm with Resource Utilisation Optimisations (MTTA+RUO)

The *Minimum Turnaround Time Algorithm with Resource Utilisation Optimisations (MTTA+RUO)* utilises the same scheduling algorithm as the Minimum Turnaround Time Algorithm (MTTA) with some additional optimisations to increase resource utilisation. This means that MTTA+RUO also uses the Best Fit algorithm to schedule a job to available servers or calculates and selects the next available server, i.e., the capable server with the lowest available server time, when there are no capable servers with sufficient available resources for the job.

The way in which MTTA+RUO optimises resource utilisation is by minimising the server idle time. When a job is completed by the server, the job scheduling client will check if the server that completed that job is idle, i.e., the server has no running jobs, and then terminates it if it is. This reduces the number of servers that are powered on, but not utilised, therefore increasing both the server utilisation rate, and reducing the wasted rental costs for servers that are powered on but sitting idle. However, this does slightly increase the average turnaround time, as the terminated server must now be booted up once again when a job is scheduled to it, increasing the turnaround time for the job by the server bootup time.

## Evaluation

A simulation setup with a set of 18 test case configurations from ‘ds-sim/configs/other/’ was used to evaluate the results of both the MTTA and MTTA+RUO algorithms with regards to the optimisation of the 3 performance objectives: the minimisation of average turnaround time, maximisation of average resource utilisation, and minimisation of total server rental cost. These results were compared with the results from the 3 baseline algorithms, First Fit, Best Fit and Worst Fit to determine what performance metrics were improved, and the pros and cons of both algorithms.

### 1. Table of Result Averages for Turnaround Time

|                               | FF      | BF      | WF      | MTTA   | MTTA+RUO |
|-------------------------------|---------|---------|---------|--------|----------|
| Average                       | 1473.33 | 1462.83 | 6240.72 | 1433   | 1480.44  |
| Normalised (FF)               | 1       | 0.9929  | 4.2358  | 0.9726 | 1.0048   |
| Normalised (BF)               | 1.0072  | 1       | 4.2662  | 0.9796 | 1.012    |
| Normalised (WF)               | 0.2361  | 0.2344  | 1       | 0.2296 | 0.2372   |
| Normalised (AVG [FF,BF,WF])   | 0.4816  | 0.4782  | 2.0401  | 0.4685 | 0.484    |

In Table 1, we can see that the MTTA scheduling algorithm has the lowest average turnaround time on average across all the test case configurations at 1433 seconds compared to the next lowest value of 1462.83 seconds from Best Fit. The MTTA+RUO scheduling algorithm has an average turnaround time of 1480.44 seconds, higher than both Best Fit and First Fit. However, it is only 1.2% higher than Best Fit, and still significantly lower than Worst Fit at 6240.72 seconds.

### 2. Table of Result Averages for Resource Utilisation

|                               | FF     | BF     | WF     | MTTA   | MTTA+RUO |
|-------------------------------|--------|--------|--------|--------|----------|
| Average                       | 66.79  | 64.94  | 72.85  | 66.08  | 100      |
| Normalised (FF)               | 1      | 0.9724 | 1.0908 | 0.9894 | 1.4973   |
| Normalised (BF)               | 1.0284 | 1      | 1.1218 | 1.0175 | 1.5398   |
| Normalised (WF)               | 0.9168 | 0.8914 | 1      | 0.907  | 1.3726   |
| Normalised (AVG [FF,BF,WF])   | 0.9794 | 0.9523 | 1.0683 | 0.969  | 1.4664   |

In Table 2, we can see that the MTTA scheduling algorithm has a low resource utilisation on average across all the test case configurations at 66.08%, only slightly higher than Best Fit with the lowest average resource utilisation of 64.94%. The MTTA+RUO scheduling algorithm has a perfect average resource utilisation of 100% due to its termination of all idle servers. While this is only simple utilisation, this is still a significant improvement compared to the baseline algorithms, with a 46.64% increase over the average resource utilisation of the 3 baseline algorithms.

### 3. Table of Result Averages for Total Rental Cost

|                               | FF     | BF     | WF     | MTTA   | MTTA+RUO |
|-------------------------------|--------|--------|--------|--------|----------|
| Average                       | 417.9  | 414.42 | 443.03 | 412.2  | 238.77   |
| Normalised (FF)               | 1      | 0.9917 | 1.0601 | 0.9864 | 0.5714   |
| Normalised (BF)               | 1.0084 | 1      | 1.069  | 0.9947 | 0.5762   |
| Normalised (WF)               | 0.9433 | 0.9354 | 1      | 0.9304 | 0.539    |
| Normalised (AVG [FF,BF,WF])   | 0.983  | 0.9748 | 1.0421 | 0.9696 | 0.5617   |

In Table 3, we can see that the MTTA scheduling algorithm has a total rental cost on average across all the test case configurations that is only marginally lower than the 3 baseline algorithms at $412.20, compared to FF at $417.90, BF at $414.42, and WF at $443.03. The MTTA+RUO scheduling algorithm on the other hand, has a significantly lower average total rental cost at $238.77 due to its termination of all idle servers. This is a significant improvement compared to the baseline algorithms, with a 43.83% reduction in average total rental costs compared to the average total rental costs of the 3 baseline algorithms.

### Summary

In conclusion, MTTA does provide a reduction in turnaround time, and is marginally superior to all three baseline algorithms in both turnaround time and total rental cost. However, this reduction in turnaround time is not very significant when compared to Best Fit, with an average reduction of only 2.04%. On the other hand, while MTTA+RUO has a slightly higher turnaround time, it is only 1.2% higher than Best Fit, and instead provides significant performance improvements with a 46.64% increase over the average resource utilisation and a 43.83% reduction in average total rental costs compared to the 3 baseline algorithms. Thus, MTTA+RUO is the suggested job scheduling algorithm to use as it can provide significant performance benefits in maximising resource utilisation and minimising total rental cost while maintaining a low average turnaround time comparable to that of Best Fit and MTTA.
