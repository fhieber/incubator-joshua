/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.joshua.decoder.cky;

import static org.apache.joshua.decoder.cky.TestUtil.translate;
import static org.testng.Assert.assertEquals;

import org.apache.joshua.decoder.Decoder;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;

public class DenormalizationTest {

  private static final String INPUT = "¿ who you lookin' at , mr. ?";
  private static final String GOLD = "¿Who you lookin' at, Mr.?";

  private Decoder decoder = null;

  @BeforeMethod
  public void setUp() throws Exception {
    Config config = Decoder.getDefaultFlags()
        .withValue("top_n", ConfigValueFactory.fromAnyRef(1))
        .withValue("mark_oovs", ConfigValueFactory.fromAnyRef(false))
        .withValue("output_format", ConfigValueFactory.fromAnyRef("%S"));
    decoder = new Decoder(config);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    decoder.cleanUp();
    decoder = null;
  }

  @Test
  public void givenTokenizedInputWithSpecialCharacters_whenDecoding_thenOutputNormalized() {
    String output = translate(INPUT, decoder);
    assertEquals(output.trim(), GOLD);
  }
}