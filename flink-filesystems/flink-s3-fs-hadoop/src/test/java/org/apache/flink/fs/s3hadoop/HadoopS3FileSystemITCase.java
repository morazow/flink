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

package org.apache.flink.fs.s3hadoop;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.core.testutils.AllCallbackWrapper;
import org.apache.flink.core.testutils.TestContainerExtension;
import org.apache.flink.fs.s3.common.MinioTestContainer;
import org.apache.flink.runtime.fs.hdfs.AbstractHadoopFileSystemITTest;

import org.junit.BeforeClass;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertFalse;

/**
 * Unit tests for the S3 file system support via Hadoop's {@link
 * org.apache.hadoop.fs.s3a.S3AFileSystem}.
 *
 * <p><strong>BEWARE</strong>: tests must take special care of S3's <a
 * href="https://docs.aws.amazon.com/AmazonS3/latest/dev/Introduction.html#ConsistencyModel">consistency
 * guarantees</a> and what the {@link org.apache.hadoop.fs.s3a.S3AFileSystem} offers.
 */
public class HadoopS3FileSystemITCase extends AbstractHadoopFileSystemITTest {

    //    @Container
    //    private static final MinioTestContainer MINIO = new MinioTestContainer().withReuse(true);
    @RegisterExtension
    @Order(2)
    private static final AllCallbackWrapper<TestContainerExtension<MinioTestContainer>>
            MINIO_EXTENSION =
                    new AllCallbackWrapper<>(new TestContainerExtension<>(MinioTestContainer::new));

    @BeforeClass
    public static void setup() throws IOException {
        // check whether credentials exist
        //        S3TestCredentials.assumeCredentialsAvailable();
        //        MINIO.start();
        final MinioTestContainer minio = MINIO_EXTENSION.getCustomExtension().getTestContainer();

        // initialize configuration with valid credentials
        final Configuration conf = new Configuration();
        minio.setS3ConfigOptions(conf);
        minio.initializeFileSystem(conf);
        //        conf.setString("s3.access.key", S3TestCredentials.getS3AccessKey());
        //        conf.setString("s3.secret.key", S3TestCredentials.getS3SecretKey());
        //        FileSystem.initialize(conf);

        //        basePath = new Path(S3TestCredentials.getTestBucketUri() + "tests-" +
        // UUID.randomUUID());
        basePath = new Path(minio.getS3UriForDefaultBucket() + "tests-" + UUID.randomUUID());
        fs = basePath.getFileSystem();
        consistencyToleranceNS = 30_000_000_000L; // 30 seconds

        // check for uniqueness of the test directory
        // directory must not yet exist
        assertFalse(fs.exists(basePath));
    }
}
