//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.huawei.octopus.jobstatusplugin.util;

import com.apimgt.sdk.auth.signer.DefaultSigner;
import com.apimgt.sdk.auth.signer.RegionSigner;
import com.apimgt.sdk.auth.signer.ServiceSigner;
import com.apimgt.sdk.auth.signer.Signer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SignerFactory {
    private static final String DEFAULT_SIGNER = "DefaultSignerType";
    private static final Map<String, Class<? extends Signer>> SIGNERS = new ConcurrentHashMap();

    private SignerFactory() {
    }

    public static Signer getSigner(String serviceName, String regionName) {
        return createSigner(DEFAULT_SIGNER, serviceName, regionName);
    }

    private static Signer createSigner(String signerType, String serviceName, String regionName) {
        Class<? extends Signer> signerClass = (Class)SIGNERS.get(signerType);
        if (signerClass == null) {
            throw new IllegalArgumentException("unknown signer type: " + signerType);
        } else {
            Signer signer;
            try {
                signer = (Signer)signerClass.newInstance();
            } catch (InstantiationException var6) {
                throw new IllegalStateException("Cannot create an instance of " + signerClass.getName(), var6);
            } catch (IllegalAccessException var7) {
                throw new IllegalStateException("Cannot create an instance of " + signerClass.getName(), var7);
            }

            if (signer instanceof ServiceSigner) {
                ((ServiceSigner)signer).setServiceName(serviceName);
            }

            if (signer instanceof RegionSigner) {
                ((RegionSigner)signer).setRegionName(regionName);
            }

            return signer;
        }
    }

    static {
        SIGNERS.put("DefaultSignerType", DefaultSigner.class);
    }
}
