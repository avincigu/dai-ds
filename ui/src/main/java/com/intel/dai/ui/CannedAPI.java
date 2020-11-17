// Copyright (C) 2018 Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package com.intel.dai.ui;

import com.intel.config_io.ConfigIO;
import com.intel.config_io.ConfigIOFactory;
import com.intel.dai.dsimpl.jdbc.DbConnectionFactory;
import com.intel.dai.exceptions.DataStoreException;
import com.intel.dai.exceptions.ProviderException;
import com.intel.logging.Logger;
import com.intel.properties.PropertyArray;
import com.intel.properties.PropertyMap;
import com.intel.properties.PropertyNotExpectedType;

import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class CannedAPI {
    private Connection conn = null;
    JsonConverter jsonConverter = new JsonConverter();
    private static ConfigIO jsonParser = ConfigIOFactory.getInstance("json");
    private final Logger log_;

    private static final Map<String, String> owner_map = Collections.unmodifiableMap(
            new HashMap<String,String>() {{
                put("W", "WLM");
                put("S", "Service");
                put("G", "General");
                put("F", "Free Pool");
            }});

    private static final Map<String, String> state_map = Collections.unmodifiableMap(
            new HashMap<String,String>() {{
                put("B", "Bios Starting");
                put("D", "Discovered (dhcp discover)");
                put("I", "IP address assigned (dhcp request)");
                put("L", "Starting load of Boot images");
                put("K", "Kernel boot started");
                put("A", "Active");
                put("M", "Missing");
                put("E", "Error");
                put("U", "Unknown");
                put("H", "Halting/Shutting Down");
            }});

    private static final Map<String, String> wlmstate_map = Collections.unmodifiableMap(
            new HashMap<String,String>() {{
                put("A", "Available");
                put("U", "Unavailable");
                put("G", "General");
                put("F", "Free Pool");
            }});

    private static final Map<String, String> jobstate_map = Collections.unmodifiableMap(
            new HashMap<String,String>() {{
                put("T", "Terminated");
                put("S", "Started");
            }});

    CannedAPI(Logger logger) {
        assert jsonParser != null: "Failed to get a JSON parser!";
        assert logger != null: "Passed a null logger to the ctor!";
        log_ = logger;
    }

    public Connection get_connection() throws DataStoreException {
        return DbConnectionFactory.createDefaultConnection();
    }

    public synchronized String getData(String requestKey, Map<String, String> params_map)
            throws SQLException, DataStoreException, ProviderException {
        assert params_map != null : "Input parameters should be provided";
        log_.info("Establishing DB connection");
        if(conn == null)
            conn = get_connection();
        try {
            Timestamp endtime = getTimestamp(getStartEndTime(params_map, "EndTime"));
            Timestamp starttime = getTimestamp(getStartEndTime(params_map, "StartTime"));
            String limit = params_map.getOrDefault("Limit", null);

            PropertyMap jsonResult;
            switch (requestKey) {
                case "getraswithfilters": {
                    String lctn = params_map.getOrDefault("Lctn", null);
                    String event_type = params_map.getOrDefault("EventType", "%");
                    String severity = params_map.getOrDefault("Severity", "%");
                    String jobIdValue = params_map.getOrDefault("JobId", null);
                    String exclude = params_map.getOrDefault("Exclude", "%");
                    jsonResult = executeProcedureFiveVariableFilter("{call GetRasEventsWithFilters(?, ?, ?, ?, ?, ?, ?, ?)}", starttime, endtime, lctn, event_type, severity, jobIdValue, limit, exclude);
                    break;
                }
                case "getenvwithfilters": {
                    String lctn = params_map.getOrDefault("Lctn", "%");
                    jsonResult = executeProcedureOneVariableFilter("{call GetAggregatedEvnDataWithFilters(?, ?, ?, ?)}", starttime, endtime, lctn, limit);
                    break;
                }
                case "getinvspecificlctn": {
                    String lctn = params_map.getOrDefault("Lctn", "%");
                    jsonResult = executeProcedureOneVariableFilter("{call GetInventoryDataForLctn(?, ?, ?, ?)}", starttime, endtime, lctn, limit);
                    jsonResult = map_state_values(jsonResult);
                    break;
                }
                case "getjobinfo": {
                    String username = params_map.getOrDefault("Username", "%");
                    String jobid = params_map.getOrDefault("Jobid", "%");
                    String state = params_map.getOrDefault("State", "%");
                    String locations = params_map.getOrDefault("Lctn", "%");
                    Timestamp atTime = getTimestamp(getStartEndTime(params_map, "AtTime"));
                    log_.info("GetJobInfo procedure called with Jobid = %s and Username = %s", jobid, username);
                    jsonResult = executeProcedureAtTimeFourVariableFilter("{call GetJobInfo(?, ?, ?, ?, ?, ?, ?, ?)}", starttime, endtime, atTime, jobid, username, state, locations, limit);
                    jsonResult = map_job_values(jsonResult);
                    break;
                }
                case "getreservationinfo": {
                    String username = params_map.getOrDefault("Username", null);
                    String reservation = params_map.getOrDefault("Name", null);
                    log_.info("GetReservationInfo procedure called with Reservation Name = %s and Username = %s", reservation, username);
                    jsonResult = executeProcedureTwoVariableFilter("{call GetReservationInfo(?, ?, ?, ?, ?)}", starttime, endtime, reservation, username, limit);
                    break;
                }
                case "system_summary": {
                    jsonResult = new PropertyMap();
                    log_.info("GetComputeNodeSummary procedure called");
                    jsonResult.put("compute", map_state_values(executeProcedure("{call GetComputeNodeSummary()}")));

                    log_.info("GetServiceNodeSummary procedure called");
                    jsonResult.put("service", map_state_values(executeProcedure("{call GetServiceNodeSummary()}")));
                    break;
                }
                case "getfrumigrationhistory": {
                    String lctn = params_map.getOrDefault("Lctn", "%");
                    jsonResult = executeProcedureOneVariableFilter("{call MigrationHistoryOfFru(?, ?, ?, ?)}", starttime, endtime, lctn, limit);
                    break;
                }
                case "getinvchanges": {
                    String lctn = params_map.getOrDefault("Lctn", null);
                    jsonResult = executeProcedureOneVariableFilter("{call GetInventoryChange(?, ?, ?, ?)}", starttime, endtime, lctn, limit);
                    break;
                }
                case "getinvhislctn": {
                    String lctn = params_map.getOrDefault("Lctn", "%");
                    jsonResult = executeProcedureOneVariableFilter("{call GetInventoryHistoryForLctn(?, ?, ?, ?)}", starttime, endtime, lctn, limit);
                    jsonResult = map_state_values(jsonResult);
                    break;
                }
                case "getnodeinvinfo": {
                    String lctn = params_map.getOrDefault("Lctn", "%");
                    jsonResult = executeProcedureOneVariableFilter("{call GetInventoryInfoForLctn(?, ?)}", lctn, limit);
                    break;
                }
                default:
                    throw new ProviderException("Invalid request, request key: '" + requestKey + "' : Not Found");
            }
            return jsonParser.toString(jsonResult);
        } finally {
            conn.close();
        }
    }

    private PropertyMap map_state_values(PropertyMap jsonResult)
    {
        try {
            Integer owner_pos = null;
            Integer state_pos = null;
            Integer wlmnodestate_pos = null;

            PropertyArray schema = jsonResult.getArray("schema");

            if(schema != null){
                for(int i = 0; i < schema.size(); i++){
                    PropertyMap m = schema.getMap(i);
                    if(m.getString("data").equals("owner"))
                        owner_pos = Integer.valueOf(i);
                    else if (m.getString("data").equals("state"))
                        state_pos = Integer.valueOf(i);
                    else if (m.getString("data").equals("wlmnodestate"))
                        wlmnodestate_pos = Integer.valueOf(i);
                }

                PropertyArray data = jsonResult.getArray("data");

                for (int i = 0; i < data.size(); i++){
                    PropertyArray items = data.getArray(i);
                    if (owner_pos != null)
                        items.set(owner_pos.intValue(), owner_map.get(items.getString(owner_pos.intValue())));
                    if (state_pos != null)
                        items.set(state_pos.intValue(), state_map.get(items.getString(state_pos.intValue())));
                    if (wlmnodestate_pos != null)
                        items.set(wlmnodestate_pos.intValue(), wlmstate_map.get(items.getString(wlmnodestate_pos.intValue())));
                }

                jsonResult.put("data", data);
            }

        }
        catch(PropertyNotExpectedType e){
            return jsonResult;
        }

        return jsonResult;
    }

    private PropertyMap map_job_values(PropertyMap jsonResult)
    {
        try {
            Integer state_pos = null;

            PropertyArray schema = jsonResult.getArray("schema");

            if(schema != null){
                for(int i = 0; i < schema.size(); i++){
                    PropertyMap m = schema.getMap(i);
                    if (m.getString("data").equals("state"))
                        state_pos = i;
                }

                PropertyArray data = jsonResult.getArray("data");

                for (int i = 0; i < data.size(); i++){
                    PropertyArray items = data.getArray(i);
                    if (state_pos != null)
                        items.set(state_pos, jobstate_map.get(items.getString(state_pos)));
                }

                jsonResult.put("data", data);
            }

        }
        catch(PropertyNotExpectedType e) {
            return jsonResult;
        }

        return jsonResult;
    }

    private String getStartEndTime(Map<String, String> params_map, String key)
    {
        String val_time;
        val_time = params_map.getOrDefault(key, "null");
        return val_time;
    }

    private Timestamp getTimestamp(String Time)
    {
        Timestamp new_time;
        if(Time == null || Time.equalsIgnoreCase("null"))
            return null;
        else
            new_time = Timestamp.valueOf(Time);
        return new_time;
    }

    private PropertyMap executeProcedure(String prepProcedure)
            throws SQLException
    {
        try (CallableStatement stmt = conn.prepareCall(prepProcedure)) {

            try (ResultSet rs = stmt.executeQuery()) {
                return jsonConverter.convertToJsonResultSet(rs);
            }
        }
    }

    private PropertyMap executeProcedureOneVariableFilter(String prepProcedure, Timestamp StartTime,
                                                          Timestamp EndTime, String FilterVariableOne, String Limit)
            throws SQLException
    {
        try (CallableStatement stmt = conn.prepareCall(prepProcedure)) {
            stmt.setTimestamp(1, StartTime);
            stmt.setTimestamp(2, EndTime);
            stmt.setString(3, FilterVariableOne);
            handleLimit(Limit, stmt, 4);

            try (ResultSet rs = stmt.executeQuery()) {
                return jsonConverter.convertToJsonResultSet(rs);
            }
        }
    }

    private PropertyMap executeProcedureOneVariableFilter(String prepProcedure, String FilterVariableOne, String Limit)
            throws SQLException
    {
        try (CallableStatement stmt = conn.prepareCall(prepProcedure)) {
            stmt.setString(1, FilterVariableOne);
            handleLimit(Limit, stmt, 2);

            try (ResultSet rs = stmt.executeQuery()) {
                return jsonConverter.convertToJsonResultSet(rs);
            }
        }
    }

    private PropertyMap executeProcedureTwoVariableFilter(String prepProcedure, Timestamp StartTime,
                                                          Timestamp EndTime, String FilterVariableOne,
                                                          String FilterVariableTwo ,String Limit)
            throws SQLException
    {
        try (CallableStatement stmt = conn.prepareCall(prepProcedure)) {
            stmt.setTimestamp(1, StartTime);
            stmt.setTimestamp(2, EndTime);
            stmt.setString(3, FilterVariableOne);
            stmt.setString(4, FilterVariableTwo);
            handleLimit(Limit, stmt, 5);

            try (ResultSet rs = stmt.executeQuery()) {
                return jsonConverter.convertToJsonResultSet(rs);
            }
        }
    }
    
    private PropertyMap executeProcedureAtTimeFourVariableFilter(String prepProcedure, Timestamp StartTime,
                                                            Timestamp EndTime, Timestamp AtTime, String FilterVariableOne,
                                                            String FilterVariableTwo, String FilterVariableThree, String FilterVariableFour, String Limit)
            throws SQLException
    {
        try (CallableStatement stmt = conn.prepareCall(prepProcedure)) {
            stmt.setTimestamp(1, StartTime);
            stmt.setTimestamp(2, EndTime);
            stmt.setTimestamp(3, AtTime);
            stmt.setString(4, FilterVariableOne);
            stmt.setString(5, FilterVariableTwo);
            stmt.setString(6, FilterVariableThree);
            stmt.setString(7, FilterVariableFour);
            handleLimit(Limit, stmt, 8);

            try (ResultSet rs = stmt.executeQuery()) {
                return jsonConverter.convertToJsonResultSet(rs);
            }
        }
    }

    private PropertyMap executeProcedureFiveVariableFilter(String prepProcedure, Timestamp StartTime,
                                                           Timestamp EndTime, String FilterVariableOne, String FilterVariableTwo, String FilterVariableThree, String  FilterVariableFour, String Limit, String FilterVariableFive)
            throws SQLException
    {
        try (CallableStatement stmt = conn.prepareCall(prepProcedure)) {
            stmt.setTimestamp(1, StartTime);
            stmt.setTimestamp(2, EndTime);
            stmt.setString(3, FilterVariableOne);
            stmt.setString(4, FilterVariableTwo);
            stmt.setString(5, FilterVariableThree);
            stmt.setString(6, FilterVariableFour);
            handleLimit(Limit, stmt, 7);
            stmt.setString(8, FilterVariableFive);

            try (ResultSet rs = stmt.executeQuery()) {
                return jsonConverter.convertToJsonResultSet(rs);
            }
        }
    }

    private void handleLimit(String Limit, CallableStatement stmt, int value) throws SQLException {
        if(Limit != null)
            stmt.setInt(value, Integer.parseInt(Limit));
        else
            stmt.setNull(value, Types.INTEGER);
    }
}