package edu.uci.ics.jung.layout.truth;

import com.google.common.base.Preconditions;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.MathUtil;
import com.google.common.truth.Subject;

import javax.annotation.Nullable;
import java.awt.geom.Rectangle2D;

import static com.google.common.truth.Truth.assertAbout;

public class Rectangle2DSubject extends Subject<Rectangle2DSubject, Rectangle2D> {
    private static final long NEG_ZERO_BITS = Double.doubleToLongBits(-0.0D);

    Rectangle2DSubject(FailureMetadata metadata, @Nullable Rectangle2D actual) {
        super(metadata, actual);
    }

    public static Factory<Rectangle2DSubject, Rectangle2D> rectangles() {
        return Rectangle2DSubject::new;
    }

    public static Rectangle2DSubject assertThat(@Nullable Rectangle2D actual) {
        return assertAbout(rectangles()).that(actual);
    }

    static void checkTolerance(double tolerance) {
        Preconditions.checkArgument(!Double.isNaN(tolerance), "tolerance cannot be NaN");
        Preconditions.checkArgument(tolerance >= 0.0D, "tolerance (%s) cannot be negative", tolerance);
        Preconditions.checkArgument(Double.doubleToLongBits(tolerance) != NEG_ZERO_BITS, "tolerance (%s) cannot be negative", tolerance);
        Preconditions.checkArgument(tolerance != 1.0D / 0.0, "tolerance cannot be POSITIVE_INFINITY");
    }

    public Rectangle2DSubject.TolerantRectangle2DComparison isWithin(double tolerance) {

        return new Rectangle2DSubject.TolerantRectangle2DComparison() {
            public void of(Rectangle2D expected) {
                Rectangle2D actual = (Rectangle2D)Rectangle2DSubject.this.actual();
                Preconditions.checkNotNull(actual, "actual value cannot be null. tolerance=%s expected=%s", tolerance, expected);
                Rectangle2DSubject.checkTolerance(tolerance);
                if (!MathUtil.equalWithinTolerance(actual.getMinX(), expected.getMinX(), tolerance) ||
                        !MathUtil.equalWithinTolerance(actual.getMinY(), expected.getMinY(), tolerance) ||
                        !MathUtil.equalWithinTolerance(actual.getMaxX(), expected.getMaxX(), tolerance) ||
                        !MathUtil.equalWithinTolerance(actual.getMaxY(), expected.getMaxY(), tolerance)
                        ) {
                    Rectangle2DSubject.this.failWithRawMessage("%s and <%s> should have been finite values not within <%s> of each other",
                            new Object[]{Rectangle2DSubject.this.actualAsString(), expected, tolerance});
                }

            }
        };
    }

    public Rectangle2DSubject.TolerantRectangle2DComparison isNotWithin(final double tolerance) {
        return new Rectangle2DSubject.TolerantRectangle2DComparison() {
            public void of(Rectangle2D expected) {
                Rectangle2D actual = (Rectangle2D)Rectangle2DSubject.this.actual();
                Preconditions.checkNotNull(actual, "actual value cannot be null. tolerance=%s expected=%s", tolerance, expected);
                Rectangle2DSubject.checkTolerance(tolerance);
                if (!MathUtil.notEqualWithinTolerance(actual.getMinX(), expected.getMinX(), tolerance)  &&
                        !MathUtil.notEqualWithinTolerance(actual.getMinY(), expected.getMinY(), tolerance) &&
                        !MathUtil.notEqualWithinTolerance(actual.getMaxX(), expected.getMaxX(), tolerance) &&
                        !MathUtil.notEqualWithinTolerance(actual.getMaxY(), expected.getMaxY(), tolerance)
                        ) {
                    Rectangle2DSubject.this.failWithRawMessage("%s and <%s> should have been finite values not within <%s> of each other", new Object[]{Rectangle2DSubject.this.actualAsString(), expected, tolerance});
                }

            }
        };
    }

    public abstract static class TolerantRectangle2DComparison {
        private TolerantRectangle2DComparison() {
        }

        public abstract void of(Rectangle2D var1);

        /** @deprecated */
        @Deprecated
        public boolean equals(@Nullable Object o) {
            throw new UnsupportedOperationException("If you meant to compare rectangles, use .of(rectangle) instead.");
        }

        /** @deprecated */
        @Deprecated
        public int hashCode() {
            throw new UnsupportedOperationException("Subject.hashCode() is not supported.");
        }
    }

}
