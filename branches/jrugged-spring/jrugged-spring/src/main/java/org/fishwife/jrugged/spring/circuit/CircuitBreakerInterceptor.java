/* ExceptionCircuitInterceptor.java
 *
 * Copyright 2009 Comcast Interactive Media, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fishwife.jrugged.spring.circuit;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.fishwife.jrugged.DefaultFailureInterpreter;
import org.fishwife.jrugged.CircuitBreaker;
import org.fishwife.jrugged.CircuitBreakerException;
import org.fishwife.jrugged.FailureInterpreter;

/** A Spring interceptor that allows wrapping a method invocation with
 *  a {@link CircuitBreaker}.
 */
public class CircuitBreakerInterceptor implements MethodInterceptor {

	private Map<String, CircuitBreaker> methodMap =
		new HashMap<String, CircuitBreaker>();

	/** See if the given method invocation is one that needs to be
	 * called through a {@link CircuitBreaker}, and if so, do so.
	 * @param invocation the {@link MethodInvocation} in question
	 * @return whatever the underlying method call would normally
	 * return
	 * @throws any Throwables that the method call would generate, or
	 *   that the {@link CircuitBreaker} would generate when tripped.
	 */
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        CircuitBreaker circuit  = methodMap.get(invocation.getMethod().getName());
		if (circuit == null) {
			return invocation.proceed();
		}

		return circuit.invoke(new Callable<Object>() {
                public Object call() throws Exception {
                    try {
                        return invocation.proceed();
                    } catch (Throwable e) {
                        if (e instanceof Exception)
                            throw (Exception) e;
                        else if (e instanceof Error)
                            throw (Error) e;
                        else
                            throw new RuntimeException(e);
                    }
                }
            });
    }

	/** Specifies which methods will be wrapped with which CircuitBreakers.
	 *  @param methodMap the mapping!
	 */
    public void setMethods(Map<String, CircuitBreaker> methodMap)  {
        this.methodMap = methodMap;
    }
}
