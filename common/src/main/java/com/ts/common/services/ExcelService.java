package com.ts.common.services;

import com.ts.common.services.models.TaskFromExcelModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExcelService {
    public static List<TaskFromExcelModel> readFromExcel(String file) throws IOException {
        var tasks = new ArrayList<TaskFromExcelModel>();
        DataFormatter dataFormatter = new DataFormatter();
        XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(file));
        XSSFSheet sheet = myExcelBook.getSheetAt(0);
        for (int rowIndex = 3; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null && !dataFormatter.formatCellValue(row.getCell(1)).isEmpty()) {
                var task = TaskFromExcelModel.builder()
                        .name(dataFormatter.formatCellValue(row.getCell(1)))
                        .description(dataFormatter.formatCellValue(row.getCell(2)))
                        .creator(dataFormatter.formatCellValue(row.getCell(3)))
                        .handler(dataFormatter.formatCellValue(row.getCell(4)))
                        .UDF_WATCHER(dataFormatter.formatCellValue(row.getCell(5)))
                        .UDF_CDP_BL(dataFormatter.formatCellValue(row.getCell(6)))
                        .UDF_SD_MODULE(dataFormatter.formatCellValue(row.getCell(7)))
                        .UDF_MIS_SERVICE(dataFormatter.formatCellValue(row.getCell(8)))
                        .priority(dataFormatter.formatCellValue(row.getCell(9)))
                        .UDF_WORKTASK_ANALYSIS(dataFormatter.formatCellValue(row.getCell(10)))
                        .UDF_BDKU_CONFIGURATION(dataFormatter.formatCellValue(row.getCell(11)))
                        .UDF_SD_LINKEDREQUEST(dataFormatter.formatCellValue(row.getCell(12)))
                        .clientWatcher(dataFormatter.formatCellValue(row.getCell(13)))
                        .tag(dataFormatter.formatCellValue(row.getCell(14)))
                        .status(dataFormatter.formatCellValue(row.getCell(15)))
//                        .file(dataFormatter.formatCellValue(row.getCell(15)))
                        .build();
                tasks.add(task);
            }
        }
        myExcelBook.close();
        return tasks;
    }
}
