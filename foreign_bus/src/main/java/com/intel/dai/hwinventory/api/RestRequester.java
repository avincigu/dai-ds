package com.intel.dai.hwinventory.api;

import com.intel.logging.Logger;
import com.intel.networking.restclient.RESTClient;

public interface RestRequester {
    void initialize(Logger logger, Requester config, RESTClient restClient);
    int initiateDiscovery(String xname);
    int getDiscoveryStatus();
    int getHwInventory(String outputFile);
    int getHwInventory(String xname, String outputFile);
}
