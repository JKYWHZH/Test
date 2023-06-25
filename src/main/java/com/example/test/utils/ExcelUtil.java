package com.example.test.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;

@Slf4j(topic = "excel工具类")
public class ExcelUtil {

    /**
     * 标题风格
     *
     * @param workbook 工作excel
     * @return 单元格样式
     */
    public static CellStyle titleStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        //设置背景色
        //cellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        //设置上边框
        cellStyle.setBorderTop(BorderStyle.THIN);
        //设置下边框
        cellStyle.setBorderBottom(BorderStyle.THIN);
        //cellStyle.setBorderLeft(BorderStyle.THIN);
        //cellStyle.setBorderRight(BorderStyle.THIN);
        //自动换行
        cellStyle.setWrapText(true);
        //设置水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        //字体
        Font font = workbook.createFont();
        font.setFontName("黑体");
        font.setFontHeightInPoints((short) 12);
        cellStyle.setFont(font);

        return cellStyle;
    }

    /**
     * 正常单元格样式
     *
     * @param workbook 工作excel
     * @return 单元格样式
     */
    public static CellStyle cellStyle(Workbook workbook){
        CellStyle cellStyle = workbook.createCellStyle();

        //自动换行
        cellStyle.setWrapText(true);
        //设置水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        return cellStyle;
    }

    /**
     * 错误单元格样式
     *
     * @param workbook 工作excel
     * @return 单元格样式
     */
    public static CellStyle cellErrorStyle(Workbook workbook){
        CellStyle cellStyle = workbook.createCellStyle();
        //设置背景色
        cellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        //设置上边框
        cellStyle.setBorderTop(BorderStyle.THIN);
        //设置下边框
        cellStyle.setBorderBottom(BorderStyle.THIN);

        cellStyle.setTopBorderColor(IndexedColors.RED.getIndex());
        cellStyle.setBottomBorderColor(IndexedColors.RED.getIndex());

        //自动换行
        cellStyle.setWrapText(true);
        //设置水平居中
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        //设置垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return cellStyle;
    }
}
