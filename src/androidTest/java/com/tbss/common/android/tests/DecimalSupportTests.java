package com.tbss.common.android.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.requery.android.database.sqlite.SQLiteDatabase;

@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(AndroidJUnit4.class)
public class DecimalSupportTests {

    private SQLiteDatabase mDatabase;
    private static final String decimalOption = "ENABLE_DECIMAL";
    private static final BigDecimal[] testNumbers = new BigDecimal[] {
            new BigDecimal("434342342342.43433"),
            new BigDecimal("29989943.458483371268433"),
            new BigDecimal("9"),
            new BigDecimal("1000000000000"),
            new BigDecimal("1234567890.12345678901234567890"),
            new BigDecimal("0.00000000000000000001"),
            new BigDecimal("99999999999999999999.99999999999999999999"),
            new BigDecimal("-1234567890.12345678901234567890"),
            new BigDecimal("3.14159265358979323846")
    };

    @Before
    public void setUp() {
        mDatabase = SQLiteDatabase.create(null);
    }

    @After
    public void tearDown() {
        mDatabase.close();
    }

    @MediumTest
    @Test
    public void checkCompilationOptionsSupport() {
        int optionNumber = 0;
        List<String> options = new ArrayList<>();
        while (true) {
            var cursor = mDatabase.query("select sqlite_compileoption_get(" + optionNumber + ")");
            assertEquals(1, cursor.getCount());
            cursor.moveToFirst();
            var option = cursor.getString(0);
            if (option == null)
                break;
            options.add(option);
            optionNumber++;
        }
    }

    @MediumTest
    @Test
    public void checkDecimalOptionInOptionsList() {
        int optionNumber = 0;
        List<String> options = new ArrayList<>();
        while (true) {
            var cursor = mDatabase.query("select sqlite_compileoption_get(" + optionNumber + ")");
            assertEquals(1, cursor.getCount());
            cursor.moveToFirst();
            var option = cursor.getString(0);
            if (option == null)
                break;
            options.add(option);
            optionNumber++;
        }
        assert options.contains(decimalOption) : "List does not contain: " + decimalOption;
    }

    @MediumTest
    @Test
    public void checkDecimalOption() {
        var cursor = mDatabase.query("select sqlite_compileoption_used('" + decimalOption + "')");
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        var optionPresent = cursor.getInt(0);
        assertEquals(1, optionPresent);
    }

    @MediumTest
    @Test
    public void checkUnknownOption() {
        var cursor = mDatabase.query("select sqlite_compileoption_used('NON_EXISTENT_OPTION')");
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        var optionAbsent = cursor.getInt(0);
        assertEquals(0, optionAbsent);
    }

    @MediumTest
    @Test
    public void addingDecimals() {
        var cursor = mDatabase.query("select decimal_add('" + testNumbers[0].toPlainString() + "', '" + testNumbers[1].toPlainString() + "')");
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        var result = cursor.getString(0);
        assertEquals(testNumbers[0].add(testNumbers[1]), new BigDecimal(result));
    }

    @MediumTest
    @Test
    public void subtractingDecimals() {
        var cursor = mDatabase.query("select decimal_sub('" + testNumbers[0].toPlainString() + "', '" + testNumbers[1].toPlainString() + "')");
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        var result = cursor.getString(0);
        assertEquals(testNumbers[0].subtract(testNumbers[1]), new BigDecimal(result));
    }

    @MediumTest
    @Test
    public void multiplyingDecimals() {
        var cursor = mDatabase.query("select decimal_mul('" + testNumbers[0].toPlainString() + "', '" + testNumbers[1].toPlainString() + "')");
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        var result = cursor.getString(0);
        assertEquals(testNumbers[0].multiply(testNumbers[1]), new BigDecimal(result));
    }

    @MediumTest
    @Test
    public void totalDecimals() {
        mDatabase.execSQL("CREATE TABLE numbers (_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, number DECIMAL_TEXT not null)");
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal number : testNumbers) {
            mDatabase.execSQL("INSERT INTO numbers (number) values ('" + number.toPlainString() + "')");
            sum = sum.add(number);
        }
        var cursor = mDatabase.query("select decimal_sum(number) from numbers");
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        var total = cursor.getString(0);
        assertEquals(sum, new BigDecimal(total));
    }

    @MediumTest
    @Test
    public void orderingDecimalsAscending() {
        mDatabase.execSQL("CREATE TABLE numbers (_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, number DECIMAL_TEXT not null)");
        for (BigDecimal number : testNumbers) {
            mDatabase.execSQL("INSERT INTO numbers (number) values ('" + number.toPlainString() + "')");
        }
        var cursor = mDatabase.query("select number from numbers ORDER BY number COLLATE DECIMAL");
        assertEquals(testNumbers.length, cursor.getCount());
        if (cursor.moveToFirst()) {
            var current = new BigDecimal(cursor.getString(0));
            while (cursor.moveToNext()) {
                var next = new BigDecimal(cursor.getString(0));
                assert current.compareTo(next) <= 0 : "Decimals not in order: " + current + " > " + next;
                current = next;
            }
        }   
    }

    @MediumTest
    @Test
    public void orderingDecimalsDescending() {
        mDatabase.execSQL("CREATE TABLE numbers (_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, number DECIMAL_TEXT not null)");
        for (BigDecimal number : testNumbers) {
            mDatabase.execSQL("INSERT INTO numbers (number) values ('" + number.toPlainString() + "')");
        }
        var cursor = mDatabase.query("select number from numbers ORDER BY number COLLATE DECIMAL DESC");
        assertEquals(testNumbers.length, cursor.getCount());
        if (cursor.moveToFirst()) {
            var current = new BigDecimal(cursor.getString(0));
            while (cursor.moveToNext()) {
                var next = new BigDecimal(cursor.getString(0));
                assert current.compareTo(next) >= 0 : "Decimals not in order: " + current + " < " + next;
                current = next;
            }
        }   
    }
}