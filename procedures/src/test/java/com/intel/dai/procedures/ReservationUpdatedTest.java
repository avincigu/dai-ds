// Copyright (C) 2018 Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

package com.intel.dai.procedures;

import org.junit.Test;
import org.voltdb.Expectation;
import org.voltdb.SQLStmt;
import org.voltdb.VoltTable;
import org.voltdb.VoltType;
import org.voltdb.types.TimestampType;

import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.*;

public class ReservationUpdatedTest {
    class MockReservationUpdated extends ReservationUpdated {
        @Override
        public void voltQueueSQL(final SQLStmt stmt, Expectation expectation, Object... args) { }

        @Override
        public void voltQueueSQL(final SQLStmt stmt, Object... args) { }

        @Override
        public VoltTable[] voltExecuteSQL(boolean value) {
            return null;
        }

        @Override
        public Date getTransactionTime() {
            return Date.from(Instant.now());
        }
    }

    @Test
    public void run() {
        MockReservationUpdated proc = new MockReservationUpdated();
        proc.run("Name", "Users", "Nodes", 0L, 10L,
                20L, "RAS", 9999L);
    }
}
