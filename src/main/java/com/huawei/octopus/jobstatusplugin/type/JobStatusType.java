package com.huawei.octopus.jobstatusplugin.type;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JobStatusType {
    /**
     * The build is running.
     */
    public static final String IN_PROGRESS = "IN_PROGRESS";
    
    /**
     * The module was not built.
     * <p>
     * This status code is used in a multi-stage build (like maven2)
     * where a problem in earlier stage prevented later stages from building.
     */
    public static final String NOT_BUILT = "NOT_BUILT";
    
    /**
     * The build had no errors.
     */
    public static final String SUCCESS = "SUCCESS";
    
    /**
     * The build had some errors but they were not fatal.
     * For example, some tests failed.
     */
    public static final String UNSTABLE = "UNSTABLE";
    
    /**
     * The build had a fatal error.
     */
    public static final String FAILURE = "FAILURE";

    /**
     * The build was manually aborted.
     *
     */
    public static final String ABORTED = "ABORTED";
    
    private static final Logger log = Logger.getLogger(JobStatusType.class.getName());
    
    public static boolean isCompleted(String status) {
        switch(status) {
        case JobStatusType.NOT_BUILT:
            return false;
        case JobStatusType.IN_PROGRESS:
            return false;
        case JobStatusType.SUCCESS:
            return true;
        case JobStatusType.FAILURE:
            return true;
        case JobStatusType.ABORTED:
            return true;
        case JobStatusType.UNSTABLE:
            return true;
        default:
            log.log(Level.SEVERE, String.format("status does not exist, status: ", status));
            return false;
        }
    }
}
