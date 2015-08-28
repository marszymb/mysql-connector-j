/*
  Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.

  The MySQL Connector/J is licensed under the terms of the GPLv2
  <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most MySQL Connectors.
  There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
  this software, see the FOSS License Exception
  <http://www.mysql.com/about/legal/licensing/foss-exception.html>.

  This program is free software; you can redistribute it and/or modify it under the terms
  of the GNU General Public License as published by the Free Software Foundation; version 2
  of the License.

  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with this
  program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
  Floor, Boston, MA 02110-1301  USA

 */

package testsuite.mysqlx.devapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.mysql.cj.api.x.Expression.expr;
import com.mysql.cj.api.x.FetchedRows;
import com.mysql.cj.api.x.Table;
import com.mysql.cj.api.x.Row;

/**
 * @todo
 */
public class TableUpdateTest extends TableTest {
    @Before
    @Override
    public void setupTableTest() {
        super.setupTableTest();
    }

    @After
    @Override
    public void teardownTableTest() {
        super.teardownTableTest();
    }

    @Test
    public void testUpdates() {
        sqlUpdate("drop table if exists updates");
        sqlUpdate("create table updates (_id varchar(32), name varchar(20), birthday date, age int)");
        sqlUpdate("insert into updates values ('1', 'Sakila', '2000-05-27', 14)");
        sqlUpdate("insert into updates values ('2', 'Shakila', '2001-06-26', 13)");

        Table table = this.schema.getTable("updates");
        table.update().set("name", expr("concat(name, '-updated')"), "age", expr("age + 1")).where("name == 'Sakila'").execute();
        FetchedRows rows = table.select("name, age").where("_id == 1").execute();
        Row r = rows.next();
        assertEquals("Sakila-updated", r.getString(0));
        assertEquals(15, r.getInt(1));
        assertFalse(rows.hasNext());
    }

    // TODO: there could be more tests, but I expect this API and implementation to change to better accommodate some "normal" use cases
}