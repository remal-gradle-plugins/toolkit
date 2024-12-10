package name.remal.gradle_plugins.toolkit;

import static name.remal.gradle_plugins.toolkit.NumbersAwareStringComparator.numbersAwareStringComparator;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import org.junit.jupiter.api.Test;

class NumbersAwareStringComparatorTest {

    final Comparator<String> comparator = numbersAwareStringComparator();

    @Test
    @SuppressWarnings("EqualsWithItself")
    void tests() {
        assertThat(comparator.compare("", "")).isZero();
        assertThat(comparator.compare("a", "a")).isZero();
        assertThat(comparator.compare("1", "1")).isZero();
        assertThat(comparator.compare("asd1qwe", "asd1qwe")).isZero();

        assertThat(comparator.compare("", "a")).isNegative();
        assertThat(comparator.compare("a", "")).isPositive();

        assertThat(comparator.compare("", "1")).isNegative();
        assertThat(comparator.compare("1", "")).isPositive();

        assertThat(comparator.compare("qwe1asd", "qwezasd")).isNegative();
        assertThat(comparator.compare("qwezasd", "qwe1asd")).isPositive();

        assertThat(comparator.compare("asd2qwe2zxc", "asd10qwe1zxc")).isNegative();
        assertThat(comparator.compare("asd2qwe2zxc", "asd100000000000000000000000000000000qwe1zxc")).isNegative();
        assertThat(comparator.compare("asd2qwe2zxc", "asd2qwe10zxc")).isNegative();
        assertThat(comparator.compare("asd2qwe2zxc", "asd2qwe100000000000000000000000000000000zxc")).isNegative();

        assertThat(comparator.compare("asd10qwe1zxc", "asd2qwe2zxc")).isPositive();
        assertThat(comparator.compare("asd100000000000000000000000000000000qwe1zxc", "asd2qwe2zxc")).isPositive();
        assertThat(comparator.compare("asd2qwe10zxc", "asd2qwe2zxc")).isPositive();
        assertThat(comparator.compare("asd2qwe100000000000000000000000000000000zxc", "asd2qwe2zxc")).isPositive();
    }

}
