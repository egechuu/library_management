package com.example.library.unit;

import com.example.library.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UNIT TEST - Model Layer
 */
class BorrowRecordTest {

    private Book createSampleBook() {
        Book book = new Book("978-0-13-468599-1", "Clean Code", "Robert C. Martin", 3, Genre.TECHNOLOGY);
        book.setId(1L);
        return book;
    }

    private Member createSampleMember() {
        Member member = new Member("Alice", "alice@example.com", MembershipType.STANDARD);
        member.setId(1L);
        return member;
    }

    // =========================================================================
    // EXAMPLE: calculateFine() tests — filled in as reference
    // =========================================================================

    @Nested
    @DisplayName("calculateFine()")
    class CalculateFineTests {

        @Test
        @DisplayName("should return 0 when book is returned on time")
        void shouldReturnZeroFine_WhenReturnedOnTime() {
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            record.setReturnDate(record.getDueDate()); // returned exactly on due date

            assertEquals(0.0, record.calculateFine());
        }

        @Test
        @DisplayName("should return 0 when book is returned before due date")
        void shouldReturnZeroFine_WhenReturnedEarly() {
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            record.setReturnDate(record.getBorrowDate().plusDays(5)); // returned after 5 days

            assertEquals(0.0, record.calculateFine());
        }

        @Test
        @DisplayName("should calculate correct fine when returned 3 days late")
        void shouldCalculateCorrectFine_WhenReturnedLate() {
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            record.setReturnDate(record.getDueDate().plusDays(3)); // 3 days late

            double expectedFine = 3 * BorrowRecord.DAILY_FINE_RATE; // 3 * 1.50 = 4.50
            assertEquals(expectedFine, record.calculateFine());
        }

        @Test
        @DisplayName("should return 0 when book is not yet returned")
        void shouldReturnZeroFine_WhenNotYetReturned() {
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            // returnDate is null

            assertEquals(0.0, record.calculateFine());
        }
    }

    // =========================================================================
    // TODO: Students should write these tests
    // AUTHOR : @egechuu
    // DATE : 2026-04-30
    // =========================================================================

    @Nested
    @DisplayName("isOverdue()")
    class IsOverdueTests {

        @Test
        @DisplayName("should return true when checked after due date and still borrowed")
        void shouldBeOverdue_WhenPastDueDateAndStillBorrowed() {
            // create a borrow record with a due date past the current date
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            LocalDate afterDueDate = record.getDueDate().plusDays(1);
            // check if it's overdue after the due date 
            assertTrue(record.isOverdue(afterDueDate));
        }

        @Test
        @DisplayName("should return false when checked before due date")
        void shouldNotBeOverdue_WhenBeforeDueDate() {
            // create a borrow record and check if it's overdue before the due date
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            // date before the due date
            LocalDate beforeDueDate = record.getDueDate().minusDays(1);

            assertFalse(record.isOverdue(beforeDueDate));
        }

        @Test
        @DisplayName("should return false when book is already returned (even if past due)")
        void shouldNotBeOverdue_WhenAlreadyReturned() {
            // create a borrow record &  set it to returned
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            record.setStatus(BorrowStatus.RETURNED);
            // date after the due date
            LocalDate afterDueDate = record.getDueDate().plusDays(5);

            assertFalse(record.isOverdue(afterDueDate));
        }

        @Test
        @DisplayName("should return false on exactly the due date")
        void shouldNotBeOverdue_OnExactDueDate() {
            // create a borrow record 
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            LocalDate exactDueDate = record.getDueDate();
            // check if it's overdue on the exact due date
            assertFalse(record.isOverdue(exactDueDate));
        }
    }

    @Nested
    @DisplayName("Constructor / default values")
    class ConstructorTests {

        @Test
        @DisplayName("should set borrow date to today")
        void shouldSetBorrowDateToToday() {
            // create a borrow record 
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            // check if the borrow date is set to the current date
            assertEquals(LocalDate.now(), record.getBorrowDate());
        }

        @Test
        @DisplayName("should set due date to 14 days from today")
        void shouldSetDueDateTo14DaysFromToday() {
            // create a borrow record 
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            // check if the due date is set to 14 days from the current date
            LocalDate expected = LocalDate.now().plusDays(BorrowRecord.STANDARD_BORROW_DAYS);

            assertEquals(expected, record.getDueDate());
        }

        @Test
        @DisplayName("should set status to BORROWED")
        void shouldSetStatusToBorrowed() {
            // create a borrow record
            BorrowRecord record = new BorrowRecord(createSampleBook(), createSampleMember());
            // check if the status is set to BORROWED by default
            assertEquals(BorrowStatus.BORROWED, record.getStatus());
        }
    }
}