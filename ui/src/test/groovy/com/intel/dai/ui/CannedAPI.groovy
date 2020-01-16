package com.intel.dai.ui

import spock.lang.Specification
import com.intel.config_io.ConfigIO;
import com.intel.config_io.ConfigIOFactory;
import com.intel.properties.*;
import com.intel.logging.LoggerFactory;
import com.intel.logging.Logger;
import com.intel.dai.dsimpl.jdbc.DbConnectionFactory;
import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.ResultSet;


class CannedAPISpec extends Specification {

    def underTest_

    def setup() {

        def jsonParser = Mock(ConfigIO)
        ConfigIOFactory.getInstance(_) >> jsonParser

        underTest_ = new CannedAPI()
    }

    def "Test map_state_values owner"() {

        def ownermap = new HashMap<String, String>()
        ownermap.put("data","owner")
        def ownerpmap = new PropertyMap(ownermap)

        def schemaarray = new ArrayList()
        schemaarray.add(ownerpmap)
        def schema = new PropertyArray(schemaarray)

        def ownermapdata = new ArrayList()
        ownermapdata.add("W")
        def ownerpmapdata = new PropertyArray(ownermapdata)

        def dataarray = new ArrayList()
        dataarray.add(ownerpmapdata)
        def datap = new PropertyArray(dataarray)

        def jsonResult = new HashMap<String, Object>()
        jsonResult.put("schema", schema)
        jsonResult.put("data", datap)
        def jsonResultMap = new PropertyMap(jsonResult)

        def log = Mock(Logger)
        LoggerFactory.getInstance(_, _, _) >> log

        expect:
        underTest_.map_state_values(jsonResultMap)
    }

    def "Test map_state_values state"() {

        def statemap = new HashMap<String, String>()
        statemap.put("data","state")
        def statepmap = new PropertyMap(statemap)

        def schemaarray = new ArrayList()
        schemaarray.add(statepmap)
        def schema = new PropertyArray(schemaarray)

        def statemapdata = new ArrayList()
        statemapdata.add("B")
        def statepmapdata = new PropertyArray(statemapdata)

        def dataarray = new ArrayList()
        dataarray.add(statepmapdata)
        def datap = new PropertyArray(dataarray)

        def jsonResult = new HashMap<String, Object>()
        jsonResult.put("schema", schema)
        jsonResult.put("data", datap)
        def jsonResultMap = new PropertyMap(jsonResult)

        def log = Mock(Logger)
        LoggerFactory.getInstance(_, _, _) >> log

        expect:
        underTest_.map_state_values(jsonResultMap)
    }

    def "Test map_state_values wlm"() {

        def wlmnodestatemap = new HashMap<String, String>()
        wlmnodestatemap.put("data","wlmnodestate")
        def wlmnodestatepmap = new PropertyMap(wlmnodestatemap)

        def schemaarray = new ArrayList()
        schemaarray.add(wlmnodestatepmap)
        def schema = new PropertyArray(schemaarray)

        def wlmnodestatemapdata = new ArrayList()
        wlmnodestatemapdata.add("A")
        def wlmnodestatepmapdata = new PropertyArray(wlmnodestatemapdata)

        def dataarray = new ArrayList()
        dataarray.add(wlmnodestatepmapdata)
        def datap = new PropertyArray(dataarray)

        def jsonResult = new HashMap<String, Object>()
        jsonResult.put("schema", schema)
        jsonResult.put("data", datap)
        def jsonResultMap = new PropertyMap(jsonResult)

        def log = Mock(Logger)
        LoggerFactory.getInstance(_, _, _) >> log

        expect:
        underTest_.map_state_values(jsonResultMap)
    }

    def "Test map_job_values state"() {

        def statemap = new HashMap<String, String>()
        statemap.put("data","state")
        def statepmap = new PropertyMap(statemap)

        def schemaarray = new ArrayList()
        schemaarray.add(statepmap)
        def schema = new PropertyArray(schemaarray)

        def statemapdata = new ArrayList()
        statemapdata.add("T")
        def statepmapdata = new PropertyArray(statemapdata)

        def dataarray = new ArrayList()
        dataarray.add(statepmapdata)
        def datap = new PropertyArray(dataarray)

        def jsonResult = new HashMap<String, Object>()
        jsonResult.put("schema", schema)
        jsonResult.put("data", datap)
        def jsonResultMap = new PropertyMap(jsonResult)

        def log = Mock(Logger)
        LoggerFactory.getInstance(_, _, _) >> log

        expect:
        underTest_.map_job_values(jsonResultMap)
    }

    def "Test map_job_values nodes"() {

        def nodemap = new HashMap<String, String>()
        nodemap.put("data","nodes")
        def nodepmap = new PropertyMap(nodemap)

        def schemaarray = new ArrayList()
        schemaarray.add(nodepmap)
        def schema = new PropertyArray(schemaarray)

        def nodemapdata = new ArrayList()
        nodemapdata.add("00000000000000000000000040922409")
        def nodepmapdata = new PropertyArray(nodemapdata)

        def dataarray = new ArrayList()
        dataarray.add(nodepmapdata)
        def datap = new PropertyArray(dataarray)

        def jsonResult = new HashMap<String, Object>()
        jsonResult.put("schema", schema)
        jsonResult.put("data", datap)
        def jsonResultMap = new PropertyMap(jsonResult)

        def log = Mock(Logger)
        LoggerFactory.getInstance(_, _, _) >> log

        expect:
        underTest_.map_job_values(jsonResultMap)
    }

    def "Test hexStringToByteArray"() {
        expect:
        underTest_.hexStringToByteArray("00000000000000000000000040922409")
    }

}
