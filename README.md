# r-trees

This is a repo for the INFO-F203 course at ULB in BA-INFO:2

This project use the **Geotools** and **Javatuples** libraries to
make the project work

## Depedencies

- Java 17

> The project was not tested with other version of Java

- Geotools and Javatuples

> Installed with the command in [Installation](#installation)

## Installation

```bash
make install
```

## Compilation

```bash
make 
```

## Execute The Programm

```bash
make run
```

You can specify arguments to the program by typing this:

`make run ARGS="<max child InternalNode> <min child InternalNode> <split methode>"`

> The program will then ask you to choose a data set amon a list.
You 'll just need to enter a name of the list.

## Clean all the compilations files

```bash
make clean
```

## Data Sets

All the data sets are in the data folder each folder except
the zip contain all the data files to run the project with differents data sets.

### belgium_sectors

Division of Belgium into statistical sectors

### french_regions

French municipalities

### World countries

> Quite a self-explanatory name

## Current Problems

- Cannot upload shp and dbf file to github from belgium_sectors
event while using git-lfs, here is the link to the [entire folder](https://statbel.fgov.be/fr/open-data/secteurs-statistiques-2022)

## Contributors

@callhc

@JustRayCB
