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
package com.example.ui;

import com.example.persistence.SomeDao;
import com.example.rest.SomeResource;

public class SomeViewController {

    private final SomeDao dao = new SomeDao();

    // Uh oh, referencing the "rest" package from "ui" is not intended
    private final SomeResource resource = SomeResource.getInstance();

    public String getView() {
        return null;
    }
}
