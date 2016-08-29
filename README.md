# groupme-utils
### A series of useful things for groupme
[GroupMe](https://www.groupme.com/) is a free group messaging app.

This repository contains several scripts for accomplishing various tasks relating to GroupMe's Open API.
Most importantly, it contains a wrapper for GroupMe's API to make writing future scripts easier.
I'm still in the process of porting some [old](https://github.com/TheGuyWithTheFace/groupme-absentee-finder),
[hacky](https://github.com/TheGuyWithTheFace/groupme-dump)
[python](https://github.com/TheGuyWithTheFace/groupme-bot-detector) scripts I did when I first started working
with the groupme api, and will continue to add useful things here as I think of them.

## Building
Make sure you have maven installed.
From the command line, in the base directory of this project:

    mvn install
    mvn package
  
## Running
After building, the compiled .jar file will be located in the `target/` directory of this project.
Run it with 

    java -jar target/groupme-utils-0.5.jar
