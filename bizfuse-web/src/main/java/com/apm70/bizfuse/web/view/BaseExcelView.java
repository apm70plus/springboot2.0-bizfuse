package com.apm70.bizfuse.web.view;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.document.AbstractXlsView;

public class BaseExcelView extends AbstractXlsView {

    /**
     * 本地模板文件路径
     */
    private String url;
    /** 报表模板URL（可选） **/
    public static final String TEMPLATE_URL = "templateUrl";

    /**
     * 重写父类的该方法， 支持excel模板
     */
    @Override
    protected Workbook createWorkbook(final Map<String, Object> model, final HttpServletRequest request) {
        String templateUrl = (String) model.get(BaseExcelView.TEMPLATE_URL);
        if (templateUrl == null) {
            templateUrl = this.url;
        }
        if (templateUrl != null) {
            try {
                return this.getTemplateSource(templateUrl, request);
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return new HSSFWorkbook();
    }

    /**
     * 获取模板文件RUL
     *
     * @param model 数据对象
     * @return
     */
    protected String getTemplateUrl(final Map<String, Object> model) {
        String templateUrl = (String) model.get(BaseExcelView.TEMPLATE_URL);
        if (templateUrl == null) {
            templateUrl = this.url;
        }
        return templateUrl;
    }

    protected HSSFWorkbook getTemplateSource(final String url, final HttpServletRequest request)
            throws Exception {
        final ClassPathResource resource = new ClassPathResource(url);
        //        final LocalizedResourceHelper helper = new LocalizedResourceHelper(this.getApplicationContext());
        //        final Locale userLocale = RequestContextUtils.getLocale(request);
        //        final Resource inputFile = helper.findLocalizedResource(url, ".xls", userLocale);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Loading Excel workbook from " + resource);
        }
        return new HSSFWorkbook(resource.getInputStream());
    }

    @Override
    protected void buildExcelDocument(final Map<String, Object> model, final Workbook workbook,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        final String filename = (String) model.get("filename");
        if (filename != null) {
            response.setHeader("Content-Disposition", "attachment; " + this.encodeFilename(request, filename));
        }

        this.buildExcelDocumentContents(model, workbook, request, response);
    }

    protected void buildExcelDocumentContents(final Map<String, Object> model, final Workbook workbook,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {

    }

    /**
     * 构建Excel表体的默认单元格格式
     *
     * @param workbook
     * @return
     */
    protected CellStyle buildDefaultCellStyle(final Workbook workbook) {
        final CellStyle defaultCellStyle = workbook.createCellStyle();
        defaultCellStyle.setWrapText(true);
        defaultCellStyle.setRightBorderColor(HSSFColor.GREY_50_PERCENT.index);
        defaultCellStyle.setBorderBottom(CellStyle.BORDER_THIN); //下边框
        defaultCellStyle.setBorderLeft(CellStyle.BORDER_THIN);//左边框
        defaultCellStyle.setBorderTop(CellStyle.BORDER_THIN);//上边框
        defaultCellStyle.setBorderRight(CellStyle.BORDER_THIN);//右边框
        defaultCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        final Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        defaultCellStyle.setFont(font);

        return defaultCellStyle;
    }

    /**
     * 构建Excel表头的默认单元格格式
     *
     * @param workbook
     * @return
     */
    protected CellStyle buildDefaultHeaderCellStyle(final Workbook workbook) {
        final CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(false);
        cellStyle.setRightBorderColor(HSSFColor.GREY_50_PERCENT.index);
        cellStyle.setBorderBottom(CellStyle.BORDER_THIN); //下边框
        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);//左边框
        cellStyle.setBorderTop(CellStyle.BORDER_THIN);//上边框
        cellStyle.setBorderRight(CellStyle.BORDER_THIN);//右边框
        cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        final Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("宋体");
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        cellStyle.setFont(font);
        return cellStyle;
    }

    /**
     * 合并单元格
     *
     * @param sheet Sheet
     * @param rowStart 起始行（从0开始）
     * @param colStart 结束列（从0开始）
     * @param cellStyle 单元格格式
     * @return 合并后的单元格
     */
    protected Cell buildMergedRowCell(final Sheet sheet, final int rowStart, final int colStart,
            final CellStyle cellStyle) {
        return this.buildMergedRowCell(sheet, rowStart, rowStart, colStart, colStart, cellStyle);
    }

    /**
     * 合并单元格
     *
     * @param sheet Sheet
     * @param rowStart 起始行（从0开始）
     * @param rowEnd 结束行（从0开始）
     * @param colStart 起始列（从0开始）
     * @param colEnd 结束列（从0开始）
     * @param cellStyle 单元格格式
     * @return 合并后的单元格
     */
    protected Cell buildMergedRowCell(final Sheet sheet, final int rowStart, final int rowEnd,
            final int colStart, final int colEnd,
            final CellStyle cellStyle) {
        final CellRangeAddress ca = new CellRangeAddress(rowStart, rowEnd, colStart, colEnd);

        sheet.addMergedRegion(ca);
        Row row = sheet.getRow(rowStart);
        if (row == null) {
            row = sheet.createRow(rowStart);
        }
        final Cell cell = row.createCell(colStart);
        cell.setCellStyle(cellStyle);
        return cell;
    }

    /**
     * 构建单元格
     *
     * @param sheet
     * @param rowNumber
     * @param colNumber
     * @param cellStyle
     * @return
     */
    protected Cell buildCell(final Sheet sheet, final int rowNumber, final int colNumber,
            final CellStyle cellStyle) {
        Row row = sheet.getRow(rowNumber);
        if (row == null) {
            row = sheet.createRow(rowNumber);
        }
        final Cell cell = row.createCell(colNumber);
        cell.setCellStyle(cellStyle);
        return cell;
    }

    protected String encodeFilename(final HttpServletRequest request, final String filename) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "";
        }
        try {
            userAgent = userAgent.toLowerCase();
            if ((userAgent.indexOf("msie") != -1) || (userAgent.indexOf("trident") != -1)) {
                // IE浏览器，只能采用URLEncoder编码
                String name = java.net.URLEncoder.encode(filename, "UTF-8");
                name = StringUtils.replace(name, "+", "%20");//替换空格
                return "filename=\"" + name + "\"";
            } else if (userAgent.indexOf("opera") != -1) {
                // Opera浏览器只能采用filename*
                return "filename*=UTF-8''" + filename;
            } else if (userAgent.indexOf("safari") != -1) {
                // Safari浏览器，只能采用ISO编码的中文输出
                return "filename=\"" + new String(filename.getBytes("UTF-8"), "ISO8859-1") + "\"";
            } else if (userAgent.indexOf("applewebkit") != -1) {
                // Chrome浏览器，只能采用MimeUtility编码或ISO编码的中文输出
                return "filename=\"" + new String(filename.getBytes("UTF-8"), "ISO8859-1") + "\"";
            } else if (userAgent.indexOf("mozilla") != -1) {
                // FireFox浏览器，可以使用MimeUtility或filename*或ISO编码的中文输出
                return "filename=\"=?UTF-8?B?" + (new String(Base64.encodeBase64(filename.getBytes("UTF-8"))))
                        + "?=\"";
            } else {
                return "filename=\"" + java.net.URLEncoder.encode(filename, "UTF-8") + "\"";
            }
        } catch (final UnsupportedEncodingException e) {
            return "filename=\"" + filename + "\"";
        }
    }

    protected Object getData(final Map<String, Object> model) {
        return model.get("data");
    }

    public void setUrl(final String url) {
        this.url = url;
    }

}
