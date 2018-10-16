package com.apm70.bizfuse.web.view;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

@Component("defaultExcelView")
@SuppressWarnings("unchecked")
public class DefaultListDataExcelView extends BaseExcelView {

    /** 数据对象列表 **/
    public static final String DATAS = "datas";
    /** 文件名称 **/
    public static final String FILENAME = "filename";
    /** Sheet页名称（可选） **/
    public static final String SHEET_NAME = "sheetname";
    /** 报表中要使用的数据对象Field名列表 **/
    public static final String HEADERS = "headers";
    /** 报表表头名称列表（可选） **/
    public static final String HEADER_NAMES = "headerNames";
    /** 报表列宽定义（可选） **/
    public static final String HEADER_WIDTHS = "headerWidths";
    /** 特殊列的自定义编辑器 */
    public static final String CELL_EDITORS = "cellEditors";

    public static final String CELL_STYLE_DEFAULT = "default";
    /** 日期格式 yyyy-MM-dd */
    public static final String CELL_STYLE_DATE = "date";
    /** 时间戳格式 yyyy-MM-dd HH:MM:SS */
    public static final String CELL_STYLE_TIMESTAMP = "timestamp";
    /** 起始行， 默认是1 */
    public static final String START_ROW = "startRow";

    private final boolean enableDateStyle = false;

    /**
     * 构建Excel文档内容
     */
    @Override
    protected void buildExcelDocumentContents(final Map<String, Object> model, final Workbook workbook,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String fileName = (String) model.get(DefaultListDataExcelView.FILENAME);
        String sheetName = (String) model.get(DefaultListDataExcelView.SHEET_NAME);

        Sheet sheet = null;
        final boolean hasTemplage = super.getTemplateUrl(model) != null;
        if (!hasTemplage) { // 如果没有模板，需要初始化Sheet格式
            if (!StringUtils.hasText(sheetName)) {
                sheetName = fileName.substring(0, fileName.indexOf(".xls"));
            }
            sheet = workbook.createSheet(sheetName);
            sheet.setDisplayGridlines(false);
            this.initColumnWidth(model, sheet);
            this.buildExcelHeader(model, workbook, sheet);
        } else {
            sheet = workbook.getSheetAt(0);
            if (StringUtils.hasText(sheetName)) {
                workbook.setSheetName(0, sheetName);
            }
        }

        final List<String> headers = this.getHeaders(model);
        final List<String> columnStyles = this.getColumnStyles(model);
        List<CellStyle> cellStyles = null;
        if (hasTemplage) {
            cellStyles = this.getCellStylesOfTemplate(model, columnStyles, sheet);
        }
        if (cellStyles == null) {
            cellStyles = this.getCellStyles(columnStyles, workbook);
        }
        final List<Object> dataList = (List<Object>) model.get(DefaultListDataExcelView.DATAS);

        final List<CellEditor> cellEditors = (List<CellEditor>) model.get(DefaultListDataExcelView.CELL_EDITORS);
        final Map<String, CellEditor> editors = new HashMap<>();
        if (cellEditors != null) {
            cellEditors.forEach(editor -> {
                editors.put(editor.getHeader(), editor);
            });
        }

        int row = this.getStartRowNo(model);
        for (int i = 0; i < dataList.size(); i++) {
            for (int col = 0; col <= headers.size(); col++) {
                if (col == 0) {
                    final Cell cell = this.buildCell(sheet, row, col, cellStyles.get(0));
                    cell.setCellValue(i + 1);
                    continue;
                }
                final String header = headers.get(col - 1);
                final Cell cell = this.buildCell(sheet, row, col, cellStyles.get(col));
                final CellEditor editor = editors.get(header);
                if (editor == null) {
                    this.setCellValue(cell, header, dataList.get(i), columnStyles.get(col - 1));
                } else {
                    editor.setCellValue(cell, dataList.get(i));
                }
            }
            ++row;
        }
    }

    private Integer getStartRowNo(final Map<String, Object> model) {
        Integer startRow = (Integer) model.get(DefaultListDataExcelView.START_ROW);
        if (startRow == null) {
            startRow = 1;
        }
        return startRow;
    }

    private List<String> getColumnStyles(final Map<String, Object> model) {
        final List<String> styles = new ArrayList<>();
        final List<String> headers = (List<String>) model.get(DefaultListDataExcelView.HEADERS);
        for (int i = 0; i < headers.size(); i++) {
            final String header = headers.get(i);
            String style = DefaultListDataExcelView.CELL_STYLE_DEFAULT;
            if (header.indexOf(":") > 0) {
                style = header.split(":")[1];
            }
            styles.add(style);
        }
        return styles;
    }

    /**
     * 给Excel单元格赋值
     *
     * @param cell
     * @param header
     * @param object
     */
    protected void setCellValue(final Cell cell, final String header, final Object object, final String style) {
        final String[] fields = header.split("\\.");
        Object value = object;
        for (final String field : fields) {
            final Class<?> clazz = ClassUtils.getUserClass(value);
            value = this.getFieldValue(clazz, field, value);
            if (value == null) {
                break;
            }
        }
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            if ((style != null) && DefaultListDataExcelView.CELL_STYLE_DATE.equals(style)) {
                cell.setCellValue(DateFormatUtils.format((Date) value, "yyyy-MM-dd", LocaleContextHolder.getLocale()));
            } else if ((style != null) && DefaultListDataExcelView.CELL_STYLE_TIMESTAMP.equals(style)) {
                cell.setCellValue(
                        DateFormatUtils.format((Date) value, "yyyy-MM-dd HH:mm:ss", LocaleContextHolder.getLocale()));
            } else {
                cell.setCellValue(DateFormatUtils.format((Date) value, "yyyy-MM-dd", LocaleContextHolder.getLocale()));
            }
        } else if (value instanceof Double) {
            BigDecimal decimal = new BigDecimal((Double) value);
            decimal = decimal.setScale(2, RoundingMode.HALF_UP);
            final CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(decimal.toPlainString());
        } else if (value instanceof Float) {
            BigDecimal decimal = new BigDecimal((Float) value);
            decimal = decimal.setScale(2, RoundingMode.HALF_UP);
            final CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(decimal.toPlainString());
        } else if (value instanceof BigDecimal) {
            final CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(((BigDecimal) value).toPlainString());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    /**
     * 构建Excel表头
     *
     * @param model
     * @param workbook
     * @param sheet
     */
    protected void buildExcelHeader(final Map<String, Object> model, final Workbook workbook,
            final Sheet sheet) {
        final CellStyle headerCellStyle = this.buildDefaultHeaderCellStyle(workbook);
        final List<String> headers = (List<String>) model.get(DefaultListDataExcelView.HEADER_NAMES);

        final int rowNumber = 0;
        int colNumber = 0;
        Cell cell = super.buildCell(sheet, rowNumber, colNumber++, headerCellStyle);
        cell.setCellValue("序号");
        for (final String columnName : headers) {
            cell = super.buildCell(sheet, rowNumber, colNumber++, headerCellStyle);
            cell.setCellValue(columnName);
        }
    }

    /**
     * 初始化列宽
     *
     * @param model
     * @param sheet
     */
    protected void initColumnWidth(final Map<String, Object> model, final Sheet sheet) {
        final List<Integer> headerWidths = (List<Integer>) model.get(DefaultListDataExcelView.HEADER_WIDTHS);
        sheet.setColumnWidth(0, 1000);
        if (headerWidths != null) {
            for (int column = 1; column <= headerWidths.size(); column++) {
                sheet.setColumnWidth(column, headerWidths.get(column));
            }
        } else {
            final List<String> headers = (List<String>) model.get(DefaultListDataExcelView.HEADERS);
            for (int column = 1; column <= headers.size(); column++) {
                sheet.setColumnWidth(column, 4400);
            }
        }
    }

    /**
     * 获取表头信息
     *
     * @param model
     * @return
     */
    protected List<String> getHeaders(final Map<String, Object> model) {
        final List<String> headerList = new ArrayList<>();
        final List<String> headers = (List<String>) model.get(DefaultListDataExcelView.HEADERS);
        for (int i = 0; i < headers.size(); i++) {
            final String header = headers.get(i);
            if (header.indexOf(":") > 0) {
                headerList.add(header.split(":")[0]);
            } else {
                headerList.add(header);
            }
        }
        return headerList;
    }

    private List<CellStyle> getCellStylesOfTemplate(final Map<String, Object> model, final List<String> styles,
            final Sheet sheet) {
        final int startRow = this.getStartRowNo(model);
        final Row row = sheet.getRow(startRow);
        if (row == null) {
            return null;
        }
        final List<CellStyle> cellStyles = new ArrayList<>();
        for (int i = 0; i <= styles.size(); i++) {
            final Cell cell = row.getCell(i);
            cellStyles.add(cell.getCellStyle());
        }
        return cellStyles;
    }

    private List<CellStyle> getCellStyles(final List<String> styles, final Workbook workbook) {
        final List<CellStyle> cellStyles = new ArrayList<>();
        final CellStyle defaultCellStyle = this.buildDefaultCellStyle(workbook);
        cellStyles.add(defaultCellStyle); // 第一列为序号列
        for (final String style : styles) {
            // 由于日期格式跨系统不兼容, 日期统一为文本格式
            if (!this.enableDateStyle) {
                cellStyles.add(defaultCellStyle);
                continue;
            }
            CellStyle cellStyle = null;
            switch (style) {
            case CELL_STYLE_DATE:
                cellStyle = this.buildDefaultDateCellStyle(workbook);
                break;
            case CELL_STYLE_TIMESTAMP:
                cellStyle = this.buildDefaultDateTimeCellStyle(workbook);
                break;
            default:
                cellStyle = defaultCellStyle;
                break;
            }
            cellStyles.add(cellStyle);
        }
        return cellStyles;
    }

    private Object getFieldValue(final Class<?> clazz, final String field, final Object obj) {
        final Method method = this.getFieldMethod(clazz, field);
        if (method != null) {
            method.setAccessible(true);
            try {
                return method.invoke(obj, new Object[0]);
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            final Field theField = this.getField(clazz, field);
            if (theField == null) {
                throw new RuntimeException("field is not found:" + field);
            }
            theField.setAccessible(true);
            try {
                return theField.get(obj);
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private Field getField(final Class<?> clazz, final String field) {
        if (clazz == Object.class) {
            return null;
        }
        try {
            return clazz.getDeclaredField(field);
        } catch (final NoSuchFieldException e) {
        } catch (final SecurityException e) {
        }
        return this.getField(clazz.getSuperclass(), field);
    }

    private Method getFieldMethod(final Class<?> clazz, final String field) {
        String methodName = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
        try {
            final Method method = clazz.getMethod(methodName, new Class[0]);
            return method;
        } catch (final NoSuchMethodException e) {
        } catch (final SecurityException e) {
        }
        methodName = "is" + field.substring(0, 1).toUpperCase() + field.substring(1);
        try {
            final Method method = clazz.getMethod(methodName, new Class[0]);
            return method;
        } catch (final NoSuchMethodException e) {
        } catch (final SecurityException e) {
        }
        return null;
    }

    public interface CellEditor {

        /**
         * 编辑单元格的值
         *
         * @param cell
         * @param rowData
         */
        void setCellValue(final Cell cell, final Object rowData);

        /**
         * 单元格对应的Header
         *
         * @return
         */
        String getHeader();
    }

    protected CellStyle buildDefaultDateCellStyle(final Workbook workbook) {
        final CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        cellStyle.setRightBorderColor(HSSFColor.GREY_50_PERCENT.index);
        cellStyle.setBorderBottom(CellStyle.BORDER_THIN); //下边框
        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);//左边框
        cellStyle.setBorderTop(CellStyle.BORDER_THIN);//上边框
        cellStyle.setBorderRight(CellStyle.BORDER_THIN);//右边框
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        final Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        cellStyle.setFont(font);
        final CreationHelper creationHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd"));
        return cellStyle;
    }

    protected CellStyle buildDefaultDateTimeCellStyle(final Workbook workbook) {
        final CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        cellStyle.setRightBorderColor(HSSFColor.GREY_50_PERCENT.index);
        cellStyle.setBorderBottom(CellStyle.BORDER_THIN); //下边框
        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);//左边框
        cellStyle.setBorderTop(CellStyle.BORDER_THIN);//上边框
        cellStyle.setBorderRight(CellStyle.BORDER_THIN);//右边框
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        final Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        cellStyle.setFont(font);
        final CreationHelper creationHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd hh:mm:ss"));
        return cellStyle;
    }
}
