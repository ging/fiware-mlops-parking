#!/usr/bin/env python


import socketserver,subprocess,os,requests,json,time,shutil
import logging

class S():
    def execute_maven(self):
        print(os.getcwd())
        logging.info("Packaging program with maven")
        mypath = './prediction-job'
        os.chdir(mypath)
        p = subprocess.Popen(["mvn clean package"],shell=True)
        output, err = p.communicate()
        os.chdir("..")
        print(output)
        logging.info(output)
        print(mypath)
        return mypath


if __name__ == "__main__":
    print("=================================")
    print("Generating JAR")
    print("=================================")
    S().execute_maven()
    print("=================================")
    print("JAR GENERATED")
    print("=================================")