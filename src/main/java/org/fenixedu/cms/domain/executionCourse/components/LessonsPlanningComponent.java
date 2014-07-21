package org.fenixedu.cms.domain.executionCourse.components;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.LessonPlanning;
import net.sourceforge.fenixedu.domain.ShiftType;

import org.fenixedu.bennu.cms.domain.ComponentType;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;
import org.fenixedu.cms.domain.executionCourse.ExecutionCourseSite;

@ComponentType(type = "lessonsPlaning", name = "LessonsPlanning", description = "Lessons planing for an Execution Course")
public class LessonsPlanningComponent extends LessonsPlanningComponent_Base {

    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        Map<ShiftType, List<LessonPlanning>> lessonPlanningsMap = new TreeMap<ShiftType, List<LessonPlanning>>();
        if (executionCourse.getSite().getLessonPlanningAvailable() != null
                && executionCourse.getSite().getLessonPlanningAvailable()) {
            for (ShiftType shiftType : executionCourse.getShiftTypes()) {
                List<LessonPlanning> lessonPlanningsOrderedByOrder = executionCourse.getLessonPlanningsOrderedByOrder(shiftType);
                if (!lessonPlanningsOrderedByOrder.isEmpty()) {
                    lessonPlanningsMap.put(shiftType, lessonPlanningsOrderedByOrder);
                }
            }
        }
        globalContext.put("lessonPlanningsMap", lessonPlanningsMap);
    }
    
    
}