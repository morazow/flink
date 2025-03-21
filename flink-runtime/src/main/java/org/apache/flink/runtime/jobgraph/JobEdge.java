/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.jobgraph;

import org.apache.flink.runtime.io.network.api.writer.SubtaskStateMapper;

import static org.apache.flink.util.Preconditions.checkNotNull;

/**
 * This class represent edges (communication channels) in a job graph. The edges always go from an
 * intermediate result partition to a job vertex. An edge is parametrized with its {@link
 * DistributionPattern}.
 */
public class JobEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /** The vertex connected to this edge. */
    private final JobVertex target;

    /** The distribution pattern that should be used for this job edge. */
    private final DistributionPattern distributionPattern;

    /** The channel rescaler that should be used for this job edge on downstream side. */
    private SubtaskStateMapper downstreamSubtaskStateMapper = SubtaskStateMapper.ROUND_ROBIN;

    /** The channel rescaler that should be used for this job edge on upstream side. */
    private SubtaskStateMapper upstreamSubtaskStateMapper = SubtaskStateMapper.ROUND_ROBIN;

    /** The data set at the source of the edge, may be null if the edge is not yet connected. */
    private final IntermediateDataSet source;

    /**
     * Optional name for the data shipping strategy (forward, partition hash, rebalance, ...), to be
     * displayed in the JSON plan.
     */
    private String shipStrategyName;

    private final boolean isBroadcast;

    private final boolean isForward;

    /**
     * Optional name for the pre-processing operation (sort, combining sort, ...), to be displayed
     * in the JSON plan.
     */
    private String preProcessingOperationName;

    /** Optional description of the caching inside an operator, to be displayed in the JSON plan. */
    private String operatorLevelCachingDescription;

    private final int typeNumber;

    /**
     * There are relationships between multiple inputs, if the records corresponding to the same key
     * from one input is split, the corresponding key records from the other inputs must be
     * duplicated (meaning that it must be sent to the downstream nodes where the split data is
     * sent).
     */
    private final boolean interInputsKeysCorrelated;

    /**
     * Whether records with the same key are correlated and must be sent to the same downstream task
     * to be processed together.
     */
    private final boolean intraInputKeyCorrelated;

    /**
     * Constructs a new job edge, that connects an intermediate result to a consumer task.
     *
     * @param source The data set that is at the source of this edge.
     * @param target The operation that is at the target of this edge.
     * @param distributionPattern The pattern that defines how the connection behaves in parallel.
     * @param isBroadcast Whether the source broadcasts data to the target.
     */
    public JobEdge(
            IntermediateDataSet source,
            JobVertex target,
            DistributionPattern distributionPattern,
            boolean isBroadcast,
            boolean isForward,
            int typeNumber,
            boolean interInputsKeysCorrelated,
            boolean intraInputKeyCorrelated) {
        if (source == null || target == null || distributionPattern == null) {
            throw new NullPointerException();
        }
        this.target = target;
        this.distributionPattern = distributionPattern;
        this.source = source;
        this.isBroadcast = isBroadcast;
        this.isForward = isForward;
        this.typeNumber = typeNumber;
        this.interInputsKeysCorrelated = interInputsKeysCorrelated;
        this.intraInputKeyCorrelated = intraInputKeyCorrelated;
    }

    /**
     * Returns the data set at the source of the edge. May be null, if the edge refers to the source
     * via an ID and has not been connected.
     *
     * @return The data set at the source of the edge
     */
    public IntermediateDataSet getSource() {
        return source;
    }

    /**
     * Returns the vertex connected to this edge.
     *
     * @return The vertex connected to this edge.
     */
    public JobVertex getTarget() {
        return target;
    }

    /**
     * Returns the distribution pattern used for this edge.
     *
     * @return The distribution pattern used for this edge.
     */
    public DistributionPattern getDistributionPattern() {
        return this.distributionPattern;
    }

    /**
     * Gets the ID of the consumed data set.
     *
     * @return The ID of the consumed data set.
     */
    public IntermediateDataSetID getSourceId() {
        return source.getId();
    }

    // --------------------------------------------------------------------------------------------

    /**
     * Gets the name of the ship strategy for the represented input, like "forward", "partition
     * hash", "rebalance", "broadcast", ...
     *
     * @return The name of the ship strategy for the represented input, or null, if none was set.
     */
    public String getShipStrategyName() {
        return shipStrategyName;
    }

    /**
     * Sets the name of the ship strategy for the represented input.
     *
     * @param shipStrategyName The name of the ship strategy.
     */
    public void setShipStrategyName(String shipStrategyName) {
        this.shipStrategyName = shipStrategyName;
    }

    /** Gets whether the edge is broadcast edge. */
    public boolean isBroadcast() {
        return isBroadcast;
    }

    /** Gets whether the edge is forward edge. */
    public boolean isForward() {
        return isForward;
    }

    /**
     * Gets the channel state rescaler used for rescaling persisted data on downstream side of this
     * JobEdge.
     *
     * @return The channel state rescaler to use, or null, if none was set.
     */
    public SubtaskStateMapper getDownstreamSubtaskStateMapper() {
        return downstreamSubtaskStateMapper;
    }

    /**
     * Sets the channel state rescaler used for rescaling persisted data on downstream side of this
     * JobEdge.
     *
     * @param downstreamSubtaskStateMapper The channel state rescaler selector to use.
     */
    public void setDownstreamSubtaskStateMapper(SubtaskStateMapper downstreamSubtaskStateMapper) {
        this.downstreamSubtaskStateMapper = checkNotNull(downstreamSubtaskStateMapper);
    }

    /**
     * Gets the channel state rescaler used for rescaling persisted data on upstream side of this
     * JobEdge.
     *
     * @return The channel state rescaler to use, or null, if none was set.
     */
    public SubtaskStateMapper getUpstreamSubtaskStateMapper() {
        return upstreamSubtaskStateMapper;
    }

    /**
     * Sets the channel state rescaler used for rescaling persisted data on upstream side of this
     * JobEdge.
     *
     * @param upstreamSubtaskStateMapper The channel state rescaler selector to use.
     */
    public void setUpstreamSubtaskStateMapper(SubtaskStateMapper upstreamSubtaskStateMapper) {
        this.upstreamSubtaskStateMapper = checkNotNull(upstreamSubtaskStateMapper);
    }

    /**
     * Gets the name of the pro-processing operation for this input.
     *
     * @return The name of the pro-processing operation, or null, if none was set.
     */
    public String getPreProcessingOperationName() {
        return preProcessingOperationName;
    }

    /**
     * Sets the name of the pre-processing operation for this input.
     *
     * @param preProcessingOperationName The name of the pre-processing operation.
     */
    public void setPreProcessingOperationName(String preProcessingOperationName) {
        this.preProcessingOperationName = preProcessingOperationName;
    }

    /**
     * Gets the operator-level caching description for this input.
     *
     * @return The description of operator-level caching, or null, is none was set.
     */
    public String getOperatorLevelCachingDescription() {
        return operatorLevelCachingDescription;
    }

    /**
     * Sets the operator-level caching description for this input.
     *
     * @param operatorLevelCachingDescription The description of operator-level caching.
     */
    public void setOperatorLevelCachingDescription(String operatorLevelCachingDescription) {
        this.operatorLevelCachingDescription = operatorLevelCachingDescription;
    }

    /** Gets typeNumber of the edge. */
    public int getTypeNumber() {
        return typeNumber;
    }

    /** Gets whether the records with same key of this edge are correlated with other inputs. */
    public boolean areInterInputsKeysCorrelated() {
        return interInputsKeysCorrelated;
    }

    /** Gets whether the records with same key of this edge are correlated. */
    public boolean isIntraInputKeyCorrelated() {
        return intraInputKeyCorrelated;
    }

    // --------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("%s --> %s [%s]", source.getId(), target, distributionPattern.name());
    }
}
