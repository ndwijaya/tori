/*
 * Copyright 2012 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.tori.component;

import java.util.List;

import org.apache.log4j.Logger;
import org.vaadin.tori.ToriApiLoader;
import org.vaadin.tori.ToriNavigator;
import org.vaadin.tori.ToriScheduler;
import org.vaadin.tori.ToriScheduler.ScheduledCommand;
import org.vaadin.tori.data.DataSource;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.DiscussionThread;
import org.vaadin.tori.exception.DataSourceException;
import org.vaadin.tori.exception.NoSuchThreadException;
import org.vaadin.tori.mvp.AbstractView;
import org.vaadin.tori.service.AuthorizationService;
import org.vaadin.tori.view.listing.ListingView;
import org.vaadin.tori.view.thread.ThreadView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class Breadcrumbs extends CustomComponent implements ViewChangeListener {

    static final String STYLE_CRUMB = "crumb";
    static final String STYLE_THREAD = "thread";
    static final String STYLE_CATEGORY = "category";

    private HorizontalLayout crumbsLayout;
    private Label viewCaption;
    private Component myPosts;
    private final DataSource dataSource = ToriApiLoader.getCurrent()
            .getDataSource();
    private final String pageTitlePrefix = dataSource.getPageTitlePrefix();
    private final AuthorizationService authorizationService = ToriApiLoader
            .getCurrent().getAuthorizationService();

    public Breadcrumbs() {
        setStyleName("breadcrumbs");
        ToriNavigator.getCurrent().addViewChangeListener(this);

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addComponent(buildCrumbsLayout());
        mainLayout.addComponent(buildCaptionLayout());
        setCompositionRoot(mainLayout);
    }

    private Component buildCaptionLayout() {
        viewCaption = new Label("");
        viewCaption.addStyleName("viewcaption");

        myPosts = new Label("My Posts");

        final HorizontalLayout captionLayout = new HorizontalLayout(
                viewCaption, myPosts);
        captionLayout.setWidth(100.0f, Unit.PERCENTAGE);
        captionLayout.setExpandRatio(viewCaption, 1.0f);
        return captionLayout;
    }

    private Component buildCrumbsLayout() {
        crumbsLayout = new HorizontalLayout();
        crumbsLayout.setStyleName("breadcrumbs-layout");
        return crumbsLayout;
    }

    @Override
    public void afterViewChange(ViewChangeEvent event) {
        viewCaption.setValue(null);

        final View view = event.getNewView();

        if (view instanceof AbstractView) {
            String viewTitle = ((AbstractView) view).getTitle();
            final Long urlParameterId = ((AbstractView) view)
                    .getUrlParameterId();
            if (urlParameterId == null) {
                crumbsLayout.removeAllComponents();
                viewCaption.setValue(getDashboardTitle());
            } else {
                viewCaption.setValue(viewTitle);
                ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        crumbsLayout.removeAllComponents();
                        buildCrumbs(view, urlParameterId);
                    }
                });
            }
        }

    }

    private void buildCrumbs(View view, Long urlParameterId) {
        Category parentCategory = null;
        if (view instanceof ThreadView) {
            try {
                DiscussionThread thread = dataSource.getThread(urlParameterId);
                parentCategory = thread.getCategory();
            } catch (NoSuchThreadException e) {
                e.printStackTrace();
            } catch (DataSourceException e) {
                e.printStackTrace();
            }
        } else if (view instanceof ListingView) {
            try {
                Category category = dataSource.getCategory(urlParameterId);
                parentCategory = category.getParentCategory();
            } catch (DataSourceException e) {
                e.printStackTrace();
            }
        }

        prependLink(parentCategory);
    }

    private void prependLink(Category category) {
        if (category == null) {
            crumbsLayout.addComponent(getDashboardLink(), 0);
        } else {
            crumbsLayout.addComponent(getCategoryLink(category), 0);
            prependLink(category.getParentCategory());
        }
    }

    private Component getDashboardLink() {
        return new Link("Dashboard", new ExternalResource("#"
                + ToriNavigator.ApplicationView.DASHBOARD.getUrl()));
    }

    private String getDashboardTitle() {
        return pageTitlePrefix != null ? pageTitlePrefix : "Tori";
    }

    private Component getCategoryLink(final Category category) {
        HorizontalLayout result = new HorizontalLayout();
        final Link crumb = new Link(category.getName(), new ExternalResource(
                "#" + ToriNavigator.ApplicationView.CATEGORIES.getUrl() + "/"
                        + category.getId()));
        result.addComponent(crumb);
        result.addComponent(getSiblingMenuBar(category));
        return result;
    }

    private Component getSiblingMenuBar(final Category category) {
        final MenuBar menuBar = new MenuBar();
        final MenuItem topItem = menuBar.addItem("", null);
        // Lazily populate the menubar
        ToriScheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                if (!topItem.hasChildren()) {
                    populateSiblingMenu(topItem, category);
                }
            }
        });
        return menuBar;
    }

    protected void populateSiblingMenu(MenuItem topItem, Category category) {
        try {
            Category parent = category.getParentCategory();
            Long parentId = parent != null ? parent.getId() : null;
            final List<Category> siblings = dataSource
                    .getSubCategories(parentId);
            for (final Category sibling : siblings) {
                if (authorizationService.mayViewCategory(sibling.getId())) {
                    topItem.addItem(sibling.getName(), new Command() {
                        @Override
                        public void menuSelected(MenuItem selectedItem) {
                            ToriNavigator.getCurrent().navigateToCategory(
                                    sibling.getId());
                        }
                    });
                }
            }
        } catch (final DataSourceException e) {
            getLogger().error(e);
            e.printStackTrace();
        }
    }

    private static Logger getLogger() {
        return Logger.getLogger(Breadcrumbs.class);
    }

    @Override
    public boolean beforeViewChange(ViewChangeEvent event) {
        return true;
    }
}
