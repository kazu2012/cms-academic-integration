package org.fenixedu.cms.domain;

import javax.servlet.http.HttpServletRequest;

import org.fenixedu.bennu.cms.domain.ComponentType;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.rendering.TemplateContext;

@ComponentType(type = "contentSearch", name = "ContentSearch", description = "Search content of an Execution Course")
public class ExecutionCourseContentSearch extends ExecutionCourseContentSearch_Base {
    @Override
    public void handle(Page page, HttpServletRequest req, TemplateContext componentContext, TemplateContext globalContext) {
    }
}
