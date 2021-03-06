/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License, version 2.0, as published by the
 * Free Software Foundation.
 *
 * This program is also distributed with certain software (including but not
 * limited to OpenSSL) that is licensed under separate terms, as designated in a
 * particular file or component or in included license documentation. The
 * authors of MySQL hereby grant you an additional permission to link the
 * program and your derivative works with the separately licensed software that
 * they have included with MySQL.
 *
 * Without limiting anything contained in the foregoing, this file, which is
 * part of MySQL Connector/J, is also subject to the Universal FOSS Exception,
 * version 1.0, a copy of which can be found at
 * http://oss.oracle.com/licenses/universal-foss-exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License, version 2.0,
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301  USA
 */

package com.mysql.cj.conf;

import java.lang.management.ManagementFactory;

import javax.xml.parsers.ParserConfigurationException;

import com.mysql.cj.log.Log;

public class ConnectionPropertyMaxResultBufferParser {

    private static Log logger;

    private static final String[] PERCENT_PHRASES = new String[] { "p", "pct", "percent" };

    /**
     * Method to parse value of max result buffer size.
     *
     * @param value
     *            string containing size of bytes with optional multiplier (T, G, M or K) or percent
     *            value to declare max percent of heap memory to use.
     * @param log
     *            reference to log object
     * @return
     *         value of max result buffer size.
     * @throws ParserConfigurationException
     *             when given value can't be parsed.
     */
    public static long parseProperty(String value, Log log) throws ParserConfigurationException {
        logger = log;
        long result = -1;
        if (checkIfValueContainsPercent(value)) {
            result = parseBytePercentValue(value);
        } else if (checkIfValueExistsToBeParsed(value)) {
            result = parseByteValue(value);
        }
        result = adjustResultSize(result);
        return result;
    }

    /**
     * Method to check if given value can contain percent declaration of size of max result buffer.
     *
     * @param value
     *            Value to check.
     * @return
     *         Result if value contains percent.
     */
    private static boolean checkIfValueContainsPercent(String value) {
        return (value != null) && (getPercentPhraseLengthIfContains(value) != -1);
    }

    /**
     * Method to get percent value of max result buffer size dependable on actual free memory. This
     * method doesn't check other possibilities of value declaration.
     *
     * @param value
     *            string containing percent used to define max result buffer.
     * @return
     *         percent value of max result buffer size.
     * @throws ParserConfigurationException
     *             Exception when given value can't be parsed.
     */
    private static long parseBytePercentValue(String value) throws ParserConfigurationException {
        long result = -1;
        int length;

        if (checkIfValueExistsToBeParsed(value)) {
            length = getPercentPhraseLengthIfContains(value);

            if (length == -1) {
                throw new ParserConfigurationException("10 - bytes; 10K - kilobytes; 10M - megabytes; 10G - gigabytes; 10T - terabytes; "
                        + "10p, 10pct, 10percent - percentage of heap memory");
            }

            result = calculatePercentOfMemory(value, length);
        }
        return result;
    }

    /**
     * Method to get length of percent phrase existing in given string, only if one of phrases exist
     * on the length of string.
     *
     * @param valueToCheck
     *            String which is gonna be checked if contains percent phrase.
     * @return
     *         Length of phrase inside string, returns -1 when no phrase found.
     */
    private static int getPercentPhraseLengthIfContains(String valueToCheck) {
        int result = -1;
        for (String phrase : PERCENT_PHRASES) {
            int indx = getPhraseLengthIfContains(valueToCheck, phrase);
            if (indx != -1) {
                result = indx;
            }
        }
        return result;
    }

    /**
     * Method to get length of given phrase in given string to check, method checks if phrase exist on
     * the end of given string.
     *
     * @param valueToCheck
     *            String which gonna be checked if contains phrase.
     * @param phrase
     *            Phrase to be looked for on the end of given string.
     * @return
     *         Length of phrase inside string, returns -1 when phrase wasn't found.
     */
    private static int getPhraseLengthIfContains(String valueToCheck, String phrase) {
        int searchValueLength = phrase.length();

        if (valueToCheck.length() > searchValueLength) {
            String subValue = valueToCheck.substring(valueToCheck.length() - searchValueLength);
            if (subValue.equals(phrase)) {
                return searchValueLength;
            }
        }
        return -1;
    }

    /**
     * Method to calculate percent of given max heap memory.
     *
     * @param value
     *            String which contains percent + percent phrase which gonna be use during calculations.
     * @param percentPhraseLength
     *            Length of percent phrase inside given value.
     * @return
     *         Size of byte buffer based on percent of max heap memory.
     */
    private static long calculatePercentOfMemory(String value, int percentPhraseLength) {
        String realValue = value.substring(0, value.length() - percentPhraseLength);
        double percent = Double.parseDouble(realValue) / 100;
        long result = (long) (percent * ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());
        return result;
    }

    /**
     * Method to check if given value has any chars to be parsed.
     *
     * @param value
     *            Value to be checked.
     * @return
     *         Result if value can be parsed.
     */
    private static boolean checkIfValueExistsToBeParsed(String value) {
        return value != null && value.length() != 0;
    }

    /**
     * Method to get size based on given string value. String can contains just a number or number +
     * multiplier sign (like T, G, M or K).
     *
     * @param value
     *            Given string to be parsed.
     * @return
     *         Size based on given string.
     * @throws ParserConfigurationException
     *             Exception when given value can't be parsed.
     */
    private static long parseByteValue(String value) throws ParserConfigurationException {
        long result = -1;
        long multiplier = 1;
        long mul = 1000;
        String realValue;
        char sign = value.charAt(value.length() - 1);

        switch (sign) {

            case 'T':
            case 't':
                multiplier *= mul;

            case 'G':
            case 'g':
                multiplier *= mul;

            case 'M':
            case 'm':
                multiplier *= mul;

            case 'K':
            case 'k':
                multiplier *= mul;
                realValue = value.substring(0, value.length() - 1);
                result = Integer.parseInt(realValue) * multiplier;
                break;

            case '%':
                return result;

            default:
                if (sign >= '0' && sign <= '9') {
                    result = Long.parseLong(value);
                } else {
                    throw new ParserConfigurationException("10 - bytes; 10K - kilobytes; 10M - megabytes; 10G - gigabytes; 10T - terabytes; "
                            + "10p, 10pct, 10percent - percentage of heap memory");
                }
                break;
        }
        return result;
    }

    /**
     * Method to adjust result memory limit size. If given memory is larger than 90% of max heap
     * memory then it gonna be reduced to 90% of max heap memory.
     *
     * @param value
     *            Size to be adjusted.
     * @return
     *         Adjusted size (original size or 90% of max heap memory)
     */
    private static long adjustResultSize(long value) {
        if (value > 0.9 * ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax()) {
            long newResult = (long) (0.9 * ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax());

            logger.logWarn(new StringBuilder("WARNING! Required to allocate ").append(value)
                    .append(" bytes, which exceeded possible heap memory size. Assigned ").append(newResult).append(" bytes as limit.").toString());

            value = newResult;
        }
        return value;
    }
}
