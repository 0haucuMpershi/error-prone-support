package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.Streams;
import com.google.errorprone.refaster.ImportPolicy;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.MayOptionallyUse;
import com.google.errorprone.refaster.annotation.Placeholder;
import com.google.errorprone.refaster.annotation.UseImportPolicy;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with {@link Optional}s. */
final class OptionalTemplates {
  private OptionalTemplates() {}

  static final class OptionalOfNullable<T> {
    @BeforeTemplate
    // XXX: Refaster should be smart enough to also rewrite occurrences in which there are
    // parentheses around the null check, but that's currently not the case. Try to fix that.
    Optional<T> before(T object) {
      return object == null ? Optional.empty() : Optional.of(object);
    }

    @AfterTemplate
    Optional<T> after(T object) {
      return Optional.ofNullable(object);
    }
  }

  /** Prefer {@link Optional#isEmpty()} over the more verbose alternative. */
  static final class OptionalIsEmpty<T> {
    @BeforeTemplate
    boolean before(Optional<T> optional) {
      return !optional.isPresent();
    }

    @AfterTemplate
    boolean after(Optional<T> optional) {
      return optional.isEmpty();
    }
  }

  /** Prefer {@link Optional#isPresent()} over the inverted alternative. */
  static final class OptionalIsPresent<T> {
    @BeforeTemplate
    boolean before(Optional<T> optional) {
      return !optional.isEmpty();
    }

    @AfterTemplate
    boolean after(Optional<T> optional) {
      return optional.isPresent();
    }
  }

  /** Prefer {@link Optional#stream()} over the Guava alternative. */
  static final class OptionalToStream<T> {
    @BeforeTemplate
    Stream<T> before(Optional<T> optional) {
      return Streams.stream(optional);
    }

    @AfterTemplate
    Stream<T> after(Optional<T> optional) {
      return optional.stream();
    }
  }

  /**
   * Don't use the ternary operator to extract the first element of a possibly-empty {@link
   * Collection} as an {@link Optional}.
   */
  static final class OptionalFirstCollectionElement<T> {
    @BeforeTemplate
    Optional<T> before(Collection<T> collection) {
      return collection.isEmpty() ? Optional.empty() : Optional.of(collection.iterator().next());
    }

    @BeforeTemplate
    Optional<T> before(List<T> collection) {
      return collection.isEmpty() ? Optional.empty() : Optional.of(collection.get(0));
    }

    @AfterTemplate
    Optional<T> after(Collection<T> collection) {
      return collection.stream().findFirst();
    }
  }

  /**
   * Don't use the ternary operator to extract the first element of a possibly-empty {@link
   * Iterator} as an {@link Optional}.
   */
  static final class OptionalFirstIteratorElement<T> {
    @BeforeTemplate
    Optional<T> before(Iterator<T> it) {
      return it.hasNext() ? Optional.of(it.next()) : Optional.empty();
    }

    @AfterTemplate
    @UseImportPolicy(ImportPolicy.STATIC_IMPORT_ALWAYS)
    Optional<T> after(Iterator<T> it) {
      return Streams.stream(it).findFirst();
    }
  }

  /** Prefer {@link Optional#filter(Predicate)} over usage of the ternary operator. */
  // XXX: This rule may introduce a compilation error: the `test` expression may reference a
  // non-effectively final variable, which is not allowed in the replacement lambda expression.
  // Maybe our RefasterCheck should test `compiledWithFix`?
  abstract static class TernaryOperatorOptionalPositiveFiltering<T> {
    @Placeholder
    abstract boolean test(T value);

    @BeforeTemplate
    Optional<T> before(T input) {
      return test(input) ? Optional.of(input) : Optional.empty();
    }

    @AfterTemplate
    Optional<T> after(T input) {
      return Refaster.emitCommentBefore(
          "Or Optional.ofNullable (can't auto-infer).", Optional.of(input).filter(v -> test(v)));
    }
  }

  /** Prefer {@link Optional#filter(Predicate)} over usage of the ternary operator. */
  // XXX: This rule may introduce a compilation error: the `test` expression may reference a
  // non-effectively final variable, which is not allowed in the replacement lambda expression.
  // Maybe our RefasterCheck should test `compiledWithFix`?
  abstract static class TernaryOperatorOptionalNegativeFiltering<T> {
    @Placeholder
    abstract boolean test(T value);

    @BeforeTemplate
    Optional<T> before(T input) {
      return test(input) ? Optional.empty() : Optional.of(input);
    }

    @AfterTemplate
    Optional<T> after(T input) {
      return Refaster.emitCommentBefore(
          "Or Optional.ofNullable (can't auto-infer).", Optional.of(input).filter(v -> !test(v)));
    }
  }

  /**
   * Prefer {@link Optional#filter(Predicate)} over {@link Optional#map(Function)} when converting
   * an {@link Optional} to a boolean.
   */
  abstract static class MapOptionalToBoolean<T> {
    @Placeholder
    abstract boolean test(T value);

    @BeforeTemplate
    boolean before(Optional<T> optional, Function<T, Boolean> predicate) {
      return optional.map(predicate).orElse(Refaster.anyOf(false, Boolean.FALSE));
    }

    @AfterTemplate
    boolean after(Optional<T> optional, Predicate<T> predicate) {
      return optional.filter(predicate).isPresent();
    }
  }

  /**
   * Flatten a stream of {@link Optional}s using {@link Optional#stream()}, rather than using one of
   * the more verbose alternatives.
   */
  static final class FlatmapOptionalToStream<T> {
    @BeforeTemplate
    Stream<T> before(Stream<Optional<T>> stream) {
      return Refaster.anyOf(
          stream.filter(Optional::isPresent).map(Optional::get), stream.flatMap(Streams::stream));
    }

    @AfterTemplate
    Stream<T> after(Stream<Optional<T>> stream) {
      return stream.flatMap(Optional::stream);
    }
  }

  /** Within a stream's map operation unconditional {@link Optional#get()} calls can be avoided. */
  // XXX: An alternative approach is to `.flatMap(Optional::stream)`. That may be a bit longer, but
  // yield nicer code. Think about it.
  abstract static class MapToOptionalGet<T, S> {
    @Placeholder
    abstract Optional<S> toOptionalFunction(@MayOptionallyUse T element);

    @BeforeTemplate
    Stream<S> before(Stream<T> stream, Optional<S> optional) {
      return stream.map(e -> toOptionalFunction(e).get());
    }

    @AfterTemplate
    Stream<S> after(Stream<T> stream, Optional<S> optional) {
      return stream.flatMap(e -> toOptionalFunction(e).stream());
    }
  }
}