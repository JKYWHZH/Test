package com.example.test.service;

import com.example.test.entity.Receiver;
import com.example.test.entity.WorkInfo;
import com.example.test.utils.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "excel业务类")
public class ExcelService {

    /**
     * 导出excel
     *
     * @param receivers 接收人
     * @return excel
     */
    public Workbook export(List<Receiver> receivers) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        //异步总结，5分钟内下班次数和代打卡次数
        CompletableFuture future = asyncSummary(workbook, receivers);
        receivers
                .stream()
                //TODO: 并行流有问题
                //.parallel()
                .peek(receiver -> {
                    String name = receiver.getName();
                    XSSFSheet sheet;
                    synchronized (ExcelService.class) {
                        sheet = workbook.createSheet(name);
                    }
                    XSSFRow row = sheet.createRow(0);

                    XSSFCell cell = row.createCell(0);
                    cell.setCellValue("姓名");
                    cell.setCellStyle(ExcelUtil.titleStyle(workbook));

                    XSSFCell cell01 = row.createCell(1);
                    cell01.setCellValue("日期");
                    sheet.setColumnWidth(1, 3500);
                    cell01.setCellStyle(ExcelUtil.titleStyle(workbook));

                    XSSFCell cell02 = row.createCell(2);
                    cell02.setCellValue("考勤信息");
                    sheet.setColumnWidth(2, 3500);
                    cell02.setCellStyle(ExcelUtil.titleStyle(workbook));

                    XSSFCell cell03 = row.createCell(3);
                    sheet.setColumnWidth(3, 3500);
                    cell03.setCellStyle(ExcelUtil.titleStyle(workbook));

                    sheet.addMergedRegion(new CellRangeAddress(0, 0, 2, 3));
                    receiver.setSheet(sheet);
                })
                .forEach(receiver -> {
                    List<WorkInfo> workInfos = receiver.getWorkInfos();
                    for (int i = 1; i <= workInfos.size(); i++) {
                        Row row = receiver.getSheet().createRow(i);
                        WorkInfo workInfo = workInfos.get(i - 1);
                        List<Cell> cells = new ArrayList<>();
                        Cell cell = row.createCell(0);
                        cell.setCellValue(receiver.getName());
                        cells.add(cell);
                        Cell cell01 = row.createCell(1);
                        cell01.setCellValue(workInfo.getDate());
                        cells.add(cell01);
                        Cell cell02 = row.createCell(2);
                        cell02.setCellValue(workInfo.getWork().getInfo());
                        cells.add(cell02);
                        Cell cell03 = row.createCell(3);
                        cell03.setCellValue(workInfo.getHome().getInfo());
                        cells.add(cell03);
                        CellStyle cellStyle;
                        if (workInfo.getAns()) {
                            cellStyle = ExcelUtil.cellStyle(workbook);
                        } else {
                            cellStyle = ExcelUtil.cellErrorStyle(workbook);
                        }
                        cells.forEach(tempCell -> {
                            tempCell.setCellStyle(cellStyle);
                        });
                    }
                });
        future.join();
        return workbook;
    }


    /**
     * 异步总结
     */
    private CompletableFuture asyncSummary(Workbook workbook, List<Receiver> receivers) {
        List<Receiver> collect = receivers.stream().parallel().sorted(Comparator.comparing(Receiver::getWorryCount).reversed()).collect(Collectors.toList());

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            XSSFSheet sheet;
            synchronized (ExcelService.class) {
                sheet = (XSSFSheet) workbook.createSheet("总结");
            }
            //样式
            CellStyle cellStyle = ExcelUtil.cellStyle(workbook);
            //人员姓名
            XSSFRow username = sheet.createRow(0);
            //5分钟内下班次数
            XSSFRow count = sheet.createRow(1);
            //代打卡
            XSSFRow proxy = sheet.createRow(2);

            for (int i = 0; i < collect.size(); i++) {
                Receiver receiver = collect.get(i);
                //姓名单元
                XSSFCell usernameCell = username.createCell(i);
                usernameCell.setCellStyle(cellStyle);
                usernameCell.setCellValue(receiver.getName());
                //5分钟内下班单元
                XSSFCell countCell = count.createCell(i);
                countCell.setCellStyle(cellStyle);
                countCell.setCellValue(receiver.getWorryCount());
                //代打卡单元
                XSSFCell proxyCell = proxy.createCell(i);
                proxyCell.setCellStyle(cellStyle);
                proxyCell.setCellValue(receiver.getProxyClockCount());
            }

            int size = collect.size();
            //创建画布
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = (XSSFClientAnchor) drawing.createAnchor(0, 0, 0, 0, 0, 3, size, 26);
            //创建一个chart对象
            XSSFChart chart = drawing.createChart(anchor);
            //标题
            chart.setTitleText("5分钟内下班及代打卡统计");
            //标题覆盖
            chart.setTitleOverlay(false);
            XDDFChartLegend legend = chart.getOrAddLegend();
            legend.setPosition(LegendPosition.TOP);
            //分类轴标（X轴），标题位置
            XDDFCategoryAxis x = chart.createCategoryAxis(AxisPosition.BOTTOM);
            x.setTitle("姓名");
            //值轴（y轴），标题位置
            XDDFValueAxis y = chart.createValueAxis(AxisPosition.LEFT);
            y.setTitle("次数");
            //分类轴标（X轴）数据，单元格范围位置[1,0]到[1,6]
            XDDFDataSource<String> xData = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(0, 0, 0, size - 1));
            //final XDDFCategoryDataSource xddfCategoryDataSource = XDDFDataSourcesFactory.fromArray(nre String[]{})
            XDDFNumericalDataSource<Double> area = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(1, 1, 0, size - 1));
            XDDFNumericalDataSource<Double> proxyCount = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(2, 2, 0, size - 1));
            //bar 条形图
            XDDFBarChartData bar = (XDDFBarChartData) chart.createData(ChartTypes.BAR, x, y);
            y.setCrossBetween(AxisCrossBetween.BETWEEN);
            //设置为可变颜色
            bar.setVaryColors(false);
            //条形图方向，纵向/横向：纵向
            bar.setBarDirection(BarDirection.COL);
            //图标加载数据，条形图
            XDDFBarChartData.Series series01 = (XDDFBarChartData.Series) bar.addSeries(xData, area);
            series01.setTitle("5分钟内下班次数", null);
            //XDDFSolidFillProperties fill = new XDDFSolidFillProperties(XDDFColor.from(PresetColor.RED));
            //series.setShapeProperties(fill);
            XDDFBarChartData.Series series02 = (XDDFBarChartData.Series) bar.addSeries(xData, proxyCount);
            series02.setTitle("代打卡次数", null);
            CTPlotArea plotArea = chart.getCTChart().getPlotArea();
            //柱形图1显示数字
            plotArea.getBarChartArray(0).getSerArray(0).addNewDLbls();
            plotArea.getBarChartArray(0).getSerArray(0).getDLbls().addNewShowVal().setVal(true);
            plotArea.getBarChartArray(0).getSerArray(0).getDLbls().addNewShowLegendKey().setVal(false);
            plotArea.getBarChartArray(0).getSerArray(0).getDLbls().addNewShowCatName().setVal(false);
            plotArea.getBarChartArray(0).getSerArray(0).getDLbls().addNewShowSerName().setVal(false);
            //柱形图2显示数字
            plotArea.getBarChartArray(0).getSerArray(1).addNewDLbls();
            plotArea.getBarChartArray(0).getSerArray(1).getDLbls().addNewShowVal().setVal(true);
            plotArea.getBarChartArray(0).getSerArray(1).getDLbls().addNewShowLegendKey().setVal(false);
            plotArea.getBarChartArray(0).getSerArray(1).getDLbls().addNewShowCatName().setVal(false);
            plotArea.getBarChartArray(0).getSerArray(1).getDLbls().addNewShowSerName().setVal(false);
            chart.plot(bar);
        }, Executors.newSingleThreadExecutor());
        return future;
    }

}
