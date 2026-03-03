package com.parking.shared.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

class BusinessDayUtilsTest {

    @Test
    void countBusinessDays_mondayToFriday_returns5() {
        LocalDate monday = LocalDate.of(2025, 3, 3);
        LocalDate friday = LocalDate.of(2025, 3, 7);
        assertThat(BusinessDayUtils.countBusinessDays(monday, friday)).isEqualTo(5);
    }

    @Test
    void countBusinessDays_includesWeekend_skipsWeekend() {
        LocalDate monday = LocalDate.of(2025, 3, 3);
        LocalDate nextMonday = LocalDate.of(2025, 3, 10);
        // Mon-Fri + Mon = 6 business days
        assertThat(BusinessDayUtils.countBusinessDays(monday, nextMonday)).isEqualTo(6);
    }

    @Test
    void countBusinessDays_sameDay_returns1() {
        LocalDate monday = LocalDate.of(2025, 3, 3);
        assertThat(BusinessDayUtils.countBusinessDays(monday, monday)).isEqualTo(1);
    }

    @Test
    void isBusinessDay_returnsTrue_forMonday() {
        assertThat(BusinessDayUtils.isBusinessDay(LocalDate.of(2025, 3, 3))).isTrue();
    }

    @Test
    void isBusinessDay_returnsFalse_forSaturday() {
        assertThat(BusinessDayUtils.isBusinessDay(LocalDate.of(2025, 3, 8))).isFalse();
    }
}
