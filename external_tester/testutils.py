#!/usr/bin/env python3
import json
import os
import filecmp
import tempfile
import pathlib
import csv
from collections import namedtuple

TempDirectoryData = namedtuple('TempDirectoryData', 'dirPath initializationPath resultPath mablSpecPath')

initializationConfigurationPath = "wt/mm.json"
simulationConfigurationPath = "wt/start_message.json"
mablExample = "wt/example1.mabl"
folderWithModuleDefinitions =  "../typechecker/src/main/resources/org/intocps/maestro/typechecker/"
mablExpansionExample="wt/expansion_example.mabl"

# Update paths to FMUs
def retrieveConfiguration():
    config = json.load(open(initializationConfigurationPath))
    config["fmus"]["{crtl}"]=pathlib.Path(os.path.abspath('wt/watertankcontroller-c.fmu')).as_uri()
    config["fmus"]["{wt}"]=pathlib.Path(os.path.abspath('wt/singlewatertank-20sim.fmu')).as_uri()
    return config

def retrieveSimulationConfiguration():
    config = json.load(open(simulationConfigurationPath))
    return config

def compareCSV(expected, actual):
    if os.path.exists(expected):
        convert(expected)
        compareResult = True

        with open(expected) as expectedCSV, open(actual) as actualCSV:
            expectedReader = csv.DictReader(expectedCSV)
            
            expectedReader.fieldnames
            actualReader = csv.DictReader(actualCSV)

            if not(set(expectedReader.fieldnames) == set(actualReader.fieldnames)):
                print("Columns does not match!")
                compareResult = False

            elif not(expectedReader.__sizeof__() == actualReader.__sizeof__()):
                 print(f"Column lengths does not match! {expectedReader.__sizeof__()} {actualReader.__sizeof__()}")
                 compareResult = False

            else: 
                for actualRow, expectedRow in zip(actualReader, expectedReader):
                    if not(compareResult):
                        break
                    for expectedColumn in expectedReader.fieldnames:
                        if not(actualRow[expectedColumn] == expectedRow[expectedColumn]):
                            print(f"Value mismatch for column '{expectedColumn}' on line {expectedReader.line_num}. Expected value: {actualRow[expectedColumn]} and actual value: {expectedRow[expectedColumn]}")
                            compareResult = False

        if not compareResult:
            print("ERROR: CSV files {} and {} do not match!".format(expected, actual))
            return False
        else:
            print("CSV files match")
            return True
    else:
        print(f"ERROR: {expected} doest not exist!")
        return False

def compare(strPrefix, expected, actual):
    if os.path.exists(expected):
        convert(expected)

        compareResult = filecmp.cmp(expected, actual)
        if not compareResult:
            print("ERROR: {}: Files {} and {} do not match".format(strPrefix, expected, actual))
            return False
        else:
            print("%s: Files match" % strPrefix)
            return True
    else:
        print("ERROR: %s: No results file exists within wt. Results are not compared." % strPrefix)
        return False

def convert(expected):
    # Converts the expected results.csv to the current OS line ending format as COE outputs using current OS line endings
    with open(expected, 'r') as f:
        content = f.read()

    with open(expected, 'w+') as f:
        f.write(content)
        
def printSection(section):
    hashes = "###############################"
    print("\n" + hashes)
    print(section)
    print(hashes)

def createAndPrepareTempDirectory():
    tempDirectory = tempfile.mkdtemp()
    print("Temporary directory: " + tempDirectory)

    config = retrieveConfiguration()
    print("Initialization config: %s" % json.dumps(config))
    newInitializationFilePath = tempDirectory+"/initialization.json"
    with open(newInitializationFilePath, 'w') as newInitFIle:
        json.dump(config, newInitFIle)
    resultPath = tempDirectory+"/actual_result.csv"
    mablSpecPath = tempDirectory + "/spec.mabl"
    return TempDirectoryData(tempDirectory, newInitializationFilePath, resultPath, mablSpecPath)

def checkMablSpecExists(mablSpecPath):
    if os.path.isfile(mablSpecPath):
        print("MaBL Spec exists at: " + mablSpecPath)
    else:
        raise Exception(f"Mable spec does not exist at {mablSpecPath}")
