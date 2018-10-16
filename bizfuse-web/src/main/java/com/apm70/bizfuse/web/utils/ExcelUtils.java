package com.apm70.bizfuse.web.utils;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.util.StringUtils;

/**
 * Excel工具类
 *
 * @author liuyg
 */
public final class ExcelUtils {

    public static String getCellStringValue(final Row row, final int columnNumber,
            final boolean emptyEnabled) {
        Cell cell = row.getCell(columnNumber);
        if (!emptyEnabled && ((cell == null) || (cell.getCellType() == Cell.CELL_TYPE_BLANK))) {
            cell = row.createCell(columnNumber);
            throw new RuntimeException("[" + cell.getSheet().getSheetName() + "]第" + (cell.getRow().getRowNum() + 1)
                    + "行第" + Character.toString((char) ('A' + cell.getColumnIndex())) + "列内容错误： 内容为空。");
        }
        if ((cell == null) || (cell.getCellType() == Cell.CELL_TYPE_BLANK)) {
            return null;
        }
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            final Double numericCellValue = cell.getNumericCellValue();
            final String numStr = String.valueOf(numericCellValue.longValue());
            return numStr;
        }

        if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            final boolean booleanCellValue = cell.getBooleanCellValue();
            final String str = String.valueOf(booleanCellValue);
            return str;
        }

        if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
            throw new RuntimeException("[" + cell.getSheet().getSheetName() + "]第" + (cell.getRow().getRowNum() + 1)
                    + "行第" + Character.toString((char) ('A' + cell.getColumnIndex())) + "列内容错误： 内容格式不是文本内容。");
        }
        final String value = cell.getStringCellValue();
        if (!emptyEnabled && StringUtils.isEmpty(value)) {
            throw new RuntimeException("[" + cell.getSheet().getSheetName() + "]第" + (cell.getRow().getRowNum() + 1)
                    + "行第" + Character.toString((char) ('A' + cell.getColumnIndex())) + "列内容错误： 内容为空。");
        }

        return value.trim();
    }

    public static Integer getCellIntegerValue(final Row row, final int columnNumber,
            final boolean emptyEnabled) {
        final Double value = ExcelUtils.getCellNumbericValue(row, columnNumber, emptyEnabled);
        if (value == null) {
            return null;
        }

        return value.intValue();
    }

    public static Double getCellDoubleValue(final Row row, final int columnNumber,
            final boolean emptyEnabled) {
        return ExcelUtils.getCellNumbericValue(row, columnNumber, emptyEnabled);
    }

    public static Double getCellNumbericValue(final Row row, final int columnNumber,
            final boolean emptyEnabled) {
        Cell cell = row.getCell(columnNumber);
        if (!emptyEnabled && ((cell == null) || (cell.getCellType() == Cell.CELL_TYPE_BLANK))) {
            cell = row.createCell(columnNumber);
            throw new RuntimeException("[" + cell.getSheet().getSheetName() + "]第" + (cell.getRow().getRowNum() + 1)
                    + "行第" + Character.toString((char) ('A' + cell.getColumnIndex())) + "列内容错误： 内容为空。");
        }
        if ((cell == null) || (cell.getCellType() == Cell.CELL_TYPE_BLANK)) {
            return null;
        }

        try {
            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                return cell.getNumericCellValue();
            } else {
                final String cellStringValue = ExcelUtils.getCellStringValue(row, columnNumber, true);
                if (StringUtils.isEmpty(cellStringValue)) {
                    return null;
                }
                final Double result = Double.valueOf(cellStringValue.trim());
                return result;
            }
        } catch (final Exception e) {
            throw new RuntimeException("[" + cell.getSheet().getSheetName() + "]第" + (cell.getRow().getRowNum() + 1)
                    + "行第" + Character.toString((char) ('A' + cell.getColumnIndex())) + "列内容错误： 数据类型不正确，需要数值。");
        }
    }

    public static Date getCellDateValue(final Row row, final int columnNumber,
            final boolean emptyEnabled) {
        Cell cell = row.getCell(columnNumber);
        if (!emptyEnabled && ((cell == null) || (cell.getCellType() == Cell.CELL_TYPE_BLANK))) {
            cell = row.createCell(columnNumber);
            throw new RuntimeException("[" + cell.getSheet().getSheetName() + "]第" + (cell.getRow().getRowNum() + 1)
                    + "行第" + Character.toString((char) ('A' + cell.getColumnIndex())) + "列内容错误： 内容为空。");
        }
        if ((cell == null) || (cell.getCellType() == Cell.CELL_TYPE_BLANK)) {
            return null;
        }

        try {
            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return DateUtil.getJavaDate(cell.getNumericCellValue());
                }
            } else {
                final String cellStringValue = ExcelUtils.getCellStringValue(row, columnNumber, true);
                if (StringUtils.isEmpty(cellStringValue)) {
                    return null;
                }
                final Date result = DateUtils.parseDate(cellStringValue.trim(), "yyyy-MM-dd");
                return result;
            }
        } catch (final Exception e) {
            throw new RuntimeException("[" + cell.getSheet().getSheetName() + "]第" + (cell.getRow().getRowNum() + 1)
                    + "行第" + Character.toString((char) ('A' + cell.getColumnIndex())) + "列内容错误： 日期内容不正确。");
        }
    }
}
