package org.fenixedu.cms.domain;

import static org.fenixedu.bennu.core.i18n.BundleUtil.getLocalizedString;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

import net.sourceforge.fenixedu.domain.Item;
import net.sourceforge.fenixedu.domain.Section;
import net.sourceforge.fenixedu.domain.cms.CmsContent;
import net.sourceforge.fenixedu.domain.cms.TemplatedSection;
import net.sourceforge.fenixedu.domain.cms.TemplatedSectionInstance;

import org.fenixedu.bennu.cms.domain.Category;
import org.fenixedu.bennu.cms.domain.ListCategoryPosts;
import org.fenixedu.bennu.cms.domain.Menu;
import org.fenixedu.bennu.cms.domain.MenuComponent;
import org.fenixedu.bennu.cms.domain.MenuItem;
import org.fenixedu.bennu.cms.domain.Page;
import org.fenixedu.bennu.cms.domain.Post;
import org.fenixedu.bennu.cms.domain.SideMenuComponent;
import org.fenixedu.bennu.cms.domain.Site;
import org.fenixedu.bennu.cms.domain.TopMenuComponent;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.i18n.LocalizedString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class MigrationUtils {

    private static final Logger log = LoggerFactory.getLogger(MigrationUtils.class);

    private static final LocalizedString TOP_MENU = getLocalizedString("resources.FenixEduCMSResources", "label.topMenu");
    public static final LocalizedString MENU = getLocalizedString("resources.FenixEduCMSResources", "label.menu");

    private static Predicate<Item> hasName = i -> i.getName() != null && !i.getName().isEmpty();
    private static Predicate<Item> hasBody = i -> i.getBody() != null && !i.getBody().isEmpty();

    public static void createStaticPages(Site newSite, MenuItem menuItemParent, net.sourceforge.fenixedu.domain.Site oldSite) {
        log.info("creating static pages for site " + newSite.getSlug());
        List<Section> topMenuSections = topMenuSections(oldSite.getOrderedSections());
        List<Section> sideMenuSections = sideMenuSections(oldSite.getOrderedSections());
        Menu topMenu = new Menu(newSite, TOP_MENU);
        Menu sideMenu = new Menu(newSite, MENU);
        topMenuSections.forEach(s -> createStaticPage(newSite, topMenu, menuItemParent, s));
        sideMenuSections.forEach(s -> createStaticPage(newSite, sideMenu, menuItemParent, s));
        //assign top and side menu components to all pages5
        for (Page page : newSite.getPagesSet()) {
            if (!topMenuSections.isEmpty() && !hasMenu(page, topMenu)) {
                new TopMenuComponent(topMenu, page);
            }
            if (!sideMenuSections.isEmpty() && !hasMenu(page, sideMenu)) {
                new SideMenuComponent(sideMenu, page);
            }
        }
        if (topMenu.getComponentSet().isEmpty()) {
            topMenu.delete();
        }
        if (sideMenu.getComponentSet().isEmpty()) {
            sideMenu.delete();
        }
    }

    public static void createStaticPage(Site site, Menu menu, MenuItem menuItemParent, Section section) {
        List<Section> subsections = section.getOrderedSubSections();
        if (!isIgnoredSection(section)) {
            LocalizedString name = localized(section.getName());
            log.info("migrating section " + name.getContent());
            //it means that the folder has no content and just acts like a folder on the menu
            boolean isFolderSection = section.getOrderedChildItems().isEmpty() && section.getFileContentSet().isEmpty();
            if (isFolderSection) {
                MenuItem parent = MenuItem.create(menu, null, name, menuItemParent);
                subsections.forEach(subsection -> createStaticPage(site, menu, parent, subsection));
                return;
            } else {
                Category category = new Category();
                category.setName(name);
                ListCategoryPosts pageCategory = new ListCategoryPosts(category);

                boolean isPublished = Optional.ofNullable(section.getEnabled()).orElse(true);
                final Page page = Page.create(site, menu, menuItemParent, name, isPublished, "category", null, pageCategory);
                page.setCreationDate(site.getCreationDate());

                section.getOrderedChildItems().stream().filter(hasName.and(hasBody)).forEach(item -> {
                    boolean isEnabled = Optional.ofNullable(item.getEnabled()).orElse(true);
                    Post.create(site, page, localized(item.getName()), localized(item.getBody()), category, isEnabled, null);
                });
            }
        }
        subsections.forEach(subsection -> createStaticPage(site, menu, menuItemParent, subsection));
    }

    private static TemplatedSection getTemplatedSection(Section section) {
        if (section instanceof TemplatedSection) {
            return (TemplatedSection) section;
        } else if (section instanceof TemplatedSectionInstance) {
            return ((TemplatedSectionInstance) section).getSectionTemplate();
        }
        return null;
    }

    private static boolean hasMenu(Page page, Menu menu) {
        return menu.getComponentsOfClass(MenuComponent.class).stream().filter(m -> m.getPage().equals(page)).findAny()
                .isPresent();
    }

    private static List<Section> sideMenuSections(List<Section> sections) {
        List<Section> sideMenuSections = Lists.newArrayList();
        for (Section section : sections) {
            if (isSideSection(section)) {
                sideMenuSections.addAll(section.getChildrenSections());
            } else if (!isTopSection(section)) {
                sideMenuSections.add(section);
            }
        }
        return sideMenuSections;
    }

    private static List<Section> topMenuSections(List<Section> sections) {
        List<Section> sideMenuSections = Lists.newArrayList();
        for (Section section : sections) {
            if (isTopSection(section)) {
                sideMenuSections.addAll(section.getChildrenSections());
            }
        }
        return sideMenuSections;
    }

    private static boolean isTopSection(Section section) {
        Predicate<String> predicate = name -> "top".equalsIgnoreCase(name) || "topo".equalsIgnoreCase(name);
        return section.getName().getAllContents().stream().anyMatch(predicate);
    }

    private static boolean isSideSection(Section section) {
        Predicate<String> predicate = name -> "side".equalsIgnoreCase(name) || "lateral".equalsIgnoreCase(name);
        return section.getName().getAllContents().stream().anyMatch(predicate);
    }

    private static boolean equalContent(LocalizedString str1, LocalizedString str2) {
        return str1.getContent().equalsIgnoreCase(str2.getContent());
    }

    private static boolean isIgnoredSection(CmsContent cmsContent) {
        return cmsContent instanceof TemplatedSection || isTopSection((Section) cmsContent)
                || isSideSection((Section) cmsContent);
    }

    private static boolean isTemplatedSection(CmsContent cmsContent) {
        return cmsContent instanceof TemplatedSection || cmsContent instanceof TemplatedSectionInstance;
    }

    public static void deleteAllSites() {
        log.info("removing all sites..");
        for (Site site : Bennu.getInstance().getSitesSet()) {
            site.delete();
        }
    }

    public static LocalizedString localizedStr(String str) {
        LocalizedString result = new LocalizedString();
        if (!Strings.isNullOrEmpty(str)) {
            for (Locale locale : CoreConfiguration.supportedLocales()) {
                result = result.with(locale, str);
            }
        }
        return result;
    }

    public static LocalizedString localized(MultiLanguageString mls) {
        return mls != null ? mls.toLocalizedString() : new LocalizedString();
    }
}
