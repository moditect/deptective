/**
 *  Copyright 2019 The ModiTect authors
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
package org.moditect.deptective.plugintest.analyzewithcomponent.foo;

import org.moditect.deptective.plugintest.analyzewithcomponent.bar.Bar;
import org.moditect.deptective.plugintest.analyzewithcomponent.bar.barsub1.BarSub1;
import org.moditect.deptective.plugintest.analyzewithcomponent.bar.barsub2.BarSub2;
import org.moditect.deptective.plugintest.analyzewithcomponent.qux.Qux;
import org.moditect.deptective.plugintest.analyzewithcomponent.qux.quxsub1.QuxSub1;

public class Foo {

    private final Bar bar = new Bar();
    private final BarSub1 barSub1 = new BarSub1();
    private final BarSub2 barSub2 = new BarSub2();
    private final Qux qux = new Qux();
    private final QuxSub1 quxSub1 = new QuxSub1();
}
