# D20PRO-Hero-Lab-Native-Importer
A native Hero Lab .por importer tool for D20PRO Virtual Tabletop.

## Setup
Create a `gradle.properties` file in the working directory and populate it with the following line:
```
targetD20PRODir=D:/apps/d20Pro3.7.3
```
where `targetD20PRODir` is where you have installed D20PRO. This property is used to:
1. find the `D20PRO.jar` and other dependencies needed to compile the JAR. 
2. deploy the import .jar to the proper location in `judge\addon\creature`.
  
For convenience a `gradle.properties.sample` file is included. Make a copy of this file and rename it to `gradle.properties`.  
  
## Usage
First set up the `gradle.properties` as directed above. If a valid `targetD20PRODir` is not found, the build will fail.

After making any code changes, run the `buildAndCopyJarToD20PRO` task. This task will compile the code, build the .jar, and copy it to the specified D20PRO installation.

There is no need to restart D20PRO after rebuilding the import JAR. Simply start the creature import process again and it will hotswap in the latest code.  