package com.xz.scorep.executor.exportexcel.impl.total;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.utils.Direction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TotalDistributionSheet0 extends SheetGenerator {

    @Autowired
    private DAOFactory daoFactory;

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        sheetContext.headerPut("科目", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("学校名称", 2, 1);

        sheetContext.tableSetKey("school_id");
        sheetContext.columnSet(0, "subject_name");
        sheetContext.columnSet(1, "school_name");

        String projectId = sheetContext.getProjectId();
        DAO dao = daoFactory.getProjectDao(projectId);

        List<Row> rows = dao.query("select " +
                "  id as school_id, " +
                "  '总分' as subject_name, " +
                "  name as school_name " +
                "from school");

        sheetContext.rowAdd(rows);

        Row totalRow = new Row();
        totalRow.put("school_id", "z");
        totalRow.put("subject_name", "总分");
        totalRow.put("school_name", "总计");
        sheetContext.rowAdd(totalRow);

        sheetContext.saveData();
    }
}
