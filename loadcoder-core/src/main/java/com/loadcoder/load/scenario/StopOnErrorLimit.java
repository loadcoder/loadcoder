package com.loadcoder.load.scenario;


public class StopOnErrorLimit {

    private int errorLimit;
    private int numberOfErrors;

    public StopOnErrorLimit(int errorLimit) {
        this.errorLimit = errorLimit;
    }

    public boolean stopExecution(int errorLimit, int numberOfErrors) {
        if (numberOfErrors < errorLimit) {
            return false;
        }
        System.out.println(" Execution stopped due to reached error limit: " + errorLimit + ", errors: " + numberOfErrors);
        return true;
    }

    public int getErrorLimit() {
        return errorLimit;
    }

    public int getNumberOfErrors() {
        return numberOfErrors;
    }

    public void errorCounter(boolean status) {
        if (!status) {
            this.numberOfErrors++;
        }
    }
}
