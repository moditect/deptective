/*
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
package org.moditect.deptective.plugintest.basic.foo;

import java.util.ArrayList;
import java.util.List;

import org.moditect.deptective.plugintest.basic.baranparam.BarAnnotationParameter;
import org.moditect.deptective.plugintest.basic.barclass.BarClass;
import org.moditect.deptective.plugintest.basic.barclazzan.BarClazzAnnotation;
import org.moditect.deptective.plugintest.basic.barclsliteral.BarClassLiteral;
import org.moditect.deptective.plugintest.basic.barctorcall.BarCtorCall;
import org.moditect.deptective.plugintest.basic.barctorparam.BarCtorParam;
import org.moditect.deptective.plugintest.basic.bardemand.BarDemand;
import org.moditect.deptective.plugintest.basic.barfield.BarField;
import org.moditect.deptective.plugintest.basic.barfieldan.BarFieldAnnotation;
import org.moditect.deptective.plugintest.basic.bargen.BarGeneric;
import org.moditect.deptective.plugintest.basic.bargenericbound.BarGenericBound;
import org.moditect.deptective.plugintest.basic.bargentype.BarGenType;
import org.moditect.deptective.plugintest.basic.barinnerinner.BarInnerInterface;
import org.moditect.deptective.plugintest.basic.barinnersuper.BarInnerSuperClass;
import org.moditect.deptective.plugintest.basic.barinter.BarInterface;
import org.moditect.deptective.plugintest.basic.barlocalvar.BarLocalVar;
import org.moditect.deptective.plugintest.basic.barloopvar.BarLoopVar;
import org.moditect.deptective.plugintest.basic.barparameter.BarParameter;
import org.moditect.deptective.plugintest.basic.barretval.BarRetVal;
import org.moditect.deptective.plugintest.basic.barretvalgen.RetValGen;
import org.moditect.deptective.plugintest.basic.barsuper.BarSuper;
import org.moditect.deptective.plugintest.basic.bartypearg.BarTypeArg;
import org.moditect.deptective.plugintest.basic.barvalueann.BarValue;

@FooAnnotation(classParameter = BarAnnotationParameter.class)
@BarClazzAnnotation
@FooValueAnnotation(BarValue.class)
public class Foo extends BarSuper implements BarInterface, /* allowed: */ IFoo {

    @BarFieldAnnotation
    private String s;
    private final BarField bar = new BarField();
    private Class<?> clazz = BarClass.class;

    private org.moditect.deptective.plugintest.basic.barqualified.BarQualified barQualified;

    private String x = org.moditect.deptective.plugintest.basic.barstatic.BarStatic.staticBar();

    private final String barbarbar = org.moditect.deptective.plugintest.basic.barstaticfield.BarStaticField.BAR_STATIC;

    private BarDemand barDemand;

    private Class<? extends BarGenericBound> genericBound;

    public Foo(BarCtorParam bar) {
    }

    public BarRetVal doSomething(BarParameter bar) {
        BarLocalVar varLocalVar = new BarLocalVar();

        List<BarTypeArg> bars = new ArrayList<>();

        for (BarLoopVar oneBar : new ArrayList<BarLoopVar>()) {
        }

        new BarCtorCall();

        doIt(BarClassLiteral.class);

        return null;
    }

    private void doIt(Class<?> clz) {

    }

    private boolean isAllowed() {
        return true;
    }

    private void isAlsoAllowed() {
    }

    private List<RetValGen> isNotAllowed() {
        return null;
    }

    static class InvalidFooGeneric<T extends BarGeneric> {
    }

    static class InvalidFooImplementation extends FooContainer<BarGenType> {
    }

    static interface InnerFoo extends BarInnerInterface {
    }

    static class InnerFooClass extends BarInnerSuperClass {
    }
}
