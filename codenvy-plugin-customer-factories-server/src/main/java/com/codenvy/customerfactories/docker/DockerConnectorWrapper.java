/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.customerfactories.docker;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.params.BuildImageParams;
import org.eclipse.che.plugin.docker.client.params.PushParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

public class DockerConnectorWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(DockerConnectorWrapper.class);
    private final DockerConnector dockerConnector;

    @Inject
    public DockerConnectorWrapper(final DockerConnector dockerConnector) {
        this.dockerConnector = dockerConnector;
    }

    /**
     * @param imageName
     * @param files
     * @return the id of the created Docker image
     * @throws ServerException
     */
    public String buildImage(final String imageName, File... files) throws ServerException {
        final BuildImageParams buildParams = BuildImageParams.create(files)
                                                             .withRepository(imageName)
                                                             .withTag("latest");
        String imageId;
        try {
            imageId = dockerConnector.buildImage(buildParams, progressStatus -> {
                LOG.debug(progressStatus.getStatus());
            });
        } catch (IOException e) {
            throw new ServerException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerException(e.getLocalizedMessage(), e);
        }

        if (imageId == null) {
            throw new ServerException("A problem occurred during creation of the Docker image.");
        }

        return imageId;
    }

    /**
     * @param repositoryName
     * @return the digest of the push operation
     * @throws ServerException
     */
    public String pushImage(String registryUrl, String repositoryName) throws ServerException {
        final PushParams pushParams = PushParams.create(repositoryName)
                                                .withRegistry(registryUrl);
        String digest;
        try {
            digest = dockerConnector.push(pushParams, progressStatus -> {
                LOG.debug(progressStatus.getStatus());
            });
        } catch (IOException e) {
            throw new ServerException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerException(e.getLocalizedMessage(), e);
        }

        if (digest == null) {
            throw new ServerException("A problem occurred during pushing of the Docker image");
        }

        return digest;
    }
}
