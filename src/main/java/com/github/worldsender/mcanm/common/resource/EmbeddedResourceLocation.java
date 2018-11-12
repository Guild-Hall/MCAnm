package com.github.worldsender.mcanm.common.resource;

import com.github.worldsender.mcanm.common.util.CallResolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class EmbeddedResourceLocation extends ResourceLocationAdapter {
    private final URL url;
    private final String name;

    public EmbeddedResourceLocation(String name) {
        this(name, CallResolver.INSTANCE.getCallingClass().getClassLoader());
    }
    public EmbeddedResourceLocation(String name, ClassLoader loader) {
        this(loader.getResource(name), name);
    }

    public EmbeddedResourceLocation(URL url) {
        this(url, null);
    }

    public EmbeddedResourceLocation(URL url, String resourceName) {
        this.url = url;
        this.name = resourceName;
    }

    private static InputStream createStream(URL url) throws IOException {
        if (url == null) {
            throw new IOException("Couldn't open the stream");
        }
        return url.openStream();
    }

    @Override
    public IResource open() throws IOException {
        return new EmbeddedResource(url);
    }

    @Override
    public String getResourceName() {
        return url != null ? url.toString() : name != null ? name : "<unnamed resource>";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof EmbeddedResourceLocation)) {
            return false;
        }
        EmbeddedResourceLocation other = (EmbeddedResourceLocation) obj;
        if (url == null) {
            return other.url == null;
        } else return url.equals(other.url);
    }

    private /* static */ class EmbeddedResource extends ResourceAdapter {
        public EmbeddedResource(URL url) throws IOException {
            super(EmbeddedResourceLocation.this, createStream(url));
        }
    }
}
