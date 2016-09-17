/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.joshua.system;

import static com.typesafe.config.ConfigFactory.parseResources;
import static com.typesafe.config.ConfigValueFactory.fromAnyRef;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.joshua.decoder.Decoder;
import org.apache.joshua.decoder.StructuredTranslation;
import org.apache.joshua.decoder.Translation;
import org.apache.joshua.decoder.segment_file.Sentence;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.typesafe.config.Config;

/**
 * Integration test for the complete Joshua decoder using a toy grammar that translates
 * a bunch of capital letters to lowercase letters. Rules in the test grammar
 * drop and generate additional words and simulate reordering of rules, so that
 * proper extraction of word alignments and other information from the decoder
 * can be tested.
 *
 * @author fhieber
 */
public class StructuredTranslationTest {

  private Decoder decoder = null;
  private static final String INPUT = "A K B1 U Z1 Z2 B2 C";
  private static final String EXPECTED_TRANSLATION = "a b n1 u z c1 k1 k2 k3 n1 n2 n3 c2";
  private static final List<String> EXPECTED_TRANSLATED_TOKENS = asList(EXPECTED_TRANSLATION.split("\\s+"));
  private static final String EXPECTED_WORD_ALIGNMENT_STRING = "0-0 2-1 6-1 3-3 4-4 5-4 7-5 1-6 1-7 1-8 7-12";
  private static final List<List<Integer>> EXPECTED_WORD_ALIGNMENT = asList(
      asList(0), asList(2, 6), asList(), asList(3),
      asList(4, 5), asList(7), asList(1),
      asList(1), asList(1), asList(), asList(),
      asList(), asList(7));
  private static final double EXPECTED_SCORE = -17.0;
  private static final Map<String,Float> EXPECTED_FEATURES = new HashMap<>();
  private static final int EXPECTED_NBEST_LIST_SIZE = 8;
  static {
    EXPECTED_FEATURES.put("glue_0", -1.0f);
    EXPECTED_FEATURES.put("pt_0", 3.0f);
    EXPECTED_FEATURES.put("pt_1", 3.0f);
    EXPECTED_FEATURES.put("pt_2", 3.0f);
    EXPECTED_FEATURES.put("pt_3", 3.0f);
    EXPECTED_FEATURES.put("pt_4", 3.0f);
    EXPECTED_FEATURES.put("pt_5", 3.0f);
    EXPECTED_FEATURES.put("pt_OOV", 7.0f);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    Config flags = parseResources(this.getClass(), "StructuredTranslationTest.conf")
        .withFallback(Decoder.getDefaultFlags());
    decoder = new Decoder(flags);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    decoder.cleanUp();
    decoder = null;
  }

  private Translation decode(String input, Config flags) {
    Sentence sentence = new Sentence(input, 0, flags);
    return decoder.decode(sentence);
  }

  @Test
  public void givenInput_whenRegularOutputFormat_thenExpectedOutput() {
    // GIVEN
    boolean useStructuredOutput = false;
    String outputFormat = "%s | %a ";

    // WHEN
    final String translation = decode(
        INPUT,
        decoder.getFlags()
          .withValue("output_format", fromAnyRef(outputFormat))
          .withValue("use_structured_output", fromAnyRef(useStructuredOutput))).toString().trim();

    // THEN
    assertEquals(translation, EXPECTED_TRANSLATION + " | " + EXPECTED_WORD_ALIGNMENT_STRING);
  }

  @Test
  public void givenInput_whenRegularOutputFormatWithTopN1_thenExpectedOutput() {
    // GIVEN
    boolean useStructuredOutput = false;
    String outputFormat = "%s | %e | %a | %c";
    int topN = 1;

    // WHEN
    final String translation = decode(
        INPUT,
        decoder.getFlags()
          .withValue("top_n", fromAnyRef(topN))
          .withValue("output_format", fromAnyRef(outputFormat))
          .withValue("use_structured_output", fromAnyRef(useStructuredOutput))).toString().trim();

    // THEN
    assertEquals(translation,
        EXPECTED_TRANSLATION + " | " + INPUT + " | " + EXPECTED_WORD_ALIGNMENT_STRING + String.format(" | %.3f", EXPECTED_SCORE));
  }

  @Test
  public void givenInput_whenStructuredOutputFormatWithTopN0_thenExpectedOutput() {
    // GIVEN
    boolean useStructuredOutput = true;
    int topN = 0;

    // WHEN
    final Translation translation = decode(
        INPUT,
        decoder.getFlags()
          .withValue("top_n", fromAnyRef(topN))
          .withValue("use_structured_output", fromAnyRef(useStructuredOutput)));
    
    final StructuredTranslation structuredTranslation = translation.getStructuredTranslations().get(0);
    final String translationString = structuredTranslation.getTranslationString();
    final List<String> translatedTokens = structuredTranslation.getTranslationTokens();
    final float translationScore = structuredTranslation.getTranslationScore();
    final List<List<Integer>> wordAlignment = structuredTranslation.getTranslationWordAlignments();
    final Map<String,Float> translationFeatures = structuredTranslation.getTranslationFeatures();

    // THEN
    assertTrue(translation.getStructuredTranslations().size() == 1);
    assertEquals(translationString, EXPECTED_TRANSLATION);
    assertEquals(translatedTokens, EXPECTED_TRANSLATED_TOKENS);
    assertEquals(translationScore, EXPECTED_SCORE, 0.00001);
    assertEquals(wordAlignment, EXPECTED_WORD_ALIGNMENT);
    assertEquals(translatedTokens.size(), wordAlignment.size());
    assertEquals(translationFeatures.entrySet(), EXPECTED_FEATURES.entrySet());
  }

  @Test
  public void givenInput_whenStructuredOutputFormatWithTopN1_thenExpectedOutput() {
    // GIVEN
    boolean useStructuredOutput = true;
    int topN = 0;

    // WHEN
    final Translation translation = decode(
        INPUT,
        decoder.getFlags()
          .withValue("top_n", fromAnyRef(topN))
          .withValue("use_structured_output", fromAnyRef(useStructuredOutput)));
    
    final List<StructuredTranslation> structuredTranslations = translation.getStructuredTranslations();
    final StructuredTranslation structuredTranslation = structuredTranslations.get(0);
    final String translationString = structuredTranslation.getTranslationString();
    final List<String> translatedTokens = structuredTranslation.getTranslationTokens();
    final float translationScore = structuredTranslation.getTranslationScore();
    final List<List<Integer>> wordAlignment = structuredTranslation.getTranslationWordAlignments();
    final Map<String,Float> translationFeatures = structuredTranslation.getTranslationFeatures();

    // THEN   
    assertTrue(structuredTranslations.size() == 1);
    assertEquals(translationString, EXPECTED_TRANSLATION);
    assertEquals(translatedTokens, EXPECTED_TRANSLATED_TOKENS);
    assertEquals(translationScore, EXPECTED_SCORE, 0.00001);
    assertEquals(wordAlignment, EXPECTED_WORD_ALIGNMENT);
    assertEquals(translatedTokens.size(), wordAlignment.size());
    assertEquals(translationFeatures.entrySet(), EXPECTED_FEATURES.entrySet());
  }

  @Test
  public void givenInput_whenStructuredOutputFormatWithKBest_thenExpectedOutput() {
    // GIVEN
    boolean useStructuredOutput = true;
    int topN = 100;

    // WHEN
    final Translation translation = decode(
        INPUT,
        decoder.getFlags()
          .withValue("top_n", fromAnyRef(topN))
          .withValue("use_structured_output", fromAnyRef(useStructuredOutput)));
    
    final List<StructuredTranslation> structuredTranslations = translation.getStructuredTranslations();
    final StructuredTranslation viterbiTranslation = structuredTranslations.get(0);
    final StructuredTranslation lastKBest = structuredTranslations.get(structuredTranslations.size() - 1);

    // THEN
    assertEquals(structuredTranslations.size(), EXPECTED_NBEST_LIST_SIZE);
    assertTrue(structuredTranslations.size() > 1);
    assertEquals(viterbiTranslation.getTranslationString(), EXPECTED_TRANSLATION);
    assertEquals(viterbiTranslation.getTranslationTokens(), EXPECTED_TRANSLATED_TOKENS);
    assertEquals(viterbiTranslation.getTranslationScore(), EXPECTED_SCORE, 0.00001);
    assertEquals(viterbiTranslation.getTranslationWordAlignments(), EXPECTED_WORD_ALIGNMENT);
    assertEquals(viterbiTranslation.getTranslationFeatures().entrySet(), EXPECTED_FEATURES.entrySet());
    // last entry in KBEST is all input words untranslated, should have 8 OOVs.
    assertEquals(lastKBest.getTranslationString(), INPUT);
    assertEquals(lastKBest.getTranslationFeatures().get("OOVPenalty"), -800.0, 0.0001);

  }

  @Test
  public void givenEmptyInput_whenStructuredOutputFormat_thenEmptyOutput() {
    // GIVEN
    boolean useStructuredOutput = true;

    // WHEN
    final Translation translation = decode(
        "",
        decoder.getFlags()
          .withValue("use_structured_output", fromAnyRef(useStructuredOutput)));
    final StructuredTranslation structuredTranslation = translation.getStructuredTranslations().get(0);
    final String translationString = structuredTranslation.getTranslationString();
    final List<String> translatedTokens = structuredTranslation.getTranslationTokens();
    final float translationScore = structuredTranslation.getTranslationScore();
    final List<List<Integer>> wordAlignment = structuredTranslation.getTranslationWordAlignments();

    // THEN
    assertEquals("", translationString);
    assertTrue(translatedTokens.isEmpty());
    assertEquals(0, translationScore, 0.00001);
    assertTrue(wordAlignment.isEmpty());
  }

  @Test
  public void givenOOVInput_whenStructuredOutputFormat_thenOOVOutput() {
    // GIVEN
    boolean useStructuredOutput = true;
    final String input = "gabarbl";

    // WHEN
    final Translation translation = decode(
        input,
        decoder.getFlags()
          .withValue("use_structured_output", fromAnyRef(useStructuredOutput)));
    
    final StructuredTranslation structuredTranslation = translation.getStructuredTranslations().get(0);
    final String translationString = structuredTranslation.getTranslationString();
    final List<String> translatedTokens = structuredTranslation.getTranslationTokens();
    final Map<String,Float> translationFeatures = structuredTranslation.getTranslationFeatures();
    final float translationScore = structuredTranslation.getTranslationScore();
    final List<List<Integer>> wordAlignment = structuredTranslation.getTranslationWordAlignments();
    
    System.out.println(translationFeatures);
    System.out.println(decoder.getWeights().textFormat());

    // THEN
    assertEquals(input, translationString);
    assertTrue(translatedTokens.contains(input));
    assertEquals(translationScore, -99.0, 0.00001);
    assertTrue(wordAlignment.contains(asList(0)));
  }

  @Test
  public void givenEmptyInput_whenRegularOutputFormat_thenNewlineOutput() {
    // GIVEN
    boolean useStructuredOutput = false;
    String outputFormat = "%s";

    // WHEN
    final Translation translation = decode(
        "",
        decoder.getFlags()
          .withValue("output_format", fromAnyRef(outputFormat))
          .withValue("use_structured_output", fromAnyRef(useStructuredOutput)));
    final String translationString = translation.toString();

    // THEN
    assertEquals("\n", translationString);
  }

}