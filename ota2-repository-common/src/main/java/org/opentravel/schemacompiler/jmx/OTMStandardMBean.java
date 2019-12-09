/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.schemacompiler.jmx;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

/**
 * Base class for MBean implementations used to monitor the various OTM repository components.
 */
public abstract class OTMStandardMBean extends StandardMBean {

    /**
     * An MBean whose management interface is determined by reflection on a Java interface.
     * 
     * @param mbeanInterface the interface of the MBean that is implemented by the class
     */
    protected OTMStandardMBean(Class<?> mbeanInterface) {
        super( mbeanInterface, true );
    }

    /**
     * Returns the CPU utilization as a percentage JVM server process.
     * 
     * @return double
     */
    public double getCpuUtilization() {
        double utilization = -1.0;

        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance( "java.lang:type=OperatingSystem" );
            AttributeList attrList = mbs.getAttributes( name, new String[] {"SystemCpuLoad"} );

            if (!attrList.isEmpty()) {
                utilization = (Double) ((Attribute) attrList.get( 0 )).getValue();
            }

        } catch (Exception e) {
            e.printStackTrace( System.out );
            // Ignore error and return a negative value
        }
        return ((int) (utilization * 1000) / 1000.0);
    }

    /**
     * Returns the memory utilization as a percentage for the JVM process.
     * 
     * @return double
     */
    public double getMemoryUtilization() {
        MemoryUsage memUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        double usedMemory = memUsage.getUsed();
        double maxMemory = memUsage.getMax();
        double utilization = usedMemory / maxMemory;

        return ((int) (utilization * 1000) / 1000.0);
    }

    /**
     * Returns the maximum amount of memory (in GB) available to the JVM process.
     * 
     * @return int
     */
    public int getMemoryMaxGB() {
        return (int) (ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() / 1073741824L);
    }

    /**
     * Returns the disk utilization (as a percentage) of the volume used for storage at the specified folder location.
     * 
     * @param volumeFolder a folder location for which to return the volume's storage utilization
     * @return double
     */
    protected double getDiskVolumeUtilization(File volumeFolder) {
        double utilization = -1.0;

        if ((volumeFolder != null) && volumeFolder.exists()) {
            double totalSpace = volumeFolder.getTotalSpace();
            double freeSpace = volumeFolder.getFreeSpace();

            utilization = (totalSpace - freeSpace) / totalSpace;
        }
        return ((int) (utilization * 1000) / 1000.0);
    }

    /**
     * Returns the maximum storage capacity (in GB) of the volume used for storage at the specified folder location.
     * 
     * @param volumeFolder a folder location for which to return the volume's maximum storage capacity
     * @return double
     */
    protected int getDiskVolumeMaxGB(File volumeFolder) {
        int maxGB = -1;

        if ((volumeFolder != null) && volumeFolder.exists()) {
            maxGB = (int) (volumeFolder.getTotalSpace() / 1073741824L);
        }
        return maxGB;
    }

}
