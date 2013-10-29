/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.undertow.server.handlers.resource;

import java.io.File;
import java.io.IOException;

import io.undertow.UndertowLogger;
import io.undertow.UndertowMessages;

/**
 * Serves files from the file system.
 */
public class FileResourceManager implements ResourceManager {

    private volatile String base;

    /**
      * Size to use direct FS to network transfer (if supported by OS/JDK) instead of read/write
      */
    private final long transferMinSize;

    public FileResourceManager(final File base, long transferMinSize) {
        if (base == null) {
            throw UndertowMessages.MESSAGES.argumentCannotBeNull("base");
        }
        String basePath = base.getAbsolutePath();
        if(!basePath.endsWith("/")) {
            basePath = basePath + '/';
        }
        this.base = basePath;
        this.transferMinSize = transferMinSize;

    }

    public File getBase() {
        return new File(base);
    }

    public FileResourceManager setBase(final File base) {
        if (base == null) {
            throw UndertowMessages.MESSAGES.argumentCannotBeNull("base");
        }
        String basePath = base.getAbsolutePath();
        if(!basePath.endsWith("/")) {
            basePath = basePath + '/';
        }
        this.base = basePath;
        return this;
    }

    public Resource getResource(final String p) {
        String path = null;
        //base always ends with a /
        if (p.startsWith("/")) {
            path = p.substring(1);
        } else {
            path = p;
        }
        try {
            File file = new File(base, path);
            if (file.exists()) {
                //security check for case insensitive file systems
                //we make sure the case of the filename matches the case of the request
                //TODO: we should be able to avoid this if we can tell a FS is case sensitive
                if(file.getCanonicalFile().getName().equals(file.getName())) {
                    return new FileResource(file, this, path);
                }
            }
            return null;
        } catch (Exception e) {
            UndertowLogger.REQUEST_LOGGER.debugf(e, "Invalid path %s");
            return null;
        }
    }

    public long getTransferMinSize() {
        return transferMinSize;
    }

    @Override
    public void close() throws IOException {
    }
}
