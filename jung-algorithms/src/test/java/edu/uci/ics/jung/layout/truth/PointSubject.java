package edu.uci.ics.jung.layout.truth;

import com.google.common.base.Preconditions;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.MathUtil;
import com.google.common.truth.Subject;
import edu.uci.ics.jung.layout.model.Point;

import javax.annotation.Nullable;

import static com.google.common.truth.Truth.assertAbout;

public class PointSubject extends Subject<PointSubject, Point> {
    private static final long NEG_ZERO_BITS = Double.doubleToLongBits(-0.0D);

    PointSubject(FailureMetadata metadata, @Nullable Point actual) {
        super(metadata, actual);
    }

    public static Subject.Factory<PointSubject, Point> points() {
        return PointSubject::new;
    }

    public static PointSubject assertThat(@Nullable Point actual) {
        return assertAbout(points()).that(actual);
    }

    static void checkTolerance(double tolerance) {
        Preconditions.checkArgument(!Double.isNaN(tolerance), "tolerance cannot be NaN");
        Preconditions.checkArgument(tolerance >= 0.0D, "tolerance (%s) cannot be negative", tolerance);
        Preconditions.checkArgument(Double.doubleToLongBits(tolerance) != NEG_ZERO_BITS, "tolerance (%s) cannot be negative", tolerance);
        Preconditions.checkArgument(tolerance != 1.0D / 0.0, "tolerance cannot be POSITIVE_INFINITY");
    }

    public PointSubject.TolerantPointComparison isWithin(double tolerance) {

        return new PointSubject.TolerantPointComparison() {
            public void of(Point expected) {
                Point actual = (Point)PointSubject.this.actual();
                Preconditions.checkNotNull(actual, "actual value cannot be null. tolerance=%s expected=%s", tolerance, expected);
                PointSubject.checkTolerance(tolerance);
                if (!MathUtil.equalWithinTolerance(actual.x, expected.x, tolerance) || !MathUtil.equalWithinTolerance(actual.y, expected.y, tolerance)) {
                    PointSubject.this.failWithRawMessage("%s and <%s> should have been finite values not within <%s> of each other",
                            new Object[]{PointSubject.this.actualAsString(), expected, tolerance});
                }

            }
        };
    }

    public PointSubject.TolerantPointComparison isNotWithin(final double tolerance) {
        return new PointSubject.TolerantPointComparison() {
            public void of(Point expected) {
                Point actual = (Point)PointSubject.this.actual();
                Preconditions.checkNotNull(actual, "actual value cannot be null. tolerance=%s expected=%s", tolerance, expected);
                PointSubject.checkTolerance(tolerance);
                if (!MathUtil.notEqualWithinTolerance(actual.x, expected.x, tolerance)  && !MathUtil.notEqualWithinTolerance(actual.y, expected.y, tolerance)) {
                    PointSubject.this.failWithRawMessage("%s and <%s> should have been finite values not within <%s> of each other", new Object[]{PointSubject.this.actualAsString(), expected, tolerance});
                }

            }
        };
    }

    public abstract static class TolerantPointComparison {
        private TolerantPointComparison() {
        }

        public abstract void of(Point var1);

        /** @deprecated */
        @Deprecated
        public boolean equals(@Nullable Object o) {
            throw new UnsupportedOperationException("If you meant to compare doubles, use .of(double) instead.");
        }

        /** @deprecated */
        @Deprecated
        public int hashCode() {
            throw new UnsupportedOperationException("Subject.hashCode() is not supported.");
        }
    }

}
