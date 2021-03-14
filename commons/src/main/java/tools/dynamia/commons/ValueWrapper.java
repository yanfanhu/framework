/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
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
package tools.dynamia.commons;


/**
 * The Class ValueWrapper.
 */
public class ValueWrapper {

    /**
     * The value.
     */
    private Object value;

    /**
     * The value class.
     */
    private Class<?> valueClass;

    /**
     * Instantiates a new value wrapper.
     *
     * @param value the value
     * @param valueClass the value class
     */
    public ValueWrapper(Object value, Class<?> valueClass) {
        this.value = value;
        this.valueClass = valueClass;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Gets the value class.
     *
     * @return the value class
     */
    public Class<?> getValueClass() {
        return valueClass;
    }

}
