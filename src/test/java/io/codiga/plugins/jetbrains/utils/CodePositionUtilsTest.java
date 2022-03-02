package io.codiga.plugins.jetbrains.utils;

import io.codiga.plugins.jetbrains.testutils.TestBase;
import org.junit.Test;

import java.util.Optional;

public class CodePositionUtilsTest extends TestBase {
  String code_testcase_1 = "\tfunction test (this) {\n return this\n}\n";
  String code_testcase_2 = "function test (this)\t {\n return this\n}\n";
  String code_testcase_3 = "  function test (this)\t {\n return this\n}\n";
  String code_testcase_4 = "\t\t\tfunction test (this) {\n return this\n}\n";

  @Test
  public void testDetectIfTabs() {
    assertTrue(CodePositionUtils.detectIfTabs(code_testcase_1));
    assertFalse(CodePositionUtils.detectIfTabs(code_testcase_2));
    assertFalse(CodePositionUtils.detectIfTabs(code_testcase_3));
  }

  @Test
  public void testGetIndentation() {
    assertEquals(CodePositionUtils.getIndentation(code_testcase_1, true), 1);
    assertEquals(CodePositionUtils.getIndentation(code_testcase_4, true), 3);
    assertEquals(CodePositionUtils.getIndentation(code_testcase_2, false), 0);
    assertEquals(CodePositionUtils.getIndentation(code_testcase_3, false), 2);
  }

  @Test
  public void testGetKeywordFromLine() {
    assertEquals(CodePositionUtils.getKeywordFromLine("bla bli blo.", 11), Optional.of("blo."));
    assertEquals(CodePositionUtils.getKeywordFromLine("bla bli blo.", 2), Optional.of("bla"));
    assertEquals(CodePositionUtils.getKeywordFromLine(null, 11), Optional.empty());
  }
}
