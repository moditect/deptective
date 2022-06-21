/**
 *  Copyright 2019-2022 The ModiTect authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.moditect.deptective.plugintest.visualize.foo;

import java.math.BigDecimal;

import org.moditect.deptective.plugintest.visualize.bar.Bar;
import org.moditect.deptective.plugintest.visualize.qux.Qux;

public class Foo {

    private String s;
    private BigDecimal bd;
    private final Bar bar = new Bar();
    private final Qux qux = new Qux();
}
