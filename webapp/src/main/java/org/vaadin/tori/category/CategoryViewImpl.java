package org.vaadin.tori.category;

import java.util.List;

import org.vaadin.tori.ToriApplication;
import org.vaadin.tori.component.CategoryListing;
import org.vaadin.tori.component.HeadingLabel;
import org.vaadin.tori.component.HeadingLabel.HeadingLevel;
import org.vaadin.tori.component.thread.ThreadListing;
import org.vaadin.tori.data.entity.Category;
import org.vaadin.tori.data.entity.Thread;
import org.vaadin.tori.mvp.AbstractView;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class CategoryViewImpl extends
        AbstractView<CategoryView, CategoryPresenter> implements CategoryView {

    private VerticalLayout layout;
    private ThreadListing threadListing;
    private CategoryListing categoryListing;
    private VerticalLayout categoryLayout;

    @Override
    protected Component createCompositionRoot() {
        return layout = new VerticalLayout();
    }

    @Override
    public void initView() {
        categoryLayout = new VerticalLayout();
        categoryLayout.addComponent(new HeadingLabel("Contained Categories",
                HeadingLevel.H2));
        categoryLayout.addComponent(categoryListing = new CategoryListing());
        layout.addComponent(categoryLayout);

        layout.addComponent(new HeadingLabel("Threads", HeadingLevel.H2));
        layout.addComponent(threadListing = new ThreadListing());
    }

    @Override
    protected CategoryPresenter createPresenter() {
        return new CategoryPresenter(ToriApplication.getCurrent()
                .getDataSource());
    }

    @Override
    public void displaySubCategories(final List<Category> subCategories) {
        // show contained categories only if there are any
        categoryLayout.setVisible(!subCategories.isEmpty());

        categoryListing.setCategories(subCategories);
    }

    @Override
    public void displayThreads(final List<Thread> threadsInCategory) {
        threadListing.setThreads(threadsInCategory);
    }

    @Override
    public void navigateTo(final String requestedDataId) {
        super.navigateTo(requestedDataId);
        super.getPresenter().setCurrentCategoryById(requestedDataId);
    }

    @Override
    public void displayCategoryNotFoundError(final String requestedCategoryId) {
        getWindow().showNotification(
                "No category found for " + requestedCategoryId,
                Notification.TYPE_ERROR_MESSAGE);
    }

}