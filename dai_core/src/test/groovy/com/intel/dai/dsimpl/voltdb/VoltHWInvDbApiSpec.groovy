package com.intel.dai.dsimpl.voltdb

import com.intel.dai.dsapi.HWInvHistory
import com.intel.dai.exceptions.DataStoreException
import com.intel.logging.Logger
import com.intel.logging.LoggerFactory
import org.voltdb.client.Client
import spock.lang.Specification
import java.nio.file.Paths

class VoltHWInvDbApiSpec extends Specification {
    VoltHWInvDbApi api
    Logger logger = LoggerFactory.getInstance("Test", "VoltHWInvDbApiSpec", "console")

    def setup() {
        VoltDbClient.voltClient = Mock(Client)
        String[] servers = ["localhost"]
        api = new VoltHWInvDbApi(logger, new HWInvUtilImpl(Mock(Logger)), servers)
    }

    def "initialize"() {
        when: api.initialize()
        then: notThrown Exception
    }
    def "ingest from String failed"() {
        String[] servers = ["localhost"]
        def util = Mock(HWInvUtilImpl)
        api = new VoltHWInvDbApi(logger, util, servers)
        util.toCanonicalPOJO(_) >> null
        expect: api.ingest(null as String) == 0
    }
    def "ingest from empty HWInvHistory"() {
        HWInvHistory hist = new HWInvHistory();
        expect: api.ingest(hist) == []
    }
    def "ingestHistory from String failed"() {
        String[] servers = ["localhost"]
        def util = Mock(HWInvUtilImpl)
        api = new VoltHWInvDbApi(logger, util, servers)
        util.toCanonicalHistoryPOJO(_) >> null
        expect: api.ingestHistory(null as String) == []
    }
    // Ingesting nonexistent file now results in a no-op
    def "ingest -- nonexistent file"() {
        when: api.ingest Paths.get("noSuchFile")
        then: notThrown IOException
    }
    def "ingest -- empty json"() {
        when: api.ingest Paths.get("src/test/resources/data/empty.json")
        then: thrown DataStoreException
    }
    def "ingest -- string"() {
        when: api.ingest ""
        then: notThrown DataStoreException
    }
    def "delete - null client"() {
        when: api.delete "x0"
        then: thrown DataStoreException
    }

    def "allLocationsAt"() {
        when: api.allLocationsAt(null, null)
        then: thrown DataStoreException
    }

    def "numberOfLocationsInHWInv"() {
        when: api.numberOfRawInventoryRows()
        then: thrown DataStoreException
    }

    def "insertHistoricalRecord"() {
        api.client = Mock(Client)
        when: api.insertRawHistoricalRecord null
        then: thrown DataStoreException
    }

    def "lastHwInvHistoryUpdate"() {
        api.client = Mock(Client)
        when: api.lastHwInvHistoryUpdate()
        then: thrown DataStoreException
    }
}
