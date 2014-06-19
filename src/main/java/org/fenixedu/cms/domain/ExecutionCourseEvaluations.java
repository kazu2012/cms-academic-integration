package org.fenixedu.cms.domain;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.fenixedu.domain.Exam;
import net.sourceforge.fenixedu.domain.ExecutionCourse;

import org.fenixedu.bennu.cms.domain.ComponentType;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;

@ComponentType(type = "evaluations", name = "Evaluations", description = "Evaluations for an Execution Course")
public class ExecutionCourseEvaluations extends ExecutionCourseEvaluations_Base {

    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext componentContext, TemplateContext globalContext) {
        ExecutionCourse executionCourse = ((ExecutionCourseSite) page.getSite()).getExecutionCourse();
        globalContext.put("evaluations", executionCourse.getAssociatedEvaluationsSet());
        globalContext.put("comment", executionCourse.getComment());
        globalContext.put("onlineTests", executionCourse.getAssociatedOnlineTests());
        globalContext.put("adHocEvaluations", executionCourse.getOrderedAssociatedAdHocEvaluations());
        globalContext.put("projects", executionCourse.getAssociatedProjects());
        globalContext.put("publishedExams", publishedExams(executionCourse));
        globalContext.put("writtenTests", executionCourse.getAssociatedWrittenTests());
    }

    private List<Exam> publishedExams(ExecutionCourse executionCourse) {
        return executionCourse.getAssociatedExams().stream().filter(e -> e.isExamsMapPublished()).collect(Collectors.toList());
    }
    

}
