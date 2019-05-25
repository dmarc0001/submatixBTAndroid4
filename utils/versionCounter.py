#!/usr/bin/python3
# -*- coding: utf-8 -*-

import datetime
import time
import re
import shutil


"""
BUILD-Nummer hochzählen, Builddatum erzeugen
"""
__author__ = 'Dirk Marciniak'
__copyright__ = 'Copyright 2019'
__license__ = 'GPL'
__version__ = '1.0'

BUILD_FILEPATH = 'src/main/java/de/dmarcini/submatix/android4/full/utils/'
BUILD_JAVA_FILE = BUILD_FILEPATH + 'BuildVersion.java'
TEMP_BUILD_JAVA_FILE = BUILD_FILEPATH + 'BuildVersion.java.tmp'

def main():
    """Der Conter Zähler"""
    print("PYTHON SCRIPT STARTED...")
    counterPattern = re.compile(r"\s+private\s+static\s+final\s+long\s+buildNumber\s+=\s+\d+L;.*")
    buildDatePattern = re.compile(r"\s+private\s+static\s+final\s+long\s+buildDate\s+=\s+\d+L;.*")
    counterValuePattern = re.compile(r"\d+")
    #
    # mache einen Zeitstempel (Java timestamp in ms seit 1970)
    #
    today_datetime = int(time.mktime(datetime.datetime.now().timetuple())) * 1000
    today_timestamp_line = "  private static final long buildDate = " + str(today_datetime) + 'L;\n'
    print("update build file...")
    #
    # oeffne die originaldatei zum lesen
    #
    readfile = open(BUILD_JAVA_FILE, "r")
    #
    # oeffne die zu modifizierende Datei zum schreiben/ueberschreiben
    # schreibe nur Newline aka Linux
    #
    writefile = open(TEMP_BUILD_JAVA_FILE, "w", newline='\n')
    #
    # alle Zeilen der Datei bearbeiten
    #
    for line in readfile:
        if counterPattern.match(line):
            # finde den Wert
            numVal = int(counterValuePattern.findall(line)[0])
            print("found build count value: {}, increment...".format(numVal))
            numVal += 1
            # und in die Datei neu schreiben
            writefile.write("  private static final long buildNumber = {}L;\n".format(numVal))
            continue
        if buildDatePattern.match(line):
            numVal = int(counterValuePattern.findall(line)[0])
            print("found timestamp: {}, update...".format(numVal))
            writefile.write(today_timestamp_line)
            continue
        writefile.write(line)
    readfile.close()
    writefile.flush()
    writefile.close()
    #
    # umbrenennen, verchieben
    #
    shutil.move(TEMP_BUILD_JAVA_FILE, BUILD_JAVA_FILE)
    print("update build file...end")

if __name__ == '__main__':
    main()

