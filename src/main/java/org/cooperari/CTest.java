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

package org.cooperari;

import java.lang.reflect.AnnotatedElement;

import org.cooperari.config.CBaseConfiguration;

/**
 * Cooperative test.
 * 
 * @since 0.2
 */
public interface CTest extends Runnable {
  
  /**
   * Get test name.
   * @return A string identifying the test.
   */
  String getName();
  
  /**
   * Get name of the test suite to which this test belongs. 
   * @return A string identifying the suite.
   */
  String getSuiteName();
  
  /**
   * Get configuration.
   * @return An annotation provider for the test configuration.
   * The default implementation returns the class handle for {@link CBaseConfiguration}.
   */
  default AnnotatedElement getConfiguration() {
    return CBaseConfiguration.class;
  }
  
  /**
   * Execution method.
   */
  @Override
  void run();
  
  /**
   * Callback method invoked in case test trial completed normally (without exceptions, e.g., related
   * to test assertions).
   * The default implementation does nothing.
   * @throws Throwable In case an error needs to be reported by the handler.
   */
  default void onNormalCompletion() throws Throwable {
    
  }
    
  /**
   * Callback method invoked in case test trial completes with an exception.
   * The default implementation returns <code>false</code>.
   * 
   * @param e Exception.
   * @return <code>true</code> if exception is expected and/or should be ignored
   */
  default boolean ignoreException(Throwable e) {
    return false;
  }

}
