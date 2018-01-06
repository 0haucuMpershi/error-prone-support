package systems.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class LexicographicalAnnotationAttributeListingCheckTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(
          LexicographicalAnnotationAttributeListingCheck.class, getClass());
  private final CompilationTestHelper restrictedCompilationTestHelper =
      CompilationTestHelper.newInstance(
              LexicographicalAnnotationAttributeListingCheck.class, getClass())
          .setArgs(
              ImmutableList.of(
                  "-XepOpt:LexicographicalAnnotationAttributeListing:Includes=pkg.A.Foo,pkg.A.Bar",
                  "-XepOpt:LexicographicalAnnotationAttributeListing:Excludes=pkg.A.Bar#value"));
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(
          new LexicographicalAnnotationAttributeListingCheck(), getClass());

  @Test
  public void testIdentification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static java.math.RoundingMode.UP;",
            "import static java.math.RoundingMode.DOWN;",
            "",
            "import java.math.RoundingMode;",
            "",
            "interface A {",
            "  @interface Foo {",
            "    String[] value() default {};",
            "    int[] ints() default {};",
            "    Class<?>[] cls() default {};",
            "    RoundingMode[] enums() default {};",
            "    Bar[] anns() default {};",
            "  }",
            "",
            "  @interface Bar {",
            "    String[] value() default {};",
            "  }",
            "",
            "  @Foo({}) A noString();",
            "  @Foo({\"a\"}) A oneString();",
            "  @Foo({\"a\", \"b\"}) A sortedStrings();",
            "  // BUG: Diagnostic contains:",
            "  @Foo({\"b\", \"a\"}) A unsortedString();",
            "",
            "  @Foo(ints = {}) A noInts();",
            "  @Foo(ints = {0}) A oneInt();",
            "  @Foo(ints = {0, 1}) A sortedInts();",
            "  @Foo(ints = {1, 0}) A unsortedInts();",
            "",
            "  @Foo(cls = {}) A noClasses();",
            "  @Foo(cls = {int.class}) A oneClass();",
            "  @Foo(cls = {int.class, long.class}) A sortedClasses();",
            "  // BUG: Diagnostic contains:",
            "  @Foo(cls = {long.class, int.class}) A unsortedClasses();",
            "",
            "  @Foo(enums = {}) A noEnums();",
            "  @Foo(enums = {DOWN}) A oneEnum();",
            "  @Foo(enums = {DOWN, UP}) A sortedEnums();",
            "  // BUG: Diagnostic contains:",
            "  @Foo(enums = {UP, DOWN}) A unsortedEnums();",
            "",
            "  @Foo(anns = {}) A noAnns();",
            "  @Foo(anns = {@Bar(\"a\")}) A oneAnnd();",
            "  @Foo(anns = {@Bar(\"a\"), @Bar(\"b\")}) A sortedAnns();",
            "  // BUG: Diagnostic contains:",
            "  @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) A unsortedAnns();",
            "  // BUG: Diagnostic contains:",
            "  @Foo(anns = {@Bar(\"a\"), @Bar({\"b\", \"a\"})}) A unsortedInnderAnns();",
            "}")
        .doTest();
  }

  // XXX: Note that in the output below in one instance redundant `value = ` assignments are
  // introduced.
  // Avoiding that might make the code too complex. Instead, users can have the
  // `CanonicalAnnotationSyntaxCheck`
  // correct the situation in a subsequent run.
  @Test
  public void testReplacement() throws IOException {
    refactoringTestHelper
        .addInputLines(
            "in/A.java",
            "import static java.math.RoundingMode.UP;",
            "import static java.math.RoundingMode.DOWN;",
            "",
            "import java.math.RoundingMode;",
            "",
            "interface A {",
            "  @interface Foo {",
            "    String[] value() default {};",
            "    Class<?>[] cls() default {};",
            "    RoundingMode[] enums() default {};",
            "    Bar[] anns() default {};",
            "  }",
            "",
            "  @interface Bar {",
            "    String[] value() default {};",
            "  }",
            "",
            "  @Foo({\"b\", \"a\"}) A unsortedString();",
            "  @Foo(cls = {long.class, int.class}) A unsortedClasses();",
            "  @Foo(enums = {UP, DOWN}) A unsortedEnums();",
            "  @Foo(anns = {@Bar(\"b\"), @Bar(\"a\")}) A unsortedAnns();",
            "  @Foo(anns = {@Bar(\"a\"), @Bar({\"b\", \"a\"})}) A unsortedInnderAnns();",
            "}")
        .addOutputLines(
            "out/A.java",
            "import static java.math.RoundingMode.UP;",
            "import static java.math.RoundingMode.DOWN;",
            "",
            "import java.math.RoundingMode;",
            "",
            "interface A {",
            "  @interface Foo {",
            "    String[] value() default {};",
            "    Class<?>[] cls() default {};",
            "    RoundingMode[] enums() default {};",
            "    Bar[] anns() default {};",
            "  }",
            "",
            "  @interface Bar {",
            "    String[] value() default {};",
            "  }",
            "",
            "  @Foo({\"a\", \"b\"}) A unsortedString();",
            "  @Foo(cls = {int.class, long.class}) A unsortedClasses();",
            "  @Foo(enums = {DOWN, UP}) A unsortedEnums();",
            "  @Foo(anns = {@Bar(value = \"a\"), @Bar(value = \"b\")}) A unsortedAnns();",
            "  @Foo(anns = {@Bar(\"a\"), @Bar({\"a\", \"b\"})}) A unsortedInnderAnns();",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  public void testFiltering() {
    /* Some violations are not flagged because they are not in- or excluded. */
    restrictedCompilationTestHelper
        .addSourceLines(
            "pkg/A.java",
            "package pkg;",
            "",
            "interface A {",
            "  @interface Foo {",
            "    String[] value() default {};",
            "    String[] value2() default {};",
            "  }",
            "",
            "  @interface Bar {",
            "    String[] value() default {};",
            "    String[] value2() default {};",
            "  }",
            "",
            "  @interface Baz {",
            "    String[] value() default {};",
            "    String[] value2() default {};",
            "  }",
            "",
            "  // BUG: Diagnostic contains:",
            "  @Foo({\"b\", \"a\"}) A fooValue();",
            "  // BUG: Diagnostic contains:",
            "  @Foo(value2 = {\"b\", \"a\"}) A fooValue2();",
            "  @Bar({\"b\", \"a\"}) A barValue();",
            "  // BUG: Diagnostic contains:",
            "  @Bar(value2 = {\"b\", \"a\"}) A barValue2();",
            "  @Baz({\"b\", \"a\"}) A bazValue();",
            "  @Baz(value2 = {\"b\", \"a\"}) A bazValue2();",
            "}")
        .doTest();
  }
}