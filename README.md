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
> If none attribute is passed, the default will be used

⚠️ The attributes of the feature is used to make the label of the leaf, please change the attribute
name in the `getAttribute("<name>")` in Main.java
according to the dataset.

You can change the dataset file in Main.java by changing the value of filename

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

### Personnal dataset 

https://datacatalog.worldbank.org/search/dataset/0038272/World-Bank-Official-Boundaries

## Current Problems

- Cannot upload shp and dbf file to github from belgium_sectors
event while using git-lfs, here is the link to the [entire folder](https://statbel.fgov.be/fr/open-data/secteurs-statistiques-2022)



## Contributors

@callhc

@JustRayCB
