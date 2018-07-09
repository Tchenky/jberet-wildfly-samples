/*
 * Copyright (c) 2016 Red Hat, Inc. and/or its affiliates.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.jberet.samples.wildfly.clusterChunkServlet;

import javax.batch.runtime.BatchStatus;

import org.jberet.rest.client.BatchClient;
import org.jberet.rest.entity.JobExecutionEntity;
import org.jberet.rest.entity.StepExecutionEntity;
import org.jberet.samples.wildfly.common.BatchTestBase;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Ignore("need to configure and start WildFly cluster first")
public final class ClusterChunkServletIT extends BatchTestBase {
    private static final String clusterChunkServlet = "clusterChunkServlet";
    private static final String clusterBatchletServlet = "clusterBatchletServlet";
    private static final String clusterChunkServletStop = "clusterChunkServletStop";
    private static final String clusterBatchletServletStop = "clusterBatchletServletStop";
    private static final long waitForCompletionMillis = 60000;

    protected static final String BASE_URL_1 = "http://localhost:8230/";

    /**
     * The full REST API URL, including scheme, hostname, port number, context path, servlet path for REST API.
     * For example, "http://localhost:8080/testApp/api"
     */
    private static final String restUrl = BASE_URL_1 + "clusterChunkServlet/api";

    private BatchClient batchClient = new BatchClient(restUrl);

    @Override
    protected BatchClient getBatchClient() {
        return batchClient;
    }

    @Test
    public void clusterChunkServlet() throws Exception {
        runChunkOrBatchletJob(clusterChunkServlet);
    }

    @Test
    public void clusterBatchletServlet() throws Exception {
        runChunkOrBatchletJob(clusterBatchletServlet);
    }

    @Test
    public void stopRemoteChunkPartitions() throws Exception {
        stopRemotePartitions(clusterChunkServletStop);
    }

    @Test
    public void stopRemoteBatchletPartitions() throws Exception {
        stopRemotePartitions(clusterBatchletServletStop);
    }

    private void runChunkOrBatchletJob(final String jobName) throws Exception {
        final JobExecutionEntity jobExecutionEntity = batchClient.startJob(jobName, null);
        Thread.sleep(waitForCompletionMillis);
        final JobExecutionEntity jobExecution1 = batchClient.getJobExecution(jobExecutionEntity.getExecutionId());
        assertEquals(BatchStatus.COMPLETED, jobExecution1.getBatchStatus());

        final StepExecutionEntity[] stepExecutions = batchClient.getStepExecutions(jobExecution1.getExecutionId());
        assertEquals(1, stepExecutions.length);
        final StepExecutionEntity stepExecutionEntity = stepExecutions[0];
        Assert.assertEquals(BatchStatus.COMPLETED, stepExecutionEntity.getBatchStatus());
    }

    private void stopRemotePartitions(final String jobName) throws Exception {
        JobExecutionEntity jobExecutionEntity = batchClient.startJob(jobName, null);
        Thread.sleep(20000);
        batchClient.stopJobExecution(jobExecutionEntity.getExecutionId());

        Thread.sleep(120000);

        jobExecutionEntity = batchClient.getJobExecution(jobExecutionEntity.getExecutionId());
        final StepExecutionEntity[] stepExecutions = batchClient.getStepExecutions(jobExecutionEntity.getExecutionId());
        final StepExecutionEntity stepExecution = stepExecutions[0];

        System.out.printf("job batch status: %s, step batch status: %s%n",
                jobExecutionEntity.getBatchStatus(), stepExecution.getBatchStatus());
        assertEquals(BatchStatus.STOPPED, jobExecutionEntity.getBatchStatus());
        assertEquals(BatchStatus.STOPPED, stepExecution.getBatchStatus());
    }

}
