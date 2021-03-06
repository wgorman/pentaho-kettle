/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package org.pentaho.di.core.database.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseTransactionListener;

/**
 * This class contains a map between on the one hand
 * 
 *      the transformation name/thread 
 *      the partition ID
 *      the connection group
 *      
 * And on the other hand 
 *      
 *      The database connection
 *      The number of times it was opened
 *      
 * @author Matt
 *
 */
public class DatabaseConnectionMap
{
    private Map<String,Database> map;
    private AtomicInteger transactionId; 
    private Map<String, List<DatabaseTransactionListener>> transactionListenersMap;
    
    private static DatabaseConnectionMap connectionMap;
    
    public synchronized static final DatabaseConnectionMap getInstance()
    {
        if (connectionMap!=null) return connectionMap;
        connectionMap = new DatabaseConnectionMap();
        return connectionMap;
    }
    
    private DatabaseConnectionMap()
    {
        map = new Hashtable<String,Database>();
        transactionId = new AtomicInteger(0);
        transactionListenersMap = new HashMap<String, List<DatabaseTransactionListener>>();
    }
    
    public synchronized void storeDatabase(String connectionGroup, String partitionID, Database database)
    {
        String key = createEntryKey(connectionGroup, partitionID, database);
        map.put(key, database);
    }
    
    public synchronized void removeConnection(String connectionGroup, String partitionID, Database database)
    {
        String key = createEntryKey(connectionGroup, partitionID, database);
        map.remove(key);
    }
        
    
    public synchronized Database getDatabase(String connectionGroup, String partitionID, Database database)
    {
        String key = createEntryKey(connectionGroup, partitionID, database);
        return map.get(key);
    }
    
    public static final String createEntryKey(String connectionGroup, String partitionID, Database database)
    {
        StringBuffer key = new StringBuffer(connectionGroup);
        
        key.append(':').append(database.getDatabaseMeta().getName());
        if (!Const.isEmpty(partitionID))
        {
            key.append(':').append(partitionID);
        }

        return key.toString();
    }
    
    public Map<String, Database> getMap() {
		return map;
	}
    
  public String getNextTransactionId() {
    return Integer.toString( transactionId.incrementAndGet() );
  }

  public void addTransactionListener(String transactionId, DatabaseTransactionListener listener) {
    List<DatabaseTransactionListener> transactionListeners = getTransactionListeners(transactionId);
    transactionListeners.add(listener);
  }
  
  public void removeTransactionListener(String transactionId, DatabaseTransactionListener listener) {
    List<DatabaseTransactionListener> transactionListeners = getTransactionListeners(transactionId);
    transactionListeners.remove(listener);
  }
  
  public List<DatabaseTransactionListener> getTransactionListeners(String transactionId) {
    List<DatabaseTransactionListener> transactionListeners = transactionListenersMap.get(transactionId);
    if (transactionListeners==null) {
      transactionListeners = new ArrayList<DatabaseTransactionListener>();
      transactionListenersMap.put(transactionId, transactionListeners);
    }
    return transactionListeners;
  }

  public void removeTransactionListeners(String transactionId) {
    transactionListenersMap.remove(transactionId);
  }
}
