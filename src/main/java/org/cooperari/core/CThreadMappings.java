//
//   Copyright 2014-2019 Eduardo R. B. Marques
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

package org.cooperari.core;

import java.util.IdentityHashMap;

import org.cooperari.errors.CInternalError;

/**
 * Provides mapping from {@link Thread} objects to {@link CThread objects}.
 * 
 * <p>
 * The execution of each (explicitly) started {@link Thread} instances is accomplished by a {@link CThread} instance.
 * This class records such associations to support thread yield points.
 * </p>
 * 
 * @since 0.2
 *
 */
public final class CThreadMappings {

  /**
   * {@link Thread}-to-{@link CThread} map.
   */
  private final IdentityHashMap<Thread, CThread> _map = new IdentityHashMap<>();
  
  /**
   * Gets the {@link CThread} instance that renders the execution of a given {@link Thread},
   * or the given thread instance if it is already a cooperative thread.
   * @param t Thread.
   * @return {@link CThread} instance.
   * @throws CInternalError If no  <code>t</code> has no association to a cooperative thread.
   */
  public CThread getCThread(Thread t) throws CInternalError {
    CThread ct; 
    if (t instanceof CThread) {
      ct = (CThread) t; 
    } else {
      ct = _map.get(t);
    }
    if (ct == null) {
      throw new CInternalError(
          String.format("No cooperative thread associated to thread '%s' of type '%s'.",
                        t.getName(), t.getClass().getCanonicalName()));
    }
    return ct;
  }
  
  /**
   * Create a {@link Thread}-to-{@link CThread} association.
   * @param t Thread.
   * @param ct Cooperative thread.
   * @throws CInternalError If  <code>t</code> is a cooperative thread or is already associated to to a cooperative thread.
   */
  public void associate(Thread t, CThread ct) {
    if (t instanceof CThread || _map.containsKey(t)) {
      throw new CInternalError(
          String.format("Invalid thread association for thread '%s' of type '%s'.",
                        t.getName(), t.getClass().getCanonicalName()));
    }
    _map.put(t,  ct);
  }
 
}
