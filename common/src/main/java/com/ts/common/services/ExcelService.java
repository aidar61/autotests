package com.ts.common.services;

import com.ts.common.services.models.TaskFromExcelModel;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelService {
    public static List<TaskFromExcelModel> readFromExcel(String file) throws IOException {
        var tasks = new ArrayList<TaskFromExcelModel>();
        DataFormatter dataFormatter = new DataFormatter();
        XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(file));
        XSSFSheet sheet = myExcelBook.getSheetAt(0);
        for (int rowIndex = 3; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null && !dataFormatter.formatCellValue(row.getCell(1)).isEmpty()) {
                var name = dataFormatter.formatCellValue(row.getCell(1));
                var description = dataFormatter.formatCellValue(row.getCell(2));
                var creator = dataFormatter.formatCellValue(row.getCell(3));
                var handler = dataFormatter.formatCellValue(row.getCell(4));
                var watcher = dataFormatter.formatCellValue(row.getCell(5));
                var cdpBl = dataFormatter.formatCellValue(row.getCell(6));
                var sdModule = dataFormatter.formatCellValue(row.getCell(7));
                var misService = dataFormatter.formatCellValue(row.getCell(8));
                var priority = dataFormatter.formatCellValue(row.getCell(9));
                var preliminaryAnalysis = dataFormatter.formatCellValue(row.getCell(10));
                var bdku = dataFormatter.formatCellValue(row.getCell(11));
                var task = new TaskFromExcelModel();
                task.setName(name);
                task.setDescription(description);
                task.setCreator(creator);
                task.setHandler(handler);
                task.setUDF_WATCHER(watcher);
                task.setUDF_CDP_BL(cdpBl);
                task.setUDF_SD_MODULE(sdModule);
                task.setUDF_MIS_SERVICE(misService);
                task.setUDF_BDKU_CONFIGURATION(bdku);
                task.setPriority(priority);
                task.setWorkTaskAnalysis(preliminaryAnalysis);
                tasks.add(task);
            }
        }
        myExcelBook.close();
        return tasks;
    }

    public static void main(String[] args) throws IOException {
        var res = readFromExcel("integration_tests/src/main/java/resources/tasks.xlsx");
        for (TaskFromExcelModel task: res)
            System.out.println(task);
    }
}
